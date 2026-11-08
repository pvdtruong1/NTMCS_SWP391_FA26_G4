package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.AuditLog;

public class AuditLogDAO extends DBContext {

    /**
     * UC-12 Log Admin Action.
     * Call this after every successful admin change.
     * entityId can be null when the action is not about one row.
     */
    public void log(int actorUserId, String action, String entityType,
            Integer entityId, String details) {
        String sql = "INSERT INTO Audit_Log (actor_user_id, action, entity_type, entity_id, details) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, actorUserId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, entityId);
            }
            ps.setString(5, details);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * UC-08 View System Audit Log.
     * Newest 200 rows. If keyword is not empty, only rows that contain it.
     */
    public List<AuditLog> getRecent(String keyword) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT TOP 200 a.log_id, a.actor_user_id, u.full_name, a.action, "
                + "a.entity_type, a.entity_id, a.details, a.created_at "
                + "FROM Audit_Log a JOIN [User] u ON a.actor_user_id = u.user_id ";
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        if (hasKeyword) {
            sql += "WHERE a.action LIKE ? OR a.entity_type LIKE ? "
                    + "OR a.details LIKE ? OR u.full_name LIKE ? ";
        }
        sql += "ORDER BY a.created_at DESC, a.log_id DESC";

        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasKeyword) {
                String like = "%" + keyword + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog a = new AuditLog();
                    a.setLogId(rs.getInt("log_id"));
                    a.setActorUserId(rs.getInt("actor_user_id"));
                    a.setActorName(rs.getString("full_name"));
                    a.setAction(rs.getString("action"));
                    a.setEntityType(rs.getString("entity_type"));
                    int entityId = rs.getInt("entity_id");
                    if (rs.wasNull()) {
                        a.setEntityId(null);
                    } else {
                        a.setEntityId(entityId);
                    }
                    a.setDetails(rs.getString("details"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
