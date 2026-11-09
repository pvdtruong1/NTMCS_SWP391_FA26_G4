package controller.editorial;

import dal.TopicDAO;
import dto.CmsHandoverRecordDTO;
import dto.EditorialDashboardDTO;
import dto.TopicDeliverableDTO;
import dto.TopicPendingDTO;
import model.Desk;
import model.User;
import util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for SCR-2.1: Editorial Board Executive Dashboard & RULE-02 Acceptance Handover Protocol.
 * 
 * Provides real-time newsroom executive KPI monitoring, deliverable inspection,
 * RULE-01 asset interlock enforcement, and HMAC-SHA256 CMS token minting.
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2) & Executive Editor (TBT - Role 3)
 */
@WebServlet(name = "EditorialDashboardServlet", urlPatterns = {"/editorial/dashboard", "/editorial/editorial-dashboard"})
public class EditorialDashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(EditorialDashboardServlet.class.getName());
    private final TopicDAO topicDAO = new TopicDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // RBAC check: Must be authenticated
        User currentUser = AuthUtil.getLoggedUser(request);
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("user");
        }
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isExecutive = (currentUser.getRoleId() == 3 || currentUser.getRoleId() == 7 || "EXECUTIVE_EDITOR".equalsIgnoreCase(currentUser.getRoleName()) || "SYSTEM_ADMINISTRATOR".equalsIgnoreCase(currentUser.getRoleName()));
        boolean isDeskEditor = (currentUser.getRoleId() == 2 || "DESK_EDITOR".equalsIgnoreCase(currentUser.getRoleName()));

        if (!isExecutive && !isDeskEditor) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập Dashboard Ban Biên Tập.");
            return;
        }

        // 2. Parse Desk Filter (TBT can filter all desks or specific desk; Editor scoped to their desk)
        int selectedDeskId = 0;
        if (!isExecutive) {
            selectedDeskId = (currentUser.getDeskId() != null) ? currentUser.getDeskId() : 1;
        } else {
            String deskParam = request.getParameter("deskId");
            if (deskParam != null && !deskParam.trim().isEmpty()) {
                try {
                    selectedDeskId = Integer.parseInt(deskParam.trim());
                } catch (NumberFormatException ignored) {
                    selectedDeskId = 0;
                }
            }
        }

        // 3. Fetch KPI Metrics
        EditorialDashboardDTO metrics = topicDAO.getEditorialDashboardMetrics(selectedDeskId, isExecutive);

        // 4. Fetch Pending Deliverables (DELIVERED topics awaiting acceptance review)
        List<TopicDeliverableDTO> pendingDeliverables = topicDAO.getPendingDeliverables(selectedDeskId, isExecutive);

        // 4b. Fetch Pending Pitches (SUBMITTED topics awaiting evaluation)
        int filterDesk = (selectedDeskId > 0) ? selectedDeskId : 1;
        List<TopicPendingDTO> pendingPitches = topicDAO.getPendingTopics(filterDesk, "SUBMITTED", null, 1, 5);

        // 5. Fetch Recent CMS Handover Records (Audit Trail)
        List<CmsHandoverRecordDTO> recentHandovers = topicDAO.getRecentCmsHandovers(selectedDeskId, isExecutive, 10);

        // 6. Fetch all desks for TBT scope switcher
        List<Desk> deskList = topicDAO.getAllDesks();

        // 7. Pass context to JSP
        request.setAttribute("metrics", metrics);
        request.setAttribute("pendingDeliverables", pendingDeliverables);
        request.setAttribute("deliverables", pendingDeliverables);
        request.setAttribute("pendingPitches", pendingPitches);
        request.setAttribute("recentHandovers", recentHandovers);
        request.setAttribute("deskList", deskList);
        request.setAttribute("selectedDeskId", selectedDeskId);
        request.setAttribute("isExecutive", isExecutive);
        request.setAttribute("currentUser", currentUser);

        // Forward flash feedback parameters from PRG
        String msg = request.getParameter("msg");
        String code = request.getParameter("code");
        String token = request.getParameter("token");
        String error = request.getParameter("error");
        request.setAttribute("msg", msg);
        request.setAttribute("code", code);
        request.setAttribute("token", token);
        request.setAttribute("error", error);

        request.getRequestDispatcher("/editorial/editorial-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User currentUser = AuthUtil.getLoggedUser(request);
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("user");
        }
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int editorId = currentUser.getUserId();
        String action = request.getParameter("action");
        String topicIdRaw = request.getParameter("topicId");

        if (topicIdRaw == null || topicIdRaw.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" + URLEncoder.encode("Thiếu mã định danh đề tài.", StandardCharsets.UTF_8));
            return;
        }

        int topicId;
        try {
            topicId = Integer.parseInt(topicIdRaw.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" + URLEncoder.encode("Mã định danh đề tài không hợp lệ.", StandardCharsets.UTF_8));
            return;
        }

        try {
            if ("accept_cms".equalsIgnoreCase(action)) {
                // Execute RULE-02 Acceptance Handover & HMAC-SHA256 Token Minting
                String reviewNote = request.getParameter("note");
                String mintedToken = topicDAO.acceptTopicAndMintCmsToken(topicId, editorId, reviewNote, null);

                // PRG Pattern redirect
                response.sendRedirect(request.getContextPath() + "/editorial/dashboard?msg=accepted&topicId=" + topicId 
                        + "&token=" + URLEncoder.encode(mintedToken, StandardCharsets.UTF_8));
                return;

            } else if ("request_revision".equalsIgnoreCase(action)) {
                // Request deliverable revision
                String feedback = request.getParameter("feedback");
                if (feedback == null || feedback.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" 
                            + URLEncoder.encode("Ý kiến chỉ đạo yêu cầu hoàn thiện lại bản thảo không được để trống.", StandardCharsets.UTF_8));
                    return;
                }
                topicDAO.requestDeliverableRevision(topicId, editorId, feedback);

                response.sendRedirect(request.getContextPath() + "/editorial/dashboard?msg=revision_requested&topicId=" + topicId);
                return;

            } else {
                response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" 
                        + URLEncoder.encode("Hành động không được hỗ trợ.", StandardCharsets.UTF_8));
            }

        } catch (IllegalStateException ex) {
            // Business rule violations (e.g. RULE-01 unreturned asset interlock)
            LOGGER.log(Level.WARNING, "Business rule violation in EditorialDashboardServlet: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" 
                    + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error in EditorialDashboardServlet action=" + action, ex);
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" 
                    + URLEncoder.encode("Lỗi cơ sở dữ liệu: " + ex.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error in EditorialDashboardServlet", ex);
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=" 
                    + URLEncoder.encode("Đã xảy ra lỗi không xác định: " + ex.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
