package servlet;

import dao.SLAConfigDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.SLAConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/sla-config")
public class SLAConfigServlet extends HttpServlet {
    private final SLAConfigDAO configDAO = new SLAConfigDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        prepareJson(response);
        try {
            writeConfig(response, configDAO.getConfig());
        } catch (Exception e) {
            getServletContext().log("Failed to load SLA configuration", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                       "Không tải được cấu hình SLA.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        prepareJson(response);
        try {
            String body = readBody(request);
            SLAConfig config = new SLAConfig(
                extractBoolean(body, "inAppEnabled"),
                extractInt(body, "thresholdHours1"),
                extractInt(body, "thresholdHours2"),
                extractInt(body, "thresholdHours3"),
                extractString(body, "earlyWarningTemplate"),
                extractString(body, "warningTemplate"),
                extractString(body, "urgentTemplate"),
                extractString(body, "nudgeTemplate")
            );
            config.validate();
            configDAO.saveConfig(config);
            response.getWriter().write("{\"message\":\"Đã lưu cấu hình SLA.\"}");
        } catch (IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Failed to save SLA configuration", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                       "Không lưu được cấu hình SLA.");
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private int extractInt(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key)
            + "\\\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu hoặc sai trường " + key + ".");
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Giá trị " + key + " không hợp lệ.");
        }
    }

    private boolean extractBoolean(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key)
            + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE).matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu hoặc sai trường " + key + ".");
        return Boolean.parseBoolean(matcher.group(1));
    }

    private String extractString(String json, String key) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key)
            + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(json);
        if (!matcher.find()) throw new IllegalArgumentException("Thiếu trường " + key + ".");
        return unescapeJson(matcher.group(1));
    }

    private String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) {
                result.append(c);
                continue;
            }
            char escaped = value.charAt(++i);
            switch (escaped) {
                case '"': result.append('"'); break;
                case '\\': result.append('\\'); break;
                case '/': result.append('/'); break;
                case 'b': result.append('\b'); break;
                case 'f': result.append('\f'); break;
                case 'n': result.append('\n'); break;
                case 'r': result.append('\r'); break;
                case 't': result.append('\t'); break;
                case 'u':
                    if (i + 4 >= value.length()) throw new IllegalArgumentException("Chuỗi JSON Unicode không hợp lệ.");
                    try {
                        result.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
                        i += 4;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Chuỗi JSON Unicode không hợp lệ.");
                    }
                    break;
                default: throw new IllegalArgumentException("Chuỗi JSON có escape không hợp lệ.");
            }
        }
        return result.toString();
    }

    private void writeConfig(HttpServletResponse response, SLAConfig config) throws IOException {
        StringBuilder json = new StringBuilder("{");
        json.append("\"inAppEnabled\":").append(config.isInAppEnabled()).append(',')
            .append("\"thresholdHours1\":").append(config.getThresholdHours1()).append(',')
            .append("\"thresholdHours2\":").append(config.getThresholdHours2()).append(',')
            .append("\"thresholdHours3\":").append(config.getThresholdHours3()).append(',')
            .append("\"earlyWarningTemplate\":\"").append(escapeJson(config.getEarlyWarningTemplate())).append("\",")
            .append("\"warningTemplate\":\"").append(escapeJson(config.getWarningTemplate())).append("\",")
            .append("\"urgentTemplate\":\"").append(escapeJson(config.getUrgentTemplate())).append("\",")
            .append("\"nudgeTemplate\":\"").append(escapeJson(config.getNudgeTemplate())).append("\"")
            .append('}');
        response.getWriter().write(json.toString());
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private void prepareJson(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
