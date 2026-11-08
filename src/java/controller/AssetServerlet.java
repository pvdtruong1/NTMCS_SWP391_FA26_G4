package controller;

import dao.AssetDAO;
import dao.AssetDAO.DeleteResult;
import dao.AssetDAO.PageResult;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import model.Asset;

@WebServlet(name = "AssetServerlet", urlPatterns = {"/assets"})
public class AssetServerlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final int PAGE_SIZE = 10;

    private static final String VIEW = "/WEB-INF/views/assets/";

    private static final Set<String> CATEGORIES =
            new HashSet<>(Arrays.asList(
                "CAMERA_EQUIPMENT",
                "PHOTO_EQUIPMENT",
                "AUDIO_EQUIPMENT",
                "ACCESSORIES",
                "IT_EQUIPMENT",
                "VEHICLE"
            ));

    private static final Set<String> STATUSES =
            new HashSet<>(Arrays.asList(
                "AVAILABLE",
                "CHECKED_OUT",
                "MAINTENANCE"
            ));

    private final AssetDAO dao = new AssetDAO();

    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));

        try {
            if ("add".equals(action)) {
                Asset asset = new Asset();
                asset.setAssetStatus("AVAILABLE");
                req.setAttribute("asset", asset);
                req.setAttribute("formMode", "add");
                forward(req, resp, "asset-form.jsp");
                return;
            }

            if ("edit".equals(action) || "detail".equals(action)) {
                int id = positiveInt(req.getParameter("id"), -1);

                if (id < 1) {
                    resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "ID tài sản không hợp lệ."
                    );
                    return;
                }

                Asset asset = dao.getAssetById(id);

                if (asset == null) {
                    resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy tài sản."
                    );
                    return;
                }

                req.setAttribute("asset", asset);

                if ("edit".equals(action)) {
                    req.setAttribute("formMode", "edit");
                    forward(req, resp, "asset-form.jsp");
                } else {
                    forward(req, resp, "asset-detail.jsp");
                }
                return;
            }

            showList(req, resp);

        } catch (SQLException ex) {
            log("Asset GET failed", ex);
            resp.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Không thể tải dữ liệu tài sản. Chi tiết lỗi đã được ghi vào log server."
            );
        }
    }

    // =====================================================
    // DANH SÁCH TÀI SẢN
    // =====================================================

    private void showList(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, ServletException, IOException {

        String keyword = trim(req.getParameter("keyword"));
        String category = trim(req.getParameter("category"));
        String status = trim(req.getParameter("status"));

        if (!category.isEmpty() && !CATEGORIES.contains(category)) {
            category = "";
        }

        if (!status.isEmpty() && !STATUSES.contains(status)) {
            status = "";
        }

        int page = positiveInt(req.getParameter("page"), 1);

        PageResult result = dao.searchAssets(
            keyword,
            category,
            status,
            page,
            PAGE_SIZE
        );

        int totalAll = dao.countAllAssets();
        int availableCount = dao.countByStatus("AVAILABLE");
        int checkedOutCount = dao.countByStatus("CHECKED_OUT");
        int maintenanceCount = dao.countByStatus("MAINTENANCE");

        req.setAttribute("assets", result.getAssets());
        req.setAttribute("pageResult", result);

        req.setAttribute("keyword", keyword);
        req.setAttribute("category", category);
        req.setAttribute("status", status);
        req.setAttribute("selectedCategory", category);
        req.setAttribute("selectedStatus", status);

        req.setAttribute("currentPage", result.getCurrentPage());
        req.setAttribute("totalPages", result.getTotalPages());
        req.setAttribute("totalRecords", result.getTotalRecords());

        req.setAttribute("totalAssets", totalAll);
        req.setAttribute("availableCount", availableCount);
        req.setAttribute("checkedOutCount", checkedOutCount);
        req.setAttribute("maintenanceCount", maintenanceCount);

        req.setAttribute("availableAssets", availableCount);
        req.setAttribute("checkedOutAssets", checkedOutCount);
        req.setAttribute("maintenanceAssets", maintenanceCount);

        forward(req, resp, "asset-list.jsp");
    }

    // =====================================================
    // POST
    // =====================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));

        try {
            switch (action) {
                case "insert":
                    save(req, resp, false);
                    return;

                case "update":
                    save(req, resp, true);
                    return;

                case "delete":
                    delete(req, resp);
                    return;

                default:
                    resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Thao tác không hợp lệ."
                    );
            }

        } catch (IllegalArgumentException ex) {
            showFormError(req, resp, action, ex.getMessage());

        } catch (SQLException ex) {
            log("Asset POST failed. action=" + action, ex);

            if (isDuplicateKey(ex)
                    && ("insert".equals(action) || "update".equals(action))) {
                showFormError(
                    req,
                    resp,
                    action,
                    "Mã tài sản đã tồn tại. Vui lòng nhập mã khác."
                );
            } else {
                resp.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể xử lý dữ liệu tài sản. Hãy xem log của máy chủ."
                );
            }
        }
    }

    // =====================================================
    // INSERT / UPDATE
    // =====================================================

    private void save(
            HttpServletRequest req,
            HttpServletResponse resp,
            boolean editing)
            throws SQLException, ServletException, IOException {

        int id = editing ? positiveInt(req.getParameter("id"), -1) : 0;

        if (editing && id < 1) {
            throw new IllegalArgumentException("ID tài sản không hợp lệ.");
        }

        Asset asset = readForm(req);
        asset.setAssetId(id);

        Asset existing = editing ? dao.getAssetById(id) : null;
        if (editing && existing == null) {
            throw new IllegalArgumentException("Tài sản cần cập nhật không tồn tại.");
        }

        validate(asset, editing, existing);

        boolean saved;
        if (editing) {
            saved = dao.updateAsset(asset);
        } else {
            saved = dao.insertAsset(asset);
        }

        if (!saved) {
            throw new SQLException("Không thể lưu dữ liệu tài sản.");
        }

        redirect(req, resp, editing ? "updated" : "added");
    }

    private Asset readForm(HttpServletRequest req) {
        Asset asset = new Asset();
        asset.setAssetCode(emptyToNull(req.getParameter("assetCode")));
        asset.setAssetName(trim(req.getParameter("assetName")));
        asset.setAssetCategory(trim(req.getParameter("assetCategory")));
        asset.setAssetStatus(trim(req.getParameter("assetStatus")));
        asset.setSerialNumber(emptyToNull(req.getParameter("serialNumber")));
        asset.setDescription(emptyToNull(req.getParameter("description")));
        return asset;
    }

    private void validate(
            Asset asset,
            boolean editing,
            Asset existing) throws SQLException {

        String code = asset.getAssetCode();
        String name = asset.getAssetName();
        String category = asset.getAssetCategory();
        String status = asset.getAssetStatus();

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tài sản không được để trống.");
        }

        if (code.length() > 30) {
            throw new IllegalArgumentException("Mã tài sản tối đa 30 ký tự.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài sản không được để trống.");
        }

        if (name.length() > 150) {
            throw new IllegalArgumentException("Tên tài sản tối đa 150 ký tự.");
        }

        if (category == null || category.trim().isEmpty() || !CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Vui lòng chọn loại tài sản hợp lệ.");
        }

        if (status == null || status.trim().isEmpty() || !STATUSES.contains(status)) {
            asset.setAssetStatus("AVAILABLE");
            status = "AVAILABLE";
        }

        int id = asset.getAssetId();
        if (dao.isAssetCodeExists(asset.getAssetCode(), id)) {
            throw new IllegalArgumentException("Mã tài sản đã tồn tại.");
        }

        // Không cho phép tạo mới hoặc tự chuyển trạng thái sang CHECKED_OUT bằng form
        if ("CHECKED_OUT".equals(status)
                && (!editing
                || existing == null
                || !"CHECKED_OUT".equals(existing.getAssetStatus()))) {

            throw new IllegalArgumentException(
                "Trạng thái 'Đang bàn giao' chỉ được cập nhật khi thực hiện bàn giao yêu cầu."
            );
        }

        // Nếu đang sửa mà tài sản đang có yêu cầu PENDING/APPROVED/CHECKED_OUT thì không được chuyển sang MAINTENANCE
        if (editing
                && "MAINTENANCE".equals(status)
                && !"MAINTENANCE".equals(existing.getAssetStatus())
                && dao.hasActiveRequest(id)) {

            throw new IllegalArgumentException(
                "Không thể chuyển sang bảo trì khi tài sản đang có yêu cầu chờ duyệt, đã duyệt hoặc đang bàn giao."
            );
        }

        if (asset.getSerialNumber() != null && asset.getSerialNumber().length() > 100) {
            throw new IllegalArgumentException("Số serial tối đa 100 ký tự.");
        }

        if (asset.getDescription() != null && asset.getDescription().length() > 500) {
            throw new IllegalArgumentException("Mô tả tối đa 500 ký tự.");
        }
    }

    // =====================================================
    // DELETE
    // =====================================================

    private void delete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws SQLException, IOException {

        int id = positiveInt(req.getParameter("id"), -1);

        if (id < 1) {
            redirect(req, resp, "notfound");
            return;
        }

        DeleteResult result = dao.deleteAsset(id);

        switch (result) {
            case DELETED:
                redirect(req, resp, "deleted");
                break;
            case NOT_FOUND:
                redirect(req, resp, "notfound");
                break;
            case IN_USE:
                redirect(req, resp, "inuse");
                break;
            case HAS_REQUEST_HISTORY:
                redirect(req, resp, "history");
                break;
            default:
                redirect(req, resp, "deleteerror");
                break;
        }
    }

    // =====================================================
    // HIỂN THỊ LỖI FORM
    // =====================================================

    private void showFormError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String action,
            String message)
            throws ServletException, IOException {

        boolean editing = "update".equals(action);
        Asset asset = readForm(req);

        if (editing) {
            asset.setAssetId(positiveInt(req.getParameter("id"), -1));
        }

        req.setAttribute("asset", asset);
        req.setAttribute("formMode", editing ? "edit" : "add");
        req.setAttribute("error", message);

        forward(req, resp, "asset-form.jsp");
    }

    // =====================================================
    // HELPER
    // =====================================================

    private boolean isDuplicateKey(SQLException ex) {
        for (SQLException e = ex; e != null; e = e.getNextException()) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627) {
                return true;
            }
        }
        return false;
    }

    private void redirect(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message) throws IOException {

        resp.sendRedirect(
            req.getContextPath() + "/assets?message=" + message
        );
    }

    private void forward(
            HttpServletRequest req,
            HttpServletResponse resp,
            String file)
            throws ServletException, IOException {

        req.getRequestDispatcher(VIEW + file).forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToNull(String value) {
        String valueTrimmed = trim(value);
        return valueTrimmed.isEmpty() ? null : valueTrimmed;
    }

    private int positiveInt(String value, int fallback) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private boolean safeEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}