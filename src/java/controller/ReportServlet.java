package controller;

import dal.DeskDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import model.User;
import util.AuthUtil;
import util.ReportExporter;
import util.ReportRequest;

/**
 * UC-38 Build Performance Report (SCR-5.5), with UC-42 Export Report.
 * The user picks how to group the topics, a deadline range, a desk and the columns.
 *   GET /report                   -> builder + result table
 *   GET /report?...&format=xlsx   -> the same report as an Excel file
 *   GET /report?...&format=print  -> the same report as a print-ready page (Save as PDF)
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/report"})
public class ReportServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    private final DeskDAO deskDAO = new DeskDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, PERMISSION)) {
            return;
        }
        User user = AuthUtil.getLoggedUser(request);
        ReportRequest report = ReportRequest.read(request);
        String format = request.getParameter("format");

        if (report.getError() == null) {
            Map<String, Object> result = ReportExporter.run(report);
            if ("xlsx".equals(format)) {
                ReportExporter.sendXlsx(response, report, result, user);
                return;
            }
            if ("print".equals(format)) {
                ReportExporter.showPrint(request, response, report, result, user);
                return;
            }
            request.setAttribute("result", result);
        } else {
            request.setAttribute("error", report.getError());
        }

        request.setAttribute("report", report);
        request.setAttribute("groups", ReportRequest.GROUPS);
        request.setAttribute("metrics", ReportRequest.METRICS);
        request.setAttribute("deskList", deskDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/report.jsp").forward(request, response);
    }
}
