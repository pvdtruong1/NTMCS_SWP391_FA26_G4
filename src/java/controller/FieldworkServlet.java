package controller;

import dal.BroadcastAuditDAO;
import dal.EmergencyIncidentDAO;
import dal.FieldworkDeliverableDAO;
import dal.FieldworkTaskDAO;
import dal.SLAConfigDAO;
import dal.TaskNudgeDAO;
import dal.TopicDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.FieldworkDeliverable;
import model.FieldworkTask;
import model.SLAConfig;
import model.Topic;
import model.TopicEstimate;
import model.TopicMember;
import model.User;
import util.AuthUtil;

@WebServlet(name = "FieldworkServlet", urlPatterns = {
    "/fieldwork/kanban",
    "/fieldwork/todo",
    "/fieldwork/nudge",
    "/fieldwork/audit",
    "/fieldwork/sla",
    "/fieldwork/topic-detail"
})
public class FieldworkServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FieldworkServlet.class.getName());

    private final FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();
    private final FieldworkDeliverableDAO deliverableDAO = new FieldworkDeliverableDAO();
    private final EmergencyIncidentDAO incidentDAO = new EmergencyIncidentDAO();
    private final BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();
    private final TaskNudgeDAO nudgeDAO = new TaskNudgeDAO();
    private final SLAConfigDAO slaDAO = new SLAConfigDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthUtil.getLoggedUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        try {
            switch (path) {
                case "/fieldwork/kanban":
                    renderKanban(request, response, user);
                    break;
                case "/fieldwork/todo":
                    renderTodo(request, response, user);
                    break;
                case "/fieldwork/nudge":
                    renderNudge(request, response, user);
                    break;
                case "/fieldwork/audit":
                    renderAudit(request, response, user);
                    break;
                case "/fieldwork/sla":
                    renderSLA(request, response, user);
                    break;
                case "/fieldwork/topic-detail":
                    renderTopicDetail(request, response, user);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/fieldwork/kanban");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi điều hướng phân hệ Hiện trường: " + path, e);
            request.setAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/fieldwork/kanban.jsp").forward(request, response);
        }
    }

    private void renderKanban(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        List<FieldworkTask> allTasks = taskDAO.getAllTasks();
        request.setAttribute("tasks", allTasks);

        List<Object[]> approvedTopics = taskDAO.getActiveApprovedTopics();
        request.setAttribute("approvedTopics", approvedTopics);

        // Chỉ nạp danh sách Phóng viên tác nghiệp (Role 1), loại bỏ Trưởng ban, TBT, Admin, Hậu cần khỏi danh sách giao việc
        List<User> allUsers = userDAO.getAll();
        List<User> reporters = new ArrayList<>();
        if (allUsers != null) {
            for (User u : allUsers) {
                if (u.isActiveStatus() && (u.getRoleId() == 1 || "REPORTER".equalsIgnoreCase(u.getRoleName()))) {
                    reporters.add(u);
                }
            }
        }
        request.setAttribute("users", reporters);

        request.getRequestDispatcher("/WEB-INF/views/fieldwork/kanban.jsp").forward(request, response);
    }

    private void renderTodo(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        List<FieldworkTask> myTasks = taskDAO.getTasksByAssignee(user.getUserId());
        request.setAttribute("tasks", myTasks);

        List<Object[]> approvedTopics = taskDAO.getActiveApprovedTopics();
        request.setAttribute("approvedTopics", approvedTopics);

        request.getRequestDispatcher("/WEB-INF/views/fieldwork/todo.jsp").forward(request, response);
    }

    private void renderNudge(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        List<Object[]> assignments = nudgeDAO.getReporterTopicAssignments();
        request.setAttribute("assignments", assignments);
        request.getRequestDispatcher("/WEB-INF/views/fieldwork/nudge.jsp").forward(request, response);
    }

    private void renderAudit(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        try {
            List<Object[]> auditLogs = auditDAO.getFieldworkNotificationAudit(user.getUserId());

            String export = request.getParameter("export");
            if ("csv".equalsIgnoreCase(export)) {
                exportAuditCsv(response, auditLogs, user);
                return;
            }

            request.setAttribute("auditLogs", auditLogs);
        } catch (IllegalArgumentException e) {
            request.setAttribute("permissionError", e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/views/fieldwork/audit.jsp").forward(request, response);
    }

    private void exportAuditCsv(HttpServletResponse response, List<Object[]> auditLogs, User user) throws IOException {
        String filename = "NTMCS_Notification_Audit_" + java.time.LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        java.io.OutputStream out = response.getOutputStream();
        // UTF-8 BOM so Excel on Windows opens Vietnamese characters without mojibake
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("Mã thông báo,Loại nguồn,Mức độ,Người gửi,Người nhận,Email người nhận,Phòng ban,Mã đề tài,Tên đề tài,Nội dung thông báo,Thời gian gửi,Trạng thái,Thời gian đọc");

            for (Object[] r : auditLogs) {
                String notifId = r[0] != null ? r[0].toString() : "";
                String recipientName = r[2] != null ? r[2].toString() : "";
                String recipientEmail = r[3] != null ? r[3].toString() : "";
                String recipientDesk = r[4] != null ? r[4].toString() : "";
                String topicCode = r[6] != null ? r[6].toString() : "";
                String topicTitle = r[7] != null ? r[7].toString() : "";
                String message = r[8] != null ? r[8].toString() : "";
                boolean isRead = Boolean.TRUE.equals(r[9]);
                String senderName = r[11] != null ? r[11].toString() : "Hệ thống";
                String sentAt = r[12] != null ? r[12].toString() : "";
                String readAt = r[13] != null ? r[13].toString() : "";
                String sourceType = r[14] != null ? r[14].toString() : "";
                String urgency = r[15] != null ? r[15].toString() : "";

                String sourceLabel;
                if ("TASK_ASSIGNMENT".equals(sourceType)) sourceLabel = "Giao việc hiện trường";
                else if ("TASK_ACCEPTED".equals(sourceType)) sourceLabel = "Tiếp nhận tác nghiệp";
                else if ("TASK_COMPLETED".equals(sourceType)) sourceLabel = "Hoàn thành tác nghiệp";
                else if ("TASK_NUDGE".equals(sourceType)) sourceLabel = "Đôn đốc tác nghiệp";
                else if ("EMERGENCY_INCIDENT".equals(sourceType)) sourceLabel = "Sự cố khẩn cấp";
                else if ("PING_NUDGE".equals(sourceType)) sourceLabel = "Ping đôn đốc";
                else if ("SLA_ALERT".equals(sourceType)) sourceLabel = "Cảnh báo SLA";
                else sourceLabel = sourceType;

                String urgencyLabel;
                if ("EMERGENCY".equals(urgency)) urgencyLabel = "Khẩn cấp";
                else if ("HIGH".equals(urgency)) urgencyLabel = "Ưu tiên cao";
                else urgencyLabel = "Tiêu chuẩn";

                writer.println(String.join(",",
                    csvEscape(notifId),
                    csvEscape(sourceLabel),
                    csvEscape(urgencyLabel),
                    csvEscape(senderName),
                    csvEscape(recipientName),
                    csvEscape(recipientEmail),
                    csvEscape(recipientDesk),
                    csvEscape(topicCode),
                    csvEscape(topicTitle),
                    csvEscape(message),
                    csvEscape(sentAt),
                    csvEscape(isRead ? "Đã đọc" : "Chưa đọc"),
                    csvEscape(readAt)
                ));
            }
            writer.flush();
        }

        try {
            new dal.AuditLogDAO().log(user.getUserId(), "EXPORT_NOTIFICATION_AUDIT", "NOTIFICATION", null,
                "Xuất " + auditLogs.size() + " dòng nhật ký thông báo ra file CSV");
        } catch (Exception ignored) {}
    }

    private String csvEscape(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    private void renderSLA(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        SLAConfig config = slaDAO.getConfig();
        request.setAttribute("slaConfig", config);
        request.getRequestDispatcher("/WEB-INF/views/fieldwork/sla.jsp").forward(request, response);
    }

    private void renderTopicDetail(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            idParam = request.getParameter("topicId");
        }

        int topicId = -1;
        try {
            if (idParam != null) {
                topicId = Integer.parseInt(idParam.trim());
            }
        } catch (NumberFormatException ignored) {}

        if (topicId <= 0) {
            response.sendRedirect(request.getContextPath() + "/fieldwork/kanban");
            return;
        }

        Topic topic = topicDAO.getTopicDetailForReporter(topicId, 0);
        if (topic == null) {
            request.setAttribute("errorMessage", "Không tìm thấy hồ sơ đề tài #" + topicId);
            request.getRequestDispatcher("/WEB-INF/views/fieldwork/kanban.jsp").forward(request, response);
            return;
        }

        List<TopicMember> members = topicDAO.findMembersByTopicId(topicId);
        List<TopicEstimate> estimates = topicDAO.findEstimatesByTopicId(topicId);
        List<FieldworkTask> tasks = taskDAO.getTasksByTopicId(topicId);
        List<FieldworkDeliverable> deliverables = deliverableDAO.getDeliverablesByTopicId(topicId);
        List<Object[]> incidents = incidentDAO.getIncidentsByTopicId(topicId);

        request.setAttribute("topic", topic);
        request.setAttribute("members", members);
        request.setAttribute("estimates", estimates);
        request.setAttribute("tasks", tasks);
        request.setAttribute("deliverables", deliverables);
        request.setAttribute("incidents", incidents);

        request.getRequestDispatcher("/WEB-INF/views/fieldwork/topic-detail.jsp").forward(request, response);
    }
}
