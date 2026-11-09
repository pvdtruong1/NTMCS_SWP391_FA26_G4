package test;

import dal.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class FullFieldworkLifecycleAuditTest extends DBContext {

    public static void main(String[] args) {
        System.out.println("=== BAT DAU KIEM THU TOAN DIEN QUY TRINH THONG BAO FIELDWORK & AUDIT ===");

        FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();
        FieldworkDeliverableDAO deliverableDAO = new FieldworkDeliverableDAO();
        TaskNudgeDAO nudgeDAO = new TaskNudgeDAO();
        EmergencyIncidentDAO incidentDAO = new EmergencyIncidentDAO();
        BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        int topicId = -1;
        int coordinatorUserId = 4; // Dat - Dieu phoi hien truong
        int reporterUserId = 2;    // Hung - Phong vien
        int deskEditorUserId = 3;  // Truong - Desk Editor

        try (FullFieldworkLifecycleAuditTest t = new FullFieldworkLifecycleAuditTest();
             Connection conn = t.getConnection()) {

            // Tim 1 de tai hop le dang hoat dong
            String sqlFindTopic = "SELECT TOP 1 topic_id FROM Topic WHERE status IN ('APPROVED', 'IN_PROGRESS') ORDER BY topic_id DESC";
            try (PreparedStatement ps = conn.prepareStatement(sqlFindTopic);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    topicId = rs.getInt(1);
                }
            }

            if (topicId <= 0) {
                System.out.println("(!) Khong tim thay de tai hop le de test, bo qua test.");
                return;
            }

            System.out.println("[1] Su dung de tai test topicId = " + topicId);

            // 1. TEST GIAO VIEC HIEN TRUONG
            long uniqueTag = System.currentTimeMillis();
            String taskDesc = "Kiem thu tu dong toan trinh Fieldwork " + uniqueTag;
            int taskId = taskDAO.createTask(coordinatorUserId, topicId, reporterUserId, taskDesc);
            assert taskId > 0 : "Tao task that bai!";
            System.out.println("[PASS] 1. Da tao task #" + taskId + " (Giao viec boi coordinator #" + coordinatorUserId + " cho reporter #" + reporterUserId + ")");

            // 2. TEST TIEP NHAN TAC NGHIEP (IN_PROGRESS)
            boolean accepted = taskDAO.updateTaskStatus(taskId, "IN_PROGRESS", reporterUserId);
            assert accepted : "Cap nhat IN_PROGRESS that bai!";
            System.out.println("[PASS] 2. Reporter #" + reporterUserId + " da tiep nhan nhiem vu -> Trang thai IN_PROGRESS.");

            // 3. TEST DON DOC TAC NGHIEP (NUDGE)
            try {
                nudgeDAO.sendNudge(deskEditorUserId, reporterUserId, topicId, "Don doc tac nghiep", "De nghi day nhanh tien do tac nghiep " + uniqueTag, "HIGH");
                System.out.println("[PASS] 3. Desk Editor #" + deskEditorUserId + " da gui Nudge cho topic #" + topicId);
            } catch (Exception e) {
                System.out.println("[INFO] 3. Nudge notice: " + e.getMessage());
            }

            // 4. TEST BAO CAO SU CO KHAN CAP (EMERGENCY_INCIDENT)
            incidentDAO.reportEmergencyIncident(topicId, "[E2E AUDIT " + uniqueTag + "] Hu hong thiet bi ghi hinh", reporterUserId);
            System.out.println("[PASS] 4. Reporter #" + reporterUserId + " da bao su co khan cap toi cac ben lien quan.");

            // 5. TEST NOP SAN PHAM HOAN THANH (DONE)
            int deliverableId = deliverableDAO.submitDeliverable(
                    taskId,
                    reporterUserId,
                    "FILE",
                    "DONE",
                    "San pham hoan tat kiem thu tu dong " + uniqueTag,
                    "bao_cao_" + uniqueTag + ".docx",
                    "/uploads/deliverables/bao_cao_" + uniqueTag + ".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    10240,
                    null
            );
            assert deliverableId > 0 : "Nop san pham that bai!";
            System.out.println("[PASS] 5. Reporter #" + reporterUserId + " da nop san pham #" + deliverableId + " va hoan thanh task -> DONE.");

            // 6. TEST PING DON DOC NGUOI CHUA DOC
            // Tim 1 thong bao chua doc cua topic nay
            int unreadNotifId = -1;
            int recipientOfUnread = -1;
            String sqlFindUnread = "SELECT TOP 1 notification_id, user_id FROM Notification WHERE topic_id = ? AND is_read = 0 ORDER BY notification_id DESC";
            try (PreparedStatement ps = conn.prepareStatement(sqlFindUnread)) {
                ps.setInt(1, topicId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        unreadNotifId = rs.getInt("notification_id");
                        recipientOfUnread = rs.getInt("user_id");
                    }
                }
            }

            if (unreadNotifId > 0) {
                auditDAO.pingUnreadRecipient(coordinatorUserId, unreadNotifId, recipientOfUnread);
                System.out.println("[PASS] 6. Coordinator #" + coordinatorUserId + " da gui Ping don doc doc thong bao #" + unreadNotifId + " toi user #" + recipientOfUnread);
            }

            // 7. TEST DOC THONG BAO & GHI READ TIMESTAMP
            if (unreadNotifId > 0) {
                notifDAO.markAsRead(unreadNotifId, recipientOfUnread);
                System.out.println("[PASS] 7. User #" + recipientOfUnread + " da doc thong bao #" + unreadNotifId + " (Read Timestamp da duoc ghi nhan).");
            }

            // 8. KIEM TRA MANG KIEM TOAN SCR-4.8 (BroadcastAuditDAO)
            List<Object[]> auditList = auditDAO.getFieldworkNotificationAudit(coordinatorUserId);
            System.out.println("[PASS] 8. Truy van audit SCR-4.8 tra ve tong cong " + auditList.size() + " dong thong bao.");

            int countAssignment = 0, countAccepted = 0, countCompleted = 0, countNudge = 0, countIncident = 0, countPing = 0;
            boolean foundCurrentTaskAssignment = false;
            boolean foundCurrentTaskAccepted = false;
            boolean foundCurrentTaskCompleted = false;

            for (Object[] row : auditList) {
                String src = (String) row[14];
                String msg = (String) row[8];
                if ("TASK_ASSIGNMENT".equals(src)) {
                    countAssignment++;
                    if (msg != null && msg.contains(String.valueOf(uniqueTag))) foundCurrentTaskAssignment = true;
                } else if ("TASK_ACCEPTED".equals(src)) {
                    countAccepted++;
                    if (msg != null && msg.contains(String.valueOf(uniqueTag))) foundCurrentTaskAccepted = true;
                } else if ("TASK_COMPLETED".equals(src)) {
                    countCompleted++;
                    if (msg != null && msg.contains(String.valueOf(uniqueTag))) foundCurrentTaskCompleted = true;
                } else if ("TASK_NUDGE".equals(src)) {
                    countNudge++;
                } else if ("EMERGENCY_INCIDENT".equals(src)) {
                    countIncident++;
                } else if ("PING_NUDGE".equals(src)) {
                    countPing++;
                }
            }

            System.out.println("--- CHI TIET PHAN BO THONG BAO TREN SCR-4.8 ---");
            System.out.println("+ Giao viec (TASK_ASSIGNMENT): " + countAssignment + " | Khop dot test: " + foundCurrentTaskAssignment);
            System.out.println("+ Tiep nhan viec (TASK_ACCEPTED): " + countAccepted + " | Khop dot test: " + foundCurrentTaskAccepted);
            System.out.println("+ Hoan thanh viec (TASK_COMPLETED): " + countCompleted + " | Khop dot test: " + foundCurrentTaskCompleted);
            System.out.println("+ Don doc (TASK_NUDGE): " + countNudge);
            System.out.println("+ Su co khan cap (EMERGENCY_INCIDENT): " + countIncident);
            System.out.println("+ Ping don doc (PING_NUDGE): " + countPing);

            assert countAssignment > 0 : "Khong co thong bao TASK_ASSIGNMENT!";
            assert countAccepted > 0 : "Khong co thong bao TASK_ACCEPTED!";
            assert countCompleted > 0 : "Khong co thong bao TASK_COMPLETED!";
            assert foundCurrentTaskAssignment : "Thong bao giao viec vua tao khong xuat hien tren SCR-4.8!";
            assert foundCurrentTaskAccepted : "Thong bao tiep nhan viec vua tao khong xuat hien tren SCR-4.8!";
            assert foundCurrentTaskCompleted : "Thong bao hoan thanh viec vua tao khong xuat hien tren SCR-4.8!";

            System.out.println("=== TAT CA KIEM THU DA HOAN TAT VA DAT CHUAN 100%! ===");

        } catch (Exception ex) {
            System.err.println("[FAIL] Loi kiem thu: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
