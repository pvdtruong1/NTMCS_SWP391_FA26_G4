package servlet;

import dao.EmergencyIncidentDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/emergency-incidents")
public class EmergencyIncidentServlet extends HttpServlet {

    private final EmergencyIncidentDAO incidentDAO =
            new EmergencyIncidentDAO();

    private static final Pattern TOPIC_ID_PATTERN =
            Pattern.compile("\"topicId\"\\s*:\\s*(\\d+)");

    private static final Pattern ACTOR_ID_PATTERN =
            Pattern.compile("\"actorUserId\"\\s*:\\s*(\\d+)");

    private static final Pattern DESCRIPTION_PATTERN =
            Pattern.compile("\"incidentDescription\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            StringBuilder body = new StringBuilder();

            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            String json = body.toString();

            int topicId = parsePositiveInt(
                    extract(TOPIC_ID_PATTERN, json), "topicId");

            int actorUserId = parsePositiveInt(
                    extract(ACTOR_ID_PATTERN, json), "actorUserId");

            String incidentDescription =
                    unescapeJsonString(extract(DESCRIPTION_PATTERN, json)).trim();

            if (incidentDescription.isEmpty()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Vui lòng nhập nội dung sự cố.");
                return;
            }

            incidentDAO.reportEmergencyIncident(
                    topicId,
                    incidentDescription,
                    actorUserId
            );

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(
                    "{\"message\":\"Báo cáo sự cố khẩn cấp thành công\","
                    + "\"topicId\":" + topicId
                    + ",\"notificationsSent\":2}"
            );

        } catch (IllegalArgumentException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage() == null
                            ? "Dữ liệu gửi lên không hợp lệ."
                            : ex.getMessage());

        } catch (SQLException ex) {
            getServletContext().log(
                    "Failed to save emergency incident report.", ex);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể lưu báo cáo sự cố. Vui lòng kiểm tra log Backend.");
        }
    }

    private String extract(Pattern pattern, String json) {
        Matcher matcher = pattern.matcher(json == null ? "" : json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Thiếu hoặc sai dữ liệu gửi lên.");
        }
        return matcher.group(1);
    }

    private int parsePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }

    private String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);

            if (current != '\\') {
                result.append(current);
                continue;
            }

            if (++i >= value.length()) {
                throw new IllegalArgumentException("Nội dung báo cáo không hợp lệ.");
            }

            char escaped = value.charAt(i);
            switch (escaped) {
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '/':
                    result.append('/');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (i + 4 >= value.length()) {
                        throw new IllegalArgumentException(
                                "Nội dung báo cáo không hợp lệ.");
                    }
                    String hex = value.substring(i + 1, i + 5);
                    try {
                        result.append((char) Integer.parseInt(hex, 16));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException(
                                "Nội dung báo cáo không hợp lệ.");
                    }
                    i += 4;
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Nội dung báo cáo không hợp lệ.");
            }
        }

        return result.toString();
    }

    private void writeError(HttpServletResponse response,
                            int status,
                            String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(
                "{\"error\":\"" + escapeJson(message) + "\"}");
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
