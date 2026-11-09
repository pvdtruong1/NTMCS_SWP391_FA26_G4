package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Notification;

/**
 * Data Access Object for In-App Notifications (Notification Hub SCR-4.5 & SCR-1.x).
 * Supports automatic compliance read timestamp audit logging for SCR-4.8.
 */
public class NotificationDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());

    public List<Notification> findByUserId(int userId) {
        return getRecentByUserId(userId, 50);
    }

    /**
     * Retrieve recent notifications for a user, sorted descending by notification_id.
     */
    public List<Notification> getRecentByUserId(int userId, int limit) {
        List<Notification> list = new ArrayList<>();
        if (userId <= 0) {
            return list;
        }

        String sql = "SELECT TOP (?) notification_id, user_id, topic_id, message, is_read "
                   + "FROM Notification "
                   + "WHERE user_id = ? "
                   + "ORDER BY notification_id DESC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit > 0 ? limit : 20);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Notification n = new Notification();
                        n.setNotificationId(rs.getInt("notification_id"));
                        n.setUserId(rs.getInt("user_id"));
                        int topicId = rs.getInt("topic_id");
                        n.setTopicId(rs.wasNull() ? null : topicId);
                        n.setMessage(rs.getString("message"));
                        n.setRead(rs.getBoolean("is_read"));
                        list.add(n);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thông báo gần đây của user " + userId, e);
        } finally {
            close();
        }
        return list;
    }

    /**
     * Retrieve unread notifications for a user.
     */
    public List<Notification> getUnreadByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        if (userId <= 0) {
            return list;
        }

        String sql = "SELECT notification_id, user_id, topic_id, message, is_read "
                   + "FROM Notification "
                   + "WHERE user_id = ? AND is_read = 0 "
                   + "ORDER BY notification_id DESC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Notification n = new Notification();
                        n.setNotificationId(rs.getInt("notification_id"));
                        n.setUserId(rs.getInt("user_id"));
                        int topicId = rs.getInt("topic_id");
                        n.setTopicId(rs.wasNull() ? null : topicId);
                        n.setMessage(rs.getString("message"));
                        n.setRead(rs.getBoolean("is_read"));
                        list.add(n);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn thông báo chưa đọc của user " + userId, e);
        } finally {
            close();
        }
        return list;
    }

    /**
     * Count unread notifications for badge icon on header.
     */
    public int countUnreadByUserId(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM Notification WHERE user_id = ? AND is_read = 0";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm thông báo chưa đọc của user " + userId, e);
        } finally {
            close();
        }
        return 0;
    }

    /**
     * Mark all notifications as read for a specific user, recording read timestamps in Audit_Log.
     */
    public boolean markAllAsRead(int userId) {
        if (userId <= 0) {
            return false;
        }

        String selectUnreadSql = "SELECT notification_id FROM Notification WHERE user_id = ? AND is_read = 0";
        String updateSql = "UPDATE Notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'READ_NOTIFICATION', 'NOTIFICATION', ?, N'Xác nhận đã đọc toàn bộ thông báo')";

        try {
            Connection conn = getConnection();
            List<Integer> unreadIds = new ArrayList<>();
            try (PreparedStatement psSelect = conn.prepareStatement(selectUnreadSql)) {
                psSelect.setInt(1, userId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    while (rs.next()) {
                        unreadIds.add(rs.getInt("notification_id"));
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, userId);
                int updatedCount = psUpdate.executeUpdate();
                if (updatedCount > 0 && !unreadIds.isEmpty()) {
                    try (PreparedStatement psAudit = conn.prepareStatement(auditSql)) {
                        for (int notifId : unreadIds) {
                            psAudit.setInt(1, userId);
                            psAudit.setInt(2, notifId);
                            psAudit.addBatch();
                        }
                        psAudit.executeBatch();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "Không thể ghi Audit_Log READ_NOTIFICATION hàng loạt cho user " + userId, ex);
                    }
                }
                return updatedCount >= 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu tất cả thông báo đã đọc cho user " + userId, e);
            return false;
        } finally {
            close();
        }
    }

    /**
     * Mark a single notification as read.
     */
    public boolean markAsRead(int notificationId) {
        if (notificationId <= 0) {
            return false;
        }

        int userId = -1;
        String findUserSql = "SELECT user_id FROM Notification WHERE notification_id = ?";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(findUserSql)) {
                ps.setInt(1, notificationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không tìm thấy user cho thông báo " + notificationId, e);
        }

        if (userId > 0) {
            return markAsRead(notificationId, userId);
        }

        String sql = "UPDATE Notification SET is_read = 1 WHERE notification_id = ?";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu đã đọc cho thông báo " + notificationId, e);
            return false;
        } finally {
            close();
        }
    }

    public boolean markAsRead(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) {
            return false;
        }

        String sql = "UPDATE Notification SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'READ_NOTIFICATION', 'NOTIFICATION', ?, N'Xác nhận đã đọc thông báo')";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.setInt(2, userId);
                boolean updated = ps.executeUpdate() > 0;
                if (updated) {
                    try (PreparedStatement auditPs = conn.prepareStatement(auditSql)) {
                        auditPs.setInt(1, userId);
                        auditPs.setInt(2, notificationId);
                        auditPs.executeUpdate();
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "Không thể ghi Audit_Log READ_NOTIFICATION cho notification " + notificationId, ex);
                    }
                }
                return updated;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu đã đọc cho thông báo " + notificationId, e);
            return false;
        } finally {
            close();
        }
    }

    public boolean markOneAsRead(int notificationId, int userId) {
        return markAsRead(notificationId, userId);
    }

    /**
     * Create a notification record.
     */
    public boolean createNotification(int userId, Integer topicId, String message) {
        if (userId <= 0 || message == null || message.trim().isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                if (topicId != null && topicId > 0) {
                    ps.setInt(2, topicId);
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                }
                ps.setString(3, message.trim());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tạo thông báo mới", e);
            return false;
        } finally {
            close();
        }
    }
}
