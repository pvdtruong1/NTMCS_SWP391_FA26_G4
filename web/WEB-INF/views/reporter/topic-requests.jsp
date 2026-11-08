<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phiếu Đề Xuất &amp; Xin Hỗ Trợ Đề Tài — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
    <style>
        .tabs-nav {
            display: flex;
            gap: 6px;
            border-bottom: 2px solid var(--line);
            margin-bottom: 22px;
            overflow-x: auto;
        }
        .tab-btn {
            background: transparent;
            border: none;
            border-bottom: 3px solid transparent;
            padding: 12px 18px;
            font-size: 13.5px;
            font-weight: 700;
            color: var(--muted);
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            white-space: nowrap;
            transition: all 0.15s ease;
            margin-bottom: -2px;
            border-radius: 8px 8px 0 0;
        }
        .tab-btn:hover {
            color: var(--text);
            background: var(--surface2);
        }
        .tab-btn.active {
            color: var(--primary);
            border-bottom-color: var(--primary);
            background: rgba(52, 88, 212, 0.06);
        }
        .tab-pane {
            display: none;
        }
        .tab-pane.active {
            display: block;
        }
        .form-card {
            max-width: 800px;
            margin: 0 auto;
        }
        .form-row {
            display: flex;
            flex-direction: column;
            gap: 6px;
            margin-bottom: 16px;
        }
        .form-row label {
            font-size: 13px;
            font-weight: 700;
            color: var(--text);
        }
        .form-row input, .form-row select, .form-row textarea {
            border: 1px solid var(--line);
            background: var(--surface);
            border-radius: 10px;
            padding: 10px 12px;
            font-size: 13.5px;
            outline: none;
            transition: border-color 0.15s ease;
        }
        .form-row input:focus, .form-row select:focus, .form-row textarea:focus {
            border-color: #9eafe9;
            box-shadow: 0 0 0 3px rgba(52, 88, 212, 0.15);
        }
        .form-hint {
            font-size: 12px;
            color: var(--muted);
            margin-top: 2px;
        }
        .radio-switch {
            display: flex;
            gap: 12px;
        }
        .radio-switch label {
            flex: 1;
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 12px 14px;
            border: 1px solid var(--line);
            border-radius: 10px;
            background: var(--surface);
            cursor: pointer;
            font-weight: 700;
            font-size: 13px;
            transition: all 0.15s ease;
        }
        .radio-switch input[type="radio"]:checked + span {
            color: var(--primary);
        }
        .radio-switch label.active {
            border-color: var(--primary);
            background: rgba(52, 88, 212, 0.05);
            box-shadow: 0 0 0 2px rgba(52, 88, 212, 0.2);
        }
        .lock-box {
            padding: 36px 20px;
            text-align: center;
            background: var(--surface2);
            border: 1px dashed var(--line);
            border-radius: 14px;
            margin: 12px 0;
        }
        .lock-box i {
            font-size: 36px;
            display: block;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="reporter-requests"/>
    </jsp:include>

    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="Đề xuất xin hỗ trợ"/>
        </jsp:include>

        <main>
            <div style="margin-bottom: 12px;">
                <a href="${base}/reporter/requests" class="btn sm" style="display: inline-flex; align-items: center; gap: 6px;">
                    ← Quay lại danh sách đề tài
                </a>
            </div>

            <div class="page-head">
                <div>
                    <span class="eyebrow">PHÂN HỆ PHÓNG VIÊN / WF-01 &middot; PHIẾU ĐỀ XUẤT</span>
                    <h1>Phiếu Đề Xuất &amp; Xin Hỗ Trợ Đề Tài</h1>
                    <p>Khởi tạo đề xuất mượn tài sản, xin gia hạn hạn nộp bài (deadline) hoặc thay đổi nhân sự nhóm đề tài.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/requests">
                        <i>📋</i> <span>Danh sách đề tài đã duyệt</span>
                    </a>
                    <a class="btn" href="${base}/reporter/detail?id=${topic.topicId}">
                        <i>👁</i> <span>Chi tiết đề tài</span>
                    </a>
                </div>
            </div>

            <!-- THÔNG BÁO ALERT PHẢN HỒI KHI THỰC HIỆN THAO TÁC -->
            <c:if test="${param.status eq 'success'}">
                <div style="background: #e8f7ef; color: var(--success); border: 1px solid #ccebdd; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <span style="font-size: 18px;">✓</span>
                    <div>
                        <strong>Thành công!</strong>
                        <c:choose>
                            <c:when test="${param.type eq 'asset'}">
                                Yêu cầu mượn tài sản đã được tạo thành công và đang chuyển cho Quản lý tài sản thẩm duyệt.
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
                    </div>
                </div>
            </c:if>

            <c:if test="${param.status eq 'notApproved'}">
                <div style="background: #fff4dd; color: #9a6200; border: 1px solid #fce8b2; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <span style="font-size: 18px;">⚠️</span>
                    <div>
                        <strong>Không đủ điều kiện!</strong> Chỉ những đề tài đã được phê duyệt chính thức (APPROVED) mới có quyền xin cấp tài sản hoặc thay đổi nhân sự.
                    </div>
                </div>
            </c:if>

            <c:if test="${param.status eq 'failed'}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <span style="font-size: 18px;">✕</span>
                    <div>
                        <strong>Lỗi xử lý!</strong> Không thể gửi yêu cầu. Vui lòng kiểm tra lại thời gian hoặc dữ liệu đã nhập.
                    </div>
                </div>
            </c:if>

            <!-- TÓM TẮT THÔNG TIN ĐỀ TÀI -->
            <div class="card" style="margin-bottom: 22px; background: linear-gradient(135deg, #ffffff 0%, var(--surface2) 100%);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 16px; flex-wrap: wrap;">
                    <div>
                        <span class="tag" style="font-weight: 800; color: var(--primary); margin-bottom: 6px;">
                            <c:choose>
                                <c:when test="${not empty topic.topicCode}">
                                    <c:out value="${topic.topicCode}"/>
                                </c:when>
                                <c:otherwise>
                                    #${topic.topicId}
                                </c:otherwise>
                            </c:choose>
                        </span>
                        <h2 style="font-size: 20px; font-weight: 800; margin: 4px 0 6px; color: var(--text);">
                            <c:out value="${topic.title}"/>
                        </h2>
                        <div style="color: var(--muted); font-size: 13.5px; line-height: 1.5;">
                            <c:out value="${topic.angle != null ? topic.angle : topic.summary}"/>
                        </div>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${topic.status eq 'APPROVED'}">
                                <span class="badge b-approved" style="font-size: 12px; padding: 6px 14px;">
                                    ✓ ĐÃ DUYỆT (APPROVED)
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge" style="font-size: 12px; padding: 6px 14px;">
                                    <c:out value="${topic.status}"/>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="meta-grid" style="margin: 0; padding-top: 14px; border-top: 1px solid var(--line);">
                    <div>
                        <span>Chuyên mục &amp; Ban</span>
                        <b><c:out value="${topic.categoryName}"/> (<c:out value="${topic.deskName}"/>)</b>
                    </div>
                    <div>
                        <span>Thể loại &amp; Ưu tiên</span>
                        <b><c:out value="${topic.genre}"/> &middot; <span class="tag"><c:out value="${topic.priority}"/></span></b>
                    </div>
                    <div>
                        <span>Hạn nộp hiện tại (Deadline)</span>
                        <b style="color: var(--danger);">📅 <c:out value="${topic.deadline != null ? topic.deadline : 'Chưa đặt'}"/></b>
                    </div>
                    <div>
                        <span>Thành viên nhóm đề tài</span>
                        <b>👥 <c:out value="${not empty currentMembers ? fn:length(currentMembers) : 0}"/> người</b>
                    </div>
                </div>
            </div>

            <!-- TABS ĐIỀU HƯỚNG 3 LOẠI REQUEST + LỊCH SỬ -->
            <div class="card" style="padding: 24px;">
                <div class="tabs-nav">
                    <button type="button" class="tab-btn active" id="tabBtn-asset" onclick="switchTab('asset')">
                        📦 1. Mượn trang thiết bị
                        <c:if test="${not isApproved}"><span title="Cần trạng thái APPROVED">🔒</span></c:if>
                    </button>
                    <button type="button" class="tab-btn" id="tabBtn-deadline" onclick="switchTab('deadline')">
                        ⏳ 2. Xin gia hạn Deadline
                    </button>
                    <button type="button" class="tab-btn" id="tabBtn-member" onclick="switchTab('member')">
                        👥 3. Đề xuất nhân sự
                        <c:if test="${not isApproved}"><span title="Cần trạng thái APPROVED">🔒</span></c:if>
                    </button>
                    <button type="button" class="tab-btn" id="tabBtn-history" onclick="switchTab('history')" style="margin-left: auto;">
                        📜 Lịch sử đề xuất
                        <span class="badge" style="background: var(--surface2); border: 1px solid var(--line); font-size: 11px;">
                            ${(not empty assetRequests ? fn:length(assetRequests) : 0) + (not empty deadlineRequests ? fn:length(deadlineRequests) : 0) + (not empty memberNotifications ? fn:length(memberNotifications) : 0)}
                        </span>
                    </button>
                </div>

                <!-- ================= TAB 1: MƯỢN TÀI SẢN ================= -->
                <div class="tab-pane active" id="pane-asset">
                    <div class="form-card">
                        <div style="border-bottom: 1px solid var(--line); padding-bottom: 12px; margin-bottom: 20px;">
                            <h3 style="font-size: 17px; margin: 0 0 4px; font-weight: 800; color: var(--primary);">
                                📷 Đề xuất xin cấp phát / mượn thiết bị tác nghiệp
                            </h3>
                            <small style="color: var(--muted);">Chỉ áp dụng cho các đề tài đã được duyệt chính thức. Tài sản mượn từ kho thiết bị của tòa soạn.</small>
                        </div>

                        <c:choose>
                            <c:when test="${not isApproved}">
                                <div class="lock-box">
                                    <i>🔒</i>
                                    <h4 style="font-size: 16px; margin: 0 0 6px; font-weight: 700;">Chức năng mượn tài sản đang bị khóa</h4>
                                    <p style="color: var(--muted); font-size: 13px; max-width: 500px; margin: 0 auto;">
                                        Đề tài này hiện đang ở trạng thái <strong>${topic.status}</strong>. Chỉ những đề tài đã được duyệt chính thức (<strong>APPROVED</strong>) mới đủ điều kiện xin cấp phát trang thiết bị tác nghiệp thực địa.
                                    </p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="ASSET_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="form-row">
                                        <label for="assetId">Chọn tài sản / thiết bị cần mượn <span style="color: var(--danger);">*</span></label>
                                        <select id="assetId" name="assetId" required style="width: 100%;">
                                            <option value="">-- Chọn tài sản khả dụng từ kho --</option>
                                            <c:forEach var="a" items="${availableAssets}">
                                                <option value="${a.assetId}">
                                                    [${a.assetCategory}] ${a.assetName} (${a.assetCode}) - Serial: ${a.serialNumber != null ? a.serialNumber : 'N/A'}
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <span class="form-hint">Chỉ các tài sản đang ở trạng thái khả dụng (AVAILABLE) mới hiển thị trong danh mục.</span>
                                    </div>

                                    <div class="form-grid" style="margin-bottom: 18px;">
                                        <div class="form-row" style="margin-bottom: 0;">
                                            <label for="startTime">Thời gian bắt đầu mượn <span style="color: var(--danger);">*</span></label>
                                            <input type="datetime-local" id="startTime" name="startTime" required>
                                        </div>
                                        <div class="form-row" style="margin-bottom: 0;">
                                            <label for="endTime">Thời gian dự kiến hoàn trả <span style="color: var(--danger);">*</span></label>
                                            <input type="datetime-local" id="endTime" name="endTime" required>
                                        </div>
                                    </div>

                                    <div style="text-align: right; margin-top: 24px;">
                                        <button type="submit" class="btn primary" style="padding: 11px 22px; font-size: 14px;">
                                            <i>📤</i> <span>Gửi yêu cầu mượn thiết bị</span>
                                        </button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- ================= TAB 2: GIA HẠN DEADLINE ================= -->
                <div class="tab-pane" id="pane-deadline">
                    <div class="form-card">
                        <div style="border-bottom: 1px solid var(--line); padding-bottom: 12px; margin-bottom: 20px;">
                            <h3 style="font-size: 17px; margin: 0 0 4px; font-weight: 800; color: #b26a00;">
                                ⏳ Xin gia hạn hạn nộp bài (Deadline)
                            </h3>
                            <small style="color: var(--muted);">Đơn xin gia hạn sẽ được chuyển trực tiếp tới Thư ký tòa soạn (Desk Editor) xem xét phê duyệt.</small>
                        </div>

                        <form action="${base}/reporter/requests" method="POST">
                            <input type="hidden" name="requestType" value="DEADLINE_REQUEST">
                            <input type="hidden" name="topicId" value="${topic.topicId}">

                            <div class="form-row">
                                <label>Hạn nộp hiện tại của đề tài</label>
                                <input type="text" value="${topic.deadline != null ? topic.deadline : 'Chưa thiết lập'}" readonly style="background: var(--surface2); color: var(--muted);">
                                <span class="form-hint">Hạn nộp đã được ấn định khi đề tài khởi tạo / được phê duyệt.</span>
                            </div>

                            <div class="form-row">
                                <label for="requestedDeadline">Hạn nộp mới mong muốn <span style="color: var(--danger);">*</span></label>
                                <input type="date" id="requestedDeadline" name="requestedDeadline" required min="${topic.deadline}">
                                <span class="form-hint">Hạn nộp mới xin gia hạn phải sau thời hạn hiện tại.</span>
                            </div>

                            <div class="form-row">
                                <label for="deadlineReason">Lý do xin gia hạn <span style="color: var(--danger);">*</span></label>
                                <textarea id="deadlineReason" name="reason" rows="4" placeholder="Giải trình cụ thể lý do khách quan/chủ quan cần kéo dài thời gian tác nghiệp..." required></textarea>
                            </div>

                            <div style="text-align: right; margin-top: 24px;">
                                <button type="submit" class="btn warning" style="padding: 11px 22px; font-size: 14px; font-weight: 800;">
                                    <i>⏳</i> <span>Gửi đơn xin gia hạn</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- ================= TAB 3: ĐỀ XUẤT NHÂN SỰ ================= -->
                <div class="tab-pane" id="pane-member">
                    <div class="form-card">
                        <div style="border-bottom: 1px solid var(--line); padding-bottom: 12px; margin-bottom: 20px;">
                            <h3 style="font-size: 17px; margin: 0 0 4px; font-weight: 800; color: var(--success);">
                                👥 Đề xuất thay đổi nhân sự nhóm đề tài
                            </h3>
                            <small style="color: var(--muted);">Chỉ dành cho đề tài đã duyệt. Đề xuất gửi tới Ban biên tập để bổ sung hoặc rút bớt thành viên.</small>
                        </div>

                        <c:choose>
                            <c:when test="${not isApproved}">
                                <div class="lock-box">
                                    <i>🔒</i>
                                    <h4 style="font-size: 16px; margin: 0 0 6px; font-weight: 700;">Chức năng thay đổi nhân sự đang bị khóa</h4>
                                    <p style="color: var(--muted); font-size: 13px; max-width: 500px; margin: 0 auto;">
                                        Đề tài này chưa được duyệt (trạng thái: <strong>${topic.status}</strong>). Chỉ những đề tài đã duyệt chính thức (<strong>APPROVED</strong>) mới có quyền gửi đề xuất thêm hoặc rút bớt nhân sự nhóm.
                                    </p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="MEMBER_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="form-row">
                                        <label>Loại hành động <span style="color: var(--danger);">*</span></label>
                                        <div class="radio-switch">
                                            <label id="lblActionAdd" class="active">
                                                <input type="radio" name="actionType" id="actionAdd" value="ADD" checked onchange="toggleMemberSelect('ADD')">
                                                <span>➕ Xin bổ sung thêm người vào nhóm</span>
                                            </label>
                                            <label id="lblActionRemove">
                                                <input type="radio" name="actionType" id="actionRemove" value="REMOVE" onchange="toggleMemberSelect('REMOVE')">
                                                <span>➖ Xin rút / xóa người khỏi nhóm</span>
                                            </label>
                                        </div>
                                    </div>

                                    <!-- Chọn khi THÊM NGƯỜI (danh sách user tòa soạn) -->
                                    <div class="form-row" id="selectAddGroup">
                                        <label for="targetUserAdd">Chọn nhân sự muốn thêm vào nhóm <span style="color: var(--danger);">*</span></label>
                                        <select id="targetUserAdd" name="targetUserId" required style="width: 100%;">
                                            <option value="">-- Chọn phóng viên / biên tập viên --</option>
                                            <c:forEach var="u" items="${allUsers}">
                                                <option value="${u.userId}">${u.fullName} (${u.email}) - Vị trí: ${u.roleName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <!-- Chọn khi RÚT NGƯỜI (chỉ thành viên đang trong nhóm đề tài) -->
                                    <div class="form-row" id="selectRemoveGroup" style="display: none;">
                                        <label for="targetUserRemove">Chọn thành viên muốn rút khỏi nhóm <span style="color: var(--danger);">*</span></label>
                                        <select id="targetUserRemove" style="width: 100%;">
                                            <option value="">-- Chọn thành viên nhóm hiện tại --</option>
                                            <c:forEach var="m" items="${currentMembers}">
                                                <option value="${m.userId}">${m.userName} (${m.userEmail}) - Vai trò: ${m.roleInTeam}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="form-row">
                                        <label for="memberReason">Lý do đề xuất <span style="color: var(--danger);">*</span></label>
                                        <textarea id="memberReason" name="reason" rows="3" placeholder="Nêu rõ lý do cần bổ sung thêm người hoặc rút thành viên khỏi đề tài..." required></textarea>
                                    </div>

                                    <div style="text-align: right; margin-top: 24px;">
                                        <button type="submit" class="btn success" style="padding: 11px 22px; font-size: 14px; font-weight: 800;">
                                            <i>👥</i> <span>Gửi đề xuất nhân sự</span>
                                        </button>
                                    </div>
                                </form>

                                <!-- HIỂN THỊ CÁC THÔNG BÁO / ĐỀ XUẤT NHÂN SỰ ĐÃ GỬI TRỰC TIẾP TẠI TAB NÀY -->
                                <div style="margin-top: 36px; padding-top: 20px; border-top: 1px solid var(--line);">
                                    <h4 style="font-size: 15px; font-weight: 800; margin: 0 0 12px; color: var(--text);">
                                        🔔 Các đề xuất nhân sự đã gửi cho đề tài này (${not empty memberNotifications ? fn:length(memberNotifications) : 0})
                                    </h4>
                                    <div style="display: flex; flex-direction: column; gap: 8px;">
                                        <c:forEach var="msg" items="${memberNotifications}">
                                            <div style="background: var(--surface2); border: 1px solid var(--line); border-radius: 10px; padding: 12px 14px; display: flex; align-items: flex-start; gap: 10px;">
                                                <span class="badge b-approved" style="font-size: 10px; margin-top: 2px;">Đã gửi</span>
                                                <div style="flex: 1;">
                                                    <div style="font-size: 13px; font-weight: 600; color: var(--text);"><c:out value="${msg}"/></div>
                                                    <small style="color: var(--muted); font-size: 11px;">Đã tạo thông báo gửi tới Ban biên tập &amp; lưu vết hệ thống</small>
                                                </div>
                                            </div>
                                        </c:forEach>
                                        <c:if test="${empty memberNotifications}">
                                            <div style="padding: 24px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 10px; font-size: 13px;">
                                                Chưa có đề xuất thay đổi nhân sự nào cho đề tài này.
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- ================= TAB 4: LỊCH SỬ ĐỀ XUẤT ================= -->
                <div class="tab-pane" id="pane-history">
                    <div style="border-bottom: 1px solid var(--line); padding-bottom: 12px; margin-bottom: 20px;">
                        <h3 style="font-size: 17px; margin: 0 0 4px; font-weight: 800; color: var(--text);">
                            📜 Lịch sử các yêu cầu đã gửi cho đề tài này
                        </h3>
                        <small style="color: var(--muted);">Toàn bộ danh sách đề xuất mượn tài sản, xin gia hạn deadline và đề xuất nhân sự.</small>
                    </div>

                    <!-- 1. Yêu cầu mượn tài sản -->
                    <h4 style="font-size: 14.5px; font-weight: 800; color: var(--primary); margin: 24px 0 10px; display: flex; align-items: center; gap: 6px;">
                        <span>📷</span> 1. Yêu cầu mượn tài sản (${not empty assetRequests ? fn:length(assetRequests) : 0})
                    </h4>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 90px;">Mã YC</th>
                                    <th>Tài sản</th>
                                    <th>Phân loại</th>
                                    <th>Thời gian mượn - trả</th>
                                    <th style="width: 130px; text-align: right;">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="ar" items="${assetRequests}">
                                    <tr>
                                        <td><strong>#${ar.requestId}</strong></td>
                                        <td>
                                            <strong><c:out value="${ar.assetName}"/></strong>
                                            <small style="color: var(--muted);">(<c:out value="${ar.assetCode}"/>)</small>
                                        </td>
                                        <td><span class="tag"><c:out value="${ar.assetCategory}"/></span></td>
                                        <td><small style="font-size: 12px;">📅 ${ar.startTime} &rarr; ${ar.endTime}</small></td>
                                        <td style="text-align: right;">
                                            <c:choose>
                                                <c:when test="${ar.status eq 'APPROVED'}"><span class="badge b-approved">Đã duyệt</span></c:when>
                                                <c:when test="${ar.status eq 'REJECTED'}"><span class="badge b-rejected">Bị từ chối</span></c:when>
                                                <c:otherwise><span class="badge b-pending">Chờ duyệt</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty assetRequests}">
                                    <tr><td colspan="5" style="text-align: center; color: var(--muted); padding: 24px;">Chưa có yêu cầu mượn tài sản nào.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <!-- 2. Yêu cầu gia hạn Deadline -->
                    <h4 style="font-size: 14.5px; font-weight: 800; color: #b26a00; margin: 30px 0 10px; display: flex; align-items: center; gap: 6px;">
                        <span>⏳</span> 2. Yêu cầu xin gia hạn Deadline (${not empty deadlineRequests ? fn:length(deadlineRequests) : 0})
                    </h4>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="width: 90px;">Mã YC</th>
                                    <th style="width: 140px;">Hạn nộp gốc</th>
                                    <th style="width: 140px;">Hạn mới xin</th>
                                    <th>Lý do giải trình</th>
                                    <th style="width: 130px; text-align: right;">Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dr" items="${deadlineRequests}">
                                    <tr>
                                        <td><strong>#${dr.requestId}</strong></td>
                                        <td style="color: var(--muted);"><del>${dr.currentDeadline}</del></td>
                                        <td style="color: var(--danger); font-weight: 800;">${dr.requestedDeadline}</td>
                                        <td><c:out value="${dr.reason}"/></td>
                                        <td style="text-align: right;">
                                            <c:choose>
                                                <c:when test="${dr.status eq 'APPROVED'}"><span class="badge b-approved">Đã duyệt</span></c:when>
                                                <c:when test="${dr.status eq 'REJECTED'}"><span class="badge b-rejected">Bị từ chối</span></c:when>
                                                <c:otherwise><span class="badge b-pending">Chờ duyệt</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty deadlineRequests}">
                                    <tr><td colspan="5" style="text-align: center; color: var(--muted); padding: 24px;">Chưa có yêu cầu gia hạn nào.</td></tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <!-- 3. Đề xuất đổi nhân sự -->
                    <h4 style="font-size: 14.5px; font-weight: 800; color: var(--success); margin: 30px 0 10px; display: flex; align-items: center; gap: 6px;">
                        <span>👥</span> 3. Đề xuất thay đổi nhân sự nhóm (${not empty memberNotifications ? fn:length(memberNotifications) : 0})
                    </h4>
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <c:forEach var="msg" items="${memberNotifications}">
                            <div style="background: var(--surface2); border: 1px solid var(--line); border-radius: 10px; padding: 12px 14px; display: flex; align-items: flex-start; gap: 10px;">
                                <span class="badge b-approved" style="font-size: 10px; margin-top: 2px;">Đã gửi</span>
                                <div style="flex: 1;">
                                    <div style="font-size: 13px; font-weight: 600; color: var(--text);"><c:out value="${msg}"/></div>
                                    <small style="color: var(--muted); font-size: 11px;">Thông báo đã chuyển tới Ban Biên Tập (Desk Editor)</small>
                                </div>
                            </div>
                        </c:forEach>
                        <c:if test="${empty memberNotifications}">
                            <div style="padding: 24px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 10px; font-size: 13px;">
                                Chưa có đề xuất thay đổi nhân sự nào.
                            </div>
                        </c:if>
                    </div>
                </div>

            </div>
        </main>
    </div>
