package controller;

import dal.AuditLogDAO;
import dal.DeskDAO;
import dal.DeskQuotaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Desk;
import model.DeskQuota;
import model.User;
import util.ApiUtil;
import util.FormUtil;
import util.Json;
import util.QuotaValidator;

/**
 * JSON version of QuotaServlet (UC-02, with UC-09). The rules are the same as QuotaServlet.
 *   POST /api/quotas  action=create | update | delete
 *     create : desk_id, period, quota
 *     update : id, quota          (only the number can change)
 *     delete : id
 */
@WebServlet(name = "ApiQuotaServlet", urlPatterns = {"/api/quotas"})
public class ApiQuotaServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final DeskQuotaDAO quotaDAO = new DeskQuotaDAO();
    private final DeskDAO deskDAO = new DeskDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User admin = ApiUtil.requirePermission(request, response, PERMISSION);
        if (admin == null) {
            return;
        }
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            createQuota(request, response, admin);
        } else if ("update".equals(action)) {
            updateQuota(request, response, admin);
        } else if ("delete".equals(action)) {
            deleteQuota(request, response, admin);
        } else {
            ApiUtil.error(response, 400, "Unknown action.");
        }
    }

    // ---------------------------------------------------------------- create
    private void createQuota(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        DeskQuota quota = new DeskQuota();
        quota.setDeskId(FormUtil.toInt(request.getParameter("desk_id")));
        quota.setPeriod(QuotaValidator.normalizePeriod(FormUtil.text(request, "period")));
        String countText = FormUtil.text(request, "quota");

        String error = null;
        Desk desk = deskDAO.getById(quota.getDeskId());
        if (desk == null) {
            error = "Please choose a desk.";
        } else {
            error = QuotaValidator.validate(quota.getPeriod(), countText);   // UC-09
        }
        if (error == null && quotaDAO.find(quota.getDeskId(), quota.getPeriod()) != null) {
            error = "This desk already has a quota for that period. Edit the existing one instead.";
        }
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }

        quota.setQuotaCount(Integer.parseInt(countText));
        int newId = quotaDAO.insert(quota);
        if (newId == 0) {
            ApiUtil.error(response, 500, "Could not save the quota. Please try again.");
            return;
        }

        auditDAO.log(admin.getUserId(), "CREATE_QUOTA", "DESK_QUOTA", newId,
                "Set quota " + quota.getQuotaCount() + " for " + desk.getDeskName()
                + " in " + quota.getPeriod());
        ApiUtil.send(response, 200, Json.obj("ok", true, "quota_id", newId));
    }

    // ---------------------------------------------------------------- update
    private void updateQuota(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        DeskQuota quota = quotaDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (quota == null) {
            ApiUtil.error(response, 404, "Quota not found.");
            return;
        }
        String countText = FormUtil.text(request, "quota");

        String error = QuotaValidator.validateCount(countText);   // UC-09
        if (error != null) {
            ApiUtil.error(response, 400, error);
            return;
        }

        int newCount = Integer.parseInt(countText);
        int oldCount = quota.getQuotaCount();
        if (newCount == oldCount) {
            ApiUtil.send(response, 200, Json.obj("ok", true, "changed", false));
            return;
        }
        if (!quotaDAO.updateCount(quota.getQuotaId(), newCount)) {
            ApiUtil.error(response, 500, "Could not save the quota. Please try again.");
            return;
        }

        auditDAO.log(admin.getUserId(), "UPDATE_QUOTA", "DESK_QUOTA", quota.getQuotaId(),
                "Changed quota of " + quota.getDeskName() + " in " + quota.getPeriod()
                + " from " + oldCount + " to " + newCount);
        ApiUtil.send(response, 200, Json.obj("ok", true, "changed", true));
    }

    // ---------------------------------------------------------------- delete
    private void deleteQuota(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        DeskQuota quota = quotaDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (quota != null && quotaDAO.delete(quota.getQuotaId())) {
            auditDAO.log(admin.getUserId(), "DELETE_QUOTA", "DESK_QUOTA", quota.getQuotaId(),
                    "Deleted quota " + quota.getQuotaCount() + " of " + quota.getDeskName()
                    + " in " + quota.getPeriod());
        }
        ApiUtil.send(response, 200, Json.obj("ok", true));
    }
}
