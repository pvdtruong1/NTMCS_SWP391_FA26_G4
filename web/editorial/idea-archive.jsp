<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kho Lưu Trữ Ý Tưởng (SCR-2.8) | NTMCS — Tòa Soạn Hội Tụ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        a, a:hover, a:focus, a:visited, a:active {
            text-decoration: none !important;
        }
        .btn, .nav-item, .page-link-item, .tab-btn {
            text-decoration: none !important;
        }
        .pagination-bar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 18px;
            flex-wrap: wrap;
            gap: 12px;
        }
        .page-links {
            display: flex;
            gap: 6px;
        }
        .page-link-item {
            padding: 6px 12px;
            border: 1px solid var(--line);
            border-radius: 8px;
            background: var(--surface);
            color: var(--text);
            text-decoration: none;
            font-weight: 600;
            font-size: 13px;
        }
        .page-link-item:hover {
            border-color: var(--primary);
            color: var(--primary);
        }
        .page-link-item.active {
            background: var(--primary);
            color: #fff;
            border-color: var(--primary);
        }
        .page-link-item.disabled {
            opacity: 0.4;
            pointer-events: none;
        }
        .reason-box {
            background: #fff8f8;
            border-left: 3px solid #ef4444;
            padding: 8px 12px;
            border-radius: 4px;
            font-size: 12.5px;
            color: #7f1d1d;
            max-width: 320px;
            line-height: 1.4;
        }
        .reason-box.aborted {
            background: #fffbeb;
            border-left-color: #f59e0b;
            color: #78350f;
        }
        .evaluator-meta {
            margin-top: 4px;
            font-size: 11px;
            color: #991b1b;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .reason-box.aborted .evaluator-meta {
            color: #b45309;
        }
        /* Modal Styles */
        .modal-overlay {
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.6);
            backdrop-filter: blur(2px);
            display: none;
            align-items: center;
            justify-content: center;
            z-index: 9999;
        }
        .modal-overlay.show {
            display: flex;
        }
        .modal-card {
            background: #ffffff;
            border-radius: 12px;
            padding: 24px;
            width: 100%;
            max-width: 520px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.2), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            border: 1px solid var(--line);
        }
        .modal-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;
        }
        .modal-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: #eef2ff;
            color: var(--primary);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
        }
        .modal-title {
            font-size: 18px;
            font-weight: 700;
            color: #0f172a;
        }
        .modal-body {
            font-size: 14px;
            color: #334155;
            line-height: 1.5;
            margin-bottom: 20px;
        }
        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }
    </style>
