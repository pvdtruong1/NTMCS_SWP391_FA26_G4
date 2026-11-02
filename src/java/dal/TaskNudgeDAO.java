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
 * Data Access Object for Task Nudges (SCR-4.7).
 */
public class TaskNudgeDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(TaskNudgeDAO.class.getName());

    public List<Object[]> getReporterTopicAssignments() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT DISTINCT t.topic_id, t.topic_code, t.title AS topic_title, "
                   + "       u.user_id AS reporter_id, u.full_name AS reporter_name, tm.role_in_team "
                   + "FROM Topic t "
                   + "JOIN Topic_Member tm ON tm.topic_id = t.topic_id "
                   + "JOIN [User] u ON u.user_id = tm.user_id "
                   + "JOIN Role r ON r.role_id = u.role_id "
                   + "WHERE u.active_status = 1 AND r.role_name = 'REPORTER' "
                   + "UNION "
                   + "SELECT t.topic_id, t.topic_code, t.title AS topic_title, "
                   + "       u.user_id AS reporter_id, u.full_name AS reporter_name, 'AUTHOR' AS role_in_team "
                   + "FROM Topic t "
                   + "JOIN [User] u ON u.user_id = t.author_user_id "
                   + "JOIN Role r ON r.role_id = u.role_id "
                   + "WHERE u.active_status = 1 AND r.role_name = 'REPORTER' "
                   + "ORDER BY topic_title, reporter_name";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("topic_id"),
                    rs.getString("topic_code"),
                    rs.getString("topic_title"),
                    rs.getInt("reporter_id"),
                    rs.getString("reporter_name"),
                    rs.getString("role_in_team")
                });
            }
        }
        return rows;
    }

    public void sendNudge(int actorUserId, int reporterUserId, int topicId,
                          String title, String message, String urgency) throws SQLException {
        String actorSql = "SELECT r.role_name FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                        + "WHERE u.user_id = ? AND u.active_status = 1";
        String recipientSql = "SELECT COUNT(*) FROM [User] u JOIN Role r ON r.role_id = u.role_id "
                            + "WHERE u.user_id = ? AND u.active_status = 1 AND r.role_name = 'REPORTER' "
                            + "AND (EXISTS (SELECT 1 FROM Topic_Member tm WHERE tm.topic_id = ? AND tm.user_id = u.user_id) "
                            + "     OR EXISTS (SELECT 1 FROM Topic t WHERE t.topic_id = ? AND t.author_user_id = u.user_id))";
        String notificationSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
        String auditSql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                        + "VALUES (?, 'SEND_TASK_NUDGE', 'NOTIFICATION', ?, ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                String actorRole = null;
                try (PreparedStatement ps = connection.prepareStatement(actorSql)) {
                    ps.setInt(1, actorUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            actorRole = rs.getString(1);
                        }
                    }
                }

                // Allow Fieldwork Coordinator, Desk Editor, and System Admin
                if (!"FIELDWORK_COORDINATOR".equals(actorRole) 
                        && !"DESK_EDITOR".equals(actorRole) 
                        && !"SYSTEM_ADMINISTRATOR".equals(actorRole)) {
                    throw new IllegalArgumentException("Tài khoản không có quyền gửi lời nhắc đôn đốc.");
                }

                int validRecipient = 0;
                try (PreparedStatement ps = connection.prepareStatement(recipientSql)) {
                    ps.setInt(1, reporterUserId);
                    ps.setInt(2, topicId);
                    ps.setInt(3, topicId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            validRecipient = rs.getInt(1);
                        }
                    }
                }
                if (validRecipient == 0) {
                    throw new IllegalArgumentException("Phóng viên không thuộc đề tài đã chọn hoặc đề tài không tồn tại.");
                }

                String urgencyLabel;
                if ("NORMAL".equals(urgency)) urgencyLabel = "THÔNG THƯỜNG";
                else if ("HIGH".equals(urgency)) urgencyLabel = "ƯU TIÊN CAO";
                else if ("EMERGENCY".equals(urgency)) urgencyLabel = "KHẨN CẤP";
                else throw new IllegalArgumentException("Mức độ ưu tiên không hợp lệ.");

                String notificationMessage = "[NHẮC VIỆC - " + urgencyLabel + "] " + title.trim() + "\n" + message.trim();
                int notificationId;
                try (PreparedStatement ps = connection.prepareStatement(notificationSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, reporterUserId);
                    ps.setInt(2, topicId);
                    ps.setString(3, notificationMessage);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Không lấy được mã thông báo vừa tạo.");
                        }
                        notificationId = keys.getInt(1);
                    }
                }

                String auditDetails = "Gửi đôn đốc (" + urgencyLabel + ") tới phóng viên #" + reporterUserId
                                    + " cho đề tài #" + topicId + ": " + title.trim();
                try (PreparedStatement ps = connection.prepareStatement(auditSql)) {
                    ps.setInt(1, actorUserId);
                    ps.setInt(2, notificationId);
                    ps.setString(3, auditDetails);
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
