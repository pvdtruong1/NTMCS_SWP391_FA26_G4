package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.DeskQuota;

public class DeskQuotaDAO extends DBContext {

    // Every query starts with this, so one method (mapQuota) can read every row.
    private static final String BASE_SELECT
            = "SELECT q.quota_id, q.desk_id, d.desk_name, q.period, q.quota_count "
            + "FROM Desk_Quota q JOIN Desk d ON q.desk_id = d.desk_id ";

    private DeskQuota mapQuota(ResultSet rs) throws Exception {
        DeskQuota q = new DeskQuota();
        q.setQuotaId(rs.getInt("quota_id"));
        q.setDeskId(rs.getInt("desk_id"));
        q.setDeskName(rs.getString("desk_name"));
        q.setPeriod(rs.getString("period"));
        q.setQuotaCount(rs.getInt("quota_count"));
        return q;
    }

    public List<DeskQuota> getAll() {
        List<DeskQuota> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY q.period DESC, d.desk_name";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapQuota(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public DeskQuota getById(int quotaId) {
        String sql = BASE_SELECT + "WHERE q.quota_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quotaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuota(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** The quota of one desk in one period, or null if there is none. */
    public DeskQuota find(int deskId, String period) {
        String sql = BASE_SELECT + "WHERE q.desk_id = ? AND q.period = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deskId);
            ps.setString(2, period);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuota(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Returns the new quota_id, or 0 if saving failed. */
    public int insert(DeskQuota q) {
        String sql = "INSERT INTO Desk_Quota (desk_id, period, quota_count) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getDeskId());
            ps.setString(2, q.getPeriod());
            ps.setInt(3, q.getQuotaCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateCount(int quotaId, int quotaCount) {
        String sql = "UPDATE Desk_Quota SET quota_count = ? WHERE quota_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quotaCount);
            ps.setInt(2, quotaId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int quotaId) {
        String sql = "DELETE FROM Desk_Quota WHERE quota_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quotaId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
