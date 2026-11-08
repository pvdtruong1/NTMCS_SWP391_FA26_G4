package controller;

import dao.AssetRequestDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import model.AssetRequest;

@WebServlet(name = "AssetRequestServlet", urlPatterns = {"/asset-requests"})
public class AssetRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 10;

    private static final String VIEW = "/WEB-INF/views/asset-requests/";

    private static final String[] VALID_STATUSES = {
        "ALL", "PENDING", "APPROVED", "REJECTED",
        "CHECKED_OUT", "RETURNED"
    };

    private final AssetRequestDAO dao = new AssetRequestDAO();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));

        try {
            switch (action) {
                case "detail":
                    showDetail(req, resp);
                    break;

                case "schedule":
                    showSchedule(req, resp);
                    break;

                case "history":
                    showHistory(req, resp);
                    break;

                default:
                    showList(req, resp);
                    break;
            }

        } catch (IllegalArgumentException ex) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                ex.getMessage()
            );

        } catch (SQLException ex) {
            log("Asset Request GET failed", ex);

            resp.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể tải dữ liệu yêu cầu tài sản. Hãy kiểm tra log của Tomcat."
            );
        }
    }

    // =====================================================
    // DANH SÁCH - SEARCH - FILTER - PAGINATION
    // =====================================================

    private void showList(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        String keyword = trim(req.getParameter("keyword"));
        String status = trim(req.getParameter("status"));
        String fromDate = trim(req.getParameter("fromDate"));
        String toDate = trim(req.getParameter("toDate"));

        if (status.isEmpty()) {
            status = "ALL";
        }

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Bộ lọc trạng thái không hợp lệ.");
        }

        validateDate(fromDate);
        validateDate(toDate);

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);

            if (from.isAfter(to)) {
                throw new IllegalArgumentException("Từ ngày không được sau đến ngày.");
            }
        }

        int page = positiveInt(req.getParameter("page"), 1);

        int total = dao.countAll(keyword, status, fromDate, toDate);

        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));

        if (page > totalPages) {
            page = totalPages;
        }

        List<AssetRequest> rows = dao.findAll(keyword, status, fromDate, toDate, page);

        req.setAttribute("requests", rows);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);

        req.setAttribute("totalRecords", total);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("pageSize", PAGE_SIZE);

        req.setAttribute("pendingCount", dao.countByStatus("PENDING"));
        req.setAttribute("approvedCount", dao.countByStatus("APPROVED"));
        req.setAttribute("rejectedCount", dao.countByStatus("REJECTED"));
        req.setAttribute("checkedOutCount", dao.countByStatus("CHECKED_OUT"));
        req.setAttribute("returnedCount", dao.countByStatus("RETURNED"));

        forward(req, resp, "asset-request-list.jsp");
    }

    // =====================================================
    // CHI TIẾT YÊU CẦU
    // =====================================================

    private void showDetail(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        int id = positiveInt(req.getParameter("id"), -1);

        if (id < 1) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "ID yêu cầu không hợp lệ."
            );
            return;
        }

        AssetRequest item = dao.findById(id);

        if (item == null) {
            resp.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy yêu cầu tài sản."
            );
            return;
        }

        req.setAttribute("assetRequest", item);
        forward(req, resp, "asset-request-detail.jsp");
    }

    // =====================================================
    // KIỂM TRA LỊCH TRÙNG
    // =====================================================

    private void showSchedule(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        int id = positiveInt(req.getParameter("id"), -1);

        if (id < 1) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "ID yêu cầu không hợp lệ."
            );
            return;
        }

        AssetRequest item = dao.findById(id);

        if (item == null) {
            resp.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "Không tìm thấy yêu cầu tài sản."
            );
            return;
        }

        List<AssetRequest> conflicts = dao.findConflicts(
            item.getAssetId(),
            item.getStartTime(),
            item.getEndTime(),
            item.getRequestId()
        );

        boolean hasBlockingConflict = false;
        for (AssetRequest c : conflicts) {
            if ("APPROVED".equals(c.getStatus()) || "CHECKED_OUT".equals(c.getStatus())) {
                hasBlockingConflict = true;
                break;
            }
        }

        req.setAttribute("assetRequest", item);
        req.setAttribute("conflicts", conflicts);
        req.setAttribute("hasConflict", hasBlockingConflict);

        forward(req, resp, "asset-request-schedule.jsp");
    }

    // =====================================================
    // LỊCH SỬ SỬ DỤNG
    // =====================================================

    private void showHistory(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        int assetId = positiveInt(req.getParameter("assetId"), -1);

        if (assetId < 1) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã tài sản không hợp lệ."
            );
            return;
        }

        List<AssetRequest> history = dao.findHistoryByAssetId(assetId);

        req.setAttribute("history", history);
        req.setAttribute("assetId", assetId);

        forward(req, resp, "asset-history.jsp");
    }

    // =====================================================
    // XỬ LÝ POST: APPROVE / REJECT / CHECKOUT / RETURN
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));
        int id = positiveInt(req.getParameter("id"), -1);

        if (id < 1) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Mã yêu cầu không hợp lệ."
            );
            return;
        }

        try {
            String result;

            switch (action) {
                case "approve":
                    result = dao.approveRequest(id);
                    break;

                case "reject":
                    result = dao.rejectRequest(id);
                    break;

                case "checkout":
                    result = dao.checkoutRequest(id);
                    break;

                case "return":
                    String returnStatus = trim(req.getParameter("returnStatus"));

                    if (returnStatus.isEmpty()) {
                        returnStatus = "AVAILABLE";
                    }

                    if (!"AVAILABLE".equals(returnStatus) && !"MAINTENANCE".equals(returnStatus)) {
                        throw new IllegalArgumentException(
                            "Trạng thái sau thu hồi không hợp lệ."
                        );
                    }

                    result = dao.returnRequest(id, returnStatus);
                    break;

                default:
                    resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Thao tác không hợp lệ."
                    );
                    return;
            }

            String message = mapMessage(result);

            resp.sendRedirect(
                req.getContextPath() + "/asset-requests?message=" + message
            );

        } catch (IllegalArgumentException ex) {
            resp.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                ex.getMessage()
            );

        } catch (SQLException ex) {
            log("Asset Request POST failed. action=" + action + ", requestId=" + id, ex);

            resp.sendRedirect(
                req.getContextPath() + "/asset-requests?message=error"
            );
        }
    }

    // =====================================================
    // CHUYỂN KẾT QUẢ DAO THÀNH MESSAGE TRÊN JSP
    // =====================================================

    private String mapMessage(String result) {
        if (result == null) {
            return "error";
        }

        switch (result) {
            case "APPROVED":
                return "approved";
            case "REJECTED":
                return "rejected";
            case "CHECKED_OUT":
                return "checkedout";
            case "RETURNED":
                return "returned";
            case "CONFLICT":
                return "conflict";
            case "ASSET_MAINTENANCE":
                return "maintenance";
            case "ASSET_NOT_AVAILABLE":
                return "unavailable";
            case "NOT_PENDING":
                return "notpending";
            case "NOT_APPROVED":
                return "notapproved";
            case "NOT_CHECKED_OUT":
                return "notcheckedout";
            case "NOT_FOUND":
                return "notfound";
            default:
                return "error";
        }
    }

    // =====================================================
    // VALIDATION / HELPER
    // =====================================================

    private boolean isValidStatus(String value) {
        for (String status : VALID_STATUSES) {
            if (status.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private void validateDate(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }

        try {
            LocalDate.parse(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Ngày lọc không đúng định dạng yyyy-MM-dd.");
        }
    }

    private int positiveInt(String value, int fallback) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void forward(
            HttpServletRequest req,
            HttpServletResponse resp,
            String jsp)
            throws ServletException, IOException {

        req.getRequestDispatcher(VIEW + jsp).forward(req, resp);
    }
}
