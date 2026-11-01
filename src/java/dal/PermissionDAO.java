package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Permission;

public class PermissionDAO extends DBContext {

    public List<Permission> getAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT permission_id, permission_code, description FROM Permission ORDER BY permission_id";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Permission(rs.getInt("permission_id"),
                        rs.getString("permission_code"),
                        rs.getString("description")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Every ticked box of the matrix, as text like "7-3" (roleId-permissionId).
     * The JSP uses it to know which checkboxes to tick.
     */
    public Set<String> getAllPairs() {
        Set<String> pairs = new HashSet<>();
        String sql = "SELECT role_id, permission_id FROM Role_Permission";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pairs.add(rs.getInt("role_id") + "-" + rs.getInt("permission_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pairs;
    }

    /** All permission codes of one role, e.g. USER_MANAGE. Used to build the Home page and menu. */
    public Set<String> getCodesByRole(int roleId) {
        Set<String> codes = new HashSet<>();
        String sql = "SELECT p.permission_code FROM Role_Permission rp "
                + "JOIN Permission p ON rp.permission_id = p.permission_id "
                + "WHERE rp.role_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    codes.add(rs.getString("permission_code"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codes;
    }

    /** Does this role have this permission? Used to protect every screen. */
    public boolean roleHasPermission(int roleId, String permissionCode) {
        String sql = "SELECT COUNT(*) FROM Role_Permission rp "
                + "JOIN Permission p ON rp.permission_id = p.permission_id "
                + "WHERE rp.role_id = ? AND p.permission_code = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            ps.setString(2, permissionCode);
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
     * Save the whole matrix: delete the old rows, insert the ticked ones.
     * Done in one transaction, so either everything is saved or nothing is.
     * The locked role (System Administrator) is never changed.
     */
    public boolean saveMatrix(String[] pairs, int lockedRoleId) {
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);   // start transaction

            PreparedStatement del = con.prepareStatement(
                    "DELETE FROM Role_Permission WHERE role_id <> ?");
            del.setInt(1, lockedRoleId);
            del.executeUpdate();

            PreparedStatement ins = con.prepareStatement(
                    "INSERT INTO Role_Permission (role_id, permission_id) VALUES (?, ?)");
            if (pairs != null) {
                for (String pair : pairs) {
                    String[] parts = pair.split("-");
                    int roleId = Integer.parseInt(parts[0]);
                    int permissionId = Integer.parseInt(parts[1]);
                    if (roleId == lockedRoleId) {
                        continue;
                    }
                    ins.setInt(1, roleId);
                    ins.setInt(2, permissionId);
                    ins.executeUpdate();
                }
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
