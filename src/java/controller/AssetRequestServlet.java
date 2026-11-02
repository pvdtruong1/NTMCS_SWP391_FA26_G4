package controller;

import dao.AssetRequestDAO;
import model.AssetRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "AssetRequestServlet", urlPatterns = {"/asset-requests"})
public class AssetRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AssetRequestDAO dao = new AssetRequestDAO();

    private static final String VIEW_PATH =
        "/WEB-INF/views/asset-requests/";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("detail".equals(action)) {
                showDetail(request, response);
                return;
            }

            if ("schedule".equals(action)) {
                showSchedule(request, response);
                return;
            }

            showList(request, response);

        } catch (IllegalArgumentException ex) {
            response.sendError(400, ex.getMessage());

        } catch (SQLException ex) {
            log("Lỗi tải danh sách yêu cầu tài sản", ex);
            response.sendError(
                500,
                "Không thể tải dữ liệu yêu cầu tài sản. Kiểm tra log Tomcat."
            );
        }
    }

    private void showList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String keyword = trim(request.getParameter("keyword"));
        String status = trim(request.getParameter("status"));
        String fromDate = trim(request.getParameter("fromDate"));
        String toDate = trim(request.getParameter("toDate"));

        if (status == null || status.isBlank()) {
            status = "ALL";
        }

        validateDate(fromDate);
        validateDate(toDate);

        if (fromDate != null && toDate != null
                && LocalDate.parse(fromDate).isAfter(LocalDate.parse(toDate))) {
            throw new IllegalArgumentException(
                "Ngày bắt đầu không được sau ngày kết thúc."
            );
        }

        int page = parsePage(request.getParameter("page"));

        int totalRecords = dao.countAll(
            keyword, status, fromDate, toDate
        );

        int pageSize = 10;
        int totalPages = Math.max(1, (int) Math.ceil(
            totalRecords / (double) pageSize
        ));

        if (page > totalPages) {
            page = totalPages;
        }

        List<AssetRequest> requests = dao.findAll(
            keyword, status, fromDate, toDate, page
        );

        request.setAttribute("requests", requests);

        request.setAttribute("keyword", keyword == null ? "" : keyword);
        request.setAttribute("status", status);
        request.setAttribute("fromDate", fromDate == null ? "" : fromDate);
        request.setAttribute("toDate", toDate == null ? "" : toDate);

        request.setAttribute("pendingCount", dao.countByStatus("PENDING"));
        request.setAttribute("approvedCount", dao.countByStatus("APPROVED"));
        request.setAttribute("rejectedCount", dao.countByStatus("REJECTED"));

        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher(
            VIEW_PATH + "asset-request-list.jsp"
        ).forward(request, response);
    }

    private void showDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        int id = parseId(request.getParameter("id"));

        if (id <= 0) {
            response.sendError(400, "Mã yêu cầu không hợp lệ.");
            return;
        }

        AssetRequest item = dao.findById(id);

        if (item == null) {
            response.sendError(404, "Không tìm thấy yêu cầu tài sản.");
            return;
        }

        request.setAttribute("assetRequest", item);

        request.getRequestDispatcher(
            VIEW_PATH + "asset-request-detail.jsp"
        ).forward(request, response);
    }

    private void showSchedule(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        int id = parseId(request.getParameter("id"));

        if (id <= 0) {
            response.sendError(400, "Mã yêu cầu không hợp lệ.");
            return;
        }

        AssetRequest item = dao.findById(id);

        if (item == null) {
            response.sendError(404, "Không tìm thấy yêu cầu tài sản.");
            return;
        }

        List<AssetRequest> conflicts = dao.findConflicts(
            item.getAssetId(),
            item.getStartTime(),
            item.getEndTime(),
            item.getRequestId()
        );

        request.setAttribute("assetRequest", item);
        request.setAttribute("conflicts", conflicts);
        request.setAttribute("hasConflict", !conflicts.isEmpty());

        request.getRequestDispatcher(
            VIEW_PATH + "asset-request-schedule.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = trim(request.getParameter("action"));
        int id = parseId(request.getParameter("id"));

        if (id <= 0) {
            response.sendError(400, "Mã yêu cầu không hợp lệ.");
            return;
        }

        try {
            String result;

            if ("approve".equals(action)) {
                result = dao.approveRequest(id);

            } else if ("reject".equals(action)) {
                result = dao.rejectRequest(id);

            } else {
                response.sendError(400, "Thao tác không hợp lệ.");
                return;
            }

            String message;

            switch (result) {
                case "APPROVED":
                    message = "approved";
                    break;

                case "REJECTED":
                    message = "rejected";
                    break;

                case "CONFLICT":
                    message = "conflict";
                    break;

                case "NOT_PENDING":
                    message = "notpending";
                    break;

                case "NOT_FOUND":
                    message = "notfound";
                    break;

                default:
                    message = "error";
                    break;
            }

            response.sendRedirect(
                request.getContextPath()
                + "/asset-requests?message=" + message
            );

        } catch (SQLException ex) {
            log("Lỗi duyệt/từ chối yêu cầu tài sản", ex);

            response.sendRedirect(
                request.getContextPath()
                + "/asset-requests?message=error"
            );
        }
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return -1;
        }
    }

    private int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (Exception ex) {
            return 1;
        }
    }

    private String trim(String value) {
        return value == null || value.isBlank()
            ? null : value.trim();
    }

    private void validateDate(String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        try {
            LocalDate.parse(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                "Ngày lọc không đúng định dạng."
            );
        }
    }
}