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
        if (!"TODO".equals(status) && !"IN_PROGRESS".equals(status) && !"DONE".equals(status)) {
            throw new IllegalArgumentException("Trạng thái nhiệm vụ không hợp lệ: " + status);
        }

        String sql = "UPDATE Fieldwork_Task SET task_status = ? WHERE task_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, taskId);
            boolean updated = statement.executeUpdate() > 0;
            if (updated && "IN_PROGRESS".equals(status)) {
                String syncTopicSql = "UPDATE Topic SET status = 'IN_PROGRESS' "
                                    + "WHERE topic_id = (SELECT topic_id FROM Fieldwork_Task WHERE task_id = ?) "
                                    + "  AND status = 'APPROVED';";
                try (PreparedStatement syncPs = connection.prepareStatement(syncTopicSql)) {
                    syncPs.setInt(1, taskId);
                    syncPs.executeUpdate();
                }
            }
            return updated;
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
