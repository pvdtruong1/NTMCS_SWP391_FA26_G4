package controller.editorial;

import dal.TopicDAO;
import dal.TopicRequestDAO;
import dto.MemberChangeRequestDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Desk;
import model.User;
import util.AuthUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for evaluating reporter's member change requests (Add / Remove crew member).
 * Part of Editorial Workflow (WF-02).
 * 
 * Actor: Desk Editor (Phạm Vũ Đình Trường - Role 2) & Executive Editor (TBT - Role 3)
 */
@WebServlet(name = "MemberRequestServlet", urlPatterns = {"/editorial/member-requests"})
public class MemberRequestServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(MemberRequestServlet.class.getName());
    private final TopicRequestDAO requestDAO = new TopicRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        if (!AuthUtil.check(request, response, "TOPIC_EVALUATE")) {
            return;
        }

        User currentUser = AuthUtil.getLoggedUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        boolean isExecutive = (currentUser.getRoleId() == 3 || currentUser.getRoleId() == 7
                || "EXECUTIVE_EDITOR".equalsIgnoreCase(currentUser.getRoleName())
                || "SYSTEM_ADMINISTRATOR".equalsIgnoreCase(currentUser.getRoleName()));

        int deskId = (currentUser.getDeskId() != null) ? currentUser.getDeskId() : 1;
        if (isExecutive) {
            String deskParam = request.getParameter("deskId");
            if (deskParam != null && !deskParam.trim().isEmpty()) {
                try {
                    deskId = Integer.parseInt(deskParam.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        String statusFilter = request.getParameter("status");
        if (statusFilter == null || statusFilter.trim().isEmpty()) {
            statusFilter = "PENDING";
        }

        List<MemberChangeRequestDTO> allRequests = requestDAO.getMemberChangeRequests(deskId, isExecutive);

        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        List<MemberChangeRequestDTO> filteredList = new ArrayList<>();
        for (MemberChangeRequestDTO req : allRequests) {
            if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                pendingCount++;
            } else if ("APPROVED".equalsIgnoreCase(req.getStatus())) {
                approvedCount++;
            } else if ("REJECTED".equalsIgnoreCase(req.getStatus())) {
                rejectedCount++;
            }

            if ("ALL".equalsIgnoreCase(statusFilter) || statusFilter.equalsIgnoreCase(req.getStatus())) {
                filteredList.add(req);
            }
        }

        // Lấy danh sách Desk cho scope switcher nếu là TBT/Admin
        try (TopicDAO topicDAO = new TopicDAO()) {
            List<Desk> deskList = topicDAO.getAllDesks();
            request.setAttribute("deskList", deskList);
        } catch (Exception ignored) {}

        request.setAttribute("requests", filteredList);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("pendingCount", pendingCount);
        request.setAttribute("approvedCount", approvedCount);
        request.setAttribute("rejectedCount", rejectedCount);
        request.setAttribute("totalCount", allRequests.size());
        request.setAttribute("selectedDeskId", deskId);
        request.setAttribute("isExecutive", isExecutive);

        request.getRequestDispatcher("/WEB-INF/views/editorial/member-requests.jsp").forward(request, response);
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
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        String notificationIdStr = request.getParameter("notificationId");
        String feedback = request.getParameter("feedback");

        int notificationId = 0;
        try {
            notificationId = Integer.parseInt(notificationIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/editorial/member-requests?error=invalid_id");
            return;
        }

        boolean success = false;
        String actionMsg = "";

        if ("approve".equalsIgnoreCase(action)) {
            success = requestDAO.approveMemberChangeRequest(notificationId, currentUser.getUserId(), feedback);
            actionMsg = success ? "approved" : "approve_failed";
        } else if ("reject".equalsIgnoreCase(action)) {
            success = requestDAO.rejectMemberChangeRequest(notificationId, currentUser.getUserId(), feedback);
            actionMsg = success ? "rejected" : "reject_failed";
        } else {
            response.sendRedirect(request.getContextPath() + "/editorial/member-requests?error=unknown_action");
            return;
        }

        String redirectUrl = request.getContextPath() + "/editorial/member-requests?status=PENDING&result=" + actionMsg;
        response.sendRedirect(redirectUrl);
    }
}
