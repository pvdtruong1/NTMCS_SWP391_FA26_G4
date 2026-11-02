package servlet;

import dao.NotificationDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Notification;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/notifications")
public class NotificationServlet extends HttpServlet {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String userValue = request.getParameter("userId");
            if (userValue == null || userValue.trim().isEmpty()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Thiếu userId.");
                return;
            }
            int userId = Integer.parseInt(userValue);
            List<Notification> notifications = notificationDAO.getByUserId(userId);
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                if (i > 0) json.append(',');
                json.append('{')
                    .append("\"notificationId\":").append(n.getNotificationId()).append(',')
                    .append("\"userId\":").append(n.getUserId()).append(',')
                    .append("\"topicId\":");
                if (n.getTopicId() == null) json.append("null");
                else json.append(n.getTopicId());
                json.append(',')
                    .append("\"message\":\"").append(escapeJson(n.getMessage())).append("\",")
                    .append("\"isRead\":").append(n.isRead()).append(',')
                    .append("\"createdAt\":\"").append(escapeJson(n.getCreatedAt())).append("\"")
                    .append('}');
            }
            json.append(']');
            response.getWriter().write(json.toString());
        } catch (NumberFormatException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "userId không hợp lệ.");
        } catch (SQLException e) {
            getServletContext().log("Failed to retrieve notifications", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không tải được thông báo.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }

        try {
            String json = body.toString();
            int userId = extractInt(json, "userId");
            boolean markAll = json.matches("(?s).*\\\"markAll\\\"\\s*:\\s*true.*");
            if (markAll) {
                int updated = notificationDAO.markAllAsRead(userId);
                response.getWriter().write("{\"updatedCount\":" + updated + "}");
                return;
            }

            int notificationId = extractInt(json, "notificationId");
            boolean updated = notificationDAO.markOneAsRead(notificationId, userId);
            if (!updated) {
                writeError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy thông báo chưa đọc của người dùng này.");
                return;
            }
            response.getWriter().write("{\"message\":\"Notification marked as read\"}");
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Failed to update notification", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không cập nhật được thông báo.");
        }
    }

    private int extractInt(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("\\\"" + key + "\\\"\\s*:\\s*(\\d+)")
            .matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu hoặc sai trường " + key + ".");
        return Integer.parseInt(matcher.group(1));
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
