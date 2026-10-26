package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Topic;

/**
 * Truy vấn và quản lý đề tài của phóng viên trong NTMCS Workflow 1.
 */
public class TopicRepository {

    private static final Logger LOGGER
            = Logger.getLogger(TopicRepository.class.getName());

    private static final TopicRepository INSTANCE
            = new TopicRepository();

    public static final String[] CATEGORIES = {
        "Chính trị - Ngoại giao",
        "Đô thị & Dân sinh",
        "Tài chính - Ngân hàng",
        "Doanh nghiệp & Thị trường",
        "Điều tra tiêu cực",
        "Pháp đình & Trật tự an ninh"
    };

    public static final String[] GENRES = {
        "Tin nhanh",
        "Phỏng vấn",
        "Phóng sự / Feature",
        "Điều tra",
        "Infographic / Ảnh"
    };

    public static final String[] PRIORITIES = {
        "NORMAL", "HOT", "URGENT"
    };

    private TopicRepository() {
    }

    public static TopicRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Câu SELECT dùng chung cho các chức năng lấy đề tài.
     */
    private String baseSelect() {
        return "SELECT t.topic_id, t.topic_code, t.title, "
                + "t.summary, t.angle, t.genre, t.priority, "
                + "t.status, t.deadline, t.created_date, "
                + "t.rejection_reason, c.category_name, "
                + "d.desk_name, u.email, u.full_name "
                + "FROM Topic t "
                + "JOIN Category c ON t.category_id = c.category_id "
                + "JOIN Desk d ON c.desk_id = d.desk_id "
                + "JOIN [User] u ON t.author_user_id = u.user_id ";
    }

