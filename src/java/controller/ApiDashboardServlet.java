package controller;

import dal.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.User;
import util.ApiUtil;
import util.FormUtil;
import util.QuotaValidator;

/**
 * JSON version of DashboardServlet (UC-34, SCR-5.1).
 *   GET /api/dashboard?period=2026-10   (empty period = this month)
 */
@WebServlet(name = "ApiDashboardServlet", urlPatterns = {"/api/dashboard"})
public class ApiDashboardServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = ApiUtil.requirePermission(request, response, PERMISSION);
        if (user == null) {
            return;
        }
        String period = QuotaValidator.normalizePeriod(FormUtil.text(request, "period"));
        if (period.isEmpty()) {
            period = DashboardServlet.currentMonth();
        } else if (!QuotaValidator.isValidPeriod(period)) {
            ApiUtil.error(response, 400, "Period must look like 2026-10 (a month) or 2026-Q4 (a quarter).");
            return;
        }
        ApiUtil.send(response, 200, reportDAO.getDashboard(period,
                QuotaValidator.periodStart(period), QuotaValidator.periodEnd(period)));
    }
}
