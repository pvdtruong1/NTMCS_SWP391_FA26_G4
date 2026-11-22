package controller;

import dal.AssetDAO;
import dal.TopicDAO;
import dal.TopicRequestDAO;
import dal.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Asset;
import model.AssetRequest;
import model.RequestDeadlineExtension;
import model.Topic;
import model.TopicMember;
import model.User;
import util.AuthUtil;

/**
 * Controller handling Reporter requests for a Topic:
 * - GET /reporter/requests: Danh sách các đề tài ĐÃ DUYỆT (APPROVED)
 * - GET /reporter/requests?topicId=...: Màn hình gửi phiếu đề xuất xin hỗ trợ
 * - POST /reporter/requests: Xử lý lưu các đề xuất
 */
@WebServlet(name = "TopicRequestServlet", urlPatterns = {"/reporter/requests"})
public class TopicRequestServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(TopicRequestServlet.class.getName());

    private final TopicDAO topicDAO = new TopicDAO();
    private final AssetDAO assetDAO = new AssetDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TopicRequestDAO requestDAO = new TopicRequestDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User loggedUser = AuthUtil.getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String topicIdStr = request.getParameter("topicId");

        // 1. Nếu KHÔNG có topicId -> Hiển thị danh sách các đề tài của phóng viên đã APPROVED
        if (topicIdStr == null || topicIdStr.trim().isEmpty()) {
            List<Topic> approvedTopics = requestDAO.getApprovedTopicsForReporter(loggedUser.getUserId());
            request.setAttribute("approvedTopics", approvedTopics);
            request.getRequestDispatcher("/WEB-INF/views/reporter/topic-requests-list.jsp").forward(request, response);
            return;
        }

        // 2. Nếu CÓ topicId -> Mở form đề xuất cho đề tài đó
        try {
            int topicId = Integer.parseInt(topicIdStr.trim());
            Topic topic = topicDAO.getTopicById(topicId);
            if (topic == null) {
                response.sendRedirect(request.getContextPath() + "/reporter/requests?error=topicNotFound");
                return;
            }

            // Kiểm tra trạng thái duyệt của đề tài (chỉ duy nhất APPROVED đang trong giai đoạn tác nghiệp)
            boolean isApproved = "APPROVED".equalsIgnoreCase(topic.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(topic.getStatus());

            List<Asset> availableAssets = assetDAO.getAvailableAssets();
            List<User> allUsers = userDAO.getActiveReporters();
            List<TopicMember> currentMembers = topicDAO.findMembersByTopicId(topicId);
            List<AssetRequest> assetRequests = requestDAO.getAssetRequestsByTopic(topicId);
            List<RequestDeadlineExtension> deadlineRequests = requestDAO.getDeadlineRequestsByTopic(topicId);
            List<String> memberNotifications = requestDAO.getMemberChangeNotificationsByTopic(topicId);

            // 1. Tập hợp các ID đã có trong nhóm (bao gồm Tác giả/Chủ đề tài + mọi thành viên hiện tại)
            Set<Integer> existingUserIds = new HashSet<>();
            existingUserIds.add(topic.getAuthorUserId());
            if (currentMembers != null) {
                for (TopicMember m : currentMembers) {
                    existingUserIds.add(m.getUserId());
                }
            }

            // 2. Danh sách nhân sự CÓ THỂ THÊM: Chỉ những phóng viên CHƯA có mặt trong nhóm
            List<User> availableUsersToAdd = new ArrayList<>();
            if (allUsers != null) {
                for (User u : allUsers) {
                    if (!existingUserIds.contains(u.getUserId())) {
                        availableUsersToAdd.add(u);
                    }
                }
            }

            // 3. Danh sách thành viên CÓ THỂ RÚT: Loại bỏ Tác giả/Chủ đề tài và Lead Reporter
            List<TopicMember> removableMembers = new ArrayList<>();
            if (currentMembers != null) {
                for (TopicMember m : currentMembers) {
                    if (m.getUserId() != topic.getAuthorUserId() && !"LEAD_REPORTER".equalsIgnoreCase(m.getRoleInTeam())) {
                        removableMembers.add(m);
                    }
                }
            }

            request.setAttribute("topic", topic);
            request.setAttribute("isApproved", isApproved);
            request.setAttribute("availableAssets", availableAssets);
            request.setAttribute("allUsers", allUsers);
            request.setAttribute("availableUsersToAdd", availableUsersToAdd);
            request.setAttribute("currentMembers", currentMembers);
            request.setAttribute("removableMembers", removableMembers);
            request.setAttribute("assetRequests", assetRequests);
            request.setAttribute("deadlineRequests", deadlineRequests);
            request.setAttribute("memberNotifications", memberNotifications);

            request.getRequestDispatcher("/WEB-INF/views/reporter/topic-requests.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid topicId param: " + topicIdStr, e);
            response.sendRedirect(request.getContextPath() + "/reporter/requests");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        User loggedUser = AuthUtil.getLoggedUser(request);
        if (loggedUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String requestType = request.getParameter("requestType");
        String topicIdStr = request.getParameter("topicId");
        int topicId = 0;
        try {
            topicId = Integer.parseInt(topicIdStr != null ? topicIdStr.trim() : "0");
        } catch (NumberFormatException ignored) {}

        if (topicId <= 0) {
            response.sendRedirect(request.getContextPath() + "/reporter/requests");
            return;
        }

        Topic topic = topicDAO.getTopicById(topicId);
        if (topic == null) {
            response.sendRedirect(request.getContextPath() + "/reporter/requests?error=topicNotFound");
            return;
        }

        boolean isApproved = "APPROVED".equalsIgnoreCase(topic.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(topic.getStatus());

        // CHỈ ĐỀ TÀI ĐƯỢC DUYỆT (APPROVED) MỚI ĐƯỢC XIN MƯỢN TÀI SẢN HOẶC ĐỔI NHÂN SỰ
        if (("ASSET_REQUEST".equalsIgnoreCase(requestType) || "MEMBER_REQUEST".equalsIgnoreCase(requestType)) && !isApproved) {
            response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId 
                    + "&status=notApproved");
            return;
        }

        boolean success = false;
        String type = "";

        try {
            if ("ASSET_REQUEST".equalsIgnoreCase(requestType)) {
                // 1. Phóng viên xin mượn tài sản (hỗ trợ nhiều thiết bị cùng lúc)
                String[] assetIdStrs = request.getParameterValues("assetId");
                List<Integer> selectedAssetIds = new ArrayList<>();
                if (assetIdStrs != null) {
                    for (String str : assetIdStrs) {
                        if (str != null && !str.trim().isEmpty()) {
                            try {
                                int aId = Integer.parseInt(str.trim());
                                if (aId > 0 && !selectedAssetIds.contains(aId)) {
                                    selectedAssetIds.add(aId);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                if (selectedAssetIds.isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=noAsset&type=asset");
                    return;
                }

                String startTimeStr = request.getParameter("startTime");
                String endTimeStr = request.getParameter("endTime");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                Timestamp startTime = Timestamp.valueOf(LocalDateTime.parse(startTimeStr, formatter));
                Timestamp endTime = Timestamp.valueOf(LocalDateTime.parse(endTimeStr, formatter));

                // 1.1 Kiểm tra thứ tự thời gian: Giờ trả phải sau giờ mượn
                if (!endTime.after(startTime)) {
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=invalidTime&type=asset");
                    return;
                }

                // 1.2 Kiểm tra thời gian bắt đầu không được ở quá khứ (cho phép linh động 5 phút)
                Timestamp nowThreshold = new Timestamp(System.currentTimeMillis() - 5 * 60 * 1000L);
                if (startTime.before(nowThreshold)) {
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=pastTime&type=asset");
                    return;
                }

                // 1.3 Kiểm tra xung đột lịch cho từng thiết bị
                for (int aId : selectedAssetIds) {
                    if (requestDAO.hasAssetScheduleConflict(aId, startTime, endTime)) {
                        String conflictName = requestDAO.getAssetName(aId);
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=conflict&type=asset&conflictName=" + URLEncoder.encode(conflictName, StandardCharsets.UTF_8.name()));
                        return;
                    }
                }

                List<AssetRequest> requestList = new ArrayList<>();
                for (int aId : selectedAssetIds) {
                    AssetRequest req = new AssetRequest();
                    req.setTopicId(topicId);
                    req.setAssetId(aId);
                    req.setRequesterUserId(loggedUser.getUserId());
                    req.setStartTime(startTime);
                    req.setEndTime(endTime);
                    requestList.add(req);
                }

                int insertedCount = requestDAO.insertAssetRequests(requestList);
                success = (insertedCount > 0);
                type = "asset";

                response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                        + "&status=" + (success ? "success" : "failed")
                        + "&count=" + insertedCount
                        + "&type=asset");
                return;

            } else if ("DEADLINE_REQUEST".equalsIgnoreCase(requestType)) {
                // 2. Phóng viên xin gia hạn deadline
                String requestedDeadlineStr = request.getParameter("requestedDeadline");
                String reason = request.getParameter("reason");
                if (requestedDeadlineStr == null || requestedDeadlineStr.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=invalidTime&type=deadline");
                    return;
                }
                Date requestedDeadline = Date.valueOf(requestedDeadlineStr.trim());
                long currentTime = System.currentTimeMillis();
                Date today = new Date(currentTime - (currentTime % 86400000L)); // Ngày hôm nay (00:00:00)
                Date currentDeadline = (topic.getDeadline() != null) ? topic.getDeadline() : today;

                // Chặn tuyệt đối: Không được chọn ngày trong quá khứ hoặc trước hạn nộp hiện tại
                if (requestedDeadline.before(today) || requestedDeadline.before(currentDeadline)) {
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=pastTime&type=deadline");
                    return;
                }

                RequestDeadlineExtension req = new RequestDeadlineExtension();
                req.setTopicId(topicId);
                req.setRequestedByUserId(loggedUser.getUserId());
                req.setCurrentDeadline(currentDeadline);
                req.setRequestedDeadline(requestedDeadline);
                req.setReason(reason != null ? reason.trim() : "");

                success = requestDAO.insertDeadlineExtensionRequest(req);
                type = "deadline";

            } else if ("MEMBER_REQUEST".equalsIgnoreCase(requestType)) {
                // 3. Phóng viên xin thay đổi nhân sự
                int targetUserId = Integer.parseInt(request.getParameter("targetUserId"));
                String actionType = request.getParameter("actionType");
                String reason = request.getParameter("reason");

                if ("ADD".equalsIgnoreCase(actionType)) {
                    if (!userDAO.isReporter(targetUserId)) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=invalidRole&type=member");
                        return;
                    }
                    // Kiểm tra nhân sự đã có trong nhóm hay chưa
                    List<TopicMember> currentMembers = topicDAO.findMembersByTopicId(topicId);
                    boolean alreadyInTeam = (targetUserId == topic.getAuthorUserId());
                    if (!alreadyInTeam && currentMembers != null) {
                        for (TopicMember m : currentMembers) {
                            if (m.getUserId() == targetUserId) {
                                alreadyInTeam = true;
                                break;
                            }
                        }
                    }
                    if (alreadyInTeam) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=alreadyMember&type=member");
                        return;
                    }
                } else if ("REMOVE".equalsIgnoreCase(actionType)) {
                    // Không được phép xin rút tác giả / người chủ trì đề tài
                    if (targetUserId == topic.getAuthorUserId()) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=cannotRemoveAuthor&type=member");
                        return;
                    }
                    // Kiểm tra nhân sự có thực sự đang nằm trong nhóm hay không và có phải Lead không
                    List<TopicMember> currentMembers = topicDAO.findMembersByTopicId(topicId);
                    boolean inTeam = false;
                    boolean isLead = false;
                    if (currentMembers != null) {
                        for (TopicMember m : currentMembers) {
                            if (m.getUserId() == targetUserId) {
                                inTeam = true;
                                if ("LEAD_REPORTER".equalsIgnoreCase(m.getRoleInTeam())) {
                                    isLead = true;
                                }
                                break;
                            }
                        }
                    }
                    if (isLead) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=cannotRemoveAuthor&type=member");
                        return;
                    }
                    if (!inTeam) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=notInTeam&type=member");
                        return;
                    }
                }

                success = requestDAO.sendMemberChangeNotification(topicId, loggedUser.getUserId(), targetUserId, actionType, reason);
                type = "member";
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing Topic request: " + requestType, e);
            success = false;
        }

        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                + "&status=" + (success ? "success" : "failed")
                + "&type=" + type);
    }
}
