package servlet;

import dao.TaskNudgeDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/task-nudges")
public class TaskNudgeServlet extends HttpServlet {
    private final TaskNudgeDAO taskNudgeDAO = new TaskNudgeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepare(response);
        try {
            List<Object[]> rows = taskNudgeDAO.getReporterTopicAssignments();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < rows.size(); i++) {
                Object[] row = rows.get(i);
                if (i > 0) json.append(',');
                json.append('{')
                    .append("\"topicId\":").append(row[0]).append(',')
                    .append("\"topicCode\":"); appendNullableString(json, (String) row[1]);
                json.append(',').append("\"topicTitle\":\"").append(escape((String) row[2])).append("\",")
                    .append("\"reporterId\":").append(row[3]).append(',')
                    .append("\"reporterName\":\"").append(escape((String) row[4])).append("\",")
                    .append("\"roleInTeam\":\"").append(escape((String) row[5])).append("\"}");
            }
            json.append(']');
            response.getWriter().write(json.toString());
        } catch (SQLException e) {
            getServletContext().log("Failed to load task nudge recipients", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không tải được danh sách phóng viên và đề tài.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        prepare(response);
        try {
            String body = readBody(request);
            int actorUserId = extractInt(body, "actorUserId");
            int reporterUserId = extractInt(body, "reporterUserId");
            int topicId = extractInt(body, "topicId");
            String title = extractString(body, "title");
            String message = extractString(body, "message");
            String urgency = extractString(body, "urgency");
            if (title.trim().isEmpty() || title.length() > 180) throw new IllegalArgumentException("Tiêu đề phải có từ 1 đến 180 ký tự.");
            if (message.trim().isEmpty() || message.length() > 4000) throw new IllegalArgumentException("Nội dung phải có từ 1 đến 4000 ký tự.");
            taskNudgeDAO.sendNudge(actorUserId, reporterUserId, topicId, title, message, urgency);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\":\"Đã gửi lời nhắc thành công.\"}");
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Failed to send task nudge", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không gửi được lời nhắc. Vui lòng thử lại.");
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line; while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private int extractInt(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(\\d+)").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu hoặc sai trường " + key + ".");
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Giá trị " + key + " không hợp lệ."); }
    }

    private String extractString(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu trường " + key + ".");
        return unescape(matcher.group(1));
    }

    private String unescape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) { out.append(c); continue; }
            char n = value.charAt(++i);
            switch (n) {
                case '"': out.append('"'); break; case '\\': out.append('\\'); break;
                case 'n': out.append('\n'); break; case 'r': out.append('\r'); break;
                case 't': out.append('\t'); break; default: throw new IllegalArgumentException("Chuỗi JSON không hợp lệ.");
            }
        }
        return out.toString();
    }

    private void appendNullableString(StringBuilder json, String value) {
        if (value == null) json.append("null");
        else json.append('"').append(escape(value)).append('"');
    }
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    private void prepare(HttpServletResponse response) {
        response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
    }
    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status); response.getWriter().write("{\"error\":\"" + escape(message) + "\"}");
    }
}
