package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO extends DBContext {

    // Every query starts with this, so one method (mapUser) can read every row.
    private static final String BASE_SELECT
            = "SELECT u.user_id, u.full_name, u.email, u.password_hash, u.role_id, "
            + "u.active_status, u.desk_id, r.role_name, d.desk_name "
            + "FROM [User] u "
            + "JOIN Role r ON u.role_id = r.role_id "
            + "LEFT JOIN Desk d ON u.desk_id = d.desk_id ";

    private User mapUser(ResultSet rs) throws Exception {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setActiveStatus(rs.getBoolean("active_status"));
        int deskId = rs.getInt("desk_id");
        if (rs.wasNull()) {
            u.setDeskId(null);
        } else {
            u.setDeskId(deskId);
        }
        u.setRoleName(rs.getString("role_name"));
        u.setDeskName(rs.getString("desk_name"));
        return u;
    }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY u.user_id";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public User getById(int userId) {
        String sql = BASE_SELECT + "WHERE u.user_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getByEmail(String email) {
        String sql = BASE_SELECT + "WHERE u.email = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        return getByEmail(email) != null;
    }

    public boolean insert(User u) {
        String sql = "INSERT INTO [User] (full_name, email, password_hash, role_id, active_status, desk_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setInt(4, u.getRoleId());
            ps.setBoolean(5, u.isActiveStatus());
            if (u.getDeskId() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, u.getDeskId());
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Updates name, role and desk. (Email and password are not changed here.) */
    public boolean update(User u) {
        String sql = "UPDATE [User] SET full_name = ?, role_id = ?, desk_id = ? WHERE user_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setInt(2, u.getRoleId());
            if (u.getDeskId() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, u.getDeskId());
            }
            ps.setInt(4, u.getUserId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE [User] SET password_hash = ? WHERE user_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** active = true unlocks the account, false locks it. */
    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE [User] SET active_status = ? WHERE user_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
