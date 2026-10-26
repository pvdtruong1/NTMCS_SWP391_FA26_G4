package controller;

import dao.CategoryDAO;
import dao.TopicRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Category;
import model.Topic;

@WebServlet(name = "ReporterServlet", urlPatterns = {"/reporter/*"})
public class ReporterServlet extends HttpServlet {

    public static final String DEMO_REPORTER_EMAIL = "hung.reporter@newsroom.vn";
    public static final String DEMO_REPORTER_NAME = "Mai Khánh Hùng";
    public static final String DEMO_REPORTER_ROLE = "REPORTER";
    public static final String DEMO_REPORTER_DESK = "Thời sự";

    private final TopicRepository repository = TopicRepository.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        setUserAttributes(request);

        String path = request.getPathInfo();
        if (path == null || "/".equals(path) || "/dashboard".equals(path)) {
            showDashboard(request, response);
        } else if ("/topics".equals(path)) {
            showTopics(request, response);
        } else if ("/duplicate-search".equals(path)) {
            searchDuplicates(request, response);
        } else if ("/compose".equals(path)) {
            showCompose(request, response);
        } else if ("/detail".equals(path)) {
            showDetail(request, response);
        } else if ("/delete".equals(path)) {
            handleDelete(request, response);
        } else if ("/quick-submit".equals(path)) {
            handleQuickSubmit(request, response);
        } else if ("/reset".equals(path)) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/dashboard?message=resetDisabled");
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        setUserAttributes(request);

        String path = request.getPathInfo();
        if ("/delete".equals(path)) {
            handleDelete(request, response);
            return;
        }
        if ("/quick-submit".equals(path)) {
            handleQuickSubmit(request, response);
            return;
        }
        if ("/reset".equals(path)) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/dashboard?message=resetDisabled");
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
        String category = trim(request.getParameter("category"));
        String genre = trim(request.getParameter("genre"));
        String priority = trim(request.getParameter("priority"));
        String deadlineText = trim(request.getParameter("deadline"));

        Topic topic;
        try {
            // Resolve the requested topic before validating or saving. An invalid ID must not create a new topic.
            topic = requestTopic(request);
        } catch (IllegalArgumentException e) {
            Topic formTopic = createFormTopic();
            fillTopicFromForm(formTopic, title, summary, angle, category,
                    genre, priority, deadlineText);
            forwardComposeWithError(request, response, e.getMessage(), formTopic);
            return;
        }

        String error = validate(title, summary, angle, category, genre,
                priority, deadlineText, action);
        if (error != null) {
            fillTopicFromForm(topic, title, summary, angle, category,
                    genre, priority, deadlineText);
            forwardComposeWithError(request, response, error, topic);
            return;
        }

        fillTopicFromForm(topic, title, summary, angle, category,
                genre, priority, deadlineText);
        topic.setReporterEmail(DEMO_REPORTER_EMAIL);
        topic.setReporterName(DEMO_REPORTER_NAME);
        topic.setDesk(DEMO_REPORTER_DESK);
        topic.setStatus("submit".equals(action) ? "SUBMITTED" : "DRAFT");
        if ("submit".equals(action)) {
            topic.setRejectionReason(null);
        }

        try {
            repository.save(topic);
        } catch (IllegalArgumentException | IllegalStateException e) {
            forwardComposeWithError(request, response,
                    safeMessage(e, "Không thể lưu đề tài. Vui lòng thử lại."), topic);
            return;
        }

