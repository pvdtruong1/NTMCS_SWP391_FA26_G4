package dal;

import dto.TopicArchiveDTO;
import dto.TopicEvaluationDetailDTO;
import dto.TopicPendingDTO;
import model.Category;
import model.CriteriaConfig;
import model.Desk;
import model.TopicEstimate;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.Year;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Topic operations, supporting Editorial Workflow (WF-02).
 * Reuses parent DBContext connection across queries for high-throughput, low-latency execution.
 */
public class TopicDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(TopicDAO.class.getName());
    private static volatile List<CriteriaConfig> CACHED_CRITERIA = null;
    private static final Map<Integer, Desk> CACHED_DESKS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Retrieve all pending and escalated topics for a specific desk in a single query.
     */
    public List<TopicPendingDTO> getDeskPendingTopics(int deskId, String keyword) {
        List<TopicPendingDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.topic_id, t.topic_code, t.title, t.angle, t.status, t.deadline, ")
           .append("       c.category_id, c.category_name, d.desk_id, d.desk_name, ")
           .append("       u.user_id AS author_user_id, u.full_name AS author_name, u.email AS author_email ")
           .append("FROM Topic t ")
           .append("INNER JOIN Category c ON t.category_id = c.category_id ")
           .append("INNER JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("INNER JOIN [User] u ON t.author_user_id = u.user_id ")
           .append("WHERE d.desk_id = ? ")
           .append("  AND t.status IN ('SUBMITTED', 'PENDING_EXECUTIVE_APPROVAL') ");

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.angle LIKE ? OR u.full_name LIKE ?) ");
        }

        sql.append("ORDER BY t.topic_id DESC;");

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, deskId);

                if (hasKeyword) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TopicPendingDTO dto = new TopicPendingDTO(
                                rs.getInt("topic_id"),
                                rs.getString("topic_code"),
                                rs.getString("title"),
                                rs.getString("angle"),
                                rs.getString("status"),
                                rs.getDate("deadline"),
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getInt("desk_id"),
                                rs.getString("desk_name"),
                                rs.getInt("author_user_id"),
                                rs.getString("author_name"),
                                rs.getString("author_email")
                        );
                        list.add(dto);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getDeskPendingTopics", ex);
        }
        return list;
    }

    /**
     * Retrieve paginated pending topics for a specific desk and status, with optional keyword search.
     *
     * @param deskId Desk identifier
     * @param status Topic status (e.g. 'SUBMITTED', 'PENDING_EXECUTIVE_APPROVAL')
     * @param keyword Search keyword (searches title, angle, author name)
     * @param page Current page (1-based index)
     * @param pageSize Records per page
     * @return List of TopicPendingDTO
     */
    public List<TopicPendingDTO> getPendingTopics(int deskId, String status, String keyword, int page, int pageSize) {
        List<TopicPendingDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.topic_id, t.topic_code, t.title, t.angle, t.status, t.deadline, ")
           .append("       c.category_id, c.category_name, d.desk_id, d.desk_name, ")
           .append("       u.user_id AS author_user_id, u.full_name AS author_name, u.email AS author_email ")
           .append("FROM Topic t ")
           .append("INNER JOIN Category c ON t.category_id = c.category_id ")
           .append("INNER JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("INNER JOIN [User] u ON t.author_user_id = u.user_id ")
           .append("WHERE d.desk_id = ? ")
           .append("  AND t.status = ? ");

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.angle LIKE ? OR u.full_name LIKE ?) ");
        }

        sql.append("ORDER BY t.topic_id DESC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;");

        int offset = (page - 1) * pageSize;

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, deskId);
                ps.setString(paramIndex++, status);

                if (hasKeyword) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                }

                ps.setInt(paramIndex++, offset);
                ps.setInt(paramIndex, pageSize);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TopicPendingDTO dto = new TopicPendingDTO(
                                rs.getInt("topic_id"),
                                rs.getString("topic_code"),
                                rs.getString("title"),
                                rs.getString("angle"),
                                rs.getString("status"),
                                rs.getDate("deadline"),
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getInt("desk_id"),
                                rs.getString("desk_name"),
                                rs.getInt("author_user_id"),
                                rs.getString("author_name"),
                                rs.getString("author_email")
                        );
                        list.add(dto);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getPendingTopics", ex);
        }
        return list;
    }

    /**
     * Count total pending topics matching filter criteria.
     */
    public int countPendingTopics(int deskId, String status, String keyword) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
           .append("FROM Topic t ")
           .append("INNER JOIN Category c ON t.category_id = c.category_id ")
           .append("INNER JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("INNER JOIN [User] u ON t.author_user_id = u.user_id ")
           .append("WHERE d.desk_id = ? ")
           .append("  AND t.status = ? ");

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.angle LIKE ? OR u.full_name LIKE ?) ");
        }

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, deskId);
                ps.setString(paramIndex++, status);

                if (hasKeyword) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex, pattern);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in countPendingTopics", ex);
        }
        return 0;
    }

    /**
     * Single aggregated query retrieving badge counts for both SUBMITTED and PENDING_EXECUTIVE_APPROVAL.
     */
    public Map<String, Integer> getBadgeCounts(int deskId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("SUBMITTED", 0);
        counts.put("PENDING_EXECUTIVE_APPROVAL", 0);

        String sql = "SELECT t.status, COUNT(*) AS cnt FROM Topic t " +
                     "INNER JOIN Category c ON t.category_id = c.category_id " +
                     "WHERE c.desk_id = ? AND t.status IN ('SUBMITTED', 'PENDING_EXECUTIVE_APPROVAL') " +
                     "GROUP BY t.status;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        counts.put(rs.getString("status"), rs.getInt("cnt"));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getBadgeCounts", ex);
        }
        return counts;
    }

    /**
     * Fast count by status and desk (kept for backward compatibility).
     */
    public int countByStatusAndDesk(int deskId, String status) {
        Map<String, Integer> map = getBadgeCounts(deskId);
        return map.getOrDefault(status, 0);
    }

    /**
     * Retrieve Desk information by deskId.
     */
    public Desk getDeskById(int deskId) {
        Desk cached = CACHED_DESKS.get(deskId);
        if (cached != null) {
            return cached;
        }
        String sql = "SELECT desk_id, desk_name FROM Desk WHERE desk_id = ?;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Desk desk = new Desk(rs.getInt("desk_id"), rs.getString("desk_name"));
                        CACHED_DESKS.put(deskId, desk);
                        return desk;
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getDeskById", ex);
        }
        return null;
    }

    /**
     * Retrieve all active rubric evaluation criteria (Criteria_Config).
     */
    public List<CriteriaConfig> getCriteriaConfigs() {
        if (CACHED_CRITERIA != null && !CACHED_CRITERIA.isEmpty()) {
            return CACHED_CRITERIA;
        }
        List<CriteriaConfig> list = new ArrayList<>();
        String sql = "SELECT criteria_id, criteria_name, weight_percent FROM Criteria_Config ORDER BY criteria_id ASC;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CriteriaConfig(
                            rs.getInt("criteria_id"),
                            rs.getString("criteria_name"),
                            rs.getDouble("weight_percent")
                    ));
                }
            }
            CACHED_CRITERIA = list;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getCriteriaConfigs", ex);
        }
        return list;
    }

    /**
     * Retrieve complete topic dossier including itemized cost estimates for SCR-2.3 evaluation desk.
     */
    public TopicEvaluationDetailDTO getTopicDetailForEvaluation(int topicId) {
        String topicSql = "SELECT t.topic_id, t.topic_code, t.title, t.angle, t.status, t.deadline, " +
                          "       c.category_id, c.category_name, d.desk_id, d.desk_name, " +
                          "       u.user_id AS author_user_id, u.full_name AS author_name, u.email AS author_email " +
                          "FROM Topic t " +
                          "INNER JOIN Category c ON t.category_id = c.category_id " +
                          "INNER JOIN Desk d ON c.desk_id = d.desk_id " +
                          "INNER JOIN [User] u ON t.author_user_id = u.user_id " +
                          "WHERE t.topic_id = ?;";

        String estimateSql = "SELECT estimate_id, topic_id, cost_item, amount " +
                             "FROM Topic_Estimate " +
                             "WHERE topic_id = ? " +
                             "ORDER BY estimate_id ASC;";

        try {
            Connection conn = getConnection();
            TopicEvaluationDetailDTO dto = null;

            try (PreparedStatement ps = conn.prepareStatement(topicSql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dto = new TopicEvaluationDetailDTO(
                                rs.getInt("topic_id"),
                                rs.getString("topic_code"),
                                rs.getString("title"),
                                rs.getString("angle"),
                                rs.getString("status"),
                                rs.getDate("deadline") != null ? rs.getDate("deadline").toString() : null,
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getInt("desk_id"),
                                rs.getString("desk_name"),
                                rs.getInt("author_user_id"),
                                rs.getString("author_name"),
                                rs.getString("author_email")
                        );
                    }
                }
            }

            if (dto != null) {
                // Fetch Estimates
                List<TopicEstimate> estimates = new ArrayList<>();
                try (PreparedStatement psEst = conn.prepareStatement(estimateSql)) {
                    psEst.setInt(1, topicId);
                    try (ResultSet rsEst = psEst.executeQuery()) {
                        while (rsEst.next()) {
                            estimates.add(new TopicEstimate(
                                    rsEst.getInt("estimate_id"),
                                    rsEst.getInt("topic_id"),
                                    rsEst.getString("cost_item"),
                                    rsEst.getBigDecimal("amount")
                            ));
                        }
                    }
                }
                dto.setEstimates(estimates);

                // Fetch Criteria Configs
                dto.setCriteriaList(getCriteriaConfigs());
            }

            return dto;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getTopicDetailForEvaluation for topicId " + topicId, ex);
        }
        return null;
    }

    /**
     * Executes atomic ACID transaction for topic evaluation:
     * 1. Inserts Topic_Evaluation record
     * 2. Inserts individual Evaluation_Detail criterion scores
     * 3. Updates Topic.status (and mints topic_code if APPROVED)
     * 4. Inserts Notification for topic author
     */
    public boolean evaluateTopic(int topicId, int evaluatorUserId, String decision, String comments,
                                 Map<Integer, Double> criteriaScores, Map<Integer, String> criteriaComments,
                                 double legalRiskScore) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 1. Insert into Topic_Evaluation
            int evalId = 0;
            String insertEvalSql = "INSERT INTO Topic_Evaluation (topic_id, evaluator_user_id, evaluation_stage, decision, comments) " +
                                  "VALUES (?, ?, 'DESK_REVIEW', ?, ?);";
            try (PreparedStatement ps = conn.prepareStatement(insertEvalSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, topicId);
                ps.setInt(2, evaluatorUserId);
                ps.setString(3, decision);
                ps.setString(4, comments);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        evalId = rs.getInt(1);
                    }
                }
            }

            // 2. Insert into Evaluation_Detail
            if (evalId > 0 && criteriaScores != null && !criteriaScores.isEmpty()) {
                String insertDetailSql = "INSERT INTO Evaluation_Detail (evaluation_id, criteria_id, score, item_comment) " +
                                         "VALUES (?, ?, ?, ?);";
                try (PreparedStatement ps = conn.prepareStatement(insertDetailSql)) {
                    for (Map.Entry<Integer, Double> entry : criteriaScores.entrySet()) {
                        int criteriaId = entry.getKey();
                        double score = entry.getValue();
                        String itemComment = criteriaComments != null ? criteriaComments.get(criteriaId) : null;

                        ps.setInt(1, evalId);
                        ps.setInt(2, criteriaId);
                        ps.setBigDecimal(3, BigDecimal.valueOf(score));
                        ps.setString(4, itemComment);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            // 3. Determine new Topic status and Topic_Code
            String newStatus;
            String topicCode = null;
            if ("APPROVED".equalsIgnoreCase(decision)) {
                // Defense-in-Depth Guard 1: Legal Risk must be strictly below 8.0
                if (legalRiskScore >= 8.0) {
                    conn.rollback();
                    throw new IllegalStateException("Không được phép phê duyệt trực tiếp đề tài có rủi ro pháp lý >= 8.0 (Yêu cầu chuyển TBT duyệt vượt cấp).");
                }

                // Defense-in-Depth Guard 2: Composite weighted score must be >= 5.0
                if (criteriaScores != null && !criteriaScores.isEmpty()) {
                    List<CriteriaConfig> configs = getCriteriaConfigs();
                    double totalWeighted = 0.0;
                    double totalWeight = 0.0;
                    for (CriteriaConfig cfg : configs) {
                        Double s = criteriaScores.get(cfg.getCriteriaId());
                        if (s != null) {
                            totalWeighted += (s * cfg.getWeightPercent());
                            totalWeight += cfg.getWeightPercent();
                        }
                    }
                    double composite = (totalWeight > 0) ? (totalWeighted / totalWeight) : 0.0;
                    if (composite < 5.0) {
                        conn.rollback();
                        throw new IllegalStateException("Không đủ điều kiện phê duyệt Cấp 1: Điểm tổng hợp (" +
                                String.format("%.2f", composite) + "/10) chưa đạt ngưỡng tối thiểu 5.0.");
                    }
                }

                newStatus = "APPROVED";
                topicCode = generateUniqueTopicCode(conn, topicId);
            } else if ("ESCALATED".equalsIgnoreCase(decision)) {
                newStatus = "PENDING_EXECUTIVE_APPROVAL";
            } else if ("REVISION_REQUESTED".equalsIgnoreCase(decision)) {
                newStatus = "REVISION_REQUESTED";
            } else if ("REJECTED".equalsIgnoreCase(decision)) {
                newStatus = "REJECTED";
            } else {
                newStatus = decision;
            }

            if ("APPROVED".equals(newStatus)) {
                String updateTopicSql = "UPDATE Topic SET status = ?, topic_code = COALESCE(topic_code, ?) WHERE topic_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(updateTopicSql)) {
                    ps.setString(1, newStatus);
                    ps.setString(2, topicCode);
                    ps.setInt(3, topicId);
                    ps.executeUpdate();
                }
            } else {
                String updateTopicSql = "UPDATE Topic SET status = ? WHERE topic_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(updateTopicSql)) {
                    ps.setString(1, newStatus);
                    ps.setInt(2, topicId);
                    ps.executeUpdate();
                }
            }

            // 4. Retrieve author_user_id to send Notification
            int authorId = 0;
            String findAuthorSql = "SELECT author_user_id FROM Topic WHERE topic_id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(findAuthorSql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        authorId = rs.getInt(1);
                    }
                }
            }

            if (authorId > 0) {
                String notifMsg;
                if ("APPROVED".equals(newStatus)) {
                    notifMsg = "Đề tài #" + topicId + " đã được BTV phê duyệt cấp 1 thành công (Mã: " + topicCode + ").";
                } else if ("PENDING_EXECUTIVE_APPROVAL".equals(newStatus)) {
                    notifMsg = "Đề tài #" + topicId + " đã được chuyển lên Tổng Biên Tập thẩm định vượt cấp (Rủi ro pháp lý: " + legalRiskScore + "/10).";
                } else if ("REVISION_REQUESTED".equals(newStatus)) {
                    notifMsg = "Đề tài #" + topicId + " được yêu cầu chỉnh sửa đề cương: " + (comments != null ? comments : "");
                } else {
                    notifMsg = "Đề tài #" + topicId + " đã bị từ chối thẩm định: " + (comments != null ? comments : "");
                }

                String insertNotifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0);";
                try (PreparedStatement ps = conn.prepareStatement(insertNotifSql)) {
                    ps.setInt(1, authorId);
                    ps.setInt(2, topicId);
                    ps.setString(3, notifMsg);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            LOGGER.log(Level.SEVERE, "Transaction rollback during evaluateTopic for topicId " + topicId, ex);
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    private String generateUniqueTopicCode(Connection conn, int topicId) throws SQLException {
        int year = Year.now().getValue();
        String prefix = "TOPIC-" + year + "-";
        int seq = topicId;
        String checkSql = "SELECT COUNT(*) FROM Topic WHERE topic_code = ?;";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            while (true) {
                String candidate = String.format("%s%03d", prefix, seq);
                ps.setString(1, candidate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        seq++;
                    } else {
                        return candidate;
                    }
                }
            }
        }
    }

    /**
     * Retrieve all active categories for a specific desk.
     */
    public List<Category> getCategoriesByDesk(int deskId) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, parent_category_id, desk_id FROM Category WHERE desk_id = ? ORDER BY category_name ASC;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer parentId = rs.getObject("parent_category_id") != null ? rs.getInt("parent_category_id") : null;
                        list.add(new Category(
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                parentId,
                                rs.getInt("desk_id")
                        ));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getCategoriesByDesk", ex);
        }
        return list;
    }

    /**
     * Retrieve paginated archived/suspended topics for SCR-2.8 Idea Archive catalog.
     * Extracts latest rejection comments and evaluator information via OUTER APPLY.
     */
    public List<TopicArchiveDTO> getArchivedTopics(int deskId, String statusFilter, int categoryId, String keyword, int page, int pageSize) {
        List<TopicArchiveDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.topic_id, t.topic_code, t.title, t.angle, t.status, t.deadline, ")
           .append("       c.category_id, c.category_name, d.desk_id, d.desk_name, ")
           .append("       u.user_id AS author_user_id, u.full_name AS author_name, u.email AS author_email, ")
           .append("       latest_eval.evaluation_id, latest_eval.comments AS rejection_reason, ")
           .append("       latest_eval.evaluator_user_id, latest_eval.evaluator_name ")
           .append("FROM Topic t ")
           .append("INNER JOIN Category c ON t.category_id = c.category_id ")
           .append("INNER JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("INNER JOIN [User] u ON t.author_user_id = u.user_id ")
           .append("OUTER APPLY ( ")
           .append("    SELECT TOP 1 te.evaluation_id, te.comments, te.evaluator_user_id, ue.full_name AS evaluator_name ")
           .append("    FROM Topic_Evaluation te ")
           .append("    INNER JOIN [User] ue ON te.evaluator_user_id = ue.user_id ")
           .append("    WHERE te.topic_id = t.topic_id ")
           .append("    ORDER BY te.evaluation_id DESC ")
           .append(") latest_eval ")
           .append("WHERE d.desk_id = ? ");

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
            sql.append("  AND t.status = ? ");
        } else {
            sql.append("  AND t.status IN ('REJECTED', 'ABORTED') ");
        }

        if (categoryId > 0) {
            sql.append("  AND c.category_id = ? ");
        }

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.angle LIKE ? OR u.full_name LIKE ?) ");
        }

        sql.append("ORDER BY t.topic_id DESC ")
           .append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;");

        int offset = (page - 1) * pageSize;

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, deskId);

                if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
                    ps.setString(paramIndex++, statusFilter.trim());
                }

                if (categoryId > 0) {
                    ps.setInt(paramIndex++, categoryId);
                }

                if (hasKeyword) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                }

                ps.setInt(paramIndex++, offset);
                ps.setInt(paramIndex, pageSize);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer evalId = rs.getObject("evaluation_id") != null ? rs.getInt("evaluation_id") : null;
                        Integer evalUserId = rs.getObject("evaluator_user_id") != null ? rs.getInt("evaluator_user_id") : null;

                        TopicArchiveDTO dto = new TopicArchiveDTO(
                                rs.getInt("topic_id"),
                                rs.getString("topic_code"),
                                rs.getString("title"),
                                rs.getString("angle"),
                                rs.getString("status"),
                                rs.getDate("deadline"),
                                rs.getInt("category_id"),
                                rs.getString("category_name"),
                                rs.getInt("desk_id"),
                                rs.getString("desk_name"),
                                rs.getInt("author_user_id"),
                                rs.getString("author_name"),
                                rs.getString("author_email"),
                                evalId,
                                rs.getString("rejection_reason"),
                                evalUserId,
                                rs.getString("evaluator_name")
                        );
                        list.add(dto);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getArchivedTopics", ex);
        }
        return list;
    }

    /**
     * Count total archived topics matching filter parameters.
     */
    public int countArchivedTopics(int deskId, String statusFilter, int categoryId, String keyword) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
           .append("FROM Topic t ")
           .append("INNER JOIN Category c ON t.category_id = c.category_id ")
           .append("INNER JOIN Desk d ON c.desk_id = d.desk_id ")
           .append("INNER JOIN [User] u ON t.author_user_id = u.user_id ")
           .append("WHERE d.desk_id = ? ");

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
            sql.append("  AND t.status = ? ");
        } else {
            sql.append("  AND t.status IN ('REJECTED', 'ABORTED') ");
        }

        if (categoryId > 0) {
            sql.append("  AND c.category_id = ? ");
        }

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.angle LIKE ? OR u.full_name LIKE ?) ");
        }

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                ps.setInt(paramIndex++, deskId);

                if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter.trim())) {
                    ps.setString(paramIndex++, statusFilter.trim());
                }

                if (categoryId > 0) {
                    ps.setInt(paramIndex++, categoryId);
                }

                if (hasKeyword) {
                    String pattern = "%" + keyword.trim() + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex, pattern);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in countArchivedTopics", ex);
        }
        return 0;
    }

    /**
     * Retrieve aggregated KPI metrics for Idea Archive catalog (TOTAL, REJECTED, ABORTED).
     */
    public Map<String, Integer> getArchiveStats(int deskId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("TOTAL", 0);
        stats.put("REJECTED", 0);
        stats.put("ABORTED", 0);

        String sql = "SELECT t.status, COUNT(*) AS cnt " +
                     "FROM Topic t " +
                     "INNER JOIN Category c ON t.category_id = c.category_id " +
                     "WHERE c.desk_id = ? AND t.status IN ('REJECTED', 'ABORTED') " +
                     "GROUP BY t.status;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    int total = 0;
                    while (rs.next()) {
                        String st = rs.getString("status");
                        int count = rs.getInt("cnt");
                        stats.put(st, count);
                        total += count;
                    }
                    stats.put("TOTAL", total);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getArchiveStats", ex);
        }
        return stats;
    }

    /**
     * Closed-loop Reactivation Lifecycle:
     * REJECTED / ABORTED -> Reactivate -> DRAFT -> Edit & Submit -> SUBMITTED.
     * Enforces domain guard clause: strictly prohibits direct mutation to APPROVED or IN_PROGRESS.
     */
    public boolean reactivateArchivedTopic(int topicId, int editorUserId, String reactivateNotes) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 1. Guard check current topic status
            String checkSql = "SELECT title, status, author_user_id FROM Topic WHERE topic_id = ?;";
            String currentStatus = null;
            String title = null;
            int authorId = 0;

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        title = rs.getString("title");
                        currentStatus = rs.getString("status");
                        authorId = rs.getInt("author_user_id");
                    }
                }
            }

            if (currentStatus == null) {
                conn.rollback();
                return false;
            }

            if (!"REJECTED".equalsIgnoreCase(currentStatus) && !"ABORTED".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                throw new IllegalStateException("Chỉ đề tài ở trạng thái REJECTED hoặc ABORTED mới được phép tái kích hoạt về DRAFT. Trạng thái hiện tại: " + currentStatus);
            }

            // 2. Mutate Topic status strictly to DRAFT
            String updateSql = "UPDATE Topic SET status = 'DRAFT' WHERE topic_id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }

            // 3. Dispatch Notification to topic author
            if (authorId > 0) {
                String noteText = (reactivateNotes != null && !reactivateNotes.trim().isEmpty())
                        ? ": " + reactivateNotes.trim()
                        : ". Vui lòng cập nhật đề cương và gửi thẩm định lại.";
                String message = "Đề tài #" + topicId + " (" + title + ") đã được BTV tái kích hoạt về bản nháp (DRAFT)" + noteText;

                String notifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0);";
                try (PreparedStatement ps = conn.prepareStatement(notifSql)) {
                    ps.setInt(1, authorId);
                    ps.setInt(2, topicId);
                    ps.setString(3, message);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            LOGGER.log(Level.SEVERE, "Transaction rollback in reactivateArchivedTopic for topicId " + topicId, ex);
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }
}
