<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<%
    // Đảm bảo danh sách thông báo đề xuất nhân sự của đề tài luôn được lấy mới nhất từ bảng Notification
    java.util.List<String> notifs = (java.util.List<String>) request.getAttribute("memberNotifications");
    if (notifs == null || notifs.isEmpty()) {
        notifs = new java.util.ArrayList<String>();
        try {
            model.Topic tObj = (model.Topic) request.getAttribute("topic");
            if (tObj != null) {
                try (java.sql.Connection conn = new dal.DBContext().getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement(
                        "SELECT message FROM Notification WHERE topic_id = ? AND (message LIKE '[[]ĐỀ XUẤT NHÂN SỰ]%' OR message LIKE N'%ĐỀ XUẤT NHÂN SỰ%') ORDER BY notification_id DESC")) {
                    ps.setInt(1, tObj.getTopicId());
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            notifs.add(rs.getString("message"));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        request.setAttribute("memberNotifications", notifs);
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đề Xuất & Xin Hỗ Trợ Đề Tài — NTMCS</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
    <style>
        .nav-pills .nav-link {
            border-radius: 8px;
            padding: 10px 18px;
            color: #495057;
        }
        .nav-pills .nav-link.active {
            background-color: #0d6efd;
            color: #fff;
            box-shadow: 0 2px 4px rgba(13,110,253,0.3);
        }
        .topic-summary-card {
            border-radius: 10px;
            background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        }
    </style>
</head>
<body class="bg-light">
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-requests"/>
    </jsp:include>

    <div class="main p-4">
        <!-- Breadcrumb & Top Bar -->
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1">
                        <li class="breadcrumb-item"><a href="${base}/reporter/requests">Đề xuất xin hỗ trợ</a></li>
                        <li class="breadcrumb-item"><a href="${base}/reporter/detail?id=${topic.topicId}"><c:out value="${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}"/></a></li>
                        <li class="breadcrumb-item active" aria-current="page">Phiếu đề xuất</li>
                    </ol>
                </nav>
                <h3 class="fw-bold mb-0 text-primary">
                    <i class="bi bi-file-earmark-diff me-2"></i>Phiếu Đề Xuất & Xin Hỗ Trợ
                </h3>
            </div>
            <div class="d-flex align-items-center gap-2">
                <!-- Notification Bell Button -->
                <div class="position-relative">
                    <button class="btn btn-outline-secondary position-relative" type="button" title="Thông báo hệ thống" onclick="toggleReqNotifDropdown()">
                        <i class="bi bi-bell-fill text-warning"></i>
                        <span id="reqNotifBadge" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="display:none;">0</span>
                    </button>
                    <div id="reqNotifDropdown" class="position-absolute end-0 mt-2 bg-white rounded-3 shadow border" style="display:none; width:340px; z-index:1050; overflow:hidden;">
                        <div class="p-2 px-3 border-bottom d-flex justify-content-between align-items-center bg-light">
                            <span class="fw-bold small text-dark"><i class="bi bi-bell me-1"></i>Thông báo của bạn</span>
                            <button type="button" class="btn btn-link btn-sm text-decoration-none p-0" onclick="markAllReqNotifsRead()">Đã đọc hết</button>
                        </div>
                        <div id="reqNotifList" class="p-2" style="max-height:300px; overflow-y:auto;">
                            <div class="text-center text-muted py-3 small">Đang tải thông báo...</div>
                        </div>
                    </div>
                </div>

                <a href="${base}/reporter/requests" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i> Danh sách đề tài đã duyệt
                </a>
                <a href="${base}/reporter/detail?id=${topic.topicId}" class="btn btn-outline-primary">
                    <i class="bi bi-eye me-1"></i> Xem chi tiết đề tài
                </a>
            </div>
        </div>

        <!-- THÔNG BÁO ALERT KHI GỬI FORM THÀNH CÔNG HOẶC THẤT BẠI -->
        <c:if test="${param.status eq 'success'}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                <strong>Thành công!</strong>
                <c:choose>
                    <c:when test="${param.type eq 'asset'}">
                        Yêu cầu mượn tài sản đã được tạo thành công và đang chuyển cho Quản lý tài sản duyệt.
                    </c:when>
                    <c:when test="${param.type eq 'deadline'}">
                        Đơn xin gia hạn Deadline đã được chuyển tới Thư ký tòa soạn (Desk Editor) xem xét.
                    </c:when>
                    <c:when test="${param.type eq 'member'}">
                        Đề xuất thay đổi nhân sự nhóm đã được gửi thành công dưới dạng thông báo tới Ban biên tập.
                    </c:when>
                    <c:otherwise>
                        Yêu cầu của bạn đã được ghi nhận vào hệ thống.
                    </c:otherwise>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${param.status eq 'notApproved'}">
            <div class="alert alert-warning alert-dismissible fade show shadow-sm mb-4" role="alert">
                <i class="bi bi-shield-lock-fill me-2 fs-5"></i>
                <strong>Không đủ điều kiện!</strong> Chỉ những đề tài đã được phê duyệt chính thức (APPROVED) mới có quyền xin cấp tài sản hoặc thay đổi nhân sự.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${param.status eq 'failed'}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <strong>Lỗi xử lý!</strong> Không thể gửi yêu cầu. Vui lòng kiểm tra lại thời gian hoặc dữ liệu đã nhập.
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- THÔNG TIN TÓM TẮT ĐỀ TÀI -->
        <div class="card border-0 shadow-sm mb-4 topic-summary-card">
            <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                    <div>
                        <span class="badge bg-secondary mb-2"><c:out value="${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}"/></span>
                        <h4 class="fw-bold text-dark mb-1"><c:out value="${topic.title}"/></h4>
                        <div class="text-muted"><c:out value="${topic.angle}"/></div>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${topic.status eq 'APPROVED'}">
                                <span class="badge bg-success fs-6 px-3 py-2"><i class="bi bi-check-circle me-1"></i> ĐÃ DUYỆT (APPROVED)</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary fs-6 px-3 py-2"><c:out value="${topic.status}"/></span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="row g-3 pt-2 border-top">
                    <div class="col-md-3 col-6">
                        <small class="text-muted d-block">Chuyên mục / Ban</small>
                        <strong class="text-dark"><c:out value="${topic.categoryName}"/> (<c:out value="${topic.deskName}"/>)</strong>
                    </div>
                    <div class="col-md-3 col-6">
                        <small class="text-muted d-block">Thể loại / Độ ưu tiên</small>
                        <strong class="text-dark"><c:out value="${topic.genre}"/> &middot; <span class="badge bg-light text-dark border"><c:out value="${topic.priority}"/></span></strong>
                    </div>
                    <div class="col-md-3 col-6">
                        <small class="text-muted d-block">Hạn nộp hiện tại (Deadline)</small>
                        <strong class="text-danger"><i class="bi bi-calendar-event me-1"></i><c:out value="${topic.deadline}"/></strong>
                    </div>
                    <div class="col-md-3 col-6">
                        <small class="text-muted d-block">Số thành viên nhóm</small>
                        <strong class="text-dark"><i class="bi bi-people me-1"></i><c:out value="${not empty currentMembers ? currentMembers.size() : 0}"/> người</strong>
                    </div>
                </div>
            </div>
        </div>

        <!-- TABS GIAO DIỆN 3 LOẠI REQUEST + LỊCH SỬ -->
        <div class="card border-0 shadow-sm">
            <div class="card-header bg-white border-0 pt-3 px-4">
                <ul class="nav nav-pills card-header-pills" id="requestTabs" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active fw-semibold" id="asset-tab" data-bs-toggle="pill" data-bs-target="#asset-pane" type="button" role="tab">
                            <i class="bi bi-box-seam me-1"></i> 1. Mượn trang thiết bị / Tài sản
                            <c:if test="${not isApproved}"><i class="bi bi-lock-fill ms-1 text-muted" title="Cần trạng thái APPROVED"></i></c:if>
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="deadline-tab" data-bs-toggle="pill" data-bs-target="#deadline-pane" type="button" role="tab">
                            <i class="bi bi-clock-history me-1"></i> 2. Xin gia hạn Deadline
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="member-tab" data-bs-toggle="pill" data-bs-target="#member-pane" type="button" role="tab">
                            <i class="bi bi-person-gear me-1"></i> 3. Đề xuất nhân sự
                            <c:if test="${not isApproved}"><i class="bi bi-lock-fill ms-1 text-muted" title="Cần trạng thái APPROVED"></i></c:if>
                        </button>
                    </li>
                    <li class="nav-item ms-auto" role="presentation">
                        <button class="nav-link fw-semibold text-secondary" id="history-tab" data-bs-toggle="pill" data-bs-target="#history-pane" type="button" role="tab">
                            <i class="bi bi-list-check me-1"></i> Lịch sử đề xuất
                            <span class="badge bg-secondary ms-1">${assetRequests.size() + deadlineRequests.size() + memberNotifications.size()}</span>
                        </button>
                    </li>
                </ul>
            </div>

            <div class="card-body p-4">
                <div class="tab-content" id="requestTabsContent">

                    <!-- ================= TAB 1: MƯỢN TÀI SẢN ================= -->
                    <div class="tab-pane fade show active" id="asset-pane" role="tabpanel">
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="border-bottom pb-3 mb-4">
                                    <h5 class="fw-bold mb-1 text-primary"><i class="bi bi-camera-reels me-2"></i>Đề xuất xin cấp phát / mượn thiết bị tác nghiệp</h5>
                                    <small class="text-muted">Chỉ áp dụng cho các đề tài đã được duyệt chính thức. Tài sản mượn từ kho thiết bị của tòa soạn.</small>
                                </div>

                                <c:choose>
                                    <c:when test="${not isApproved}">
                                        <div class="card bg-light border-0 p-4 text-center my-3">
                                            <i class="bi bi-lock-fill fs-1 text-secondary mb-2"></i>
                                            <h6 class="fw-bold text-dark">Chức năng mượn tài sản đang bị khóa</h6>
                                            <p class="text-muted small mb-0">Đề tài này hiện đang ở trạng thái <strong>${topic.status}</strong>. Chỉ những đề tài đã được duyệt chính thức (<strong>APPROVED</strong>) mới đủ điều kiện xin cấp phát trang thiết bị tác nghiệp thực địa.</p>
                                        </div>
                                    </c:when>

                                    <c:otherwise>
                                        <form action="${base}/reporter/requests" method="POST">
                                            <input type="hidden" name="requestType" value="ASSET_REQUEST">
                                            <input type="hidden" name="topicId" value="${topic.topicId}">

                                            <div class="mb-3">
                                                <label for="assetId" class="form-label fw-semibold">Chọn tài sản / thiết bị cần mượn <span class="text-danger">*</span></label>
                                                <select class="form-select" id="assetId" name="assetId" required>
                                                    <option value="">-- Chọn tài sản khả dụng từ kho --</option>
                                                    <c:forEach var="a" items="${availableAssets}">
                                                        <option value="${a.assetId}">
                                                            [${a.assetCategory}] ${a.assetName} (${a.assetCode}) - Serial: ${a.serialNumber != null ? a.serialNumber : 'N/A'}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <div class="form-text">Chỉ các tài sản đang ở trạng thái khả dụng (AVAILABLE) mới được hiển thị.</div>
                                            </div>

                                            <div class="row g-3 mb-4">
                                                <div class="col-md-6">
                                                    <label for="startTime" class="form-label fw-semibold">Thời gian bắt đầu mượn <span class="text-danger">*</span></label>
                                                    <input type="datetime-local" class="form-control" id="startTime" name="startTime" required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label for="endTime" class="form-label fw-semibold">Thời gian dự kiến hoàn trả <span class="text-danger">*</span></label>
                                                    <input type="datetime-local" class="form-control" id="endTime" name="endTime" required>
                                                </div>
                                            </div>

                                            <div class="text-end">
                                                <button type="submit" class="btn btn-primary px-4 py-2">
                                                    <i class="bi bi-send me-1"></i> Gửi yêu cầu mượn thiết bị
                                                </button>
                                            </div>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- ================= TAB 2: GIA HẠN DEADLINE ================= -->
                    <div class="tab-pane fade" id="deadline-pane" role="tabpanel">
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="border-bottom pb-3 mb-4">
                                    <h5 class="fw-bold mb-1 text-warning text-dark"><i class="bi bi-hourglass-split me-2"></i>Xin gia hạn hạn nộp bài (Deadline)</h5>
                                    <small class="text-muted">Đơn xin gia hạn sẽ được chuyển trực tiếp tới Thư ký tòa soạn (Desk Editor) xem xét phê duyệt.</small>
                                </div>

                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="DEADLINE_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="mb-3">
                                        <label class="form-label fw-semibold">Hạn nộp hiện tại của đề tài</label>
                                        <input type="text" class="form-control bg-light" value="${topic.deadline}" readonly>
                                        <div class="form-text">Hạn nộp đã được ấn định khi đề tài khởi tạo/duyệt.</div>
                                    </div>

                                    <div class="mb-3">
                                        <label for="requestedDeadline" class="form-label fw-semibold">Hạn nộp mới mong muốn <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control" id="requestedDeadline" name="requestedDeadline" required min="${topic.deadline}">
                                        <div class="form-text">Hạn mới phải sau hạn nộp hiện tại.</div>
                                    </div>

                                    <div class="mb-4">
                                        <label for="deadlineReason" class="form-label fw-semibold">Lý do xin gia hạn <span class="text-danger">*</span></label>
                                        <textarea class="form-control" id="deadlineReason" name="reason" rows="4" placeholder="Giải trình cụ thể lý do khách quan/chủ quan cần kéo dài thời gian tác nghiệp..." required></textarea>
                                    </div>

                                    <div class="text-end">
                                        <button type="submit" class="btn btn-warning px-4 py-2 text-dark fw-semibold">
                                            <i class="bi bi-calendar-plus me-1"></i> Gửi đơn xin gia hạn
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- ================= TAB 3: ĐỀ XUẤT NHÂN SỰ ================= -->
                    <div class="tab-pane fade" id="member-pane" role="tabpanel">
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="border-bottom pb-3 mb-4">
                                    <h5 class="fw-bold mb-1 text-success"><i class="bi bi-people-fill me-2"></i>Đề xuất thay đổi nhân sự nhóm đề tài</h5>
                                    <small class="text-muted">Chỉ dành cho đề tài đã duyệt. Đề xuất gửi tới Ban biên tập để bổ sung hoặc rút bớt thành viên.</small>
                                </div>

                                <c:choose>
                                    <c:when test="${not isApproved}">
                                        <div class="card bg-light border-0 p-4 text-center my-3">
                                            <i class="bi bi-lock-fill fs-1 text-secondary mb-2"></i>
                                            <h6 class="fw-bold text-dark">Chức năng thay đổi nhân sự đang bị khóa</h6>
                                            <p class="text-muted small mb-0">Đề tài này chưa được duyệt (trạng thái: <strong>${topic.status}</strong>). Chỉ những đề tài đã duyệt chính thức (<strong>APPROVED</strong>) mới có quyền gửi đề xuất thêm hoặc rút bớt nhân sự nhóm.</p>
                                        </div>
                                    </c:when>

                                    <c:otherwise>
                                        <form action="${base}/reporter/requests" method="POST">
                                            <input type="hidden" name="requestType" value="MEMBER_REQUEST">
                                            <input type="hidden" name="topicId" value="${topic.topicId}">

                                            <div class="mb-3">
                                                <label class="form-label fw-semibold d-block">Loại hành động <span class="text-danger">*</span></label>
                                                <div class="btn-group w-100" role="group">
                                                    <input type="radio" class="btn-check" name="actionType" id="actionAdd" value="ADD" checked onchange="toggleMemberSelect('ADD')">
                                                    <label class="btn btn-outline-success py-2" for="actionAdd">
                                                        <i class="bi bi-person-plus-fill me-1"></i> Xin bổ sung thêm người vào nhóm
                                                    </label>

                                                    <input type="radio" class="btn-check" name="actionType" id="actionRemove" value="REMOVE" onchange="toggleMemberSelect('REMOVE')">
                                                    <label class="btn btn-outline-danger py-2" for="actionRemove">
                                                        <i class="bi bi-person-dash-fill me-1"></i> Xin rút / xóa người khỏi nhóm
                                                    </label>
                                                </div>
                                            </div>

                                            <!-- Chọn khi THÊM NGƯỜI (danh sách user tòa soạn) -->
                                            <div class="mb-3" id="selectAddGroup">
                                                <label for="targetUserAdd" class="form-label fw-semibold">Chọn nhân sự muốn thêm vào nhóm <span class="text-danger">*</span></label>
                                                <select class="form-select" id="targetUserAdd" name="targetUserId" required>
                                                    <option value="">-- Chọn phóng viên / biên tập viên --</option>
                                                    <c:forEach var="u" items="${allUsers}">
                                                        <option value="${u.userId}">${u.fullName} (${u.email}) - Vị trí: ${u.roleName}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <!-- Chọn khi RÚT NGƯỜI (chỉ thành viên đang trong nhóm đề tài) -->
                                            <div class="mb-3 d-none" id="selectRemoveGroup">
                                                <label for="targetUserRemove" class="form-label fw-semibold">Chọn thành viên muốn rút khỏi nhóm <span class="text-danger">*</span></label>
                                                <select class="form-select" id="targetUserRemove">
                                                    <option value="">-- Chọn thành viên nhóm hiện tại --</option>
                                                    <c:forEach var="m" items="${currentMembers}">
                                                        <option value="${m.userId}">${m.userName} (${m.userEmail}) - Vai trò: ${m.roleInTeam}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <div class="mb-4">
                                                <label for="memberReason" class="form-label fw-semibold">Lý do đề xuất <span class="text-danger">*</span></label>
                                                <textarea class="form-control" id="memberReason" name="reason" rows="3" placeholder="Nêu rõ lý do cần bổ sung thêm người hoặc rút thành viên khỏi đề tài..." required></textarea>
                                            </div>

                                            <div class="text-end">
                                                <button type="submit" class="btn btn-success px-4 py-2">
                                                    <i class="bi bi-check2-circle me-1"></i> Gửi đề xuất nhân sự
                                                </button>
                                            </div>
                                        </form>

                                        <!-- HIỂN THỊ CÁC THÔNG BÁO / ĐỀ XUẤT NHÂN SỰ ĐÃ GỬI TRỰC TIẾP TẠI TAB NÀY -->
                                        <div class="mt-5 border-top pt-4">
                                            <h6 class="fw-bold text-dark mb-3">
                                                <i class="bi bi-bell-fill text-success me-1"></i> Các đề xuất nhân sự đã gửi cho đề tài này (${not empty memberNotifications ? memberNotifications.size() : 0})
                                            </h6>
                                            <div class="list-group">
                                                <c:forEach var="msg" items="${memberNotifications}">
                                                    <div class="list-group-item list-group-item-action d-flex align-items-start gap-2 py-3 bg-light">
                                                        <span class="badge bg-success mt-1"><i class="bi bi-send-check"></i> Đã gửi</span>
                                                        <div class="flex-grow-1">
                                                            <div class="text-dark fw-medium"><c:out value="${msg}"/></div>
                                                            <small class="text-muted"><i class="bi bi-info-circle me-1"></i>Đã tạo thông báo gửi tới Ban biên tập & lưu vết hệ thống</small>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                                <c:if test="${empty memberNotifications}">
                                                    <div class="text-center text-muted py-3 border rounded bg-white small">
                                                        Chưa có đề xuất thay đổi nhân sự nào cho đề tài này.
                                                    </div>
                                                </c:if>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- ================= TAB 4: LỊCH SỬ ĐỀ XUẤT ================= -->
                    <div class="tab-pane fade" id="history-pane" role="tabpanel">
                        <div class="border-bottom pb-2 mb-3">
                            <h5 class="fw-bold mb-0 text-secondary"><i class="bi bi-journal-text me-2"></i>Lịch sử các yêu cầu đã gửi cho Đề tài này</h5>
                        </div>

                        <!-- 1. Yêu cầu mượn tài sản -->
                        <h6 class="fw-bold mt-4 text-primary"><i class="bi bi-camera me-1"></i> 1. Yêu cầu mượn tài sản (Asset Requests)</h6>
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Mã YC</th>
                                        <th>Tài sản</th>
                                        <th>Loại</th>
                                        <th>Thời gian mượn - trả</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="ar" items="${assetRequests}">
                                        <tr>
                                            <td>#${ar.requestId}</td>
                                            <td><strong>${ar.assetName}</strong> <small class="text-muted">(${ar.assetCode})</small></td>
                                            <td><span class="badge bg-light text-dark border">${ar.assetCategory}</span></td>
                                            <td><small>${ar.startTime} &rarr; ${ar.endTime}</small></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${ar.status eq 'APPROVED'}"><span class="badge bg-success">Đã duyệt</span></c:when>
                                                    <c:when test="${ar.status eq 'REJECTED'}"><span class="badge bg-danger">Bị từ chối</span></c:when>
                                                    <c:otherwise><span class="badge bg-warning text-dark">Chờ duyệt</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty assetRequests}">
                                        <tr><td colspan="5" class="text-center text-muted py-3">Chưa có yêu cầu mượn tài sản nào.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>

                        <!-- 2. Yêu cầu gia hạn Deadline -->
                        <h6 class="fw-bold mt-4 text-warning text-dark"><i class="bi bi-calendar-plus me-1"></i> 2. Yêu cầu xin gia hạn Deadline</h6>
                        <div class="table-responsive">
                            <table class="table table-bordered table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Mã YC</th>
                                        <th>Hạn nộp gốc</th>
                                        <th>Hạn mới xin</th>
                                        <th>Lý do giải trình</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="dr" items="${deadlineRequests}">
                                        <tr>
                                            <td>#${dr.requestId}</td>
                                            <td class="text-muted"><del>${dr.currentDeadline}</del></td>
                                            <td class="text-danger fw-bold">${dr.requestedDeadline}</td>
                                            <td><c:out value="${dr.reason}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${dr.status eq 'APPROVED'}"><span class="badge bg-success">Đã duyệt</span></c:when>
                                                    <c:when test="${dr.status eq 'REJECTED'}"><span class="badge bg-danger">Bị từ chối</span></c:when>
                                                    <c:otherwise><span class="badge bg-warning text-dark">Chờ duyệt</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty deadlineRequests}">
                                        <tr><td colspan="5" class="text-center text-muted py-3">Chưa có yêu cầu gia hạn nào.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>

                        <!-- 3. Đề xuất đổi nhân sự (TỪ BẢNG NOTIFICATION) -->
                        <h6 class="fw-bold mt-4 text-success"><i class="bi bi-people me-1"></i> 3. Đề xuất thay đổi nhân sự nhóm (${not empty memberNotifications ? memberNotifications.size() : 0})</h6>
                        <div class="list-group">
                            <c:forEach var="msg" items="${memberNotifications}">
                                <div class="list-group-item list-group-item-action d-flex align-items-start gap-2 py-3 bg-light">
                                    <span class="badge bg-success mt-1"><i class="bi bi-send-check"></i> Đã gửi</span>
                                    <div class="flex-grow-1">
                                        <div class="text-dark fw-medium"><c:out value="${msg}"/></div>
                                        <small class="text-muted"><i class="bi bi-bell me-1"></i>Thông báo đã gửi tới Ban Biên Tập (Desk Editor)</small>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty memberNotifications}">
                                <div class="text-center text-muted py-3 border rounded bg-white">Chưa có đề xuất thay đổi nhân sự nào.</div>
                            </c:if>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap 5 Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function toggleMemberSelect(type) {
        const addGroup = document.getElementById('selectAddGroup');
        const removeGroup = document.getElementById('selectRemoveGroup');
        const addSelect = document.getElementById('targetUserAdd');
        const removeSelect = document.getElementById('targetUserRemove');

        if (type === 'ADD') {
            addGroup.classList.remove('d-none');
            removeGroup.classList.add('d-none');
            addSelect.setAttribute('name', 'targetUserId');
            addSelect.setAttribute('required', 'required');
            removeSelect.removeAttribute('name');
            removeSelect.removeAttribute('required');
        } else {
            addGroup.classList.add('d-none');
            removeGroup.classList.remove('d-none');
            removeSelect.setAttribute('name', 'targetUserId');
            removeSelect.setAttribute('required', 'required');
            addSelect.removeAttribute('name');
            addSelect.removeAttribute('required');
        }
    }

    // Topbar Notification Bell Script
    function toggleReqNotifDropdown() {
        const dd = document.getElementById('reqNotifDropdown');
        if (!dd) return;
        const isShown = dd.style.display === 'block';
        dd.style.display = isShown ? 'none' : 'block';
        if (!isShown) {
            loadReqNotifs();
        }
    }

    function loadReqNotifs() {
        fetch('${base}/api-notifications.jsp')
            .then(r => r.json())
            .then(data => {
                const badge = document.getElementById('reqNotifBadge');
                const list = document.getElementById('reqNotifList');
                if (badge) {
                    if (data.unreadCount > 0) {
                        badge.innerText = data.unreadCount > 99 ? '99+' : data.unreadCount;
                        badge.style.display = 'inline-block';
                    } else {
                        badge.style.display = 'none';
                    }
                }
                if (list) {
                    if (!data.notifications || data.notifications.length === 0) {
                        list.innerHTML = '<div class="text-center text-muted py-3 small">Chưa có thông báo nào.</div>';
                    } else {
                        list.innerHTML = data.notifications.map(n => {
                            const isUnread = !n.isRead;
                            const topicLink = n.topicId ? ('${base}/reporter/requests?topicId=' + n.topicId) : '#';
                            return '<div class="p-2 mb-1 rounded border-bottom ' + (isUnread ? 'bg-light border-primary' : 'bg-white') + '" style="font-size:12px;">'
                                 + '<div class="d-flex justify-content-between align-items-start gap-1">'
                                 + '<span class="' + (isUnread ? 'fw-bold text-dark' : 'text-muted') + '">' + escapeHtml(n.message) + '</span>'
                                 + (isUnread ? '<span class="badge bg-primary" style="font-size:8px;">Mới</span>' : '')
                                 + '</div>'
                                 + (n.topicId ? '<div class="mt-1"><a href="' + topicLink + '" class="small text-decoration-none">Xem đề tài &rarr;</a></div>' : '')
                                 + '</div>';
                        }).join('');
                    }
                }
            })
            .catch(err => {
                const list = document.getElementById('reqNotifList');
                if (list) list.innerHTML = '<div class="text-center text-danger py-2 small">Lỗi tải thông báo</div>';
            });
    }

    function markAllReqNotifsRead() {
        fetch('${base}/api-notifications.jsp?action=markAllRead', { method: 'POST' })
            .then(r => r.json())
            .then(() => {
                const badge = document.getElementById('reqNotifBadge');
                if (badge) badge.style.display = 'none';
                loadReqNotifs();
            });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    document.addEventListener('DOMContentLoaded', loadReqNotifs);
    document.addEventListener('click', function(e) {
        const btn = document.querySelector('[onclick="toggleReqNotifDropdown()"]');
        const dd = document.getElementById('reqNotifDropdown');
        if (btn && dd && !btn.contains(e.target) && !dd.contains(e.target)) {
            dd.style.display = 'none';
        }
    });
</script>
</body>
</html>
