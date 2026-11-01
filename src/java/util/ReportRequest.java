package util;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UC-38 Build Performance Report (SCR-5.5): what the user picked on the report builder.
 * Shared by ReportServlet (JSP) and ApiReportServlet (React), which use the same parameters:
 *   group    = desk | category | reporter | status       (default desk)
 *   from, to = deadline range, yyyy-MM-dd, both optional
 *   desk_id  = only this desk (empty or 0 = all desks)
 *   metric   = one parameter per chosen column            (default: the first four)
 */
public class ReportRequest {

    /** { code, label } of every way to group the rows. */
    public static final String[][] GROUPS = {
        {"desk", "Desk"},
        {"category", "Category"},
        {"reporter", "Reporter"},
        {"status", "Topic status"}
    };

    /** { code, label } of every column the user can pick, in the order they are shown. */
    public static final String[][] METRICS = {
        {"total", "Topics"},
        {"approved", "Approved"},
        {"pending", "Waiting for review"},
        {"revision", "Revision requested"},
        {"rejected", "Rejected"},
        {"approval_rate", "Approval rate (%)"},
        {"overdue", "Overdue"},
        {"avg_score", "Average desk score"},
        {"estimate_total", "Estimated budget (VND)"},
        {"asset_requests", "Asset requests"}
    };

    private static final List<String> DEFAULT_METRICS = Arrays.asList("total", "approved", "rejected", "approval_rate");

    private String groupBy = "desk";
    private String fromText = "";
    private String toText = "";
    private Date from;
    private Date to;
    private int deskId;
    private List<String> metrics = new ArrayList<>();
    private String error;

    /** Reads and checks the parameters. If something is wrong, getError() says what. */
    public static ReportRequest read(HttpServletRequest request) {
        ReportRequest r = new ReportRequest();

        String group = FormUtil.text(request, "group");
        if (!group.isEmpty()) {
            r.groupBy = group;
        }
        if (labelOf(GROUPS, r.groupBy) == null) {
            r.error = "Please choose how to group the report.";
            r.groupBy = "desk";
        }

        r.fromText = FormUtil.text(request, "from");
        r.toText = FormUtil.text(request, "to");
        r.from = parseDate(r.fromText);
        r.to = parseDate(r.toText);
        if ((!r.fromText.isEmpty() && r.from == null) || (!r.toText.isEmpty() && r.to == null)) {
            r.setError("Dates must look like 2026-10-31.");
        } else if (r.from != null && r.to != null && r.from.after(r.to)) {
            r.setError("The \"from\" date must be on or before the \"to\" date.");
        }

        r.deskId = FormUtil.toInt(request.getParameter("desk_id"));

        String[] picked = request.getParameterValues("metric");
        if (picked == null) {
            r.metrics.addAll(DEFAULT_METRICS);
        } else {
            // keep the screen order, whatever order the browser sent them in
            List<String> pickedList = Arrays.asList(picked);
            for (String[] m : METRICS) {
                if (pickedList.contains(m[0])) {
                    r.metrics.add(m[0]);
                }
            }
            if (r.metrics.isEmpty()) {
                r.setError("Pick at least one column for the report.");
                r.metrics.addAll(DEFAULT_METRICS);
            }
        }
        return r;
    }

    private void setError(String message) {
        if (error == null) {
            error = message;
        }
    }

    /** yyyy-MM-dd to a date, or null if it is empty or not a real date. */
    private static Date parseDate(String text) {
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The label for a code in GROUPS or METRICS, or null if the code is unknown. */
    public static String labelOf(String[][] list, String code) {
        for (String[] item : list) {
            if (item[0].equals(code)) {
                return item[1];
            }
        }
        return null;
    }

    /** A short sentence with the filters, shown above the report and in the exported file. */
    public String describe(String deskName) {
        StringBuilder sb = new StringBuilder("Grouped by ").append(labelOf(GROUPS, groupBy).toLowerCase());
        sb.append("; deadline ");
        if (from == null && to == null) {
            sb.append("any date");
        } else {
            sb.append(from == null ? "..." : from.toString()).append(" to ").append(to == null ? "..." : to.toString());
        }
        sb.append("; ").append(deskName == null ? "all desks" : "desk " + deskName);
        return sb.toString();
    }

    public String getGroupBy() { return groupBy; }
    public String getGroupLabel() { return labelOf(GROUPS, groupBy); }
    public String getFromText() { return fromText; }
    public String getToText() { return toText; }
    public Date getFrom() { return from; }
    public Date getTo() { return to; }
    public int getDeskId() { return deskId; }
    public List<String> getMetrics() { return metrics; }
    public String getError() { return error; }

    /** { code, label } of the chosen columns only. */
    public List<String[]> getMetricColumns() {
        List<String[]> columns = new ArrayList<>();
        for (String code : metrics) {
            columns.add(new String[]{code, labelOf(METRICS, code)});
        }
        return columns;
    }
}
