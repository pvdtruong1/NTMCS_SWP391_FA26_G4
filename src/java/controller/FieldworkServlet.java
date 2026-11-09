package controller;

import dal.BroadcastAuditDAO;
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
import model.FieldworkTask;
import model.SLAConfig;
import model.Topic;
import model.User;
import util.AuthUtil;

@WebServlet(name = "FieldworkServlet", urlPatterns = {
    "/fieldwork/kanban",
    "/fieldwork/todo",
    "/fieldwork/nudge",
    "/fieldwork/audit",
    "/fieldwork/sla"
})
public class FieldworkServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FieldworkServlet.class.getName());

    private final FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();
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
            request.setAttribute("auditLogs", auditLogs);
        } catch (IllegalArgumentException e) {
            request.setAttribute("permissionError", e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/views/fieldwork/audit.jsp").forward(request, response);
    }

    private void renderSLA(HttpServletRequest request, HttpServletResponse response, User user)
            throws Exception {
        SLAConfig config = slaDAO.getConfig();
        request.setAttribute("slaConfig", config);
        request.getRequestDispatcher("/WEB-INF/views/fieldwork/sla.jsp").forward(request, response);
    }
}
