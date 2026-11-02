package dao;

import model.AssetRequest;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AssetRequestDAO {

    private static final int PAGE_SIZE = 10;

    /*
     * Các bảng và cột được sử dụng:
     * Asset_Request: request_id, topic_id, asset_id,
     *                 requester_user_id, start_time, end_time, status
     * Topic: topic_id, topic_code, title
     * Asset: asset_id, asset_name, asset_status
     * User: user_id, full_name, email
     */

    private static final String BASE_SELECT =
        "SELECT " +
        "ar.request_id, " +
        "ar.topic_id, " +
        "ar.asset_id, " +
        "ar.requester_user_id, " +
        "ar.start_time, " +
        "ar.end_time, " +
        "ar.status, " +
        "t.topic_code AS topic_code, " +
        "t.title AS topic_title, " +
        "a.asset_name AS asset_name, " +
        "a.asset_status AS asset_status, " +
        "u.full_name AS requester_name, " +
        "u.email AS requester_email " +
        "FROM Asset_Request ar " +
        "INNER JOIN Topic t ON ar.topic_id = t.topic_id " +
        "INNER JOIN Asset a ON ar.asset_id = a.asset_id " +
        "INNER JOIN [User] u ON ar.requester_user_id = u.user_id ";

    /*
     * Lấy danh sách yêu cầu có tìm kiếm, lọc và phân trang.
     */
    public List<AssetRequest> findAll(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            int page) throws SQLException {

        List<AssetRequest> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append(" WHERE 1 = 1 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                " AND (" +
                "t.title LIKE ? " +
                "OR t.topic_code LIKE ? " +
                "OR a.asset_name LIKE ? " +
                "OR u.full_name LIKE ? " +
                "OR u.email LIKE ?" +
                ") "
            );

            String search = "%" + keyword.trim() + "%";

            for (int i = 0; i < 5; i++) {
                params.add(search);
            }
        }

        if (status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status)) {

            sql.append(" AND ar.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append(" AND ar.start_time >= ? ");
            params.add(Timestamp.valueOf(fromDate.trim() + " 00:00:00"));
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND ar.start_time < DATEADD(DAY, 1, ?) ");
            params.add(java.sql.Date.valueOf(toDate.trim()));
        }

        sql.append(
            " ORDER BY ar.request_id DESC " +
            " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"
        );

        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        params.add(offset);
        params.add(PAGE_SIZE);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResult(rs));
                }
            }
        }

        return list;
    }

    /*
     * Đếm tổng số yêu cầu theo điều kiện tìm kiếm và lọc.
     */
    public int countAll(
            String keyword,
            String status,
            String fromDate,
            String toDate) throws SQLException {

        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) " +
            "FROM Asset_Request ar " +
            "INNER JOIN Topic t ON ar.topic_id = t.topic_id " +
            "INNER JOIN Asset a ON ar.asset_id = a.asset_id " +
            "INNER JOIN [User] u ON ar.requester_user_id = u.user_id " +
            "WHERE 1 = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                " AND (" +
                "t.title LIKE ? " +
                "OR t.topic_code LIKE ? " +
                "OR a.asset_name LIKE ? " +
                "OR u.full_name LIKE ? " +
                "OR u.email LIKE ?" +
                ") "
            );

            String search = "%" + keyword.trim() + "%";

            for (int i = 0; i < 5; i++) {
                params.add(search);
            }
        }

        if (status != null
                && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status)) {

            sql.append(" AND ar.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            sql.append(" AND ar.start_time >= ? ");
            params.add(Timestamp.valueOf(fromDate.trim() + " 00:00:00"));
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            sql.append(" AND ar.start_time < DATEADD(DAY, 1, ?) ");
            params.add(java.sql.Date.valueOf(toDate.trim()));
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            setParameters(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /*
     * Đếm số yêu cầu theo từng trạng thái.
     */
    public int countByStatus(String status) throws SQLException {

        String sql =
            "SELECT COUNT(*) " +
            "FROM Asset_Request " +
            "WHERE status = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /*
     * Lấy chi tiết một yêu cầu.
     */
    public AssetRequest findById(int requestId) throws SQLException {

        String sql = BASE_SELECT +
            " WHERE ar.request_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResult(rs);
                }
            }
        }

        return null;
    }

    /*
     * Kiểm tra các yêu cầu có thời gian sử dụng bị trùng.
     *
     * Các trạng thái chiếm lịch:
     * PENDING, APPROVED, CHECKED_OUT
     *
     * Không chiếm lịch:
     * REJECTED, RETURNED
     */
    public List<AssetRequest> findConflicts(
            int assetId,
            Timestamp startTime,
            Timestamp endTime,
            int excludeRequestId) throws SQLException {

        List<AssetRequest> conflicts = new ArrayList<>();

        String sql = BASE_SELECT +
            " WHERE ar.asset_id = ? " +
            " AND ar.request_id <> ? " +
            " AND ar.status IN ('PENDING', 'APPROVED', 'CHECKED_OUT') " +
            " AND ar.start_time < ? " +
            " AND ar.end_time > ? " +
            " ORDER BY ar.start_time ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ps.setInt(2, excludeRequestId);
            ps.setTimestamp(3, endTime);
            ps.setTimestamp(4, startTime);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    conflicts.add(mapResult(rs));
                }
            }
        }

        return conflicts;
    }

    /*
     * Duyệt yêu cầu.
     */
    public String approveRequest(int requestId) throws SQLException {
        return updateDecision(requestId, "APPROVED");
    }

    /*
     * Từ chối yêu cầu.
     */
    public String rejectRequest(int requestId) throws SQLException {
        return updateDecision(requestId, "REJECTED");
    }

    /*
     * Cập nhật trạng thái và tạo thông báo trong cùng transaction.
     *
     * Chỉ cho phép xử lý yêu cầu đang PENDING.
     */
    private String updateDecision(
            int requestId,
            String decision) throws SQLException {

        if (!"APPROVED".equals(decision)
                && !"REJECTED".equals(decision)) {
            throw new IllegalArgumentException(
                "Trạng thái xử lý không hợp lệ."
            );
        }

        String lockRequestSql =
            "SELECT " +
            "ar.request_id, ar.topic_id, ar.asset_id, " +
            "ar.requester_user_id, ar.start_time, ar.end_time, ar.status, " +
            "t.topic_code, t.title, a.asset_name, a.asset_status, " +
            "u.full_name " +
            "FROM Asset_Request ar WITH (UPDLOCK, HOLDLOCK) " +
            "INNER JOIN Topic t ON ar.topic_id = t.topic_id " +
            "INNER JOIN Asset a WITH (UPDLOCK, HOLDLOCK) " +
            "ON ar.asset_id = a.asset_id " +
            "INNER JOIN [User] u ON ar.requester_user_id = u.user_id " +
            "WHERE ar.request_id = ?";

        String conflictSql =
            "SELECT COUNT(*) " +
            "FROM Asset_Request WITH (UPDLOCK, HOLDLOCK) " +
            "WHERE asset_id = ? " +
            "AND request_id <> ? " +
            "AND status IN ('PENDING', 'APPROVED', 'CHECKED_OUT') " +
            "AND start_time < ? " +
            "AND end_time > ?";

        String updateSql =
            "UPDATE Asset_Request " +
            "SET status = ? " +
            "WHERE request_id = ? AND status = 'PENDING'";

        String notificationSql =
            "INSERT INTO Notification " +
            "(user_id, topic_id, message, is_read) " +
            "VALUES (?, ?, ?, 0)";

        try (Connection con = DBConnection.getConnection()) {

            boolean oldAutoCommit = con.getAutoCommit();

            try {
                con.setAutoCommit(false);

                int topicId;
                int assetId;
                int requesterId;
                Timestamp startTime;
                Timestamp endTime;
                String currentStatus;
                String topicCode;
                String topicTitle;
                String assetName;
                String assetStatus;

                // 1. Khóa và lấy thông tin yêu cầu.
                try (PreparedStatement ps =
                        con.prepareStatement(lockRequestSql)) {

                    ps.setInt(1, requestId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            return "NOT_FOUND";
                        }

                        currentStatus = rs.getString("status");

                        if (!"PENDING".equalsIgnoreCase(currentStatus)) {
                            con.rollback();
                            return "NOT_PENDING";
                        }

                        topicId = rs.getInt("topic_id");
                        assetId = rs.getInt("asset_id");
                        requesterId = rs.getInt("requester_user_id");
                        startTime = rs.getTimestamp("start_time");
                        endTime = rs.getTimestamp("end_time");
                        topicCode = rs.getString("topic_code");
                        topicTitle = rs.getString("title");
                        assetName = rs.getString("asset_name");
                        assetStatus = rs.getString("asset_status");
                    }
                }

                // 2. Nếu duyệt, kiểm tra tài sản và lịch bị trùng.
                if ("APPROVED".equals(decision)) {

                    if ("MAINTENANCE".equalsIgnoreCase(assetStatus)) {
                        con.rollback();
                        return "CONFLICT";
                    }

                    try (PreparedStatement ps =
                            con.prepareStatement(conflictSql)) {

                        ps.setInt(1, assetId);
                        ps.setInt(2, requestId);
                        ps.setTimestamp(3, endTime);
                        ps.setTimestamp(4, startTime);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                con.rollback();
                                return "CONFLICT";
                            }
                        }
                    }
                }

                // 3. Cập nhật trạng thái.
                int updated;

                try (PreparedStatement ps =
                        con.prepareStatement(updateSql)) {

                    ps.setString(1, decision);
                    ps.setInt(2, requestId);

                    updated = ps.executeUpdate();
                }

                if (updated != 1) {
                    con.rollback();
                    return "ERROR";
                }

                // 4. Tạo thông báo cho người gửi yêu cầu.
                String message;

                if ("APPROVED".equals(decision)) {
                    message =
                        "Yêu cầu sử dụng tài sản " + assetName +
                        " cho đề tài " + topicCode +
                        " - " + topicTitle +
                        " đã được phê duyệt.";
                } else {
                    message =
                        "Yêu cầu sử dụng tài sản " + assetName +
                        " cho đề tài " + topicCode +
                        " - " + topicTitle +
                        " đã bị từ chối.";
                }

                try (PreparedStatement ps =
                        con.prepareStatement(notificationSql)) {

                    ps.setInt(1, requesterId);
                    ps.setInt(2, topicId);
                    ps.setString(3, message);

                    ps.executeUpdate();
                }

                // 5. Hoàn tất transaction.
                con.commit();
                return decision;

            } catch (SQLException | RuntimeException ex) {

                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }

                throw ex;

            } finally {
                try {
                    con.setAutoCommit(oldAutoCommit);
                } catch (SQLException ex) {
                    // Không che mất lỗi SQL chính.
                }
            }
        }
    }

    /*
     * Ánh xạ dữ liệu SQL sang AssetRequest.
     *
     * Yêu cầu model AssetRequest có các setter tương ứng.
     */
    private AssetRequest mapResult(ResultSet rs) throws SQLException {

        AssetRequest request = new AssetRequest();

        request.setRequestId(rs.getInt("request_id"));
        request.setTopicId(rs.getInt("topic_id"));
        request.setAssetId(rs.getInt("asset_id"));
        request.setRequesterUserId(rs.getInt("requester_user_id"));

        request.setStartTime(rs.getTimestamp("start_time"));
        request.setEndTime(rs.getTimestamp("end_time"));
        request.setStatus(rs.getString("status"));

        request.setTopicCode(rs.getString("topic_code"));
        request.setTopicTitle(rs.getString("topic_title"));

        request.setAssetName(rs.getString("asset_name"));
        request.setAssetStatus(rs.getString("asset_status"));

        request.setRequesterName(rs.getString("requester_name"));
        request.setRequesterEmail(rs.getString("requester_email"));

        return request;
    }

    /*
     * Gán danh sách tham số vào PreparedStatement.
     */
    private void setParameters(
            PreparedStatement ps,
            List<Object> params) throws SQLException {

        for (int i = 0; i < params.size(); i++) {

            Object value = params.get(i);

            if (value instanceof Timestamp) {
                ps.setTimestamp(i + 1, (Timestamp) value);

            } else if (value instanceof java.sql.Date) {
                ps.setDate(i + 1, (java.sql.Date) value);

            } else if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);

            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }
}