    /**
     * Lấy danh sách đề tài của một phóng viên.
     */
    public List<Topic> findByReporter(String email) {
        validateEmail(email);

        String sql = baseSelect()
                + "WHERE u.email = ? ORDER BY t.topic_id DESC";

        List<Topic> topics = new ArrayList<>();

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(mapRow(rs));
                }
            }

            return topics;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể truy vấn danh sách đề tài.", e);

            throw new IllegalStateException(
                    "Không thể truy vấn danh sách đề tài từ SQL Server.", e);
        }
    }

    /**
     * Tìm đề tài theo ID và email của phóng viên sở hữu.
     */
    public Topic findById(int id, String email) {
        validateEmail(email);

        String sql = baseSelect()
                + "WHERE t.topic_id = ? AND u.email = ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, email.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể tìm đề tài theo ID.", e);

            throw new IllegalStateException(
                    "Không thể tìm đề tài từ SQL Server.", e);
        }
    }

    /**
     * Tìm đề tài theo ID.
     */
    public Topic findById(int id) {
        String sql = baseSelect() + "WHERE t.topic_id = ?";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể tìm đề tài theo ID.", e);

            throw new IllegalStateException(
                    "Không thể tìm đề tài từ SQL Server.", e);
        }
    }

    /**
     * Tạo mới hoặc cập nhật đề tài.
     */
    public Topic save(Topic topic) {
        if (topic == null) {
            throw new IllegalArgumentException(
                    "Topic không được null.");
        }

        if (topic.getTitle() == null
                || topic.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Tiêu đề đề tài không được để trống.");
        }

        if (topic.getCategory() == null
                || topic.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Phải chọn chuyên mục hợp lệ.");
        }

        validateEmail(topic.getReporterEmail());

        String categorySql
                = "SELECT category_id FROM Category WHERE category_name = ?";

        String userSql
                = "SELECT user_id FROM [User] WHERE email = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int categoryId = findRequiredId(
                        conn,
                        categorySql,
                        topic.getCategory().trim(),
                        "Không tìm thấy chuyên mục: "
                        + topic.getCategory());

                int authorId = findRequiredId(
                        conn,
                        userSql,
                        topic.getReporterEmail().trim(),
                        "Không tìm thấy tài khoản phóng viên: "
                        + topic.getReporterEmail());

                if (topic.getTopicId() == 0) {
                    String sql
                            = "INSERT INTO Topic "
                            + "(title, summary, angle, genre, priority, "
                            + "status, deadline, category_id, "
                            + "author_user_id, rejection_reason) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps
                            = conn.prepareStatement(
                                    sql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, topic.getTitle().trim());
                        setNullableString(ps, 2, topic.getSummary());
                        setNullableString(ps, 3, topic.getAngle());
                        setNullableString(ps, 4, topic.getGenre());

                        ps.setString(
                                5,
                                topic.getPriority() == null
                                || topic.getPriority().trim().isEmpty()
                                ? "NORMAL"
                                : topic.getPriority().trim());

                        ps.setString(
                                6,
                                topic.getStatus() == null
                                || topic.getStatus().trim().isEmpty()
                                ? "DRAFT"
                                : topic.getStatus().trim());

                        setNullableDate(ps, 7, topic.getDeadline());
                        ps.setInt(8, categoryId);
                        ps.setInt(9, authorId);
                        setNullableString(ps, 10, topic.getRejectionReason());

                        ps.executeUpdate();

                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                topic.setTopicId(keys.getInt(1));
                            }
                        }
                    }

                } else {
                    String sql
                            = "UPDATE Topic SET "
                            + "title = ?, summary = ?, angle = ?, "
                            + "genre = ?, priority = ?, status = ?, "
                            + "deadline = ?, category_id = ?, "
                            + "rejection_reason = ? "
                            + "WHERE topic_id = ? AND author_user_id = ?";

                    try (PreparedStatement ps
                            = conn.prepareStatement(sql)) {

                        ps.setString(1, topic.getTitle().trim());
                        setNullableString(ps, 2, topic.getSummary());
                        setNullableString(ps, 3, topic.getAngle());
                        setNullableString(ps, 4, topic.getGenre());

                        ps.setString(
                                5,
                                topic.getPriority() == null
                                || topic.getPriority().trim().isEmpty()
                                ? "NORMAL"
                                : topic.getPriority().trim());

                        ps.setString(
                                6,
                                topic.getStatus() == null
                                || topic.getStatus().trim().isEmpty()
                                ? "DRAFT"
                                : topic.getStatus().trim());

                        setNullableDate(ps, 7, topic.getDeadline());
                        ps.setInt(8, categoryId);
                        setNullableString(ps, 9, topic.getRejectionReason());
                        ps.setInt(10, topic.getTopicId());
                        ps.setInt(11, authorId);

                        if (ps.executeUpdate() == 0) {
                            throw new SQLException(
                                    "Không tìm thấy đề tài hoặc "
                                    + "phóng viên không có quyền cập nhật.");
                        }
                    }
                }

                conn.commit();
                return topic;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể lưu đề tài vào SQL Server.", e);

            throw new IllegalStateException(
                    "Không thể lưu đề tài: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy ID bắt buộc từ database.
     */
    private int findRequiredId(
            Connection conn,
            String sql,
            String value,
            String error) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(error);
    }

    /**
     * Gán chuỗi, cho phép giá trị null hoặc rỗng.
     */
    private void setNullableString(
            PreparedStatement ps,
            int index,
            String value) throws SQLException {

        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.NVARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    /**
     * Gán ngày hạn cho câu SQL.
     */
    private void setNullableDate(
            PreparedStatement ps,
            int index,
            java.time.LocalDate value) throws SQLException {

        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    /**
     * Kiểm tra email đầu vào.
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Email phóng viên không được để trống.");
        }
    }

    /**
     * Xóa bản nháp của chính phóng viên.
     */
    public boolean delete(int id, String email) {
        validateEmail(email);

        String sql
                = "DELETE FROM Topic "
                + "WHERE topic_id = ? AND status = 'DRAFT' "
                + "AND author_user_id = "
                + "(SELECT user_id FROM [User] WHERE email = ?)";

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, email.trim());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể xóa bản nháp.", e);

            throw new IllegalStateException(
                    "Không thể xóa bản nháp từ SQL Server.", e);
        }
    }

    /**
     * Tìm đề tài tương tự theo tiêu đề, tóm tắt, góc khai thác hoặc chuyên mục.
     */
    public List<Topic> searchSimilar(String query, String email) {
        String q = query == null ? "" : query.trim();

        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        validateEmail(email);

        String sql = baseSelect()
                + "WHERE (t.title LIKE ? "
                + "OR t.summary LIKE ? "
                + "OR t.angle LIKE ? "
                + "OR c.category_name LIKE ?) "
                + "ORDER BY t.topic_id DESC";

        List<Topic> topics = new ArrayList<>();

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + q + "%";

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(mapRow(rs));
                }
            }

            return topics;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể tìm kiếm đề tài tương tự.", e);

            throw new IllegalStateException(
                    "Không thể tìm kiếm đề tài từ SQL Server.", e);
        }
    }

    /**
     * Không tạo hoặc reset dữ liệu trong Java.
     */
    @Deprecated
    public synchronized void initSeedData() {
        // Dữ liệu seed được quản lý bằng script SQL.
    }

    /**
     * Chuyển một dòng ResultSet thành đối tượng Topic.
     */
    private Topic mapRow(ResultSet rs) throws SQLException {
        Topic topic = new Topic();

        topic.setTopicId(rs.getInt("topic_id"));
        topic.setTopicCode(rs.getString("topic_code"));
        topic.setTitle(rs.getString("title"));
        topic.setSummary(rs.getString("summary"));
        topic.setAngle(rs.getString("angle"));
        topic.setGenre(rs.getString("genre"));
        topic.setPriority(rs.getString("priority"));

        Date deadline = rs.getDate("deadline");
        topic.setDeadline(
                deadline == null ? null : deadline.toLocalDate());

        java.sql.Timestamp createdDate
                = rs.getTimestamp("created_date");

        topic.setCreatedDate(
                createdDate == null
                        ? null
                        : createdDate.toLocalDateTime().toLocalDate());

        topic.setCategory(rs.getString("category_name"));
        topic.setDesk(rs.getString("desk_name"));
        topic.setStatus(rs.getString("status"));
        topic.setReporterEmail(rs.getString("email"));
        topic.setReporterName(rs.getString("full_name"));
        topic.setRejectionReason(rs.getString("rejection_reason"));

        return topic;
    }

    public List<String> findExistingPriorities() {
        String sql = "SELECT DISTINCT priority "
                + "FROM Topic "
                + "WHERE priority IS NOT NULL "
                + "ORDER BY priority";

        List<String> priorities = new ArrayList<>();

        try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                priorities.add(rs.getString("priority"));
            }

            return priorities;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Không thể lấy danh sách mức độ ưu tiên.", e);

            throw new IllegalStateException(
                    "Không thể lấy mức độ ưu tiên từ database.", e);
        }
    }
}
