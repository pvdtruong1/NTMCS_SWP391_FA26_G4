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
import model.FieldworkTask;

/**
 * Data Access Object for Fieldwork Tasks (SCR-4.1 Kanban & SCR-4.2 To-do list).
 */
public class FieldworkTaskDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(FieldworkTaskDAO.class.getName());

    public List<FieldworkTask> getAllTasks() throws SQLException {
        List<FieldworkTask> tasks = new ArrayList<>();
        String sql = "SELECT ft.task_id, ft.topic_id, ft.assignee_user_id, ft.task_status, ft.description, "
                   + "       t.topic_code, t.title AS topic_title, u.full_name AS assignee_name, u.email AS assignee_email "
                   + "FROM Fieldwork_Task ft "
                   + "JOIN Topic t ON ft.topic_id = t.topic_id "
                   + "JOIN [User] u ON ft.assignee_user_id = u.user_id "
                   + "ORDER BY ft.task_id DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                FieldworkTask task = new FieldworkTask(
                    resultSet.getInt("task_id"),
                    resultSet.getInt("topic_id"),
                    resultSet.getInt("assignee_user_id"),
                    resultSet.getString("task_status"),
                    resultSet.getString("description")
                );
                task.setTopicCode(resultSet.getString("topic_code"));
                task.setTopicTitle(resultSet.getString("topic_title"));
                task.setAssigneeName(resultSet.getString("assignee_name"));
                task.setAssigneeEmail(resultSet.getString("assignee_email"));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<FieldworkTask> getTasksByTopicId(int topicId) throws SQLException {
        List<FieldworkTask> tasks = new ArrayList<>();
        String sql = "SELECT ft.task_id, ft.topic_id, ft.assignee_user_id, ft.task_status, ft.description, "
                   + "       t.topic_code, t.title AS topic_title, u.full_name AS assignee_name, u.email AS assignee_email "
                   + "FROM Fieldwork_Task ft "
                   + "JOIN Topic t ON ft.topic_id = t.topic_id "
                   + "JOIN [User] u ON ft.assignee_user_id = u.user_id "
                   + "WHERE ft.topic_id = ? "
                   + "ORDER BY ft.task_id ASC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, topicId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    FieldworkTask task = new FieldworkTask(
                        resultSet.getInt("task_id"),
                        resultSet.getInt("topic_id"),
                        resultSet.getInt("assignee_user_id"),
                        resultSet.getString("task_status"),
                        resultSet.getString("description")
                    );
                    task.setTopicCode(resultSet.getString("topic_code"));
                    task.setTopicTitle(resultSet.getString("topic_title"));
                    task.setAssigneeName(resultSet.getString("assignee_name"));
                    task.setAssigneeEmail(resultSet.getString("assignee_email"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<FieldworkTask> getTasksByAssignee(int userId) throws SQLException {
        List<FieldworkTask> tasks = new ArrayList<>();
        String sql = "SELECT ft.task_id, ft.topic_id, ft.assignee_user_id, ft.task_status, ft.description, "
                   + "       t.topic_code, t.title AS topic_title, u.full_name AS assignee_name, u.email AS assignee_email "
                   + "FROM Fieldwork_Task ft "
                   + "JOIN Topic t ON ft.topic_id = t.topic_id "
                   + "JOIN [User] u ON ft.assignee_user_id = u.user_id "
                   + "WHERE ft.assignee_user_id = ? "
                   + "ORDER BY ft.task_id DESC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    FieldworkTask task = new FieldworkTask(
                        resultSet.getInt("task_id"),
                        resultSet.getInt("topic_id"),
                        resultSet.getInt("assignee_user_id"),
                        resultSet.getString("task_status"),
                        resultSet.getString("description")
                    );
                    task.setTopicCode(resultSet.getString("topic_code"));
                    task.setTopicTitle(resultSet.getString("topic_title"));
                    task.setAssigneeName(resultSet.getString("assignee_name"));
                    task.setAssigneeEmail(resultSet.getString("assignee_email"));
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public FieldworkTask getTaskById(int taskId) throws SQLException {
        String sql = "SELECT ft.task_id, ft.topic_id, ft.assignee_user_id, ft.task_status, ft.description, "
                   + "       t.topic_code, t.title AS topic_title, u.full_name AS assignee_name, u.email AS assignee_email "
                   + "FROM Fieldwork_Task ft "
                   + "JOIN Topic t ON ft.topic_id = t.topic_id "
                   + "JOIN [User] u ON ft.assignee_user_id = u.user_id "
                   + "WHERE ft.task_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, taskId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    FieldworkTask task = new FieldworkTask(
                        resultSet.getInt("task_id"),
                        resultSet.getInt("topic_id"),
                        resultSet.getInt("assignee_user_id"),
                        resultSet.getString("task_status"),
                        resultSet.getString("description")
                    );
                    task.setTopicCode(resultSet.getString("topic_code"));
                    task.setTopicTitle(resultSet.getString("topic_title"));
                    task.setAssigneeName(resultSet.getString("assignee_name"));
                    task.setAssigneeEmail(resultSet.getString("assignee_email"));
                    return task;
                }
            }
        }
        return null;
    }

    public boolean updateTaskStatus(int taskId, String status) throws SQLException {
        return updateTaskStatus(taskId, status, -1);
    }

    public boolean updateTaskStatus(int taskId, String status, int actorUserId) throws SQLException {
        if (!"TODO".equals(status) && !"IN_PROGRESS".equals(status) && !"DONE".equals(status)) {
            throw new IllegalArgumentException("Trạng thái nhiệm vụ không hợp lệ: " + status);
        }

        String fetchSql = "SELECT ft.topic_id, ft.assignee_user_id, ft.description, ft.task_status, "
                        + "       t.topic_code, t.title AS topic_title, u.full_name AS assignee_name "
                        + "FROM Fieldwork_Task ft "
                        + "JOIN Topic t ON ft.topic_id = t.topic_id "
                        + "LEFT JOIN [User] u ON ft.assignee_user_id = u.user_id "
                        + "WHERE ft.task_id = ?";

        String updateSql = "UPDATE Fieldwork_Task SET task_status = ? WHERE task_id = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int topicId = -1;
                int assigneeUserId = -1;
                String oldStatus = "";
                String description = "";
                String topicCode = "";
                String topicTitle = "";
                String assigneeName = "";

                try (PreparedStatement fetchPs = connection.prepareStatement(fetchSql)) {
                    fetchPs.setInt(1, taskId);
                    try (ResultSet rs = fetchPs.executeQuery()) {
                        if (rs.next()) {
                            topicId = rs.getInt("topic_id");
                            assigneeUserId = rs.getInt("assignee_user_id");
                            oldStatus = rs.getString("task_status");
                            description = rs.getString("description");
                            topicCode = rs.getString("topic_code");
                            topicTitle = rs.getString("topic_title");
                            assigneeName = rs.getString("assignee_name");
                        } else {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                try (PreparedStatement updatePs = connection.prepareStatement(updateSql)) {
                    updatePs.setString(1, status);
                    updatePs.setInt(2, taskId);
                    int updated = updatePs.executeUpdate();
                    if (updated == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                int effectiveActorId = (actorUserId > 0) ? actorUserId : assigneeUserId;

                // Ghi vết cập nhật trạng thái nhiệm vụ vào Audit_Log
                String auditTaskSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                                    + "VALUES (?, 'UPDATE_FIELDWORK_TASK_STATUS', 'TASK', ?, ?)";
                try (PreparedStatement psAudit = connection.prepareStatement(auditTaskSql)) {
                    psAudit.setInt(1, effectiveActorId);
                    psAudit.setInt(2, taskId);
                    psAudit.setString(3, "Cập nhật trạng thái nhiệm vụ #" + taskId + " từ " + oldStatus + " sang " + status);
                    psAudit.executeUpdate();
                }

                // Khi phóng viên tiếp nhận nhiệm vụ (IN_PROGRESS), bắn thông báo tới ĐPV, Desk Editor & Tác giả đề tài
                if ("IN_PROGRESS".equals(status) && !"IN_PROGRESS".equals(oldStatus)) {
                    String topicLabel = (topicCode != null && !topicCode.trim().isEmpty()) ? topicCode.trim() : ("#" + topicId);
                    String reporter = (assigneeName != null && !assigneeName.trim().isEmpty()) ? assigneeName.trim() : ("User #" + effectiveActorId);
                    String notifMsg = "[TIẾP NHẬN TÁC NGHIỆP] Phóng viên " + reporter
                                    + " đã tiếp nhận và bắt đầu thực hiện nhiệm vụ: " + (description != null ? description.trim() : "")
                                    + " (Đề tài [" + topicLabel + "] " + (topicTitle != null ? topicTitle.trim() : "") + ")";

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
                        psRel.setInt(2, effectiveActorId);
                        try (ResultSet rsRel = psRel.executeQuery()) {
                            while (rsRel.next()) {
                                recipientIds.add(rsRel.getInt("user_id"));
                            }
                        }
                    }

                    if (!recipientIds.isEmpty()) {
                        String insertNotifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
                        String insertNotifAuditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                                                   + "VALUES (?, 'SEND_TASK_ACCEPTED', 'NOTIFICATION', ?, ?)";

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
                                        psNotifAudit.setInt(1, effectiveActorId);
                                        psNotifAudit.setInt(2, notifId);
                                        psNotifAudit.setString(3, "Thông báo tiếp nhận nhiệm vụ #" + taskId + " tới user #" + recId);
                                        psNotifAudit.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public int createTask(int actorUserId, int topicId, int assigneeUserId, String description) throws SQLException {
        if (topicId <= 0 || assigneeUserId <= 0) {
            throw new IllegalArgumentException("Topic ID và Assignee User ID phải hợp lệ.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả nhiệm vụ không được để trống.");
        }

        String insertSql = "INSERT INTO Fieldwork_Task (topic_id, assignee_user_id, task_status, description) "
                         + "VALUES (?, ?, 'TODO', ?)";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'CREATE_FIELDWORK_TASK', 'TASK', ?, ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int taskId;
                try (PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, topicId);
                    ps.setInt(2, assigneeUserId);
                    ps.setString(3, description.trim());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Không lấy được task_id vừa tạo.");
                        }
                        taskId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(auditSql)) {
                    ps.setInt(1, actorUserId);
                    ps.setInt(2, taskId);
                    ps.setString(3, "Tạo nhiệm vụ hiện trường mới: " + description.trim());
                    ps.executeUpdate();
                }

                // Truy van thong tin de tai de ban thong bao In-app cho nguoi nhan viec
                String topicCode = "TOPIC-" + topicId;
                String topicTitle = "";
                String topicSql = "SELECT topic_code, title FROM Topic WHERE topic_id = ?";
                try (PreparedStatement psTopic = connection.prepareStatement(topicSql)) {
                    psTopic.setInt(1, topicId);
                    try (ResultSet rsTopic = psTopic.executeQuery()) {
                        if (rsTopic.next()) {
                            String code = rsTopic.getString("topic_code");
                            if (code != null && !code.trim().isEmpty()) topicCode = code.trim();
                            String t = rsTopic.getString("title");
                            if (t != null) topicTitle = t.trim();
                        }
                    }
                }

                String notifMsg = "[GIAO VI\u1EC6C HI\u1EC6N TR\u01AF\u1EDCNG] B\u1EA1n \u0111\u01B0\u1EE3c ph\u00E2n c\u00F4ng nhi\u1EC7m v\u1EE5 m\u1EDBi cho \u0111\u1EC1 t\u00E0i ["
                                + topicCode + "] " + topicTitle + ": " + description.trim();
                String notifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
                String notifAuditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                                     + "VALUES (?, 'SEND_TASK_ASSIGNMENT', 'NOTIFICATION', ?, ?)";

                try (PreparedStatement psNotif = connection.prepareStatement(notifSql, Statement.RETURN_GENERATED_KEYS);
                     PreparedStatement psNotifAudit = connection.prepareStatement(notifAuditSql)) {
                    psNotif.setInt(1, assigneeUserId);
                    psNotif.setInt(2, topicId);
                    psNotif.setString(3, notifMsg);
                    psNotif.executeUpdate();

                    try (ResultSet notifKeys = psNotif.getGeneratedKeys()) {
                        if (notifKeys.next()) {
                            int notifId = notifKeys.getInt(1);
                            psNotifAudit.setInt(1, actorUserId);
                            psNotifAudit.setInt(2, notifId);
                            psNotifAudit.setString(3, "Giao nhiệm vụ #" + taskId + " cho user #" + assigneeUserId);
                            psNotifAudit.executeUpdate();
                        }
                    }
                }

                connection.commit();
                return taskId;
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Object[]> getActiveApprovedTopics() throws SQLException {
        List<Object[]> topics = new ArrayList<>();
        String sql = "SELECT topic_id, topic_code, title, status FROM Topic "
                   + "WHERE status IN ('APPROVED', 'IN_PROGRESS', 'DELIVERED') "
                   + "ORDER BY topic_id DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                topics.add(new Object[]{
                    rs.getInt("topic_id"),
                    rs.getString("topic_code"),
                    rs.getString("title"),
                    rs.getString("status")
                });
            }
        }
        return topics;
    }
}
