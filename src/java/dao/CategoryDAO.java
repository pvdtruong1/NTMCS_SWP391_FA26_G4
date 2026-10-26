package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Category;

/**
 * DAO truy vấn danh mục trực tiếp từ bảng Category trong SQL Server.
 */
public class CategoryDAO {
    private static final Logger LOGGER = Logger.getLogger(CategoryDAO.class.getName());

    public List<Category> getAll() {
        List<Category> list = new ArrayList<Category>();
        String sql = "SELECT category_id, category_name, parent_category_id, desk_id FROM Category ORDER BY category_id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(rs.getInt("category_id"));
                c.setCategoryName(rs.getString("category_name"));
                int parent = rs.getInt("parent_category_id");
                if (!rs.wasNull()) {
                    c.setParentCategoryId(parent);
                }
                c.setDeskId(rs.getInt("desk_id"));
                list.add(c);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn Category từ SQL Server: " + e.getMessage(), e);
        }
        return list;
    }

    public Category findById(int id) {
        String sql = "SELECT category_id, category_name, parent_category_id, desk_id FROM Category WHERE category_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setCategoryId(rs.getInt("category_id"));
                    c.setCategoryName(rs.getString("category_name"));
                    int parent = rs.getInt("parent_category_id");
                    if (!rs.wasNull()) {
                        c.setParentCategoryId(parent);
                    }
                    c.setDeskId(rs.getInt("desk_id"));
                    return c;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm Category theo ID: " + e.getMessage(), e);
        }
        return null;
    }
}
