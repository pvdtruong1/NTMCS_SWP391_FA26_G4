package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.FieldworkDeliverable;

/**
 * Data Access Object for Fieldwork Deliverables (SCR-4.3).
 */
public class FieldworkDeliverableDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(FieldworkDeliverableDAO.class.getName());

    public int submitDeliverable(
            int taskId,
            int submittedByUserId,
            String submissionType,
            String taskStatus,
            String note,
            String originalFileName,
            String storedFilePath,
            String contentType,
            long fileSizeBytes,
            String externalUrl) throws SQLException {

        if (!"FILE".equals(submissionType) && !"LINK".equals(submissionType)) {
            throw new IllegalArgumentException("Loại nộp không hợp lệ: " + submissionType);
        }
        if (!"IN_PROGRESS".equals(taskStatus) && !"DONE".equals(taskStatus)) {
            throw new IllegalArgumentException("Trạng thái nhiệm vụ không hợp lệ: " + taskStatus);
        }

        String taskSql = "SELECT ft.topic_id, ft.assignee_user_id, ft.description, "
                       + "       t.topic_code, t.title AS topic_title, u.full_name AS submitter_name "
                       + "FROM Fieldwork_Task ft "
                       + "JOIN Topic t ON ft.topic_id = t.topic_id "
                       + "LEFT JOIN [User] u ON u.user_id = ? "
                       + "WHERE ft.task_id = ?";
        String insertSql = "INSERT INTO Fieldwork_Deliverable "
                + "(task_id, submitted_by_user_id, submission_type, deliverable_note, "
                + " original_file_name, stored_file_path, content_type, file_size_bytes, external_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateTaskSql = "UPDATE Fieldwork_Task SET task_status = ? WHERE task_id = ?";
        String updateTopicSql = "UPDATE Topic SET status = 'DELIVERED' WHERE topic_id = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int topicId;
                int assigneeUserId;
                String taskDesc = "";
                String topicCode = "";
                String topicTitle = "";
                String submitterName = "";

                try (PreparedStatement taskStatement = connection.prepareStatement(taskSql)) {
                    taskStatement.setInt(1, submittedByUserId);
                    taskStatement.setInt(2, taskId);
                    try (ResultSet rs = taskStatement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy đầu việc #" + taskId);
                        }
                        topicId = rs.getInt("topic_id");
                        assigneeUserId = rs.getInt("assignee_user_id");
                        taskDesc = rs.getString("description");
                        topicCode = rs.getString("topic_code");
                        topicTitle = rs.getString("topic_title");
                        submitterName = rs.getString("submitter_name");
                    }
                }

                if (assigneeUserId != submittedByUserId) {
                    throw new IllegalArgumentException("Bạn không được giao đầu việc này.");
                }

                int deliverableId;
                try (PreparedStatement insertStatement =
                        connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                    insertStatement.setInt(1, taskId);
                    insertStatement.setInt(2, submittedByUserId);
                    insertStatement.setString(3, submissionType);
                    insertStatement.setString(4, note);
                    insertStatement.setString(5, originalFileName);
                    insertStatement.setString(6, storedFilePath);
                    insertStatement.setString(7, contentType);

                    if (fileSizeBytes >= 0) {
                        insertStatement.setLong(8, fileSizeBytes);
                    } else {
                        insertStatement.setNull(8, java.sql.Types.BIGINT);
                    }

                    insertStatement.setString(9, externalUrl);

                    if (insertStatement.executeUpdate() == 0) {
                        throw new SQLException("Không lưu được sản phẩm nộp.");
                    }

                    try (ResultSet keys = insertStatement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Không lấy được deliverable_id.");
                        }
                        deliverableId = keys.getInt(1);
                    }
                }

                try (PreparedStatement updateTask = connection.prepareStatement(updateTaskSql)) {
                    updateTask.setString(1, taskStatus);
                    updateTask.setInt(2, taskId);
                    updateTask.executeUpdate();
                }

                // If task is completed, advance topic status to DELIVERED
                if ("DONE".equals(taskStatus)) {
                    try (PreparedStatement updateTopic = connection.prepareStatement(updateTopicSql)) {
                        updateTopic.setInt(1, topicId);
                        updateTopic.executeUpdate();
                    }
                }

                // Gửi thông báo nộp sản phẩm / hoàn thành tác nghiệp tới ĐPV, Desk Editor & Tác giả đề tài
                String topicLabel = (topicCode != null && !topicCode.trim().isEmpty()) ? topicCode.trim() : ("#" + topicId);
                String reporter = (submitterName != null && !submitterName.trim().isEmpty()) ? submitterName.trim() : ("User #" + submittedByUserId);
                String deliverableSummary = (note != null && !note.trim().isEmpty()) ? note.trim() : (taskDesc != null ? taskDesc.trim() : "Sản phẩm nộp");
                
                String notifMsg = "DONE".equals(taskStatus)
                        ? ("[HOÀN THÀNH TÁC NGHIỆP] Phóng viên " + reporter + " đã hoàn thành nhiệm vụ và nộp sản phẩm cho đề tài [" + topicLabel + "] " + topicTitle + ": " + deliverableSummary)
                        : ("[NỘP SẢN PHẨM HIỆN TRƯỜNG] Phóng viên " + reporter + " đã nộp tài liệu tác nghiệp cho đề tài [" + topicLabel + "] " + topicTitle + ": " + deliverableSummary);

                String relatedManagersSql =
                    "SELECT DISTINCT u.user_id "
                  + "FROM [User] u "
                  + "JOIN Role r ON u.role_id = r.role_id "
                  + "WHERE u.active_status = 1 AND ( "
                  + "    r.role_name IN ('FIELDWORK_COORDINATOR', 'DESK_EDITOR') "
                  + "    OR u.user_id IN (SELECT author_user_id FROM Topic WHERE topic_id = ?) "
                  + ") AND u.user_id <> ?";

                java.util.Set<Integer> recipientIds = new java.util.LinkedHashSet<>();
                try (PreparedStatement psRel = connection.prepareStatement(relatedManagersSql)) {
                    psRel.setInt(1, topicId);
                    psRel.setInt(2, submittedByUserId);
                    try (ResultSet rsRel = psRel.executeQuery()) {
                        while (rsRel.next()) {
                            recipientIds.add(rsRel.getInt("user_id"));
                        }
                    }
                }

                if (!recipientIds.isEmpty()) {
                    String insertNotifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
                    String insertNotifAuditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                                               + "VALUES (?, 'SEND_TASK_COMPLETED', 'NOTIFICATION', ?, ?)";

                    try (PreparedStatement psNotif = connection.prepareStatement(insertNotifSql, Statement.RETURN_GENERATED_KEYS);
                         PreparedStatement psNotifAudit = connection.prepareStatement(insertNotifAuditSql)) {

                        for (int recId : recipientIds) {
                            psNotif.setInt(1, recId);
                            psNotif.setInt(2, topicId);
                            psNotif.setString(3, notifMsg);
                            psNotif.executeUpdate();

                            try (ResultSet keys = psNotif.getGeneratedKeys()) {
                                if (keys.next()) {
                                    int notifId = keys.getInt(1);
                                    psNotifAudit.setInt(1, submittedByUserId);
                                    psNotifAudit.setInt(2, notifId);
                                    psNotifAudit.setString(3, "Thông báo nộp sản phẩm nhiệm vụ #" + taskId + " tới user #" + recId);
                                    psNotifAudit.executeUpdate();
                                }
                            }
                        }
                    }
                }

                connection.commit();
                return deliverableId;

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<FieldworkDeliverable> getDeliverablesByTaskId(int taskId) throws SQLException {
        List<FieldworkDeliverable> list = new ArrayList<>();
        String sql = "SELECT fd.*, u.full_name AS submitter_name "
                   + "FROM Fieldwork_Deliverable fd "
                   + "JOIN [User] u ON fd.submitted_by_user_id = u.user_id "
                   + "WHERE fd.task_id = ? "
                   + "ORDER BY fd.submitted_at DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    FieldworkDeliverable d = new FieldworkDeliverable();
                    d.setDeliverableId(rs.getInt("deliverable_id"));
                    d.setTaskId(rs.getInt("task_id"));
                    d.setSubmittedByUserId(rs.getInt("submitted_by_user_id"));
                    d.setSubmissionType(rs.getString("submission_type"));
                    d.setDeliverableNote(rs.getString("deliverable_note"));
                    d.setOriginalFileName(rs.getString("original_file_name"));
                    d.setStoredFilePath(rs.getString("stored_file_path"));
                    d.setContentType(rs.getString("content_type"));

                    long size = rs.getLong("file_size_bytes");
                    d.setFileSizeBytes(rs.wasNull() ? null : size);

                    d.setExternalUrl(rs.getString("external_url"));
                    d.setSubmittedAt(rs.getTimestamp("submitted_at"));
                    d.setSubmitterName(rs.getString("submitter_name"));
                    list.add(d);
                }
            }
        }
        return list;
    }

    public List<FieldworkDeliverable> getDeliverablesByTopicId(int topicId) throws SQLException {
        List<FieldworkDeliverable> list = new ArrayList<>();
        String sql = "SELECT fd.*, u.full_name AS submitter_name "
                   + "FROM Fieldwork_Deliverable fd "
                   + "JOIN Fieldwork_Task ft ON fd.task_id = ft.task_id "
                   + "JOIN [User] u ON fd.submitted_by_user_id = u.user_id "
                   + "WHERE ft.topic_id = ? "
                   + "ORDER BY fd.submitted_at DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, topicId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    FieldworkDeliverable d = new FieldworkDeliverable();
                    d.setDeliverableId(rs.getInt("deliverable_id"));
                    d.setTaskId(rs.getInt("task_id"));
                    d.setSubmittedByUserId(rs.getInt("submitted_by_user_id"));
                    d.setSubmissionType(rs.getString("submission_type"));
                    d.setDeliverableNote(rs.getString("deliverable_note"));
                    d.setOriginalFileName(rs.getString("original_file_name"));
                    d.setStoredFilePath(rs.getString("stored_file_path"));
                    d.setContentType(rs.getString("content_type"));

                    long size = rs.getLong("file_size_bytes");
                    d.setFileSizeBytes(rs.wasNull() ? null : size);

                    d.setExternalUrl(rs.getString("external_url"));
                    d.setSubmittedAt(rs.getTimestamp("submitted_at"));
                    d.setSubmitterName(rs.getString("submitter_name"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}
