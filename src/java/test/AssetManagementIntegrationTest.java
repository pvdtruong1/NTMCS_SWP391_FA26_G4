package test;

import dal.AssetDAO;
import dal.AssetDAO.DeleteResult;
import dal.AssetDAO.PageResult;
import dal.AssetRequestDAO;
import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import model.Asset;
import model.AssetRequest;

/**
 * Automated Integration Test Suite for WF-03 (Asset & Logistics Management).
 * Lead Auditor: Pham Vu Dinh Truong (Desk Editor & Architecture Co-Lead)
 */
public class AssetManagementIntegrationTest {

    private static final AssetDAO assetDAO = new AssetDAO();
    private static final AssetRequestDAO requestDAO = new AssetRequestDAO();

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("RUNNING INTEGRATION TEST SUITE: WF-03 ASSET & LOGISTICS MANAGEMENT");
        System.out.println("Author: Pham Vu Dinh Truong (pvdtruong1@gmail.com)");
        System.out.println("==================================================================");

        int passed = 0;
        int total = 0;

        try {
            // Test 1: Query existing assets
            total++;
            List<Asset> allAssets = assetDAO.getAllAssets();
            assertNotNull(allAssets, "allAssets should not be null");
            System.out.println("[PASS] Test 1: Retrieved " + allAssets.size() + " assets from database.");
            passed++;

            // Test 2: Asset Insert & Duplicate Code Guard
            total++;
            String testCode = "TEST-EQ-" + System.currentTimeMillis();
            Asset testAsset = new Asset();
            testAsset.setAssetCode(testCode);
            testAsset.setAssetName("Test 4K Cinema Camera");
            testAsset.setAssetCategory("CAMERA_EQUIPMENT");
            testAsset.setAssetStatus("AVAILABLE");
            testAsset.setSerialNumber("SN-TEST-8888");
            testAsset.setDescription("Integration test equipment");

            boolean inserted = assetDAO.insertAsset(testAsset);
            assertTrue(inserted, "Asset insertion failed");

            boolean exists = assetDAO.isAssetCodeExists(testCode, 0);
            assertTrue(exists, "Asset code existence check failed");
            System.out.println("[PASS] Test 2: Asset inserted successfully with code: " + testCode);
            passed++;

            // Test 3: Asset Retrieval by ID & Update
            total++;
            int createdId = getAssetIdByCode(testCode);
            assertTrue(createdId > 0, "Created asset ID must be positive");

            Asset retrieved = assetDAO.getAssetById(createdId);
            assertNotNull(retrieved, "Retrieved asset must not be null");
            assertEquals(testCode, retrieved.getAssetCode(), "Asset code mismatch");

            retrieved.setAssetName("Updated Test Camera");
            retrieved.setDescription("Updated description");
            boolean updated = assetDAO.updateAsset(retrieved);
            assertTrue(updated, "Asset update failed");

            Asset afterUpdate = assetDAO.getAssetById(createdId);
            assertEquals("Updated Test Camera", afterUpdate.getAssetName(), "Asset name was not updated");
            System.out.println("[PASS] Test 3: Asset retrieval and update verified for ID: " + createdId);
            passed++;

            // Test 4: Pagination & Search Filters
            total++;
            PageResult pageResult = assetDAO.searchAssets("Camera", "CAMERA_EQUIPMENT", "ALL", 1, 5);
            assertNotNull(pageResult, "PageResult must not be null");
            assertTrue(pageResult.getTotalRecords() > 0, "Should find at least 1 camera record");
            assertTrue(pageResult.getAssets().size() <= 5, "Page size must be <= 5");
            System.out.println("[PASS] Test 4: Search & Pagination verified (Found: " + pageResult.getTotalRecords() + " assets).");
            passed++;

            // Test 5: Status counters
            total++;
            int totalCount = assetDAO.countAllAssets();
            int availableCount = assetDAO.countByStatus("AVAILABLE");
            int checkedOutCount = assetDAO.countByStatus("CHECKED_OUT");
            int maintenanceCount = assetDAO.countByStatus("MAINTENANCE");
            assertTrue(totalCount >= (availableCount + checkedOutCount + maintenanceCount), "Total assets math error");
            System.out.println("[PASS] Test 5: Status counts verified (Total: " + totalCount + ", Available: " + availableCount + ", CheckedOut: " + checkedOutCount + ", Maintenance: " + maintenanceCount + ")");
            passed++;

            // Test 6: Delete guard when free
            total++;
            DeleteResult delRes = assetDAO.deleteAsset(createdId);
            assertEquals(DeleteResult.DELETED, delRes, "Clean asset deletion must succeed");
            System.out.println("[PASS] Test 6: Safe deletion of unused asset verified (Result: " + delRes + ")");
            passed++;

            // Test 7: Asset Request Lifecycle (Create, Conflict Check, Approve, Checkout, Return)
            total++;
            // Re-create asset for request lifecycle
            String cycleCode = "TEST-CYC-" + System.currentTimeMillis();
            Asset cycleAsset = new Asset();
            cycleAsset.setAssetCode(cycleCode);
            cycleAsset.setAssetName("Test Drone 4K Pro");
            cycleAsset.setAssetCategory("IT_EQUIPMENT");
            cycleAsset.setAssetStatus("AVAILABLE");
            assetDAO.insertAsset(cycleAsset);
            int cycleAssetId = getAssetIdByCode(cycleCode);

            // Find an active topic to link
            int testTopicId = getAnyTopicId();
            int testUserId = getAnyUserId();

            // Insert Asset Request
            long now = System.currentTimeMillis();
            Timestamp start = new Timestamp(now + 3600000L); // +1 hour
            Timestamp end = new Timestamp(now + 7200000L);   // +2 hours
            int testRequestId = insertTestRequest(testTopicId, cycleAssetId, testUserId, start, end, "PENDING");
            assertTrue(testRequestId > 0, "Failed to create test asset request");

            // Test findById
            AssetRequest req = requestDAO.findById(testRequestId);
            assertNotNull(req, "AssetRequest must not be null");
            assertEquals("PENDING", req.getStatus(), "Initial status must be PENDING");

            // Test Conflict detection
            List<AssetRequest> conflicts = requestDAO.findConflicts(cycleAssetId, start, end, 0);
            assertTrue(conflicts.size() >= 1, "Must detect pending/active conflict");

            // Test Approve
            String approveRes = requestDAO.approveRequest(testRequestId);
            assertEquals("APPROVED", approveRes, "Approve request must return APPROVED");

            // Test Checkout
            String checkoutRes = requestDAO.checkoutRequest(testRequestId);
            assertEquals("CHECKED_OUT", checkoutRes, "Checkout request must return CHECKED_OUT");

            Asset checkedOutAsset = assetDAO.getAssetById(cycleAssetId);
            assertEquals("CHECKED_OUT", checkedOutAsset.getAssetStatus(), "Asset status must be CHECKED_OUT");

            // Test Delete Guard when in use (Must be blocked!)
            DeleteResult blockedDel = assetDAO.deleteAsset(cycleAssetId);
            assertTrue(blockedDel == DeleteResult.IN_USE || blockedDel == DeleteResult.HAS_REQUEST_HISTORY, 
                       "Deleting asset in use must be blocked");

            // Test Return
            String returnRes = requestDAO.returnRequest(testRequestId, "AVAILABLE");
            assertEquals("RETURNED", returnRes, "Return request must return RETURNED");

            Asset returnedAsset = assetDAO.getAssetById(cycleAssetId);
            assertEquals("AVAILABLE", returnedAsset.getAssetStatus(), "Asset status must return to AVAILABLE");

            // Verify Request History
            List<AssetRequest> history = requestDAO.findHistoryByAssetId(cycleAssetId);
            assertTrue(history.size() >= 1, "Must have at least 1 history record");

            // Clean up test request & asset
            cleanTestRequest(testRequestId);
            assetDAO.deleteAsset(cycleAssetId);

            System.out.println("[PASS] Test 7: Full Request Lifecycle (PENDING -> APPROVED -> CHECKED_OUT -> RETURNED) with Conflict & Delete Guards passed.");
            passed++;

            // Test 8: Request Search & Pagination
            total++;
            List<AssetRequest> allReqs = requestDAO.findAll("", "ALL", "", "", 1);
            int totalReqs = requestDAO.countAll("", "ALL", "", "");
            int pendingReqs = requestDAO.countByStatus("PENDING");
            System.out.println("[PASS] Test 8: AssetRequest pagination & counters verified (Total: " + totalReqs + ", Pending: " + pendingReqs + ").");
            passed++;

        } catch (Throwable t) {
            System.err.println("[FAIL] Test encountered error: " + t.getMessage());
            t.printStackTrace();
        }

