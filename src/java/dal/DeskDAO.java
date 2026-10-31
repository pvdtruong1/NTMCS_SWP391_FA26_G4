package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Desk;

public class DeskDAO extends DBContext {

    public List<Desk> getAll() {
        List<Desk> list = new ArrayList<>();
        String sql = "SELECT desk_id, desk_name FROM Desk ORDER BY desk_name";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Desk(rs.getInt("desk_id"), rs.getString("desk_name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Desk getById(int deskId) {
        String sql = "SELECT desk_id, desk_name FROM Desk WHERE desk_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Desk(rs.getInt("desk_id"), rs.getString("desk_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Is this name already used by another desk? (excludeId = the desk being edited, or 0) */
    public boolean nameExists(String name, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Desk WHERE LOWER(desk_name) = LOWER(?) AND desk_id <> ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
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

    /** Returns the new desk_id, or 0 if saving failed. */
    public int insert(String deskName) {
        String sql = "INSERT INTO Desk (desk_name) VALUES (?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, deskName);
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

    public boolean updateName(int deskId, String deskName) {
        String sql = "UPDATE Desk SET desk_name = ? WHERE desk_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, deskName);
            ps.setInt(2, deskId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int deskId) {
        String sql = "DELETE FROM Desk WHERE desk_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, deskId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // How much still depends on a desk (a desk with any of these cannot be deleted)
    public int countCategories(int deskId) {
        return count("SELECT COUNT(*) FROM Category WHERE desk_id = ?", deskId);
    }

    public int countUsers(int deskId) {
        return count("SELECT COUNT(*) FROM [User] WHERE desk_id = ?", deskId);
    }

    public int countQuotas(int deskId) {
        return count("SELECT COUNT(*) FROM Desk_Quota WHERE desk_id = ?", deskId);
    }

    private int count(String sql, int id) {
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
