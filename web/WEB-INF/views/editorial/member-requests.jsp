<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu Cầu Điều Chuyển Kíp Tác Nghiệp (WF-02) | NTMCS — Tòa Soạn Hội Tụ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        a, a:hover, a:focus, a:visited, a:active {
            text-decoration: none !important;
        }
        .btn, .nav-item, .page-link-item, .tab-btn {
            text-decoration: none !important;
        }
        .tabs-header {
            display: flex;
            gap: 10px;
            margin-bottom: 18px;
            border-bottom: 2px solid var(--line);
            padding-bottom: 2px;
        }
        .tab-btn {
            padding: 10px 18px;
            border: none;
            background: transparent;
            font-size: 14px;
            font-weight: 700;
            color: var(--muted);
            border-bottom: 3px solid transparent;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            text-decoration: none;
            transition: color 0.15s ease, border-color 0.15s ease;
        }
        .tab-btn:hover {
            color: var(--primary);
        }
        .tab-btn.active {
            color: var(--primary);
            border-bottom-color: var(--primary);
        }
        .tab-badge {
            background: var(--surface2);
            border: 1px solid var(--line);
            padding: 2px 7px;
            border-radius: 99px;
            font-size: 11px;
        }
        .tab-btn.active .tab-badge {
            background: #eef2ff;
            color: var(--primary);
            border-color: #d1dcff;
        }
        .action-modal-backdrop {
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.65);
            backdrop-filter: blur(4px);
            z-index: 1050;
            display: none;
            align-items: center;
            justify-content: center;
        }
        .action-modal {
            background: var(--surface);
            border-radius: 14px;
            border: 1px solid var(--line);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 540px;
            padding: 24px;
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="member-requests"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="👥 Điều Phối Kíp Tác Nghiệp"/>
        </jsp:include>

        <main>
            <!-- Page Header -->
            <div class="page-head">
                <div>
                    <span class="eyebrow">EDITORIAL WORKFLOW / WF-02</span>
                    <h1>Yêu Cầu Điều Chuyển &amp; Bổ Sung Kíp Tác Nghiệp</h1>
                    <p>Thẩm định và phê duyệt các đề xuất bổ sung nhân sự hoặc điều chuyển thành viên rời kíp tác nghiệp đề tài từ Phóng viên.</p>
                </div>
            </div>

            <!-- Feedback Notification Banner -->
            <c:if test="${param.result == 'approved'}">
                <div class="banner success" style="margin-bottom: 20px;">
                    <div>
                        <strong>✅ Phê duyệt thành công!</strong> Đã cập nhật kíp tác nghiệp đề tài, tự động chuyển giao nhiệm vụ dở dang (nếu có) và gửi thông báo điều động tác nghiệp tới các phóng viên liên quan.
                    </div>
                </div>
            </c:if>
            <c:if test="${param.result == 'rejected'}">
                <div class="banner warning" style="margin-bottom: 20px;">
                    <div>
                        <strong>⚠️ Đã từ chối đề xuất!</strong> Hệ thống đã lưu vết kiểm toán và gửi thông báo phản hồi kèm ý kiến chỉ đạo tới Phóng viên chủ trì đề tài.
                    </div>
                </div>
            </c:if>
            <c:if test="${param.result == 'approve_failed' or param.result == 'reject_failed'}">
                <div class="banner danger" style="margin-bottom: 20px;">
                    <div>
                        <strong>❌ Thao tác thất bại!</strong> Không thể xử lý yêu cầu hoặc đề xuất này đã được giải quyết trước đó.
                    </div>
                </div>
            </c:if>

            <!-- Metrics KPI -->
            <div class="grid col-4" style="margin-bottom: 24px;">
                <div class="card" style="border-left: 4px solid #f59e0b;">
                    <div class="stat-value" style="color: #d97706;">${pendingCount}</div>
                    <div class="stat-label">Chờ Trưởng Ban Duyệt</div>
                </div>
                <div class="card" style="border-left: 4px solid #10b981;">
                    <div class="stat-value" style="color: #059669;">${approvedCount}</div>
                    <div class="stat-label">Đã Phê Duyệt</div>
                </div>
                <div class="card" style="border-left: 4px solid #ef4444;">
                    <div class="stat-value" style="color: #dc2626;">${rejectedCount}</div>
                    <div class="stat-label">Đã Từ Chối</div>
                </div>
                <div class="card" style="border-left: 4px solid var(--primary);">
                    <div class="stat-value" style="color: var(--primary);">${totalCount}</div>
                    <div class="stat-label">Tổng Số Yêu Cầu</div>
                </div>
            </div>

            <!-- Filter Tabs -->
            <div class="tabs-header">
                <a href="${pageContext.request.contextPath}/editorial/member-requests?status=PENDING"
                   class="tab-btn ${statusFilter == 'PENDING' ? 'active' : ''}">
                    <i>⏳</i> Chờ Phê Duyệt
                    <span class="tab-badge">${pendingCount}</span>
                </a>
                <a href="${pageContext.request.contextPath}/editorial/member-requests?status=APPROVED"
                   class="tab-btn ${statusFilter == 'APPROVED' ? 'active' : ''}">
                    <i>✅</i> Đã Duyệt
                    <span class="tab-badge">${approvedCount}</span>
                </a>
                <a href="${pageContext.request.contextPath}/editorial/member-requests?status=REJECTED"
                   class="tab-btn ${statusFilter == 'REJECTED' ? 'active' : ''}">
                    <i>❌</i> Đã Từ Chối
                    <span class="tab-badge">${rejectedCount}</span>
                </a>
                <a href="${pageContext.request.contextPath}/editorial/member-requests?status=ALL"
                   class="tab-btn ${statusFilter == 'ALL' ? 'active' : ''}">
                    <i>📑</i> Tất Cả Yêu Cầu
                    <span class="tab-badge">${totalCount}</span>
                </a>
            </div>

            <!-- Requests Table Card -->
            <div class="card">
                <div class="card-head" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div>
                        <h3 style="margin: 0; font-size: 16px;">Danh Sách Đề Xuất Điều Chỉnh Kíp Tác Nghiệp</h3>
                        <p style="margin: 4px 0 0; color: var(--muted); font-size: 13px;">
                            Xem xét tính khả thi, tải công việc và phê duyệt điều động nhân sự thực địa.
                        </p>
                    </div>
                </div>

                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th style="width: 180px;">Đề Tài Tác Nghiệp</th>
                            <th style="width: 140px;">Chủ Trì Đề Xuất</th>
                            <th style="width: 130px;">Loại Yêu Cầu</th>
                            <th>Nhân Sự Liên Quan</th>
                            <th>Lý Do Đề Xuất</th>
                            <th style="width: 110px;">Trạng Thái</th>
                            <th style="width: 150px; text-align: center;">Thao Tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty requests}">
                                <tr>
                                    <td colspan="7" style="text-align: center; padding: 48px 16px; color: var(--muted);">
                                        <div style="font-size: 36px; margin-bottom: 12px;">📭</div>
                                        <div style="font-size: 15px; font-weight: 600;">Không có yêu cầu điều chỉnh kíp tác nghiệp nào</div>
                                        <div style="font-size: 13px; margin-top: 4px;">Hiện tại không có đề xuất nào trong danh mục này.</div>
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="req" items="${requests}">
                                    <tr>
                                        <td>
                                            <div style="font-weight: 700; color: var(--text);">${req.topicCode}</div>
                                            <div style="font-size: 13px; color: var(--muted); word-break: break-word;">${req.topicTitle}</div>
                                            <small style="color: var(--primary); font-weight: 600;">${req.deskName}</small>
                                        </td>
                                        <td>
                                            <div style="font-weight: 600; color: var(--text);">${req.requesterName}</div>
                                            <small style="color: var(--muted);">ID #${req.requesterId}</small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.actionType == 'ADD'}">
                                                    <span class="badge" style="background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; padding: 4px 8px; border-radius: 6px; font-size: 11px; font-weight: 700;">
                                                        ➕ Bổ sung kíp
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: #fff7ed; color: #c2410c; border: 1px solid #fed7aa; padding: 4px 8px; border-radius: 6px; font-size: 11px; font-weight: 700;">
                                                        🔄 Điều chuyển
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="font-weight: 700; color: var(--text);">${req.targetUserName}</div>
                                            <div style="font-size: 12px; color: var(--muted);">${req.targetUserEmail}</div>
                                            <c:if test="${req.actionType == 'REMOVE' and req.pendingTasksCount > 0}">
                                                <div style="margin-top: 6px; display: inline-flex; align-items: center; gap: 4px; background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 6px; padding: 2px 6px; font-size: 11px; font-weight: 600;">
                                                    ⚠️ Có ${req.pendingTasksCount} nhiệm vụ dở dang (sẽ tự động bàn giao lại cho chủ trì)
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <div style="font-size: 13px; color: var(--text); word-break: break-word;">${req.reason}</div>
                                            <small style="color: var(--muted); font-size: 11px;">Mã yêu cầu #${req.notificationId}</small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status == 'PENDING'}">
                                                    <span class="badge warning" style="padding: 4px 8px; font-size: 11px; font-weight: 700;">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'APPROVED'}">
                                                    <span class="badge success" style="padding: 4px 8px; font-size: 11px; font-weight: 700;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'REJECTED'}">
                                                    <span class="badge danger" style="padding: 4px 8px; font-size: 11px; font-weight: 700;">Từ chối</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge muted" style="padding: 4px 8px; font-size: 11px;">${req.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${req.status == 'PENDING'}">
                                                    <button type="button" class="btn primary sm"
                                                            onclick="openReviewModal(${req.notificationId}, '${req.actionType}', '${req.targetUserName}', '${req.topicCode}', '${req.topicTitle}', ${req.pendingTasksCount})"
                                                            style="padding: 5px 12px; font-size: 12px; font-weight: 700; border-radius: 6px;">
                                                        ⚖️ Thẩm định
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-size: 12px; color: var(--muted);">Đã hoàn tất</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>

