package test;

import dal.TopicDAO;
import dto.TopicArchiveDTO;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * Automated test suite covering all operational requirements, domain guard invariants,
 * and edge cases for SCR-2.8 (Idea Archive & Suspended Topic Catalog).
 */
public class IdeaArchiveTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: UC SCR-2.8 BROWSE IDEA ARCHIVE");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        TopicDAO dao = new TopicDAO();

        // Test 1: Query Archived Topics for Desk 1
        try {
            List<TopicArchiveDTO> list = dao.getArchivedTopics(1, "ALL", 0, null, 1, 10);
            if (list != null && !list.isEmpty()) {
                TopicArchiveDTO first = list.get(0);
                if (first.getTopicId() == 8 && "REJECTED".equals(first.getStatus()) && first.getDeskId() == 1) {
                    System.out.println("[PASS] Test 1: Retrieved archived topic #" + first.getTopicId() + " (" + first.getTitle().substring(0, 30) + "...) with desk isolation.");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 1: Unexpected topic data: ID=" + first.getTopicId() + ", Status=" + first.getStatus());
                    failed++;
                }
            } else {
                System.err.println("[FAIL] Test 1: No archived topics returned for Desk 1.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Desk Isolation Verification (Desk 2 must not see Desk 1's archives)
        try {
            List<TopicArchiveDTO> desk2List = dao.getArchivedTopics(2, "ALL", 0, null, 1, 10);
            boolean leaked = false;
            for (TopicArchiveDTO dto : desk2List) {
                if (dto.getDeskId() != 2) {
                    leaked = true;
                    break;
                }
            }
            if (!leaked) {
                System.out.println("[PASS] Test 2: Desk 2 isolation verified (No cross-desk data leakage).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Desk isolation violated.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Status Filtering (REJECTED vs ABORTED)
        try {
            List<TopicArchiveDTO> rejectedList = dao.getArchivedTopics(1, "REJECTED", 0, null, 1, 10);
            List<TopicArchiveDTO> abortedList = dao.getArchivedTopics(1, "ABORTED", 0, null, 1, 10);

            if (rejectedList.size() >= 1 && abortedList.isEmpty()) {
                System.out.println("[PASS] Test 3: Status filtering verified (REJECTED: " + rejectedList.size() + ", ABORTED: " + abortedList.size() + ").");
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Status filter mismatch. REJECTED=" + rejectedList.size() + ", ABORTED=" + abortedList.size());
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Keyword Search in Archive
        try {
            List<TopicArchiveDTO> searchFound = dao.getArchivedTopics(1, "ALL", 0, "ẩm thực", 1, 10);
            List<TopicArchiveDTO> searchNotFound = dao.getArchivedTopics(1, "ALL", 0, "xyz_not_exist_999", 1, 10);

            if (searchFound.size() >= 1 && searchNotFound.isEmpty()) {
                System.out.println("[PASS] Test 4: Keyword search verified (Matches found: " + searchFound.size() + ", Non-matching: " + searchNotFound.size() + ").");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Search filter failure.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Rejection Reason and Evaluator Information Integrity
        try {
            List<TopicArchiveDTO> list = dao.getArchivedTopics(1, "ALL", 0, null, 1, 10);
            TopicArchiveDTO dto = list.get(0);

            if (dto.getRejectionReason() != null && !dto.getRejectionReason().isEmpty() &&
                dto.getEvaluatorName() != null && !dto.getEvaluatorName().isEmpty()) {
                System.out.println("[PASS] Test 5: Reason & Evaluator audit metadata intact (BTV: " + dto.getEvaluatorName() + ", Lý do: " + dto.getRejectionReason().substring(0, 35) + "...).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Missing rejection reason or evaluator name: reason=" + dto.getRejectionReason() + ", evaluator=" + dto.getEvaluatorName());
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
            failed++;
        }

        // Test 6: KPI Stats Calculation & Categories Retrieval
        try {
            Map<String, Integer> stats = dao.getArchiveStats(1);
            List<Category> categories = dao.getCategoriesByDesk(1);

            if (stats != null && stats.containsKey("TOTAL") && stats.containsKey("REJECTED") &&
                stats.get("TOTAL") >= 1 && stats.get("REJECTED") >= 1 &&
                categories != null && !categories.isEmpty()) {
                System.out.println("[PASS] Test 6: KPI Stats and Categories verified (Categories: " + categories.size() + ", Stats Total: " + stats.get("TOTAL") + ", Rejected: " + stats.get("REJECTED") + ").");
                passed++;
            } else {
                System.err.println("[FAIL] Test 6: KPI Stats or Categories missing.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Exception - " + e.getMessage());
            failed++;
        }

        // Test 7: Closed-Loop Reactivation Lifecycle (REJECTED -> DRAFT)
        try {
            boolean reactivated = dao.reactivateArchivedTopic(8, 1, "Phục hồi đề tài theo chỉ đạo thời sự mới.");
            if (reactivated) {
                // Verify topic status mutated to DRAFT
                Connection conn = dao.getConnection();
                String checkSql = "SELECT status FROM Topic WHERE topic_id = 8;";
                String currentStatus = null;
                try (PreparedStatement ps = conn.prepareStatement(checkSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = rs.getString("status");
                    }
                }

                // Verify notification sent to author
                String notifSql = "SELECT COUNT(*) FROM Notification WHERE topic_id = 8 AND message LIKE N'%tái kích hoạt%';";
                int notifCount = 0;
                try (PreparedStatement ps = conn.prepareStatement(notifSql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        notifCount = rs.getInt(1);
                    }
                }

                if ("DRAFT".equals(currentStatus) && notifCount >= 1) {
                    System.out.println("[PASS] Test 7: Closed-loop reactivation verified (Status mutated to DRAFT, Author notification dispatched).");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 7: Status is " + currentStatus + ", notifCount=" + notifCount);
                    failed++;
                }
            } else {
                System.err.println("[FAIL] Test 7: reactivateArchivedTopic returned false.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: Exception - " + e.getMessage());
            failed++;
        }

        // Test 8: Domain Guard Clause (Prohibit illegal reactivation on active/submitted topic)
        try {
            // Topic 1 is SUBMITTED, not REJECTED or ABORTED
            boolean caughtIllegal = false;
            try {
                dao.reactivateArchivedTopic(1, 1, "Illegal reactivation test");
            } catch (IllegalStateException ex) {
                caughtIllegal = true;
            }

            if (caughtIllegal) {
                System.out.println("[PASS] Test 8: Domain guard clause verified (Illegal reactivation on non-archived topic strictly blocked).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 8: Expected IllegalStateException was not thrown for Topic #1.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: Exception - " + e.getMessage());
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
