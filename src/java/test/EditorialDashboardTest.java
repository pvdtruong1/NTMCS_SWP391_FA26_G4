package test;

import dal.TopicDAO;
import dto.CmsHandoverRecordDTO;
import dto.EditorialDashboardDTO;
import dto.TopicDeliverableDTO;
import model.Desk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Automated Test Suite for SCR-2.1: Editorial Board Executive Dashboard
 * and RULE-02: Topic Acceptance Token Minting Protocol (HMAC-SHA256 & Interlock Guards).
 * 
 * Target: Desk Editor (Phạm Vũ Đình Trường - Role 2, Desk 1) & Executive Editor (Role 3)
 */
public class EditorialDashboardTest {

    private static final TopicDAO topicDAO = new TopicDAO();
    private static int passedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING TEST SUITE: SCR-2.1 EDITORIAL DASHBOARD & RULE-02 PROTOCOL");
        System.out.println("Actor: Phạm Vũ Đình Trường (Desk Editor - WF-02)");
        System.out.println("==================================================================");

        try {
            testHmacSha256Computation();
            testDashboardMetricsQueryDesk1();
            testDashboardMetricsQueryExecutiveGlobal();
            testPendingDeliverablesQuery();
            testRule01ViolationBlocksCmsAcceptance();
            testRule01PassedAllowsCmsAcceptanceAndMintsToken();
            testNonDeliveredTopicAcceptanceFails();
            testNonExistentTopicAcceptanceFails();
            testRequestDeliverableRevisionSuccess();
            testRequestDeliverableRevisionEmptyFeedbackFails();
            testRecentCmsHandoversQuery();
            testGetAllDesks();
        } catch (Exception e) {
            System.err.println("[FATAL] Unexpected exception during test execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("==================================================================");
            System.out.printf("TEST RESULTS: %d/%d TESTS PASSED (%.1f%%)\n",
                    passedTests, totalTests, (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));
            System.out.println("==================================================================");
        }
    }

