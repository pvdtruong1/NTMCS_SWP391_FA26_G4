package controller.fieldwork;

import dal.FieldworkDeliverableDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import model.User;
import util.AuthUtil;

@WebServlet(name = "FieldworkDeliverableServlet", urlPatterns = {"/api/fieldwork-deliverables", "/api/fieldwork/deliverables"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 15L * 1024L * 1024L,
    maxRequestSize = 20L * 1024L * 1024L
)
public class FieldworkDeliverableServlet extends HttpServlet {

    private static final long MAX_FILE_SIZE = 15L * 1024L * 1024L;
    private final FieldworkDeliverableDAO deliverableDAO = new FieldworkDeliverableDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            int taskId = Integer.parseInt(request.getParameter("taskId"));
            
            // Prefer authenticated session user to prevent IDOR / impersonation
            User loggedUser = AuthUtil.getLoggedUser(request);
            int submittedByUserId;
            if (loggedUser != null) {
                submittedByUserId = loggedUser.getUserId();
            } else if (request.getParameter("submittedByUserId") != null) {
                submittedByUserId = Integer.parseInt(request.getParameter("submittedByUserId"));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Vui lòng đăng nhập để nộp sản phẩm tác nghiệp.\"}");
                return;
            }

            String submissionType = trim(request.getParameter("submissionType")).toUpperCase(Locale.ROOT);
            String taskStatus = trim(request.getParameter("taskStatus")).toUpperCase(Locale.ROOT);
            if (!"IN_PROGRESS".equals(taskStatus) && !"DONE".equals(taskStatus)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"taskStatus phải là IN_PROGRESS hoặc DONE.\"}");
                return;
            }

            String note = trim(request.getParameter("deliverableNote"));
            if (note.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Vui lòng nhập ghi chú nộp sản phẩm.\"}");
                return;
            }

            String originalFileName = null;
            String storedFilePath = null;
            String contentType = null;
            long fileSize = -1L;
            String externalUrl = null;

            if ("FILE".equals(submissionType)) {
                Part filePart = request.getPart("file");
                if (filePart == null || filePart.getSize() <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Vui lòng chọn file Word (.doc, .docx) hoặc PDF.\"}");
                    return;
                }

                if (filePart.getSize() > MAX_FILE_SIZE) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Dung lượng file vượt quá giới hạn 15 MB.\"}");
                    return;
                }

                originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String lowerName = originalFileName.toLowerCase(Locale.ROOT);
                if (!lowerName.endsWith(".doc") && !lowerName.endsWith(".docx") && !lowerName.endsWith(".pdf")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Chỉ chấp nhận file tài liệu .doc, .docx hoặc .pdf.\"}");
                    return;
                }

                contentType = filePart.getContentType();
                fileSize = filePart.getSize();

                Path uploadDirectory = getUploadDirectory();
                Files.createDirectories(uploadDirectory);

                String ext = lowerName.substring(lowerName.lastIndexOf("."));
                String storedName = UUID.randomUUID().toString() + ext;
                Path savedFile = uploadDirectory.resolve(storedName);

                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, savedFile, StandardCopyOption.REPLACE_EXISTING);
                }

                storedFilePath = "uploads/fieldwork/" + storedName;

            } else if ("LINK".equals(submissionType)) {
                externalUrl = trim(request.getParameter("externalUrl"));
                if (externalUrl.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Vui lòng nhập đường dẫn liên kết bản thảo.\"}");
                    return;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"submissionType phải là FILE hoặc LINK.\"}");
                return;
            }

            int deliverableId = deliverableDAO.submitDeliverable(
                    taskId,
                    submittedByUserId,
                    submissionType,
                    taskStatus,
                    note,
                    originalFileName,
                    storedFilePath,
                    contentType,
                    fileSize,
                    externalUrl
            );

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(
                    "{\"deliverableId\":" + deliverableId
                    + ",\"message\":\"Nộp sản phẩm hiện trường thành công!\","
                    + "\"taskStatus\":\"" + taskStatus + "\","
                    + "\"topicStatus\":\"" + ("DONE".equals(taskStatus) ? "DELIVERED" : "UNCHANGED") + "\"}"
            );

        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Lỗi hệ thống khi nộp sản phẩm: " + ex.getMessage() + "\"}");
        }
    }

    private Path getUploadDirectory() {
        String catalinaBase = System.getProperty("catalina.base");
        Path root = (catalinaBase != null && !catalinaBase.trim().isEmpty())
                ? Paths.get(catalinaBase)
                : Paths.get(System.getProperty("user.home"), ".ntmcs");
        return root.resolve("data").resolve("uploads").resolve("fieldwork");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
