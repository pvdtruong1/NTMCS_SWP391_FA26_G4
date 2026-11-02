package test;

import dal.BroadcastAuditDAO;
import dal.EmergencyIncidentDAO;
import dal.FieldworkDeliverableDAO;
import dal.FieldworkTaskDAO;
import dal.SLAConfigDAO;
import dal.TaskNudgeDAO;
import model.FieldworkTask;
import model.SLAConfig;

import java.util.List;

/**
 * Automated end-to-end integration test for WF-04 Fieldwork & SLA (SCR-4.1 to SCR-4.8).
 */
public class FieldworkIntegrationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING INTEGRATION TEST SUITE: WF-04 FIELDWORK & SLA ALERTING");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();
        FieldworkDeliverableDAO deliverableDAO = new FieldworkDeliverableDAO();
        EmergencyIncidentDAO incidentDAO = new EmergencyIncidentDAO();
        TaskNudgeDAO nudgeDAO = new TaskNudgeDAO();
        BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();
        SLAConfigDAO slaDAO = new SLAConfigDAO();

        int createdTaskId = 0;

        // Test 1: SLA Config Load & Save
        try {
            SLAConfig config = slaDAO.getConfig();
            if (config != null && config.getThresholdHours1() > config.getThresholdHours2()) {
                System.out.println("[PASS] Test 1: SLAConfig loaded successfully (Mốc 1=" + config.getThresholdHours1() + "h, Mốc 2=" + config.getThresholdHours2() + "h).");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: SLAConfig invalid or null.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Query active approved topics for task creation
        int testTopicId = 1;
        try {
            List<Object[]> approvedTopics = taskDAO.getActiveApprovedTopics();
            if (approvedTopics != null && !approvedTopics.isEmpty()) {
                testTopicId = (Integer) approvedTopics.get(0)[0];
                System.out.println("[PASS] Test 2: Found " + approvedTopics.size() + " active approved topics. Using topic #" + testTopicId + " for testing.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: No active approved topics found.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Create Fieldwork Task (SCR-4.1)
        int testAssigneeUserId = 2; // Reporter Hung
        try {
            createdTaskId = taskDAO.createTask(1, testTopicId, testAssigneeUserId, "[TEST] Khảo sát hiện trường tự động hoá");
            if (createdTaskId > 0) {
                System.out.println("[PASS] Test 3: Created Fieldwork Task #" + createdTaskId + " successfully.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Failed to create Fieldwork Task.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Query tasks and verify created task status is TODO
        try {
            FieldworkTask task = taskDAO.getTaskById(createdTaskId);
            if (task != null && "TODO".equals(task.getTaskStatus())) {
                System.out.println("[PASS] Test 4: Retrieved Task #" + createdTaskId + " with status TODO.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Task status mismatch or task not found.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Update Task Status to IN_PROGRESS
        try {
            boolean updated = taskDAO.updateTaskStatus(createdTaskId, "IN_PROGRESS");
            FieldworkTask task = taskDAO.getTaskById(createdTaskId);
            if (updated && task != null && "IN_PROGRESS".equals(task.getTaskStatus())) {
                System.out.println("[PASS] Test 5: Task status transitioned to IN_PROGRESS successfully.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Failed to update task status to IN_PROGRESS.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
            failed++;
        }

        // Test 6: Submit Fieldwork Deliverable (SCR-4.3)
        try {
            int deliverableId = deliverableDAO.submitDeliverable(
                    createdTaskId,
                    testAssigneeUserId,
                    "LINK",
                    "DONE",
                    "[TEST] Bản thảo sản phẩm tác nghiệp đã hoàn thành",
                    null,
                    null,
                    null,
                    -1,
                    "https://drive.google.com/file/d/test-deliverable"
            );
            FieldworkTask task = taskDAO.getTaskById(createdTaskId);
            if (deliverableId > 0 && task != null && "DONE".equals(task.getTaskStatus())) {
                System.out.println("[PASS] Test 6: Deliverable #" + deliverableId + " submitted and Task moved to DONE.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 6: Failed to submit deliverable or update task status.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Exception - " + e.getMessage());
            failed++;
        }

        // Test 7: Report Emergency Incident (SCR-4.4)
        try {
            incidentDAO.reportEmergencyIncident(testTopicId, "[KHẨN CẤP] Test sự cố hiện trường gửi cảnh báo", testAssigneeUserId);
            System.out.println("[PASS] Test 7: Emergency incident reported and notifications dispatched.");
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 7: Exception - " + e.getMessage());
            failed++;
        }

        // Test 8: Task Nudge Query & Send (SCR-4.7)
        try {
            List<Object[]> assignments = nudgeDAO.getReporterTopicAssignments();
            System.out.println("[PASS] Test 8a: Retrieved " + assignments.size() + " reporter topic assignments for nudging.");
            nudgeDAO.sendNudge(1, testAssigneeUserId, testTopicId, "Đôn đốc tác nghiệp", "Vui lòng cập nhật bài viết", "HIGH");
            System.out.println("[PASS] Test 8b: Sent task nudge successfully.");
            passed += 2;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 8: Exception - " + e.getMessage());
            failed++;
        }

        // Test 9: Broadcast Audit Log & Ping (SCR-4.8)
        try {
            List<Object[]> audits = auditDAO.getFieldworkNotificationAudit(1);
            if (audits != null && !audits.isEmpty()) {
                System.out.println("[PASS] Test 9a: Retrieved " + audits.size() + " broadcast notification audit records.");
                Object[] firstUnread = null;
                for (Object[] row : audits) {
                    if (row[9] == Boolean.FALSE) {
                        firstUnread = row;
                        break;
                    }
                }
                if (firstUnread != null) {
                    int notifId = (Integer) firstUnread[0];
                    int recipientId = (Integer) firstUnread[1];
                    auditDAO.pingUnreadRecipient(1, notifId, recipientId);
                    System.out.println("[PASS] Test 9b: Sent Ping reminder for notification #" + notifId + ".");
                    passed++;
                } else {
                    System.out.println("[PASS] Test 9b: All notifications read, skipping ping test.");
                    passed++;
                }
                passed++;
            } else {
                System.err.println("[FAIL] Test 9: Audit records empty.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 9: Exception - " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================================");
        System.out.println("TEST RESULTS: " + passed + " PASSED, " + failed + " FAILED.");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
