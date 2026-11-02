package dal;

import dto.ReporterWorkloadDTO;
import dto.TopicArchiveDTO;
import dto.TopicEvaluationDetailDTO;
import dto.TopicPendingDTO;
import model.Category;
import model.CriteriaConfig;
import model.Desk;
import model.Topic;
import model.TopicEstimate;
import model.TopicMember;
import model.User;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private static final Map<Integer, List<Category>> CACHED_CATEGORIES = new java.util.concurrent.ConcurrentHashMap<>();

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

                // Fetch latest Evaluation Session if exists (SCR-2.3 Review & Read-only State)
                String latestEvalSql = "SELECT TOP 1 te.evaluation_id, te.evaluator_user_id, u.full_name AS evaluator_name, " +
                                       "             te.evaluation_stage, te.decision, te.comments " +
                                       "FROM Topic_Evaluation te " +
                                       "INNER JOIN [User] u ON te.evaluator_user_id = u.user_id " +
                                       "WHERE te.topic_id = ? " +
                                       "ORDER BY te.evaluation_id DESC;";

                try (PreparedStatement psEval = conn.prepareStatement(latestEvalSql)) {
                    psEval.setInt(1, topicId);
                    try (ResultSet rsEval = psEval.executeQuery()) {
                        if (rsEval.next()) {
                            dto.setEvaluationId(rsEval.getInt("evaluation_id"));
                            dto.setEvaluatorUserId(rsEval.getInt("evaluator_user_id"));
                            dto.setEvaluatorName(rsEval.getString("evaluator_name"));
                            dto.setEvaluationStage(rsEval.getString("evaluation_stage"));
                            dto.setEvaluationDecision(rsEval.getString("decision"));

                            String rawComments = rsEval.getString("comments");
                            if (rawComments != null) {
                                // Parse optional Legal Risk prefix if present: "[Rủi ro pháp lý: X.X/10] ..."
                                if (rawComments.startsWith("[Rủi ro pháp lý: ")) {
                                    int endIdx = rawComments.indexOf("/10]");
                                    if (endIdx != -1) {
                                        try {
                                            String scoreSub = rawComments.substring("[Rủi ro pháp lý: ".length(), endIdx).trim();
                                            dto.setLegalRiskScore(Double.parseDouble(scoreSub));
                                            rawComments = rawComments.substring(endIdx + 4).trim();
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                            dto.setEvaluationComments(rawComments != null ? rawComments : "");

                            // Fetch Evaluation_Detail rows
                            String detailSql = "SELECT criteria_id, score, item_comment " +
                                               "FROM Evaluation_Detail " +
                                               "WHERE evaluation_id = ?;";
                            Map<Integer, Double> scoreMap = new HashMap<>();
                            Map<Integer, String> commentMap = new HashMap<>();
                            try (PreparedStatement psDet = conn.prepareStatement(detailSql)) {
                                psDet.setInt(1, dto.getEvaluationId());
                                try (ResultSet rsDet = psDet.executeQuery()) {
                                    while (rsDet.next()) {
                                        scoreMap.put(rsDet.getInt("criteria_id"), rsDet.getBigDecimal("score").doubleValue());
                                        String cmt = rsDet.getString("item_comment");
                                        if (cmt != null) {
                                            commentMap.put(rsDet.getInt("criteria_id"), cmt);
                                        }
                                    }
                                }
                            }
                            dto.setEvaluatedScores(scoreMap);
                            dto.setEvaluatedComments(commentMap);

                            // Compute composite weighted score from evaluated scores
                            if (!scoreMap.isEmpty() && dto.getCriteriaList() != null) {
                                double totalWeighted = 0.0;
                                double totalWeight = 0.0;
                                for (CriteriaConfig cfg : dto.getCriteriaList()) {
                                    Double s = scoreMap.get(cfg.getCriteriaId());
                                    if (s != null) {
                                        totalWeighted += (s * cfg.getWeightPercentDouble());
                                        totalWeight += cfg.getWeightPercentDouble();
                                    }
                                }
                                dto.setCompositeScore(totalWeight > 0 ? (totalWeighted / totalWeight) : 0.0);
                            }
                        }
                    }
                }
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
        return evaluateTopic(topicId, evaluatorUserId, 2, decision, comments, criteriaScores, criteriaComments, legalRiskScore);
    }

    /**
     * Executes atomic ACID transaction for topic evaluation (supports Desk Editor Level 1 & TBT Executive Override):
     * 1. Inserts Topic_Evaluation record with appropriate evaluation_stage ('DESK_REVIEW' or 'EXECUTIVE_APPROVAL')
     * 2. Inserts individual Evaluation_Detail criterion scores
     * 3. Updates Topic.status (and mints topic_code if APPROVED)
     * 4. Inserts Notification for topic author reflecting whether Desk Editor or TBT approved
     */
    public boolean evaluateTopic(int topicId, int evaluatorUserId, int evaluatorRoleId, String decision, String comments,
                                 Map<Integer, Double> criteriaScores, Map<Integer, String> criteriaComments,
                                 double legalRiskScore) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 0. Defense-in-Depth Guard: Verify topic status before evaluating
            String checkTopicStatusSql = "SELECT status FROM Topic WHERE topic_id = ?;";
            try (PreparedStatement psCheck = conn.prepareStatement(checkTopicStatusSql)) {
                psCheck.setInt(1, topicId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (!rsCheck.next()) {
                        conn.rollback();
                        throw new IllegalArgumentException("Đề tài #" + topicId + " không tồn tại.");
                    }
                    String currentStatus = rsCheck.getString("status");
                    if (!"SUBMITTED".equalsIgnoreCase(currentStatus)) {
                        conn.rollback();
                        throw new IllegalStateException("Đề tài #" + topicId + " đang ở trạng thái '" + currentStatus
                                + "'. Chỉ đề tài ở trạng thái 'SUBMITTED' mới được phép thẩm định Cấp 1.");
                    }
                }
            }

            // Defense-in-Depth Guard: TBT cannot escalate to themselves
            if (evaluatorRoleId == 3 && "ESCALATED".equalsIgnoreCase(decision)) {
                conn.rollback();
                throw new IllegalStateException("Tổng Biên Tập là cấp thẩm quyền cao nhất, không thể chuyển đề tài lên chính mình.");
            }

            // Format full comments with Legal Risk Tag if legalRiskScore > 0
            String fullComments = comments != null ? comments.trim() : "";
            if (legalRiskScore > 0) {
                fullComments = String.format(java.util.Locale.US, "[Rủi ro pháp lý: %.1f/10] ", legalRiskScore) + fullComments;
            }

            // 1. Insert into Topic_Evaluation (stage is EXECUTIVE_APPROVAL if TBT overrides, otherwise DESK_REVIEW)
            String evaluationStage = (evaluatorRoleId == 3) ? "EXECUTIVE_APPROVAL" : "DESK_REVIEW";
            int evalId = 0;
            String insertEvalSql = "INSERT INTO Topic_Evaluation (topic_id, evaluator_user_id, evaluation_stage, decision, comments) " +
                                  "VALUES (?, ?, ?, ?, ?);";
            try (PreparedStatement ps = conn.prepareStatement(insertEvalSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, topicId);
                ps.setInt(2, evaluatorUserId);
                ps.setString(3, evaluationStage);
                ps.setString(4, decision);
                ps.setString(5, fullComments);
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
                // Defense-in-Depth Guard 1: For Desk Editors (role != 3), Legal Risk must be strictly below 8.0
                if (evaluatorRoleId != 3 && legalRiskScore >= 8.0) {
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
                            totalWeighted += (s * cfg.getWeightPercentDouble());
                            totalWeight += cfg.getWeightPercentDouble();
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
                    if (evaluatorRoleId == 3) {
                        notifMsg = "Đề tài #" + topicId + " đã được Tổng Biên Tập trực tiếp phê duyệt đặc cách (Mã: " + topicCode + ").";
                    } else {
                        notifMsg = "Đề tài #" + topicId + " đã được BTV phê duyệt cấp 1 thành công (Mã: " + topicCode + ").";
                    }
                } else if ("PENDING_EXECUTIVE_APPROVAL".equals(newStatus)) {
                    notifMsg = "Đề tài #" + topicId + " đã được chuyển lên Tổng Biên Tập thẩm định vượt cấp (Rủi ro pháp lý: " + legalRiskScore + "/10).";
                } else if ("REVISION_REQUESTED".equals(newStatus)) {
                    if (evaluatorRoleId == 3) {
                        notifMsg = "Tổng Biên Tập yêu cầu chỉnh sửa đề cương đề tài #" + topicId + ": " + (comments != null ? comments : "");
                    } else {
                        notifMsg = "Đề tài #" + topicId + " được yêu cầu chỉnh sửa đề cương: " + (comments != null ? comments : "");
                    }
                } else {
                    if (evaluatorRoleId == 3) {
                        notifMsg = "Tổng Biên Tập đã bác bỏ đề tài #" + topicId + ": " + (comments != null ? comments : "");
                    } else {
                        notifMsg = "Đề tài #" + topicId + " đã bị từ chối thẩm định: " + (comments != null ? comments : "");
                    }
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

    /**
     * Executes atomic transaction for Executive Editor (TBT) reviewing an escalated topic (SCR-2.3B / UC: Review Escalated Pitch).
     * Enforces guard clause: Topic must currently be in 'PENDING_EXECUTIVE_APPROVAL'.
     *
     * @param topicId         Topic identifier
     * @param evaluatorUserId Executive Editor user identifier (e.g. TBT)
     * @param decision        Binary review decision ('APPROVED', 'REVISION_REQUESTED', or 'REJECTED')
     * @param comments        Executive direction comments (mandatory for REVISION_REQUESTED and REJECTED)
     * @return true if evaluation transaction committed successfully
     * @throws SQLException on database transaction error
     */
    public boolean evaluateEscalatedTopic(int topicId, int evaluatorUserId, String decision, String comments) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 1. Guard check: Topic must exist and currently be in PENDING_EXECUTIVE_APPROVAL status
            String checkTopicSql = "SELECT status, author_user_id FROM Topic WHERE topic_id = ?;";
            int authorId = 0;
            try (PreparedStatement psCheck = conn.prepareStatement(checkTopicSql)) {
                psCheck.setInt(1, topicId);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (!rsCheck.next()) {
                        conn.rollback();
                        throw new IllegalArgumentException("Đề tài #" + topicId + " không tồn tại.");
                    }
                    String currentStatus = rsCheck.getString("status");
                    if (!"PENDING_EXECUTIVE_APPROVAL".equalsIgnoreCase(currentStatus)) {
                        conn.rollback();
                        throw new IllegalStateException("Đề tài #" + topicId + " đang ở trạng thái '" + currentStatus
                                + "', không thể thực hiện phê duyệt cấp cao của Tổng Biên Tập (chỉ áp dụng cho 'PENDING_EXECUTIVE_APPROVAL').");
                    }
                    authorId = rsCheck.getInt("author_user_id");
                }
            }

            // 2. Validate decision: must be APPROVED, REVISION_REQUESTED, or REJECTED
            String normDecision = decision != null ? decision.toUpperCase().trim() : "";
            if (!"APPROVED".equals(normDecision) && !"REVISION_REQUESTED".equals(normDecision) && !"REJECTED".equals(normDecision)) {
                conn.rollback();
                throw new IllegalArgumentException("Quyết định thẩm định của TBT không hợp lệ: " + decision);
            }

            if (("REVISION_REQUESTED".equals(normDecision) || "REJECTED".equals(normDecision))
                    && (comments == null || comments.trim().isEmpty())) {
                conn.rollback();
                throw new IllegalArgumentException("Tổng Biên Tập bắt buộc phải nhập ý kiến chỉ đạo khi yêu cầu sửa đổi hoặc bác bỏ đề tài.");
            }

            // 3. Insert into Topic_Evaluation with evaluation_stage = 'EXECUTIVE_APPROVAL'
            String insertEvalSql = "INSERT INTO Topic_Evaluation (topic_id, evaluator_user_id, evaluation_stage, decision, comments) "
                    + "VALUES (?, ?, 'EXECUTIVE_APPROVAL', ?, ?);";
            try (PreparedStatement psEval = conn.prepareStatement(insertEvalSql)) {
                psEval.setInt(1, topicId);
                psEval.setInt(2, evaluatorUserId);
                psEval.setString(3, normDecision);
                psEval.setString(4, comments != null ? comments.trim() : "");
                psEval.executeUpdate();
            }

            // 4. Update Topic status and topic_code (if APPROVED)
            String topicCode = null;
            if ("APPROVED".equals(normDecision)) {
                topicCode = generateUniqueTopicCode(conn, topicId);
                String updateApprovedSql = "UPDATE Topic SET status = 'APPROVED', topic_code = ? WHERE topic_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(updateApprovedSql)) {
                    ps.setString(1, topicCode);
                    ps.setInt(2, topicId);
                    ps.executeUpdate();
                }
            } else {
                String updateStatusSql = "UPDATE Topic SET status = ? WHERE topic_id = ?;";
                try (PreparedStatement ps = conn.prepareStatement(updateStatusSql)) {
                    ps.setString(1, normDecision);
                    ps.setInt(2, topicId);
                    ps.executeUpdate();
                }
            }

            // 5. Send Notification to Author
            if (authorId > 0) {
                String notifMsg;
                if ("APPROVED".equals(normDecision)) {
                    notifMsg = "Tổng Biên Tập đã phê duyệt cấp cao đề tài #" + topicId + " (Mã: " + topicCode + ").";
                } else if ("REVISION_REQUESTED".equals(normDecision)) {
                    notifMsg = "Tổng Biên Tập yêu cầu hoàn thiện đề tài #" + topicId + ": " + (comments != null ? comments : "");
                } else {
                    notifMsg = "Tổng Biên Tập đã bác bỏ đề tài #" + topicId + ": " + (comments != null ? comments : "");
                }

                String insertNotifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0);";
                try (PreparedStatement psNotif = conn.prepareStatement(insertNotifSql)) {
                    psNotif.setInt(1, authorId);
                    psNotif.setInt(2, topicId);
                    psNotif.setString(3, notifMsg);
                    psNotif.executeUpdate();
                }
            }

            // 6. Notify Desk Editor who escalated this topic
            String findEscalatorSql = "SELECT TOP 1 evaluator_user_id FROM Topic_Evaluation "
                    + "WHERE topic_id = ? AND evaluation_stage = 'DESK_REVIEW' AND decision = 'ESCALATED' "
                    + "ORDER BY evaluation_id DESC;";
            int deskEditorId = 0;
            try (PreparedStatement psEsc = conn.prepareStatement(findEscalatorSql)) {
                psEsc.setInt(1, topicId);
                try (ResultSet rsEsc = psEsc.executeQuery()) {
                    if (rsEsc.next()) {
                        deskEditorId = rsEsc.getInt(1);
                    }
                }
            }
            if (deskEditorId > 0 && deskEditorId != authorId) {
                String deskMsg = "Tổng Biên Tập đã ban hành kết luận '" + normDecision + "' cho đề tài #" + topicId + " do Ban chuyên môn chuyển lên.";
                String insertNotifDeskSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0);";
                try (PreparedStatement psNotifDesk = conn.prepareStatement(insertNotifDeskSql)) {
                    psNotifDesk.setInt(1, deskEditorId);
                    psNotifDesk.setInt(2, topicId);
                    psNotifDesk.setString(3, deskMsg);
                    psNotifDesk.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            conn.rollback();
            LOGGER.log(Level.SEVERE, "Transaction rollback during evaluateEscalatedTopic for topicId " + topicId, ex);
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
        List<Category> cached = CACHED_CATEGORIES.get(deskId);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
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
            if (!list.isEmpty()) {
                CACHED_CATEGORIES.put(deskId, list);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getCategoriesByDesk", ex);
        }
        return list;
    }

    /**
     * Retrieves Topic entity by primary key topic_id.
     */
    public Topic getTopicById(int topicId) {
        String sql = "SELECT topic_id, topic_code, title, angle, status, deadline, category_id, author_user_id "
                   + "FROM Topic WHERE topic_id = ?;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Topic(
                                rs.getInt("topic_id"),
                                rs.getString("topic_code"),
                                rs.getString("title"),
                                rs.getString("angle"),
                                rs.getString("status"),
                                rs.getDate("deadline"),
                                rs.getInt("category_id"),
                                rs.getInt("author_user_id")
                        );
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getTopicById for topicId " + topicId, ex);
        }
        return null;
    }

    /**
     * Creates a new Directive Topic in 'DRAFT' status (SCR-2.4: Directive Topic Creation Form).
     * Returns the newly generated topic_id.
     */
    public int createDirectiveTopicDraft(Topic topic) throws SQLException {
        String sql = "INSERT INTO Topic (title, angle, status, deadline, category_id, author_user_id) "
                   + "VALUES (?, ?, 'DRAFT', ?, ?, ?);";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, topic.getTitle().trim());
            ps.setString(2, topic.getAngle() != null ? topic.getAngle().trim() : "");
            if (topic.getDeadline() != null) {
                ps.setDate(3, topic.getDeadline());
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setInt(4, topic.getCategoryId());
            ps.setInt(5, topic.getAuthorUserId());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Lưu bản thảo đề tài chỉ đạo thất bại, không có bản ghi nào được ghi nhận.");
            }

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) {
                    int generatedId = gk.getInt(1);
                    topic.setTopicId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("Lưu bản thảo đề tài chỉ đạo thất bại, không lấy được mã định danh tự tăng.");
                }
            }
        }
    }

    /**
     * Updates an existing Directive Topic draft (SCR-2.4).
     */
    public boolean updateDirectiveTopicDraft(Topic topic) throws SQLException {
        String sql = "UPDATE Topic SET title = ?, angle = ?, deadline = ?, category_id = ? "
                   + "WHERE topic_id = ? AND status = 'DRAFT';";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, topic.getTitle().trim());
            ps.setString(2, topic.getAngle() != null ? topic.getAngle().trim() : "");
            if (topic.getDeadline() != null) {
                ps.setDate(3, topic.getDeadline());
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setInt(4, topic.getCategoryId());
            ps.setInt(5, topic.getTopicId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves the production quota count for a desk in a given period (e.g. '2026-10').
     */
    public int getDeskQuota(int deskId, String period) {
        String sql = "SELECT quota_count FROM Desk_Quota WHERE desk_id = ? AND period = ?;";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                ps.setString(2, period);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying Desk Quota for desk " + deskId, ex);
        }
        return 15; // default fallback
    }

    /**
     * Retrieves count of active/approved topics for a desk to compute Quota fulfillment progress.
     */
    public int getDeskProductionCount(int deskId) {
        String sql = "SELECT COUNT(*) FROM Topic t "
                   + "INNER JOIN Category c ON t.category_id = c.category_id "
                   + "WHERE c.desk_id = ? AND t.status IN ('APPROVED', 'IN_PROGRESS', 'DELIVERED', 'ACCEPTED_READY_FOR_CMS');";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying Desk Production Count for desk " + deskId, ex);
        }
        return 0;
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

    /**
     * Retrieves reporter workload matrix for a specific desk (SCR-2.5: Reporter Dispatch & Workload Desk).
     * Calculates active topics, overdue topics, and completed topics for each reporter.
     * Orders reporters prioritizing current desk members, then lowest active workload first.
     */
    public List<ReporterWorkloadDTO> getReportersWorkloadByDesk(int deskId) {
        List<ReporterWorkloadDTO> list = new ArrayList<>();
        String sql = "SELECT "
                   + "    u.user_id, "
                   + "    u.full_name, "
                   + "    u.email, "
                   + "    u.desk_id, "
                   + "    COALESCE(d.desk_name, N'Ban Chuyên đề') AS desk_name, "
                   + "    ( "
                   + "        SELECT COUNT(DISTINCT t.topic_id) "
                   + "        FROM Topic t "
                   + "        LEFT JOIN Topic_Member tm ON t.topic_id = tm.topic_id "
                   + "        WHERE (tm.user_id = u.user_id OR (tm.user_id IS NULL AND t.author_user_id = u.user_id)) "
                   + "          AND t.status IN ('APPROVED', 'IN_PROGRESS') "
                   + "    ) AS active_count, "
                   + "    ( "
                   + "        SELECT COUNT(DISTINCT t.topic_id) "
                   + "        FROM Topic t "
                   + "        LEFT JOIN Topic_Member tm ON t.topic_id = tm.topic_id "
                   + "        WHERE (tm.user_id = u.user_id OR (tm.user_id IS NULL AND t.author_user_id = u.user_id)) "
                   + "          AND t.status IN ('APPROVED', 'IN_PROGRESS') "
                   + "          AND t.deadline < CAST(GETDATE() AS DATE) "
                   + "    ) AS overdue_count, "
                   + "    ( "
                   + "        SELECT COUNT(DISTINCT t.topic_id) "
                   + "        FROM Topic t "
                   + "        LEFT JOIN Topic_Member tm ON t.topic_id = tm.topic_id "
                   + "        WHERE (tm.user_id = u.user_id OR (tm.user_id IS NULL AND t.author_user_id = u.user_id)) "
                   + "          AND t.status IN ('ACCEPTED_READY_FOR_CMS', 'PUBLISHED') "
                   + "    ) AS completed_count "
                   + "FROM [User] u "
                   + "JOIN [Role] r ON u.role_id = r.role_id "
                   + "LEFT JOIN Desk d ON u.desk_id = d.desk_id "
                   + "WHERE r.role_name = 'REPORTER' AND u.active_status = 1 "
                   + "ORDER BY "
                   + "    CASE WHEN u.desk_id = ? THEN 0 ELSE 1 END, "
                   + "    active_count ASC, "
                   + "    u.full_name ASC;";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, deskId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int userId = rs.getInt("user_id");
                        int activeCount = rs.getInt("active_count");
                        ReporterWorkloadDTO dto = new ReporterWorkloadDTO(
                                userId,
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getInt("desk_id"),
                                rs.getString("desk_name"),
                                activeCount,
                                rs.getInt("overdue_count"),
                                rs.getInt("completed_count")
                        );

                        // If reporter has active topics, fetch up to 3 topic titles for quick context tooltip
                        if (activeCount > 0) {
                            String activeTitlesSql = "SELECT DISTINCT TOP 3 t.title "
                                                   + "FROM Topic t "
                                                   + "LEFT JOIN Topic_Member tm ON t.topic_id = tm.topic_id "
                                                   + "WHERE (tm.user_id = ? OR (tm.user_id IS NULL AND t.author_user_id = ?)) "
                                                   + "  AND t.status IN ('APPROVED', 'IN_PROGRESS') "
                                                   + "ORDER BY t.title ASC;";
                            try (PreparedStatement psTitles = conn.prepareStatement(activeTitlesSql)) {
                                psTitles.setInt(1, userId);
                                psTitles.setInt(2, userId);
                                try (ResultSet rsTitles = psTitles.executeQuery()) {
                                    while (rsTitles.next()) {
                                        dto.addActiveTopicTitle(rsTitles.getString("title"));
                                    }
                                }
                            }
                        }

                        list.add(dto);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error in getReportersWorkloadByDesk for desk " + deskId, ex);
        }
        return list;
    }

    /**
     * Executes atomic ACID transaction for commissioning a directive topic (SCR-2.5: Lead Reporter Assignment Desk):
     * 1. Status Guard: Verifies topic is strictly in 'DRAFT' status.
     * 2. Lead Reporter Guard: Verifies reporter exists, has role REPORTER (1), and is active.
     * 3. Mints unique topic_code via generateUniqueTopicCode.
     * 4. Updates Topic status to 'APPROVED' and assigns minted topic_code.
     * 5. Inserts lead reporter into Topic_Member with role_in_team = 'LEAD_REPORTER'.
     * 6. Dispatches in-app notifications to assigned Lead Reporter and Commissioning Editor.
     * 
     * Returns the minted topic_code upon success.
     */
    public String commissionDirectiveTopic(int topicId, int leadReporterId, int commissioningEditorId, String commissionNotes) throws SQLException {
        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            // 1. Guard check: Topic must exist and be in DRAFT status
            String topicCheckSql = "SELECT title, status, deadline FROM Topic WHERE topic_id = ?;";
            String title = null;
            String currentStatus = null;
            java.sql.Date deadline = null;

            try (PreparedStatement ps = conn.prepareStatement(topicCheckSql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        title = rs.getString("title");
                        currentStatus = rs.getString("status");
                        deadline = rs.getDate("deadline");
                    }
                }
            }

            if (currentStatus == null) {
                conn.rollback();
                throw new IllegalArgumentException("Không tìm thấy đề tài có mã #" + topicId);
            }

            if (!"DRAFT".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                throw new IllegalStateException("Chỉ đề tài ở trạng thái DRAFT mới có thể phát lệnh đặt hàng. Trạng thái hiện tại: " + currentStatus);
            }

            // 2. Guard check: Reporter must exist, be active and have role REPORTER
            String reporterCheckSql = "SELECT u.full_name FROM [User] u JOIN [Role] r ON u.role_id = r.role_id WHERE u.user_id = ? AND r.role_name = 'REPORTER' AND u.active_status = 1;";
            String reporterName = null;

            try (PreparedStatement ps = conn.prepareStatement(reporterCheckSql)) {
                ps.setInt(1, leadReporterId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        reporterName = rs.getString("full_name");
                    }
                }
            }

            if (reporterName == null) {
                conn.rollback();
                throw new IllegalArgumentException("Phóng viên chỉ định không hợp lệ hoặc đang ở trạng thái không hoạt động.");
            }

            // 3. Mint unique topic_code
            String topicCode = generateUniqueTopicCode(conn, topicId);

            // 4. Mutate Topic status to APPROVED and set topic_code
            String updateTopicSql = "UPDATE Topic SET status = 'APPROVED', topic_code = ? WHERE topic_id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(updateTopicSql)) {
                ps.setString(1, topicCode);
                ps.setInt(2, topicId);
                ps.executeUpdate();
            }

            // 5. Clean prior members for this draft topic and assign Lead Reporter
            String deleteMembersSql = "DELETE FROM Topic_Member WHERE topic_id = ?;";
            try (PreparedStatement ps = conn.prepareStatement(deleteMembersSql)) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }

            String insertMemberSql = "INSERT INTO Topic_Member (topic_id, user_id, role_in_team) VALUES (?, ?, 'LEAD_REPORTER');";
            try (PreparedStatement ps = conn.prepareStatement(insertMemberSql)) {
                ps.setInt(1, topicId);
                ps.setInt(2, leadReporterId);
                ps.executeUpdate();
            }

            // 6. Dispatch Notification to Lead Reporter
            String deadlineNote = deadline != null ? ". Hạn hoàn thành: " + deadline : "";
            String extraNote = (commissionNotes != null && !commissionNotes.trim().isEmpty())
                    ? ". Chỉ đạo nghiệp vụ: " + commissionNotes.trim()
                    : "";
            String reporterMessage = "Bạn được Trưởng ban giao quyền Trưởng nhóm tác nghiệp đề tài chỉ đạo: [" + topicCode + "] "
                    + title + deadlineNote + extraNote;

            String notifSql = "INSERT INTO Notification (user_id, topic_id, message, is_read) VALUES (?, ?, ?, 0);";
            try (PreparedStatement ps = conn.prepareStatement(notifSql)) {
                ps.setInt(1, leadReporterId);
                ps.setInt(2, topicId);
                ps.setString(3, reporterMessage);
                ps.executeUpdate();
            }

            // 7. Dispatch Notification to Commissioning Editor (audit confirmation)
            if (commissioningEditorId > 0) {
                String editorMessage = "Đã phát lệnh đặt hàng thành công đề tài chỉ đạo [" + topicCode + "] - " + title
                        + ". Trưởng nhóm tác nghiệp: " + reporterName;
                try (PreparedStatement ps = conn.prepareStatement(notifSql)) {
                    ps.setInt(1, commissioningEditorId);
                    ps.setInt(2, topicId);
                    ps.setString(3, editorMessage);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return topicCode;
        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            LOGGER.log(Level.SEVERE, "Transaction rollback in commissionDirectiveTopic for topicId " + topicId, ex);
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
        }
    }

    // =========================================================================
    // WF-01: REPORTER WORKFLOW METHODS (Integrated from TopicRepository)
    // =========================================================================

    private String baseReporterSelect() {
        return "SELECT t.topic_id, t.topic_code, t.title, t.summary, t.angle, t.genre, t.priority, "
             + "       t.status, t.deadline, t.created_date, t.category_id, t.author_user_id, "
             + "       c.category_name, d.desk_id, d.desk_name, u.full_name AS reporter_name, u.email AS reporter_email, "
             + "       te.comments AS rejection_reason "
             + "FROM Topic t "
             + "JOIN Category c ON t.category_id = c.category_id "
             + "JOIN Desk d ON c.desk_id = d.desk_id "
             + "JOIN [User] u ON t.author_user_id = u.user_id "
             + "LEFT JOIN Topic_Evaluation te ON t.topic_id = te.topic_id "
             + "     AND te.evaluation_id = (SELECT MAX(te2.evaluation_id) FROM Topic_Evaluation te2 WHERE te2.topic_id = t.topic_id) ";
    }

    private Topic mapReporterTopicRow(ResultSet rs) throws SQLException {
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
        t.setReporterEmail(rs.getString("reporter_email"));
        t.setRejectionReason(rs.getString("rejection_reason"));
        return t;
    }

    public List<Topic> getTopicsByReporter(int userId, String statusFilter, String searchKeyword) {
        List<Topic> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(baseReporterSelect());
        sql.append("WHERE t.author_user_id = ? ");

        boolean hasStatus = (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter));
        if (hasStatus) {
            sql.append("  AND t.status = ? ");
        }

        boolean hasKeyword = (searchKeyword != null && !searchKeyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append("  AND (t.title LIKE ? OR t.summary LIKE ? OR t.angle LIKE ? OR c.category_name LIKE ?) ");
        }

        sql.append("ORDER BY t.topic_id DESC");

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                ps.setInt(idx++, userId);

                if (hasStatus) {
                    ps.setString(idx++, statusFilter.trim());
                }

                if (hasKeyword) {
                    String p = "%" + searchKeyword.trim() + "%";
                    ps.setString(idx++, p);
                    ps.setString(idx++, p);
                    ps.setString(idx++, p);
                    ps.setString(idx++, p);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapReporterTopicRow(rs));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y danh sﾃ｡ch ﾄ黛ｻ・tﾃi c盻ｧa phﾃｳng viﾃｪn #" + userId, ex);
        } finally {
            close();
        }
        return list;
    }

    public Topic getTopicDetailForReporter(int topicId, int userId) {
        String sql = baseReporterSelect() + "WHERE t.topic_id = ? AND (t.author_user_id = ? OR ? = 0)";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, topicId);
                ps.setInt(2, userId);
                ps.setInt(3, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapReporterTopicRow(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y chi ti蘯ｿt ﾄ黛ｻ・tﾃi #" + topicId + " cho phﾃｳng viﾃｪn #" + userId, ex);
        } finally {
            close();
        }
        return null;
    }

    public Topic saveTopicPitch(Topic topic, List<TopicEstimate> estimates, List<TopicMember> members) throws SQLException {
        if (topic == null) {
            throw new IllegalArgumentException("Topic khﾃｴng ﾄ柁ｰ盻｣c null.");
        }
        if (topic.getTitle() == null || topic.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiﾃｪu ﾄ黛ｻ・ﾄ黛ｻ・tﾃi khﾃｴng ﾄ柁ｰ盻｣c ﾄ黛ｻ・tr盻創g.");
        }
        if (topic.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Vui lﾃｲng ch盻肱 chuyﾃｪn m盻･c h盻｣p l盻・");
        }

        Connection conn = getConnection();
        boolean oldAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);

        try {
            if (topic.getTopicId() == 0) {
                // INSERT NEW TOPIC
                String insertSql = "INSERT INTO Topic (title, summary, angle, genre, priority, status, deadline, category_id, author_user_id, created_date) "
                                 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME());";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, topic.getTitle().trim());
                    if (topic.getSummary() != null && !topic.getSummary().trim().isEmpty()) {
                        ps.setString(2, topic.getSummary().trim());
                    } else {
                        ps.setNull(2, java.sql.Types.NVARCHAR);
                    }
                    if (topic.getAngle() != null && !topic.getAngle().trim().isEmpty()) {
                        ps.setString(3, topic.getAngle().trim());
                    } else {
                        ps.setNull(3, java.sql.Types.NVARCHAR);
                    }
                    if (topic.getGenre() != null && !topic.getGenre().trim().isEmpty()) {
                        ps.setString(4, topic.getGenre().trim());
                    } else {
                        ps.setNull(4, java.sql.Types.NVARCHAR);
                    }
                    ps.setString(5, (topic.getPriority() != null && !topic.getPriority().trim().isEmpty()) ? topic.getPriority().trim() : "NORMAL");
                    ps.setString(6, (topic.getStatus() != null && !topic.getStatus().trim().isEmpty()) ? topic.getStatus().trim() : "DRAFT");
                    if (topic.getDeadline() != null) {
                        ps.setDate(7, topic.getDeadline());
                    } else {
                        ps.setNull(7, java.sql.Types.DATE);
                    }
                    ps.setInt(8, topic.getCategoryId());
                    ps.setInt(9, topic.getAuthorUserId());

                    ps.executeUpdate();
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            topic.setTopicId(gk.getInt(1));
                        }
                    }
                }
            } else {
                // UPDATE EXISTING TOPIC (ch盻・cho phﾃｩp khi 盻・tr蘯｡ng thﾃ｡i DRAFT ho蘯ｷc REVISE)
                String updateSql = "UPDATE Topic SET title = ?, summary = ?, angle = ?, genre = ?, priority = ?, status = ?, deadline = ?, category_id = ? "
                                 + "WHERE topic_id = ? AND author_user_id = ? AND status IN ('DRAFT', 'REVISION_REQUESTED', 'REVISION_REQUIRED', 'REVISE', 'REJECTED');";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, topic.getTitle().trim());
                    if (topic.getSummary() != null && !topic.getSummary().trim().isEmpty()) {
                        ps.setString(2, topic.getSummary().trim());
                    } else {
                        ps.setNull(2, java.sql.Types.NVARCHAR);
                    }
                    if (topic.getAngle() != null && !topic.getAngle().trim().isEmpty()) {
                        ps.setString(3, topic.getAngle().trim());
                    } else {
                        ps.setNull(3, java.sql.Types.NVARCHAR);
                    }
                    if (topic.getGenre() != null && !topic.getGenre().trim().isEmpty()) {
                        ps.setString(4, topic.getGenre().trim());
                    } else {
                        ps.setNull(4, java.sql.Types.NVARCHAR);
                    }
                    ps.setString(5, (topic.getPriority() != null && !topic.getPriority().trim().isEmpty()) ? topic.getPriority().trim() : "NORMAL");
                    ps.setString(6, (topic.getStatus() != null && !topic.getStatus().trim().isEmpty()) ? topic.getStatus().trim() : "DRAFT");
                    if (topic.getDeadline() != null) {
                        ps.setDate(7, topic.getDeadline());
                    } else {
                        ps.setNull(7, java.sql.Types.DATE);
                    }
                    ps.setInt(8, topic.getCategoryId());
                    ps.setInt(9, topic.getTopicId());
                    ps.setInt(10, topic.getAuthorUserId());

                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        throw new SQLException("Không tìm thấy đề tài hoặc đề tài không ở trạng thái được phép chỉnh sửa.");
                    }
                }
            }

            // Sync Topic_Estimate
            if (estimates != null) {
                String delEst = "DELETE FROM Topic_Estimate WHERE topic_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(delEst)) {
                    psDel.setInt(1, topic.getTopicId());
                    psDel.executeUpdate();
                }

                String insEst = "INSERT INTO Topic_Estimate (topic_id, cost_item, amount) VALUES (?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insEst)) {
                    for (TopicEstimate est : estimates) {
                        if (est == null || est.getCostItem() == null || est.getCostItem().trim().isEmpty()) {
                            continue;
                        }
                        psIns.setInt(1, topic.getTopicId());
                        psIns.setString(2, est.getCostItem().trim());
                        psIns.setBigDecimal(3, est.getAmount() != null ? est.getAmount() : BigDecimal.ZERO);
                        psIns.executeUpdate();
                    }
                }
            }

            // Sync Topic_Member
            if (members != null) {
                String delMem = "DELETE FROM Topic_Member WHERE topic_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(delMem)) {
                    psDel.setInt(1, topic.getTopicId());
                    psDel.executeUpdate();
                }

                String insMem = "INSERT INTO Topic_Member (topic_id, user_id, role_in_team) VALUES (?, ?, ?)";
                try (PreparedStatement psIns = conn.prepareStatement(insMem)) {
                    Set<Integer> addedUsers = new HashSet<>();
                    for (TopicMember mem : members) {
                        if (mem == null || mem.getUserId() <= 0 || addedUsers.contains(mem.getUserId())) {
                            continue;
                        }
                        String role = mem.getRoleInTeam();
                        if (role == null || role.trim().isEmpty()) {
                            role = "ASSISTANT";
                        } else {
                            role = role.trim();
                        }
                        psIns.setInt(1, topic.getTopicId());
                        psIns.setInt(2, mem.getUserId());
                        psIns.setString(3, role);
                        psIns.executeUpdate();
                        addedUsers.add(mem.getUserId());
                    }
                }
            }

            conn.commit();
            return topic;
        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            LOGGER.log(Level.SEVERE, "Transaction rollback in saveTopicPitch", ex);
            throw ex;
        } finally {
            conn.setAutoCommit(oldAutoCommit);
            close();
        }
    }

    public boolean deleteReporterDraft(int topicId, int userId) {
        String sql = "DELETE FROM Topic WHERE topic_id = ? AND author_user_id = ? AND status = 'DRAFT'";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, topicId);
                ps.setInt(2, userId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 xﾃｳa b蘯｣n nhﾃ｡p #" + topicId + " c盻ｧa user #" + userId, ex);
            return false;
        } finally {
            close();
        }
    }

    public List<Topic> searchSimilarTopics(String query) {
        List<Topic> list = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return list;
        }

        String sql = baseReporterSelect()
                   + "WHERE (t.title LIKE ? OR t.summary LIKE ? OR t.angle LIKE ? OR c.category_name LIKE ?) "
                   + "ORDER BY t.topic_id DESC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String pattern = "%" + query.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
                ps.setString(3, pattern);
                ps.setString(4, pattern);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapReporterTopicRow(rs));
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 tra c盻ｩu ﾄ黛ｻ・tﾃi tﾆｰﾆ｡ng t盻ｱ", ex);
        } finally {
            close();
        }
        return list;
    }

    public List<TopicEstimate> findEstimatesByTopicId(int topicId) {
        List<TopicEstimate> list = new ArrayList<>();
        if (topicId <= 0) return list;

        String sql = "SELECT estimate_id, topic_id, cost_item, amount FROM Topic_Estimate WHERE topic_id = ? ORDER BY estimate_id ASC";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TopicEstimate est = new TopicEstimate();
                        est.setEstimateId(rs.getInt("estimate_id"));
                        est.setTopicId(rs.getInt("topic_id"));
                        est.setCostItem(rs.getString("cost_item"));
                        est.setAmount(rs.getBigDecimal("amount"));
                        list.add(est);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y chi phﾃｭ cho topic " + topicId, ex);
        } finally {
            close();
        }
        return list;
    }

    public List<TopicMember> findMembersByTopicId(int topicId) {
        List<TopicMember> list = new ArrayList<>();
        if (topicId <= 0) return list;

        String sql = "SELECT tm.member_id, tm.topic_id, tm.user_id, tm.role_in_team, "
                   + "       u.full_name, u.email, r.role_name "
                   + "FROM Topic_Member tm "
                   + "JOIN [User] u ON tm.user_id = u.user_id "
                   + "JOIN Role r ON u.role_id = r.role_id "
                   + "WHERE tm.topic_id = ? "
                   + "ORDER BY tm.member_id ASC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        TopicMember tm = new TopicMember();
                        tm.setMemberId(rs.getInt("member_id"));
                        tm.setTopicId(rs.getInt("topic_id"));
                        tm.setUserId(rs.getInt("user_id"));
                        tm.setRoleInTeam(rs.getString("role_in_team"));
                        tm.setUserName(rs.getString("full_name"));
                        tm.setUserEmail(rs.getString("email"));
                        tm.setUserRole(rs.getString("role_name"));
                        list.add(tm);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y thﾃnh viﾃｪn cho topic " + topicId, ex);
        } finally {
            close();
        }
        return list;
    }

    public List<User> findAllActiveUsersForTeam() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, u.email, u.role_id, r.role_name, u.active_status, u.desk_id, d.desk_name "
                   + "FROM [User] u "
                   + "JOIN Role r ON u.role_id = r.role_id "
                   + "LEFT JOIN Desk d ON u.desk_id = d.desk_id "
                   + "WHERE u.active_status = 1 "
                   + "ORDER BY u.user_id ASC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setRoleId(rs.getInt("role_id"));
                    u.setRoleName(rs.getString("role_name"));
                    u.setActiveStatus(rs.getBoolean("active_status"));
                    int dId = rs.getInt("desk_id");
                    if (!rs.wasNull()) {
                        u.setDeskId(dId);
                    }
                    u.setDeskName(rs.getString("desk_name"));
                    list.add(u);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y danh sﾃ｡ch nhﾃ｢n s盻ｱ tﾃ｡c nghi盻㎝", ex);
        } finally {
            close();
        }
        return list;
    }

    public Map<Integer, List<String>> getUsersBusySchedule(int excludeTopicId) {
        Map<Integer, List<String>> map = new HashMap<>();
        String sql = "SELECT DISTINCT tm_all.user_id, t.topic_id, t.topic_code, t.title, t.status "
                   + "FROM ( "
                   + "    SELECT author_user_id AS user_id, topic_id FROM Topic "
                   + "    UNION "
                   + "    SELECT user_id, topic_id FROM Topic_Member "
                   + "    UNION "
                   + "    SELECT assignee_user_id AS user_id, topic_id FROM Fieldwork_Task WHERE task_status IN ('TODO', 'IN_PROGRESS') "
                   + ") tm_all "
                   + "JOIN Topic t ON tm_all.topic_id = t.topic_id "
                   + "WHERE t.status NOT IN ('DRAFT', 'REJECTED', 'ABORTED', 'ACCEPTED_READY_FOR_CMS') "
                   + "  AND (? <= 0 OR t.topic_id <> ?) "
                   + "ORDER BY tm_all.user_id, t.topic_id ASC";

        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, excludeTopicId);
                ps.setInt(2, excludeTopicId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int uid = rs.getInt("user_id");
                        int tid = rs.getInt("topic_id");
                        String code = rs.getString("topic_code");
                        String title = rs.getString("title");
                        String label = (code != null && !code.trim().isEmpty())
                                ? (code.trim() + " (" + title + ")")
                                : ("#" + tid + " (" + title + ")");
                        map.computeIfAbsent(uid, k -> new ArrayList<>()).add(label);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y l盻議h b蘯ｭn nhﾃ｢n s盻ｱ", ex);
        } finally {
            close();
        }
        return map;
    }

    public List<String> getUserBusyTopics(int userId, int excludeTopicId) {
        if (userId <= 0) return Collections.emptyList();
        Map<Integer, List<String>> map = getUsersBusySchedule(excludeTopicId);
        return map.getOrDefault(userId, Collections.emptyList());
    }

    public List<String> findExistingGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT DISTINCT genre FROM Topic WHERE genre IS NOT NULL AND LTRIM(RTRIM(genre)) <> '' ORDER BY genre ASC";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String g = rs.getString("genre");
                    if (g != null && !g.trim().isEmpty()) {
                        genres.add(g.trim());
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y danh sﾃ｡ch th盻・lo蘯｡i t盻ｫ DB", ex);
        } finally {
            close();
        }

        String[] defaults = {"Tin nhanh", "Ph盻熟g v蘯･n", "Phﾃｳng s盻ｱ / Feature", "ﾄ進盻「 tra", "Infographic / 蘯｢nh"};
        for (String def : defaults) {
            if (!genres.contains(def)) {
                genres.add(def);
            }
        }
        return genres;
    }

    public List<String> findExistingPriorities() {
        List<String> priorities = new ArrayList<>();
        String sql = "SELECT DISTINCT priority FROM Topic WHERE priority IS NOT NULL AND LTRIM(RTRIM(priority)) <> '' ORDER BY priority ASC";
        try {
            Connection conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString("priority");
                    if (p != null && !p.trim().isEmpty()) {
                        priorities.add(p.trim());
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "L盻擁 l蘯･y danh sﾃ｡ch m盻ｩc ﾄ黛ｻ・ﾆｰu tiﾃｪn t盻ｫ DB", ex);
        } finally {
            close();
        }

        if (!priorities.contains("NORMAL")) priorities.add(0, "NORMAL");
        if (!priorities.contains("HOT")) priorities.add("HOT");
        if (!priorities.contains("URGENT")) priorities.add("URGENT");
        return priorities;
    }
}