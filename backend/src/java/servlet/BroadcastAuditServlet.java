package servlet;

import dao.BroadcastAuditDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/broadcast-audit")
public class BroadcastAuditServlet extends HttpServlet {
    private final BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            String value = request.getParameter("userId");
            if (value == null || value.trim().isEmpty()) {
                writeError(response, 400, "Thiếu userId.");
                return;
            }
            int userId = Integer.parseInt(value);
            List<Object[]> rows = auditDAO.getFieldworkNotificationAudit(userId);
            StringBuilder json = new StringBuilder("{\"items\":[");
            for (int i = 0; i < rows.size(); i++) {
                Object[] r = rows.get(i);
                if (i > 0) json.append(',');
                json.append('{')
                    .append("\"notificationId\":").append(r[0]).append(',')
                    .append("\"recipientId\":").append(r[1]).append(',')
                    .append("\"recipientName\":\"").append(escape(r[2])).append("\",")
                    .append("\"recipientEmail\":\"").append(escape(r[3])).append("\",")
                    .append("\"recipientDesk\":\"").append(escape(r[4])).append("\",")
                    .append("\"topicId\":").append(r[5] == null ? "null" : r[5]).append(',')
                    .append("\"topicCode\":\"").append(escape(r[6])).append("\",")
                    .append("\"topicTitle\":\"").append(escape(r[7])).append("\",")
                    .append("\"message\":\"").append(escape(r[8])).append("\",")
                    .append("\"isRead\":").append(r[9]).append(',')
                    .append("\"senderId\":").append(r[10]).append(',')
                    .append("\"senderName\":\"").append(escape(r[11])).append("\",")
                    .append("\"sentAt\":\"").append(escape(r[12])).append("\",")
                    .append("\"readAt\":");
                if (r[13] == null) json.append("null");
                else json.append('"').append(escape(r[13])).append('"');
                json.append(',').append("\"urgency\":\"").append(escape(r[14])).append("\",")
                    .append("\"sourceType\":\"").append(escape(r[15])).append("\"}");
            }
            json.append("],\"count\":").append(rows.size()).append('}');
            response.getWriter().write(json.toString());
        } catch (NumberFormatException e) {
            writeError(response, 400, "userId không hợp lệ.");
        } catch (IllegalArgumentException e) {
            writeError(response, 403, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Failed to retrieve WF-04 notification audit", e);
            writeError(response, 500, "Không tải được nhật ký thông báo.");
        }
    }

    private String escape(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"error\":\"" + escape(message) + "\"}");
    }
}
