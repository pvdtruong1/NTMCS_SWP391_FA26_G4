package util;

import dal.AuditLogDAO;
import dal.DeskDAO;
import dal.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.Desk;
import model.User;

/**
 * Runs a performance report (UC-38) and exports it (UC-42 Export Report).
 * Shared by ReportServlet (JSP) and ApiReportServlet (React):
 *   Excel = a real .xlsx file download
 *   PDF   = a print-ready page that opens the browser's print dialog ("Save as PDF")
 * Every export is written to the audit log.
 */
public class ReportExporter {

    private static final ReportDAO reportDAO = new ReportDAO();
    private static final DeskDAO deskDAO = new DeskDAO();
    private static final AuditLogDAO auditDAO = new AuditLogDAO();

    /** Runs the report. The result has "rows", "total" (one row over everything) and "description". */
    public static Map<String, Object> run(ReportRequest r) {
        List<Map<String, Object>> rows = reportDAO.buildReport(r.getGroupBy(), r.getFrom(), r.getTo(), r.getDeskId());
        List<Map<String, Object>> total = reportDAO.buildReport(null, r.getFrom(), r.getTo(), r.getDeskId());
        Desk desk = r.getDeskId() > 0 ? deskDAO.getById(r.getDeskId()) : null;
        return Json.obj(
                "rows", rows,
                "total", total.isEmpty() ? null : total.get(0),
                "description", r.describe(desk == null ? null : desk.getDeskName()));
    }

    /** Sends the report as an .xlsx download. */
    @SuppressWarnings("unchecked")
    public static void sendXlsx(HttpServletResponse response, ReportRequest r, Map<String, Object> result,
            User actor) throws IOException {
        List<String[]> columns = r.getMetricColumns();
        List<List<Object>> sheet = new ArrayList<>();
        sheet.add(row("NTMCS performance report"));
        sheet.add(row((String) result.get("description")));
        sheet.add(row("Generated " + now() + " by " + actor.getFullName()));
        sheet.add(row());

        List<Object> header = new ArrayList<>();
        header.add(r.getGroupLabel());
        for (String[] column : columns) {
            header.add(column[1]);
        }
        sheet.add(header);
        for (Map<String, Object> data : (List<Map<String, Object>>) result.get("rows")) {
            sheet.add(dataRow(data, columns, (String) data.get("group_label")));
        }
        Map<String, Object> total = (Map<String, Object>) result.get("total");
        if (total != null) {
            sheet.add(dataRow(total, columns, "Total"));
        }

        String fileName = "NTMCS_Report_" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setHeader("Cache-Control", "no-store");
        XlsxWriter.write(response.getOutputStream(), "Report", sheet, 5);
        logExport(actor, "EXCEL", result);
    }

    /** Shows the print-ready page (the browser saves it as PDF). */
    public static void showPrint(HttpServletRequest request, HttpServletResponse response, ReportRequest r,
            Map<String, Object> result, User actor) throws ServletException, IOException {
        request.setAttribute("report", r);
        request.setAttribute("result", result);
        request.setAttribute("generatedAt", now());
        request.setAttribute("generatedBy", actor.getFullName());
        request.getRequestDispatcher("/WEB-INF/views/report-print.jsp").forward(request, response);
        logExport(actor, "PDF", result);
    }

    private static void logExport(User actor, String format, Map<String, Object> result) {
        auditDAO.log(actor.getUserId(), "EXPORT_REPORT", "REPORT", null,
                "Exported performance report as " + format + ": " + result.get("description"));
    }

    private static List<Object> dataRow(Map<String, Object> data, List<String[]> columns, String label) {
        List<Object> cells = new ArrayList<>();
        cells.add(label);
        for (String[] column : columns) {
            cells.add(data.get(column[0]));
        }
        return cells;
    }

    private static List<Object> row(Object... cells) {
        List<Object> list = new ArrayList<>();
        for (Object cell : cells) {
            list.add(cell);
        }
        return list;
    }

    private static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
