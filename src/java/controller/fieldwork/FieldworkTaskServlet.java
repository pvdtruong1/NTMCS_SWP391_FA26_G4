package controller.fieldwork;

import dal.FieldworkTaskDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.FieldworkTask;
import model.User;
import util.AuthUtil;
import util.Json;

@WebServlet(name = "FieldworkTaskServlet", urlPatterns = {"/api/fieldwork-tasks", "/api/fieldwork/tasks"})
public class FieldworkTaskServlet extends HttpServlet {

    private final FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoggedUser(request);
        String topicIdParam = request.getParameter("topicId");
        String userIdParam = request.getParameter("userId");

        try {
            List<FieldworkTask> tasks;
            if (topicIdParam != null && !topicIdParam.trim().isEmpty()) {
                tasks = taskDAO.getTasksByTopicId(Integer.parseInt(topicIdParam.trim()));
            } else if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                tasks = taskDAO.getTasksByAssignee(Integer.parseInt(userIdParam.trim()));
            } else if (user != null && user.getRoleId() == 1) {
                tasks = taskDAO.getTasksByAssignee(user.getUserId());
            } else {
                tasks = taskDAO.getAllTasks();
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (FieldworkTask t : tasks) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("taskId", t.getTaskId());
                map.put("topicId", t.getTopicId());
                map.put("topicCode", t.getTopicCode());
                map.put("topicTitle", t.getTopicTitle());
                map.put("assigneeUserId", t.getAssigneeUserId());
                map.put("assigneeName", t.getAssigneeName());
                map.put("assigneeEmail", t.getAssigneeEmail());
                map.put("taskStatus", t.getTaskStatus());
                map.put("description", t.getDescription());
                list.add(map);
            }
            response.getWriter().write(Json.toJson(list));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi truy xuất danh sách nhiệm vụ: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line).append("\n");
            }
        }

        try {
            String json = body.toString();
            int taskId = -1;
            String status = null;

            Matcher mTaskId = Pattern.compile("\"taskId\"\\s*:\\s*(\\d+)").matcher(json);
            if (mTaskId.find()) {
                taskId = Integer.parseInt(mTaskId.group(1));
            }
            Matcher mStatus = Pattern.compile("\"taskStatus\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (mStatus.find()) {
                status = mStatus.group(1).trim();
            }

            if (taskId <= 0 && request.getParameter("taskId") != null) {
                taskId = Integer.parseInt(request.getParameter("taskId").trim());
            }
            if (status == null && request.getParameter("taskStatus") != null) {
                status = request.getParameter("taskStatus").trim();
            }

            User loggedUser = AuthUtil.getLoggedUser(request);
            int actorUserId = (loggedUser != null) ? loggedUser.getUserId() : -1;

            boolean ok = taskDAO.updateTaskStatus(taskId, status, actorUserId);
            if (ok) {
                response.getWriter().write("{\"success\":true,\"message\":\"Cập nhật trạng thái thành công\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Không tìm thấy nhiệm vụ #" + taskId + "\"}");
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi cập nhật: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.getLoggedUser(request);
        int actorUserId = (user != null) ? user.getUserId() : 4;

        try {
            int topicId = -1;
            int assigneeUserId = -1;
            String description = null;

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line).append("\n");
                    }
                }
                String json = body.toString();

                Matcher mTopic = Pattern.compile("\"topicId\"\\s*:\\s*(\\d+)").matcher(json);
                if (mTopic.find()) {
                    topicId = Integer.parseInt(mTopic.group(1));
                }
                Matcher mUser = Pattern.compile("\"assigneeUserId\"\\s*:\\s*(\\d+)").matcher(json);
                if (mUser.find()) {
                    assigneeUserId = Integer.parseInt(mUser.group(1));
                }
                Matcher mDesc = Pattern.compile("\"description\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"").matcher(json);
                if (mDesc.find()) {
                    description = mDesc.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
                }
            }

            if (topicId <= 0 && request.getParameter("topicId") != null) {
                topicId = Integer.parseInt(request.getParameter("topicId").trim());
            }
            if (assigneeUserId <= 0 && request.getParameter("assigneeUserId") != null) {
                assigneeUserId = Integer.parseInt(request.getParameter("assigneeUserId").trim());
            }
            if (description == null && request.getParameter("description") != null) {
                description = request.getParameter("description");
            }

            int taskId = taskDAO.createTask(actorUserId, topicId, assigneeUserId, description);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"taskId\":" + taskId + ",\"message\":\"Tạo nhiệm vụ thành công\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Lỗi tạo nhiệm vụ: " + e.getMessage() + "\"}");
        }
    }
}
