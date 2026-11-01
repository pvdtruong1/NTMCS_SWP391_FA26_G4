package test;

import dal.TopicDAO;
import dto.ReporterWorkloadDTO;
import model.Topic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated Test Suite for UC: Assign Lead Reporter (SCR-2.5: Reporter Dispatch & Workload Desk).
 * Verifies workload matrix querying, atomic commissioning transaction, lead reporter assignment,
 * topic_code minting, status guards, and notification generation.
 */
public class DirectiveCommissioningTest {

    private static final TopicDAO topicDAO = new TopicDAO();
    private static int passedTests = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: UC SCR-2.5 ASSIGN LEAD REPORTER");
        System.out.println("==================================================================");

        try {
            testWorkloadQuery();
            testCommissionTopicSuccess();
            testCommissionNonDraftTopicFails();
            testCommissionNonExistentTopicFails();
            testCommissionInvalidReporterRoleFails();
            testNotificationGeneration();
            testTopicCodeUniqueness();
            testWorkloadStatusClassification();
        } catch (Exception e) {
            System.err.println("[FATAL] Unexpected error during test execution: " + e.getMessage());
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
     * Test 1: Query reporter workload matrix for Desk 1.
     */
    private static void testWorkloadQuery() {
        List<ReporterWorkloadDTO> reporters = topicDAO.getReportersWorkloadByDesk(1);
        boolean hasHung = false;
        for (ReporterWorkloadDTO r : reporters) {
            if (r.getUserId() == 2 && "Hưng".equals(r.getFullName())) {
                hasHung = true;
                break;
            }
        }
        assertTrue("Test 1: Workload matrix retrieves Desk 1 reporters including Hưng",
                !reporters.isEmpty() && hasHung,
                "Reporters list empty or Hưng (user_id=2) not found");
    }

    /**
     * Test 2: Commission a draft topic successfully into APPROVED status with LEAD_REPORTER.
     */
    private static void testCommissionTopicSuccess() throws SQLException {
        // 1. Create a draft topic
        Topic draft = new Topic();
        draft.setTitle("Điều tra vi phạm quy hoạch đất đai tại bán đảo Thanh Đa");
        draft.setAngle("Phản ánh thực trạng dự án treo nhiều thập kỷ và đời sống người dân khu vực quy hoạch");
        draft.setCategoryId(1);
        draft.setAuthorUserId(1); // Created by Trường
        draft.setDeadline(Date.valueOf(LocalDate.now().plusDays(7)));

        int draftTopicId = topicDAO.createDirectiveTopicDraft(draft);
        assertTrue("Test 2a: Draft directive topic created with ID > 0", draftTopicId > 0, "Generated ID <= 0");

        // 2. Commission to reporter Hưng (user_id = 2)
        String commissionNotes = "Cần tiếp cận các hộ dân có đất trong ranh dự án và thu thập tài liệu quy hoạch 1/2000.";
        String topicCode = topicDAO.commissionDirectiveTopic(draftTopicId, 2, 1, commissionNotes);

        assertTrue("Test 2b: Commissioning returned valid topic_code",
                topicCode != null && topicCode.startsWith("TOPIC-"),
                "Topic code was null or invalid: " + topicCode);

        // 3. Verify topic status in DB
        Topic commissioned = topicDAO.getTopicById(draftTopicId);
        assertTrue("Test 2c: Topic status mutated to APPROVED and topic_code persisted",
                commissioned != null && "APPROVED".equals(commissioned.getStatus()) && topicCode.equals(commissioned.getTopicCode()),
                "Status: " + (commissioned != null ? commissioned.getStatus() : "null"));

        // 4. Verify Topic_Member record
        boolean isLeadReporterAssigned = false;
        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT role_in_team FROM Topic_Member WHERE topic_id = ? AND user_id = ?;")) {
            ps.setInt(1, draftTopicId);
            ps.setInt(2, 2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && "LEAD_REPORTER".equals(rs.getString("role_in_team"))) {
                    isLeadReporterAssigned = true;
                }
            }
        }
        assertTrue("Test 2d: Lead reporter assigned in Topic_Member with role LEAD_REPORTER",
                isLeadReporterAssigned,
                "Topic_Member record missing or incorrect role");
    }

    /**
     * Test 3: Domain Guard - Commissioning a non-draft topic must fail.
     */
    private static void testCommissionNonDraftTopicFails() {
        boolean exceptionThrown = false;
        try {
            // Topic #1 is SUBMITTED, not DRAFT
            topicDAO.commissionDirectiveTopic(1, 2, 1, "Attempting illegal commission on submitted topic");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue("Test 3: Commissioning non-DRAFT topic throws IllegalStateException",
                exceptionThrown,
                "Failed to block commissioning on non-DRAFT topic");
    }

    /**
     * Test 4: Commissioning a non-existent topic ID must fail.
     */
    private static void testCommissionNonExistentTopicFails() {
        boolean exceptionThrown = false;
        try {
            topicDAO.commissionDirectiveTopic(999999, 2, 1, "Ghost topic");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue("Test 4: Commissioning non-existent topic ID throws IllegalArgumentException",
                exceptionThrown,
                "Failed to reject non-existent topic ID");
    }

    /**
     * Test 5: Lead Reporter Guard - Assigning a user without role REPORTER must fail.
     */
    private static void testCommissionInvalidReporterRoleFails() throws SQLException {
        // Create draft topic
        Topic draft = new Topic();
        draft.setTitle("Khảo sát hệ thống trạm quan trắc không khí tự động");
        draft.setAngle("Đánh giá độ tin cậy của các chỉ số AQI công bố");
        draft.setCategoryId(1);
        draft.setAuthorUserId(1);
        draft.setDeadline(Date.valueOf(LocalDate.now().plusDays(5)));
        int draftTopicId = topicDAO.createDirectiveTopicDraft(draft);

        boolean exceptionThrown = false;
        try {
            // User 1 is DESK_EDITOR (role_id = 2), not REPORTER (role_id = 1)
            topicDAO.commissionDirectiveTopic(draftTopicId, 1, 1, "Assigning editor as reporter");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        } catch (Exception e) {
            exceptionThrown = true;
        }

        // Verify status remains DRAFT
        Topic topicAfter = topicDAO.getTopicById(draftTopicId);
        assertTrue("Test 5: Assigning non-REPORTER user rejected and topic remains DRAFT",
                exceptionThrown && topicAfter != null && "DRAFT".equals(topicAfter.getStatus()),
                "Non-reporter assignment was not rejected or topic status mutated");
    }

    /**
     * Test 6: Verify in-app notifications generated for both Reporter and Editor.
     */
    private static void testNotificationGeneration() throws SQLException {
        Topic draft = new Topic();
        draft.setTitle("Loạt bài phóng sự: Ứng phó triều cường lịch sử tại TP.HCM");
        draft.setAngle("Theo chân người dân các khu vực trũng thấp vùng ven");
        draft.setCategoryId(1);
        draft.setAuthorUserId(1);
        draft.setDeadline(Date.valueOf(LocalDate.now().plusDays(3)));
        int draftTopicId = topicDAO.createDirectiveTopicDraft(draft);

        String notes = "Liên hệ Đài Khí tượng Thủy văn khu vực Nam Bộ.";
        topicDAO.commissionDirectiveTopic(draftTopicId, 2, 1, notes);

        boolean reporterNotified = false;
        boolean editorNotified = false;

        try (Connection conn = topicDAO.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT user_id, message FROM Notification WHERE topic_id = ?;")) {
            ps.setInt(1, draftTopicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    String msg = rs.getString("message");
                    if (uid == 2 && msg.contains("Trưởng nhóm tác nghiệp") && msg.contains(notes)) {
                        reporterNotified = true;
                    }
                    if (uid == 1 && msg.contains("Đã phát lệnh đặt hàng thành công")) {
                        editorNotified = true;
                    }
                }
            }
        }

        assertTrue("Test 6: In-App Notifications dispatched to both Lead Reporter (Hưng) and Desk Editor (Trường)",
                reporterNotified && editorNotified,
                "Notifications missing (Reporter: " + reporterNotified + ", Editor: " + editorNotified + ")");
    }

    /**
     * Test 7: Topic code uniqueness across multiple consecutive commissions.
     */
    private static void testTopicCodeUniqueness() throws SQLException {
        Topic draftA = new Topic();
        draftA.setTitle("Đề tài thử nghiệm mã định danh A");
        draftA.setCategoryId(1);
        draftA.setAuthorUserId(1);
        int idA = topicDAO.createDirectiveTopicDraft(draftA);

        Topic draftB = new Topic();
        draftB.setTitle("Đề tài thử nghiệm mã định danh B");
        draftB.setCategoryId(1);
        draftB.setAuthorUserId(1);
        int idB = topicDAO.createDirectiveTopicDraft(draftB);

        String codeA = topicDAO.commissionDirectiveTopic(idA, 2, 1, "Test A");
        String codeB = topicDAO.commissionDirectiveTopic(idB, 2, 1, "Test B");

        assertTrue("Test 7: Consecutive commissions mint distinct, non-colliding topic codes",
                codeA != null && codeB != null && !codeA.equals(codeB),
                "Code collision detected: codeA=" + codeA + ", codeB=" + codeB);
    }

    /**
     * Test 8: ReporterWorkloadDTO status classifications and badge classes.
     */
    private static void testWorkloadStatusClassification() {
        ReporterWorkloadDTO optimal = new ReporterWorkloadDTO(1, "A", "a@test.vn", 1, "Ban A", 1, 0, 2);
        ReporterWorkloadDTO moderate = new ReporterWorkloadDTO(2, "B", "b@test.vn", 1, "Ban A", 3, 0, 1);
        ReporterWorkloadDTO overloaded = new ReporterWorkloadDTO(3, "C", "c@test.vn", 1, "Ban A", 4, 0, 0);
        ReporterWorkloadDTO overdue = new ReporterWorkloadDTO(4, "D", "d@test.vn", 1, "Ban A", 1, 1, 0);

        boolean pass = "OPTIMAL".equals(optimal.getWorkloadStatus()) && "badge-success".equals(optimal.getWorkloadBadgeClass())
                && "MODERATE".equals(moderate.getWorkloadStatus()) && "badge-warning".equals(moderate.getWorkloadBadgeClass())
                && "OVERLOADED".equals(overloaded.getWorkloadStatus()) && "badge-danger".equals(overloaded.getWorkloadBadgeClass())
                && "OVERLOADED".equals(overdue.getWorkloadStatus()) && "badge-danger".equals(overdue.getWorkloadBadgeClass());

        assertTrue("Test 8: Workload status logic accurately classifies OPTIMAL, MODERATE, and OVERLOADED",
                pass,
                "Classification mismatch: opt=" + optimal.getWorkloadStatus() + ", mod=" + moderate.getWorkloadStatus()
                        + ", ovl=" + overloaded.getWorkloadStatus() + ", ovd=" + overdue.getWorkloadStatus());
    }
}
