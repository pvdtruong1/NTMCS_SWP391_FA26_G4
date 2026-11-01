package dal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import util.Json;

/**
 * Read-only statistics for SCR-5.1 (Executive Dashboard) and SCR-5.5 (Performance Report Builder).
 * Rows are returned as Maps with snake_case keys, so the JSP pages (EL: ${row.total}) and the
 * JSON API (Json.toJson) can use the very same data.
 *
 * Words used below:
 *   approved = APPROVED, IN_PROGRESS, DELIVERED or ACCEPTED_READY_FOR_CMS (passed evaluation)
 *   pending  = SUBMITTED or PENDING_EXECUTIVE_APPROVAL (waiting for an editor)
 *   overdue  = APPROVED or IN_PROGRESS with a deadline before today
 * DRAFT topics are never counted: they have not entered the newsroom pipeline yet.
 */
public class ReportDAO extends DBContext {

    private static final String APPROVED_LIST = "('APPROVED','IN_PROGRESS','DELIVERED','ACCEPTED_READY_FOR_CMS')";
    private static final String TODAY = "CAST(GETDATE() AS DATE)";

    /** Report group -> { key expression, label expression }. Only these are ever put into SQL. */
    private static final Map<String, String[]> GROUP_SQL = new LinkedHashMap<>();

    static {
        GROUP_SQL.put("desk", new String[]{"d.desk_id", "d.desk_name"});
        GROUP_SQL.put("category", new String[]{"c.category_id", "c.category_name"});
        GROUP_SQL.put("reporter", new String[]{"u.user_id", "u.full_name"});
        GROUP_SQL.put("status", new String[]{"t.status", "t.status"});
    }

