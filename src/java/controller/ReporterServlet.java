package controller;

import dal.CategoryDAO;
import dal.NotificationDAO;
import dal.TopicDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Category;
import model.Notification;
import model.Topic;
import model.TopicEstimate;
import model.TopicMember;
import model.User;
import util.AuthUtil;

/**
 * Controller for Reporter Workflow (WF-01).
 * Secured by RBAC permission "TOPIC_PITCH" and dynamically bound to session user.
 */
@WebServlet(name = "ReporterServlet", urlPatterns = {"/reporter/*"})
public class ReporterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReporterServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!AuthUtil.check(request, response, "TOPIC_PITCH")) {
            return;
        }

        User loggedUser = AuthUtil.getLoggedUser(request);
        setUserAttributes(request, loggedUser);

        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/dashboard".equals(path)) {
            showDashboard(request, response, loggedUser);
        } else if ("/topics".equals(path)) {
            showTopics(request, response, loggedUser);
        } else if ("/duplicate-search".equals(path)) {
            searchDuplicates(request, response);
        } else if ("/compose".equals(path)) {
            showCompose(request, response, loggedUser);
        } else if ("/check-schedule".equals(path)) {
            handleCheckSchedule(request, response);
        } else if ("/detail".equals(path)) {
            showDetail(request, response, loggedUser);
        } else if ("/delete".equals(path)) {
            handleDelete(request, response, loggedUser);
        } else if ("/quick-submit".equals(path)) {
            handleQuickSubmit(request, response, loggedUser);
        } else if ("/notifications".equals(path)) {
            handleGetNotifications(request, response, loggedUser);
        } else if ("/reset".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/reporter/dashboard?message=resetDisabled");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!AuthUtil.check(request, response, "TOPIC_PITCH")) {
            return;
        }

        User loggedUser = AuthUtil.getLoggedUser(request);
        setUserAttributes(request, loggedUser);

        String path = request.getPathInfo();
        if ("/delete".equals(path)) {
            handleDelete(request, response, loggedUser);
            return;
        }
        if ("/quick-submit".equals(path)) {
            handleQuickSubmit(request, response, loggedUser);
            return;
        }
        if ("/notifications".equals(path)) {
            handlePostNotifications(request, response, loggedUser);
            return;
        }
        if ("/reset".equals(path)) {
            response.sendRedirect(request.getContextPath() + "/reporter/dashboard?message=resetDisabled");
            return;
        }
        if (!"/compose".equals(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String action = trim(request.getParameter("action"));
        if (!"saveDraft".equals(action) && !"submit".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String title = trim(request.getParameter("title"));
        String summary = trim(request.getParameter("summary"));
        String angle = trim(request.getParameter("angle"));
        String genre = trim(request.getParameter("genre"));
        String priority = trim(request.getParameter("priority"));
        String deadlineText = trim(request.getParameter("deadline"));

        String catParam = trim(request.getParameter("categoryId"));
        if (catParam.isEmpty()) {
            catParam = trim(request.getParameter("category"));
        }
        int categoryId = 0;
        try {
            categoryId = Integer.parseInt(catParam);
        } catch (NumberFormatException ignored) {}

        List<TopicEstimate> estimates = parseEstimatesFromRequest(request);
        List<TopicMember> members = parseMembersFromRequest(request);

        Topic topic;
        try {
            topic = requestTopic(request, loggedUser);
        } catch (IllegalArgumentException e) {
            Topic formTopic = createFormTopic();
            fillTopicFromForm(formTopic, title, summary, angle, categoryId, null,
                    genre, priority, deadlineText);
            forwardComposeWithError(request, response, e.getMessage(), formTopic, estimates, members);
            return;
        }

        String error = validate(title, summary, angle, categoryId, genre,
                priority, deadlineText, action);
        if (error != null) {
            fillTopicFromForm(topic, title, summary, angle, categoryId, null,
                    genre, priority, deadlineText);
            forwardComposeWithError(request, response, error, topic, estimates, members);
            return;
        }

        fillTopicFromForm(topic, title, summary, angle, categoryId, null,
                genre, priority, deadlineText);
        topic.setAuthorUserId(loggedUser.getUserId());
        topic.setReporterEmail(loggedUser.getEmail());
        topic.setReporterName(loggedUser.getFullName());
        topic.setDeskName(loggedUser.getDeskName());
        topic.setStatus("submit".equals(action) ? "SUBMITTED" : "DRAFT");
        if ("submit".equals(action)) {
            topic.setRejectionReason(null);
        }

        try (TopicDAO topicDAO = new TopicDAO()) {
            topicDAO.saveTopicPitch(topic, estimates, members);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu đề xuất đề tài", e);
            forwardComposeWithError(request, response,
                    safeMessage(e, "Không thể lưu đề tài. Vui lòng thử lại."), topic, estimates, members);
            return;
        }

        String message = "submit".equals(action) ? "submitted" : "draftSaved";
        response.sendRedirect(request.getContextPath() + "/reporter/topics?message=" + message);
    }

    private void setUserAttributes(HttpServletRequest request, User loggedUser) {
        if (loggedUser == null) {
            return;
        }
        request.setAttribute("currentUserId", loggedUser.getUserId());
        request.setAttribute("currentUserName", loggedUser.getFullName());
        request.setAttribute("currentUserEmail", loggedUser.getEmail());
        request.setAttribute("currentUserRole", loggedUser.getRoleName());
        request.setAttribute("currentUserDesk", loggedUser.getDeskName());
        request.setAttribute("userAvatar", "PV");

        try (NotificationDAO notifDAO = new NotificationDAO()) {
            request.setAttribute("unreadNotifCount", notifDAO.countUnreadByUserId(loggedUser.getUserId()));
        }
    }

    private void populateFormOptions(HttpServletRequest request) {
        request.setAttribute("today", LocalDate.now());
        CategoryDAO catDAO = new CategoryDAO();
        request.setAttribute("categories", catDAO.getAll());

        try (TopicDAO topicDAO = new TopicDAO()) {
            request.setAttribute("genres", topicDAO.findExistingGenres());
            request.setAttribute("priorities", topicDAO.findExistingPriorities());
            request.setAttribute("activeUsers", topicDAO.findAllActiveUsersForTeam());

            int topicId = 0;
            try {
                String idText = request.getParameter("id");
                if (idText == null || idText.trim().isEmpty()) {
                    idText = request.getParameter("topicId");
                }
                if (idText != null && !idText.trim().isEmpty()) {
                    topicId = Integer.parseInt(idText.trim());
                }
            } catch (Exception ignored) {}

            Map<Integer, List<String>> schedules = topicDAO.getUsersBusySchedule(topicId);
            request.setAttribute("userBusySchedules", schedules);
            request.setAttribute("userBusySchedulesJson", toScheduleJson(schedules));
        }
    }

    private Topic requestTopic(HttpServletRequest request, User loggedUser) {
        String idText = trim(request.getParameter("topicId"));
        if (idText.isEmpty() || "0".equals(idText)) {
            return createFormTopic();
        }

        final int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID đề tài không hợp lệ.");
        }

        if (id <= 0) {
            throw new IllegalArgumentException("ID đề tài không hợp lệ.");
        }

        try (TopicDAO topicDAO = new TopicDAO()) {
            Topic existing = topicDAO.getTopicDetailForReporter(id, loggedUser.getUserId());
            if (existing == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy đề tài hoặc bạn không có quyền chỉnh sửa đề tài này.");
            }
            return existing;
        }
    }

    private Topic createFormTopic() {
        Topic topic = new Topic();
        topic.setPriority("NORMAL");
        topic.setGenre("Tin nhanh");
        List<Category> categories = new CategoryDAO().getAll();
        if (categories != null && !categories.isEmpty() && categories.get(0) != null) {
            topic.setCategoryId(categories.get(0).getCategoryId());
            topic.setCategoryName(categories.get(0).getCategoryName());
        }
        topic.setDeadline(LocalDate.now().plusDays(7));
        return topic;
    }

    private void fillTopicFromForm(Topic topic, String title, String summary,
            String angle, int categoryId, String categoryName, String genre, String priority,
            String deadlineText) {
        topic.setTitle(title);
        topic.setSummary(summary);
        topic.setAngle(angle);
        if (categoryId > 0) {
            topic.setCategoryId(categoryId);
        }
        if (categoryName != null && !categoryName.isEmpty()) {
            topic.setCategoryName(categoryName);
        }
        topic.setGenre(genre == null || genre.isEmpty() ? "Tin nhanh" : genre);
        topic.setPriority(priority == null || priority.isEmpty() ? "NORMAL" : priority);
        if (deadlineText != null && !deadlineText.isEmpty()) {
            try {
                topic.setDeadline(LocalDate.parse(deadlineText));
            } catch (DateTimeParseException ignored) {}
        } else {
            topic.setDeadline(LocalDate.now().plusDays(7));
        }
    }

    private void forwardComposeWithError(HttpServletRequest request,
            HttpServletResponse response, String error, Topic topic,
            List<TopicEstimate> estimates, List<TopicMember> members)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        populateFormOptions(request);
        request.setAttribute("formTopic", topic == null ? createFormTopic() : topic);
        if (estimates != null) {
            request.setAttribute("formEstimates", estimates);
        }
        if (members != null) {
            request.setAttribute("formMembers", members);
        }
        request.getRequestDispatcher("/WEB-INF/views/reporter/compose.jsp").forward(request, response);
    }

    private String validate(String title, String summary, String angle,
            int categoryId, String genre, String priority, String deadline,
            String action) {
        if (title.isEmpty()) {
            return "Vui lòng nhập tiêu đề đề tài.";
        }
        if ("submit".equals(action)) {
            if (summary.isEmpty()) {
                return "Khi nộp đề xuất, vui lòng nhập tóm tắt ý tưởng.";
            }
            if (angle.isEmpty()) {
                return "Khi nộp đề xuất, vui lòng nhập góc tiếp cận (Angle).";
            }
            if (categoryId <= 0) {
                return "Vui lòng chọn chuyên mục cho đề tài.";
            }
            if (deadline.isEmpty()) {
                return "Vui lòng chọn deadline dự kiến.";
            }
        }

        if (categoryId > 0 && !isValidCategory(categoryId)) {
            return "Chuyên mục không hợp lệ. Vui lòng chọn lại chuyên mục.";
        }

        if (!deadline.isEmpty()) {
            try {
                if (LocalDate.parse(deadline).isBefore(LocalDate.now())) {
                    return "Deadline không được nằm trong quá khứ.";
                }
            } catch (DateTimeParseException e) {
                return "Định dạng ngày deadline không hợp lệ.";
            }
        }
        return null;
    }

    private boolean isValidCategory(int categoryId) {
        Category cat = new CategoryDAO().getById(categoryId);
        return cat != null;
    }

    private void showDashboard(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws ServletException, IOException {
        List<Topic> topics;
        try (TopicDAO topicDAO = new TopicDAO()) {
            topics = topicDAO.getTopicsByReporter(loggedUser.getUserId(), null, null);
        }
        int draft = 0;
        int submitted = 0;
        int approved = 0;
        int inProgress = 0;
        int revision = 0;
        int readyCms = 0;

        for (Topic topic : topics) {
            String status = topic.getStatus();
            if ("DRAFT".equals(status)) {
                draft++;
            } else if ("SUBMITTED".equals(status)
                    || "PENDING_EXECUTIVE_APPROVAL".equals(status)) {
                submitted++;
            } else if ("APPROVED".equals(status)) {
                approved++;
            } else if ("IN_PROGRESS".equals(status)) {
                inProgress++;
            } else if ("REVISION_REQUESTED".equals(status)
                    || "REVISION_REQUIRED".equals(status)) {
                revision++;
            } else if ("ACCEPTED_READY_FOR_CMS".equals(status)) {
                readyCms++;
            }
        }

        request.setAttribute("topics", topics);
        request.setAttribute("totalCount", topics.size());
        request.setAttribute("draftCount", draft);
        request.setAttribute("submittedCount", submitted);
        request.setAttribute("approvedCount", approved);
        request.setAttribute("inProgressCount", inProgress);
        request.setAttribute("revisionCount", revision);
        request.setAttribute("readyCmsCount", readyCms);
        request.setAttribute("activeCount", approved + inProgress);
        request.setAttribute("message", request.getParameter("message"));
        request.getRequestDispatcher("/WEB-INF/views/reporter/dashboard.jsp").forward(request, response);
    }

    private void showTopics(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws ServletException, IOException {
        String status = trim(request.getParameter("status"));
        String keyword = trim(request.getParameter("keyword"));
        List<Topic> filtered;
        int totalTopics;
        try (TopicDAO topicDAO = new TopicDAO()) {
            filtered = topicDAO.getTopicsByReporter(loggedUser.getUserId(), status, keyword);
            totalTopics = topicDAO.getTopicsByReporter(loggedUser.getUserId(), null, null).size();
        }

        request.setAttribute("topics", filtered);
        request.setAttribute("totalTopicsCount", totalTopics);
        request.setAttribute("status", status);
        request.setAttribute("keyword", keyword);
        request.setAttribute("message", request.getParameter("message"));
        request.getRequestDispatcher("/WEB-INF/views/reporter/topics.jsp").forward(request, response);
    }

    private void showCompose(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws ServletException, IOException {
        populateFormOptions(request);
        String idParam = trim(request.getParameter("id"));

        if (!idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Topic topic = null;
                List<TopicEstimate> estimates = null;
                List<TopicMember> members = null;

                if (id > 0) {
                    try (TopicDAO topicDAO = new TopicDAO()) {
                        topic = topicDAO.getTopicDetailForReporter(id, loggedUser.getUserId());
                        if (topic != null) {
                            estimates = topicDAO.findEstimatesByTopicId(id);
                            members = topicDAO.findMembersByTopicId(id);
                        }
                    }
                }

                if (topic == null) {
                    request.setAttribute("error",
                            "Không tìm thấy đề tài hoặc bạn không có quyền chỉnh sửa đề tài này.");
                    request.setAttribute("formTopic", createFormTopic());
                } else {
                    request.setAttribute("formTopic", topic);
                    request.setAttribute("formEstimates", estimates);
                    request.setAttribute("formMembers", members);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "ID đề tài không hợp lệ.");
                request.setAttribute("formTopic", createFormTopic());
            }
        } else {
            request.setAttribute("formTopic", createFormTopic());
        }

        request.getRequestDispatcher("/WEB-INF/views/reporter/compose.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws ServletException, IOException {
        Topic topic = null;
        List<TopicEstimate> estimates = null;
        List<TopicMember> members = null;
        String idText = trim(request.getParameter("id"));
        try {
            int id = Integer.parseInt(idText);
            if (id > 0) {
                try (TopicDAO topicDAO = new TopicDAO()) {
                    topic = topicDAO.getTopicDetailForReporter(id, loggedUser.getUserId());
                    if (topic != null) {
                        estimates = topicDAO.findEstimatesByTopicId(topic.getTopicId());
                        members = topicDAO.findMembersByTopicId(topic.getTopicId());
                    }
                }
            }
        } catch (NumberFormatException ignored) {}

        request.setAttribute("topic", topic);
        if (topic != null) {
            request.setAttribute("stepRank", calculateWorkflowRank(topic.getStatus()));
            request.setAttribute("estimates", estimates);
            request.setAttribute("members", members);
        }
        request.getRequestDispatcher("/WEB-INF/views/reporter/detail.jsp").forward(request, response);
    }

    private int calculateWorkflowRank(String status) {
        if ("DRAFT".equals(status)) return 1;
        if ("SUBMITTED".equals(status)) return 2;
        if ("PENDING_EXECUTIVE_APPROVAL".equals(status)) return 3;
        if ("APPROVED".equals(status)) return 4;
        if ("IN_PROGRESS".equals(status)) return 5;
        if ("DELIVERED".equals(status)) return 6;
        if ("ACCEPTED_READY_FOR_CMS".equals(status)) return 7;
        return 1;
    }

    private void searchDuplicates(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));
        request.setAttribute("keyword", keyword);
        List<Topic> matches = new ArrayList<>();
        if (!keyword.isEmpty()) {
            try (TopicDAO topicDAO = new TopicDAO()) {
                matches = topicDAO.searchSimilarTopics(keyword);
            }
        }
        request.setAttribute("matches", matches);
        request.getRequestDispatcher("/WEB-INF/views/reporter/duplicate-search.jsp").forward(request, response);
    }

    private void handleDelete(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws IOException {
        String idText = trim(request.getParameter("id"));
        if (idText.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/reporter/topics?message=deleteFailed");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            boolean ok = false;
            try (TopicDAO topicDAO = new TopicDAO()) {
                ok = topicDAO.deleteReporterDraft(id, loggedUser.getUserId());
            }
            if (ok) {
                response.sendRedirect(request.getContextPath() + "/reporter/topics?message=deleted");
            } else {
                response.sendRedirect(request.getContextPath() + "/reporter/topics?message=deleteFailed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/reporter/topics?message=deleteFailed");
        }
    }

    private void handleQuickSubmit(HttpServletRequest request,
            HttpServletResponse response, User loggedUser) throws IOException {
        String idText = trim(request.getParameter("id"));
        if (idText.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/reporter/topics?message=submitFailed");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            try (TopicDAO topicDAO = new TopicDAO()) {
                Topic topic = topicDAO.getTopicDetailForReporter(id, loggedUser.getUserId());
                if (topic == null || !("DRAFT".equals(topic.getStatus())
                        || "REVISION_REQUESTED".equals(topic.getStatus())
                        || "REVISION_REQUIRED".equals(topic.getStatus()))) {
                    response.sendRedirect(request.getContextPath() + "/reporter/topics?message=submitFailed");
                    return;
                }
                topic.setStatus("SUBMITTED");
                topic.setRejectionReason(null);
                topicDAO.saveTopicPitch(topic, null, null);
                response.sendRedirect(request.getContextPath() + "/reporter/topics?message=submitted");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi nộp nhanh đề tài", e);
            response.sendRedirect(request.getContextPath() + "/reporter/topics?message=submitFailed");
        }
    }

    private String safeMessage(Exception exception, String fallback) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty() ? fallback : message;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void handleCheckSchedule(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        String uidStr = trim(request.getParameter("userId"));
        String tidStr = trim(request.getParameter("topicId"));
        int userId = 0;
        int topicId = 0;
        try {
            if (!uidStr.isEmpty()) {
                userId = Integer.parseInt(uidStr);
            }
            if (!tidStr.isEmpty()) {
                topicId = Integer.parseInt(tidStr);
            }
        } catch (NumberFormatException ignored) {}

        List<String> busyTopics;
        try (TopicDAO topicDAO = new TopicDAO()) {
            busyTopics = topicDAO.getUserBusyTopics(userId, topicId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"userId\":").append(userId);
        sb.append(",\"busy\":").append(!busyTopics.isEmpty());
        sb.append(",\"count\":").append(busyTopics.size());
        sb.append(",\"topics\":[");
        for (int i = 0; i < busyTopics.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(busyTopics.get(i))).append("\"");
        }
        sb.append("]}");
        response.getWriter().write(sb.toString());
    }

    private List<TopicEstimate> parseEstimatesFromRequest(HttpServletRequest request) {
        List<TopicEstimate> list = new ArrayList<>();
        String[] costItems = request.getParameterValues("costItem");
        String[] amounts = request.getParameterValues("amount");

        if (costItems != null && amounts != null) {
            for (int i = 0; i < costItems.length && i < amounts.length; i++) {
                String item = trim(costItems[i]);
                String amtStr = trim(amounts[i]);
                if (!item.isEmpty()) {
                    BigDecimal amt = BigDecimal.ZERO;
                    try {
                        if (!amtStr.isEmpty()) {
                            amtStr = amtStr.replaceAll("[^0-9.]", "");
                            amt = new BigDecimal(amtStr);
                        }
                    } catch (Exception ignored) {}
                    TopicEstimate est = new TopicEstimate();
                    est.setCostItem(item);
                    est.setAmount(amt);
                    list.add(est);
                }
            }
        }
        return list;
    }

    private List<TopicMember> parseMembersFromRequest(HttpServletRequest request) {
        List<TopicMember> list = new ArrayList<>();
        String[] userIds = request.getParameterValues("memberUserId");
        String[] roles = request.getParameterValues("memberRole");

        Set<Integer> added = new HashSet<>();
        if (userIds != null && roles != null) {
            for (int i = 0; i < userIds.length && i < roles.length; i++) {
                String uidStr = trim(userIds[i]);
                String role = trim(roles[i]);
                if (!uidStr.isEmpty()) {
                    try {
                        int uid = Integer.parseInt(uidStr);
                        if (uid > 0 && !added.contains(uid)) {
                            added.add(uid);
                            TopicMember mem = new TopicMember();
                            mem.setUserId(uid);
                            mem.setRoleInTeam(role.isEmpty() ? "ASSISTANT" : role);
                            list.add(mem);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return list;
    }

    private String toScheduleJson(Map<Integer, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean firstUser = true;
        for (Map.Entry<Integer, List<String>> entry : map.entrySet()) {
            if (!firstUser) sb.append(",");
            firstUser = false;
            sb.append("\"").append(entry.getKey()).append("\":[");
            boolean firstTopic = true;
            for (String t : entry.getValue()) {
                if (!firstTopic) sb.append(",");
                firstTopic = false;
                sb.append("\"").append(escapeJson(t)).append("\"");
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private void handleGetNotifications(HttpServletRequest request, HttpServletResponse response, User loggedUser)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (NotificationDAO notifDAO = new NotificationDAO()) {
            List<Notification> list = notifDAO.findByUserId(loggedUser.getUserId());
            int unread = notifDAO.countUnreadByUserId(loggedUser.getUserId());

            StringBuilder json = new StringBuilder();
            json.append("{\"unreadCount\":").append(unread)
                .append(",\"totalCount\":").append(list.size())
                .append(",\"notifications\":[");

            for (int i = 0; i < list.size(); i++) {
                Notification n = list.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{")
                    .append("\"id\":").append(n.getNotificationId()).append(",")
                    .append("\"userId\":").append(n.getUserId()).append(",")
                    .append("\"topicId\":").append(n.getTopicId() == null ? "null" : n.getTopicId()).append(",")
                    .append("\"message\":\"").append(escapeJson(n.getMessage())).append("\",")
                    .append("\"isRead\":").append(n.isIsRead()).append(",")
                    .append("\"createdAt\":\"").append(escapeJson(n.getCreatedAt())).append("\"")
                    .append("}");
            }
            json.append("]}");

            response.getWriter().write(json.toString());
        }
    }

    private void handlePostNotifications(HttpServletRequest request, HttpServletResponse response, User loggedUser)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        int userId = loggedUser.getUserId();
        try (NotificationDAO notifDAO = new NotificationDAO()) {
            String action = trim(request.getParameter("action"));
            if ("markAllRead".equals(action)) {
                notifDAO.markAllAsRead(userId);
            } else if ("markRead".equals(action)) {
                try {
                    int id = Integer.parseInt(trim(request.getParameter("id")));
                    notifDAO.markAsRead(id, userId);
                } catch (Exception ignored) {}
            }

            int unread = notifDAO.countUnreadByUserId(userId);
            response.getWriter().write("{\"success\":true,\"unreadCount\":" + unread + "}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
