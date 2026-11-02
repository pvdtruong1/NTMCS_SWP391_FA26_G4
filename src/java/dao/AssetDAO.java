package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Asset;
import util.DBConnection;

public class AssetDAO {

    public enum DeleteResult {
        DELETED,
        NOT_FOUND,
        IN_USE,
        HAS_REQUEST_HISTORY,
        ERROR
    }

    private static final String SELECT_COLUMNS =
        "asset_id, asset_code, asset_name, asset_category, " +
        "asset_status, serial_number, description, image_path";

    private Asset mapAsset(ResultSet rs) throws SQLException {
        Asset asset = new Asset();

        asset.setAssetId(rs.getInt("asset_id"));
        asset.setAssetCode(rs.getString("asset_code"));
        asset.setAssetName(rs.getString("asset_name"));
        asset.setAssetCategory(rs.getString("asset_category"));
        asset.setAssetStatus(rs.getString("asset_status"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setDescription(rs.getString("description"));
        asset.setImagePath(rs.getString("image_path"));

        return asset;
    }

    public List<Asset> getAllAssets() throws SQLException {
        List<Asset> assets = new ArrayList<>();

        String sql = "SELECT " + SELECT_COLUMNS +
                     " FROM dbo.Asset ORDER BY asset_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                assets.add(mapAsset(rs));
            }
        }

        return assets;
    }

    public Asset getAssetById(int id) throws SQLException {
        String sql = "SELECT " + SELECT_COLUMNS +
                     " FROM dbo.Asset WHERE asset_id = ?";

        try (Connection con = DBConnection.getConnection();
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

    public boolean isAssetCodeExists(String code, int excludeId)
            throws SQLException {

        String sql =
            "SELECT COUNT(*) FROM dbo.Asset " +
            "WHERE asset_code = ? AND asset_id <> ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean insertAsset(Asset asset) throws SQLException {
        String sql =
            "INSERT INTO dbo.Asset " +
            "(asset_code, asset_name, asset_category, asset_status, " +
            "serial_number, description, image_path) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, asset.getAssetCode());
            ps.setString(2, asset.getAssetName());
            ps.setString(3, asset.getAssetCategory());
            ps.setString(4, asset.getAssetStatus());
            ps.setString(5, asset.getSerialNumber());
            ps.setString(6, asset.getDescription());
            ps.setString(7, asset.getImagePath());

            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateAsset(Asset asset) throws SQLException {
        String sql =
            "UPDATE dbo.Asset SET " +
            "asset_code = ?, asset_name = ?, asset_category = ?, " +
            "asset_status = ?, serial_number = ?, description = ?, " +
            "image_path = ? " +
            "WHERE asset_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, asset.getAssetCode());
            ps.setString(2, asset.getAssetName());
            ps.setString(3, asset.getAssetCategory());
            ps.setString(4, asset.getAssetStatus());
            ps.setString(5, asset.getSerialNumber());
            ps.setString(6, asset.getDescription());
            ps.setString(7, asset.getImagePath());
            ps.setInt(8, asset.getAssetId());

            return ps.executeUpdate() == 1;
        }
    }

    public DeleteResult deleteAsset(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                String checkSql =
                    "SELECT asset_status FROM dbo.Asset " +
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

                /*
                 * Giữ an toàn lịch sử mượn/trả.
                 * Nếu bảng Asset_Request có bất kỳ bản ghi nào tham chiếu
                 * đến tài sản này, không xóa tài sản.
                 */
                String requestSql =
                    "SELECT COUNT(*) FROM dbo.Asset_Request " +
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
                    "DELETE FROM dbo.Asset WHERE asset_id = ? " +
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

                // SQL Server: 547 = vi phạm ràng buộc khóa ngoại.
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