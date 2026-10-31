package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import model.User;
import util.ApiUtil;
import util.ReportExporter;
import util.ReportRequest;

/**
 * JSON version of ReportServlet (UC-38, with UC-42). Same parameters (see ReportRequest):
 *   GET /api/report?group=&from=&to=&desk_id=&metric=...   -> {description, metrics, rows, total}
 *   ... &format=xlsx    -> Excel file download
 *   ... &format=print   -> print-ready page (the browser saves it as PDF)
 */
@WebServlet(name = "ApiReportServlet", urlPatterns = {"/api/report"})
public class ApiReportServlet extends HttpServlet {

    private static final String PERMISSION = "QUOTA_MANAGE";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = ApiUtil.requirePermission(request, response, PERMISSION);
        if (user == null) {
            return;
        }
        ReportRequest report = ReportRequest.read(request);
        if (report.getError() != null) {
            ApiUtil.error(response, 400, report.getError());
            return;
        }

        Map<String, Object> result = ReportExporter.run(report);
        String format = request.getParameter("format");
        if ("xlsx".equals(format)) {
            ReportExporter.sendXlsx(response, report, result, user);
        } else if ("print".equals(format)) {
            ReportExporter.showPrint(request, response, report, result, user);
        } else {
            result.put("group_by", report.getGroupBy());
            result.put("metrics", report.getMetrics());
            ApiUtil.send(response, 200, result);
        }
    }
}
