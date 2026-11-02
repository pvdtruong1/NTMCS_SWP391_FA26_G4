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

        String taskSql = "SELECT topic_id, assignee_user_id FROM Fieldwork_Task WHERE task_id = ?";
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

                try (PreparedStatement taskStatement = connection.prepareStatement(taskSql)) {
                    taskStatement.setInt(1, taskId);
                    try (ResultSet rs = taskStatement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy đầu việc #" + taskId);
                        }
                        topicId = rs.getInt("topic_id");
                        assigneeUserId = rs.getInt("assignee_user_id");
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
}
