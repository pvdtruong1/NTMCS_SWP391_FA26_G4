package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller serving uploaded fieldwork deliverable files and assets:
 * GET /uploads/{folder}/{filename}
 * 
 * Supports inline viewing (for PDF in iframe / preview) and download streaming.
 * Includes OWASP Path Traversal protection and authentication enforcement.
 */
@WebServlet(name = "UploadServlet", urlPatterns = {"/uploads/*"})
public class UploadServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Authentication check
        User loggedUser = AuthUtil.getLoggedUser(request);
        if (loggedUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui long dang nhap de xem tai lieu.");
            return;
        }

        // 2. Validate path info
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tap tin khong hop le.");
            return;
        }

        // 3. OWASP Path Traversal Guard: strictly reject attempts to navigate parent dirs
        if (pathInfo.contains("..") || pathInfo.contains("\\")) {
            LOGGER.log(Level.WARNING, "Blocked path traversal attempt: {0} from user #{1}",
                    new Object[]{pathInfo, loggedUser.getUserId()});
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Duong dan khong hop le.");
            return;
        }

        String relPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        // 4. Resolve file location across candidate base paths
        Path targetFile = resolveFilePath(relPath, request);
        if (targetFile == null || !Files.exists(targetFile) || !Files.isRegularFile(targetFile)) {
            LOGGER.log(Level.WARNING, "Requested upload file not found on disk: {0}", relPath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tap tin khong ton tai tren he thong.");
            return;
        }

        // 5. Determine Content-Type
        String fileName = targetFile.getFileName().toString();
        String mimeType = getServletContext().getMimeType(fileName);
        if (mimeType == null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".pdf")) {
                mimeType = "application/pdf";
            } else if (lower.endsWith(".docx")) {
                mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (lower.endsWith(".doc")) {
                mimeType = "application/msword";
            } else if (lower.endsWith(".png")) {
                mimeType = "image/png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mimeType = "image/jpeg";
            } else {
                mimeType = "application/octet-stream";
            }
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(Files.size(targetFile));

        // 6. Disposition: inline by default (for iframe PDF preview), attachment if download=1
        boolean isDownload = "1".equals(request.getParameter("download")) 
                || "true".equalsIgnoreCase(request.getParameter("download"));
        String disposition = isDownload ? "attachment" : "inline";
        response.setHeader("Content-Disposition", disposition + "; filename=\"" + fileName + "\"");
        response.setHeader("Cache-Control", "private, max-age=3600");

        // 7. Stream file bytes to response output stream
        try (InputStream in = Files.newInputStream(targetFile);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[16384];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }
    }

    private Path resolveFilePath(String relPath, HttpServletRequest request) {
        // Candidate 1: catalina.base/data/uploads/{relPath} (Primary storage in FieldworkDeliverableServlet)
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.trim().isEmpty()) {
            Path p1 = Paths.get(catalinaBase, "data", "uploads").resolve(relPath).normalize();
            if (Files.exists(p1) && Files.isRegularFile(p1)) {
                return p1;
            }
        }

        // Candidate 2: user.home/.ntmcs/data/uploads/{relPath} (Fallback storage)
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.trim().isEmpty()) {
            Path p2 = Paths.get(userHome, ".ntmcs", "data", "uploads").resolve(relPath).normalize();
            if (Files.exists(p2) && Files.isRegularFile(p2)) {
                return p2;
            }
        }

        // Candidate 3: webapp root /uploads/{relPath}
        String realPath = request.getServletContext().getRealPath("/uploads/" + relPath);
        if (realPath != null) {
            Path p3 = Paths.get(realPath).normalize();
            if (Files.exists(p3) && Files.isRegularFile(p3)) {
                return p3;
            }
        }

        // Candidate 4: current working directory /data/uploads/{relPath}
        Path p4 = Paths.get("data", "uploads").resolve(relPath).normalize();
        if (Files.exists(p4) && Files.isRegularFile(p4)) {
            return p4;
        }

        return null;
    }
}
