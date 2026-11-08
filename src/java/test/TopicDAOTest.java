package test;

import dal.DBContext;
import dal.TopicDAO;
import dto.TopicPendingDTO;
import model.Desk;

import java.util.List;

/**
 * Automated test suite covering all operational edge cases for TopicDAO / SCR-2.2.
 */
public class TopicDAOTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: UC SCR-2.2 ACCESS PENDING QUEUE");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        // Test 1: Database Connection
        try {
            DBContext db = new DBContext();
            boolean connected = db.testConnection();
            if (connected) {
                System.out.println("[PASS] Test 1: DB Connection test successful.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: DB Connection failed.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        TopicDAO dao = new TopicDAO();
        resetBaselineData(dao);

        // Test 2: Pending Topics for Desk 1 (SUBMITTED) & Isolation from Desk 2
        try {
            List<TopicPendingDTO> list = dao.getPendingTopics(1, "SUBMITTED", null, 1, 10);
            boolean deskIsolated = true;
            boolean onlySubmitted = true;
            for (TopicPendingDTO t : list) {
                if (t.getDeskId() != 1) deskIsolated = false;
                if (!"SUBMITTED".equals(t.getStatus())) onlySubmitted = false;
            }
            if (list.size() >= 2 && deskIsolated && onlySubmitted) {
                System.out.println("[PASS] Test 2: Retrieved " + list.size() + " SUBMITTED topics for Desk 1 with 100% desk isolation.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Failed. Size=" + list.size() + ", deskIsolated=" + deskIsolated + ", onlySubmitted=" + onlySubmitted);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Cross-Desk Isolation (Desk 2)
        try {
            List<TopicPendingDTO> listDesk2 = dao.getPendingTopics(2, "SUBMITTED", null, 1, 10);
            boolean isDesk2Only = true;
            for (TopicPendingDTO t : listDesk2) {
                if (t.getDeskId() != 2) isDesk2Only = false;
            }
            if (listDesk2.size() >= 1 && isDesk2Only) {
                System.out.println("[PASS] Test 3: Desk 2 isolation verified (" + listDesk2.size() + " topic strictly for Desk 2).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Failed cross-desk isolation.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Escalated Queue (PENDING_EXECUTIVE_APPROVAL)
        try {
            List<TopicPendingDTO> escalated = dao.getPendingTopics(1, "PENDING_EXECUTIVE_APPROVAL", null, 1, 10);
            boolean allEscalated = true;
            for (TopicPendingDTO t : escalated) {
                if (!"PENDING_EXECUTIVE_APPROVAL".equals(t.getStatus())) allEscalated = false;
            }
            if (escalated.size() >= 1 && allEscalated) {
                System.out.println("[PASS] Test 4: Retrieved " + escalated.size() + " escalated topic(s) for Desk 1.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Failed to retrieve escalated topics.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: DRAFT status isolation (Draft must NEVER appear in Pending Queue)
        try {
            List<TopicPendingDTO> draftsInSubmitted = dao.getPendingTopics(1, "SUBMITTED", "Ý tưởng thô", 1, 10);
            List<TopicPendingDTO> draftsInEscalated = dao.getPendingTopics(1, "PENDING_EXECUTIVE_APPROVAL", "Ý tưởng thô", 1, 10);
            if (draftsInSubmitted.isEmpty() && draftsInEscalated.isEmpty()) {
                System.out.println("[PASS] Test 5: Draft status isolation verified (Drafts never leak into pending queue).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Draft leaked into pending queue!");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
            failed++;
        }

        // Test 6: Search functionality (Keyword matching & Non-existent keyword)
        try {
            List<TopicPendingDTO> matched = dao.getPendingTopics(1, "SUBMITTED", "Chuyển đổi số", 1, 10);
            List<TopicPendingDTO> nonExistent = dao.getPendingTopics(1, "SUBMITTED", "NON_EXISTENT_KEYWORD_XYZ999", 1, 10);
            if (!matched.isEmpty() && nonExistent.isEmpty()) {
                System.out.println("[PASS] Test 6: Keyword search verified (Found: " + matched.size() + ", Non-existent: 0).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 6: Keyword search failed. Matched=" + matched.size() + ", NonExistent=" + nonExistent.size());
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Exception - " + e.getMessage());
            failed++;
        }

        // Test 7: Pagination (Offset and limit integrity)
        try {
            List<TopicPendingDTO> page1 = dao.getPendingTopics(1, "SUBMITTED", null, 1, 1);
            List<TopicPendingDTO> page2 = dao.getPendingTopics(1, "SUBMITTED", null, 2, 1);
            List<TopicPendingDTO> pageEmpty = dao.getPendingTopics(1, "SUBMITTED", null, 999, 10);

            if (page1.size() == 1 && page2.size() == 1 &&
                page1.get(0).getTopicId() != page2.get(0).getTopicId() &&
                pageEmpty.isEmpty()) {
                System.out.println("[PASS] Test 7: Pagination verified (Page 1 ID: " + page1.get(0).getTopicId() +
                                   ", Page 2 ID: " + page2.get(0).getTopicId() + ", Page 999: empty).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 7: Pagination failed.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: Exception - " + e.getMessage());
            failed++;
        }

        // Test 8: Badge Counter Integrity
        try {
            int countSub = dao.countByStatusAndDesk(1, "SUBMITTED");
            int countEsc = dao.countByStatusAndDesk(1, "PENDING_EXECUTIVE_APPROVAL");
            int actualSub = dao.countPendingTopics(1, "SUBMITTED", null);
            int actualEsc = dao.countPendingTopics(1, "PENDING_EXECUTIVE_APPROVAL", null);

            if (countSub == actualSub && countEsc == actualEsc && countSub >= 2 && countEsc >= 1) {
                System.out.println("[PASS] Test 8: Badge counter integrity verified (SUBMITTED: " + countSub + ", ESCALATED: " + countEsc + ").");
                passed++;
            } else {
                System.err.println("[FAIL] Test 8: Badge counters mismatch. countSub=" + countSub + " vs " + actualSub + "; countEsc=" + countEsc + " vs " + actualEsc);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: Exception - " + e.getMessage());
            failed++;
        }

        // Test 9: Desk Info retrieval
        try {
            Desk desk = dao.getDeskById(1);
            if (desk != null && desk.getDeskName() != null && !desk.getDeskName().isEmpty()) {
                System.out.println("[PASS] Test 9: Desk retrieval verified: " + desk.getDeskName());
                passed++;
            } else {
                System.err.println("[FAIL] Test 9: Could not retrieve desk for id 1.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 9: Exception - " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================================");
        System.out.println("TEST SUITE RESULTS: " + passed + " PASSED, " + failed + " FAILED / " + (passed + failed) + " TOTAL TESTS");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    public static void resetBaselineData(TopicDAO dao) {
        try {
            java.sql.Connection conn = dao.getConnection();
            try (java.sql.Statement st = conn.createStatement()) {
                st.executeUpdate("UPDATE Topic SET status = 'SUBMITTED', topic_code = NULL WHERE topic_id IN (1, 2);");
                st.executeUpdate("UPDATE Topic SET status = 'SUBMITTED' WHERE topic_id = 3;");
                st.executeUpdate("UPDATE Topic SET status = 'PENDING_EXECUTIVE_APPROVAL' WHERE topic_id = 4;");
                st.executeUpdate("UPDATE Topic SET status = 'DRAFT' WHERE topic_id = 5;");
                st.executeUpdate("UPDATE Topic SET status = 'REJECTED' WHERE topic_id = 8;");
            }
        } catch (Exception ex) {
            System.err.println("Warning during resetBaselineData: " + ex.getMessage());
        }
    }
}
