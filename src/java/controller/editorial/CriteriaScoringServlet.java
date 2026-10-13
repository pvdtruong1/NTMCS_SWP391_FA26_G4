package controller.editorial;

import dal.TopicDAO;
import dto.TopicEvaluationDetailDTO;
import model.CriteriaConfig;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Criteria Scoring & Multi-Level Approval Desk (SCR-2.3 / UC: Evaluate Topic Pitch).
 * Implements Post-Redirect-Get (PRG) pattern with pragmatic validation and atomic transaction execution.
 */
@WebServlet(name = "CriteriaScoringServlet", urlPatterns = {"/editorial/criteria-scoring"})
public class CriteriaScoringServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CriteriaScoringServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        // 1. Session Context & Fallback Dev Mode
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            currentUser = new User(1, "Trường", "truong.editor@newsroom.vn", "123", 2, true, 1);
            session.setAttribute("user", currentUser);
        }

        // 2. Parse topicId parameter
        String topicIdStr = request.getParameter("topicId");
        if (topicIdStr == null || topicIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=missing_topic_id");
            return;
        }

        int topicId;
        try {
            topicId = Integer.parseInt(topicIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_topic_id");
            return;
        }

        // 3. Retrieve Topic Dossier & Rubric Criteria
        TopicEvaluationDetailDTO topicDetail = null;
        try (TopicDAO topicDAO = new TopicDAO()) {
            topicDetail = topicDAO.getTopicDetailForEvaluation(topicId);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error loading evaluation desk for topic " + topicId, ex);
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=server_error");
            return;
        }

        if (topicDetail == null) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=topic_not_found");
            return;
        }

        request.setAttribute("topic", topicDetail);
        request.setAttribute("criteriaList", topicDetail.getCriteriaList());
        request.setAttribute("estimates", topicDetail.getEstimates());
        request.setAttribute("totalEstimate", topicDetail.getTotalEstimateAmount());
        request.getRequestDispatcher("/editorial/criteria-scoring.jsp").forward(request, response);
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

        // 2. Parse topicId and decision
        String topicIdStr = request.getParameter("topicId");
        String decision = request.getParameter("decision");
        String comments = request.getParameter("comments");

        if (topicIdStr == null || decision == null) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_submission");
            return;
        }

        int topicId;
        try {
            topicId = Integer.parseInt(topicIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?error=invalid_topic_id");
            return;
        }

        decision = decision.trim().toUpperCase();
        if (!decision.equals("APPROVED") && !decision.equals("ESCALATED") &&
            !decision.equals("REVISION_REQUESTED") && !decision.equals("REJECTED")) {
            response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=invalid_decision");
            return;
        }

        // 3. Parse Legal Risk Score
        double legalRiskScore = 0.0;
        String legalRiskStr = request.getParameter("legalRiskScore");
        if (legalRiskStr != null && !legalRiskStr.trim().isEmpty()) {
            try {
                legalRiskScore = Double.parseDouble(legalRiskStr.trim());
            } catch (NumberFormatException ignored) {
                legalRiskScore = 0.0;
            }
        }

        // Executive Escalation Guard Clause: if legal_risk >= 8.0, decision MUST be ESCALATED
        if (legalRiskScore >= 8.0 && decision.equals("APPROVED")) {
            decision = "ESCALATED";
        }

        // Mandatory feedback requirement for Revision Request or Rejection
        if (("REVISION_REQUESTED".equals(decision) || "REJECTED".equals(decision))
                && (comments == null || comments.trim().isEmpty())) {
            response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=comment_required");
            return;
        }

        // 4. Parse Criteria Scores and Comments
        Map<Integer, Double> criteriaScores = new HashMap<>();
        Map<Integer, String> criteriaComments = new HashMap<>();

        try (TopicDAO topicDAO = new TopicDAO()) {
            List<CriteriaConfig> criteriaList = topicDAO.getCriteriaConfigs();
            double totalWeighted = 0.0;
            double totalWeight = 0.0;

            for (CriteriaConfig c : criteriaList) {
                String scoreVal = request.getParameter("score_" + c.getCriteriaId());
                String commentVal = request.getParameter("comment_" + c.getCriteriaId());

                double s = 5.0;
                if (scoreVal != null && !scoreVal.trim().isEmpty()) {
                    try {
                        s = Double.parseDouble(scoreVal.trim());
                        if (s < 0.0) s = 0.0;
                        if (s > 10.0) s = 10.0;
                    } catch (NumberFormatException ignored) {
                        s = 5.0;
                    }
                }
                criteriaScores.put(c.getCriteriaId(), s);
                totalWeighted += (s * c.getWeightPercent());
                totalWeight += c.getWeightPercent();

                if (commentVal != null && !commentVal.trim().isEmpty()) {
                    criteriaComments.put(c.getCriteriaId(), commentVal.trim());
                }
            }

            double compositeScore = (totalWeight > 0) ? (totalWeighted / totalWeight) : 0.0;

            // Defense-in-Depth Guard: Direct Approval requires compositeScore >= 5.0
            if ("APPROVED".equalsIgnoreCase(decision) && compositeScore < 5.0) {
                LOGGER.log(Level.WARNING, "Blocked illegal approval for topicId {0}: composite score {1} < 5.0 passing threshold",
                        new Object[]{topicId, String.format("%.2f", compositeScore)});
                response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=score_below_threshold");
                return;
            }

            // 5. Execute Atomic Transaction in DAL
            boolean success = topicDAO.evaluateTopic(
                    topicId,
                    currentUser.getUserId(),
                    decision,
                    comments != null ? comments.trim() : "",
                    criteriaScores,
                    criteriaComments,
                    legalRiskScore
            );

            if (success) {
                // PRG Redirect back to Pending Queue with status message
                response.sendRedirect(request.getContextPath() + "/editorial/pending-queue?msg=" + decision.toLowerCase());
            } else {
                response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=eval_failed");
            }
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Domain guard clause violation for topic " + topicId + ": " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=score_below_threshold");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error during evaluation of topic " + topicId, ex);
            response.sendRedirect(request.getContextPath() + "/editorial/criteria-scoring?topicId=" + topicId + "&error=server_error");
        }
    }
}
