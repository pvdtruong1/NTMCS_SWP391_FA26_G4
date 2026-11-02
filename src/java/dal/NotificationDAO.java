package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Notification;

/**
 * Data Access Object for Notifications, extending DBContext with Connection Pool support.
 */
public class NotificationDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Retrieve notifications for a specific user.
     */
    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        if (userId <= 0) {
            return list;
        }

        String sql = "SELECT n.notification_id, n.user_id, n.topic_id, n.message, n.is_read "
                   + "FROM Notification n "
                   + "WHERE n.user_id = ? "
                   + "ORDER BY n.is_read ASC, n.notification_id DESC";

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
                        if (!rs.wasNull()) {
                            n.setTopicId(topicId);
                        }

                        n.setMessage(rs.getString("message"));
                        n.setRead(rs.getBoolean("is_read"));
                        n.setCreatedAt(LocalDateTime.now().format(DATE_FORMATTER));

                        list.add(n);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách thông báo của user " + userId, e);
        } finally {
            close();
        }
        return list;
    }

    public List<Notification> getByUserId(int userId) {
        return findByUserId(userId);
    }

    /**
     * Count unread notifications for a specific user.
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
     * Mark all notifications as read for a specific user.
     */
    public boolean markAllAsRead(int userId) {
        if (userId <= 0) {
            return false;
        }

        String sql = "UPDATE Notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                return ps.executeUpdate() >= 0;
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
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
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