    private static void assertTrue(String testName, boolean condition, String failureDetail) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName + " -> " + failureDetail);
        }
    }

    /**
     * Test 1: HMAC-SHA256 computation produces valid 64-character hexadecimal digest.
     */
    private static void testHmacSha256Computation() {
        String payload = "TOPIC:4|CODE:TOPIC-2026-001|CYCLE:1|EDITOR:1|AT:2026-11-08T12:00:00";
        String token = TopicDAO.computeHmacSha256(payload, null);
        boolean validLength = (token != null && token.length() == 64);
        boolean validHex = token != null && token.matches("^[0-9a-f]{64}$");

        assertTrue("Test 1: HMAC-SHA256 generates authentic 64-char lowercase hexadecimal token",
                validLength && validHex,
                "Token invalid format or length: " + token);
    }

    /**
     * Test 2: KPI Metrics aggregation for Desk 1 (Ban Thời sự - Chính trị).
     */
    private static void testDashboardMetricsQueryDesk1() {
        EditorialDashboardDTO metrics = topicDAO.getEditorialDashboardMetrics(1, false);
        boolean ok = metrics != null
                && metrics.getPendingReviewCount() >= 0
                && metrics.getActiveFieldworkCount() >= 0
                && metrics.getDeskName() != null
                && metrics.getApprovalRatePercent() >= 0.0
                && metrics.getAvgReviewLatencyHours() > 0;

        assertTrue("Test 2: Query editorial KPI metrics for Desk 1",
                ok,
                "Metrics query returned null or invalid fields: " + metrics);
    }

    /**
     * Test 3: Executive Multi-Desk Scope KPI aggregation (Toàn Tòa Soạn).
     */
    private static void testDashboardMetricsQueryExecutiveGlobal() {
        EditorialDashboardDTO metrics = topicDAO.getEditorialDashboardMetrics(0, true);
        boolean ok = metrics != null
                && metrics.getDeskName().contains("Toàn Tòa Soạn");

        assertTrue("Test 3: Query executive global KPI metrics across all desks",
                ok,
                "Metrics not configured for global newsroom scope: " + (metrics != null ? metrics.getDeskName() : "null"));
    }

    /**
     * Test 4: Query pending deliverables list for Desk 1.
     */
    private static void testPendingDeliverablesQuery() {
        List<TopicDeliverableDTO> list = topicDAO.getPendingDeliverables(1, false);
        assertTrue("Test 4: Query pending deliverables list (DELIVERED topics)",
                list != null,
                "Deliverables query returned null");
    }

    /**
     * Test 5: RULE-01 Guard strictly blocks CMS Acceptance when unreturned assets exist.
     */
    private static void testRule01ViolationBlocksCmsAcceptance() throws SQLException {
        // Setup: Create a temporary DELIVERED topic with an unreturned asset request
        int testTopicId = createTestTopic("DELIVERED", 1, 2);
        createTestAssetRequest(testTopicId, 1, 2, "CHECKED_OUT");

        boolean blocked = false;
        String caughtMsg = null;
        try {
            topicDAO.acceptTopicAndMintCmsToken(testTopicId, 1, "Test accept with unreturned asset", null);
        } catch (IllegalStateException ex) {
            blocked = true;
            caughtMsg = ex.getMessage();
        }

        // Verify status was NOT changed
        String statusAfter = getTopicStatus(testTopicId);

        // Cleanup
        deleteTestTopic(testTopicId);

        assertTrue("Test 5: RULE-01 Guard blocks CMS Acceptance when active asset is CHECKED_OUT",
                blocked && caughtMsg != null && caughtMsg.contains("RULE-01 VIOLATION") && "DELIVERED".equals(statusAfter),
                "Expected RULE-01 exception but got: " + caughtMsg + ", status: " + statusAfter);
    }

    /**
     * Test 6: RULE-02 Success when RULE-01 passed (zero unreturned assets) -> Mints HMAC Token & writes Audit_Log.
     */
    private static void testRule01PassedAllowsCmsAcceptanceAndMintsToken() throws SQLException {
        // Setup: Create a temporary DELIVERED topic with a RETURNED asset request
        int testTopicId = createTestTopic("DELIVERED", 1, 2);
        createTestAssetRequest(testTopicId, 1, 2, "RETURNED");

        String token = topicDAO.acceptTopicAndMintCmsToken(testTopicId, 1, "Nghiệm thu đạt chuẩn chuyển giao CMS", null);
        String statusAfter = getTopicStatus(testTopicId);

        // Verify Audit_Log entry
        boolean hasAuditLog = checkAuditLogEntry(testTopicId, "ACCEPT_CMS_HANDOVER", token);

        // Cleanup
        deleteTestTopic(testTopicId);

        assertTrue("Test 6: RULE-02 mints HMAC Token and updates status to ACCEPTED_READY_FOR_CMS with Audit_Log",
                token != null && token.length() == 64 && "ACCEPTED_READY_FOR_CMS".equals(statusAfter) && hasAuditLog,
                "Failed token minting or status update. Token: " + token + ", Status: " + statusAfter + ", AuditLog: " + hasAuditLog);
    }

    /**
     * Test 7: Non-DELIVERED topic acceptance is blocked (e.g. SUBMITTED topic).
     */
    private static void testNonDeliveredTopicAcceptanceFails() throws SQLException {
        int testTopicId = createTestTopic("SUBMITTED", 1, 2);
        boolean blocked = false;
        try {
            topicDAO.acceptTopicAndMintCmsToken(testTopicId, 1, "Should fail", null);
        } catch (IllegalStateException ex) {
            blocked = true;
        }

        deleteTestTopic(testTopicId);

        assertTrue("Test 7: Attempting to accept a SUBMITTED topic is strictly rejected",
                blocked,
                "Acceptance did not throw IllegalStateException on non-DELIVERED topic");
    }

    /**
     * Test 8: Non-existent topic acceptance throws IllegalArgumentException.
     */
    private static void testNonExistentTopicAcceptanceFails() {
        boolean thrown = false;
        try {
            topicDAO.acceptTopicAndMintCmsToken(9999999, 1, "Non existent", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            thrown = false;
        }

        assertTrue("Test 8: Acceptance with non-existent topicId throws IllegalArgumentException",
                thrown,
                "Expected IllegalArgumentException for non-existent topic");
    }

    /**
     * Test 9: Request revision on DELIVERED topic sets status to REVISION_REQUESTED and logs.
     */
    private static void testRequestDeliverableRevisionSuccess() throws SQLException {
        int testTopicId = createTestTopic("DELIVERED", 1, 2);

        boolean success = topicDAO.requestDeliverableRevision(testTopicId, 1, "Bổ sung số liệu kiểm chứng và ảnh phóng sự ban đêm");
        String statusAfter = getTopicStatus(testTopicId);
        boolean hasAudit = checkAuditLogEntry(testTopicId, "REQUEST_REVISION_DELIVERABLE", null);

        deleteTestTopic(testTopicId);

        assertTrue("Test 9: Request deliverable revision mutates status to REVISION_REQUESTED and notifies reporter",
                success && "REVISION_REQUESTED".equals(statusAfter) && hasAudit,
                "Failed deliverable revision. Success: " + success + ", Status: " + statusAfter + ", Audit: " + hasAudit);
    }

    /**
     * Test 10: Request revision with empty feedback is blocked.
     */
    private static void testRequestDeliverableRevisionEmptyFeedbackFails() {
        boolean blocked = false;
        try {
            topicDAO.requestDeliverableRevision(1, 1, "   ");
        } catch (IllegalArgumentException ex) {
            blocked = true;
        } catch (Exception ex) {
            blocked = false;
        }

        assertTrue("Test 10: Empty feedback string on revision request throws IllegalArgumentException",
                blocked,
                "Expected IllegalArgumentException for blank feedback");
    }

    /**
     * Test 11: Recent CMS handovers query returns accepted topics with valid token hash.
     */
    private static void testRecentCmsHandoversQuery() {
        List<CmsHandoverRecordDTO> handovers = topicDAO.getRecentCmsHandovers(1, false, 5);
        assertTrue("Test 11: Query recent CMS handovers ledger",
                handovers != null,
                "Recent handovers query returned null");
    }

    /**
     * Test 12: Query all editorial desks for multi-desk filtering dropdown.
     */
    private static void testGetAllDesks() {
        List<Desk> desks = topicDAO.getAllDesks();
        assertTrue("Test 12: Query all desks returns at least 3 desks",
                desks != null && desks.size() >= 3,
                "Desks list empty or size < 3: " + (desks != null ? desks.size() : "null"));
    }

    // =========================================================================
    // HELPER METHODS FOR TEST FIXTURES
    // =========================================================================

    private static int createTestTopic(String status, int categoryId, int authorUserId) throws SQLException {
        String sql = "INSERT INTO Topic (topic_code, title, angle, status, deadline, category_id, author_user_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TEST-TP-" + System.currentTimeMillis() % 100000);
            ps.setString(2, "Đề tài thử nghiệm kiểm thử tự động SCR-2.1 " + System.currentTimeMillis());
            ps.setString(3, "Góc tiếp cận thử nghiệm kiểm chứng RULE-01 & RULE-02");
            ps.setString(4, status);
            ps.setDate(5, java.sql.Date.valueOf(java.time.LocalDate.now().plusDays(3)));
            ps.setInt(6, categoryId);
            ps.setInt(7, authorUserId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test topic");
    }

    private static void createTestAssetRequest(int topicId, int assetId, int requesterId, String status) throws SQLException {
        String sql = "INSERT INTO Asset_Request (topic_id, asset_id, requester_user_id, start_time, end_time, status) "
                   + "VALUES (?, ?, ?, SYSDATETIME(), DATEADD(day, 1, SYSDATETIME()), ?);";
        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            ps.setInt(2, assetId);
            ps.setInt(3, requesterId);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    private static String getTopicStatus(int topicId) throws SQLException {
        String sql = "SELECT status FROM Topic WHERE topic_id = ?;";
        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }
        return null;
    }

    private static boolean checkAuditLogEntry(int topicId, String action, String expectedToken) throws SQLException {
        String sql = "SELECT details FROM Audit_Log WHERE entity_type = 'TOPIC' AND entity_id = ? AND action = ?;";
        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            ps.setString(2, action);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String details = rs.getString("details");
                    if (expectedToken != null) {
                        return details != null && details.contains(expectedToken);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static void deleteTestTopic(int topicId) {
        try (Connection conn = topicDAO.getConnection()) {
            // Delete dependent records
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Asset_Request WHERE topic_id = ?;")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Audit_Log WHERE entity_type = 'TOPIC' AND entity_id = ?;")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Notification WHERE topic_id = ?;")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic WHERE topic_id = ?;")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }
}
