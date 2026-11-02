package servlet;

import dao.BroadcastAuditDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/broadcast-audit/ping")
public class BroadcastAuditPingServlet extends HttpServlet {
    private final BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            int actorUserId = readInt(body.toString(), "actorUserId");
            int notificationId = readInt(body.toString(), "notificationId");
            int recipientId = readInt(body.toString(), "recipientId");
            auditDAO.pingUnreadRecipient(actorUserId, notificationId, recipientId);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\":\"Đã Ping người nhận chưa đọc.\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (SQLException e) {
            getServletContext().log("Failed to ping unread notification recipient", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Không thể Ping người nhận.\"}");
        }
    }

    private int readInt(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu trường " + key + ".");
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Giá trị " + key + " không hợp lệ."); }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
