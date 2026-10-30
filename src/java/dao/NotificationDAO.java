package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Notification;

/**
 * Quản lý dữ liệu thông báo (Notification) đồng bộ trực tiếp với Database.
 */
public class NotificationDAO {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Lấy danh sách thông báo của người dùng theo user_id.
     */
    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        if (userId <= 0) {
            return list;
        }

        String sql = "SELECT n.notification_id, n.user_id, n.topic_id, n.message, n.is_read, t.created_date "
                   + "FROM Notification n "
                   + "LEFT JOIN Topic t ON n.topic_id = t.topic_id "
                   + "WHERE n.user_id = ? "
                   + "ORDER BY n.is_read ASC, n.notification_id DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
                    n.setIsRead(rs.getBoolean("is_read"));

                    Timestamp ts = rs.getTimestamp("created_date");
                    if (ts != null) {
                        n.setCreatedAt(ts.toLocalDateTime().format(DATE_FORMATTER));
                    } else {
                        n.setCreatedAt(LocalDateTime.now().format(DATE_FORMATTER));
                    }

                    list.add(n);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách thông báo của user " + userId, e);
        }
        return list;
    }

    /**
     * Đếm số lượng thông báo chưa đọc của người dùng.
     */
    public int countUnreadByUserId(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String sql = "SELECT COUNT(*) FROM Notification WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đếm thông báo chưa đọc của user " + userId, e);
        }
        return 0;
    }

    /**
     * Đánh dấu tất cả thông báo của người dùng là đã đọc.
     */
    public boolean markAllAsRead(int userId) {
        if (userId <= 0) {
            return false;
        }

        String sql = "UPDATE Notification SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu đã đọc tất cả cho user " + userId, e);
            return false;
        }
    }

    /**
     * Đánh dấu 1 thông báo cụ thể là đã đọc.
     */
    public boolean markAsRead(int notificationId, int userId) {
        if (notificationId <= 0 || userId <= 0) {
            return false;
        }

        String sql = "UPDATE Notification SET is_read = 1 WHERE notification_id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đánh dấu đã đọc thông báo " + notificationId, e);
            return false;
        }
    }

    /**
     * Tìm user_id theo email.
     */
    public int findUserIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return 0;
        }

        String sql = "SELECT user_id FROM [User] WHERE email = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm user_id theo email: " + email, e);
        }
        return 0;
    }
}
