<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hàng Đợi Chờ Thẩm Định (SCR-2.2) | NTMCS — Tòa Soạn Hội Tụ</title>
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
        .text-wrap-title {
            max-width: 380px;
            word-break: break-word;
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
                <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="nav-item active">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${pageContext.request.contextPath}/editorial/idea-archive" class="nav-item">
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
                    <span class="eyebrow">EDITORIAL WORKFLOW / SCR-2.2</span>
                    <h1>Hàng Đợi Chờ Thẩm Định Đề Tài</h1>
                    <p>Hàng đợi tiếp nhận đề xuất đề tài từ Phóng viên cần thẩm định tiêu chí Rubric cấp 1 & theo dõi đề tài vượt cấp TBT.</p>
                </div>
            </div>

            <!-- Feedback Notification Banner -->
            <c:if test="${not empty param.msg}">
                <div style="margin-bottom: 18px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px;
                     ${param.msg == 'approved' ? 'background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd;' : ''}
                     ${param.msg == 'escalated' ? 'background: #fff4dd; color: #9a6200; border: 1px solid #f4ddb2;' : ''}
                     ${param.msg == 'revision_requested' ? 'background: #f1ebff; color: #6e4cb1; border: 1px solid #d8c7ff;' : ''}
                     ${param.msg == 'rejected' ? 'background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce;' : ''}">
                    <span style="font-size: 20px;">
                        ${param.msg == 'approved' ? '🎉' : (param.msg == 'escalated' ? '🚀' : (param.msg == 'revision_requested' ? '🔄' : '✖'))}
                    </span>
                    <div>
                        <c:choose>
                            <c:when test="${param.msg == 'approved'}">
                                <b>Phê duyệt đề tài thành công!</b> Đề tài đã được cấp mã định danh chính thức và thông báo tự động đã được gửi tới phóng viên tác giả.
                            </c:when>
                            <c:when test="${param.msg == 'escalated'}">
                                <b>Đã chuyển lên Tổng Biên Tập!</b> Đề tài vượt cấp do rủi ro pháp lý cao đã được chuyển sang tab theo dõi phê duyệt điều hành.
                            </c:when>
                            <c:when test="${param.msg == 'revision_requested'}">
                                <b>Đã gửi yêu cầu chỉnh sửa!</b> Ý kiến góp ý của BTV đã được chuyển đến phóng viên để hoàn thiện đề cương.
                            </c:when>
                            <c:when test="${param.msg == 'rejected'}">
                                <b>Đã bác bỏ đề tài!</b> Đề tài đã được phân loại từ chối và lưu trữ vào Kho ý tưởng tòa soạn.
                            </c:when>
                            <c:otherwise>
                                <b>Thao tác thành công!</b> Dữ liệu hàng đợi đã được cập nhật.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <!-- Error Notification Banner -->
            <c:if test="${not empty param.error or not empty errorMessage}">
                <div style="margin-bottom: 18px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px; background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce;">
                    <span style="font-size: 20px;">⚠️</span>
                    <div>
                        <c:choose>
                            <c:when test="${not empty errorMessage}">
                                <b>Lỗi hệ thống:</b> ${errorMessage}
                            </c:when>
                            <c:when test="${param.error == 'missing_topic_id'}">
                                <b>Lỗi truy cập:</b> Thiếu mã định danh đề tài (topicId).
                            </c:when>
                            <c:when test="${param.error == 'invalid_topic_id'}">
                                <b>Lỗi truy cập:</b> Mã định danh đề tài không hợp lệ.
                            </c:when>
                            <c:when test="${param.error == 'topic_not_found'}">
                                <b>Không tìm thấy:</b> Đề tài yêu cầu không tồn tại trong hệ thống.
                            </c:when>
                            <c:when test="${param.error == 'invalid_submission'}">
                                <b>Thao tác không hợp lệ:</b> Dữ liệu gửi lên không đúng quy cách.
                            </c:when>
                            <c:when test="${param.error == 'server_error'}">
                                <b>Lỗi máy chủ:</b> Không thể xử lý yêu cầu lúc này. Vui lòng thử lại sau.
                            </c:when>
                            <c:otherwise>
                                <b>Có lỗi xảy ra:</b> Không thể hoàn tất thao tác. Vui lòng thử lại.
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <!-- Stats KPI Cards -->
            <div class="stats">
                <div class="stat">
                    <div class="stat-icon">📥</div>
                    <div>
                        <span>Chờ Thẩm Định Cấp 1</span>
                        <strong id="badge-count-submitted">${pendingCount}</strong>
                        <small>Trạng thái SUBMITTED</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background:#fff3e0; color:#b26a00;">🚀</div>
                    <div>
                        <span>Chờ TBT Phê Duyệt</span>
                        <strong id="badge-count-escalated">${escalatedCount}</strong>
                        <small>Vượt cấp (Legal Risk / Vĩ mô)</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background:#e8f7ef; color:#16845b;">🏛️</div>
                    <div>
                        <span>Ban Chuyên Môn</span>
                        <strong style="font-size:16px; margin-top:6px;">${currentDesk != null ? currentDesk.deskName : 'Ban Thời sự'}</strong>
                        <small>Phạm vi thẩm quyền hiện tại</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background:#eef2ff; color:#3458d4;">📑</div>
                    <div>
                        <span>Tổng Cần Xử Lý</span>
                        <strong id="badge-count-total">${pendingCount + escalatedCount}</strong>
                        <small>Hồ sơ trong hàng đợi Ban</small>
                    </div>
                </div>
            </div>

            <!-- Card Body Container -->
            <div class="card">
                <!-- Status Tab Navigation with Instant JS Switching -->
                <div class="tabs-header">
                    <button type="button"
                            data-tab-status="SUBMITTED"
                            onclick="switchTab('SUBMITTED', event)"
                            class="tab-btn ${currentStatus == 'SUBMITTED' ? 'active' : ''}">
                        <span>📥 Chờ Thẩm Định Cấp 1 (SUBMITTED)</span>
                        <span class="tab-badge">${pendingCount}</span>
                    </button>
                    <button type="button"
                            data-tab-status="PENDING_EXECUTIVE_APPROVAL"
                            onclick="switchTab('PENDING_EXECUTIVE_APPROVAL', event)"
                            class="tab-btn ${currentStatus == 'PENDING_EXECUTIVE_APPROVAL' ? 'active' : ''}">
                        <span>🚀 Đã Chuyển Lên TBT (PENDING_EXECUTIVE_APPROVAL)</span>
                        <span class="tab-badge">${escalatedCount}</span>
                    </button>
                </div>

                <!-- Filter & Search Bar -->
                <form method="get" action="${pageContext.request.contextPath}/editorial/pending-queue" class="filterbar">
                    <input type="hidden" name="status" id="form-status-input" value="${currentStatus}">
                    <input type="text" name="search" value="${searchKeyword}"
                           placeholder="🔍 Tìm kiếm tiêu đề, góc tiếp cận, tên phóng viên..." style="flex:1;">
                    <button type="submit" class="btn primary">Tìm kiếm</button>
                    <c:if test="${not empty searchKeyword}">
                        <a href="${pageContext.request.contextPath}/editorial/pending-queue?status=${currentStatus}"
                           class="btn">Xóa tìm kiếm</a>
                    </c:if>
                </form>

                <!-- Data Table -->
                <div class="table-scroll">
                    <table>
                        <thead>
                        <tr>
                            <th style="width: 60px;">ID</th>
                            <th>Tiêu Đề & Góc Tiếp Cận (Angle)</th>
                            <th style="width: 170px;">Chuyên Mục</th>
                            <th style="width: 170px;">Phóng Viên Đề Xuất</th>
                            <th style="width: 120px;">Hạn Dự Kiến</th>
                            <th style="width: 160px;">Trạng Thái</th>
                            <th style="width: 150px; text-align: right;">Hành Động</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty allDeskTopics}">
                                <c:forEach items="${allDeskTopics}" var="t">
                                    <tr data-topic-row="true"
                                        data-status="${t.status}"
                                        style="${t.status == currentStatus ? '' : 'display: none;'}">
                                        <td><strong>#${t.topicId}</strong></td>
                                        <td class="text-wrap-title">
                                            <strong>${t.title}</strong>
                                            <c:if test="${not empty t.angle}">
                                                <small>🎯 Góc tiếp cận: ${t.angle}</small>
                                            </c:if>
                                        </td>
                                        <td>
                                            <span class="tag">${t.categoryName}</span>
                                        </td>
                                        <td>
                                            <strong>${t.authorName}</strong>
                                            <small>${t.authorEmail}</small>
                                        </td>
                                        <td>
                                            <span>${t.deadline != null ? t.deadline : 'Chưa đặt'}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${t.status == 'SUBMITTED'}">
                                                    <span class="badge b-submitted">Chờ Thẩm Định</span>
                                                </c:when>
                                                <c:when test="${t.status == 'PENDING_EXECUTIVE_APPROVAL'}">
                                                    <span class="badge b-escalated">Chờ TBT Duyệt</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge">${t.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="${pageContext.request.contextPath}/editorial/criteria-scoring?topicId=${t.topicId}"
                                               class="btn primary sm">
                                                Thẩm định &rarr;
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                        </c:choose>
                        <!-- Dynamic Empty Row -->
                        <tr id="empty-state-row" style="display: none;">
                            <td colspan="7">
                                <div class="empty">
                                    <div style="font-size: 32px; margin-bottom: 8px;">📭</div>
                                    <b>Hàng đợi hiện đang trống</b>
                                    <p>Không có đề tài nào phù hợp với bộ lọc hiện tại.</p>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination Bar -->
                <c:if test="${totalPages > 1}">
                    <div class="pagination-bar">
                        <small class="muted">Hiển thị trang <b>${currentPage}</b> / <b>${totalPages}</b> (Tổng số <b>${totalRecords}</b> đề tài)</small>
                        <div class="page-links">
                            <a href="${pageContext.request.contextPath}/editorial/pending-queue?status=${currentStatus}&search=${searchKeyword}&page=${currentPage - 1}"
                               class="page-link-item ${currentPage == 1 ? 'disabled' : ''}">&laquo; Trước</a>

                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <a href="${pageContext.request.contextPath}/editorial/pending-queue?status=${currentStatus}&search=${searchKeyword}&page=${i}"
                                   class="page-link-item ${currentPage == i ? 'active' : ''}">${i}</a>
                            </c:forEach>

                            <a href="${pageContext.request.contextPath}/editorial/pending-queue?status=${currentStatus}&search=${searchKeyword}&page=${currentPage + 1}"
                               class="page-link-item ${currentPage == totalPages ? 'disabled' : ''}">Sau &raquo;</a>
                        </div>
                    </div>
                </c:if>
            </div>
        </main>
    </div>
