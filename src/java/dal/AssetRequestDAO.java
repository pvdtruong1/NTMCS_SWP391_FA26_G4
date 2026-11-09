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

/**
 * Data Access Object for Asset Requests (WF-03 Logistics & Asset Management).
 * Extends DBContext to inherit unified connection pooling.
 */
public class AssetRequestDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(AssetRequestDAO.class.getName());
    private static final int PAGE_SIZE = 10;

    private static final String BASE_SELECT =
        "SELECT ar.request_id, ar.topic_id, ar.asset_id, ar.requester_user_id, "
        + "ar.start_time, ar.end_time, ar.status, "
        + "t.topic_code, t.title AS topic_title, "
        + "a.asset_name, a.asset_code, a.asset_status, "
        + "u.full_name AS requester_name, u.email AS requester_email "
        + "FROM Asset_Request ar "
        + "JOIN Topic t ON t.topic_id = ar.topic_id "
        + "JOIN Asset a ON a.asset_id = ar.asset_id "
        + "JOIN [User] u ON u.user_id = ar.requester_user_id ";

    private static final String SCHEDULE_STATUSES =
        "'PENDING','APPROVED','CHECKED_OUT'";

    private static final String BLOCKING_STATUSES =
        "'APPROVED','CHECKED_OUT'";

    private String buildWhere(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            List<Object> params) {
        return buildWhere(keyword, status, fromDate, toDate, 0, params);
    }

    private String buildWhere(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            int topicId,
            List<Object> params) {

        StringBuilder sql = new StringBuilder(" WHERE 1=1 ");

        if (topicId > 0) {
            sql.append(" AND ar.topic_id = ? ");
            params.add(topicId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                " AND (t.title LIKE ? OR t.topic_code LIKE ? "
                + "OR a.asset_name LIKE ? OR a.asset_code LIKE ? "
                + "OR u.full_name LIKE ? OR u.email LIKE ?) "
            );

            String value = "%" + keyword.trim() + "%";

            for (int i = 0; i < 6; i++) {
                params.add(value);
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
            params.add(Timestamp.valueOf(toDate.trim() + " 00:00:00"));
        }

        return sql.toString();
    }

    private void bind(PreparedStatement ps, List<Object> params)
            throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private AssetRequest map(ResultSet rs) throws SQLException {
        AssetRequest item = new AssetRequest();

        item.setRequestId(rs.getInt("request_id"));
        item.setTopicId(rs.getInt("topic_id"));
        item.setAssetId(rs.getInt("asset_id"));
        item.setRequesterUserId(rs.getInt("requester_user_id"));

        item.setStartTime(rs.getTimestamp("start_time"));
        item.setEndTime(rs.getTimestamp("end_time"));
        item.setStatus(rs.getString("status"));

        item.setTopicCode(rs.getString("topic_code"));
        item.setTopicTitle(rs.getString("topic_title"));

        item.setAssetName(rs.getString("asset_name"));
        item.setAssetCode(rs.getString("asset_code"));
        item.setAssetStatus(rs.getString("asset_status"));

        item.setRequesterName(rs.getString("requester_name"));
        item.setRequesterEmail(rs.getString("requester_email"));

        return item;
    }

    public List<AssetRequest> findAll(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            int page) throws SQLException {
        return findAll(keyword, status, fromDate, toDate, 0, page);
    }

    public List<AssetRequest> findAll(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            int topicId,
            int page) throws SQLException {

        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, status, fromDate, toDate, topicId, params);

        String sql = BASE_SELECT + where
                + " ORDER BY ar.request_id DESC "
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<AssetRequest> result = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, params);
            ps.setInt(params.size() + 1, (Math.max(1, page) - 1) * PAGE_SIZE);
            ps.setInt(params.size() + 2, PAGE_SIZE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public int countAll(
            String keyword,
            String status,
            String fromDate,
            String toDate) throws SQLException {
        return countAll(keyword, status, fromDate, toDate, 0);
    }

    public int countAll(
            String keyword,
            String status,
            String fromDate,
            String toDate,
            int topicId) throws SQLException {

        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, status, fromDate, toDate, topicId, params);

        String sql = "SELECT COUNT(*) FROM Asset_Request ar "
                   + "JOIN Topic t ON t.topic_id = ar.topic_id "
                   + "JOIN Asset a ON a.asset_id = ar.asset_id "
                   + "JOIN [User] u ON u.user_id = ar.requester_user_id "
                   + where;

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public int countByStatus(String status) throws SQLException {
        return countByStatus(status, 0);
    }

    public int countByStatus(String status, int topicId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Asset_Request WHERE status = ?";
        if (topicId > 0) {
            sql += " AND topic_id = ?";
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status.trim().toUpperCase());
            if (topicId > 0) {
                ps.setInt(2, topicId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public AssetRequest findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE ar.request_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }

        return null;
    }

    public List<AssetRequest> findConflicts(
            int assetId,
            Timestamp start,
            Timestamp end,
            int excludeRequestId) throws SQLException {

        String sql = BASE_SELECT
            + " WHERE ar.asset_id = ? "
            + " AND ar.request_id <> ? "
            + " AND ar.status IN (" + SCHEDULE_STATUSES + ") "
            + " AND ar.start_time < ? "
            + " AND ar.end_time > ? "
            + " ORDER BY ar.start_time ASC";

        List<AssetRequest> result = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ps.setInt(2, excludeRequestId);
            ps.setTimestamp(3, end);
            ps.setTimestamp(4, start);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public String approveRequest(int requestId) throws SQLException {
        return decide(requestId, "APPROVED");
    }

    public String rejectRequest(int requestId) throws SQLException {
        return decide(requestId, "REJECTED");
    }

    private String decide(int requestId, String decision)
            throws SQLException {

        try (Connection con = getConnection()) {

            boolean oldAutoCommit = con.getAutoCommit();
            int oldIsolation = con.getTransactionIsolation();

            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);

                RequestLock item = lockRequest(con, requestId);

                if (item == null) {
                    con.rollback();
                    return "NOT_FOUND";
                }

                if (!"PENDING".equals(item.status)) {
                    con.rollback();
                    return "NOT_PENDING";
                }

                if ("APPROVED".equals(decision)) {
                    if ("MAINTENANCE".equals(item.assetStatus)) {
                        con.rollback();
                        return "ASSET_MAINTENANCE";
                    }

                    if (hasConflict(con, item.assetId, item.requestId, item.start, item.end)) {
                        con.rollback();
                        return "CONFLICT";
                    }
                }

                String updateSql =
                    "UPDATE Asset_Request "
                    + "SET status = ? "
                    + "WHERE request_id = ? AND status = 'PENDING'";

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, decision);
                    ps.setInt(2, requestId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return "NOT_PENDING";
                    }
                }

                String message =
                    "Yêu cầu #" + requestId
                    + " sử dụng tài sản " + item.assetName
                    + " cho đề tài " + item.topicTitle
                    + ("APPROVED".equals(decision)
                        ? " đã được phê duyệt."
                        : " đã bị từ chối.");

                insertNotification(con, item.requesterId, item.topicId, message);

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
                    con.setTransactionIsolation(oldIsolation);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public String checkoutRequest(int requestId) throws SQLException {

        try (Connection con = getConnection()) {

            boolean oldAutoCommit = con.getAutoCommit();
            int oldIsolation = con.getTransactionIsolation();

            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);

                RequestLock item = lockRequest(con, requestId);

                if (item == null) {
                    con.rollback();
                    return "NOT_FOUND";
                }

                if (!"APPROVED".equals(item.status)) {
                    con.rollback();
                    return "NOT_APPROVED";
                }

                if (!"AVAILABLE".equals(item.assetStatus)) {
                    con.rollback();
                    return "ASSET_NOT_AVAILABLE";
                }

                if (hasOtherCheckedOut(con, item.assetId, requestId)) {
                    con.rollback();
                    return "ASSET_NOT_AVAILABLE";
                }

                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Asset_Request "
                    + "SET status = 'CHECKED_OUT' "
                    + "WHERE request_id = ? AND status = 'APPROVED'")) {

                    ps.setInt(1, requestId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return "NOT_APPROVED";
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Asset "
                    + "SET asset_status = 'CHECKED_OUT' "
                    + "WHERE asset_id = ? AND asset_status = 'AVAILABLE'")) {

                    ps.setInt(1, item.assetId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return "ASSET_NOT_AVAILABLE";
                    }
                }

                insertNotification(
                    con,
                    item.requesterId,
                    item.topicId,
                    "Tài sản " + item.assetName
                    + " của yêu cầu #" + requestId
                    + " đã được bàn giao."
                );

                con.commit();
                return "CHECKED_OUT";

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
                    con.setTransactionIsolation(oldIsolation);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public String returnRequest(int requestId) throws SQLException {
        return returnRequest(requestId, "AVAILABLE");
    }

    public String returnRequest(
            int requestId,
            String finalAssetStatus) throws SQLException {

        if (!"AVAILABLE".equals(finalAssetStatus)
                && !"MAINTENANCE".equals(finalAssetStatus)) {
            throw new IllegalArgumentException(
                "Trạng thái sau thu hồi không hợp lệ: " + finalAssetStatus
            );
        }

        try (Connection con = getConnection()) {

            boolean oldAutoCommit = con.getAutoCommit();
            int oldIsolation = con.getTransactionIsolation();

            try {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);

                RequestLock item = lockRequest(con, requestId);

                if (item == null) {
                    con.rollback();
                    return "NOT_FOUND";
                }

                if (!"CHECKED_OUT".equals(item.status)) {
                    con.rollback();
                    return "NOT_CHECKED_OUT";
                }

                try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE Asset_Request "
                    + "SET status = 'RETURNED' "
                    + "WHERE request_id = ? AND status = 'CHECKED_OUT'")) {

                    ps.setInt(1, requestId);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return "NOT_CHECKED_OUT";
                    }
                }

                if (!hasOtherCheckedOut(con, item.assetId, requestId)) {
                    try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Asset "
                        + "SET asset_status = ? "
                        + "WHERE asset_id = ? AND asset_status = 'CHECKED_OUT'")) {

                        ps.setString(1, finalAssetStatus);
                        ps.setInt(2, item.assetId);
                        ps.executeUpdate();
                    }
                }

                insertNotification(
                    con,
                    item.requesterId,
                    item.topicId,
                    "Tài sản " + item.assetName
                    + " của yêu cầu #" + requestId
                    + " đã được thu hồi. Trạng thái sau kiểm tra: "
                    + finalAssetStatus + "."
                );

                con.commit();
                return "RETURNED";

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
                    con.setTransactionIsolation(oldIsolation);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public List<AssetRequest> findHistoryByAssetId(int assetId) throws SQLException {
        String sql = BASE_SELECT
            + " WHERE ar.asset_id = ? "
            + " AND ar.status IN ('CHECKED_OUT','RETURNED') "
            + " ORDER BY ar.start_time DESC, ar.request_id DESC";

        List<AssetRequest> result = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public List<AssetRequest> findHistoryByAssetId(
            int assetId, int page) throws SQLException {

        String sql = BASE_SELECT
            + " WHERE ar.asset_id = ? "
            + " AND ar.status IN ('CHECKED_OUT','RETURNED') "
            + " ORDER BY ar.start_time DESC, ar.request_id DESC "
            + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        List<AssetRequest> result = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ps.setInt(2, (Math.max(1, page) - 1) * PAGE_SIZE);
            ps.setInt(3, PAGE_SIZE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    public int countHistoryByAssetId(int assetId) throws SQLException {
        String sql =
            "SELECT COUNT(*) FROM Asset_Request "
            + "WHERE asset_id = ? "
            + "AND status IN ('CHECKED_OUT','RETURNED')";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private static class RequestLock {
        int requestId;
        int topicId;
        int assetId;
        int requesterId;

        Timestamp start;
        Timestamp end;

        String status;
        String assetStatus;
        String assetName;
        String topicTitle;
    }

    private RequestLock lockRequest(Connection con, int id)
            throws SQLException {

        String sql =
            "SELECT ar.request_id, ar.topic_id, ar.asset_id, "
            + "ar.requester_user_id, ar.start_time, ar.end_time, "
            + "ar.status, a.asset_status, a.asset_name, t.title "
            + "FROM Asset_Request ar WITH (UPDLOCK,HOLDLOCK) "
            + "JOIN Asset a WITH (UPDLOCK,HOLDLOCK) "
            + "ON a.asset_id = ar.asset_id "
            + "JOIN Topic t ON t.topic_id = ar.topic_id "
            + "WHERE ar.request_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                RequestLock item = new RequestLock();

                item.requestId = rs.getInt("request_id");
                item.topicId = rs.getInt("topic_id");
                item.assetId = rs.getInt("asset_id");
                item.requesterId = rs.getInt("requester_user_id");

                item.start = rs.getTimestamp("start_time");
                item.end = rs.getTimestamp("end_time");

                item.status = rs.getString("status");
                item.assetStatus = rs.getString("asset_status");
                item.assetName = rs.getString("asset_name");
                item.topicTitle = rs.getString("title");

                return item;
            }
        }
    }

    private boolean hasConflict(
            Connection con,
            int assetId,
            int excludeId,
            Timestamp start,
            Timestamp end) throws SQLException {

        String sql =
            "SELECT COUNT(*) "
            + "FROM Asset_Request WITH (UPDLOCK,HOLDLOCK) "
            + "WHERE asset_id = ? "
            + "AND request_id <> ? "
            + "AND status IN (" + BLOCKING_STATUSES + ") "
            + "AND start_time < ? "
            + "AND end_time > ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ps.setInt(2, excludeId);
            ps.setTimestamp(3, end);
            ps.setTimestamp(4, start);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean hasOtherCheckedOut(
            Connection con,
            int assetId,
            int excludeId) throws SQLException {

        String sql =
            "SELECT COUNT(*) "
            + "FROM Asset_Request WITH (UPDLOCK,HOLDLOCK) "
            + "WHERE asset_id = ? "
            + "AND request_id <> ? "
            + "AND status = 'CHECKED_OUT'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, assetId);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void insertNotification(
            Connection con,
            int userId,
            int topicId,
            String message) {

        String sql =
            "INSERT INTO Notification "
            + "(user_id, topic_id, message, is_read) "
            + "VALUES (?, ?, ?, 0)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, topicId);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Non-blocking notification: if notification table is missing or fails, business transaction completes
        }
    }

    public int getPageSize() {
        return PAGE_SIZE;
    }
}