
package servlet;

import dao.FieldworkTaskDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.FieldworkTask;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/fieldwork-tasks")
public class FieldworkTaskServlet extends HttpServlet {

    private final FieldworkTaskDAO taskDAO = new FieldworkTaskDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<FieldworkTask> tasks = taskDAO.getAllTasks();
            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            for (FieldworkTask task : tasks) {
                if (!first) {
                    json.append(",");
                }
                first = false;

                json.append("{")
                    .append("\"taskId\":").append(task.getTaskId()).append(",")
                    .append("\"topicId\":").append(task.getTopicId()).append(",")
                    .append("\"assigneeUserId\":").append(task.getAssigneeUserId()).append(",")
                    .append("\"taskStatus\":\"")
                    .append(escapeJson(task.getTaskStatus())).append("\",")
                    .append("\"description\":");

                if (task.getDescription() == null) {
                    json.append("null");
                } else {
                    json.append("\"")
                        .append(escapeJson(task.getDescription()))
                        .append("\"");
                }

                json.append("}");
            }

            json.append("]");
            response.getWriter().write(json.toString());

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Failed to retrieve fieldwork tasks\"}"
            );
            getServletContext().log("Failed to retrieve fieldwork tasks", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder body = new StringBuilder();

        try (BufferedReader reader = request.getReader()) {
            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        try {
            String json = body.toString();

            int taskId = Integer.parseInt(
                json.replaceAll(
                    ".*\"taskId\"\\s*:\\s*(\\d+).*",
                    "$1"
                )
            );

            String status = json.replaceAll(
                ".*\"taskStatus\"\\s*:\\s*\"([^\"]+)\".*",
                "$1"
            );

            if (!"TODO".equals(status)
                    && !"IN_PROGRESS".equals(status)
                    && !"DONE".equals(status)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Invalid task status\"}"
                );
                return;
            }

            boolean updated = taskDAO.updateTaskStatus(taskId, status);

            if (!updated) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(
                    "{\"error\":\"Task not found\"}"
                );
                return;
            }

            response.getWriter().write(
                "{\"message\":\"Task status updated successfully\"}"
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Invalid request body\"}"
            );

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Failed to update task status\"}"
            );
            getServletContext().log("Failed to update task status", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder body = new StringBuilder();

        try (BufferedReader reader = request.getReader()) {
            String line;

            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        try {
            String json = body.toString();

            String topicValue = json.replaceAll(
                ".*\"topicId\"\\s*:\\s*(\\d+).*",
                "$1"
            );

            String assigneeValue = json.replaceAll(
                ".*\"assigneeUserId\"\\s*:\\s*(\\d+).*",
                "$1"
            );

            String description = json.replaceAll(
                ".*\"description\"\\s*:\\s*\"(.*?)\".*",
                "$1"
            );

            int topicId = Integer.parseInt(topicValue);
            int assigneeUserId = Integer.parseInt(assigneeValue);
            String actorValue = json.replaceAll(".*\"actorUserId\"\\s*:\\s*(\\d+).*", "$1");
            int actorUserId = Integer.parseInt(actorValue);

            if (description.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Description is required\"}"
                );
                return;
            }

            int taskId = taskDAO.createTask(
                actorUserId,
                topicId,
                assigneeUserId,
                description
            );

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(
                "{\"taskId\":" + taskId
                + ",\"message\":\"Task created successfully\"}"
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Invalid request body\"}"
            );

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Failed to create task\"}"
            );
            getServletContext().log("Failed to create task", e);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
