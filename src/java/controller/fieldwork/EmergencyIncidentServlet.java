package controller.fieldwork;

import dal.EmergencyIncidentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import util.AuthUtil;

@WebServlet(name = "EmergencyIncidentServlet", urlPatterns = {"/api/emergency-incidents", "/api/fieldwork/emergency-incidents"})
public class EmergencyIncidentServlet extends HttpServlet {

    private final EmergencyIncidentDAO incidentDAO = new EmergencyIncidentDAO();

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
                response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để báo cáo sự cố khẩn cấp.\"}");
                return;
            }

            int topicId = -1;
            String incidentDescription = null;

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
                if (json.contains("\"topicId\"")) {
                    topicId = Integer.parseInt(json.replaceAll(".*\"topicId\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"incidentDescription\"")) {
                    incidentDescription = json.replaceAll(".*\"incidentDescription\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
            }

            if (topicId <= 0) {
                String tp = request.getParameter("topicId");
                if (tp != null && !tp.trim().isEmpty()) {
                    topicId = Integer.parseInt(tp.trim());
                }
            }
            if (incidentDescription == null) {
                incidentDescription = request.getParameter("incidentDescription");
            }

            incidentDAO.reportEmergencyIncident(topicId, incidentDescription, actorUserId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"success\":true,\"message\":\"Báo cáo sự cố khẩn cấp thành công và đã gửi cảnh báo tới Trưởng ban / Điều phối viên.\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi xử lý sự cố khẩn cấp: " + e.getMessage() + "\"}");
        }
    }
}
