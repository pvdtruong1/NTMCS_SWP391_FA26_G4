package controller;

import dal.AuditLogDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.AuditLog;
import util.ApiUtil;
import util.FormUtil;
import util.Json;

/** JSON version of AuditLogServlet (UC-08). GET /api/audit-logs?keyword=  Read-only, newest 200. */
@WebServlet(name = "ApiAuditServlet", urlPatterns = {"/api/audit-logs"})
public class ApiAuditServlet extends HttpServlet {

    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (ApiUtil.requirePermission(request, response, "AUDIT_VIEW") == null) {
            return;
        }
        List<Object> logs = new ArrayList<>();
        for (AuditLog a : auditDAO.getRecent(FormUtil.text(request, "keyword"))) {
            logs.add(Json.obj("log_id", a.getLogId(),
                    "actor_user_id", a.getActorUserId(),
                    "actor_name", a.getActorName(),
                    "action", a.getAction(),
                    "entity_type", a.getEntityType(),
                    "entity_id", a.getEntityId(),
                    "details", a.getDetails(),
                    "created_at", ApiUtil.timeText(a.getCreatedAt())));
        }
        ApiUtil.send(response, 200, Json.obj("logs", logs));
    }
}