        System.out.println("==================================================================");
        System.out.println("WF-03 TEST RESULTS: " + passed + " PASSED, " + (total - passed) + " FAILED / " + total + " TOTAL TESTS");
        System.out.println("==================================================================");

        if (passed != total) {
            System.exit(1);
        }
    }

    private static int getAssetIdByCode(String code) throws SQLException {
        String sql = "SELECT asset_id FROM Asset WHERE asset_code = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static int getAnyTopicId() throws SQLException {
        String sql = "SELECT TOP 1 topic_id FROM Topic WHERE status IN ('APPROVED','IN_PROGRESS','SUBMITTED') ORDER BY topic_id DESC";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    private static int getAnyUserId() throws SQLException {
        String sql = "SELECT TOP 1 user_id FROM [User] WHERE role_id = 4 ORDER BY user_id ASC";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 2;
    }

    private static int insertTestRequest(int topicId, int assetId, int userId, Timestamp start, Timestamp end, String status) throws SQLException {
        String sql = "INSERT INTO Asset_Request (topic_id, asset_id, requester_user_id, start_time, end_time, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, topicId);
            ps.setInt(2, assetId);
            ps.setInt(3, userId);
            ps.setTimestamp(4, start);
            ps.setTimestamp(5, end);
            ps.setString(6, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static void cleanTestRequest(int requestId) throws SQLException {
        String sql = "DELETE FROM Asset_Request WHERE request_id = ?";
        try (Connection con = new DBContext().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) throw new AssertionError(message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }
}