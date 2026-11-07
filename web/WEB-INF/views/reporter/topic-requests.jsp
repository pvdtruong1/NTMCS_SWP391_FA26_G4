<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
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
        <div class="d-flex justify-content-between align-items-center mb-4">
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
            <div>
                <a href="${base}/reporter/requests" class="btn btn-outline-secondary me-2">
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
                        Yêu cầu mượn tài sản đã được tạo thành công và đang chuyển cho Quản lý tài sản (Asset Manager) duyệt.
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

        <!-- Cảnh báo nếu đề tài chưa APPROVED -->
        <c:if test="${not isApproved}">
            <div class="alert alert-warning border-0 shadow-sm d-flex align-items-center mb-4 p-3" role="alert">
                <i class="bi bi-exclamation-octagon-fill fs-2 me-3 text-warning"></i>
                <div>
                    <strong class="d-block mb-1">Đề tài hiện đang ở trạng thái: <span class="badge bg-warning text-dark">${topic.status}</span></strong>
                    <span>Chỉ những đề tài đã được Ban Biên tập phê duyệt (APPROVED) mới có khả năng mượn tài sản hoặc thêm/bớt nhân sự. Chức năng xin mượn tài sản và đổi nhân sự hiện đang bị khóa đối với đề tài này.</span>
                </div>
            </div>
        </c:if>

        <!-- Thẻ tóm tắt thông tin đề tài -->
        <div class="card topic-summary-card border-0 shadow-sm mb-4">
            <div class="card-body p-3">
                <div class="row g-3 align-items-center">
                    <div class="col-md-5">
                        <small class="text-muted d-block">Tên đề tài</small>
                        <h5 class="fw-bold text-dark mb-0"><c:out value="${topic.title}"/></h5>
                    </div>
                    <div class="col-md-2">
                        <small class="text-muted d-block">Mã đề tài</small>
                        <span class="badge bg-secondary fs-6"><c:out value="${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}"/></span>
                    </div>
                    <div class="col-md-2">
                        <small class="text-muted d-block">Hạn nộp hiện tại</small>
                        <strong class="text-danger fs-6"><i class="bi bi-calendar-check me-1"></i><c:out value="${topic.deadline}"/></strong>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted d-block">Trạng thái hiện tại</small>
                        <span class="badge ${isApproved ? 'bg-success' : 'bg-primary'} fs-6"><c:out value="${topic.status}"/></span>
                    </div>
                </div>
            </div>
        </div>

        <!-- KHU VỰC TABS CHỨC NĂNG -->
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white border-bottom-0 pt-3">
                <ul class="nav nav-pills card-header-pills" id="requestTab" role="tablist">
                    <li class="nav-item" role="presentation">
                        <button class="nav-link active fw-semibold" id="asset-tab" data-bs-toggle="tab" data-bs-target="#asset-pane" type="button" role="tab">
                            <i class="bi bi-camera me-1"></i> 1. Xin mượn tài sản 
                            <c:if test="${not isApproved}"><i class="bi bi-lock-fill text-muted ms-1" title="Khóa"></i></c:if>
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="deadline-tab" data-bs-toggle="tab" data-bs-target="#deadline-pane" type="button" role="tab">
                            <i class="bi bi-calendar-plus me-1"></i> 2. Xin gia hạn Deadline
                        </button>
                    </li>
                    <li class="nav-item" role="presentation">
                        <button class="nav-link fw-semibold" id="member-tab" data-bs-toggle="tab" data-bs-target="#member-pane" type="button" role="tab">
                            <i class="bi bi-people me-1"></i> 3. Đổi nhân sự Đề tài 
                            <c:if test="${not isApproved}"><i class="bi bi-lock-fill text-muted ms-1" title="Khóa"></i></c:if>
                        </button>
                    </li>
                    <li class="nav-item ms-auto" role="presentation">
                        <button class="nav-link text-secondary fw-semibold" id="history-tab" data-bs-toggle="tab" data-bs-target="#history-pane" type="button" role="tab">
                            <i class="bi bi-clock-history me-1"></i> Lịch sử đề xuất
                        </button>
                    </li>
                </ul>
            </div>

            <div class="card-body p-4">
                <div class="tab-content" id="requestTabContent">

                    <!-- ================= TAB 1: XIN MƯỢN TÀI SẢN ================= -->
                    <div class="tab-pane fade show active" id="asset-pane" role="tabpanel">
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="border-bottom pb-3 mb-4">
                                    <h5 class="fw-bold mb-1 text-primary"><i class="bi bi-box-seam me-2"></i>Đăng ký mượn trang thiết bị / tài sản tác nghiệp</h5>
                                    <small class="text-muted">Chỉ dành cho đề tài đã duyệt. Chọn tài sản khả dụng trong kho và thời gian sử dụng.</small>
                                </div>

                                <c:choose>
                                    <c:when test="${not isApproved}">
                                        <div class="card bg-light border-0 p-4 text-center my-3">
                                            <i class="bi bi-lock-fill fs-1 text-secondary mb-2"></i>
                                            <h6 class="fw-bold text-dark">Chức năng mượn tài sản đang bị khóa</h6>
                                            <p class="text-muted small mb-0">Đề tài này chưa được duyệt (trạng thái: <strong>${topic.status}</strong>). Phóng viên chỉ có thể xin cấp tài sản khi đề tài chính thức được chuyển sang trạng thái <strong>APPROVED</strong>.</p>
                                        </div>
                                    </c:when>

                                    <c:otherwise>
                                        <form action="${base}/reporter/requests" method="POST">
                                            <input type="hidden" name="requestType" value="ASSET_REQUEST">
                                            <input type="hidden" name="topicId" value="${topic.topicId}">

                                            <div class="mb-3">
                                                <label for="assetId" class="form-label fw-semibold">Chọn tài sản muốn mượn <span class="text-danger">*</span></label>
                                                <select class="form-select form-select-lg fs-6" id="assetId" name="assetId" required>
                                                    <option value="">-- Chọn thiết bị / phương tiện (Camera, Flycam, Xe ô tô...) --</option>
                                                    <c:forEach var="asset" items="${availableAssets}">
                                                        <option value="${asset.assetId}">
                                                            [${asset.assetCategory}] ${asset.assetName} (Mã: ${asset.assetCode} - S/N: ${asset.serialNumber})
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <div class="row g-3 mb-4">
                                                <div class="col-md-6">
                                                    <label for="startTime" class="form-label fw-semibold">Thời gian mượn (Bắt đầu) <span class="text-danger">*</span></label>
                                                    <input type="datetime-local" class="form-control" id="startTime" name="startTime" required>
                                                </div>
                                                <div class="col-md-6">
                                                    <label for="endTime" class="form-label fw-semibold">Thời gian trả dự kiến <span class="text-danger">*</span></label>
                                                    <input type="datetime-local" class="form-control" id="endTime" name="endTime" required>
                                                </div>
                                            </div>

                                            <div class="text-end">
                                                <button type="submit" class="btn btn-primary px-4 py-2">
                                                    <i class="bi bi-send-fill me-1"></i> Gửi yêu cầu mượn tài sản
                                                </button>
                                            </div>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </div>

                    <!-- ================= TAB 2: XIN GIA HẠN DEADLINE ================= -->
                    <div class="tab-pane fade" id="deadline-pane" role="tabpanel">
                        <div class="row justify-content-center">
                            <div class="col-lg-8">
                                <div class="border-bottom pb-3 mb-4">
                                    <h5 class="fw-bold mb-1 text-warning text-dark"><i class="bi bi-hourglass-split me-2"></i>Đơn xin lùi hạn nộp bài (Deadline Extension)</h5>
                                    <small class="text-muted">Gửi yêu cầu gia hạn tới Thư ký tòa soạn (Desk Editor) kèm lý do giải trình cụ thể.</small>
                                </div>

                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="DEADLINE_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="mb-3 p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                        <div>
                                            <span class="text-muted d-block small">Thời hạn nộp hiện tại:</span>
                                            <h5 class="text-danger fw-bold mb-0">${topic.deadline}</h5>
                                        </div>
                                        <i class="bi bi-clock-history fs-2 text-muted"></i>
                                    </div>

                                    <div class="mb-3">
                                        <label for="requestedDeadline" class="form-label fw-semibold">Thời hạn mới mong muốn xin gia hạn <span class="text-danger">*</span></label>
                                        <input type="date" class="form-control" id="requestedDeadline" name="requestedDeadline" required>
                                    </div>

                                    <div class="mb-4">
                                        <label for="deadlineReason" class="form-label fw-semibold">Lý do xin gia hạn <span class="text-danger">*</span></label>
                                        <textarea class="form-control" id="deadlineReason" name="reason" rows="4" placeholder="Giải trình nguyên nhân cần thêm thời gian (VD: Nhân vật phỏng vấn bận đột xuất, đang chờ xác minh nguồn số liệu hiện trường...)" required></textarea>
                                    </div>

                                    <div class="text-end">
                                        <button type="submit" class="btn btn-warning text-dark px-4 py-2 fw-semibold">
                                            <i class="bi bi-send-check-fill me-1"></i> Gửi đơn xin gia hạn
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- ================= TAB 3: XIN THAY ĐỔI NHÂN SỰ ================= -->
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

                        <!-- 3. Đề xuất đổi nhân sự -->
                        <h6 class="fw-bold mt-4 text-success"><i class="bi bi-people me-1"></i> 3. Đề xuất thay đổi nhân sự nhóm</h6>
                        <div class="list-group">
                            <c:forEach var="msg" items="${memberNotifications}">
                                <div class="list-group-item list-group-item-action">
                                    <i class="bi bi-info-circle-fill text-info me-2"></i> <c:out value="${msg}"/>
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
</script>
</body>
</html>
