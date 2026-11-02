package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.DBConnection;

/** SCR-4.8: Reads WF-04 notifications that have a reliable dispatch audit event. */
public class BroadcastAuditDAO {

    public List<Object[]> getFieldworkNotificationAudit(int viewerUserId) throws SQLException {
        String roleSql = "SELECT r.role_name FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                + "WHERE u.user_id = ? AND u.active_status = 1";
        String sql =
            "WITH Dispatch AS ("
          + " SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + " senderLog.actor_user_id AS sender_id, senderLog.created_at AS sent_at, "
          + " 'TASK_NUDGE' AS source_type, "
          + " CASE WHEN n.message LIKE N'%KHẨN CẤP%' THEN 'EMERGENCY' "
          + " WHEN n.message LIKE N'%ƯU TIÊN CAO%' THEN 'HIGH' ELSE 'NORMAL' END AS urgency "
          + " FROM Notification n JOIN Audit_Log senderLog "
          + " ON senderLog.entity_type = 'NOTIFICATION' AND senderLog.entity_id = n.notification_id "
          + " AND senderLog.action = 'SEND_TASK_NUDGE' "
          + " UNION ALL "
          + " SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + " incidentLog.actor_user_id AS sender_id, incidentLog.created_at AS sent_at, "
          + " 'EMERGENCY_INCIDENT' AS source_type, 'EMERGENCY' AS urgency "
          + " FROM Audit_Log incidentLog JOIN Notification n ON n.topic_id = incidentLog.entity_id "
          + " AND LEFT(n.message, LEN(N'[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG]')) = N'[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG]' "
          + " AND CHARINDEX(SUBSTRING(incidentLog.details, "
          + " LEN(N'BÁO CÁO SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG: ') + 1, 4000), n.message) > 0 "
          + " WHERE incidentLog.action = 'EMERGENCY_INCIDENT' AND incidentLog.entity_type = 'TOPIC'"
          + " UNION ALL "
          + " SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + " assignmentLog.actor_user_id AS sender_id, assignmentLog.created_at AS sent_at, "
          + " 'TASK_ASSIGNMENT' AS source_type, 'NORMAL' AS urgency "
          + " FROM Notification n JOIN Audit_Log assignmentLog "
          + " ON assignmentLog.entity_type = 'NOTIFICATION' AND assignmentLog.entity_id = n.notification_id "
          + " AND assignmentLog.action = 'ASSIGN_FIELDWORK_TASK' "
          + ") "
          + "SELECT d.notification_id, d.recipient_id, recipient.full_name AS recipient_name, "
          + "recipient.email AS recipient_email, recipientDesk.desk_name AS recipient_desk, "
          + "d.topic_id, t.topic_code, t.title AS topic_title, d.message, d.is_read, "
          + "d.sender_id, sender.full_name AS sender_name, d.sent_at, readLog.read_at, "
          + "d.urgency, d.source_type "
          + "FROM Dispatch d JOIN [User] sender ON sender.user_id = d.sender_id "
          + "JOIN [User] recipient ON recipient.user_id = d.recipient_id "
          + "LEFT JOIN Desk recipientDesk ON recipientDesk.desk_id = recipient.desk_id "
          + "LEFT JOIN Topic t ON t.topic_id = d.topic_id "
          + "LEFT JOIN (SELECT entity_id, actor_user_id, MAX(created_at) AS read_at "
          + "FROM Audit_Log WHERE action = 'READ_NOTIFICATION' AND entity_type = 'NOTIFICATION' "
          + "GROUP BY entity_id, actor_user_id) readLog "
          + "ON readLog.entity_id = d.notification_id AND readLog.actor_user_id = d.recipient_id "
          + "ORDER BY d.sent_at DESC, d.notification_id DESC";

        try (Connection connection = DBConnection.getConnection()) {
            try (PreparedStatement roleStatement = connection.prepareStatement(roleSql)) {
                roleStatement.setInt(1, viewerUserId);
                try (ResultSet roleResult = roleStatement.executeQuery()) {
                    if (!roleResult.next()) throw new IllegalArgumentException("Tài khoản không hợp lệ hoặc đã bị khóa.");
                    String role = roleResult.getString(1);
                    if (!"FIELDWORK_COORDINATOR".equals(role) && !"SYSTEM_ADMINISTRATOR".equals(role)) {
                        throw new IllegalArgumentException("Bạn không có quyền xem nhật ký WF-04.");
                    }
                }
            }

            List<Object[]> rows = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("notification_id"), rs.getInt("recipient_id"),
                        rs.getString("recipient_name"), rs.getString("recipient_email"),
                        rs.getString("recipient_desk"),
                        (Integer) (rs.getObject("topic_id") == null ? null : rs.getInt("topic_id")),
                        rs.getString("topic_code"), rs.getString("topic_title"),
                        rs.getString("message"), rs.getBoolean("is_read"),
                        rs.getInt("sender_id"), rs.getString("sender_name"),
                        rs.getString("sent_at"), rs.getString("read_at"),
                        rs.getString("urgency"), rs.getString("source_type")
                    });
                }
            }
            return rows;
        }
    }

    public void pingUnreadRecipient(int actorUserId, int originalNotificationId, int recipientUserId) throws SQLException {
        String actorSql = "SELECT r.role_name FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                + "WHERE u.user_id = ? AND u.active_status = 1";
        String originalSql = "SELECT topic_id, message, is_read FROM Notification "
                + "WHERE notification_id = ? AND user_id = ?";
        String insertNotification = "INSERT INTO Notification (user_id, topic_id, message) VALUES (?, ?, ?)";
        String insertAudit = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                + "VALUES (?, 'PING_UNREAD_NOTIFICATION', 'NOTIFICATION', ?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            boolean oldAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String role = null;
                try (PreparedStatement ps = connection.prepareStatement(actorSql)) {
                    ps.setInt(1, actorUserId);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) role = rs.getString(1); }
                }
                if (!"FIELDWORK_COORDINATOR".equals(role) && !"SYSTEM_ADMINISTRATOR".equals(role)) {
                    throw new IllegalArgumentException("Bạn không có quyền Ping người nhận.");
                }

                Integer topicId = null;
                String originalMessage = null;
                try (PreparedStatement ps = connection.prepareStatement(originalSql)) {
                    ps.setInt(1, originalNotificationId);
                    ps.setInt(2, recipientUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy người nhận của thông báo.");
                        if (rs.getBoolean("is_read")) throw new IllegalArgumentException("Người nhận đã đọc thông báo, không cần Ping.");
                        int value = rs.getInt("topic_id");
                        topicId = rs.wasNull() ? null : value;
                        originalMessage = rs.getString("message");
                    }
                }

                String pingMessage = "[NHẮC ĐỌC THÔNG BÁO] Bạn có một thông báo chưa xác nhận đã đọc: "
                        + (originalMessage == null ? "" : originalMessage);
                int pingNotificationId;
                try (PreparedStatement ps = connection.prepareStatement(insertNotification, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, recipientUserId);
                    if (topicId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, topicId);
                    ps.setString(3, pingMessage);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được mã thông báo Ping.");
                        pingNotificationId = keys.getInt(1);
                    }
                }
                String details = "originalNotificationId=" + originalNotificationId + "; recipientUserId="
                        + recipientUserId + "; pingNotificationId=" + pingNotificationId;
                try (PreparedStatement ps = connection.prepareStatement(insertAudit)) {
                    ps.setInt(1, actorUserId);
                    ps.setInt(2, pingNotificationId);
                    ps.setString(3, details);
                    ps.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                try { connection.setAutoCommit(oldAutoCommit); } catch (SQLException ignored) { }
            }
        }
    }

}
