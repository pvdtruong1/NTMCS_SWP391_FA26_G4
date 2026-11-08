package controller.editorial;

import dal.FieldworkDeliverableDAO;
import dal.TopicDAO;
import dto.TopicDeliverableDTO;
import dto.TopicEvaluationDetailDTO;
import model.FieldworkDeliverable;
import model.TopicMember;
import model.User;
import util.AuthUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Dedicated Full-Page Article Review & Acceptance Handover (SCR-2.6).
 * Provides deep split-view deliverable inspection, direct hybrid preview (Word/PDF/Link),
 * RULE-01 asset interlock enforcement, and cryptographic HMAC-SHA256 CMS token minting.
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2) & Executive Editor (TBT - Role 3)
 */
@WebServlet(name = "DeliverableReviewServlet", urlPatterns = {"/editorial/deliverable-review"})
public class DeliverableReviewServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DeliverableReviewServlet.class.getName());

    private final TopicDAO topicDAO = new TopicDAO();
    private final FieldworkDeliverableDAO deliverableDAO = new FieldworkDeliverableDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, "TOPIC_EVALUATE")) {
            return;
        }

        User currentUser = AuthUtil.getLoggedUser(request);
        boolean isExecutive = (currentUser != null && (currentUser.getRoleId() == 3 || currentUser.getRoleId() == 6 || currentUser.getRoleId() == 7));

        String topicIdParam = request.getParameter("topicId");
        if (topicIdParam == null || topicIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=missing_topic_id");
            return;
        }

        int topicId;
        try {
            topicId = Integer.parseInt(topicIdParam.trim());
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=invalid_topic_id");
            return;
        }

        // 1. Fetch Topic Deliverable Details & RULE-01 Guard Status
        TopicDeliverableDTO deliverable = topicDAO.getTopicDeliverableDetail(topicId);
        if (deliverable == null) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=topic_not_found");
            return;
        }

        // 2. Fetch Deliverable Submissions (Files / Links / Notes)
        List<FieldworkDeliverable> submissions = null;
        try {
            submissions = deliverableDAO.getDeliverablesByTopicId(topicId);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error loading submissions for topicId: " + topicId, ex);
        }

        // 3. Fetch Stage 1 Dossier (Initial Rubric Evaluation details for reference)
        TopicEvaluationDetailDTO stage1Dossier = topicDAO.getTopicDetailForEvaluation(topicId);

        // 4. Fetch Fieldwork Team Members
        List<TopicMember> fieldMembers = topicDAO.findMembersByTopicId(topicId);

        request.setAttribute("deliverable", deliverable);
        request.setAttribute("submissions", submissions);
        request.setAttribute("stage1Dossier", stage1Dossier);
        request.setAttribute("fieldMembers", fieldMembers);
        request.setAttribute("isExecutive", isExecutive);
        request.setAttribute("currentUser", currentUser);

        // Flash params
        request.setAttribute("error", request.getParameter("error"));
        request.setAttribute("msg", request.getParameter("msg"));

        request.getRequestDispatcher("/editorial/deliverable-review.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AuthUtil.check(request, response, "TOPIC_EVALUATE")) {
            return;
        }

        request.setCharacterEncoding("UTF-8");
        User currentUser = AuthUtil.getLoggedUser(request);
        int editorId = (currentUser != null) ? currentUser.getUserId() : 1;

        String action = request.getParameter("action");
        String topicIdParam = request.getParameter("topicId");
        if (topicIdParam == null || topicIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=missing_topic_id");
            return;
        }

        int topicId;
        try {
            topicId = Integer.parseInt(topicIdParam.trim());
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/editorial/dashboard?error=invalid_topic_id");
            return;
        }

        if ("accept_cms".equals(action)) {
            String note = request.getParameter("acceptanceNote");
            try {
                String token = topicDAO.acceptTopicAndMintCmsToken(topicId, editorId, note, null);
                response.sendRedirect(request.getContextPath() + "/editorial/dashboard?msg=accepted&code=" + topicId
                        + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
            } catch (IllegalStateException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Acceptance validation failed for topic #" + topicId + ": " + ex.getMessage());
                response.sendRedirect(request.getContextPath() + "/editorial/deliverable-review?topicId=" + topicId
                        + "&error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Database error during acceptance for topic #" + topicId, ex);
                response.sendRedirect(request.getContextPath() + "/editorial/deliverable-review?topicId=" + topicId
                        + "&error=" + URLEncoder.encode("Lỗi cơ sở dữ liệu khi nghiệm thu đề tài.", StandardCharsets.UTF_8));
            }
        } else if ("request_revision".equals(action)) {
            String feedback = request.getParameter("revisionFeedback");
            try {
                topicDAO.requestDeliverableRevision(topicId, editorId, feedback);
                response.sendRedirect(request.getContextPath() + "/editorial/dashboard?msg=revision_requested&code=" + topicId);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Revision request validation failed for topic #" + topicId + ": " + ex.getMessage());
                response.sendRedirect(request.getContextPath() + "/editorial/deliverable-review?topicId=" + topicId
                        + "&error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8));
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Database error during revision request for topic #" + topicId, ex);
                response.sendRedirect(request.getContextPath() + "/editorial/deliverable-review?topicId=" + topicId
                        + "&error=" + URLEncoder.encode("Lỗi cơ sở dữ liệu khi gửi yêu cầu chỉnh sửa.", StandardCharsets.UTF_8));
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/editorial/deliverable-review?topicId=" + topicId
                    + "&error=" + URLEncoder.encode("Hành động không hợp lệ.", StandardCharsets.UTF_8));
        }
    }
}
