package controller.editorial;

import dal.TopicDAO;
import dto.ReporterWorkloadDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Category;
import model.Desk;
import model.Topic;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SCR-2.5: Reporter Dispatch & Workload Desk (UC: Assign Lead Reporter).
 * Enables the Desk Editor to inspect reporter workload metrics
 * and atomically commission a directive topic by assigning a Lead Reporter.
 */
@WebServlet(name = "ReporterDispatchServlet", urlPatterns = {"/editorial/reporter-dispatch"})
public class ReporterDispatchServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReporterDispatchServlet.class.getName());
    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        if (!AuthUtil.check(request, response, "TOPIC_EVALUATE")) {
            return;
        }

        User currentUser = AuthUtil.getLoggedUser(request);
        int currentDeskId = currentUser.getDeskId() != null ? currentUser.getDeskId() : 1;

        // Parse and validate topicId
        String topicIdParam = request.getParameter("topicId");
        int topicId = 0;
        try {
            topicId = Integer.parseInt(topicIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_topic_id");
            return;
        }

        Topic topic = topicDAO.getTopicById(topicId);
        if (topic == null) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=topic_not_found");
            return;
        }

        // Domain guard: Must be in DRAFT status
        if (!"DRAFT".equalsIgnoreCase(topic.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_status_for_dispatch");
            return;
        }

        prepareViewData(request, topic, currentDeskId);
        request.getRequestDispatcher("/editorial/reporter-dispatch.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        if (!AuthUtil.check(request, response, "TOPIC_EVALUATE")) {
            return;
        }

        User currentUser = AuthUtil.getLoggedUser(request);
        int currentDeskId = currentUser.getDeskId() != null ? currentUser.getDeskId() : 1;

        String topicIdParam = request.getParameter("topicId");
        String leadReporterIdParam = request.getParameter("leadReporterId");
        String commissionNotes = request.getParameter("commissionNotes");

        int topicId = 0;
        try {
            topicId = Integer.parseInt(topicIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_topic_id");
            return;
        }

        Topic topic = topicDAO.getTopicById(topicId);
        if (topic == null) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=topic_not_found");
            return;
        }

        int leadReporterId = 0;
        try {
            if (leadReporterIdParam != null && !leadReporterIdParam.trim().isEmpty()) {
                leadReporterId = Integer.parseInt(leadReporterIdParam.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        // Validation: Reporter selection is required
        if (leadReporterId <= 0) {
            request.setAttribute("error", "Vui lòng chọn một phóng viên phụ trách chính (Trưởng nhóm tác nghiệp) từ danh sách.");
            request.setAttribute("commissionNotes", commissionNotes);
            prepareViewData(request, topic, currentDeskId);
            request.getRequestDispatcher("/editorial/reporter-dispatch.jsp").forward(request, response);
            return;
        }

        try {
            // Execute atomic commissioning transaction
            String topicCode = topicDAO.commissionDirectiveTopic(
                    topicId,
                    leadReporterId,
                    currentUser.getUserId(),
                    commissionNotes
            );

            // Post-Redirect-Get pattern
            String redirectUrl = request.getContextPath()
                    + "/editorial/pending-queue?msg=directive_commissioned&code="
                    + URLEncoder.encode(topicCode, StandardCharsets.UTF_8);
            response.sendRedirect(redirectUrl);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Lỗi khi phát lệnh đặt hàng đề tài chỉ đạo #" + topicId, ex);
            request.setAttribute("error", "Không thể phát lệnh đặt hàng: " + ex.getMessage());
            request.setAttribute("commissionNotes", commissionNotes);
            prepareViewData(request, topic, currentDeskId);
            request.getRequestDispatcher("/editorial/reporter-dispatch.jsp").forward(request, response);
        }
    }

    private void prepareViewData(HttpServletRequest request, Topic topic, int currentDeskId) {
        // Resolve category name
        String categoryName = "Chưa phân loại";
        List<Category> categories = topicDAO.getCategoriesByDesk(currentDeskId);
        for (Category c : categories) {
            if (c.getCategoryId() == topic.getCategoryId()) {
                categoryName = c.getCategoryName();
                break;
            }
        }

        // Load reporter workload matrix
        List<ReporterWorkloadDTO> reporters = topicDAO.getReportersWorkloadByDesk(currentDeskId);
        Desk desk = topicDAO.getDeskById(currentDeskId);

        request.setAttribute("topic", topic);
        request.setAttribute("categoryName", categoryName);
        request.setAttribute("reporters", reporters);
        request.setAttribute("desk", desk);
    }
}
