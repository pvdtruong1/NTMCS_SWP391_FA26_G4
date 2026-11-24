package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.AssetRequest;
import model.RequestDeadlineExtension;
import model.Topic;
import model.User;

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
                   + "  AND t.status IN ('APPROVED', 'IN_PROGRESS') "
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

    /**
     * Kiểm tra xung đột lịch mượn trang thiết bị / phương tiện:
     * Trả về true nếu thiết bị đó đã có đơn mượn khác ở trạng thái APPROVED hoặc PENDING
     * bị trùng khung thời gian [startTime, endTime].
     */
    public boolean hasAssetScheduleConflict(int assetId, Timestamp startTime, Timestamp endTime) {
        String sql = "SELECT COUNT(*) FROM Asset_Request "
                   + "WHERE asset_id = ? "
                   + "  AND status IN ('APPROVED', 'PENDING') "
                   + "  AND start_time < ? AND end_time > ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            ps.setTimestamp(2, endTime);
            ps.setTimestamp(3, startTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check asset schedule conflict for asset " + assetId, e);
        }
        return false;
    }

    /**
     * Lấy tên đầy đủ kèm mã tài sản để hiển thị thông báo lỗi khi phát hiện xung đột lịch.
     */
    public String getAssetName(int assetId) {
        String sql = "SELECT asset_name, asset_code FROM Asset WHERE asset_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("asset_name") + " (" + rs.getString("asset_code") + ")";
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get asset name for asset " + assetId, e);
        }
        return "Thiết bị #" + assetId;
    }

    /**
     * Lưu một đơn yêu cầu mượn thiết bị đơn lẻ vào bảng Asset_Request với trạng thái PENDING.
     */
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

    /**
     * Lưu danh sách nhiều yêu cầu mượn thiết bị cùng lúc sử dụng Transaction và JDBC Batch.
     * Trả về số lượng thiết bị đã gửi yêu cầu mượn thành công.
     */
    public int insertAssetRequests(List<AssetRequest> requests) {
        if (requests == null || requests.isEmpty()) return 0;
        String sql = "INSERT INTO Asset_Request (topic_id, asset_id, requester_user_id, start_time, end_time, status) "
                   + "VALUES (?, ?, ?, ?, ?, 'PENDING')";
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            int count = 0;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (AssetRequest req : requests) {
                    ps.setInt(1, req.getTopicId());
                    ps.setInt(2, req.getAssetId());
                    ps.setInt(3, req.getRequesterUserId());
                    ps.setTimestamp(4, req.getStartTime());
                    ps.setTimestamp(5, req.getEndTime());
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                for (int res : results) {
                    if (res > 0 || res == PreparedStatement.SUCCESS_NO_INFO) {
                        count++;
                    }
                }
            }
            conn.commit();
            return count;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to batch insert asset requests, rolling back", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed", ex);
                }
            }
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Lấy danh sách toàn bộ các yêu cầu mượn thiết bị đã gửi của một đề tài cụ thể.
     */
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

    /**
     * Thêm yêu cầu xin gia hạn Deadline đề tài vào bảng Request_Deadline_Extension với trạng thái PENDING.
     */
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

    /**
     * Lấy danh sách lịch sử các yêu cầu xin gia hạn Deadline của một đề tài.
     */
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

    /**
     * Gửi đề xuất điều chỉnh nhân sự đơn lẻ (thêm hoặc điều chuyển 1 thành viên) qua hệ thống Notification:
     * - Gửi thông báo kèm tag [PENDING] tới Desk Editor phụ trách ban chuyên môn của đề tài.
     * - Lưu thông báo xác nhận cho chính phóng viên gửi đề xuất.
     * - Ghi vết kiểm toán vào bảng Audit_Log.
     * - Ngữ điệu chuẩn mực báo chí hiện đại, không dùng từ "đồng chí".
     */
    public boolean sendMemberChangeNotification(int topicId, int requesterId, int targetUserId, String actionType, String reason) {
        // Tìm thông tin đề tài, Desk Editor và Tác giả chủ trì
        String sqlFindInfo = "SELECT t.topic_code, t.title, t.author_user_id, "
                           + "       auth.full_name AS author_name, "
                           + "       u_ed.user_id AS editor_id, "
                           + "       c.category_name, d.desk_id, d.desk_name "
                           + "FROM Topic t "
                           + "JOIN Category c ON t.category_id = c.category_id "
                           + "JOIN Desk d ON c.desk_id = d.desk_id "
                           + "LEFT JOIN [User] auth ON t.author_user_id = auth.user_id "
                           + "LEFT JOIN [User] u_ed ON u_ed.desk_id = d.desk_id AND u_ed.active_status = 1 "
                           + "     AND u_ed.role_id IN (SELECT role_id FROM Role WHERE role_name IN ('DESK_EDITOR', 'MANAGING_EDITOR')) "
                           + "WHERE t.topic_id = ?";

        String sqlTargetUser = "SELECT full_name, email FROM [User] WHERE user_id = ?";
        String sqlInsertNoti = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";

        try (Connection conn = getConnection()) {
            int editorId = 0;
            String topicCode = "#" + topicId;
            String topicTitle = "Đề tài #" + topicId;
            String authorName = "Chủ trì đề tài";

            try (PreparedStatement psInfo = conn.prepareStatement(sqlFindInfo)) {
                psInfo.setInt(1, topicId);
                try (ResultSet rsInfo = psInfo.executeQuery()) {
                    if (rsInfo.next()) {
                        editorId = rsInfo.getInt("editor_id");
                        String tc = rsInfo.getString("topic_code");
                        if (tc != null && !tc.trim().isEmpty()) topicCode = tc.trim();
                        String tt = rsInfo.getString("title");
                        if (tt != null && !tt.trim().isEmpty()) topicTitle = tt.trim();
                        String an = rsInfo.getString("author_name");
                        if (an != null && !an.trim().isEmpty()) authorName = an.trim();
                    }
                }
            }

            // Fallback: nếu Desk chưa phân công editor, tìm động tài khoản Trưởng ban / Admin
            if (editorId == 0) {
                String sqlAdmin = "SELECT TOP 1 u.user_id FROM [User] u "
                                + "JOIN Role r ON u.role_id = r.role_id "
                                + "WHERE r.role_name IN ('DESK_EDITOR', 'EXECUTIVE_EDITOR', 'SYSTEM_ADMINISTRATOR', 'ADMIN') "
                                + "ORDER BY u.user_id ASC";
                try (PreparedStatement psAdmin = conn.prepareStatement(sqlAdmin);
                     ResultSet rsAdmin = psAdmin.executeQuery()) {
                    if (rsAdmin.next()) {
                        editorId = rsAdmin.getInt("user_id");
                    }
                }
            }

            String targetName = "Nhân sự #" + targetUserId;
            String targetEmail = "";
            try (PreparedStatement psUser = conn.prepareStatement(sqlTargetUser)) {
                psUser.setInt(1, targetUserId);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (rsUser.next()) {
                        targetName = rsUser.getString("full_name");
                        targetEmail = rsUser.getString("email");
                    }
                }
            }

            boolean isAdd = "ADD".equalsIgnoreCase(actionType);
            String tag = isAdd ? "[ĐỀ XUẤT BỔ SUNG NHÂN SỰ]" : "[ĐỀ XUẤT ĐIỀU CHUYỂN NHÂN SỰ]";
            String actionDesc = isAdd ? "bổ sung nhân sự" : "điều chuyển nhân sự";
            String fromTo = isAdd ? "vào kíp tác nghiệp" : "rời khỏi kíp tác nghiệp";
            String cleanReason = (reason != null && !reason.trim().isEmpty()) ? reason.trim() : "Phục vụ kế hoạch tác nghiệp";

            // Thông báo gửi Trưởng ban Biên tập
            String msg = tag + " [PENDING] Phóng viên " + authorName + " đề xuất " + actionDesc + ": " 
                       + targetName + " (" + targetEmail + ", ID: " + targetUserId + ") " + fromTo 
                       + " đề tài \"" + topicTitle + "\" (" + topicCode + "). Lý do: " + cleanReason;

            try (PreparedStatement psNoti = conn.prepareStatement(sqlInsertNoti)) {
                psNoti.setInt(1, editorId);
                psNoti.setInt(2, topicId);
                psNoti.setString(3, msg);
                psNoti.executeUpdate();
            }

            // Bản ghi thông báo xác nhận cho chính phóng viên gửi đề xuất
            if (requesterId != editorId) {
                String reporterMsg = tag + " [PENDING] Bạn đã gửi đề xuất " + actionDesc + ": " 
                                   + targetName + " (" + targetEmail + ", ID: " + targetUserId + ") " + fromTo 
                                   + " đề tài \"" + topicTitle + "\" (" + topicCode + "). Lý do: " + cleanReason;
                try (PreparedStatement psNoti2 = conn.prepareStatement(sqlInsertNoti)) {
                    psNoti2.setInt(1, requesterId);
                    psNoti2.setInt(2, topicId);
                    psNoti2.setString(3, reporterMsg);
                    psNoti2.executeUpdate();
                }
            }

            // Ghi nhận vào Audit_Log
            try {
                new AuditLogDAO().log(requesterId, isAdd ? "PROPOSE_ADD_MEMBER" : "PROPOSE_REMOVE_MEMBER", "TOPIC", topicId,
                        "Đề xuất " + actionDesc + ": " + targetName + " (" + targetEmail + ", ID: " + targetUserId + ") " + fromTo + ". Lý do: " + cleanReason);
            } catch (Exception ignored) {}

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to send member change notification", e);
            return false;
        }
    }

    /**
     * Gửi đề xuất THÊM nhiều nhân sự bằng danh sách User (đã validate email).
     * Lưu vào Notification cho từng nhân sự cụ thể để Trưởng ban quản lý độc lập,
     * đồng thời ghi Audit_Log.
     */
    public boolean sendMultiMemberAddNotification(int topicId, int requesterId, List<User> targetUsers, String reason) {
        if (targetUsers == null || targetUsers.isEmpty()) {
            return false;
        }

        boolean allSuccess = true;
        for (User u : targetUsers) {
            boolean ok = sendMemberChangeNotification(topicId, requesterId, u.getUserId(), "ADD", reason);
            if (!ok) allSuccess = false;
        }
        return allSuccess;
    }

    /**
     * Lấy danh sách các thông báo đề xuất thay đổi nhân sự đã gửi cho một đề tài,
     * phục vụ hiển thị lịch sử đề xuất trên form topic-requests.jsp.
     */
    public List<String> getMemberChangeNotificationsByTopic(int topicId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT message FROM Notification "
                   + "WHERE topic_id = ? AND (message LIKE '%ĐỀ XUẤT%NHÂN SỰ%') "
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

    // ==========================================
    // 4. DESK EDITOR: XỬ LÝ DUYỆT ĐỀ XUẤT NHÂN SỰ
    // ==========================================

    /**
     * Lấy danh sách toàn bộ các yêu cầu điều chuyển / bổ sung nhân sự của ban chuyên môn.
     * Trích xuất thông tin trực tiếp từ bảng Notification kết hợp Topic, User và Fieldwork_Task.
     */
    public List<dto.MemberChangeRequestDTO> getMemberChangeRequests(int deskId, boolean isExecutive) {
        List<dto.MemberChangeRequestDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT n.notification_id, n.topic_id, n.message, n.is_read, ")
           .append("       t.topic_code, t.title AS topic_title, t.author_user_id, ")
           .append("       auth.full_name AS author_name, ")
           .append("       c.desk_id, d.desk_name ")
           .append("FROM Notification n ")
           .append("JOIN Topic t ON n.topic_id = t.topic_id ")
           .append("JOIN Category c ON t.category_id = c.category_id ")
           .append("JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("LEFT JOIN [User] auth ON t.author_user_id = auth.user_id ")
           .append("WHERE (n.message LIKE '%[ĐỀ XUẤT%NHÂN SỰ]%' OR n.message LIKE '%[ĐỀ XUẤT NHÂN SỰ]%') ");

        if (!isExecutive && deskId > 0) {
            sql.append("  AND c.desk_id = ? ");
        }
        sql.append("ORDER BY n.notification_id DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (!isExecutive && deskId > 0) {
                ps.setInt(1, deskId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String msg = rs.getString("message");
                    dto.MemberChangeRequestDTO dto = parseMemberChangeRequest(rs, msg, conn);
                    if (dto != null) {
                        list.add(dto);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load member change requests for desk " + deskId, e);
        }
        return list;
    }

    /**
     * Parse thông tin yêu cầu nhân sự từ nội dung thông báo Notification.
     */
    private dto.MemberChangeRequestDTO parseMemberChangeRequest(ResultSet rs, String msg, Connection conn) throws SQLException {
        if (msg == null) return null;

        dto.MemberChangeRequestDTO dto = new dto.MemberChangeRequestDTO();
        dto.setNotificationId(rs.getInt("notification_id"));
        dto.setTopicId(rs.getInt("topic_id"));
        dto.setTopicCode(rs.getString("topic_code"));
        dto.setTopicTitle(rs.getString("topic_title"));
        dto.setDeskId(rs.getInt("desk_id"));
        dto.setDeskName(rs.getString("desk_name"));
        dto.setRequesterId(rs.getInt("author_user_id"));
        dto.setRequesterName(rs.getString("author_name"));
        dto.setRawMessage(msg);

        // Trạng thái yêu cầu
        if (msg.contains("[PENDING]")) {
            dto.setStatus("PENDING");
        } else if (msg.contains("[ĐÃ DUYỆT]") || msg.contains("[APPROVED]")) {
            dto.setStatus("APPROVED");
        } else if (msg.contains("[TỪ CHỐI]") || msg.contains("[REJECTED]")) {
            dto.setStatus("REJECTED");
        } else {
            dto.setStatus("PROCESSED");
        }

        // Loại hành động
        boolean isRemove = msg.contains("ĐIỀU CHUYỂN") || msg.contains("RÚT KHỎI NHÓM") || msg.contains("rời khỏi");
        dto.setActionType(isRemove ? "REMOVE" : "ADD");

        // Trích xuất Target User ID
        int targetUserId = 0;
        try {
            if (msg.contains("ID:")) {
                String afterId = msg.substring(msg.indexOf("ID:") + 3).trim();
                String idStr = afterId.replaceAll("^(\\d+).*", "$1");
                targetUserId = Integer.parseInt(idStr);
            } else if (msg.contains("ID #")) {
                String afterId = msg.substring(msg.indexOf("ID #") + 4).trim();
                String idStr = afterId.replaceAll("^(\\d+).*", "$1");
                targetUserId = Integer.parseInt(idStr);
            }
        } catch (Exception ignored) {}
        dto.setTargetUserId(targetUserId);

        // Lấy tên và email của Target User từ DB
        if (targetUserId > 0) {
            String sqlUser = "SELECT full_name, email FROM [User] WHERE user_id = ?";
            try (PreparedStatement psU = conn.prepareStatement(sqlUser)) {
                psU.setInt(1, targetUserId);
                try (ResultSet rsU = psU.executeQuery()) {
                    if (rsU.next()) {
                        dto.setTargetUserName(rsU.getString("full_name"));
                        dto.setTargetUserEmail(rsU.getString("email"));
                    }
                }
            }

            // Nếu là rút nhân sự, đếm số Fieldwork_Task đang dở dang
            if (isRemove) {
                String sqlTaskCount = "SELECT COUNT(*) FROM Fieldwork_Task "
                                    + "WHERE topic_id = ? AND assignee_user_id = ? AND task_status IN ('TODO', 'IN_PROGRESS')";
                try (PreparedStatement psT = conn.prepareStatement(sqlTaskCount)) {
                    psT.setInt(1, dto.getTopicId());
                    psT.setInt(2, targetUserId);
                    try (ResultSet rsT = psT.executeQuery()) {
                        if (rsT.next()) {
                            dto.setPendingTasksCount(rsT.getInt(1));
                        }
                    }
                }
            }
        }

        // Trích xuất lý do
        String reason = "";
        if (msg.contains("Lý do:")) {
            reason = msg.substring(msg.indexOf("Lý do:") + 6).trim();
        }
        dto.setReason(reason);

        return dto;
    }

    /**
     * Trưởng ban Biên tập PHÊ DUYỆT yêu cầu điều chuyển / bổ sung nhân sự.
     * Sử dụng Transaction để đảm bảo tính nguyên tử:
     * 1. Cập nhật bảng Topic_Member (INSERT nếu ADD, DELETE nếu REMOVE).
     * 2. Nếu REMOVE và nhân sự có Fieldwork_Task dở dang -> tự động bàn giao lại cho Chủ trì đề tài.
     * 3. Cập nhật trạng thái Notification gốc từ [PENDING] thành [ĐÃ DUYỆT].
     * 4. Gửi các thông báo xác nhận trang trọng (không dùng từ "đồng chí") cho nhân sự và chủ trì.
     * 5. Ghi vết kiểm toán Audit_Log.
     */
    public boolean approveMemberChangeRequest(int notificationId, int editorId, String editorFeedback) {
        String sqlGetNoti = "SELECT n.topic_id, n.message, "
                          + "       t.topic_code, t.title AS topic_title, t.author_user_id, "
                          + "       auth.full_name AS author_name "
                          + "FROM Notification n "
                          + "JOIN Topic t ON n.topic_id = t.topic_id "
                          + "LEFT JOIN [User] auth ON t.author_user_id = auth.user_id "
                          + "WHERE n.notification_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int topicId = 0;
            String msg = "";
            String topicCode = "";
            String topicTitle = "";
            int authorUserId = 0;
            String authorName = "";

            try (PreparedStatement ps = conn.prepareStatement(sqlGetNoti)) {
                ps.setInt(1, notificationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        topicId = rs.getInt("topic_id");
                        msg = rs.getString("message");
                        String tc = rs.getString("topic_code");
                        topicCode = (tc != null && !tc.trim().isEmpty()) ? tc.trim() : ("#" + topicId);
                        String tt = rs.getString("topic_title");
                        topicTitle = (tt != null && !tt.trim().isEmpty()) ? tt.trim() : ("Đề tài #" + topicId);
                        authorUserId = rs.getInt("author_user_id");
                        authorName = rs.getString("author_name");
                    }
                }
            }

            if (topicId <= 0 || msg == null || !msg.contains("[PENDING]")) {
                conn.rollback();
                return false;
            }

            boolean isRemove = msg.contains("ĐIỀU CHUYỂN") || msg.contains("RÚT KHỎI NHÓM") || msg.contains("rời khỏi");
            int targetUserId = 0;
            try {
                if (msg.contains("ID:")) {
                    String afterId = msg.substring(msg.indexOf("ID:") + 3).trim();
                    targetUserId = Integer.parseInt(afterId.replaceAll("^(\\d+).*", "$1"));
                } else if (msg.contains("ID #")) {
                    String afterId = msg.substring(msg.indexOf("ID #") + 4).trim();
                    targetUserId = Integer.parseInt(afterId.replaceAll("^(\\d+).*", "$1"));
                }
            } catch (Exception ignored) {}

            if (targetUserId <= 0) {
                conn.rollback();
                return false;
            }

            String targetName = "Nhân sự #" + targetUserId;
            String sqlTarget = "SELECT full_name FROM [User] WHERE user_id = ?";
            try (PreparedStatement psT = conn.prepareStatement(sqlTarget)) {
                psT.setInt(1, targetUserId);
                try (ResultSet rsT = psT.executeQuery()) {
                    if (rsT.next()) {
                        targetName = rsT.getString("full_name");
                    }
                }
            }

            String reason = "";
            if (msg.contains("Lý do:")) {
                reason = msg.substring(msg.indexOf("Lý do:") + 6).trim();
            }

            if (isRemove) {
                // RÀNG BUỘC: Không cho phép rút chính chủ trì đề tài
                if (targetUserId == authorUserId) {
                    conn.rollback();
                    return false;
                }

                // 1. Chuyển giao các Fieldwork_Task dở dang về cho Chủ trì đề tài
                String sqlReassignTasks = "UPDATE Fieldwork_Task "
                                        + "SET assignee_user_id = ?, "
                                        + "    description = CONCAT(ISNULL(description, ''), ' [Tái bàn giao cho chủ trì do điều chuyển nhân sự]') "
                                        + "WHERE topic_id = ? AND assignee_user_id = ? AND task_status IN ('TODO', 'IN_PROGRESS')";
                try (PreparedStatement psRe = conn.prepareStatement(sqlReassignTasks)) {
                    psRe.setInt(1, authorUserId);
                    psRe.setInt(2, topicId);
                    psRe.setInt(3, targetUserId);
                    psRe.executeUpdate();
                }

                // 2. Xóa khỏi Topic_Member
                String sqlDelMember = "DELETE FROM Topic_Member WHERE topic_id = ? AND user_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(sqlDelMember)) {
                    psDel.setInt(1, topicId);
                    psDel.setInt(2, targetUserId);
                    psDel.executeUpdate();
                }

                // 3. Thông báo cho người được điều chuyển (rút khỏi kíp)
                String notifTarget = "[ĐIỀU CHUYỂN NHÂN SỰ] Trưởng ban Biên tập đã phê duyệt điều chuyển bạn rời khỏi kíp tác nghiệp đề tài \""
                                   + topicTitle + "\" (" + topicCode + ") theo kế hoạch phân công tác nghiệp. Lý do điều chuyển: "
                                   + reason + ". Đề nghị bạn hoàn tất bàn giao các tài liệu, nhiệm vụ thực địa liên quan (nếu có) cho chủ trì đề tài.";
                insertNotification(conn, targetUserId, topicId, notifTarget);

                // 4. Thông báo cho Phóng viên chủ trì đề tài
                String notifAuthor = "[PHÊ DUYỆT NHÂN SỰ] Trưởng ban Biên tập đã ĐỒNG Ý đề xuất điều chuyển phóng viên "
                                   + targetName + " rời khỏi kíp tác nghiệp đề tài \"" + topicTitle + "\" (" + topicCode + ")."
                                   + ((editorFeedback != null && !editorFeedback.trim().isEmpty()) ? " Ý kiến Trưởng ban: " + editorFeedback.trim() : "")
                                   + " Đề nghị chủ trì đề tài tiếp nhận bàn giao công việc.";
                insertNotification(conn, authorUserId, topicId, notifAuthor);

            } else {
                // BỔ SUNG THÀNH VIÊN
                // 1. Kiểm tra đã có trong Topic_Member chưa
                String sqlCheck = "SELECT COUNT(*) FROM Topic_Member WHERE topic_id = ? AND user_id = ?";
                boolean exists = false;
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                    psCheck.setInt(1, topicId);
                    psCheck.setInt(2, targetUserId);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                            exists = true;
                        }
                    }
                }

                if (!exists) {
                    String sqlInsMem = "INSERT INTO Topic_Member (topic_id, user_id, role_in_team) VALUES (?, ?, 'ASSISTANT')";
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsMem)) {
                        psIns.setInt(1, topicId);
                        psIns.setInt(2, targetUserId);
                        psIns.executeUpdate();
                    }
                }

                // 2. Thông báo cho người được bổ sung (thêm vào kíp)
                String notifTarget = "[ĐIỀU ĐỘNG TÁC NGHIỆP] Trưởng ban Biên tập đã phê duyệt bổ sung bạn vào kíp tác nghiệp đề tài \""
                                   + topicTitle + "\" (" + topicCode + ") với vai trò Phóng viên tác nghiệp. Lý do: "
                                   + reason + ". Đề nghị bạn chủ động liên hệ chủ trì đề tài để phối hợp triển khai nhiệm vụ.";
                insertNotification(conn, targetUserId, topicId, notifTarget);

                // 3. Thông báo cho Phóng viên chủ trì đề tài
                String notifAuthor = "[PHÊ DUYỆT NHÂN SỰ] Trưởng ban Biên tập đã ĐỒNG Ý đề xuất bổ sung phóng viên "
                                   + targetName + " vào kíp tác nghiệp đề tài \"" + topicTitle + "\" (" + topicCode + ")."
                                   + ((editorFeedback != null && !editorFeedback.trim().isEmpty()) ? " Ý kiến Trưởng ban: " + editorFeedback.trim() : "")
                                   + " Kíp tác nghiệp đã được cập nhật chính thức.";
                insertNotification(conn, authorUserId, topicId, notifAuthor);
            }

            // 5. Cập nhật Notification gốc thành [ĐÃ DUYỆT]
            String sqlUpdateNoti = "UPDATE Notification "
                                 + "SET message = REPLACE(message, '[PENDING]', '[ĐÃ DUYỆT]'), is_read = 1 "
                                 + "WHERE notification_id = ?";
            try (PreparedStatement psUp = conn.prepareStatement(sqlUpdateNoti)) {
                psUp.setInt(1, notificationId);
                psUp.executeUpdate();
            }

            // 6. Ghi Audit Log
            try {
                new AuditLogDAO().log(editorId, isRemove ? "APPROVE_REMOVE_MEMBER" : "APPROVE_ADD_MEMBER", "TOPIC", topicId,
                        "Phê duyệt " + (isRemove ? "điều chuyển" : "bổ sung") + " nhân sự: " + targetName + " (" + targetUserId + ")"
                        + (editorFeedback != null && !editorFeedback.trim().isEmpty() ? ". Ý kiến: " + editorFeedback.trim() : ""));
            } catch (Exception ignored) {}

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to approve member change request #" + notificationId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Trưởng ban Biên tập TỪ CHỐI yêu cầu điều chuyển / bổ sung nhân sự.
     */
    public boolean rejectMemberChangeRequest(int notificationId, int editorId, String editorFeedback) {
        String sqlGetNoti = "SELECT n.topic_id, n.message, "
                          + "       t.topic_code, t.title AS topic_title, t.author_user_id "
                          + "FROM Notification n "
                          + "JOIN Topic t ON n.topic_id = t.topic_id "
                          + "WHERE n.notification_id = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int topicId = 0;
            String msg = "";
            String topicCode = "";
            String topicTitle = "";
            int authorUserId = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlGetNoti)) {
                ps.setInt(1, notificationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        topicId = rs.getInt("topic_id");
                        msg = rs.getString("message");
                        String tc = rs.getString("topic_code");
                        topicCode = (tc != null && !tc.trim().isEmpty()) ? tc.trim() : ("#" + topicId);
                        String tt = rs.getString("topic_title");
                        topicTitle = (tt != null && !tt.trim().isEmpty()) ? tt.trim() : ("Đề tài #" + topicId);
                        authorUserId = rs.getInt("author_user_id");
                    }
                }
            }

            if (topicId <= 0 || msg == null || !msg.contains("[PENDING]")) {
                conn.rollback();
                return false;
            }

            boolean isRemove = msg.contains("ĐIỀU CHUYỂN") || msg.contains("RÚT KHỎI NHÓM") || msg.contains("rời khỏi");
            String actionDesc = isRemove ? "điều chuyển" : "bổ sung";

            int targetUserId = 0;
            try {
                if (msg.contains("ID:")) {
                    String afterId = msg.substring(msg.indexOf("ID:") + 3).trim();
                    targetUserId = Integer.parseInt(afterId.replaceAll("^(\\d+).*", "$1"));
                } else if (msg.contains("ID #")) {
                    String afterId = msg.substring(msg.indexOf("ID #") + 4).trim();
                    targetUserId = Integer.parseInt(afterId.replaceAll("^(\\d+).*", "$1"));
                }
            } catch (Exception ignored) {}

            String targetName = "nhân sự đã chọn";
            if (targetUserId > 0) {
                String sqlTarget = "SELECT full_name FROM [User] WHERE user_id = ?";
                try (PreparedStatement psT = conn.prepareStatement(sqlTarget)) {
                    psT.setInt(1, targetUserId);
                    try (ResultSet rsT = psT.executeQuery()) {
                        if (rsT.next()) {
                            targetName = rsT.getString("full_name");
                        }
                    }
                }
            }

            // 1. Cập nhật Notification gốc thành [TỪ CHỐI]
            String sqlUpdateNoti = "UPDATE Notification "
                                 + "SET message = REPLACE(message, '[PENDING]', '[TỪ CHỐI]'), is_read = 1 "
                                 + "WHERE notification_id = ?";
            try (PreparedStatement psUp = conn.prepareStatement(sqlUpdateNoti)) {
                psUp.setInt(1, notificationId);
                psUp.executeUpdate();
            }

            // 2. Gửi thông báo từ chối cho Phóng viên chủ trì đề tài
            String cleanFeedback = (editorFeedback != null && !editorFeedback.trim().isEmpty())
                    ? editorFeedback.trim() : "Chưa phù hợp với kế hoạch phân bổ nhân lực hiện tại của ban";
            String notifAuthor = "[TỪ CHỐI ĐỀ XUẤT NHÂN SỰ] Trưởng ban Biên tập KHÔNG PHÊ DUYỆT đề xuất "
                               + actionDesc + " nhân sự đối với phóng viên " + targetName 
                               + " trong đề tài \"" + topicTitle + "\" (" + topicCode + "). Ý kiến chỉ đạo từ Trưởng ban: "
                               + cleanFeedback;
            insertNotification(conn, authorUserId, topicId, notifAuthor);

            // 3. Ghi Audit Log
            try {
                new AuditLogDAO().log(editorId, "REJECT_MEMBER_CHANGE", "TOPIC", topicId,
                        "Từ chối đề xuất " + actionDesc + " nhân sự: " + targetName + ". Lý do: " + cleanFeedback);
            } catch (Exception ignored) {}

            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to reject member change request #" + notificationId, e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    private void insertNotification(Connection conn, int userId, int topicId, String message) throws SQLException {
        String sql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, topicId);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }
}
