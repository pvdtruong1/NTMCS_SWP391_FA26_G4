package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AssetRequest;
import model.RequestDeadlineExtension;
import model.Topic;

/**
 * Data Access Object for handling Reporter Topic requests:
 * 1. Asset Request (Asset_Request)
 * 2. Deadline Extension Request (Request_Deadline_Extension)
 * 3. Member Change Proposal (via Notification & audit)
 */
public class TopicRequestDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(TopicRequestDAO.class.getName());

    /**
     * Lấy danh sách các đề tài của chính phóng viên (author_user_id) đã được duyệt (status = 'APPROVED')
     */
    public List<Topic> getApprovedTopicsForReporter(int userId) {
        List<Topic> list = new ArrayList<>();
        String sql = "SELECT t.topic_id, t.topic_code, t.title, t.summary, t.angle, t.genre, t.priority, "
                   + "       t.status, t.deadline, t.created_date, t.category_id, t.author_user_id, "
                   + "       c.category_name, d.desk_id, d.desk_name, u.full_name AS reporter_name "
                   + "FROM Topic t "
                   + "JOIN Category c ON t.category_id = c.category_id "
                   + "JOIN Desk d ON c.desk_id = d.desk_id "
                   + "JOIN [User] u ON t.author_user_id = u.user_id "
                   + "WHERE t.author_user_id = ? "
                   + "  AND t.status = 'APPROVED' "
                   + "ORDER BY t.topic_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Topic t = new Topic();
                    t.setTopicId(rs.getInt("topic_id"));
                    t.setTopicCode(rs.getString("topic_code"));
                    t.setTitle(rs.getString("title"));
                    t.setSummary(rs.getString("summary"));
                    t.setAngle(rs.getString("angle"));
                    t.setGenre(rs.getString("genre"));
                    t.setPriority(rs.getString("priority"));
                    t.setStatus(rs.getString("status"));
                    t.setDeadline(rs.getDate("deadline"));
                    t.setCreatedDate(rs.getTimestamp("created_date"));
                    t.setCategoryId(rs.getInt("category_id"));
                    t.setAuthorUserId(rs.getInt("author_user_id"));
                    t.setCategoryName(rs.getString("category_name"));
                    t.setDeskName(rs.getString("desk_name"));
                    t.setReporterName(rs.getString("reporter_name"));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get approved topics for reporter " + userId, e);
        }
        return list;
    }

    // ==========================================
    // 1. ASSET REQUEST (Xin mượn tài sản)
    // ==========================================
    public boolean insertAssetRequest(AssetRequest req) {
        String sql = "INSERT INTO Asset_Request (topic_id, asset_id, requester_user_id, start_time, end_time, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getTopicId());
            ps.setInt(2, req.getAssetId());
            ps.setInt(3, req.getRequesterUserId());
            ps.setTimestamp(4, req.getStartTime());
            ps.setTimestamp(5, req.getEndTime());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert Asset_Request", e);
            return false;
        }
    }

    public List<AssetRequest> getAssetRequestsByTopic(int topicId) {
        List<AssetRequest> list = new ArrayList<>();
        String sql = "SELECT ar.request_id, ar.topic_id, ar.asset_id, ar.requester_user_id, "
                   + "       ar.start_time, ar.end_time, ar.status, "
                   + "       a.asset_name, a.asset_category, a.asset_code, u.full_name AS requester_name "
                   + "FROM Asset_Request ar "
                   + "INNER JOIN Asset a ON ar.asset_id = a.asset_id "
                   + "INNER JOIN [User] u ON ar.requester_user_id = u.user_id "
                   + "WHERE ar.topic_id = ? "
                   + "ORDER BY ar.request_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssetRequest r = new AssetRequest();
                    r.setRequestId(rs.getInt("request_id"));
                    r.setTopicId(rs.getInt("topic_id"));
                    r.setAssetId(rs.getInt("asset_id"));
                    r.setRequesterUserId(rs.getInt("requester_user_id"));
                    r.setStartTime(rs.getTimestamp("start_time"));
                    r.setEndTime(rs.getTimestamp("end_time"));
                    r.setStatus(rs.getString("status"));
                    r.setAssetName(rs.getString("asset_name"));
                    r.setAssetCategory(rs.getString("asset_category"));
                    r.setAssetCode(rs.getString("asset_code"));
                    r.setRequesterName(rs.getString("requester_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get Asset_Requests for topic " + topicId, e);
        }
        return list;
    }

    // ==========================================
    // 2. DEADLINE EXTENSION (Xin gia hạn Deadline)
    // ==========================================
    public boolean insertDeadlineExtensionRequest(RequestDeadlineExtension req) {
        String sql = "INSERT INTO Request_Deadline_Extension "
                   + "(topic_id, requested_by_user_id, current_deadline, requested_deadline, reason, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getTopicId());
            ps.setInt(2, req.getRequestedByUserId());
            ps.setDate(3, req.getCurrentDeadline());
            ps.setDate(4, req.getRequestedDeadline());
            ps.setString(5, req.getReason());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert Request_Deadline_Extension", e);
            return false;
        }
    }

    public List<RequestDeadlineExtension> getDeadlineRequestsByTopic(int topicId) {
        List<RequestDeadlineExtension> list = new ArrayList<>();
        String sql = "SELECT dr.request_id, dr.topic_id, dr.requested_by_user_id, dr.current_deadline, "
                   + "       dr.requested_deadline, dr.reason, dr.status, u.full_name AS requester_name "
                   + "FROM Request_Deadline_Extension dr "
                   + "INNER JOIN [User] u ON dr.requested_by_user_id = u.user_id "
                   + "WHERE dr.topic_id = ? "
                   + "ORDER BY dr.request_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RequestDeadlineExtension r = new RequestDeadlineExtension();
                    r.setRequestId(rs.getInt("request_id"));
                    r.setTopicId(rs.getInt("topic_id"));
                    r.setRequestedByUserId(rs.getInt("requested_by_user_id"));
                    r.setCurrentDeadline(rs.getDate("current_deadline"));
                    r.setRequestedDeadline(rs.getDate("requested_deadline"));
                    r.setReason(rs.getString("reason"));
                    r.setStatus(rs.getString("status"));
                    r.setRequesterName(rs.getString("requester_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get Deadline Extensions for topic " + topicId, e);
        }
        return list;
    }

    // ==========================================
    // 3. MEMBER CHANGE PROPOSAL (Thay đổi nhân sự)
    // ==========================================
    public boolean sendMemberChangeNotification(int topicId, int requesterId, int targetUserId, String actionType, String reason) {
        // Tìm Desk Editor phụ trách chuyên mục của Đề tài
        String sqlFindEditor = "SELECT TOP 1 u.user_id FROM [User] u "
                             + "INNER JOIN Category c ON u.desk_id = c.desk_id "
                             + "INNER JOIN Topic t ON t.category_id = c.category_id "
                             + "INNER JOIN Role r ON u.role_id = r.role_id "
                             + "WHERE t.topic_id = ? AND r.role_name IN ('DESK_EDITOR', 'MANAGING_EDITOR') ";

        String sqlTargetUser = "SELECT full_name, email FROM [User] WHERE user_id = ?";
        String sqlInsertNoti = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";

        try (Connection conn = getConnection()) {
            int editorId = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlFindEditor)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        editorId = rs.getInt("user_id");
                    }
                }
            }

            // Fallback: nếu Desk chưa phân công editor, tìm động tài khoản Trưởng ban / Admin
            if (editorId == 0) {
                String sqlAdmin = "SELECT TOP 1 u.user_id FROM [User] u "
                                + "JOIN Role r ON u.role_id = r.role_id "
                                + "WHERE r.role_name IN ('MANAGING_EDITOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN') "
                                + "ORDER BY u.user_id ASC";
                try (PreparedStatement psAdmin = conn.prepareStatement(sqlAdmin);
                     ResultSet rsAdmin = psAdmin.executeQuery()) {
                    if (rsAdmin.next()) {
                        editorId = rsAdmin.getInt("user_id");
                    }
                }
            }

            String targetName = "ID #" + targetUserId;
            try (PreparedStatement psUser = conn.prepareStatement(sqlTargetUser)) {
                psUser.setInt(1, targetUserId);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (rsUser.next()) {
                        targetName = rsUser.getString("full_name") + " (" + rsUser.getString("email") + ")";
                    }
                }
            }

            String actionText = "ADD".equalsIgnoreCase(actionType) ? "THÊM" : "RÚT KHỎI NHÓM";
            String msg = "[ĐỀ XUẤT NHÂN SỰ] Phóng viên đề xuất " + actionText + " nhân sự: " 
                       + targetName + " cho đề tài. Lý do: " + reason;

            try (PreparedStatement psNoti = conn.prepareStatement(sqlInsertNoti)) {
                psNoti.setInt(1, editorId);
                psNoti.setInt(2, topicId);
                psNoti.setString(3, msg);
                return psNoti.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to send member change notification", e);
            return false;
        }
    }

    public List<String> getMemberChangeNotificationsByTopic(int topicId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT message FROM Notification "
                   + "WHERE topic_id = ? AND message LIKE '[ĐỀ XUẤT NHÂN SỰ]%' "
                   + "ORDER BY notification_id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("message"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load member notifications", e);
        }
        return list;
    }
}
