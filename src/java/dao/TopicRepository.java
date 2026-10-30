package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Topic;
import model.TopicEstimate;
import model.TopicMember;
import model.User;

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
     * Tạo mới hoặc cập nhật đề tài (không kèm dự toán và nhân sự).
     */
    public Topic save(Topic topic) {
        return save(topic, null, null);
    }

    /**
     * Tạo mới hoặc cập nhật đề tài kèm dự toán kinh phí và thành viên tổ tác nghiệp.
     */
    public Topic save(Topic topic, List<TopicEstimate> estimates, List<TopicMember> members) {
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

                // Cập nhật Topic_Estimate nếu danh sách được truyền vào
                if (estimates != null) {
                    String delEst = "DELETE FROM Topic_Estimate WHERE topic_id = ?";
                    try (PreparedStatement psDel = conn.prepareStatement(delEst)) {
                        psDel.setInt(1, topic.getTopicId());
                        psDel.executeUpdate();
                    }

                    String insEst = "INSERT INTO Topic_Estimate (topic_id, cost_item, amount) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insEst)) {
                        for (TopicEstimate est : estimates) {
                            if (est == null || est.getCostItem() == null || est.getCostItem().trim().isEmpty()) {
                                continue;
                            }
                            psIns.setInt(1, topic.getTopicId());
                            psIns.setString(2, est.getCostItem().trim());
                            psIns.setBigDecimal(3, est.getAmount() != null ? est.getAmount() : BigDecimal.ZERO);
                            psIns.executeUpdate();
                        }
                    }
                }

                // Cập nhật Topic_Member nếu danh sách được truyền vào
                if (members != null) {
                    String delMem = "DELETE FROM Topic_Member WHERE topic_id = ?";
                    try (PreparedStatement psDel = conn.prepareStatement(delMem)) {
                        psDel.setInt(1, topic.getTopicId());
                        psDel.executeUpdate();
                    }

                    String insMem = "INSERT INTO Topic_Member (topic_id, user_id, role_in_team) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insMem)) {
                        Set<Integer> addedUsers = new HashSet<>();
                        for (TopicMember mem : members) {
                            if (mem == null || mem.getUserId() <= 0 || addedUsers.contains(mem.getUserId())) {
                                continue;
                            }
                            String role = mem.getRoleInTeam();
                            if (role == null || role.trim().isEmpty()) {
                                role = "ASSISTANT";
                            } else {
                                role = role.trim();
                            }
                            psIns.setInt(1, topic.getTopicId());
                            psIns.setInt(2, mem.getUserId());
                            psIns.setString(3, role);
                            psIns.executeUpdate();
                            addedUsers.add(mem.getUserId());
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

    /**
     * Lấy danh sách dự toán kinh phí tác nghiệp của đề tài.
     */
    public List<TopicEstimate> findEstimatesByTopicId(int topicId) {
        List<TopicEstimate> list = new ArrayList<>();
        if (topicId <= 0) {
            return list;
        }

        String sql = "SELECT estimate_id, topic_id, cost_item, amount "
                   + "FROM Topic_Estimate "
                   + "WHERE topic_id = ? "
                   + "ORDER BY estimate_id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopicEstimate est = new TopicEstimate();
                    est.setEstimateId(rs.getInt("estimate_id"));
                    est.setTopicId(rs.getInt("topic_id"));
                    est.setCostItem(rs.getString("cost_item"));
                    est.setAmount(rs.getBigDecimal("amount"));
                    list.add(est);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể lấy dự toán chi phí cho topic " + topicId, e);
        }
        return list;
    }

    /**
     * Lấy danh sách thành viên tổ tác nghiệp của đề tài.
     */
    public List<TopicMember> findMembersByTopicId(int topicId) {
        List<TopicMember> list = new ArrayList<>();
        if (topicId <= 0) {
            return list;
        }

        String sql = "SELECT tm.member_id, tm.topic_id, tm.user_id, tm.role_in_team, "
                   + "u.full_name, u.email, r.role_name "
                   + "FROM Topic_Member tm "
                   + "JOIN [User] u ON tm.user_id = u.user_id "
                   + "JOIN Role r ON u.role_id = r.role_id "
                   + "WHERE tm.topic_id = ? "
                   + "ORDER BY tm.member_id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopicMember tm = new TopicMember();
                    tm.setMemberId(rs.getInt("member_id"));
                    tm.setTopicId(rs.getInt("topic_id"));
                    tm.setUserId(rs.getInt("user_id"));
                    tm.setRoleInTeam(rs.getString("role_in_team"));
                    tm.setUserName(rs.getString("full_name"));
                    tm.setUserEmail(rs.getString("email"));
                    tm.setUserRole(rs.getString("role_name"));
                    list.add(tm);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể lấy danh sách thành viên cho topic " + topicId, e);
        }
        return list;
    }

    /**
     * Lấy tất cả người dùng đang hoạt động trong hệ thống để thêm vào tổ tác nghiệp.
     */
    public List<User> findAllActiveUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, u.email, u.role_id, r.role_name, u.active_status, u.desk_id, d.desk_name "
                   + "FROM [User] u "
                   + "JOIN Role r ON u.role_id = r.role_id "
                   + "LEFT JOIN Desk d ON u.desk_id = d.desk_id "
                   + "WHERE u.active_status = 1 "
                   + "ORDER BY u.user_id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRoleId(rs.getInt("role_id"));
                u.setRoleName(rs.getString("role_name"));
                u.setActiveStatus(rs.getBoolean("active_status"));
                int deskId = rs.getInt("desk_id");
                if (!rs.wasNull()) {
                    u.setDeskId(deskId);
                }
                u.setDeskName(rs.getString("desk_name"));
                list.add(u);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể lấy danh sách nhân sự.", e);
        }
        return list;
    }

    /**
     * Lấy danh sách lịch bận của tất cả nhân sự (các đề tài đang chạy, nộp thẩm định hoặc giao việc).
     * @param excludeTopicId đề tài đang được chỉnh sửa (để không tự tính là bận với chính nó)
     * @return Map mapping userId -> List các đề tài đang bận
     */
    public Map<Integer, List<String>> getUsersBusySchedule(int excludeTopicId) {
        Map<Integer, List<String>> map = new HashMap<>();

        String sql = "SELECT DISTINCT tm_all.user_id, t.topic_id, t.topic_code, t.title, t.status "
                   + "FROM ( "
                   + "    SELECT author_user_id AS user_id, topic_id FROM Topic "
                   + "    UNION "
                   + "    SELECT user_id, topic_id FROM Topic_Member "
                   + "    UNION "
                   + "    SELECT assignee_user_id AS user_id, topic_id FROM Fieldwork_Task WHERE task_status IN ('TODO', 'IN_PROGRESS') "
                   + ") tm_all "
                   + "JOIN Topic t ON tm_all.topic_id = t.topic_id "
                   + "WHERE t.status NOT IN ('DRAFT', 'REJECTED', 'ABORTED', 'ACCEPTED_READY_FOR_CMS') "
                   + "  AND (? <= 0 OR t.topic_id <> ?) "
                   + "ORDER BY tm_all.user_id, t.topic_id ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, excludeTopicId);
            ps.setInt(2, excludeTopicId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("user_id");
                    int tid = rs.getInt("topic_id");
                    String code = rs.getString("topic_code");
                    String title = rs.getString("title");
                    String label = (code != null && !code.trim().isEmpty())
                            ? (code.trim() + " (" + title + ")")
                            : ("#" + tid + " (" + title + ")");
                    map.computeIfAbsent(uid, k -> new ArrayList<>()).add(label);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Không thể lấy danh sách lịch bận của nhân sự.", e);
        }
        return map;
    }

    /**
     * Lấy lịch bận của 1 nhân sự cụ thể.
     */
    public List<String> getUserBusyTopics(int userId, int excludeTopicId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        Map<Integer, List<String>> map = getUsersBusySchedule(excludeTopicId);
        return map.getOrDefault(userId, Collections.emptyList());
    }
}

