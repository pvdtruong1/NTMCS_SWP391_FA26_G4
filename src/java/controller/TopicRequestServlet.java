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

    /**
     * Xử lý điều hướng các yêu cầu GET của Phân hệ Đề Xuất & Xin Hỗ Trợ Đề Tài (Topic Requests):
     * 1. Khi KHÔNG truyền topicId: Hiển thị danh sách các đề tài đã APPROVED/IN_PROGRESS của phóng viên (topic-requests-list.jsp).
     * 2. Khi CÓ truyền topicId: Mở form phiếu đề xuất (topic-requests.jsp) gồm 3 tab chức năng:
     *    - Tab 1: Mượn trang thiết bị / phương tiện tác nghiệp (Asset Request).
     *    - Tab 2: Xin gia hạn thời hạn nộp bài (Deadline Extension Request).
     *    - Tab 3: Đề xuất điều chỉnh nhân sự tổ tác nghiệp (bổ sung qua email hoặc rút bớt thành viên).
     *    - Tab 4: Lịch sử các đề xuất đã gửi.
     */
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
            Topic topic = topicDAO.getTopicDetailForReporter(topicId, loggedUser.getUserId());
            if (topic == null) {
                topic = topicDAO.getTopicById(topicId);
            }
            if (topic == null) {
                response.sendRedirect(request.getContextPath() + "/reporter/requests?error=topicNotFound");
                return;
            }

            if (topic.getReporterName() == null || topic.getReporterName().trim().isEmpty()) {
                User author = userDAO.getById(topic.getAuthorUserId());
                if (author != null) {
                    topic.setReporterName(author.getFullName());
                    topic.setReporterEmail(author.getEmail());
                }
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

            String flashError = (String) request.getSession().getAttribute("flashError");
            if (flashError != null) {
                request.setAttribute("error", flashError);
                request.getSession().removeAttribute("flashError");
            }
            @SuppressWarnings("unchecked")
            List<String> flashEmails = (List<String>) request.getSession().getAttribute("flashEmails");
            if (flashEmails != null) {
                request.setAttribute("formEmails", flashEmails);
                request.getSession().removeAttribute("flashEmails");
            }
            String flashReason = (String) request.getSession().getAttribute("flashReason");
            if (flashReason != null) {
                request.setAttribute("formReason", flashReason);
                request.getSession().removeAttribute("flashReason");
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

    /**
     * Xử lý tiếp nhận và xác thực dữ liệu POST của các loại đề xuất (requestType):
     * 1. ASSET_REQUEST: Đề xuất mượn 1 hoặc nhiều trang thiết bị (kiểm tra xung đột lịch mượn,
     *    thời gian bắt đầu/kết thúc không ở quá khứ, lưu vào bảng Asset_Request).
     * 2. DEADLINE_REQUEST: Xin gia hạn hạn nộp bài (kiểm tra hạn mới phải sau hôm nay và sau hạn cũ,
     *    lưu vào Request_Deadline_Extension).
     * 3. MEMBER_REQUEST: Đề xuất thay đổi nhân sự nhóm tác nghiệp:
     *    - Action "ADD": Bổ sung phóng viên theo danh sách email (kiểm tra email tồn tại, active, role REPORTER,
     *      không trùng chính mình, không trùng người đã trong nhóm, gửi thông báo [PENDING] và ghi audit log).
     *    - Action "REMOVE": Rút thành viên (chặn rút tác giả/chủ đề tài hoặc Lead Reporter, gửi thông báo [PENDING]).
     */
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
                String actionType = request.getParameter("actionType");
                String reason = request.getParameter("reason");

                if ("ADD".equalsIgnoreCase(actionType)) {
                    String[] emailValues = request.getParameterValues("targetUserEmail");
                    List<String> rawEmailList = new ArrayList<>();
                    if (emailValues != null) {
                        for (String em : emailValues) {
                            if (em != null && !em.trim().isEmpty()) {
                                rawEmailList.add(em.trim());
                            }
                        }
                    }

                    if (rawEmailList.isEmpty()) {
                        request.getSession().setAttribute("flashError", "Vui lòng nhập ít nhất một địa chỉ email phóng viên muốn bổ sung.");
                        request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                        return;
                    }

                    // Lấy danh sách thành viên hiện tại của đề tài
                    List<TopicMember> currentMembers = topicDAO.findMembersByTopicId(topicId);
                    Set<Integer> currentMemberUserIds = new HashSet<>();
                    currentMemberUserIds.add(topic.getAuthorUserId());
                    if (currentMembers != null) {
                        for (TopicMember m : currentMembers) {
                            currentMemberUserIds.add(m.getUserId());
                        }
                    }

                    Set<String> seenEmails = new HashSet<>();
                    List<User> validTargetUsers = new ArrayList<>();

                    for (String em : rawEmailList) {
                        String lower = em.toLowerCase();
                        if (seenEmails.contains(lower)) {
                            request.getSession().setAttribute("flashError", "Email '" + em + "' bị nhập trùng lặp nhiều lần trong danh sách.");
                            request.getSession().setAttribute("flashEmails", rawEmailList);
                            request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                            response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                            return;
                        }
                        seenEmails.add(lower);

                        // Không được thêm chính mình
                        if (loggedUser.getEmail() != null && lower.equalsIgnoreCase(loggedUser.getEmail())) {
                            request.getSession().setAttribute("flashError", "Bạn (" + em + ") là người tạo yêu cầu, không thể tự thêm chính mình vào danh sách xin bổ sung.");
                            request.getSession().setAttribute("flashEmails", rawEmailList);
                            request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                            response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                            return;
                        }

                        // Kiểm tra trong DB: tồn tại, active, role REPORTER
                        User u = userDAO.getActiveReporterByEmail(em);
                        if (u == null) {
                            User existingUser = userDAO.getByEmail(em);
                            String detailMsg;
                            if (existingUser == null) {
                                detailMsg = "Không tìm thấy người dùng với email '" + em + "'.";
                            } else if (!existingUser.isActiveStatus()) {
                                detailMsg = "Tài khoản phóng viên '" + em + "' đang bị vô hiệu hóa (INACTIVE).";
                            } else {
                                detailMsg = "Người dùng '" + em + "' không có vai trò Phóng viên (REPORTER).";
                            }
                            request.getSession().setAttribute("flashError", detailMsg);
                            request.getSession().setAttribute("flashEmails", rawEmailList);
                            request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                            response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                            return;
                        }

                        // Kiểm tra xem đã có trong nhóm chưa
                        if (currentMemberUserIds.contains(u.getUserId())) {
                            request.getSession().setAttribute("flashError", "Phóng viên " + u.getFullName() + " (" + em + ") đã có mặt trong ekip đề tài này rồi.");
                            request.getSession().setAttribute("flashEmails", rawEmailList);
                            request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                            response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                            return;
                        }

                        validTargetUsers.add(u);
                    }

                    success = requestDAO.sendMultiMemberAddNotification(topicId, loggedUser.getUserId(), validTargetUsers, reason);
                    type = "member";
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=" + (success ? "success" : "failed")
                            + "&type=" + type + "#member");
                    return;

                } else if ("REMOVE".equalsIgnoreCase(actionType)) {
                    int targetUserId = 0;
                    try {
                        targetUserId = Integer.parseInt(request.getParameter("targetUserId"));
                    } catch (NumberFormatException ignored) {}

                    if (targetUserId <= 0) {
                        request.getSession().setAttribute("flashError", "Vui lòng chọn thành viên cần rút khỏi ekip.");
                        request.getSession().setAttribute("flashReason", reason != null ? reason.trim() : "");
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId + "#member");
                        return;
                    }

                    // Không được phép xin rút tác giả / người chủ trì đề tài
                    if (targetUserId == topic.getAuthorUserId()) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=cannotRemoveAuthor&type=member#member");
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
                                + "&status=cannotRemoveAuthor&type=member#member");
                        return;
                    }
                    if (!inTeam) {
                        response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                                + "&status=notInTeam&type=member#member");
                        return;
                    }

                    success = requestDAO.sendMemberChangeNotification(topicId, loggedUser.getUserId(), targetUserId, actionType, reason);
                    type = "member";
                    response.sendRedirect(request.getContextPath() + "/reporter/requests?topicId=" + topicId
                            + "&status=" + (success ? "success" : "failed")
                            + "&type=" + type + "#member");
                    return;
                }
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