    // =====================================================================
    //  SCR-5.5 PERFORMANCE REPORT
    // =====================================================================
    /**
     * One row per group (desk, category, reporter or status) with every metric.
     * groupBy = null gives a single "Total" row over all matching topics.
     * from / to filter on the topic deadline (both included); null = no limit. deskId 0 = all desks.
     */
    public List<Map<String, Object>> buildReport(String groupBy, Date from, Date to, int deskId) {
        String[] group = (groupBy == null) ? null : GROUP_SQL.get(groupBy);
        if (groupBy != null && group == null) {
            throw new IllegalArgumentException("Unknown group: " + groupBy);
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(group == null ? "N'Total'" : group[1]).append(" AS group_label, ")
                .append("COUNT(*) AS total, ")
                .append("SUM(CASE WHEN t.status IN ").append(APPROVED_LIST).append(" THEN 1 ELSE 0 END) AS approved, ")
                .append("SUM(CASE WHEN t.status IN ('SUBMITTED','PENDING_EXECUTIVE_APPROVAL') THEN 1 ELSE 0 END) AS pending, ")
                .append("SUM(CASE WHEN t.status = 'REVISION_REQUESTED' THEN 1 ELSE 0 END) AS revision, ")
                .append("SUM(CASE WHEN t.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected, ")
                .append("SUM(CASE WHEN t.status IN ('APPROVED','IN_PROGRESS') AND t.deadline < ").append(TODAY)
                .append(" THEN 1 ELSE 0 END) AS overdue, ")
                .append("AVG(s.score) AS avg_score, ")
                .append("SUM(ISNULL(e.amount, 0)) AS estimate_total, ")
                .append("SUM(ISNULL(r.cnt, 0)) AS asset_requests ")
                .append("FROM Topic t ")
                .append("JOIN Category c ON t.category_id = c.category_id ")
                .append("JOIN Desk d ON c.desk_id = d.desk_id ")
                .append("JOIN [User] u ON t.author_user_id = u.user_id ")
                // the satellites are summed per topic first, so joining them never repeats a topic
                .append("LEFT JOIN (SELECT topic_id, SUM(amount) AS amount FROM Topic_Estimate GROUP BY topic_id) e ")
                .append("ON e.topic_id = t.topic_id ")
                .append("LEFT JOIN (SELECT topic_id, COUNT(*) AS cnt FROM Asset_Request GROUP BY topic_id) r ")
                .append("ON r.topic_id = t.topic_id ")
                // weighted desk-review score of a topic: sum(score x weight) / sum(weight)
                .append("LEFT JOIN (SELECT te.topic_id, ")
                .append("SUM(ed.score * cc.weight_percent) / NULLIF(SUM(cc.weight_percent), 0) AS score ")
                .append("FROM Topic_Evaluation te ")
                .append("JOIN Evaluation_Detail ed ON ed.evaluation_id = te.evaluation_id ")
                .append("JOIN Criteria_Config cc ON cc.criteria_id = ed.criteria_id ")
                .append("WHERE te.evaluation_stage = 'DESK_REVIEW' GROUP BY te.topic_id) s ")
                .append("ON s.topic_id = t.topic_id ")
                .append("WHERE t.status <> 'DRAFT' ");
        if (from != null) {
            sql.append("AND t.deadline >= ? ");
        }
        if (to != null) {
            sql.append("AND t.deadline <= ? ");
        }
        if (deskId > 0) {
            sql.append("AND d.desk_id = ? ");
        }
        if (group != null) {
            sql.append("GROUP BY ").append(group[0]).append(", ").append(group[1])
                    .append(" ORDER BY ").append(group[1]);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (from != null) {
                ps.setDate(i++, from);
            }
            if (to != null) {
                ps.setDate(i++, to);
            }
            if (deskId > 0) {
                ps.setInt(i++, deskId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapReportRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    private Map<String, Object> mapReportRow(ResultSet rs) throws Exception {
        int approved = rs.getInt("approved");
        int rejected = rs.getInt("rejected");
        BigDecimal score = rs.getBigDecimal("avg_score");
        BigDecimal estimate = rs.getBigDecimal("estimate_total");
        return Json.obj(
                "group_label", rs.getString("group_label"),
                "total", rs.getInt("total"),
                "approved", approved,
                "pending", rs.getInt("pending"),
                "revision", rs.getInt("revision"),
                "rejected", rejected,
                "approval_rate", percent(approved, approved + rejected),
                "overdue", rs.getInt("overdue"),
                "avg_score", score == null ? null : score.setScale(2, RoundingMode.HALF_UP),
                "estimate_total", estimate == null ? BigDecimal.ZERO : estimate.setScale(0, RoundingMode.HALF_UP),
                "asset_requests", rs.getInt("asset_requests"));
    }

    // =====================================================================
    //  SCR-5.1 EXECUTIVE DASHBOARD
    // =====================================================================
    /**
     * Everything the dashboard shows. period is a valid quota period (2026-10 or 2026-Q4);
     * the quota progress counts approved topics whose deadline falls inside it.
     */
    public Map<String, Object> getDashboard(String period, LocalDate periodStart, LocalDate periodEnd) {
        Map<String, Integer> statusCounts = getStatusCounts();
        int total = 0;
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            if (!"DRAFT".equals(entry.getKey())) {
                total += entry.getValue();
            }
        }
        int approved = count(statusCounts, "APPROVED") + count(statusCounts, "IN_PROGRESS")
                + count(statusCounts, "DELIVERED") + count(statusCounts, "ACCEPTED_READY_FOR_CMS");
        int rejected = count(statusCounts, "REJECTED");
        int finished = count(statusCounts, "DELIVERED") + count(statusCounts, "ACCEPTED_READY_FOR_CMS");
        List<Map<String, Object>> overdueTopics = getTopics(
                "t.status IN ('APPROVED','IN_PROGRESS') AND t.deadline < " + TODAY, "t.deadline");

        // status -> count, in the order of the topic life cycle
        List<Map<String, Object>> statusList = new ArrayList<>();
        String[] order = {"SUBMITTED", "REVISION_REQUESTED", "PENDING_EXECUTIVE_APPROVAL", "APPROVED",
            "IN_PROGRESS", "DELIVERED", "ACCEPTED_READY_FOR_CMS", "REJECTED", "ABORTED", "DRAFT"};
        for (String status : order) {
            statusList.add(Json.obj("status", status, "count", count(statusCounts, status)));
        }

        Map<String, Integer> assetCounts = getAssetCounts();
        int assetsTotal = 0;
        for (int n : assetCounts.values()) {
            assetsTotal += n;
        }

        return Json.obj(
                "period", period,
                "total_topics", total,
                "approved_topics", approved,
                "rejected_topics", rejected,
                "pending_topics", count(statusCounts, "SUBMITTED") + count(statusCounts, "PENDING_EXECUTIVE_APPROVAL"),
                "pending_executive", count(statusCounts, "PENDING_EXECUTIVE_APPROVAL"),
                "approval_rate", percent(approved, approved + rejected),
                // no delivery date is stored, so "on time" = finished topics out of (finished + overdue)
                "finished_topics", finished,
                "overdue_count", overdueTopics.size(),
                "on_time_rate", percent(finished, finished + overdueTopics.size()),
                "estimate_total", getEstimateTotal(),
                "assets_total", assetsTotal,
                "assets_checked_out", count(assetCounts, "CHECKED_OUT"),
                "assets_maintenance", count(assetCounts, "MAINTENANCE"),
                "status_counts", statusList,
                "quota_progress", getQuotaProgress(period, periodStart, periodEnd),
                "overdue_topics", overdueTopics,
                "executive_queue", getTopics("t.status = 'PENDING_EXECUTIVE_APPROVAL'", "t.deadline"));
    }

    private Map<String, Integer> getStatusCounts() {
        return countBy("SELECT status, COUNT(*) FROM Topic GROUP BY status");
    }

    private Map<String, Integer> getAssetCounts() {
        return countBy("SELECT asset_status, COUNT(*) FROM Asset GROUP BY asset_status");
    }

    /** Runs "SELECT text, COUNT(*) ... GROUP BY text" and returns text -> count. */
    private Map<String, Integer> countBy(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private BigDecimal getEstimateTotal() {
        String sql = "SELECT SUM(e.amount) FROM Topic_Estimate e JOIN Topic t ON e.topic_id = t.topic_id "
                + "WHERE t.status <> 'DRAFT'";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getBigDecimal(1) != null) {
                return rs.getBigDecimal(1).setScale(0, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /** Every desk with its quota in the period (null if none) and how many approved topics are due in it. */
    private List<Map<String, Object>> getQuotaProgress(String period, LocalDate start, LocalDate end) {
        String sql = "SELECT d.desk_id, d.desk_name, q.quota_count, "
                + "(SELECT COUNT(*) FROM Topic t JOIN Category c ON t.category_id = c.category_id "
                + " WHERE c.desk_id = d.desk_id AND t.status IN " + APPROVED_LIST
                + " AND t.deadline >= ? AND t.deadline < ?) AS actual "
                + "FROM Desk d LEFT JOIN Desk_Quota q ON q.desk_id = d.desk_id AND q.period = ? "
                + "ORDER BY d.desk_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int quota = rs.getInt("quota_count");
                    Integer quotaCount = rs.wasNull() ? null : quota;
                    int actual = rs.getInt("actual");
                    list.add(Json.obj(
                            "desk_id", rs.getInt("desk_id"),
                            "desk_name", rs.getString("desk_name"),
                            "quota_count", quotaCount,
                            "actual", actual,
                            "percent", quotaCount == null ? null : percent(actual, quotaCount)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Topics matching a fixed condition (never user text), with their desk and author. */
    private List<Map<String, Object>> getTopics(String condition, String orderBy) {
        String sql = "SELECT t.topic_id, t.topic_code, t.title, t.status, t.deadline, "
                + "d.desk_name, u.full_name AS author_name "
                + "FROM Topic t JOIN Category c ON t.category_id = c.category_id "
                + "JOIN Desk d ON c.desk_id = d.desk_id "
                + "JOIN [User] u ON t.author_user_id = u.user_id "
                + "WHERE " + condition + " ORDER BY " + orderBy;
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date deadline = rs.getDate("deadline");
                list.add(Json.obj(
                        "topic_id", rs.getInt("topic_id"),
                        "topic_code", rs.getString("topic_code"),
                        "title", rs.getString("title"),
                        "status", rs.getString("status"),
                        "deadline", deadline == null ? null : deadline.toString(),
                        "desk_name", rs.getString("desk_name"),
                        "author_name", rs.getString("author_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =====================================================================
    //  SMALL HELPERS
    // =====================================================================
    private static int count(Map<String, Integer> map, String key) {
        Integer n = map.get(key);
        return (n == null) ? 0 : n;
    }

    /** part / whole as a percentage with 1 decimal, or null when whole is 0. */
    private static BigDecimal percent(int part, int whole) {
        if (whole == 0) {
            return null;
        }
        return new BigDecimal(part * 100).divide(new BigDecimal(whole), 1, RoundingMode.HALF_UP);
    }
}
