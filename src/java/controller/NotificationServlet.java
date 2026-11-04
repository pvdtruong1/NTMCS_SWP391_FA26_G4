package controller;

import dal.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import model.Notification;
import model.User;
import util.AuthUtil;

/**
 * Universal In-App Notification API for all newsroom users (WF-01, WF-02, WF-04, WF-05).
 * Supports fetching unread count, list of notifications, and marking items as read.
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/api/notifications"})
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notifDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoggedUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để xem thông báo.\"}");
            return;
        }

        int userId = user.getUserId();
        List<Notification> list = notifDAO.findByUserId(userId);
        int unread = notifDAO.countUnreadByUserId(userId);

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
                .append("\"isRead\":").append(n.isRead()).append(",")
                .append("\"createdAt\":\"").append(escapeJson(n.getCreatedAt() != null ? n.getCreatedAt() : "")).append("\"")
                .append("}");
        }
        json.append("]}");

        response.getWriter().write(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoggedUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để thao tác thông báo.\"}");
            return;
        }

        int userId = user.getUserId();
        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            String json = body.toString();
            if (json.contains("\"action\"")) {
                action = json.replaceAll(".*\"action\"\\s*:\\s*\"([^\"]+)\".*", "$1");
            }
            if (json.contains("\"id\"")) {
                idParam = json.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");
            }
        }

        if ("markAllRead".equalsIgnoreCase(action)) {
            notifDAO.markAllAsRead(userId);
        } else if ("markRead".equalsIgnoreCase(action)) {
            if (idParam != null && !idParam.trim().isEmpty()) {
                try {
                    int notifId = Integer.parseInt(idParam.trim());
                    notifDAO.markAsRead(notifId, userId);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int unread = notifDAO.countUnreadByUserId(userId);
        response.getWriter().write("{\"success\":true,\"unreadCount\":" + unread + "}");
    }

    private String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