<!-- Modal Thẩm Định Đề Xuất Nhân Sự -->
<div id="reviewModalBackdrop" class="action-modal-backdrop" onclick="closeReviewModal()">
    <div class="action-modal" onclick="event.stopPropagation()">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; border-bottom: 1px solid var(--line); padding-bottom: 12px;">
            <h3 style="margin: 0; font-size: 17px;" id="modalTitle">Thẩm Định Đề Xuất Kíp Tác Nghiệp</h3>
            <button type="button" onclick="closeReviewModal()" style="background: none; border: none; font-size: 20px; cursor: pointer; color: var(--muted);">&times;</button>
        </div>

        <div id="modalSummary" style="background: var(--surface2); border: 1px solid var(--line); border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 13px;">
            <!-- Render dynamically -->
        </div>

        <form id="reviewForm" method="POST" action="${pageContext.request.contextPath}/editorial/member-requests">
            <input type="hidden" name="notificationId" id="modalNotifId" value="">
            <input type="hidden" name="action" id="modalAction" value="">

            <div style="margin-bottom: 16px;">
                <label for="feedbackInput" style="display: block; font-weight: 600; font-size: 13px; margin-bottom: 6px;">
                    Ý kiến chỉ đạo của Trưởng ban (tùy chọn):
                </label>
                <textarea id="feedbackInput" name="feedback" rows="3"
                          placeholder="Nhập ghi chú điều phối hoặc lý do từ chối (nếu có)..."
                          style="width: 100%; border: 1px solid var(--line); border-radius: 8px; padding: 10px; font-family: inherit; font-size: 13px; box-sizing: border-box; resize: vertical;"></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px;">
                <button type="button" class="btn" onclick="closeReviewModal()" style="border: 1px solid var(--line); background: var(--surface); color: var(--text); padding: 8px 16px; border-radius: 8px;">
                    Hủy bỏ
                </button>
                <button type="button" class="btn danger" onclick="submitReview('reject')" style="padding: 8px 18px; border-radius: 8px; font-weight: 700;">
                    ❌ Không phê duyệt
                </button>
                <button type="button" class="btn primary" onclick="submitReview('approve')" style="padding: 8px 20px; border-radius: 8px; font-weight: 700;">
                    ✅ Phê duyệt đề xuất
                </button>
            </div>
        </form>
    </div>
