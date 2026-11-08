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
    <title>Phiếu Đề Xuất &amp; Xin Hỗ Trợ Đề Tài — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <link rel="stylesheet" href="${base}/css/reporter.css">
    <style>
        .req-tabs-bar {
            display: flex;
            gap: 8px;
            border-bottom: 2px solid var(--line);
            margin-bottom: 22px;
            overflow-x: auto;
        }
        .req-tab-btn {
            background: none;
            border: none;
            border-bottom: 3px solid transparent;
            padding: 10px 18px;
            font-size: 13.5px;
            font-weight: 700;
            color: var(--muted);
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.15s ease;
            white-space: nowrap;
            margin-bottom: -2px;
        }
        .req-tab-btn:hover {
            color: var(--text);
            background: var(--surface2);
            border-radius: 8px 8px 0 0;
        }
        .req-tab-btn.active {
            color: var(--primary);
            border-bottom-color: var(--primary);
            background: transparent;
        }
        .req-tab-pane {
            display: none;
        }
        .req-tab-pane.active {
            display: block;
        }
        .action-type-pill {
            display: flex;
            gap: 12px;
            margin-bottom: 16px;
        }
        .action-type-label {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 12px 16px;
            border: 2px solid var(--line);
            border-radius: 10px;
            background: var(--surface);
            cursor: pointer;
            font-weight: 700;
            transition: all 0.15s;
        }
        .action-type-label:hover {
            border-color: #cbd5e1;
            background: var(--surface2);
        }
        input[type="radio"]:checked + .action-type-label.add-label {
            border-color: #10b981;
            background: #ecfdf5;
            color: #065f46;
        }
        input[type="radio"]:checked + .action-type-label.remove-label {
            border-color: #ef4444;
            background: #fef2f2;
            color: #991b1b;
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
            <jsp:param name="title" value="Phiếu Đề Xuất &amp; Xin Hỗ Trợ Đề Tài"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">
                        PHÂN HỆ PHÓNG VIÊN / WF-01 &amp; WF-03 &middot; 
                        <c:out value="${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}"/>
                    </span>
                    <h1>Phiếu Đề Xuất &amp; Xin Hỗ Trợ</h1>
                    <p>Đề xuất mượn phương tiện/thiết bị, xin gia hạn nộp bài (Deadline) hoặc điều chỉnh nhân sự cho đề tài <strong>&ldquo;<c:out value="${topic.title}"/>&rdquo;</strong>.</p>
                </div>
                <div class="page-actions">
                    <a class="btn" href="${base}/reporter/requests">
                        <i>&larr;</i> <span>Danh sách đề tài đã duyệt</span>
                    </a>
                    <a class="btn" href="${base}/reporter/detail?id=${topic.topicId}">
                        <i>👁️</i> <span>Xem chi tiết đề tài</span>
                    </a>
                </div>
            </div>

            <!-- THÔNG BÁO FLASH FEEDBACK TỪ PRG -->
            <c:if test="${param.status eq 'success'}">
                <div style="background: #e8f7ef; color: var(--success); border: 1px solid #ccebdd; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display:flex; align-items:center; gap:10px;">
                    <span style="font-size: 20px;">✅</span>
                    <div>
                        <strong>Thành công!</strong>
                        <c:choose>
                            <c:when test="${param.type eq 'asset'}">
                                Yêu cầu mượn tài sản đã được tạo thành công và đang chuyển cho Quản lý tài sản duyệt.
                            </c:when>
                            <c:when test="${param.type eq 'deadline'}">
                                Đơn xin gia hạn Deadline đã được chuyển tới BTV Chuyên Môn (Desk Editor) xem xét.
                            </c:when>
                            <c:when test="${param.type eq 'member'}">
                                Đề xuất thay đổi nhân sự nhóm đã được gửi thành công dưới dạng thông báo tới Ban Biên Tập.
                            </c:when>
                            <c:otherwise>
                                Yêu cầu của bạn đã được ghi nhận vào hệ thống.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <c:if test="${param.status eq 'notApproved'}">
                <div style="background: #fffbeb; color: #92400e; border: 1px solid #fde68a; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display:flex; align-items:center; gap:10px;">
                    <span style="font-size: 20px;">⚠️</span>
                    <div>
                        <strong>Không đủ điều kiện:</strong> Chỉ những đề tài đã được phê duyệt chính thức (APPROVED) mới có quyền xin cấp tài sản hoặc thay đổi nhân sự nhóm.
                    </div>
                </div>
            </c:if>

            <c:if test="${param.status eq 'failed'}">
                <div style="background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; border-radius: 12px; padding: 14px 18px; margin-bottom: 20px; font-weight: 600; display:flex; align-items:center; gap:10px;">
                    <span style="font-size: 20px;">❌</span>
                    <div>
                        <strong>Lỗi xử lý:</strong> Không thể gửi yêu cầu. Vui lòng kiểm tra lại thời gian hoặc dữ liệu đã nhập.
                    </div>
                </div>
            </c:if>

            <!-- KHỐI TÓM TẮT ĐỀ TÀI (METADATA) -->
            <div class="card" style="margin-bottom: 24px;">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 14px; margin-bottom: 16px;">
                    <div>
                        <span class="badge" style="background: var(--surface2); border: 1px solid var(--line); font-family: monospace; font-size: 11px; margin-bottom: 6px;">
                            <c:out value="${topic.topicCode != null ? topic.topicCode : ('#' += topic.topicId)}"/>
                        </span>
                        <h2 style="font-size: 20px; margin: 4px 0 6px; color: var(--text);">
                            <c:out value="${topic.title}"/>
                        </h2>
                        <div style="color: var(--muted); font-size: 13.5px;">
                            <c:out value="${topic.angle}"/>
                        </div>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${topic.status eq 'APPROVED'}">
                                <span class="badge b-approved" style="font-size: 12px; padding: 6px 12px;">
                                    ✓ ĐÃ DUYỆT (APPROVED)
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge" style="font-size: 12px; padding: 6px 12px;">
                                    <c:out value="${topic.status}"/>
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="meta-grid" style="margin: 0; padding-top: 14px; border-top: 1px solid var(--line);">
                    <div>
                        <span>CHUYÊN MỤC / BAN</span>
                        <b><c:out value="${topic.categoryName}"/> (<c:out value="${topic.deskName}"/>)</b>
                    </div>
                    <div>
                        <span>THỂ LOẠI / ƯU TIÊN</span>
                        <b><c:out value="${topic.genre}"/> &middot; <c:out value="${topic.priority}"/></b>
                    </div>
                    <div>
                        <span>HẠN NỘP BÀI (DEADLINE)</span>
                        <b style="color: var(--danger);">📅 <c:out value="${topic.deadline}"/></b>
                    </div>
                    <div>
                        <span>NHÂN SỰ NHÓM</span>
                        <b>👥 <c:out value="${not empty currentMembers ? currentMembers.size() : 0}"/> thành viên</b>
                    </div>
                </div>
            </div>

            <!-- TABS 3 LOẠI REQUEST + LỊCH SỬ -->
            <div class="card">
                <div class="req-tabs-bar">
                    <button type="button" class="req-tab-btn active" id="tab-btn-asset" onclick="switchRequestTab('asset')">
                        <span>📦</span> <span>1. Mượn trang thiết bị</span>
                        <c:if test="${not isApproved}"><small style="color:var(--muted);">(Khóa)</small></c:if>
                    </button>
                    <button type="button" class="req-tab-btn" id="tab-btn-deadline" onclick="switchRequestTab('deadline')">
                        <span>⏱️</span> <span>2. Xin gia hạn Deadline</span>
                    </button>
                    <button type="button" class="req-tab-btn" id="tab-btn-member" onclick="switchRequestTab('member')">
                        <span>👥</span> <span>3. Đề xuất nhân sự</span>
                        <c:if test="${not isApproved}"><small style="color:var(--muted);">(Khóa)</small></c:if>
                    </button>
                    <button type="button" class="req-tab-btn" id="tab-btn-history" onclick="switchRequestTab('history')" style="margin-left: auto;">
                        <span>📜</span> <span>Lịch sử đề xuất</span>
                        <span class="badge" style="background: var(--surface2); border: 1px solid var(--line); font-size: 10px; padding: 2px 6px;">
                            ${assetRequests.size() + deadlineRequests.size() + memberNotifications.size()}
                        </span>
                    </button>
                </div>

                <!-- ================= TAB 1: MƯỢN TRANG THIẾT BỊ ================= -->
                <div class="req-tab-pane active" id="pane-asset">
                    <div style="max-width: 760px; margin: 0 auto; padding: 8px 0 20px;">
                        <div style="margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid var(--line);">
                            <h3 style="font-size: 16px; margin: 0 0 4px; color: var(--primary);">
                                📦 Đề xuất xin cấp phát / mượn thiết bị tác nghiệp thực địa
                            </h3>
                            <small style="color: var(--muted);">
                                Áp dụng cho các đề tài đã được duyệt chính thức. Tài sản mượn từ kho thiết bị chuyên dụng của tòa soạn.
                            </small>
                        </div>

                        <c:choose>
                            <c:when test="${not isApproved}">
                                <div style="text-align: center; padding: 40px 20px; background: var(--surface2); border-radius: 12px; border: 1px solid var(--line);">
                                    <div style="font-size: 32px; margin-bottom: 8px;">🔒</div>
                                    <h4 style="margin: 0 0 6px; font-size: 15px;">Chức năng mượn tài sản đang bị khóa</h4>
                                    <p style="margin: 0; color: var(--muted); font-size: 13px;">
                                        Đề tài này hiện đang ở trạng thái <strong>${topic.status}</strong>. Chỉ những đề tài đã được duyệt chính thức (<strong>APPROVED</strong>) mới đủ điều kiện xin cấp phát trang thiết bị tác nghiệp.
                                    </p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="ASSET_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="field" style="margin-bottom: 16px;">
                                        <span>CHỌN TÀI SẢN / THIẾT BỊ CẦN MƯỢN <em style="color:var(--danger)">*</em></span>
                                        <select id="assetId" name="assetId" required style="padding: 10px 12px; border-radius: 10px; border: 1px solid var(--line);">
                                            <option value="">-- Chọn tài sản khả dụng từ kho tòa soạn --</option>
                                            <c:forEach var="a" items="${availableAssets}">
                                                <option value="${a.assetId}">
                                                    [${a.assetCategory}] ${a.assetName} (${a.assetCode}) - Serial: ${a.serialNumber != null ? a.serialNumber : 'N/A'}
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <small style="color: var(--muted); font-size: 11px;">Chỉ các tài sản đang ở trạng thái khả dụng (AVAILABLE) trong kho mới được hiển thị.</small>
                                    </div>

                                    <div class="form-grid" style="margin-bottom: 20px;">
                                        <div class="field">
                                            <span>THỜI GIAN BẮT ĐẦU MƯỢN <em style="color:var(--danger)">*</em></span>
                                            <input type="datetime-local" id="startTime" name="startTime" required>
                                        </div>
                                        <div class="field">
                                            <span>THỜI GIAN DỰ KIẾN HOÀN TRẢ <em style="color:var(--danger)">*</em></span>
                                            <input type="datetime-local" id="endTime" name="endTime" required>
                                        </div>
                                    </div>

                                    <div style="text-align: right;">
                                        <button type="submit" class="btn primary" style="padding: 10px 20px; font-size: 13.5px;">
                                            <span>📤</span> Gửi yêu cầu mượn thiết bị
                                        </button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- ================= TAB 2: XIN GIA HẠN DEADLINE ================= -->
                <div class="req-tab-pane" id="pane-deadline">
                    <div style="max-width: 760px; margin: 0 auto; padding: 8px 0 20px;">
                        <div style="margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid var(--line);">
                            <h3 style="font-size: 16px; margin: 0 0 4px; color: var(--warning);">
                                ⏱️ Xin gia hạn thời hạn nộp bài (Deadline)
                            </h3>
                            <small style="color: var(--muted);">
                                Đơn xin gia hạn sẽ được chuyển trực tiếp tới BTV Chuyên Môn (Desk Editor) xem xét và phê duyệt.
                            </small>
                        </div>

                        <form action="${base}/reporter/requests" method="POST">
                            <input type="hidden" name="requestType" value="DEADLINE_REQUEST">
                            <input type="hidden" name="topicId" value="${topic.topicId}">

                            <div class="field" style="margin-bottom: 16px;">
                                <span>HẠN NỘP HIỆN TẠI CỦA ĐỀ TÀI</span>
                                <input type="text" value="${topic.deadline}" readonly style="background: var(--surface2); color: var(--muted); cursor: not-allowed;">
                                <small style="color: var(--muted); font-size: 11px;">Hạn nộp đã ấn định khi đề tài được BTV phê duyệt cấp phép.</small>
                            </div>

                            <div class="field" style="margin-bottom: 16px;">
                                <span>HẠN NỘP MỚI MONG MUỐN <em style="color:var(--danger)">*</em></span>
                                <input type="date" id="requestedDeadline" name="requestedDeadline" required min="${topic.deadline}">
                                <small style="color: var(--muted); font-size: 11px;">Thời hạn mới bắt buộc phải sau ngày hạn nộp hiện tại.</small>
                            </div>

                            <div class="field" style="margin-bottom: 22px;">
                                <span>LÝ DO XIN GIA HẠN <em style="color:var(--danger)">*</em></span>
                                <textarea id="deadlineReason" name="reason" rows="4" placeholder="Giải trình cụ thể lý do khách quan/chủ quan cần kéo dài thời gian tác nghiệp thực địa..." required></textarea>
                            </div>

                            <div style="text-align: right;">
                                <button type="submit" class="btn warning" style="padding: 10px 20px; font-size: 13.5px; font-weight: 700;">
                                    <span>📅</span> Gửi đơn xin gia hạn
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- ================= TAB 3: ĐỀ XUẤT NHÂN SỰ ================= -->
                <div class="req-tab-pane" id="pane-member">
                    <div style="max-width: 760px; margin: 0 auto; padding: 8px 0 20px;">
                        <div style="margin-bottom: 20px; padding-bottom: 12px; border-bottom: 1px solid var(--line);">
                            <h3 style="font-size: 16px; margin: 0 0 4px; color: var(--success);">
                                👥 Đề xuất thay đổi nhân sự nhóm đề tài
                            </h3>
                            <small style="color: var(--muted);">
                                Dành cho đề tài đã duyệt. Đề xuất được gửi tới Ban Biên Tập để bổ sung hoặc rút bớt phóng viên/kỹ thuật viên.
                            </small>
                        </div>

                        <c:choose>
                            <c:when test="${not isApproved}">
                                <div style="text-align: center; padding: 40px 20px; background: var(--surface2); border-radius: 12px; border: 1px solid var(--line);">
                                    <div style="font-size: 32px; margin-bottom: 8px;">🔒</div>
                                    <h4 style="margin: 0 0 6px; font-size: 15px;">Chức năng thay đổi nhân sự đang bị khóa</h4>
                                    <p style="margin: 0; color: var(--muted); font-size: 13px;">
                                        Đề tài này chưa được duyệt (trạng thái: <strong>${topic.status}</strong>). Chỉ những đề tài đã duyệt chính thức mới có quyền gửi đề xuất thêm hoặc rút nhân sự.
                                    </p>
                                </div>
                            </c:when>

                            <c:otherwise>
                                <form action="${base}/reporter/requests" method="POST">
                                    <input type="hidden" name="requestType" value="MEMBER_REQUEST">
                                    <input type="hidden" name="topicId" value="${topic.topicId}">

                                    <div class="field" style="margin-bottom: 16px;">
                                        <span>LOẠI HÀNH ĐỘNG <em style="color:var(--danger)">*</em></span>
                                        <div class="action-type-pill">
                                            <label style="flex:1; cursor:pointer;">
                                                <input type="radio" name="actionType" id="actionAdd" value="ADD" checked onchange="toggleMemberSelect('ADD')" style="display:none;">
                                                <div class="action-type-label add-label">
                                                    <span>➕</span> Xin bổ sung thêm người vào nhóm
                                                </div>
                                            </label>
                                            <label style="flex:1; cursor:pointer;">
                                                <input type="radio" name="actionType" id="actionRemove" value="REMOVE" onchange="toggleMemberSelect('REMOVE')" style="display:none;">
                                                <div class="action-type-label remove-label">
                                                    <span>➖</span> Xin rút / xóa người khỏi nhóm
                                                </div>
                                            </label>
                                        </div>
                                    </div>

                                    <!-- Chọn khi THÊM NGƯỜI (danh sách user tòa soạn) -->
                                    <div class="field" id="selectAddGroup" style="margin-bottom: 16px;">
                                        <span>CHỌN NHÂN SỰ MUỐN THÊM VÀO NHÓM <em style="color:var(--danger)">*</em></span>
                                        <select id="targetUserAdd" name="targetUserId" required style="padding: 10px 12px; border-radius: 10px; border: 1px solid var(--line);">
                                            <option value="">-- Chọn phóng viên / biên tập viên tòa soạn --</option>
                                            <c:forEach var="u" items="${allUsers}">
                                                <option value="${u.userId}">${u.fullName} (${u.email}) - Vị trí: ${u.roleName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <!-- Chọn khi RÚT NGƯỜI (chỉ thành viên đang trong nhóm đề tài) -->
                                    <div class="field" id="selectRemoveGroup" style="margin-bottom: 16px; display: none;">
                                        <span>CHỌN THÀNH VIÊN MUỐN RÚT KHỎI NHÓM <em style="color:var(--danger)">*</em></span>
                                        <select id="targetUserRemove" style="padding: 10px 12px; border-radius: 10px; border: 1px solid var(--line);">
                                            <option value="">-- Chọn thành viên nhóm hiện tại --</option>
                                            <c:forEach var="m" items="${currentMembers}">
                                                <option value="${m.userId}">${m.userName} (${m.userEmail}) - Vai trò: ${m.roleInTeam}</option>
                                            </c:forEach>
                                        </select>
                                    </div>

                                    <div class="field" style="margin-bottom: 22px;">
                                        <span>LÝ DO ĐỀ XUẤT <em style="color:var(--danger)">*</em></span>
                                        <textarea id="memberReason" name="reason" rows="3" placeholder="Nêu rõ lý do cần bổ sung thêm người hoặc rút thành viên khỏi đề tài..." required></textarea>
                                    </div>

                                    <div style="text-align: right;">
                                        <button type="submit" class="btn success" style="padding: 10px 20px; font-size: 13.5px; font-weight: 700;">
                                            <span>👥</span> Gửi đề xuất nhân sự
                                        </button>
                                    </div>
                                </form>

                                <!-- HIỂN THỊ CÁC THÔNG BÁO / ĐỀ XUẤT NHÂN SỰ ĐÃ GỬI -->
                                <div style="margin-top: 36px; padding-top: 24px; border-top: 1px solid var(--line);">
                                    <h4 style="font-size: 14px; margin: 0 0 14px; color: var(--text); display: flex; align-items: center; gap: 8px;">
                                        <span>🔔</span> Các đề xuất nhân sự đã gửi cho đề tài này (${not empty memberNotifications ? memberNotifications.size() : 0})
                                    </h4>
                                    <c:choose>
                                        <c:when test="${not empty memberNotifications}">
                                            <div style="display: flex; flex-direction: column; gap: 10px;">
                                                <c:forEach var="msg" items="${memberNotifications}">
                                                    <div style="background: var(--surface2); border: 1px solid var(--line); border-radius: 10px; padding: 12px 16px; display: flex; align-items: flex-start; gap: 12px;">
                                                        <span class="badge b-approved" style="margin-top: 2px;">Đã gửi</span>
                                                        <div style="flex: 1;">
                                                            <div style="font-weight: 600; color: var(--text); font-size: 13px;">
                                                                <c:out value="${msg}"/>
                                                            </div>
                                                            <small style="color: var(--muted); font-size: 11px;">Đã tạo thông báo gửi tới Ban Biên Tập &amp; lưu vết audit log hệ thống</small>
                                                        </div>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div style="text-align: center; padding: 20px; background: var(--surface2); border-radius: 10px; color: var(--muted); font-size: 12.5px;">
                                                Chưa có đề xuất thay đổi nhân sự nào cho đề tài này.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- ================= TAB 4: LỊCH SỬ ĐỀ XUẤT ================= -->
                <div class="req-tab-pane" id="pane-history">
                    <div style="padding: 8px 0 20px;">
                        <!-- 1. Yêu cầu mượn tài sản -->
                        <div style="margin-bottom: 28px;">
                            <h3 style="font-size: 15px; margin: 0 0 10px; color: var(--primary); display: flex; align-items: center; gap: 6px;">
                                <span>📦</span> 1. Lịch sử yêu cầu mượn tài sản (Asset Requests)
                            </h3>
                            <div class="table-scroll">
                                <table>
                                    <thead>
                                        <tr>
                                            <th style="width: 90px;">Mã YC</th>
                                            <th>Tài sản</th>
                                            <th style="width: 140px;">Chủng loại</th>
                                            <th>Thời gian mượn &rarr; hoàn trả</th>
                                            <th style="width: 130px; text-align: right;">Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="ar" items="${assetRequests}">
                                            <tr>
                                                <td><b style="font-family: monospace;">#${ar.requestId}</b></td>
                                                <td>
                                                    <strong>${ar.assetName}</strong>
                                                    <small style="color: var(--muted);">(${ar.assetCode})</small>
                                                </td>
                                                <td><span class="badge" style="background: var(--surface2); border: 1px solid var(--line);">${ar.assetCategory}</span></td>
                                                <td><span style="font-size: 12px;">${ar.startTime} &rarr; ${ar.endTime}</span></td>
                                                <td style="text-align: right;">
                                                    <c:choose>
                                                        <c:when test="${ar.status eq 'APPROVED'}"><span class="badge b-approved">✓ Đã duyệt</span></c:when>
                                                        <c:when test="${ar.status eq 'REJECTED'}"><span class="badge b-rejected">✕ Từ chối</span></c:when>
                                                        <c:otherwise><span class="badge b-pending">⏳ Chờ duyệt</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty assetRequests}">
                                            <tr><td colspan="5" style="text-align: center; color: var(--muted); padding: 24px;">Chưa có yêu cầu mượn tài sản nào cho đề tài này.</td></tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- 2. Yêu cầu gia hạn Deadline -->
                        <div style="margin-bottom: 28px;">
                            <h3 style="font-size: 15px; margin: 0 0 10px; color: var(--warning); display: flex; align-items: center; gap: 6px;">
                                <span>⏱️</span> 2. Lịch sử xin gia hạn Deadline
                            </h3>
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
                                                <td><b style="font-family: monospace;">#${dr.requestId}</b></td>
                                                <td><span style="text-decoration: line-through; color: var(--muted);">${dr.currentDeadline}</span></td>
                                                <td><strong style="color: var(--danger); font-size: 13px;">📅 ${dr.requestedDeadline}</strong></td>
                                                <td><c:out value="${dr.reason}"/></td>
                                                <td style="text-align: right;">
                                                    <c:choose>
                                                        <c:when test="${dr.status eq 'APPROVED'}"><span class="badge b-approved">✓ Đã duyệt</span></c:when>
                                                        <c:when test="${dr.status eq 'REJECTED'}"><span class="badge b-rejected">✕ Từ chối</span></c:when>
                                                        <c:otherwise><span class="badge b-pending">⏳ Chờ duyệt</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty deadlineRequests}">
                                            <tr><td colspan="5" style="text-align: center; color: var(--muted); padding: 24px;">Chưa có yêu cầu gia hạn nào cho đề tài này.</td></tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- 3. Đề xuất đổi nhân sự -->
                        <div>
                            <h3 style="font-size: 15px; margin: 0 0 10px; color: var(--success); display: flex; align-items: center; gap: 6px;">
                                <span>👥</span> 3. Lịch sử đề xuất nhân sự nhóm (${not empty memberNotifications ? memberNotifications.size() : 0})
                            </h3>
                            <c:choose>
                                <c:when test="${not empty memberNotifications}">
                                    <div style="display: flex; flex-direction: column; gap: 10px;">
                                        <c:forEach var="msg" items="${memberNotifications}">
                                            <div style="background: var(--surface2); border: 1px solid var(--line); border-radius: 10px; padding: 12px 16px; display: flex; align-items: flex-start; gap: 12px;">
                                                <span class="badge b-approved" style="margin-top: 2px;">Đã gửi</span>
                                                <div style="flex: 1;">
                                                    <div style="font-weight: 600; color: var(--text); font-size: 13px;">
                                                        <c:out value="${msg}"/>
                                                    </div>
                                                    <small style="color: var(--muted); font-size: 11px;">Thông báo đã chuyển tới Ban Biên Tập (Desk Editor)</small>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; color: var(--muted); padding: 24px; background: var(--surface2); border-radius: 10px;">
                                        Chưa có đề xuất thay đổi nhân sự nào.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
    function switchRequestTab(tabName) {
        // Toggle tab buttons
        document.querySelectorAll('.req-tab-btn').forEach(btn => btn.classList.remove('active'));
        const activeBtn = document.getElementById('tab-btn-' + tabName);
        if (activeBtn) activeBtn.classList.add('active');

        // Toggle tab panes
        document.querySelectorAll('.req-tab-pane').forEach(pane => pane.classList.remove('active'));
        const activePane = document.getElementById('pane-' + tabName);
        if (activePane) activePane.classList.add('active');

        // Update URL hash without jumping
        if (history.replaceState) {
            history.replaceState(null, null, '#' + tabName);
        }
    }

    function toggleMemberSelect(type) {
        const addGroup = document.getElementById('selectAddGroup');
        const removeGroup = document.getElementById('selectRemoveGroup');
        const addSelect = document.getElementById('targetUserAdd');
        const removeSelect = document.getElementById('targetUserRemove');

        if (type === 'ADD') {
            addGroup.style.display = 'block';
            removeGroup.style.display = 'none';
            addSelect.setAttribute('name', 'targetUserId');
            addSelect.setAttribute('required', 'required');
            removeSelect.removeAttribute('name');
            removeSelect.removeAttribute('required');
        } else {
            addGroup.style.display = 'none';
            removeGroup.style.display = 'block';
            removeSelect.setAttribute('name', 'targetUserId');
            removeSelect.setAttribute('required', 'required');
            addSelect.removeAttribute('name');
            addSelect.removeAttribute('required');
        }
    }

    // Auto switch to tab from hash or param on load
    document.addEventListener('DOMContentLoaded', function() {
        const hash = window.location.hash.replace('#', '');
        if (hash && ['asset', 'deadline', 'member', 'history'].includes(hash)) {
            switchRequestTab(hash);
        }
    });
</script>
</body>
</html>
