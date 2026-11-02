package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Notification;
import util.DBConnection;

public class NotificationDAO {

    public List<Notification> getByUserId(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, topic_id, message, is_read "
                   + "FROM Notification WHERE user_id = ? ORDER BY notification_id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int topicId = rs.getInt("topic_id");
                    Integer nullableTopicId = rs.wasNull() ? null : topicId;
                    notifications.add(new Notification(rs.getInt("notification_id"),
                        rs.getInt("user_id"), nullableTopicId, rs.getString("message"),
                        rs.getBoolean("is_read"), ""));
                }
            }
        }
        return notifications;
    }

    public boolean markOneAsRead(int notificationId, int userId) throws SQLException {
        String updateSql = "UPDATE Notification SET is_read = 1 "
                         + "WHERE notification_id = ? AND user_id = ? AND is_read = 0";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'READ_NOTIFICATION', 'NOTIFICATION', ?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setInt(1, notificationId);
                    statement.setInt(2, userId);
                    updated = statement.executeUpdate();
                }
                if (updated > 0) {
                    try (PreparedStatement audit = connection.prepareStatement(auditSql)) {
                        audit.setInt(1, userId);
                        audit.setInt(2, notificationId);
                        audit.setString(3, "Recipient confirmed notification as read.");
                        audit.executeUpdate();
                    }
                }
                connection.commit();
                return updated > 0;
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) { }
            }
        }
    }

    public int markAllAsRead(int userId) throws SQLException {
        String selectSql = "SELECT notification_id FROM Notification WHERE user_id = ? AND is_read = 0";
        String updateSql = "UPDATE Notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'READ_NOTIFICATION', 'NOTIFICATION', ?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                List<Integer> unreadIds = new ArrayList<>();
                try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                    select.setInt(1, userId);
                    try (ResultSet rs = select.executeQuery()) {
                        while (rs.next()) unreadIds.add(rs.getInt(1));
                    }
                }
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setInt(1, userId);
                    update.executeUpdate();
                }
                try (PreparedStatement audit = connection.prepareStatement(auditSql)) {
                    for (Integer notificationId : unreadIds) {
                        audit.setInt(1, userId);
                        audit.setInt(2, notificationId);
                        audit.setString(3, "Recipient confirmed notification as read (mark all).");
                        audit.addBatch();
                    }
                    if (!unreadIds.isEmpty()) audit.executeBatch();
                }
                connection.commit();
                return unreadIds.size();
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) { }
            }
        }
    }
}