</div>

<script>
    function switchTab(tabName) {
        document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
        document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));

        const targetBtn = document.getElementById('tabBtn-' + tabName);
        const targetPane = document.getElementById('pane-' + tabName);

        if (targetBtn) targetBtn.classList.add('active');
        if (targetPane) targetPane.classList.add('active');
    }

    function toggleMemberSelect(type) {
        const addGroup = document.getElementById('selectAddGroup');
        const removeGroup = document.getElementById('selectRemoveGroup');
        const addSelect = document.getElementById('targetUserAdd');
        const removeSelect = document.getElementById('targetUserRemove');
        const lblActionAdd = document.getElementById('lblActionAdd');
        const lblActionRemove = document.getElementById('lblActionRemove');

        if (!addGroup || !removeGroup || !addSelect || !removeSelect) return;

        if (type === 'ADD') {
            if (lblActionAdd) lblActionAdd.classList.add('active');
            if (lblActionRemove) lblActionRemove.classList.remove('active');
            addGroup.style.display = 'flex';
            removeGroup.style.display = 'none';
            addSelect.setAttribute('name', 'targetUserId');
            addSelect.setAttribute('required', 'required');
            removeSelect.removeAttribute('name');
            removeSelect.removeAttribute('required');
        } else {
            if (lblActionAdd) lblActionAdd.classList.remove('active');
            if (lblActionRemove) lblActionRemove.classList.add('active');
            addGroup.style.display = 'none';
            removeGroup.style.display = 'flex';
            removeSelect.setAttribute('name', 'targetUserId');
            removeSelect.setAttribute('required', 'required');
            addSelect.removeAttribute('name');
            addSelect.removeAttribute('required');
        }
    }
</script>
</body>
</html>