</div>

<script>
function openReviewModal(notifId, actionType, targetName, topicCode, topicTitle, pendingTasks) {
    document.getElementById('modalNotifId').value = notifId;
    document.getElementById('feedbackInput').value = '';

    const isAdd = (actionType === 'ADD');
    document.getElementById('modalTitle').textContent = isAdd ? 'Phê Duyệt Bổ Sung Nhân Sự' : 'Phê Duyệt Điều Chuyển Nhân Sự';

    let summaryHtml = '<div style="margin-bottom: 4px;"><strong>Đề tài:</strong> ' + topicCode + ' — ' + topicTitle + '</div>'
                    + '<div style="margin-bottom: 4px;"><strong>Nhân sự:</strong> ' + targetName + '</div>'
                    + '<div><strong>Thao tác đề xuất:</strong> ' + (isAdd ? '<span style="color: #059669; font-weight: 700;">Bổ sung vào kíp tác nghiệp</span>' : '<span style="color: #c2410c; font-weight: 700;">Điều chuyển rời khỏi kíp tác nghiệp</span>') + '</div>';

    if (!isAdd && pendingTasks > 0) {
        summaryHtml += '<div style="margin-top: 8px; padding: 6px 10px; background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; border-radius: 6px; font-size: 12px; font-weight: 600;">'
                     + '⚠️ Chú ý: Nhân sự này đang đảm nhận ' + pendingTasks + ' nhiệm vụ thực địa dở dang. Khi bạn phê duyệt, hệ thống sẽ tự động chuyển giao toàn bộ nhiệm vụ này lại cho Phóng viên chủ trì đề tài để bảo đảm tiến độ tác nghiệp.'
                     + '</div>';
    }

    document.getElementById('modalSummary').innerHTML = summaryHtml;
    document.getElementById('reviewModalBackdrop').style.display = 'flex';
}

function closeReviewModal() {
    document.getElementById('reviewModalBackdrop').style.display = 'none';
}

function submitReview(actionName) {
    document.getElementById('modalAction').value = actionName;
    if (actionName === 'reject') {
        const feedback = document.getElementById('feedbackInput').value.trim();
        if (!feedback) {
            if (!confirm('Bạn chưa nhập ý kiến/lý do từ chối. Bạn có chắc chắn muốn từ chối đề xuất này?')) {
                return;
            }
        }
    }
    document.getElementById('reviewForm').submit();
}
</script>
</body>
</html>
