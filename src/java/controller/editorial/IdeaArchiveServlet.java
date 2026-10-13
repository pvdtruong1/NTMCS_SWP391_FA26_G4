package controller.editorial;

import dal.TopicDAO;
import dto.TopicArchiveDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Category;
import model.Desk;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Idea Archive & Suspended Topic Catalog (SCR-2.8).
 * Manages shelved, rejected, and aborted pitches and facilitates closed-loop reactivation to DRAFT.
 */
@WebServlet(name = "IdeaArchiveServlet", urlPatterns = {"/editorial/idea-archive"})
public class IdeaArchiveServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(IdeaArchiveServlet.class.getName());
    private static final int DEFAULT_MOCK_DESK_ID = 1;      // Ban Thời sự - Chính trị
    private static final int DEFAULT_MOCK_EDITOR_USER_ID = 1; // BTV Trường
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int deskId = DEFAULT_MOCK_DESK_ID;

        // Parse filter parameters
        String keyword = request.getParameter("keyword");
        String statusFilter = request.getParameter("status");
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            statusFilter = "ALL";
        } else {
            statusFilter = statusFilter.trim().toUpperCase();
        }

        int categoryId = 0;
        try {
            String catStr = request.getParameter("categoryId");
            if (catStr != null && !catStr.trim().isEmpty()) {
                categoryId = Integer.parseInt(catStr.trim());
            }
        } catch (NumberFormatException e) {
            categoryId = 0;
        }

        int page = 1;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        List<TopicArchiveDTO> archivedTopics = null;
        int totalRecords = 0;
        Map<String, Integer> stats = null;
        List<Category> categories = null;
        Desk currentDesk = null;

        // Fetch data ensuring zero connection hoarding: connection closed before forward
        try (TopicDAO dao = new TopicDAO()) {
            archivedTopics = dao.getArchivedTopics(deskId, statusFilter, categoryId, keyword, page, DEFAULT_PAGE_SIZE);
            totalRecords = dao.countArchivedTopics(deskId, statusFilter, categoryId, keyword);
            stats = dao.getArchiveStats(deskId);
            categories = dao.getCategoriesByDesk(deskId);
            currentDesk = dao.getDeskById(deskId);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading Idea Archive in doGet", ex);
            request.setAttribute("errorMessage", "Không thể kết nối cơ sở dữ liệu để tải kho lưu trữ.");
        }

        int totalPages = (int) Math.ceil((double) totalRecords / DEFAULT_PAGE_SIZE);
        if (totalPages == 0) {
            totalPages = 1;
        }

        // Set view attributes
        request.setAttribute("archivedTopics", archivedTopics);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("stats", stats);
        request.setAttribute("categories", categories);
        request.setAttribute("currentDesk", currentDesk);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageSize", DEFAULT_PAGE_SIZE);
        request.setAttribute("keyword", keyword != null ? keyword.trim() : "");
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("selectedCategoryId", categoryId);

        // Feedback alert messaging
        String msg = request.getParameter("msg");
        String error = request.getParameter("error");
        if ("reactivated".equalsIgnoreCase(msg)) {
            request.setAttribute("feedbackSuccess", "Đã phục hồi đề tài thành công về bản nháp (DRAFT) và gửi thông báo cho phóng viên tác giả hoàn thiện đề cương.");
        }
        if ("invalid_state".equalsIgnoreCase(error)) {
            request.setAttribute("feedbackError", "Lỗi nghiệp vụ: Chỉ đề tài ở trạng thái REJECTED hoặc ABORTED mới được phép tái kích hoạt về DRAFT.");
        } else if ("invalid_id".equalsIgnoreCase(error)) {
            request.setAttribute("feedbackError", "Mã đề tài không hợp lệ.");
        } else if ("server_error".equalsIgnoreCase(error)) {
            request.setAttribute("feedbackError", "Đã xảy ra sự cố máy chủ khi xử lý phục hồi đề tài.");
        }

        request.getRequestDispatcher("/editorial/idea-archive.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String topicIdStr = request.getParameter("topicId");
        String reactivateNotes = request.getParameter("reactivateNotes");

        int topicId = 0;
        try {
            if (topicIdStr != null) {
                topicId = Integer.parseInt(topicIdStr.trim());
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/idea-archive?error=invalid_id");
            return;
        }

        if (topicId <= 0) {
            response.sendRedirect(request.getContextPath() + "/editorial/idea-archive?error=invalid_id");
            return;
        }

        if ("reactivate".equalsIgnoreCase(action)) {
            try (TopicDAO dao = new TopicDAO()) {
                dao.reactivateArchivedTopic(topicId, DEFAULT_MOCK_EDITOR_USER_ID, reactivateNotes);
                // Strict PRG Pattern
                response.sendRedirect(request.getContextPath() + "/editorial/idea-archive?msg=reactivated");
                return;
            } catch (IllegalStateException ex) {
                LOGGER.log(Level.WARNING, "Guard clause rejected reactivation: " + ex.getMessage());
                response.sendRedirect(request.getContextPath() + "/editorial/idea-archive?error=invalid_state");
                return;
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Database error reactivating topic #" + topicId, ex);
                response.sendRedirect(request.getContextPath() + "/editorial/idea-archive?error=server_error");
                return;
            }
        }

        // Fallback PRG
        response.sendRedirect(request.getContextPath() + "/editorial/idea-archive");
    }
}
