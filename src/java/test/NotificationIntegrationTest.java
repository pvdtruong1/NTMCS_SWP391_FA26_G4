package test;

import dal.NotificationDAO;
import model.Notification;

import java.util.List;

/**
 * Automated test suite for In-App Notification System (SCR-1.x / WF-01 / WF-02 / WF-04 / WF-05).
 */
public class NotificationIntegrationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING TEST SUITE: IN-APP NOTIFICATION SYSTEM INTEGRITY");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        NotificationDAO notifDAO = new NotificationDAO();
        int testUserId = 2; // Mai Khánh Hùng (Reporter)

        // Test 1: Count unread notifications
        try {
            int unread = notifDAO.countUnreadByUserId(testUserId);
            System.out.println("[PASS] Test 1: Counted unread notifications for user #" + testUserId + ": " + unread);
            passed++;
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Create a new notification
        int createdNotifId = 0;
        try {
            boolean created = notifDAO.createNotification(testUserId, 1, "[TEST] Thông báo thử nghiệm hệ thống tòa soạn");
            if (created) {
                System.out.println("[PASS] Test 2: Created new notification for user #" + testUserId);
                passed++;
            } else {
                System.err.println("[FAIL] Test 2: Could not create notification.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Retrieve notifications and find created one
        try {
            List<Notification> list = notifDAO.findByUserId(testUserId);
            if (list != null && !list.isEmpty()) {
                Notification latest = list.get(0);
                createdNotifId = latest.getNotificationId();
                System.out.println("[PASS] Test 3: Retrieved " + list.size() + " notifications. Latest ID: " + createdNotifId + ", Message: " + latest.getMessage());
                passed++;
            } else {
                System.err.println("[FAIL] Test 3: Notification list is empty.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Mark specific notification as read
        try {
            if (createdNotifId > 0) {
                boolean marked = notifDAO.markAsRead(createdNotifId, testUserId);
                if (marked) {
                    System.out.println("[PASS] Test 4: Marked notification #" + createdNotifId + " as read.");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 4: markAsRead returned false.");
                    failed++;
                }
            } else {
                System.err.println("[FAIL] Test 4: Skipped due to missing notification ID.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Mark all as read
        try {
            boolean allRead = notifDAO.markAllAsRead(testUserId);
            int unreadAfter = notifDAO.countUnreadByUserId(testUserId);
            if (unreadAfter == 0) {
                System.out.println("[PASS] Test 5: All notifications marked as read. Unread count is now: " + unreadAfter);
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Expected unread count 0, got " + unreadAfter);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
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
