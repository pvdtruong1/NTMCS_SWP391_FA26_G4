package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO extends DBContext {

    private Category mapCategory(ResultSet rs) throws Exception {
        Category c = new Category();
        c.setCategoryId(rs.getInt("category_id"));
        c.setCategoryName(rs.getString("category_name"));
        int parentId = rs.getInt("parent_category_id");
        if (rs.wasNull()) {
            c.setParentCategoryId(null);
        } else {
            c.setParentCategoryId(parentId);
        }
        c.setDeskId(rs.getInt("desk_id"));
        return c;
    }

    /** Every category in one flat list, grouped by desk. The servlet arranges it as a tree. */
    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, parent_category_id, desk_id "
                + "FROM Category ORDER BY desk_id, category_name";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Category getById(int categoryId) {
        String sql = "SELECT category_id, category_name, parent_category_id, desk_id "
                + "FROM Category WHERE category_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Is there already a category with this name under the same parent? */
    public boolean siblingNameExists(String name, Integer parentId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM Category WHERE LOWER(category_name) = LOWER(?) "
                + "AND category_id <> ? AND ";
        sql += (parentId == null) ? "parent_category_id IS NULL" : "parent_category_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            if (parentId != null) {
                ps.setInt(3, parentId);
            }
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

    /** How many topics use this category (a category with topics cannot be deleted). */
    public int countTopics(int categoryId) {
        String sql = "SELECT COUNT(*) FROM Topic WHERE category_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
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

    /** Returns the new category_id, or 0 if saving failed. */
    public int insert(Category c) {
        String sql = "INSERT INTO Category (category_name, parent_category_id, desk_id) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCategoryName());
            if (c.getParentCategoryId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, c.getParentCategoryId());
            }
            ps.setInt(3, c.getDeskId());
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

    public boolean update(Category c) {
        String sql = "UPDATE Category SET category_name = ?, parent_category_id = ?, desk_id = ? "
                + "WHERE category_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCategoryName());
            if (c.getParentCategoryId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, c.getParentCategoryId());
            }
            ps.setInt(3, c.getDeskId());
            ps.setInt(4, c.getCategoryId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int categoryId) {
        String sql = "DELETE FROM Category WHERE category_id = ?";
        try (Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
