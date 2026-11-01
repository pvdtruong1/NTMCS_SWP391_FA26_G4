package controller.editorial;

import dal.TopicDAO;
import model.Category;
import model.Desk;
import model.Topic;
import model.User;
import util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Directive Topic Creation Form (SCR-2.4 / UC: Create Directive Topic).
 * Allows the Desk Editor to draft editorial directives, define mandatory angles and deadlines,
 * and proceed to the Reporter Dispatch desk (SCR-2.5) for atomic commissioning.
 */
@WebServlet(name = "DirectiveTopicServlet", urlPatterns = {"/editorial/directive-topic"})
public class DirectiveTopicServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DirectiveTopicServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // 1. Session Context & Fallback Dev Mode (Trường - Desk ID 1: Ban Thời sự - Chính trị)
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            currentUser = new User(1, "Trường", "truong.editor@newsroom.vn", "123", 2, true, 1);
            session.setAttribute("user", currentUser);
        }

        int deskId = (currentUser.getDeskId() != null) ? currentUser.getDeskId() : 1;

        try (TopicDAO topicDAO = new TopicDAO()) {
            // 2. Load Categories for the Editor's Desk
            List<Category> categories = topicDAO.getCategoriesByDesk(deskId);
            Desk currentDesk = topicDAO.getDeskById(deskId);

            // 3. Load Monthly Quota & Current Production Count
            String currentPeriod = YearMonth.now().toString(); // e.g. "2026-10"
            int deskQuota = topicDAO.getDeskQuota(deskId, currentPeriod);
            int productionCount = topicDAO.getDeskProductionCount(deskId);

            // 4. Check if editing an existing DRAFT topic
            String topicIdStr = request.getParameter("topicId");
            Topic topic = null;
            if (topicIdStr != null && !topicIdStr.trim().isEmpty()) {
                try {
                    int topicId = Integer.parseInt(topicIdStr.trim());
                    topic = topicDAO.getTopicById(topicId);
                } catch (NumberFormatException ignored) {
                    // ignore invalid topic id
                }
            }

            request.setAttribute("categories", categories);
            request.setAttribute("currentDesk", currentDesk);
            request.setAttribute("deskQuota", deskQuota);
            request.setAttribute("productionCount", productionCount);
            request.setAttribute("currentPeriod", currentPeriod);
            request.setAttribute("todayDate", LocalDate.now().toString());
            request.setAttribute("topic", topic);

            request.getRequestDispatcher("/editorial/directive-creation.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading Directive Topic Creation Form (SCR-2.4)", ex);
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=server_error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();

        // 1. Session Context & Fallback Dev Mode
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            currentUser = new User(1, "Trường", "truong.editor@newsroom.vn", "123", 2, true, 1);
            session.setAttribute("user", currentUser);
        }

        int deskId = (currentUser.getDeskId() != null) ? currentUser.getDeskId() : 1;

        // 2. Parse Form Parameters
        String topicIdStr = request.getParameter("topicId");
        String title = request.getParameter("title");
        String categoryIdStr = request.getParameter("categoryId");
        String angle = request.getParameter("angle");
        String deadlineStr = request.getParameter("deadline");
        String action = request.getParameter("action");

        int topicId = 0;
        if (topicIdStr != null && !topicIdStr.trim().isEmpty()) {
            try {
                topicId = Integer.parseInt(topicIdStr.trim());
            } catch (NumberFormatException ignored) {
                topicId = 0;
            }
        }

        int categoryId = 0;
        if (categoryIdStr != null && !categoryIdStr.trim().isEmpty()) {
            try {
                categoryId = Integer.parseInt(categoryIdStr.trim());
            } catch (NumberFormatException ignored) {
                categoryId = 0;
            }
        }

        // 3. Server-Side Validation
        String validationError = null;
        if (title == null || title.trim().isEmpty()) {
            validationError = "Tiêu đề đề tài chỉ đạo không được để trống.";
        } else if (title.trim().length() < 5 || title.trim().length() > 255) {
            validationError = "Tiêu đề đề tài phải từ 5 đến 255 ký tự.";
        } else if (categoryId <= 0) {
            validationError = "Vui lòng chọn chuyên mục tin tức phù hợp.";
        } else if (angle == null || angle.trim().isEmpty()) {
            validationError = "Định hướng / Góc tiếp cận bắt buộc không được để trống.";
        } else if (angle.trim().length() < 10 || angle.trim().length() > 255) {
            validationError = "Định hướng nội dung phải từ 10 đến 255 ký tự.";
        }

        Date deadlineDate = null;
        if (validationError == null) {
            if (deadlineStr == null || deadlineStr.trim().isEmpty()) {
                validationError = "Hạn chót bàn giao bài viết (Deadline) là bắt buộc.";
            } else {
                try {
                    LocalDate parsedDate = LocalDate.parse(deadlineStr.trim());
                    if (parsedDate.isBefore(LocalDate.now())) {
                        validationError = "Hạn chót nộp bài không thể ở trong quá khứ.";
                    } else {
                        deadlineDate = Date.valueOf(parsedDate);
                    }
                } catch (Exception ex) {
                    validationError = "Định dạng ngày hạn chót nộp bài không hợp lệ (YYYY-MM-DD).";
                }
            }
        }

        // If validation failed, re-render form with error message
        if (validationError != null) {
            try (TopicDAO topicDAO = new TopicDAO()) {
                List<Category> categories = topicDAO.getCategoriesByDesk(deskId);
                Desk currentDesk = topicDAO.getDeskById(deskId);
                String currentPeriod = YearMonth.now().toString();
                int deskQuota = topicDAO.getDeskQuota(deskId, currentPeriod);
                int productionCount = topicDAO.getDeskProductionCount(deskId);

                // Preserve entered values in mock topic object
                Topic mockTopic = new Topic(topicId, null, title, angle, "DRAFT", deadlineDate, categoryId, currentUser.getUserId());

                request.setAttribute("categories", categories);
                request.setAttribute("currentDesk", currentDesk);
                request.setAttribute("deskQuota", deskQuota);
                request.setAttribute("productionCount", productionCount);
                request.setAttribute("currentPeriod", currentPeriod);
                request.setAttribute("todayDate", LocalDate.now().toString());
                request.setAttribute("topic", mockTopic);
                request.setAttribute("errorMessage", validationError);

                request.getRequestDispatcher("/editorial/directive-creation.jsp").forward(request, response);
                return;
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error recovering from validation error in DirectiveTopicServlet", ex);
                response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=server_error");
                return;
            }
        }

        // 4. Persistence in Database
        try (TopicDAO topicDAO = new TopicDAO()) {
            if (topicId > 0) {
                Topic existingTopic = topicDAO.getTopicById(topicId);
                if (existingTopic != null) {
                    existingTopic.setTitle(title.trim());
                    existingTopic.setAngle(angle.trim());
                    existingTopic.setCategoryId(categoryId);
                    existingTopic.setDeadline(deadlineDate);
                    topicDAO.updateDirectiveTopicDraft(existingTopic);
                } else {
                    topicId = 0;
                }
            }

            if (topicId == 0) {
                Topic newTopic = new Topic(0, null, title.trim(), angle.trim(), "DRAFT", deadlineDate, categoryId, currentUser.getUserId());
                topicId = topicDAO.createDirectiveTopicDraft(newTopic);
            }

            // 5. Post-Redirect-Get (PRG) Routing
            if ("proceed_dispatch".equalsIgnoreCase(action)) {
                // Advance to Step 2: Reporter Dispatch & Workload Desk (SCR-2.5)
                response.sendRedirect(request.getContextPath() + "/editorial/reporter-dispatch?topicId=" + topicId);
            } else {
                // Stay on Step 1 with draft saved message
                response.sendRedirect(request.getContextPath() + "/editorial/directive-topic?topicId=" + topicId + "&msg=draft_saved");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error persisting directive topic draft", ex);
            response.sendRedirect(request.getContextPath() + "/editorial/directive-topic?error=server_error");
        }
    }
}
