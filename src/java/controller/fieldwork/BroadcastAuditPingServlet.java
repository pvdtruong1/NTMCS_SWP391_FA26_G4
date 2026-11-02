package controller.fieldwork;

import dal.BroadcastAuditDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import model.User;
import util.AuthUtil;

@WebServlet(name = "BroadcastAuditPingServlet", urlPatterns = {"/api/broadcast-audits/ping", "/api/fieldwork/broadcast-audits/ping"})
public class BroadcastAuditPingServlet extends HttpServlet {

    private final BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            User loggedUser = AuthUtil.getLoggedUser(request);
            int actorUserId;
            if (loggedUser != null) {
                actorUserId = loggedUser.getUserId();
            } else if (request.getParameter("actorUserId") != null) {
                actorUserId = Integer.parseInt(request.getParameter("actorUserId"));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để Ping nhắc việc.\"}");
                return;
            }

            int originalNotificationId = -1;
            int recipientUserId = -1;

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                StringBuilder body = new StringBuilder();
                try (java.io.BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }
                String json = body.toString();
                if (json.contains("\"originalNotificationId\"")) {
                    originalNotificationId = Integer.parseInt(json.replaceAll(".*\"originalNotificationId\"\\s*:\\s*(\\d+).*", "$1"));
                } else if (json.contains("\"notificationId\"")) {
                    originalNotificationId = Integer.parseInt(json.replaceAll(".*\"notificationId\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"recipientUserId\"")) {
                    recipientUserId = Integer.parseInt(json.replaceAll(".*\"recipientUserId\"\\s*:\\s*(\\d+).*", "$1"));
                } else if (json.contains("\"recipientId\"")) {
                    recipientUserId = Integer.parseInt(json.replaceAll(".*\"recipientId\"\\s*:\\s*(\\d+).*", "$1"));
                }
            }

            if (originalNotificationId <= 0 && request.getParameter("originalNotificationId") != null) {
                originalNotificationId = Integer.parseInt(request.getParameter("originalNotificationId"));
            } else if (originalNotificationId <= 0 && request.getParameter("notificationId") != null) {
                originalNotificationId = Integer.parseInt(request.getParameter("notificationId"));
            }

            if (recipientUserId <= 0 && request.getParameter("recipientUserId") != null) {
                recipientUserId = Integer.parseInt(request.getParameter("recipientUserId"));
            } else if (recipientUserId <= 0 && request.getParameter("recipientId") != null) {
                recipientUserId = Integer.parseInt(request.getParameter("recipientId"));
            }

            auditDAO.pingUnreadRecipient(actorUserId, originalNotificationId, recipientUserId);

            response.getWriter().write("{\"success\":true,\"message\":\"Đã gửi Ping đôn đốc nhắc đọc thông báo thành công!\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi gửi Ping: " + e.getMessage() + "\"}");
        }
    }
}
