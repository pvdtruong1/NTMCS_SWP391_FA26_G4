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
import util.AuthUtil;
import util.FormUtil;
import util.QuotaValidator;

/**
 * UC-02 Manage Desk Quota (SCR-5.2), with UC-09 Validate Quota Input.
 *   GET  /quota                 -> list of quotas
 *   GET  /quota?action=new      -> empty form
 *   GET  /quota?action=edit&id= -> form for one quota (only the number can change)
 *   POST /quota  action=create | update | delete
 */
@WebServlet(name = "QuotaServlet", urlPatterns = {"/quota"})
public class QuotaServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final DeskQuotaDAO quotaDAO = new DeskQuotaDAO();
    private final DeskDAO deskDAO = new DeskDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("new".equals(action)) {
            showForm(request, response, new DeskQuota(), "");
        } else if ("edit".equals(action)) {
            DeskQuota quota = quotaDAO.getById(FormUtil.toInt(request.getParameter("id")));
            if (quota == null) {
                response.sendRedirect(request.getContextPath() + "/quota");
                return;
            }
            showForm(request, response, quota, String.valueOf(quota.getQuotaCount()));
        } else {
            request.setAttribute("quotaList", quotaDAO.getAll());
            request.getRequestDispatcher("/WEB-INF/views/quota.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            createQuota(request, response);
        } else if ("update".equals(action)) {
            updateQuota(request, response);
        } else if ("delete".equals(action)) {
            deleteQuota(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/quota");
        }
    }

    // ---------------------------------------------------------------- create
    private void createQuota(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);

        DeskQuota quota = new DeskQuota();
        quota.setDeskId(FormUtil.toInt(request.getParameter("deskId")));
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
            request.setAttribute("error", error);
            showForm(request, response, quota, countText);
            return;
        }

        quota.setQuotaCount(Integer.parseInt(countText));
        int newId = quotaDAO.insert(quota);
        if (newId == 0) {
            request.setAttribute("error", "Could not save the quota. Please try again.");
            showForm(request, response, quota, countText);
            return;
        }

        auditDAO.log(admin.getUserId(), "CREATE_QUOTA", "DESK_QUOTA", newId,
                "Set quota " + quota.getQuotaCount() + " for " + desk.getDeskName()
                + " in " + quota.getPeriod());
        response.sendRedirect(request.getContextPath() + "/quota?msg=created");
    }

    // ---------------------------------------------------------------- update
    private void updateQuota(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User admin = AuthUtil.getLoggedUser(request);

        DeskQuota quota = quotaDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (quota == null) {
            response.sendRedirect(request.getContextPath() + "/quota");
            return;
        }
        String countText = FormUtil.text(request, "quota");

        String error = QuotaValidator.validateCount(countText);   // UC-09
        if (error != null) {
            request.setAttribute("error", error);
            showForm(request, response, quota, countText);
            return;
        }

        int newCount = Integer.parseInt(countText);
        int oldCount = quota.getQuotaCount();
        if (newCount == oldCount) {
            response.sendRedirect(request.getContextPath() + "/quota?msg=nochange");
            return;
        }
        if (!quotaDAO.updateCount(quota.getQuotaId(), newCount)) {
            request.setAttribute("error", "Could not save the quota. Please try again.");
            showForm(request, response, quota, countText);
            return;
        }

        auditDAO.log(admin.getUserId(), "UPDATE_QUOTA", "DESK_QUOTA", quota.getQuotaId(),
                "Changed quota of " + quota.getDeskName() + " in " + quota.getPeriod()
                + " from " + oldCount + " to " + newCount);
        response.sendRedirect(request.getContextPath() + "/quota?msg=updated");
    }

    // ---------------------------------------------------------------- delete
    private void deleteQuota(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User admin = AuthUtil.getLoggedUser(request);

        DeskQuota quota = quotaDAO.getById(FormUtil.toInt(request.getParameter("id")));
        if (quota != null && quotaDAO.delete(quota.getQuotaId())) {
            auditDAO.log(admin.getUserId(), "DELETE_QUOTA", "DESK_QUOTA", quota.getQuotaId(),
                    "Deleted quota " + quota.getQuotaCount() + " of " + quota.getDeskName()
                    + " in " + quota.getPeriod());
        }
        response.sendRedirect(request.getContextPath() + "/quota?msg=deleted");
    }

    // ---------------------------------------------------------------- helper
    private void showForm(HttpServletRequest request, HttpServletResponse response,
            DeskQuota quota, String countText) throws ServletException, IOException {
        request.setAttribute("quota", quota);
        request.setAttribute("countText", countText);
        request.setAttribute("deskList", deskDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/quota-form.jsp").forward(request, response);
    }
}
