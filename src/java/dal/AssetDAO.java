package dal;

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
 * Data Access Object for Asset management.
 */
public class AssetDAO extends DBContext {
    private static final Logger LOGGER = Logger.getLogger(AssetDAO.class.getName());

    public List<Asset> getAvailableAssets() {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT asset_id, asset_name, asset_status, asset_code, asset_category, serial_number, description "
                   + "FROM Asset WHERE asset_status = 'AVAILABLE' OR asset_status = 'ACTIVE' "
                   + "ORDER BY asset_category, asset_name";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Asset a = new Asset();
                a.setAssetId(rs.getInt("asset_id"));
                a.setAssetName(rs.getString("asset_name"));
                a.setAssetStatus(rs.getString("asset_status"));
                a.setAssetCode(rs.getString("asset_code"));
                a.setAssetCategory(rs.getString("asset_category"));
                a.setSerialNumber(rs.getString("serial_number"));
                a.setDescription(rs.getString("description"));
                list.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching available assets", e);
        }
        return list;
    }

    public List<Asset> getAll() {
        List<Asset> list = new ArrayList<>();
        String sql = "SELECT asset_id, asset_name, asset_status, asset_code, asset_category, serial_number, description "
                   + "FROM Asset ORDER BY asset_id";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Asset a = new Asset();
                a.setAssetId(rs.getInt("asset_id"));
                a.setAssetName(rs.getString("asset_name"));
                a.setAssetStatus(rs.getString("asset_status"));
                a.setAssetCode(rs.getString("asset_code"));
                a.setAssetCategory(rs.getString("asset_category"));
                a.setSerialNumber(rs.getString("serial_number"));
                a.setDescription(rs.getString("description"));
                list.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all assets", e);
        }
        return list;
    }
}
