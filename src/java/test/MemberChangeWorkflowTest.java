package test;

import dal.DBContext;
import dal.TopicDAO;
import dal.TopicRequestDAO;
import dto.MemberChangeRequestDTO;
import model.Topic;
import model.TopicMember;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Test Suite for Member Change Workflow (Add / Remove crew member).
 * Verifies newsroom tone (NO "đồng chí"), ACID transaction, Task re-assignment on remove,
 * and Desk Editor approval/rejection notifications.
 * 
 * Author: Phạm Vũ Đình Trường (WF-02)
 */
public class MemberChangeWorkflowTest {

    private static final String BANNER = "==================================================================";

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println("RUNNING TEST SUITE: MEMBER CHANGE WORKFLOW (ADD / REMOVE CREW)");
        System.out.println(BANNER);

        int passed = 0;
        int failed = 0;

        TopicDAO topicDAO = new TopicDAO();
        TopicRequestDAO requestDAO = new TopicRequestDAO();

        int testTopicId = 0;
        int authorUserId = 2; // Reporter
        int targetUserId = 4; // Photographer/Reporter to test
        int editorUserId = 1; // Desk Editor

        try {
            // STEP 1: Create a test topic in APPROVED status
            Topic t = new Topic();
            t.setTitle("[TEST_MEMBER] Đề tài thử nghiệm luồng điều chuyển kíp tác nghiệp " + System.currentTimeMillis());
            t.setCategoryId(1);
            t.setAuthorUserId(authorUserId);
            t.setStatus("APPROVED");
            t.setPriority("NORMAL");
            t.setGenre("PHONG_SU");
            t.setSummary("Tóm tắt đề tài kiểm thử");

            Topic saved = topicDAO.saveTopicPitch(t, null, null);
            testTopicId = saved.getTopicId();
            System.out.println("[PASS] Step 1: Created test topic #" + testTopicId + " with status APPROVED");
            passed++;

            // STEP 2: Reporter submits proposal to ADD member (targetUserId = 4)
            boolean addNotifSent = requestDAO.sendMemberChangeNotification(testTopicId, authorUserId, targetUserId, "ADD", "Cần bổ sung nhân sự chụp ảnh tư liệu");
            if (!addNotifSent) throw new AssertionError("Failed to send add member notification");

            // Verify notification message format (MUST NOT contain "đồng chí")
            List<MemberChangeRequestDTO> pendingList = requestDAO.getMemberChangeRequests(1, true);
            MemberChangeRequestDTO addReq = null;
            for (MemberChangeRequestDTO req : pendingList) {
                if (req.getTopicId() == testTopicId && "ADD".equalsIgnoreCase(req.getActionType()) && req.getTargetUserId() == targetUserId) {
                    addReq = req;
                    break;
                }
            }

            if (addReq == null) throw new AssertionError("Add member request not found in pending queue");
            if (addReq.getRawMessage().contains("đồng chí")) throw new AssertionError("Notification MUST NOT contain 'đồng chí'!");
            if (!addReq.getRawMessage().contains("[ĐỀ XUẤT BỔ SUNG NHÂN SỰ]")) throw new AssertionError("Message missing [ĐỀ XUẤT BỔ SUNG NHÂN SỰ] tag");

            System.out.println("[PASS] Step 2: Reporter submitted ADD proposal. Notification verified (NO 'đồng chí'): " + addReq.getRawMessage());
            passed++;

            // STEP 3: Desk Editor APPROVES ADD request
            boolean approvedAdd = requestDAO.approveMemberChangeRequest(addReq.getNotificationId(), editorUserId, "Đồng ý bổ sung nhân sự theo đề xuất");
            if (!approvedAdd) throw new AssertionError("Desk Editor failed to approve add member request");

            // Verify targetUser is now in Topic_Member
            List<TopicMember> currentMembers = topicDAO.findMembersByTopicId(testTopicId);
            boolean inTeam = false;
            for (TopicMember m : currentMembers) {
                if (m.getUserId() == targetUserId) {
                    inTeam = true;
                    break;
                }
            }
            if (!inTeam) throw new AssertionError("Target user #4 was NOT inserted into Topic_Member!");

            // Verify notification sent to target user (MUST NOT contain "đồng chí", MUST contain [ĐIỀU ĐỘNG TÁC NGHIỆP])
            String notifTarget = getLatestNotificationForUser(targetUserId, testTopicId);
            if (notifTarget == null || !notifTarget.contains("[ĐIỀU ĐỘNG TÁC NGHIỆP]")) {
                throw new AssertionError("Target user did not receive [ĐIỀU ĐỘNG TÁC NGHIỆP] notification! Actual: " + notifTarget);
            }
            if (notifTarget.contains("đồng chí")) throw new AssertionError("Target notification MUST NOT contain 'đồng chí'!");

            System.out.println("[PASS] Step 3: Desk Editor approved ADD proposal. Target user added to crew and received: " + notifTarget);
            passed++;

            // STEP 4: Test REMOVE proposal with Fieldwork_Task reassignment upgrade
            // Create an in-progress Fieldwork_Task assigned to targetUserId
            int taskId = createTestFieldworkTask(testTopicId, targetUserId, "IN_PROGRESS", "Chụp 10 ảnh hiện trường sự kiện");
            System.out.println("       Created in-progress Fieldwork_Task #" + taskId + " for user #" + targetUserId);

            // Reporter submits proposal to REMOVE member (targetUserId = 4)
            boolean removeNotifSent = requestDAO.sendMemberChangeNotification(testTopicId, authorUserId, targetUserId, "REMOVE", "Hoàn thành phần việc chụp ảnh, cần điều chuyển");
            if (!removeNotifSent) throw new AssertionError("Failed to send remove member notification");

            List<MemberChangeRequestDTO> pendingList2 = requestDAO.getMemberChangeRequests(1, true);
            MemberChangeRequestDTO removeReq = null;
            for (MemberChangeRequestDTO req : pendingList2) {
                if (req.getTopicId() == testTopicId && "REMOVE".equalsIgnoreCase(req.getActionType()) && "PENDING".equalsIgnoreCase(req.getStatus())) {
                    removeReq = req;
                    break;
                }
            }

            if (removeReq == null) throw new AssertionError("Remove member request not found in pending queue");
            if (removeReq.getPendingTasksCount() != 1) {
                throw new AssertionError("Pending task count mismatch! Expected: 1, Actual: " + removeReq.getPendingTasksCount());
            }

            System.out.println("[PASS] Step 4: Reporter submitted REMOVE proposal. Pending task detected successfully: " + removeReq.getPendingTasksCount() + " task");
            passed++;

            // STEP 5: Desk Editor APPROVES REMOVE request
            boolean approvedRemove = requestDAO.approveMemberChangeRequest(removeReq.getNotificationId(), editorUserId, "Đồng ý điều chuyển theo kế hoạch");
            if (!approvedRemove) throw new AssertionError("Desk Editor failed to approve remove member request");

            // Verify user #4 removed from Topic_Member
            List<TopicMember> currentMembersAfterRemove = topicDAO.findMembersByTopicId(testTopicId);
            for (TopicMember m : currentMembersAfterRemove) {
                if (m.getUserId() == targetUserId) {
                    throw new AssertionError("User #4 is STILL in Topic_Member after approved remove!");
                }
            }

            // Verify Fieldwork_Task was re-assigned to authorUserId
            int reassignee = getTaskAssignee(taskId);
            if (reassignee != authorUserId) {
                throw new AssertionError("Fieldwork_Task was NOT re-assigned to author! Expected: " + authorUserId + ", Actual: " + reassignee);
            }

            // Verify notification sent to removed user (MUST NOT contain "đồng chí", MUST contain [ĐIỀU CHUYỂN NHÂN SỰ])
            String notifRemoved = getLatestNotificationForUser(targetUserId, testTopicId);
            if (notifRemoved == null || !notifRemoved.contains("[ĐIỀU CHUYỂN NHÂN SỰ]")) {
                throw new AssertionError("Removed user did not receive [ĐIỀU CHUYỂN NHÂN SỰ] notification! Actual: " + notifRemoved);
            }
            if (notifRemoved.contains("đồng chí")) throw new AssertionError("Removed user notification MUST NOT contain 'đồng chí'!");

            System.out.println("[PASS] Step 5: Approved REMOVE verified: Member removed from crew, task #" + taskId + " re-assigned to author #" + authorUserId + ", notification delivered (NO 'đồng chí'): " + notifRemoved);
            passed++;

            // STEP 6: Test REJECT Proposal
            boolean rejectNotifSent = requestDAO.sendMemberChangeNotification(testTopicId, authorUserId, targetUserId, "ADD", "Xin bổ sung lại");
            if (!rejectNotifSent) throw new AssertionError("Failed to send add request for rejection test");

            List<MemberChangeRequestDTO> pendingList3 = requestDAO.getMemberChangeRequests(1, true);
            MemberChangeRequestDTO rejectReq = null;
            for (MemberChangeRequestDTO req : pendingList3) {
                if (req.getTopicId() == testTopicId && "PENDING".equalsIgnoreCase(req.getStatus())) {
                    rejectReq = req;
                    break;
                }
            }

            if (rejectReq == null) throw new AssertionError("Pending request for rejection test not found");

            boolean rejected = requestDAO.rejectMemberChangeRequest(rejectReq.getNotificationId(), editorUserId, "Kíp tác nghiệp đã đủ định biên nhân sự");
            if (!rejected) throw new AssertionError("Desk Editor failed to reject request");

            // Verify targetUser was NOT added
            List<TopicMember> finalMembers = topicDAO.findMembersByTopicId(testTopicId);
            for (TopicMember m : finalMembers) {
                if (m.getUserId() == targetUserId) {
                    throw new AssertionError("User #4 was added despite rejection!");
                }
            }

            // Verify author received rejection notification with feedback (MUST NOT contain "đồng chí")
            String notifAuthorReject = getLatestNotificationForUser(authorUserId, testTopicId);
            if (notifAuthorReject == null || !notifAuthorReject.contains("[TỪ CHỐI ĐỀ XUẤT NHÂN SỰ]") || !notifAuthorReject.contains("Kíp tác nghiệp đã đủ định biên nhân sự")) {
                throw new AssertionError("Author did not receive proper rejection notification! Actual: " + notifAuthorReject);
            }
            if (notifAuthorReject.contains("đồng chí")) throw new AssertionError("Rejection notification MUST NOT contain 'đồng chí'!");

            System.out.println("[PASS] Step 6: Rejected proposal verified: Member was NOT added, Author received rejection feedback: " + notifAuthorReject);
            passed++;

        } catch (Throwable t) {
            System.err.println("[FAIL] Test failed with error: " + t.getMessage());
            t.printStackTrace();
            failed++;
        } finally {
            // CLEANUP
            if (testTopicId > 0) {
                cleanupTestTopic(testTopicId);
                System.out.println("[CLEANUP] Deleted test topic #" + testTopicId + " and associated test records.");
            }
        }

        System.out.println(BANNER);
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED.");
        System.out.println(BANNER);

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static int createTestFieldworkTask(int topicId, int assigneeUserId, String status, String description) {
        String sql = "INSERT INTO Fieldwork_Task (topic_id, assignee_user_id, task_status, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, topicId);
            ps.setInt(2, assigneeUserId);
            ps.setString(3, status);
            ps.setString(4, description);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getTaskAssignee(int taskId) {
        String sql = "SELECT assignee_user_id FROM Fieldwork_Task WHERE task_id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String getLatestNotificationForUser(int userId, int topicId) {
        String sql = "SELECT TOP 1 message FROM Notification WHERE user_id = ? AND topic_id = ? ORDER BY notification_id DESC";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("message");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void cleanupTestTopic(int topicId) {
        try (Connection conn = new DBContext().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Fieldwork_Task WHERE topic_id = ?")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic_Member WHERE topic_id = ?")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Notification WHERE topic_id = ?")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Audit_Log WHERE entity_id = ? AND entity_type = 'TOPIC'")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic WHERE topic_id = ?")) {
                ps.setInt(1, topicId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
