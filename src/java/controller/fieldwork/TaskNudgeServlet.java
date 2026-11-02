package controller.fieldwork;

import dal.TaskNudgeDAO;
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

@WebServlet(name = "TaskNudgeServlet", urlPatterns = {"/api/task-nudges", "/api/fieldwork/task-nudges"})
public class TaskNudgeServlet extends HttpServlet {

    private final TaskNudgeDAO nudgeDAO = new TaskNudgeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            List<Object[]> rows = nudgeDAO.getReporterTopicAssignments();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] r : rows) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("topicId", r[0]);
                map.put("topicCode", r[1]);
                map.put("topicTitle", r[2]);
                map.put("reporterId", r[3]);
                map.put("reporterName", r[4]);
                map.put("roleInTeam", r[5]);
                result.add(map);
            }

            response.getWriter().write(Json.toJson(result));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi lấy danh sách phân công: " + e.getMessage() + "\"}");
        }
    }

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
                response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để gửi lời nhắc.\"}");
                return;
            }

            int reporterUserId = -1;
            int topicId = -1;
            String title = null;
            String message = null;
            String urgency = null;

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
                if (json.contains("\"reporterUserId\"")) {
                    reporterUserId = Integer.parseInt(json.replaceAll(".*\"reporterUserId\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"topicId\"")) {
                    topicId = Integer.parseInt(json.replaceAll(".*\"topicId\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"title\"")) {
                    title = json.replaceAll(".*\"title\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                if (json.contains("\"message\"")) {
                    message = json.replaceAll(".*\"message\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                if (json.contains("\"urgency\"")) {
                    urgency = json.replaceAll(".*\"urgency\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
            }

            if (reporterUserId <= 0 && request.getParameter("reporterUserId") != null) {
                reporterUserId = Integer.parseInt(request.getParameter("reporterUserId"));
            }
            if (topicId <= 0 && request.getParameter("topicId") != null) {
                topicId = Integer.parseInt(request.getParameter("topicId"));
            }
            if (title == null) {
                title = request.getParameter("title");
            }
            if (message == null) {
                message = request.getParameter("message");
            }
            if (urgency == null) {
                urgency = request.getParameter("urgency");
            }

            if (urgency == null || urgency.trim().isEmpty()) {
                urgency = "NORMAL";
            }

            nudgeDAO.sendNudge(actorUserId, reporterUserId, topicId, title, message, urgency);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"success\":true,\"message\":\"Đã gửi lời nhắc đôn đốc thành công tới phóng viên.\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi gửi lời nhắc: " + e.getMessage() + "\"}");
        }
    }
}
