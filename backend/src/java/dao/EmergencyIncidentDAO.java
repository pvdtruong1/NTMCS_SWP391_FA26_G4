package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBConnection;

public class EmergencyIncidentDAO {

    private static final int DESK_EDITOR_USER_ID = 1;
    private static final int FIELDWORK_COORDINATOR_USER_ID = 4;

    /**
     * Persists an emergency incident audit entry and notifies the desk editor
     * and fieldwork coordinator in one database transaction.
     */
    public void reportEmergencyIncident(
            int topicId,
            String incidentDescription,
            int actorUserId) throws SQLException {

        if (topicId <= 0 || actorUserId <= 0) {
            throw new IllegalArgumentException("Topic hoặc người báo cáo không hợp lệ.");
        }

        if (incidentDescription == null || incidentDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập nội dung sự cố.");
        }

        String topicSql =
                "SELECT topic_code FROM Topic WHERE topic_id = ?";

        String auditSql =
                "INSERT INTO Audit_Log "
              + "(actor_user_id, action, entity_type, entity_id, details) "
              + "VALUES (?, ?, ?, ?, ?)";

        String notificationSql =
                "INSERT INTO Notification (user_id, topic_id, message, is_read) "
              + "VALUES (?, ?, ?, 0)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String topicCode;

                try (PreparedStatement topicStatement =
                             connection.prepareStatement(topicSql)) {
                    topicStatement.setInt(1, topicId);

                    try (ResultSet rs = topicStatement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Không tìm thấy đề tài.");
                        }
                        topicCode = rs.getString("topic_code");
                    }
                }

                String topicLabel = (topicCode == null || topicCode.trim().isEmpty())
                        ? "#" + topicId
                        : topicCode;

                String auditDetails =
                        "BÁO CÁO SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG: "
                        + incidentDescription.trim();

                try (PreparedStatement auditStatement =
                             connection.prepareStatement(auditSql)) {
                    auditStatement.setInt(1, actorUserId);
                    auditStatement.setString(2, "EMERGENCY_INCIDENT");
                    auditStatement.setString(3, "TOPIC");
                    auditStatement.setInt(4, topicId);
                    auditStatement.setString(5, auditDetails);
                    auditStatement.executeUpdate();
                }

                String notificationMessage =
                        "[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG] Đề tài "
                        + topicLabel + ": " + incidentDescription.trim();

                insertNotification(
                        connection,
                        notificationSql,
                        DESK_EDITOR_USER_ID,
                        topicId,
                        notificationMessage
                );

                insertNotification(
                        connection,
                        notificationSql,
                        FIELDWORK_COORDINATOR_USER_ID,
                        topicId,
                        notificationMessage
                );

                connection.commit();

            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Connection is closed by try-with-resources.
                }
            }
        }
    }

    private void insertNotification(
            Connection connection,
            String sql,
            int userId,
            int topicId,
            String message) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, topicId);
            statement.setString(3, message);
            statement.executeUpdate();
        }
    }
}
