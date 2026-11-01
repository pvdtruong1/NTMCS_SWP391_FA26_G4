package controller;

import dal.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import util.AuthUtil;
import util.FormUtil;
import util.QuotaValidator;

/**
 * UC-34 View Executive Dashboard (SCR-5.1): the whole newsroom at a glance.
 * Read only. The period (month or quarter) only changes the desk quota progress.
 *   GET /dashboard?period=2026-10
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        String period = QuotaValidator.normalizePeriod(FormUtil.text(request, "period"));
        if (period.isEmpty()) {
            period = currentMonth();
        } else if (!QuotaValidator.isValidPeriod(period)) {
            request.setAttribute("error", "Period must look like 2026-10 (a month) or 2026-Q4 (a quarter).");
            period = currentMonth();
        }

        request.setAttribute("dash", reportDAO.getDashboard(period,
                QuotaValidator.periodStart(period), QuotaValidator.periodEnd(period)));
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    /** This month as 2026-10. */
    static String currentMonth() {
        return LocalDate.now().toString().substring(0, 7);
    }
}
