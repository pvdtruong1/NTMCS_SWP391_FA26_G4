package servlet;

import dao.FieldworkDeliverableDAO;
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

@WebServlet("/api/fieldwork-deliverables")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize = 10L * 1024L * 1024L,
    maxRequestSize = 12L * 1024L * 1024L
)
public class FieldworkDeliverableServlet extends HttpServlet {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;
    private final FieldworkDeliverableDAO deliverableDAO =
            new FieldworkDeliverableDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Path savedFile = null;

        try {
            int taskId = parsePositiveInt(request.getParameter("taskId"), "taskId");
            int submittedByUserId = parsePositiveInt(
                    request.getParameter("submittedByUserId"), "submittedByUserId");

            String submissionType = trim(request.getParameter("submissionType"))
                    .toUpperCase(Locale.ROOT);
            String taskStatus = trim(request.getParameter("taskStatus"))
                    .toUpperCase(Locale.ROOT);
            if (!"IN_PROGRESS".equals(taskStatus) && !"DONE".equals(taskStatus)) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "taskStatus phải là IN_PROGRESS hoặc DONE.");
                return;
            }

            String note = trim(request.getParameter("deliverableNote"));

            if (note.isEmpty()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Vui lòng nhập ghi chú nộp sản phẩm.");
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
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Vui lòng chọn file Word hoặc PDF.");
                    return;
                }

                if (filePart.getSize() > MAX_FILE_SIZE) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "File vượt quá giới hạn 10 MB.");
                    return;
                }

                originalFileName = getSafeOriginalFileName(filePart);
                String lowerName = originalFileName.toLowerCase(Locale.ROOT);
                if (!lowerName.endsWith(".doc")
                        && !lowerName.endsWith(".docx")
                        && !lowerName.endsWith(".pdf")) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Chỉ chấp nhận file .doc, .docx hoặc .pdf.");
                    return;
                }

                contentType = filePart.getContentType();
                fileSize = filePart.getSize();

                Path uploadDirectory = getUploadDirectory();
                Files.createDirectories(uploadDirectory);

                String storedName = UUID.randomUUID().toString()
                        + getExtension(lowerName);
                savedFile = uploadDirectory.resolve(storedName);

                try (InputStream input = filePart.getInputStream()) {
                    Files.copy(input, savedFile, StandardCopyOption.REPLACE_EXISTING);
                }

                // Store a relative application-managed path, not the client filename.
                storedFilePath = "uploads/fieldwork/" + storedName;

            } else if ("LINK".equals(submissionType)) {
                externalUrl = trim(request.getParameter("externalUrl"));

                if (!(externalUrl.startsWith("https://")
                        || externalUrl.startsWith("http://"))) {
                    writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Đường dẫn bản thảo phải bắt đầu bằng http:// hoặc https://.");
                    return;
                }
            } else {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "submissionType phải là FILE hoặc LINK.");
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
                    + ",\"message\":\"Nộp sản phẩm thành công\","
                    + "\"taskStatus\":\"" + taskStatus + "\","
                    + "\"topicStatus\":\""
                    + ("DONE".equals(taskStatus) ? "DELIVERED" : "UNCHANGED") + "\"}"
            );

        } catch (IllegalArgumentException ex) {
            deleteIfExists(savedFile);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage() == null ? "Dữ liệu gửi lên không hợp lệ." : ex.getMessage());

        } catch (IllegalStateException ex) {
            deleteIfExists(savedFile);
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Yêu cầu hoặc file vượt quá giới hạn cho phép.");

        } catch (Exception ex) {
            deleteIfExists(savedFile);
            getServletContext().log("Failed to submit fieldwork deliverable.", ex);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Không thể lưu sản phẩm nộp. Vui lòng kiểm tra log Backend.");
        }
    }

    private int parsePositiveInt(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " không hợp lệ.");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String getSafeOriginalFileName(Part part) {
        String submitted = part.getSubmittedFileName();
        if (submitted == null || submitted.trim().isEmpty()) {
            return "document";
        }

        // Remove any client-supplied path components.
        String normalized = submitted.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String name = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        name = name.replaceAll("[^A-Za-z0-9._ -]", "_");

        if (name.length() > 255) {
            name = name.substring(name.length() - 255);
        }
        return name;
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    private Path getUploadDirectory() throws IOException {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null || catalinaBase.trim().isEmpty()) {
            throw new IOException("Không xác định được thư mục lưu file của Tomcat.");
        }
        return Paths.get(catalinaBase, "uploads", "fieldwork")
                .toAbsolutePath().normalize();
    }

    private void deleteIfExists(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            getServletContext().log("Could not remove orphaned upload: " + path, ex);
        }
    }

    private void writeError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.getWriter().write("{\"error\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