</head>
<body>
<div class="app">
    <!-- Sidebar Navigation -->
    <aside id="sidebar">
        <div class="brand">
            <div class="brand-logo">N</div>
            <div>
                <b>NTMCS</b>
                <small>Newsroom Operating System</small>
            </div>
        </div>
        <nav>
            <div class="nav-group">
                <div class="nav-label">PHÂN HỆ BTV CHUYÊN MÔN (WF-02)</div>
                <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="nav-item">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${pageContext.request.contextPath}/editorial/idea-archive" class="nav-item active">
                    <i>🗄️</i> Kho lưu trữ ý tưởng
                </a>
            </div>
            <div class="nav-group">
                <div class="nav-label">CÁC PHÂN HỆ KHÁC</div>
                <a href="${pageContext.request.contextPath}/" class="nav-item">
                    <i>🏠</i> Trang chủ tòa soạn
                </a>
            </div>
        </nav>
        <div class="sidebar-bottom">
            <span class="demo">ITERATION 1 — WALKING SKELETON</span>
            <small>Actor: Trường (Desk Editor)</small>
        </div>
    </aside>

    <!-- Main Content Area -->
    <div class="main">
        <!-- Top Bar -->
        <header class="topbar">
            <div class="brand-desk-info">
                <strong>🏛️ ${currentDesk != null ? currentDesk.deskName : 'Ban Thời sự - Chính trị'}</strong>
            </div>
            <div class="account-chip">
                <div class="avatar">T</div>
                <div>
                    <b>${sessionScope.user != null ? sessionScope.user.fullName : 'Trường'}</b>
                    <small>Desk Editor (BTV Chuyên Môn)</small>
                </div>
            </div>
        </header>

        <!-- Main Body -->
        <main>
            <!-- Page Header -->
            <div class="page-head">
                <div>
                    <span class="eyebrow">EDITORIAL WORKFLOW / SCR-2.8</span>
                    <h1>Kho Lưu Trữ Ý Tưởng & Đề Tài Tạm Dừng</h1>
                    <p>Tra cứu, theo dõi và phục hồi các đề tài bị từ chối hoặc tạm ngưng phục vụ tái kích hoạt khi có sự kiện thời sự phù hợp.</p>
                </div>
            </div>

            <!-- Feedback Notification Banner -->
            <c:if test="${not empty feedbackSuccess}">
                <div class="alert alert-success" style="display: flex; align-items: center; justify-content: space-between; background: #ecfdf5; border: 1px solid #10b981; color: #065f46; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span style="font-size: 20px;">✅</span>
                        <span style="font-weight: 600;">${feedbackSuccess}</span>
                    </div>
                    <button type="button" onclick="this.parentElement.style.display='none';" style="background: none; border: none; font-size: 18px; cursor: pointer; color: #065f46;">&times;</button>
                </div>
            </c:if>

            <c:if test="${not empty feedbackError}">
                <div class="alert alert-danger" style="display: flex; align-items: center; justify-content: space-between; background: #fef2f2; border: 1px solid #ef4444; color: #991b1b; padding: 12px 18px; border-radius: 8px; margin-bottom: 20px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <span style="font-size: 20px;">⚠️</span>
                        <span style="font-weight: 600;">${feedbackError}</span>
                    </div>
                    <button type="button" onclick="this.parentElement.style.display='none';" style="background: none; border: none; font-size: 18px; cursor: pointer; color: #991b1b;">&times;</button>
                </div>
            </c:if>

            <!-- KPI Metric Cards (Nằm ngang chuẩn Design System) -->
            <div class="stats" style="grid-template-columns: repeat(3, 1fr); margin-bottom: 20px;">
                <div class="stat">
                    <div class="stat-icon" style="background: #eef2ff; color: var(--primary);">🗄️</div>
                    <div>
                        <span>Tổng Đề Tài Lưu Trữ</span>
                        <strong>${stats != null ? stats['TOTAL'] : 0}</strong>
                        <small>Đề tài lưu trong kho Ban</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #ffedf0; color: #bd3344;">🚫</div>
                    <div>
                        <span>Bị Từ Chối Thẩm Định</span>
                        <strong style="color: #bd3344;">${stats != null ? stats['REJECTED'] : 0}</strong>
                        <small>Trạng thái REJECTED</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #fff4dd; color: #9a6200;">⏸️</div>
                    <div>
                        <span>Tạm Dừng / Hủy Tác Nghiệp</span>
                        <strong style="color: #9a6200;">${stats != null ? stats['ABORTED'] : 0}</strong>
                        <small>Trạng thái ABORTED</small>
                    </div>
                </div>
            </div>

            <!-- Search & Filter Bar -->
            <div class="card" style="padding: 16px 20px; margin-bottom: 20px;">
                <form action="${pageContext.request.contextPath}/editorial/idea-archive" method="GET" style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
                    <div style="flex: 2; min-width: 240px;">
                        <input type="text" name="keyword" value="<c:out value='${keyword}'/>" class="form-control" placeholder="🔍 Tìm kiếm theo tên đề tài, góc tiếp cận, tác giả..." style="width: 100%; padding: 8px 12px; border: 1px solid var(--line); border-radius: 6px;">
                    </div>
                    <div style="flex: 1; min-width: 160px;">
                        <select name="status" class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid var(--line); border-radius: 6px;">
                            <option value="ALL" ${statusFilter == 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="REJECTED" ${statusFilter == 'REJECTED' ? 'selected' : ''}>Bị từ chối (REJECTED)</option>
                            <option value="ABORTED" ${statusFilter == 'ABORTED' ? 'selected' : ''}>Tạm dừng/Hủy (ABORTED)</option>
                        </select>
                    </div>
                    <div style="flex: 1; min-width: 160px;">
                        <select name="categoryId" class="form-control" style="width: 100%; padding: 8px 12px; border: 1px solid var(--line); border-radius: 6px;">
                            <option value="0">Tất cả chuyên mục</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" ${selectedCategoryId == cat.categoryId ? 'selected' : ''}>${cat.categoryName}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <button type="submit" class="btn btn-primary" style="padding: 8px 18px; font-weight: 600;">Lọc dữ liệu</button>
                        <a href="${pageContext.request.contextPath}/editorial/idea-archive" class="btn" style="padding: 8px 14px; margin-left: 6px; border: 1px solid var(--line);">Xóa lọc</a>
                    </div>
                </form>
            </div>

            <!-- Archived Topics Table -->
            <div class="card" style="padding: 0; overflow: hidden;">
                <table class="table" style="margin-bottom: 0;">
                    <thead>
                        <tr>
                            <th style="width: 60px; text-align: center;">Mã</th>
                            <th style="min-width: 280px;">Tiêu đề đề tài & Góc tiếp cận</th>
                            <th style="width: 180px;">Chuyên mục / Phóng viên</th>
                            <th style="width: 320px;">Lý do lưu trữ / Ý kiến thẩm định</th>
                            <th style="width: 130px; text-align: center;">Trạng thái</th>
                            <th style="width: 160px; text-align: center;">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty archivedTopics}">
                                <c:forEach var="t" items="${archivedTopics}">
                                    <tr>
                                        <td style="text-align: center; font-weight: 700; color: var(--muted);">
                                            #${t.topicId}
                                        </td>
                                        <td>
                                            <div style="font-weight: 700; color: var(--text); margin-bottom: 4px; font-size: 14px; line-height: 1.4;">
                                                <c:out value="${t.title}"/>
                                            </div>
                                            <c:if test="${not empty t.angle}">
                                                <div style="font-size: 12.5px; color: var(--muted); line-height: 1.3;">
                                                    🎯 Góc tiếp cận: <c:out value="${t.angle}"/>
                                                </div>
                                            </c:if>
                                            <c:if test="${not empty t.deadline}">
                                                <div style="font-size: 11px; color: #94a3b8; margin-top: 4px;">
                                                    📅 Hạn chót ban đầu: ${t.deadline}
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="badge" style="background: var(--surface2); color: var(--primary); font-weight: 600; display: inline-block; margin-bottom: 4px;">
                                                ${t.categoryName}
                                            </span>
                                            <div style="font-size: 12.5px; font-weight: 600; color: var(--text);">
                                                ✍️ ${t.authorName}
                                            </div>
                                            <div style="font-size: 11px; color: var(--muted);">
                                                ${t.authorEmail}
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty t.rejectionReason}">
                                                    <div class="reason-box ${t.status == 'ABORTED' ? 'aborted' : ''}">
                                                        💬 <c:out value="${t.rejectionReason}"/>
                                                        <c:if test="${not empty t.evaluatorName}">
                                                            <div class="evaluator-meta">
                                                                <span>👤 BTV duyệt: <b>${t.evaluatorName}</b></span>
                                                            </div>
                                                        </c:if>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-size: 12px; color: var(--muted); font-style: italic;">
                                                        (Chưa có ghi chú chi tiết)
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${t.status == 'REJECTED'}">
                                                    <span class="badge badge-danger" style="background: #fee2e2; color: #991b1b; border: 1px solid #f87171; font-weight: 700; padding: 4px 8px; border-radius: 99px;">
                                                        REJECTED
                                                    </span>
                                                </c:when>
                                                <c:when test="${t.status == 'ABORTED'}">
                                                    <span class="badge badge-warning" style="background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; font-weight: 700; padding: 4px 8px; border-radius: 99px;">
                                                        ABORTED
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge">${t.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <button type="button" class="btn btn-sm btn-primary" onclick="openReactivateModal(${t.topicId}, '<c:out value="${t.title}"/>')" style="padding: 6px 12px; font-size: 12.5px; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                                                <span>🔄</span> Phục hồi về DRAFT
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 40px 20px; color: var(--muted);">
                                        <div style="font-size: 36px; margin-bottom: 10px;">🗄️</div>
                                        <div style="font-weight: 600; font-size: 15px; color: var(--text);">Không có đề tài nào trong kho lưu trữ</div>
                                        <p style="font-size: 13px; margin-top: 4px;">Hiện tại không có đề tài nào bị từ chối hoặc tạm ngưng phù hợp với bộ lọc tìm kiếm.</p>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Pagination Bar -->
            <c:if test="${totalPages > 1}">
                <div class="pagination-bar">
                    <div style="font-size: 13px; color: var(--muted);">
                        Hiển thị trang <b>${currentPage}</b> / <b>${totalPages}</b> (Tổng số <b>${totalRecords}</b> đề tài lưu trữ)
                    </div>
                    <div class="page-links">
                        <c:if test="${currentPage > 1}">
                            <a href="${pageContext.request.contextPath}/editorial/idea-archive?page=${currentPage - 1}&keyword=${keyword}&status=${statusFilter}&categoryId=${selectedCategoryId}" class="page-link-item">&laquo; Trước</a>
                        </c:if>
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <a href="${pageContext.request.contextPath}/editorial/idea-archive?page=${p}&keyword=${keyword}&status=${statusFilter}&categoryId=${selectedCategoryId}" class="page-link-item ${p == currentPage ? 'active' : ''}">${p}</a>
                        </c:forEach>
                        <c:if test="${currentPage < totalPages}">
                            <a href="${pageContext.request.contextPath}/editorial/idea-archive?page=${currentPage + 1}&keyword=${keyword}&status=${statusFilter}&categoryId=${selectedCategoryId}" class="page-link-item">Sau &raquo;</a>
                        </c:if>
                    </div>
                </div>
            </c:if>
        </main>
    </div>
