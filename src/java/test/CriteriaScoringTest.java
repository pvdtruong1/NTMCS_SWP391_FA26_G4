package test;

import dal.TopicDAO;
import dto.TopicEvaluationDetailDTO;
import model.CriteriaConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automated test suite covering all operational decisions & edge cases for SCR-2.3 (Criteria Scoring Desk).
 */
public class CriteriaScoringTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: UC SCR-2.3 EVALUATE TOPIC PITCH");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        TopicDAO dao = new TopicDAO();

        // Test 1: Query Rubric Criteria Configs
        try {
            List<CriteriaConfig> criteria = dao.getCriteriaConfigs();
            double totalWeight = 0.0;
            for (CriteriaConfig c : criteria) {
                totalWeight += c.getWeightPercent();
            }

            if (criteria.size() == 4 && Math.abs(totalWeight - 100.0) < 0.01) {
                System.out.println("[PASS] Test 1: Retrieved " + criteria.size() + " criteria totaling " + totalWeight + "% weight.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: Criteria count=" + criteria.size() + ", totalWeight=" + totalWeight);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Retrieve Topic Dossier for Evaluation
        try {
            TopicEvaluationDetailDTO dossier = dao.getTopicDetailForEvaluation(1);
            if (dossier != null && dossier.getTopicId() == 1 &&
                dossier.getDeskId() == 1 &&
                dossier.getTitle() != null && !dossier.getTitle().isEmpty() &&
                dossier.getCriteriaList().size() == 4) {
                System.out.println("[PASS] Test 2: Topic dossier #" + dossier.getTopicId() + " retrieved successfully (" + dossier.getTitle().substring(0, 30) + "...).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Dossier invalid or null.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Direct Approval Flow (APPROVED -> Topic_Code minted + Notification created)
        try {
            Map<Integer, Double> scores = new HashMap<>();
            scores.put(1, 9.0);
            scores.put(2, 8.5);
            scores.put(3, 9.0);
            scores.put(4, 8.5);

            Map<Integer, String> comments = new HashMap<>();
            comments.put(1, "Thời sự cao");
            comments.put(2, "Tác động lớn");
            comments.put(3, "Hậu cần rõ ràng");
            comments.put(4, "Nguồn tin chuẩn");

            boolean ok = dao.evaluateTopic(1, 1, "APPROVED", "Duyệt trực tiếp đề tài xuất sắc.", scores, comments, 3.0);

            // Verify in DB
            TopicEvaluationDetailDTO updated = dao.getTopicDetailForEvaluation(1);
            if (ok && updated != null && "APPROVED".equals(updated.getStatus()) &&
                updated.getTopicCode() != null && updated.getTopicCode().startsWith("TOPIC-")) {
                System.out.println("[PASS] Test 3: Direct approval successful. Topic status = APPROVED, Code = " + updated.getTopicCode());
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Direct approval failed. Status=" + (updated != null ? updated.getStatus() : "null"));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Executive Escalation Flow (ESCALATED -> PENDING_EXECUTIVE_APPROVAL, topic_code = NULL)
        try {
            Map<Integer, Double> scores = new HashMap<>();
            scores.put(1, 8.0);
            scores.put(2, 8.0);
            scores.put(3, 7.5);
            scores.put(4, 7.0);

            Map<Integer, String> comments = new HashMap<>();
            comments.put(1, "Tin độc quyền chất lượng cao.");

            boolean ok = dao.evaluateTopic(2, 1, "ESCALATED", "Chuyển TBT do rủi ro pháp lý cao.", scores, comments, 8.5);

            TopicEvaluationDetailDTO updated = dao.getTopicDetailForEvaluation(2);
            boolean scoresRetained = updated != null && updated.getEvaluatedScores() != null &&
                                     updated.getEvaluatedScores().get(1) != null &&
                                     updated.getEvaluatedScores().get(1) == 8.0 &&
                                     updated.getEvaluatorName() != null &&
                                     updated.getLegalRiskScore() != null &&
                                     updated.getLegalRiskScore() == 8.5 &&
                                     updated.getCompositeScore() != null &&
                                     updated.getCompositeScore() > 0.0;

            if (ok && updated != null && "PENDING_EXECUTIVE_APPROVAL".equals(updated.getStatus()) &&
                updated.getTopicCode() == null && scoresRetained) {
                System.out.println("[PASS] Test 4: Escalation successful. Status = PENDING_EXECUTIVE_APPROVAL, topic_code = null, Desk Editor scores & legal risk verified.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Escalation failed. Status=" + (updated != null ? updated.getStatus() : "null") + ", scoresRetained=" + scoresRetained);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Revision Request Flow (REVISION_REQUESTED -> No-Bypass loopback)
        try {
            try (PreparedStatement ps = dao.getConnection().prepareStatement("UPDATE Topic SET status = 'SUBMITTED', topic_code = NULL WHERE topic_id = 1;")) {
                ps.executeUpdate();
            }

            Map<Integer, Double> scores = new HashMap<>();
            scores.put(1, 6.0);
            scores.put(2, 5.5);
            scores.put(3, 5.0);
            scores.put(4, 6.0);

            boolean ok = dao.evaluateTopic(1, 1, "REVISION_REQUESTED", "Cần bổ sung ý kiến Sở Xây dựng.", scores, null, 2.0);

            TopicEvaluationDetailDTO updated = dao.getTopicDetailForEvaluation(1);
            if (ok && updated != null && "REVISION_REQUESTED".equals(updated.getStatus())) {
                System.out.println("[PASS] Test 5: Revision request successful. Status = REVISION_REQUESTED.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Revision request failed.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
            failed++;
        }

        // Test 6: Rejection Flow (REJECTED -> Routes to Idea Archive)
        try {
            try (PreparedStatement ps = dao.getConnection().prepareStatement("UPDATE Topic SET status = 'SUBMITTED', topic_code = NULL WHERE topic_id = 1;")) {
                ps.executeUpdate();
            }

            Map<Integer, Double> scores = new HashMap<>();
            scores.put(1, 4.0);
            scores.put(2, 3.5);
            scores.put(3, 4.0);
            scores.put(4, 3.0);

            boolean ok = dao.evaluateTopic(1, 1, "REJECTED", "Không đạt chuẩn thời sự.", scores, null, 1.0);

            TopicEvaluationDetailDTO updated = dao.getTopicDetailForEvaluation(1);
            if (ok && updated != null && "REJECTED".equals(updated.getStatus())) {
                System.out.println("[PASS] Test 6: Rejection flow verified. Status = REJECTED.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 6: Rejection failed.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Exception - " + e.getMessage());
            failed++;
        }

        // Test 7: Verify Evaluation_Detail and Notification records created
        try {
            Connection conn = dao.getConnection();
            int detailCount = 0;
            int notifCount = 0;

            String detailSql = "SELECT COUNT(*) FROM Evaluation_Detail;";
            try (PreparedStatement ps = conn.prepareStatement(detailSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) detailCount = rs.getInt(1);
            }

            String notifSql = "SELECT COUNT(*) FROM Notification WHERE topic_id = 1;";
            try (PreparedStatement ps = conn.prepareStatement(notifSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) notifCount = rs.getInt(1);
            }

            if (detailCount >= 4 && notifCount >= 1) {
                System.out.println("[PASS] Test 7: Audit integrity verified (Evaluation_Detail rows: " + detailCount + ", Notifications: " + notifCount + ").");
                passed++;
            } else {
                System.err.println("[FAIL] Test 7: Audit records missing. detailCount=" + detailCount + ", notifCount=" + notifCount);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: Exception - " + e.getMessage());
            failed++;
        }

        // Test 8: Passing Score Threshold Guard (Direct approval with score < 5.0 must throw IllegalStateException)
        try {
            try (PreparedStatement ps = dao.getConnection().prepareStatement("UPDATE Topic SET status = 'SUBMITTED', topic_code = NULL WHERE topic_id = 1;")) {
                ps.executeUpdate();
            }

            Map<Integer, Double> failingScores = new HashMap<>();
            failingScores.put(1, 2.0);
            failingScores.put(2, 2.5);
            failingScores.put(3, 1.5);
            failingScores.put(4, 2.0); // composite is ~2.0 < 5.0

            boolean threwExpected = false;
            try {
                dao.evaluateTopic(1, 1, "APPROVED", "Duyệt đề tài điểm kém", failingScores, null, 2.0);
            } catch (IllegalStateException ex) {
                threwExpected = true;
            }

            if (threwExpected) {
                System.out.println("[PASS] Test 8: Passing score threshold guard verified (Direct approval blocked for score < 5.0).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 8: Expected IllegalStateException for score < 5.0 was not thrown.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: Exception - " + e.getMessage());
            failed++;
        }

        // Test 9: Defense-in-Depth Status Locking Guard (Evaluating non-SUBMITTED topic must throw IllegalStateException)
        try {
            // Topic 2 was escalated to PENDING_EXECUTIVE_APPROVAL in Test 4
            Map<Integer, Double> dummyScores = new HashMap<>();
            dummyScores.put(1, 9.0);
            dummyScores.put(2, 9.0);
            dummyScores.put(3, 9.0);
            dummyScores.put(4, 9.0);

            boolean threwLockException = false;
            try {
                dao.evaluateTopic(2, 1, "APPROVED", "Cố tình phê duyệt đề tài đã chuyển TBT", dummyScores, null, 1.0);
            } catch (IllegalStateException ex) {
                if (ex.getMessage() != null && ex.getMessage().contains("PENDING_EXECUTIVE_APPROVAL")) {
                    threwLockException = true;
                }
            }

            if (threwLockException) {
                System.out.println("[PASS] Test 9: Defense-in-depth status guard verified (Evaluation strictly blocked for PENDING_EXECUTIVE_APPROVAL topic).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 9: Expected IllegalStateException for non-SUBMITTED topic was not thrown.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 9: Exception - " + e.getMessage());
            failed++;
        }

        // Restore baseline test data to preserve test isolation
        TopicDAOTest.resetBaselineData(dao);

        System.out.println("==================================================================");
        System.out.println("TEST SUITE RESULTS: " + passed + " PASSED, " + failed + " FAILED / " + (passed + failed) + " TOTAL TESTS");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
