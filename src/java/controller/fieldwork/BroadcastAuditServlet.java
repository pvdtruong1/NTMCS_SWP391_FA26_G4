package controller.fieldwork;

import dal.BroadcastAuditDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.User;
import util.AuthUtil;
import util.Json;

@WebServlet(name = "BroadcastAuditServlet", urlPatterns = {"/api/broadcast-audits", "/api/fieldwork/broadcast-audits"})
public class BroadcastAuditServlet extends HttpServlet {

    private final BroadcastAuditDAO auditDAO = new BroadcastAuditDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            User loggedUser = AuthUtil.getLoggedUser(request);
            int viewerUserId;
            if (loggedUser != null) {
                viewerUserId = loggedUser.getUserId();
            } else if (request.getParameter("viewerUserId") != null) {
                viewerUserId = Integer.parseInt(request.getParameter("viewerUserId"));
            } else {
                viewerUserId = 4; // default coordinator if testing
            }

            List<Object[]> rows = auditDAO.getFieldworkNotificationAudit(viewerUserId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] r : rows) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("notificationId", r[0]);
                map.put("recipientId", r[1]);
                map.put("recipientName", r[2]);
                map.put("recipientEmail", r[3]);
                map.put("recipientDesk", r[4]);
                map.put("topicId", r[5]);
                map.put("topicCode", r[6]);
                map.put("topicTitle", r[7]);
                map.put("message", r[8]);
                map.put("isRead", r[9]);
                map.put("senderId", r[10]);
                map.put("senderName", r[11]);
                map.put("sentAt", r[12] != null ? r[12].toString() : null);
                map.put("readAt", r[13] != null ? r[13].toString() : null);
                map.put("sourceType", r[14]);
                map.put("urgency", r[15]);
                result.add(map);
            }

            response.getWriter().write(Json.toJson(result));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi truy vấn nhật ký kiểm toán: " + e.getMessage() + "\"}");
        }
    }
}