</div>

<!-- Reactivate Confirmation Modal -->
<div id="reactivateModal" class="modal-overlay">
    <div class="modal-card">
        <form action="${pageContext.request.contextPath}/editorial/idea-archive" method="POST">
            <input type="hidden" name="action" value="reactivate">
            <input type="hidden" name="topicId" id="modalTopicId" value="">

            <div class="modal-header">
                <div class="modal-icon">🔄</div>
                <div>
                    <div class="modal-title">Tái Kích Hoạt Ý Tưởng Lưu Trữ</div>
                    <small style="color: var(--muted);">Vòng đời khép kín: REJECTED / ABORTED &rarr; DRAFT</small>
                </div>
            </div>

            <div class="modal-body">
                <p>Bạn đang yêu cầu mở lại đề tài <b id="modalTopicTitle" style="color: var(--primary);"></b>.</p>
                <div style="background: #f1f5f9; padding: 12px; border-radius: 8px; font-size: 13px; margin: 12px 0; color: #475569;">
                    ℹ️ <b>Quy tắc miền (Domain Rule):</b> Đề tài sẽ chuyển về trạng thái <b>DRAFT</b>. Phóng viên tác giả sẽ nhận thông báo để chỉnh sửa góc tiếp cận và nộp lại thẩm định. Đề tài tuyệt đối không được duyệt thẳng sang APPROVED.
                </div>
                <div style="margin-top: 14px;">
                    <label for="reactivateNotes" style="font-weight: 600; font-size: 13px; display: block; margin-bottom: 6px;">Gợi ý hướng phát triển / Ghi chú cho Phóng viên:</label>
                    <textarea name="reactivateNotes" id="reactivateNotes" rows="3" class="form-control" placeholder="Nhập lý do mở lại đề tài hoặc gợi ý góc khai thác mới theo dòng thời sự hiện tại..." style="width: 100%; padding: 8px 12px; border: 1px solid var(--line); border-radius: 6px; box-sizing: border-box; font-family: inherit; font-size: 13px;"></textarea>
                </div>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn" onclick="closeReactivateModal()" style="border: 1px solid var(--line); padding: 8px 16px;">Hủy bỏ</button>
                <button type="submit" class="btn btn-primary" style="padding: 8px 18px; font-weight: 600;">Xác nhận Phục hồi về DRAFT</button>
            </div>
        </form>
    </div>
</div>

<script>
    function openReactivateModal(topicId, title) {
        document.getElementById('modalTopicId').value = topicId;
        document.getElementById('modalTopicTitle').innerText = '#' + topicId + ' - ' + title;
        document.getElementById('reactivateNotes').value = '';
        document.getElementById('reactivateModal').classList.add('show');
    }

    function closeReactivateModal() {
        document.getElementById('reactivateModal').classList.remove('show');
    }

    // Close on click outside modal card
    window.addEventListener('click', function(e) {
        var modal = document.getElementById('reactivateModal');
        if (e.target === modal) {
            closeReactivateModal();
        }
    });
</script>
</body>
</html>
