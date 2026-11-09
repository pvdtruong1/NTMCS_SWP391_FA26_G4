package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Notification Broadcast Audit (SCR-4.8).
 */
public class BroadcastAuditDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(BroadcastAuditDAO.class.getName());

    public List<Object[]> getFieldworkNotificationAudit(int viewerUserId) throws SQLException {
        String roleSql = "SELECT r.role_name FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                       + "WHERE u.user_id = ? AND u.active_status = 1";

        String sql =
            "WITH Dispatch AS ("
          + "    SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + "           senderLog.actor_user_id AS sender_id, senderLog.created_at AS sent_at, "
          + "           'TASK_NUDGE' AS source_type, "
          + "           CASE WHEN n.message LIKE N'%[NHẮC VIỆC - KHẨN CẤP]%' THEN 'EMERGENCY' "
          + "                WHEN n.message LIKE N'%[NHẮC VIỆC - ƯU TIÊN CAO]%' THEN 'HIGH' "
          + "                ELSE 'NORMAL' END AS urgency "
          + "    FROM Notification n "
          + "    JOIN Audit_Log senderLog ON senderLog.entity_id = n.notification_id "
          + "                            AND senderLog.action = 'SEND_TASK_NUDGE' "
          + "    UNION ALL "
          + "    SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + "           incidentLog.actor_user_id AS sender_id, incidentLog.created_at AS sent_at, "
          + "           'EMERGENCY_INCIDENT' AS source_type, 'EMERGENCY' AS urgency "
          + "    FROM Notification n "
          + "    JOIN Audit_Log incidentLog ON incidentLog.entity_id = n.notification_id "
          + "                              AND incidentLog.action = 'SEND_INCIDENT_NOTIFICATION' "
          + "    UNION ALL "
          + "    SELECT n.notification_id, n.user_id AS recipient_id, n.topic_id, n.message, n.is_read, "
          + "           COALESCE(oa.actor_user_id, 2) AS sender_id, "
          + "           COALESCE(oa.created_at, SYSDATETIME()) AS sent_at, "
          + "           'EMERGENCY_INCIDENT' AS source_type, 'EMERGENCY' AS urgency "
          + "    FROM Notification n "
          + "    OUTER APPLY ( "
          + "        SELECT TOP 1 a.actor_user_id, a.created_at "
          + "        FROM Audit_Log a "
          + "        WHERE a.action = 'EMERGENCY_INCIDENT' AND a.entity_id = n.topic_id "
          + "        ORDER BY a.log_id DESC "
          + "    ) oa "
          + "    WHERE n.message LIKE N'%[[]SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG]%' "
          + "      AND NOT EXISTS ( "
          + "          SELECT 1 FROM Audit_Log al "
          + "          WHERE al.entity_id = n.notification_id AND al.action = 'SEND_INCIDENT_NOTIFICATION' "
          + "      ) "
          + ") "
          + "SELECT d.notification_id, d.recipient_id, recipient.full_name AS recipient_name, "
          + "       recipient.email AS recipient_email, recipientDesk.desk_name AS recipient_desk, "
          + "       d.topic_id, t.topic_code, t.title AS topic_title, d.message, d.is_read, "
          + "       d.sender_id, sender.full_name AS sender_name, d.sent_at, readLog.read_at, "
          + "       d.source_type, d.urgency "
          + "FROM Dispatch d "
          + "JOIN [User] recipient ON recipient.user_id = d.recipient_id "
          + "LEFT JOIN Desk recipientDesk ON recipientDesk.desk_id = recipient.desk_id "
          + "LEFT JOIN [User] sender ON sender.user_id = d.sender_id "
          + "LEFT JOIN Topic t ON t.topic_id = d.topic_id "
          + "LEFT JOIN (SELECT entity_id, actor_user_id, MAX(created_at) AS read_at "
          + "           FROM Audit_Log WHERE action = 'READ_NOTIFICATION' AND entity_type = 'NOTIFICATION' "
          + "           GROUP BY entity_id, actor_user_id) readLog "
          + "     ON readLog.entity_id = d.notification_id AND readLog.actor_user_id = d.recipient_id "
          + "ORDER BY d.sent_at DESC";

        try (Connection connection = getConnection()) {
            try (PreparedStatement roleStatement = connection.prepareStatement(roleSql)) {
                roleStatement.setInt(1, viewerUserId);
                try (ResultSet roleResult = roleStatement.executeQuery()) {
                    if (!roleResult.next()) {
                        throw new IllegalArgumentException("Tài khoản không hợp lệ hoặc đã bị khóa.");
                    }
                    String role = roleResult.getString(1);
                    if (!"FIELDWORK_COORDINATOR".equals(role) 
                            && !"DESK_EDITOR".equals(role)
                            && !"EXECUTIVE_EDITOR".equals(role) 
                            && !"SYSTEM_ADMINISTRATOR".equals(role)) {
                        throw new IllegalArgumentException("Bạn không có quyền xem nhật ký kiểm toán thông báo.");
                    }
                }
            }

            List<Object[]> rows = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("notification_id"),
                        rs.getInt("recipient_id"),
                        rs.getString("recipient_name"),
                        rs.getString("recipient_email"),
                        rs.getString("recipient_desk"),
                        rs.getInt("topic_id"),
                        rs.getString("topic_code"),
                        rs.getString("topic_title"),
                        rs.getString("message"),
                        rs.getBoolean("is_read"),
                        rs.getInt("sender_id"),
                        rs.getString("sender_name"),
                        rs.getTimestamp("sent_at"),
                        rs.getTimestamp("read_at"),
                        rs.getString("source_type"),
                        rs.getString("urgency")
                    });
                }
            }
            return rows;
        }
    }

    public void pingUnreadRecipient(int actorUserId, int originalNotificationId, int recipientUserId) throws SQLException {
        String actorSql = "SELECT r.role_name FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                        + "WHERE u.user_id = ? AND u.active_status = 1";
        String originalSql = "SELECT topic_id, message, is_read FROM Notification WHERE notification_id = ? AND user_id = ?";
        String insertNotification = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'PING_UNREAD_NOTIFICATION', 'NOTIFICATION', ?, ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                String role = null;
                try (PreparedStatement ps = connection.prepareStatement(actorSql)) {
                    ps.setInt(1, actorUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) role = rs.getString(1);
                    }
                }
                if (!"FIELDWORK_COORDINATOR".equals(role) 
                        && !"DESK_EDITOR".equals(role)
                        && !"EXECUTIVE_EDITOR".equals(role) 
                        && !"SYSTEM_ADMINISTRATOR".equals(role)) {
                    throw new IllegalArgumentException("Bạn không có quyền Ping người nhận.");
                }

                int topicId;
                String originalMessage;
                boolean isRead;
                try (PreparedStatement ps = connection.prepareStatement(originalSql)) {
                    ps.setInt(1, originalNotificationId);
                    ps.setInt(2, recipientUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy thông báo gốc.");
                        topicId = rs.getInt("topic_id");
                        originalMessage = rs.getString("message");
                        isRead = rs.getBoolean("is_read");
                    }
                }

                if (isRead) {
                    throw new IllegalArgumentException("Người nhận đã đọc thông báo này, không cần Ping lại.");
                }

                String pingMessage = "[PING ĐÔN ĐỐC] " + originalMessage;
                try (PreparedStatement ps = connection.prepareStatement(insertNotification)) {
                    ps.setInt(1, recipientUserId);
                    ps.setInt(2, topicId);
                    ps.setString(3, pingMessage);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(auditSql)) {
                    ps.setInt(1, actorUserId);
                    ps.setInt(2, originalNotificationId);
                    ps.setString(3, "Ping nhắc đọc thông báo #" + originalNotificationId + " tới User #" + recipientUserId);
                    ps.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
