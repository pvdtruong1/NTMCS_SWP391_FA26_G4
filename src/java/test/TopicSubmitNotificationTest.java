package test;

import dal.DBContext;
import dal.TopicDAO;
import dal.NotificationDAO;
import model.Notification;
import model.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Automated test verifying Desk Editor notification trigger when a reporter submits a topic pitch.
 */
public class TopicSubmitNotificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING TEST: DESK EDITOR NOTIFICATION ON TOPIC PITCH SUBMIT");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;
        int createdTopicId = 0;
        int deskEditorUserId = 1; // Trường - Desk Editor of Desk 1

        TopicDAO topicDAO = new TopicDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        try {
            // STEP 1: Create a DRAFT topic - should NOT trigger notification
            Topic draftTopic = new Topic();
            draftTopic.setTitle("[TEST_PITCH] Thử nghiệm thông báo nộp đề tài " + System.currentTimeMillis());
            draftTopic.setSummary("Tóm tắt thử nghiệm thông báo");
            draftTopic.setAngle("Góc nhìn kiểm thử");
            draftTopic.setGenre("PHONG_SU");
            draftTopic.setPriority("NORMAL");
            draftTopic.setStatus("DRAFT");
            draftTopic.setCategoryId(1); // Desk 1
            draftTopic.setAuthorUserId(2); // Reporter Hưng
            draftTopic.setReporterName("Hưng");

            Topic savedDraft = topicDAO.saveTopicPitch(draftTopic, null, null);
            createdTopicId = savedDraft.getTopicId();

            if (createdTopicId <= 0) {
                System.err.println("[FAIL] Could not save draft topic.");
                failed++;
            } else {
                System.out.println("[PASS] Step 1: Draft topic created with ID #" + createdTopicId);
                passed++;
            }

            // Verify no notification was sent for this draft
            boolean hasDraftNotif = checkTopicNotificationExists(deskEditorUserId, createdTopicId);
            if (!hasDraftNotif) {
                System.out.println("[PASS] Step 2: Confirmed ZERO notifications sent for DRAFT state.");
                passed++;
            } else {
                System.err.println("[FAIL] Step 2: Unexpected notification found for DRAFT state.");
                failed++;
            }

            // STEP 3: Transition topic to SUBMITTED - MUST trigger notification for Desk Editor
            savedDraft.setStatus("SUBMITTED");
            Topic submittedTopic = topicDAO.saveTopicPitch(savedDraft, null, null);

            boolean hasSubmitNotif = checkTopicNotificationExists(deskEditorUserId, createdTopicId);
            if (hasSubmitNotif) {
                System.out.println("[PASS] Step 3: Desk Editor #" + deskEditorUserId + " successfully received notification for SUBMITTED topic #" + createdTopicId);
                passed++;
            } else {
                System.err.println("[FAIL] Step 3: No notification found for Desk Editor after submission.");
                failed++;
            }

            // STEP 4: Verify notification message content
            List<Notification> notifs = notifDAO.findByUserId(deskEditorUserId);
            boolean contentValid = false;
            for (Notification n : notifs) {
                if (n.getTopicId() != null && n.getTopicId() == createdTopicId) {
                    if (n.getMessage().contains("[ĐỀ TÀI CHỜ DUYỆT]") && n.getMessage().contains(savedDraft.getTitle())) {
                        contentValid = true;
                        System.out.println("[PASS] Step 4: Notification payload verified: " + n.getMessage());
                        break;
                    }
                }
            }
            if (contentValid) {
                passed++;
            } else {
                System.err.println("[FAIL] Step 4: Notification content does not match expected template.");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] Exception during test execution: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            // STEP 5: Clean up test data
            if (createdTopicId > 0) {
                cleanupTestData(createdTopicId);
                System.out.println("[CLEANUP] Deleted test topic #" + createdTopicId + " and associated notifications.");
            }
        }

        System.out.println("==================================================================");
        System.out.println("TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED.");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static boolean checkTopicNotificationExists(int userId, int topicId) {
        String sql = "SELECT COUNT(*) FROM Notification WHERE user_id = ? AND topic_id = ?";
        try {
            DBContext db = new DBContext();
            Connection conn = db.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void cleanupTestData(int topicId) {
        try {
            DBContext db = new DBContext();
            Connection conn = db.getConnection();
            try {
                // Delete notifications
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Notification WHERE topic_id = ?")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
                // Delete estimates
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic_Estimate WHERE topic_id = ?")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
                // Delete members
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic_Member WHERE topic_id = ?")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
                // Delete topic
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic WHERE topic_id = ?")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
