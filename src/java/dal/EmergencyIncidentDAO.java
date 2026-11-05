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
 * Data Access Object for Emergency Incidents (SCR-4.4).
 * Notifies the Desk Editor and Fieldwork Coordinator dynamically.
 */
public class EmergencyIncidentDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(EmergencyIncidentDAO.class.getName());

    public void reportEmergencyIncident(int topicId, String incidentDescription, int actorUserId) throws SQLException {
        if (topicId <= 0 || actorUserId <= 0) {
            throw new IllegalArgumentException("Topic hoặc người báo cáo không hợp lệ.");
        }
        if (incidentDescription == null || incidentDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung sự cố.");
        }

        String topicSql = "SELECT topic_code, title FROM Topic WHERE topic_id = ?";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'EMERGENCY_INCIDENT', 'TOPIC', ?, ?)";
        String notificationSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) "
                               + "VALUES (?, ?, ?, 0)";
        String coordinatorSql = "SELECT u.user_id FROM [User] u "
                              + "JOIN Role r ON u.role_id = r.role_id "
                              + "WHERE r.role_name IN ('FIELDWORK_COORDINATOR', 'DESK_EDITOR', 'EXECUTIVE_EDITOR') AND u.active_status = 1";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                String topicCode;
                String topicTitle;
                try (PreparedStatement topicStatement = connection.prepareStatement(topicSql)) {
                    topicStatement.setInt(1, topicId);
                    try (ResultSet rs = topicStatement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy đề tài #" + topicId);
                        }
                        topicCode = rs.getString("topic_code");
                        topicTitle = rs.getString("title");
                    }
                }

                String topicLabel = (topicCode != null && !topicCode.trim().isEmpty())
                        ? topicCode : ("#" + topicId);

                String auditDetails = "BÁO CÁO SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG (" + topicLabel + "): "
                        + incidentDescription.trim();

                try (PreparedStatement auditStatement = connection.prepareStatement(auditSql)) {
                    auditStatement.setInt(1, actorUserId);
                    auditStatement.setInt(2, topicId);
                    auditStatement.setString(3, auditDetails);
                    auditStatement.executeUpdate();
                }

                String notificationMessage = "[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG] Đề tài "
                        + topicLabel + " (" + topicTitle + "): " + incidentDescription.trim();

                // Dynamically fetch all Fieldwork Coordinators and Desk Editors
                List<Integer> recipientUserIds = new ArrayList<>();
                try (PreparedStatement ps = connection.prepareStatement(coordinatorSql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        recipientUserIds.add(rs.getInt("user_id"));
                    }
                }

                // If none found dynamically, fallback to default roles (e.g. 2, 4)
                if (recipientUserIds.isEmpty()) {
                    recipientUserIds.add(1); // Desk Editor (Trường)
                    recipientUserIds.add(6); // Executive Editor (TBT)
                    recipientUserIds.add(4); // Fieldwork Coordinator
                }

                try (PreparedStatement notifStmt = connection.prepareStatement(notificationSql)) {
                    for (int recipientId : recipientUserIds) {
                        notifStmt.setInt(1, recipientId);
                        notifStmt.setInt(2, topicId);
                        notifStmt.setString(3, notificationMessage);
                        notifStmt.addBatch();
                    }
                    notifStmt.executeBatch();
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
