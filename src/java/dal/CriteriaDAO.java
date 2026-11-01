package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.CriteriaConfig;

public class CriteriaDAO extends DBContext {

    public List<CriteriaConfig> getAll() {
        List<CriteriaConfig> list = new ArrayList<>();
        String sql = "SELECT criteria_id, criteria_name, weight_percent FROM Criteria_Config ORDER BY criteria_id";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CriteriaConfig(rs.getInt("criteria_id"),
                        rs.getString("criteria_name"),
                        rs.getBigDecimal("weight_percent")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Has this criterion already been used to score a topic? (If so it cannot be deleted.) */
    public boolean isUsed(int criteriaId) {
        String sql = "SELECT COUNT(*) FROM Evaluation_Detail WHERE criteria_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, criteriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Save the whole matrix in ONE transaction: delete removed criteria,
     * update existing ones, insert new ones. Either everything is saved or nothing is.
     */
    public boolean saveAll(List<CriteriaConfig> updates, List<CriteriaConfig> inserts,
            List<Integer> deletes) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);   // start transaction

            PreparedStatement del = con.prepareStatement(
                    "DELETE FROM Criteria_Config WHERE criteria_id = ?");
            for (int id : deletes) {
                del.setInt(1, id);
                del.executeUpdate();
            }

            PreparedStatement upd = con.prepareStatement(
                    "UPDATE Criteria_Config SET criteria_name = ?, weight_percent = ? WHERE criteria_id = ?");
            for (CriteriaConfig c : updates) {
                upd.setString(1, c.getCriteriaName());
                upd.setBigDecimal(2, c.getWeightPercent());
                upd.setInt(3, c.getCriteriaId());
                upd.executeUpdate();
            }

            PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO Criteria_Config (criteria_name, weight_percent) VALUES (?, ?)");
            for (CriteriaConfig c : inserts) {
                ins.setString(1, c.getCriteriaName());
                ins.setBigDecimal(2, c.getWeightPercent());
                ins.executeUpdate();
            }

            con.commit();               // everything OK
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null) {
                    con.rollback();     // something failed: undo
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