</div>

<script>
    function switchTab(status, evt) {
        if (evt) evt.preventDefault();

        // 1. Update button active states
        document.querySelectorAll('.tab-btn').forEach(function(btn) {
            btn.classList.remove('active');
        });
        var activeBtn = document.querySelector('.tab-btn[data-tab-status="' + status + '"]');
        if (activeBtn) activeBtn.classList.add('active');

        // 2. Update hidden form input
        var formInput = document.getElementById('form-status-input');
        if (formInput) formInput.value = status;

        // 3. Toggle rows with zero network latency
        var rows = document.querySelectorAll('tbody tr[data-topic-row="true"]');
        var visibleCount = 0;
        rows.forEach(function(row) {
            if (row.getAttribute('data-status') === status) {
                row.style.display = '';
                visibleCount++;
            } else {
                row.style.display = 'none';
            }
        });

        // 4. Update empty state visibility
        var emptyRow = document.getElementById('empty-state-row');
        if (emptyRow) {
            emptyRow.style.display = (visibleCount === 0) ? '' : 'none';
        }

        // 5. Update browser URL history without reload
        try {
            var url = new URL(window.location.href);
            url.searchParams.set('status', status);
            window.history.pushState({status: status}, '', url.toString());
        } catch (e) {
            // fallback
        }
    }

    // Check empty state on initial render
    document.addEventListener('DOMContentLoaded', function() {
        var currentStatus = '${currentStatus}';
        var rows = document.querySelectorAll('tbody tr[data-topic-row="true"]');
        var visibleCount = 0;
        rows.forEach(function(row) {
            if (row.getAttribute('data-status') === currentStatus) {
                visibleCount++;
            }
        });
        var emptyRow = document.getElementById('empty-state-row');
        if (emptyRow && visibleCount === 0) {
            emptyRow.style.display = '';
        }
    });
</script>
</body>
</html>
