package test;

import dal.DBContext;
import dal.TopicDAO;
import dal.NotificationDAO;
import dto.TopicEvaluationDetailDTO;
import model.Notification;
import model.Topic;
import model.TopicMember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automated test suite verifying:
 * 1. Loading assigned team members (Fieldwork Crew) into TopicEvaluationDetailDTO for SCR-2.3.
 * 2. Dispatching in-app assignment notifications to all crew members upon topic approval.
 */
public class TopicApprovalMemberNotificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING TEST: TOPIC APPROVAL CREW MEMBERS & NOTIFICATION DISPATCH");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;
        int createdTopicId = 0;

        int authorUserId = 2; // Hưng (Reporter)
        int photoReporterUserId = 4; // Đạt (Assigned Photo Reporter)
        int evaluatorUserId = 1; // Trường (Desk Editor)

        TopicDAO topicDAO = new TopicDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        try {
            // STEP 1: Create a topic with status SUBMITTED and assigned crew members
            Topic topic = new Topic();
            topic.setTitle("[TEST_APPROVAL] Đề tài kiểm thử phân công kíp tác nghiệp " + System.currentTimeMillis());
            topic.setSummary("Tóm tắt đề tài kiểm thử kíp viên");
            topic.setAngle("Góc nhìn thử nghiệm kíp viên");
            topic.setGenre("DIEU_TRA");
            topic.setPriority("NORMAL");
            topic.setStatus("SUBMITTED");
            topic.setCategoryId(1); // Desk 1
            topic.setAuthorUserId(authorUserId);
            topic.setReporterName("Hưng");

            List<TopicMember> crew = new ArrayList<>();
            // Member 1: Author as Lead Reporter
            crew.add(new TopicMember(0, authorUserId, "LEAD_REPORTER"));
            // Member 2: Colleague as Photo Reporter
            crew.add(new TopicMember(0, photoReporterUserId, "PHOTO_REPORTER"));

            Topic savedTopic = topicDAO.saveTopicPitch(topic, null, crew);
            createdTopicId = savedTopic.getTopicId();

            if (createdTopicId <= 0) {
                System.err.println("[FAIL] Step 1: Could not create test topic.");
                failed++;
            } else {
                System.out.println("[PASS] Step 1: Test topic created with ID #" + createdTopicId);
                passed++;
            }

            // STEP 2: Verify getTopicDetailForEvaluation loads members for Desk Editor desk
            TopicEvaluationDetailDTO detailDTO = topicDAO.getTopicDetailForEvaluation(createdTopicId);
            if (detailDTO != null && detailDTO.getMembers() != null && detailDTO.getMembers().size() == 2) {
                System.out.println("[PASS] Step 2: getTopicDetailForEvaluation loaded " + detailDTO.getMembers().size() + " crew members successfully.");
                passed++;
            } else {
                System.err.println("[FAIL] Step 2: Failed to load crew members in detailDTO. Count: "
                        + (detailDTO != null && detailDTO.getMembers() != null ? detailDTO.getMembers().size() : 0));
                failed++;
            }

            // STEP 3: Desk Editor approves the topic via evaluateTopic
            Map<Integer, Double> scores = new HashMap<>();
            scores.put(1, 8.0);
            scores.put(2, 8.5);
            scores.put(3, 8.0);
            scores.put(4, 8.0);
            Map<Integer, String> criteriaComments = new HashMap<>();

            boolean approved = topicDAO.evaluateTopic(
                    createdTopicId,
                    evaluatorUserId,
                    2, // Desk Editor role
                    "APPROVED",
                    "Đồng ý phê duyệt kíp tác nghiệp và triển khai hiện trường.",
                    scores,
                    criteriaComments,
                    3.0 // Low legal risk
            );

            if (approved) {
                System.out.println("[PASS] Step 3: Topic #" + createdTopicId + " approved successfully.");
                passed++;
            } else {
                System.err.println("[FAIL] Step 3: Topic evaluation failed.");
                failed++;
            }

            // STEP 4: Verify Author received approval notification
            List<Notification> authorNotifs = notifDAO.findByUserId(authorUserId);
            boolean authorNotifFound = false;
            for (Notification n : authorNotifs) {
                if (n.getTopicId() != null && n.getTopicId() == createdTopicId && n.getMessage().contains("phê duyệt")) {
                    authorNotifFound = true;
                    System.out.println("[PASS] Step 4A: Author received approval notification: " + n.getMessage());
                    break;
                }
            }
            if (authorNotifFound) {
                passed++;
            } else {
                System.err.println("[FAIL] Step 4A: Author did not receive approval notification.");
                failed++;
            }

            // STEP 5: Verify Photo Reporter received assignment notification
            List<Notification> crewNotifs = notifDAO.findByUserId(photoReporterUserId);
            boolean crewNotifFound = false;
            for (Notification n : crewNotifs) {
                if (n.getTopicId() != null && n.getTopicId() == createdTopicId && n.getMessage().contains("[PHÂN CÔNG TÁC NGHIỆP]")) {
                    crewNotifFound = true;
                    System.out.println("[PASS] Step 4B: Crew member (User #" + photoReporterUserId + ") received assignment notification: " + n.getMessage());
                    break;
                }
            }
            if (crewNotifFound) {
                passed++;
            } else {
                System.err.println("[FAIL] Step 4B: Crew member did not receive assignment notification.");
                failed++;
            }

        } catch (Exception e) {
            System.err.println("[FAIL] Exception during test execution: " + e.getMessage());
            e.printStackTrace();
            failed++;
        } finally {
            // STEP 6: Clean up test data
            if (createdTopicId > 0) {
                cleanupTestData(createdTopicId);
                System.out.println("[CLEANUP] Cleaned up test topic #" + createdTopicId + " and associated data.");
            }
        }

        System.out.println("==================================================================");
        System.out.println("TEST SUMMARY: " + passed + " PASSED, " + failed + " FAILED.");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
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
                // Delete fieldwork tasks
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Fieldwork_Task WHERE topic_id = ?")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
                // Delete evaluation details
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Evaluation_Detail WHERE evaluation_id IN (SELECT evaluation_id FROM Topic_Evaluation WHERE topic_id = ?)")) {
                    ps.setInt(1, topicId);
                    ps.executeUpdate();
                }
                // Delete evaluation header
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic_Evaluation WHERE topic_id = ?")) {
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
