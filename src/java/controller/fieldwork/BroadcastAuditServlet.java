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

            String export = request.getParameter("export");
            if ("csv".equalsIgnoreCase(export)) {
                exportCsv(response, rows);
                return;
            }

            response.setContentType("application/json;charset=UTF-8");
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
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Lỗi truy vấn nhật ký kiểm toán: " + e.getMessage() + "\"}");
        }
    }

    private void exportCsv(HttpServletResponse response, List<Object[]> rows) throws IOException {
        String filename = "NTMCS_Notification_Audit_" + java.time.LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        java.io.OutputStream out = response.getOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8))) {
            writer.println("Mã thông báo,Loại nguồn,Mức độ,Người gửi,Người nhận,Email người nhận,Phòng ban,Mã đề tài,Tên đề tài,Nội dung thông báo,Thời gian gửi,Trạng thái,Thời gian đọc");

            for (Object[] r : rows) {
                String notifId = r[0] != null ? r[0].toString() : "";
                String recipientName = r[2] != null ? r[2].toString() : "";
                String recipientEmail = r[3] != null ? r[3].toString() : "";
                String recipientDesk = r[4] != null ? r[4].toString() : "";
                String topicCode = r[6] != null ? r[6].toString() : "";
                String topicTitle = r[7] != null ? r[7].toString() : "";
                String message = r[8] != null ? r[8].toString() : "";
                boolean isRead = Boolean.TRUE.equals(r[9]);
                String senderName = r[11] != null ? r[11].toString() : "Hệ thống";
                String sentAt = r[12] != null ? r[12].toString() : "";
                String readAt = r[13] != null ? r[13].toString() : "";
                String sourceType = r[14] != null ? r[14].toString() : "";
                String urgency = r[15] != null ? r[15].toString() : "";

                writer.println(String.join(",",
                    csvEscape(notifId),
                    csvEscape(sourceType),
                    csvEscape(urgency),
                    csvEscape(senderName),
                    csvEscape(recipientName),
                    csvEscape(recipientEmail),
                    csvEscape(recipientDesk),
                    csvEscape(topicCode),
                    csvEscape(topicTitle),
                    csvEscape(message),
                    csvEscape(sentAt),
                    csvEscape(isRead ? "Đã đọc" : "Chưa đọc"),
                    csvEscape(readAt)
                ));
            }
            writer.flush();
        }
    }

    private String csvEscape(String val) {
        if (val == null) return "\"\"";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
