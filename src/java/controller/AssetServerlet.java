package controller;

import dao.AssetDAO;
import dao.AssetDAO.DeleteResult;
import model.Asset;
import util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/assets")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 5 * 1024 * 1024,
    maxRequestSize = 6 * 1024 * 1024
)
public class AssetServerlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AssetDAO assetDAO = new AssetDAO();

    private static final Set<String> CATEGORIES = Set.of(
        "CAMERA_EQUIPMENT",
        "PHOTO_EQUIPMENT",
        "AUDIO_EQUIPMENT",
        "ACCESSORIES",
        "IT_EQUIPMENT",
        "VEHICLE"
    );

    private static final Set<String> STATUSES = Set.of(
        "AVAILABLE",
        "CHECKED_OUT",
        "MAINTENANCE"
    );

    private static final Set<String> IMAGE_TYPES = Set.of(
        "image/jpeg",
        "image/png"
    );

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("add".equals(action)) {
                request.setAttribute("asset", new Asset());
                forward(request, response, "asset-form.jsp");
                return;
            }

            if ("edit".equals(action) || "detail".equals(action)) {
                int id = parseId(request.getParameter("id"));

                if (id <= 0) {
                    response.sendError(400, "ID tài sản không hợp lệ.");
                    return;
                }

                Asset asset = assetDAO.getAssetById(id);

                if (asset == null) {
                    response.sendError(404, "Không tìm thấy tài sản.");
                    return;
                }

                request.setAttribute("asset", asset);

                if ("edit".equals(action)) {
                    forward(request, response, "asset-form.jsp");
                } else {
                    forward(request, response, "asset-detail.jsp");
                }
                return;
            }

            request.setAttribute("assets", assetDAO.getAllAssets());
            forward(request, response, "asset-list.jsp");

        } catch (SQLException ex) {
            log("Lỗi truy vấn quản lý tài sản", ex);
            response.sendError(
                500,
                "Không thể tải dữ liệu tài sản. Hãy kiểm tra kết nối database."
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("delete".equals(action)) {
                deleteAsset(request, response);
                return;
            }

            if ("insert".equals(action)) {
                saveAsset(request, response, false);
                return;
            }

            if ("update".equals(action)) {
                saveAsset(request, response, true);
                return;
            }

            response.sendError(400, "Thao tác không hợp lệ.");

        } catch (SQLException ex) {
            log("Lỗi xử lý quản lý tài sản", ex);

            if (isDuplicateKey(ex)) {
                try {
                    showFormError(
                            request, response,
                            "Mã tài sản đã tồn tại. Hãy nhập mã khác.",
                            "update".equals(action)
                    );
                } catch (SQLException ex1) {
                    Logger.getLogger(AssetServerlet.class.getName()).log(Level.SEVERE, null, ex1);
                }
                return;
            }

            response.sendError(
                500,
                "Không thể lưu dữ liệu. Hãy kiểm tra database và log của Tomcat."
            );
        }
    }

    private void saveAsset(
            HttpServletRequest request,
            HttpServletResponse response,
            boolean editing)
            throws SQLException, ServletException, IOException {

        int id = editing ? parseId(request.getParameter("id")) : 0;

        if (editing && id <= 0) {
            response.sendError(400, "ID tài sản không hợp lệ.");
            return;
        }

        Asset asset = new Asset();

        if (editing) {
            Asset existing = assetDAO.getAssetById(id);

            if (existing == null) {
                response.sendError(404, "Không tìm thấy tài sản.");
                return;
            }

            asset = existing;
            asset.setAssetId(id);
        }

        String code = trim(request.getParameter("assetCode"));
        String name = trim(request.getParameter("assetName"));
        String category = trim(request.getParameter("assetCategory"));
        String status = trim(request.getParameter("assetStatus"));
        String serial = trim(request.getParameter("serialNumber"));
        String description = trim(request.getParameter("description"));

        asset.setAssetCode(code);
        asset.setAssetName(name);
        asset.setAssetCategory(category);
        asset.setAssetStatus(status);
        asset.setSerialNumber(emptyToNull(serial));
        asset.setDescription(emptyToNull(description));

        String validationError = validate(asset);

        if (validationError != null) {
            request.setAttribute("asset", asset);
            request.setAttribute("error", validationError);
            forward(request, response, "asset-form.jsp");
            return;
        }

        if (assetDAO.isAssetCodeExists(code, editing ? id : 0)) {
            request.setAttribute("asset", asset);
            request.setAttribute("error", "Mã tài sản đã tồn tại.");
            forward(request, response, "asset-form.jsp");
            return;
        }

        String oldImage = asset.getImagePath();
        String uploadedImage = null;

        Part imagePart = request.getPart("imageFile");

        if (imagePart != null && imagePart.getSize() > 0) {
            try {
                uploadedImage = saveImage(imagePart);
                asset.setImagePath(uploadedImage);
            } catch (IllegalArgumentException ex) {
                request.setAttribute("asset", asset);
                request.setAttribute("error", ex.getMessage());
                forward(request, response, "asset-form.jsp");
                return;
            }
        }

        boolean success;

        try {
            success = editing
                ? assetDAO.updateAsset(asset)
                : assetDAO.insertAsset(asset);
        } catch (SQLException ex) {
            deleteUploadedImage(uploadedImage);
            throw ex;
        }

        if (!success) {
            deleteUploadedImage(uploadedImage);
            request.setAttribute("asset", asset);
            request.setAttribute("error", "Không thể lưu tài sản. Hãy thử lại.");
            forward(request, response, "asset-form.jsp");
            return;
        }

        // Chỉ xóa ảnh cũ sau khi ảnh mới và dữ liệu đã lưu thành công.
        if (uploadedImage != null && oldImage != null
                && !oldImage.equals(uploadedImage)) {
            deleteUploadedImage(oldImage);
        }

        response.sendRedirect(
            request.getContextPath() + "/assets?message="
            + (editing ? "updated" : "added")
        );
    }

    private void deleteAsset(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseId(request.getParameter("id"));

        if (id <= 0) {
            response.sendError(400, "ID tài sản không hợp lệ.");
            return;
        }

        DeleteResult result = assetDAO.deleteAsset(id);

        switch (result) {
            case DELETED:
                response.sendRedirect(
                    request.getContextPath() + "/assets?message=deleted"
                );
                break;

            case NOT_FOUND:
                response.sendRedirect(
                    request.getContextPath() + "/assets?message=notfound"
                );
                break;

            case IN_USE:
                response.sendRedirect(
                    request.getContextPath() + "/assets?message=inuse"
                );
                break;

            case HAS_REQUEST_HISTORY:
                response.sendRedirect(
                    request.getContextPath() + "/assets?message=history"
                );
                break;

            default:
                response.sendRedirect(
                    request.getContextPath() + "/assets?message=deleteerror"
                );
                break;
        }
    }

    private String validate(Asset asset) {
        String code = asset.getAssetCode();
        String name = asset.getAssetName();
        String category = asset.getAssetCategory();
        String status = asset.getAssetStatus();
        String serial = asset.getSerialNumber();
        String description = asset.getDescription();

        if (code == null || code.isBlank()) {
            return "Mã tài sản không được để trống.";
        }
        if (code.length() > 100) {
            return "Mã tài sản không được vượt quá 100 ký tự.";
        }
        if (!code.matches("[\\p{L}\\p{N}][\\p{L}\\p{N}_./-]*")) {
            return "Mã tài sản chỉ được chứa chữ, số, dấu gạch ngang, gạch dưới, dấu chấm hoặc dấu /.";
        }

        if (name == null || name.isBlank()) {
            return "Tên tài sản không được để trống.";
        }
        if (name.length() > 200) {
            return "Tên tài sản không được vượt quá 200 ký tự.";
        }
        if (name.chars().anyMatch(Character::isISOControl)) {
            return "Tên tài sản chứa ký tự điều khiển không hợp lệ.";
        }

        if (category == null || !CATEGORIES.contains(category)) {
            return "Hãy chọn một loại tài sản trong danh mục được cung cấp.";
        }

        if (status == null || !STATUSES.contains(status)) {
            return "Trạng thái tài sản không hợp lệ.";
        }

        if (serial != null && serial.length() > 150) {
            return "Số serial không được vượt quá 150 ký tự.";
        }
        if (serial != null && serial.chars().anyMatch(Character::isISOControl)) {
            return "Số serial chứa ký tự không hợp lệ.";
        }

        if (description != null && description.length() > 2000) {
            return "Mô tả không được vượt quá 2.000 ký tự.";
        }

        return null;
    }

    private String saveImage(Part part)
            throws IOException {

        String contentType = part.getContentType();

        if (contentType == null
                || !IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                "Ảnh chỉ chấp nhận định dạng JPG hoặc PNG."
            );
        }

        if (part.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Dung lượng ảnh không được vượt quá 5 MB."
            );
        }

        BufferedImage image;

        try (InputStream input = part.getInputStream()) {
            image = ImageIO.read(input);
        }

        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            throw new IllegalArgumentException(
                "Tệp tải lên không phải ảnh JPG/PNG hợp lệ."
            );
        }

        String extension = "image/png".equalsIgnoreCase(contentType)
            ? ".png" : ".jpg";

        String fileName = UUID.randomUUID().toString() + extension;

        String realPath = getServletContext().getRealPath("/uploads/assets");

        if (realPath == null) {
            throw new IllegalArgumentException(
                "Tomcat không xác định được thư mục lưu ảnh. " +
                "Hãy triển khai ứng dụng dưới dạng thư mục đã giải nén."
            );
        }

        File directory = new File(realPath);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Không tạo được thư mục lưu ảnh.");
        }

        File destination = new File(directory, fileName);
        part.write(destination.getAbsolutePath());

        return "/uploads/assets/" + fileName;
    }

    private void deleteUploadedImage(String imagePath) {
        if (imagePath == null || !imagePath.startsWith("/uploads/assets/")) {
            return;
        }

        String fileName = imagePath.substring("/uploads/assets/".length());

        // Chỉ xóa tên file đơn, không cho phép đường dẫn thư mục.
        if (fileName.contains("/") || fileName.contains("\\")
                || fileName.contains("..")) {
            return;
        }

        String realPath = getServletContext().getRealPath("/uploads/assets");

        if (realPath != null) {
            try {
                Files.deleteIfExists(Path.of(realPath, fileName));
            } catch (IOException ex) {
                log("Không xóa được ảnh cũ: " + fileName, ex);
            }
        }
    }

    private void showFormError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error,
            boolean editing)
            throws SQLException, ServletException, IOException {

        Asset asset = new Asset();

        if (editing) {
            int id = parseId(request.getParameter("id"));
            asset = assetDAO.getAssetById(id);

            if (asset == null) {
                response.sendError(404, "Không tìm thấy tài sản.");
                return;
            }
        }

        request.setAttribute("asset", asset);
        request.setAttribute("error", error);
        forward(request, response, "asset-form.jsp");
    }

    private boolean isDuplicateKey(SQLException ex) {
        SQLException current = ex;

        while (current != null) {
            if (current.getErrorCode() == 2601
                    || current.getErrorCode() == 2627) {
                return true;
            }
            current = current.getNextException();
        }

        return false;
    }

    private int parseId(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return -1;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void forward(
            HttpServletRequest request,
            HttpServletResponse response,
            String jsp)
            throws ServletException, IOException {

        request.getRequestDispatcher(
            "/WEB-INF/views/assets/" + jsp
        ).forward(request, response);
    }
}