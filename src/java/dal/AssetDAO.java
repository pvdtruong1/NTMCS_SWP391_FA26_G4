package dal;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Asset;

/**
 * Data Access Object for Asset management (WF-03).
 * Extends DBContext to inherit unified connection pooling.
 */
public class AssetDAO extends DBContext {

    private static final Logger LOGGER = Logger.getLogger(AssetDAO.class.getName());

    public static class PageResult implements Serializable {
        private final List<Asset> assets;
        private final int currentPage;
        private final int totalPages;
        private final int totalRecords;

        public PageResult(List<Asset> assets, int currentPage, int totalPages, int totalRecords) {
            this.assets = assets != null ? assets : new ArrayList<>();
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalRecords = totalRecords;
        }

        public List<Asset> getAssets() {
            return assets;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getTotalRecords() {
            return totalRecords;
        }
    }

    public enum DeleteResult {
        DELETED,
        NOT_FOUND,
        IN_USE,
        HAS_REQUEST_HISTORY,
        ERROR
    }

    private static final String SELECT_COLUMNS =
        "asset_id, asset_code, asset_name, asset_category, " +
        "asset_status, serial_number, description";

    private Asset mapAsset(ResultSet rs) throws SQLException {
        Asset asset = new Asset();
        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetCode(rs.getString("asset_code"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setAssetCategory(rs.getString("asset_category"));
        asset.setAssetStatus(rs.getString("asset_status"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setDescription(rs.getString("description"));
        return asset;
    }

    public List<Asset> getAvailableAssets() {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS
                   + " FROM Asset WHERE asset_status = 'AVAILABLE' OR asset_status = 'ACTIVE' "
                   + "ORDER BY asset_category, asset_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAsset(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching available assets", e);
        }
        return list;
    }

    public List<Asset> getAll() {
        return getAllAssets();
    }

    public List<Asset> getAllAssets() {
        List<Asset> assets = new ArrayList<>();
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Asset ORDER BY asset_id DESC";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                assets.add(mapAsset(rs));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all assets", ex);
        }
        return assets;
    }

    public PageResult searchAssets(
            String keyword,
            String category,
            String status,
            int page,
            int pageSize) throws SQLException {

        int totalRecords = countAssets(keyword, category, status);
        int safePageSize = Math.max(1, Math.min(100, pageSize));
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRecords / safePageSize));
        int safePage = Math.max(1, Math.min(page, totalPages));

        List<Asset> assets = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT " + SELECT_COLUMNS + " FROM Asset WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                " AND (asset_code LIKE ? OR asset_name LIKE ? " +
                "OR serial_number LIKE ? OR description LIKE ?) "
            );
            String q = "%" + keyword.trim() + "%";
            for (int i = 0; i < 4; i++) {
                params.add(q);
            }
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append(" AND asset_category = ? ");
            params.add(category.trim());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND asset_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append(" ORDER BY asset_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((safePage - 1) * safePageSize);
        params.add(safePageSize);

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    assets.add(mapAsset(rs));
                }
            }
        }

        return new PageResult(assets, safePage, totalPages, totalRecords);
    }

    public int countAssets(
            String keyword,
            String category,
            String status) throws SQLException {

        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM Asset WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                " AND (asset_code LIKE ? OR asset_name LIKE ? " +
                "OR serial_number LIKE ? OR description LIKE ?) "
            );
            String q = "%" + keyword.trim() + "%";
            for (int i = 0; i < 4; i++) {
                params.add(q);
            }
        }

        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append(" AND asset_category = ? ");
            params.add(category.trim());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND asset_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Asset WHERE asset_status = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countAllAssets() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Asset";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public Asset getAssetById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS + " FROM Asset WHERE asset_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAsset(rs);
                }
            }
        }
        return null;
    }

    public boolean isAssetCodeExists(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Asset WHERE asset_code = ? AND asset_id <> ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean hasActiveRequest(int assetId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Asset_Request "
                   + "WHERE asset_id = ? AND status IN ('PENDING', 'APPROVED', 'CHECKED_OUT')";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean insertAsset(Asset asset) throws SQLException {
        String sql =
            "INSERT INTO Asset " +
            "(asset_code, asset_name, asset_category, asset_status, " +
            "serial_number, description) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, asset.getAssetCode());
            ps.setString(2, asset.getAssetName());
            ps.setString(3, asset.getAssetCategory());
            ps.setString(4, asset.getAssetStatus());
            ps.setString(5, asset.getSerialNumber());
            ps.setString(6, asset.getDescription());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateAsset(Asset asset) throws SQLException {
        String sql =
            "UPDATE Asset SET " +
            "asset_code = ?, asset_name = ?, asset_category = ?, " +
            "asset_status = ?, serial_number = ?, description = ? " +
            "WHERE asset_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, asset.getAssetCode());
            ps.setString(2, asset.getAssetName());
            ps.setString(3, asset.getAssetCategory());
            ps.setString(4, asset.getAssetStatus());
            ps.setString(5, asset.getSerialNumber());
            ps.setString(6, asset.getDescription());
            ps.setInt(7, asset.getAssetId());

            return ps.executeUpdate() == 1;
        }
    }

    public DeleteResult deleteAsset(int id) throws SQLException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                String checkSql =
                    "SELECT asset_status FROM Asset " +
                    "WITH (UPDLOCK, HOLDLOCK) WHERE asset_id = ?";

                String status;

                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return DeleteResult.NOT_FOUND;
                        }

                        status = rs.getString("asset_status");
                    }
                }

                if ("CHECKED_OUT".equals(status)) {
                    con.rollback();
                    return DeleteResult.IN_USE;
                }

                String requestSql =
                    "SELECT COUNT(*) FROM Asset_Request " +
                    "WITH (UPDLOCK, HOLDLOCK) WHERE asset_id = ?";

                try (PreparedStatement ps = con.prepareStatement(requestSql)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            con.rollback();
                            return DeleteResult.HAS_REQUEST_HISTORY;
                        }
                    }
                }

                String deleteSql =
                    "DELETE FROM Asset WHERE asset_id = ? " +
                    "AND asset_status <> 'CHECKED_OUT'";

                try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                    ps.setInt(1, id);

                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return DeleteResult.ERROR;
                    }
                }

                con.commit();
                return DeleteResult.DELETED;

            } catch (SQLException ex) {
                con.rollback();

                if (ex.getErrorCode() == 547) {
                    return DeleteResult.HAS_REQUEST_HISTORY;
                }

                throw ex;

            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}