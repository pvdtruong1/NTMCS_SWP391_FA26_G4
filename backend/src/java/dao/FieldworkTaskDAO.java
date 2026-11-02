package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.FieldworkTask;
import util.DBConnection;

public class FieldworkTaskDAO {

    public List<FieldworkTask> getAllTasks() throws SQLException {
        List<FieldworkTask> tasks = new ArrayList<>();

        String sql = "SELECT task_id, topic_id, assignee_user_id, "
                   + "task_status, description "
                   + "FROM Fieldwork_Task "
                   + "ORDER BY task_id";

        try (Connection connection = DBConnection.getConnection();
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
                tasks.add(task);
            }
        }
        return tasks;
    }

    public boolean updateTaskStatus(int taskId, String status)
            throws SQLException {

        if (!"TODO".equals(status)
                && !"IN_PROGRESS".equals(status)
                && !"DONE".equals(status)) {
            throw new IllegalArgumentException(
                "Trạng thái nhiệm vụ không hợp lệ."
            );
        }

        String sql = "UPDATE Fieldwork_Task "
                   + "SET task_status = ? "
                   + "WHERE task_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, taskId);
            return statement.executeUpdate() > 0;
        }
    }

    public int createTask(int actorUserId, int topicId, int assigneeUserId, String description)
            throws SQLException {

        String taskSql = "INSERT INTO Fieldwork_Task "
                       + "(topic_id, assignee_user_id, task_status, description) "
                       + "VALUES (?, ?, 'TODO', ?)";
        String notificationSql = "INSERT INTO Notification "
                               + "(user_id, topic_id, message) "
                               + "VALUES (?, ?, ?)";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'ASSIGN_FIELDWORK_TASK', 'NOTIFICATION', ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int taskId;
                try (PreparedStatement taskStatement = connection.prepareStatement(
                        taskSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    taskStatement.setInt(1, topicId);
                    taskStatement.setInt(2, assigneeUserId);
                    taskStatement.setString(3, description);

                    int affectedRows = taskStatement.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Tạo nhiệm vụ thất bại.");
                    }

                    try (ResultSet generatedKeys = taskStatement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Không lấy được task_id vừa tạo.");
                        }
                        taskId = generatedKeys.getInt(1);
                    }
                }

                String message = "Bạn được giao nhiệm vụ mới cho đề tài #"
                               + topicId + ": " + description;
                int notificationId;
                try (PreparedStatement notificationStatement =
                        connection.prepareStatement(notificationSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    notificationStatement.setInt(1, assigneeUserId);
                    notificationStatement.setInt(2, topicId);
                    notificationStatement.setString(3, message);
                    notificationStatement.executeUpdate();
                    try (ResultSet keys = notificationStatement.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được notification_id vừa tạo.");
                        notificationId = keys.getInt(1);
                    }
                }

                try (PreparedStatement auditStatement = connection.prepareStatement(auditSql)) {
                    auditStatement.setInt(1, actorUserId);
                    auditStatement.setInt(2, notificationId);
                    auditStatement.setString(3, "taskId=" + taskId + "; topicId=" + topicId
                            + "; recipientUserId=" + assigneeUserId);
                    auditStatement.executeUpdate();
                }

                connection.commit();
                return taskId;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                    // Connection is closing; preserve the original operation result.
                }
            }
        }
    }
}
