package test;

import dal.TopicDAO;
import model.Category;
import model.Topic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated test suite covering operational logic and DAL integrity for SCR-2.4: Directive Topic Creation Form.
 */
public class DirectiveTopicTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING AUTOMATED TEST SUITE: UC SCR-2.4 CREATE DIRECTIVE TOPIC");
        System.out.println("==================================================================");

        int passed = 0;
        int failed = 0;

        TopicDAO dao = new TopicDAO();

        // Test 1: Category retrieval by Desk
        try {
            List<Category> categories = dao.getCategoriesByDesk(1);
            if (categories != null && categories.size() >= 2) {
                System.out.println("[PASS] Test 1: Retrieved " + categories.size() + " categories for Desk 1.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 1: Expected >= 2 categories for Desk 1, got " + (categories != null ? categories.size() : "null"));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 1: Exception - " + e.getMessage());
            failed++;
        }

        // Test 2: Create Directive Topic Draft (DRAFT status, topic_code = NULL)
        int createdTopicId = 0;
        try {
            LocalDate deadline = LocalDate.now().plusDays(7);
            Topic draft = new Topic(
                    0,
                    null,
                    "Vệt bài điều tra trọng điểm: Thực trạng vi phạm quy hoạch đất công ven đô",
                    "Điều tra hồ sơ pháp lý giao đất, tiếp cận trực địa và thu thập chứng cứ chứng nhận quyền sử dụng đất.",
                    "DRAFT",
                    Date.valueOf(deadline),
                    1, // Category: Chính trị - Ngoại giao
                    1  // Author: Trường (Desk Editor)
            );

            createdTopicId = dao.createDirectiveTopicDraft(draft);
            if (createdTopicId > 0) {
                Topic retrieved = dao.getTopicById(createdTopicId);
                if (retrieved != null && "DRAFT".equals(retrieved.getStatus()) && retrieved.getTopicCode() == null &&
                    retrieved.getTitle().contains("Thực trạng vi phạm quy hoạch đất công")) {
                    System.out.println("[PASS] Test 2: Created directive draft successfully (Topic ID: " + createdTopicId + ", Status: DRAFT, Code: NULL).");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 2: Topic retrieved does not match expected draft state.");
                    failed++;
                }
            } else {
                System.err.println("[FAIL] Test 2: Failed to generate topic ID for directive draft.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 2: Exception - " + e.getMessage());
            failed++;
        }

        // Test 3: Update Directive Topic Draft
        try {
            if (createdTopicId > 0) {
                Topic existing = dao.getTopicById(createdTopicId);
                existing.setTitle("Vệt bài điều tra cập nhật: Thực trạng vi phạm đất công và trách nhiệm quản lý");
                existing.setAngle("Bổ sung đối thoại cùng Sở Xây dựng và đại diện chính quyền cơ sở.");
                LocalDate newDeadline = LocalDate.now().plusDays(10);
                existing.setDeadline(Date.valueOf(newDeadline));

                boolean updated = dao.updateDirectiveTopicDraft(existing);
                Topic reloaded = dao.getTopicById(createdTopicId);

                if (updated && reloaded != null && reloaded.getTitle().contains("trách nhiệm quản lý") &&
                    reloaded.getAngle().contains("Sở Xây dựng")) {
                    System.out.println("[PASS] Test 3: Updated directive draft successfully.");
                    passed++;
                } else {
                    System.err.println("[FAIL] Test 3: Failed to update directive draft in database.");
                    failed++;
                }
            } else {
                System.err.println("[FAIL] Test 3: Skipped due to previous creation failure.");
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 3: Exception - " + e.getMessage());
            failed++;
        }

        // Test 4: Desk Quota Retrieval
        try {
            int quota = dao.getDeskQuota(1, "2026-10");
            if (quota == 15) {
                System.out.println("[PASS] Test 4: Retrieved Desk Quota for Desk 1 in 2026-10: " + quota + " topics.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 4: Expected quota=15, got " + quota);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 4: Exception - " + e.getMessage());
            failed++;
        }

        // Test 5: Desk Production Count
        try {
            int prodCount = dao.getDeskProductionCount(1);
            if (prodCount >= 0) {
                System.out.println("[PASS] Test 5: Retrieved Desk Production Count for Desk 1: " + prodCount + " active/approved topics.");
                passed++;
            } else {
                System.err.println("[FAIL] Test 5: Invalid negative production count: " + prodCount);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 5: Exception - " + e.getMessage());
            failed++;
        }

        // Test 6: Clean-up Draft to Maintain Test Isolation
        try {
            if (createdTopicId > 0) {
                Connection conn = dao.getConnection();
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Topic WHERE topic_id = ? AND status = 'DRAFT';")) {
                    ps.setInt(1, createdTopicId);
                    int deleted = ps.executeUpdate();
                    if (deleted > 0) {
                        System.out.println("[PASS] Test 6: Cleaned up test directive draft #" + createdTopicId + " (Test isolation preserved).");
                        passed++;
                    } else {
                        System.err.println("[FAIL] Test 6: Could not delete draft #" + createdTopicId);
                        failed++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Test 6: Exception - " + e.getMessage());
            failed++;
        }

        System.out.println("==================================================================");
        System.out.println("TEST SUITE RESULTS: " + passed + " PASSED, " + failed + " FAILED / " + (passed + failed) + " TOTAL TESTS");
        System.out.println("==================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