        String message = "submit".equals(action) ? "submitted" : "draftSaved";
        response.sendRedirect(request.getContextPath()
                + "/reporter/topics?message=" + message);
    }

    private void setUserAttributes(HttpServletRequest request) {
        request.setAttribute("currentUserName", DEMO_REPORTER_NAME);
        request.setAttribute("currentUserEmail", DEMO_REPORTER_EMAIL);
        request.setAttribute("currentUserRole", DEMO_REPORTER_ROLE);
        request.setAttribute("currentUserDesk", DEMO_REPORTER_DESK);
        request.setAttribute("userAvatar", "PV");
    }

    private void populateFormOptions(HttpServletRequest request) {
        request.setAttribute("today", LocalDate.now());
        List<String> categoryNames = new ArrayList<>();
        for (Category category : new CategoryDAO().getAll()) {
            if (category != null && category.getCategoryName() != null) {
                categoryNames.add(category.getCategoryName());
            }
        }
        request.setAttribute("categories", categoryNames);
        request.setAttribute("genres", TopicRepository.GENRES);
        request.setAttribute("priorities",
                repository.findExistingPriorities());
    }

    /**
     * Returns a new topic only when topicId is empty; invalid IDs are rejected.
     */
    /**
     * Returns a new topic when topicId is empty or zero. Loads an existing
     * topic only when topicId is a positive number.
     */
    private Topic requestTopic(HttpServletRequest request) {
        String idText = trim(request.getParameter("topicId"));

        // Tạo đề tài mới nếu ID không được gửi hoặc bằng 0.
        if (idText.isEmpty() || "0".equals(idText)) {
            return createFormTopic();
        }

        final int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID đề tài không hợp lệ.");
        }

        if (id < 0) {
            throw new IllegalArgumentException("ID đề tài không hợp lệ.");
        }

        Topic existing = repository.findById(id, DEMO_REPORTER_EMAIL);

        if (existing == null) {
            throw new IllegalArgumentException(
                    "Không tìm thấy đề tài hoặc bạn không có quyền chỉnh sửa đề tài này.");
        }

        return existing;
    }

    private Topic createFormTopic() {
        Topic topic = new Topic();
        topic.setPriority("NORMAL");
        topic.setGenre("Tin nhanh");
        List<Category> categories = new CategoryDAO().getAll();
        if (categories != null && !categories.isEmpty()
                && categories.get(0) != null) {
            topic.setCategory(categories.get(0).getCategoryName());
        }
        topic.setDeadline(LocalDate.now().plusDays(7));
        return topic;
    }

    private void fillTopicFromForm(Topic topic, String title, String summary,
            String angle, String category, String genre, String priority,
            String deadlineText) {
        topic.setTitle(title);
        topic.setSummary(summary);
        topic.setAngle(angle);
        topic.setCategory(category);
        topic.setGenre(genre.isEmpty() ? "Tin nhanh" : genre);
        topic.setPriority(priority.isEmpty() ? "NORMAL" : priority);
        if (!deadlineText.isEmpty()) {
            try {
                topic.setDeadline(LocalDate.parse(deadlineText));
            } catch (DateTimeParseException ignored) {
                // Keep the existing/default deadline so the form can be redisplayed with the validation message.
            }
        } else {
            topic.setDeadline(LocalDate.now().plusDays(7));
        }
    }

    private void forwardComposeWithError(HttpServletRequest request,
            HttpServletResponse response, String error, Topic topic)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        populateFormOptions(request);
        request.setAttribute("formTopic", topic == null ? createFormTopic() : topic);
        request.getRequestDispatcher("/WEB-INF/views/reporter/compose.jsp")
                .forward(request, response);
    }

    private String validate(String title, String summary, String angle,
            String category, String genre, String priority, String deadline,
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
            if (category.isEmpty()) {
                return "Vui lòng chọn chuyên mục cho đề tài.";
            }
            if (deadline.isEmpty()) {
                return "Vui lòng chọn deadline dự kiến.";
            }
        }

        if (!category.isEmpty() && !isValidCategory(category)) {
            return "Chuyên mục không hợp lệ. Vui lòng chọn lại chuyên mục.";
        }

        if (!genre.isEmpty()
                && !java.util.Arrays.asList(TopicRepository.GENRES)
                        .contains(genre)) {
            return "Thể loại đề tài không hợp lệ.";
        }

        if (!priority.isEmpty()
                && !java.util.Arrays.asList(TopicRepository.PRIORITIES)
                        .contains(priority)) {
            return "Mức độ ưu tiên không hợp lệ.";
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

    private boolean isValidCategory(String categoryName) {
        for (Category category : new CategoryDAO().getAll()) {
            if (category != null && categoryName.equals(category.getCategoryName())) {
                return true;
            }
        }
        return false;
    }

    private void showDashboard(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        List<Topic> topics = repository.findByReporter(DEMO_REPORTER_EMAIL);
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
        request.getRequestDispatcher("/WEB-INF/views/reporter/dashboard.jsp")
                .forward(request, response);
    }

    private void showTopics(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String status = trim(request.getParameter("status"));
        String keyword = trim(request.getParameter("keyword")).toLowerCase();
        List<Topic> all = repository.findByReporter(DEMO_REPORTER_EMAIL);
        List<Topic> filtered = new ArrayList<>();

        for (Topic topic : all) {
            boolean statusMatch = status.isEmpty()
                    || status.equalsIgnoreCase(topic.getStatus());
            boolean keywordMatch = keyword.isEmpty()
                    || containsIgnoreCase(topic.getTitle(), keyword)
                    || containsIgnoreCase(topic.getSummary(), keyword)
                    || containsIgnoreCase(topic.getAngle(), keyword)
                    || containsIgnoreCase(topic.getTopicCode(), keyword);
            if (statusMatch && keywordMatch) {
                filtered.add(topic);
            }
        }

        request.setAttribute("topics", filtered);
        request.setAttribute("totalTopicsCount", all.size());
        request.setAttribute("status", status);
        request.setAttribute("keyword", trim(request.getParameter("keyword")));
        request.setAttribute("message", request.getParameter("message"));
        request.getRequestDispatcher("/WEB-INF/views/reporter/topics.jsp")
                .forward(request, response);
    }

    private void showCompose(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        populateFormOptions(request);
        String idParam = trim(request.getParameter("id"));

        if (!idParam.isEmpty()) {
            try {
                int id = Integer.parseInt(idParam);
                Topic topic = id > 0
                        ? repository.findById(id, DEMO_REPORTER_EMAIL) : null;
                if (topic == null) {
                    request.setAttribute("error",
                            "Không tìm thấy đề tài hoặc bạn không có quyền chỉnh sửa đề tài này.");
                    request.setAttribute("formTopic", createFormTopic());
                } else {
                    request.setAttribute("formTopic", topic);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("error", "ID đề tài không hợp lệ.");
                request.setAttribute("formTopic", createFormTopic());
            }
        } else {
            request.setAttribute("formTopic", createFormTopic());
        }

        request.getRequestDispatcher("/WEB-INF/views/reporter/compose.jsp")
                .forward(request, response);
    }

    private void showDetail(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        Topic topic = null;
        String idText = trim(request.getParameter("id"));
        try {
            int id = Integer.parseInt(idText);
            if (id > 0) {
                topic = repository.findById(id, DEMO_REPORTER_EMAIL);
            }
        } catch (NumberFormatException ignored) {
            // Invalid ID is handled by rendering the detail page without a topic.
        }
        request.setAttribute("topic", topic);
        if (topic != null) {
            request.setAttribute("stepRank", calculateWorkflowRank(topic.getStatus()));
        }
        request.getRequestDispatcher("/WEB-INF/views/reporter/detail.jsp")
                .forward(request, response);
    }

    private int calculateWorkflowRank(String status) {
        if ("DRAFT".equals(status)) {
            return 1;
        }
        if ("SUBMITTED".equals(status)) {
            return 2;
        }
        if ("PENDING_EXECUTIVE_APPROVAL".equals(status)) {
            return 3;
        }
        if ("APPROVED".equals(status)) {
            return 4;
        }
        if ("IN_PROGRESS".equals(status)) {
            return 5;
        }
        if ("DELIVERED".equals(status)) {
            return 6;
        }
        if ("ACCEPTED_READY_FOR_CMS".equals(status)) {
            return 7;
        }
        return 1;
    }

    private void searchDuplicates(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String keyword = trim(request.getParameter("keyword"));
        request.setAttribute("keyword", keyword);
        request.setAttribute("matches",
                repository.searchSimilar(keyword, DEMO_REPORTER_EMAIL));
        request.getRequestDispatcher("/WEB-INF/views/reporter/duplicate-search.jsp")
                .forward(request, response);
    }

    private void handleDelete(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String idText = trim(request.getParameter("id"));
        if (idText.isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=deleteFailed");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            if (id <= 0 || repository.findById(id, DEMO_REPORTER_EMAIL) == null) {
                response.sendRedirect(request.getContextPath()
                        + "/reporter/topics?message=deleteFailed");
                return;
            }
            repository.delete(id, DEMO_REPORTER_EMAIL);
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=deleted");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=deleteFailed");
        }
    }

    private void handleQuickSubmit(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String idText = trim(request.getParameter("id"));
        if (idText.isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=submitFailed");
            return;
        }
        try {
            int id = Integer.parseInt(idText);
            Topic topic = id > 0
                    ? repository.findById(id, DEMO_REPORTER_EMAIL) : null;
            if (topic == null || !("DRAFT".equals(topic.getStatus())
                    || "REVISION_REQUESTED".equals(topic.getStatus())
                    || "REVISION_REQUIRED".equals(topic.getStatus()))) {
                response.sendRedirect(request.getContextPath()
                        + "/reporter/topics?message=submitFailed");
                return;
            }
            topic.setStatus("SUBMITTED");
            topic.setRejectionReason(null);
            repository.save(topic);
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=submitted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.sendRedirect(request.getContextPath()
                    + "/reporter/topics?message=submitFailed");
        }
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private String safeMessage(Exception exception, String fallback) {
        String message = exception.getMessage();
        return message == null || message.trim().isEmpty() ? fallback : message;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
