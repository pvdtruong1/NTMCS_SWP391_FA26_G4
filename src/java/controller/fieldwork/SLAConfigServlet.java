package controller.fieldwork;

import dal.SLAConfigDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import model.SLAConfig;
import model.User;
import util.AuthUtil;
import util.Json;

@WebServlet(name = "SLAConfigServlet", urlPatterns = {"/api/sla-configs", "/api/fieldwork/sla-configs"})
public class SLAConfigServlet extends HttpServlet {

    private final SLAConfigDAO slaDAO = new SLAConfigDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            SLAConfig config = slaDAO.getConfig();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("inAppEnabled", config.isInAppEnabled());
            map.put("thresholdHours1", config.getThresholdHours1());
            map.put("thresholdHours2", config.getThresholdHours2());
            map.put("thresholdHours3", config.getThresholdHours3());
            map.put("earlyWarningTemplate", config.getEarlyWarningTemplate());
            map.put("warningTemplate", config.getWarningTemplate());
            map.put("urgentTemplate", config.getUrgentTemplate());
            map.put("nudgeTemplate", config.getNudgeTemplate());

            response.getWriter().write(Json.toJson(map));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi lấy cấu hình SLA: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        User loggedUser = AuthUtil.getLoggedUser(request);
        if (loggedUser != null && loggedUser.getRoleId() != 5 && loggedUser.getRoleId() != 7 && loggedUser.getRoleId() != 2) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Chỉ Điều phối viên Hiện trường, Trưởng ban Biên tập hoặc Quản trị hệ thống mới có quyền đổi cấu hình SLA.\"}");
            return;
        }

        try {
            boolean inAppEnabled = true;
            int thresholdHours1 = 24;
            int thresholdHours2 = 8;
            int thresholdHours3 = 2;
            String earlyWarningTemplate = null;
            String warningTemplate = null;
            String urgentTemplate = null;
            String nudgeTemplate = null;

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
                if (json.contains("\"inAppEnabled\"")) {
                    inAppEnabled = json.replaceAll(".*\"inAppEnabled\"\\s*:\\s*(true|false).*", "$1").equalsIgnoreCase("true");
                }
                if (json.contains("\"thresholdHours1\"")) {
                    thresholdHours1 = Integer.parseInt(json.replaceAll(".*\"thresholdHours1\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"thresholdHours2\"")) {
                    thresholdHours2 = Integer.parseInt(json.replaceAll(".*\"thresholdHours2\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"thresholdHours3\"")) {
                    thresholdHours3 = Integer.parseInt(json.replaceAll(".*\"thresholdHours3\"\\s*:\\s*(\\d+).*", "$1"));
                }
                if (json.contains("\"earlyWarningTemplate\"")) {
                    earlyWarningTemplate = json.replaceAll(".*\"earlyWarningTemplate\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                if (json.contains("\"warningTemplate\"")) {
                    warningTemplate = json.replaceAll(".*\"warningTemplate\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                if (json.contains("\"urgentTemplate\"")) {
                    urgentTemplate = json.replaceAll(".*\"urgentTemplate\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
                if (json.contains("\"nudgeTemplate\"")) {
                    nudgeTemplate = json.replaceAll(".*\"nudgeTemplate\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                }
            }

            if (request.getParameter("inAppEnabled") != null) {
                inAppEnabled = Boolean.parseBoolean(request.getParameter("inAppEnabled"));
            }
            if (request.getParameter("thresholdHours1") != null) {
                thresholdHours1 = Integer.parseInt(request.getParameter("thresholdHours1"));
            }
            if (request.getParameter("thresholdHours2") != null) {
                thresholdHours2 = Integer.parseInt(request.getParameter("thresholdHours2"));
            }
            if (request.getParameter("thresholdHours3") != null) {
                thresholdHours3 = Integer.parseInt(request.getParameter("thresholdHours3"));
            }
            if (earlyWarningTemplate == null && request.getParameter("earlyWarningTemplate") != null) {
                earlyWarningTemplate = request.getParameter("earlyWarningTemplate");
            }
            if (warningTemplate == null && request.getParameter("warningTemplate") != null) {
                warningTemplate = request.getParameter("warningTemplate");
            }
            if (urgentTemplate == null && request.getParameter("urgentTemplate") != null) {
                urgentTemplate = request.getParameter("urgentTemplate");
            }
            if (nudgeTemplate == null && request.getParameter("nudgeTemplate") != null) {
                nudgeTemplate = request.getParameter("nudgeTemplate");
            }

            SLAConfig config = new SLAConfig(
                    inAppEnabled, thresholdHours1, thresholdHours2, thresholdHours3,
                    earlyWarningTemplate, warningTemplate, urgentTemplate, nudgeTemplate
            );

            slaDAO.saveConfig(config);

            response.getWriter().write("{\"success\":true,\"message\":\"Lưu cấu hình SLA thành công!\"}");

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi lưu cấu hình SLA: " + e.getMessage() + "\"}");
        }
    }
}
