<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Điều Động Phóng Viên & Cân Bằng Tải (SCR-2.5) | NTMCS — Tòa Soạn Hội Tụ</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .btn, .nav-item, .page-link-item, .tab-btn {
            text-decoration: none !important;
        }
        .step-progress {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-bottom: 24px;
            background: #fff;
            padding: 16px 20px;
            border-radius: 12px;
            border: 1px solid var(--line);
        }
        .step-item {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
            font-weight: 700;
            color: var(--muted);
        }
        .step-item.active {
            color: var(--primary);
        }
        .step-item.completed {
            color: #10b981;
        }
        .step-num {
            width: 28px;
            height: 28px;
            border-radius: 99px;
            background: var(--surface2);
            color: var(--muted);
            display: grid;
            place-items: center;
            font-size: 12px;
            font-weight: 800;
        }
        .step-item.active .step-num {
            background: var(--primary);
            color: #fff;
        }
        .step-item.completed .step-num {
            background: #10b981;
            color: #fff;
        }
        .step-arrow {
            color: var(--line);
            font-size: 16px;
        }
        .dispatch-layout {
            display: grid;
            grid-template-columns: 2.2fr 1fr;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 1024px) {
            .dispatch-layout {
                grid-template-columns: 1fr;
            }
        }
        .dossier-card {
            background: #f8fafc;
            border: 1px solid var(--line);
            border-left: 4px solid var(--primary);
            border-radius: 12px;
            padding: 18px 22px;
            margin-bottom: 22px;
        }
        .workload-table {
            width: 100%;
            border-collapse: separate;
            border-spacing: 0;
            margin-bottom: 20px;
        }
        .workload-table th {
            background: var(--surface2);
            color: var(--text);
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: .5px;
            padding: 12px 14px;
            border-bottom: 2px solid var(--line);
            text-align: left;
        }
        .workload-table td {
            padding: 14px;
            border-bottom: 1px solid var(--line);
            font-size: 13px;
            vertical-align: middle;
        }
        .workload-table tbody tr {
            cursor: pointer;
            transition: background .15s;
        }
        .workload-table tbody tr:hover {
            background: rgba(52, 88, 212, 0.04);
        }
        .workload-table tbody tr.selected {
            background: rgba(16, 185, 129, 0.08);
        }
        .form-control {
            width: 100%;
            border: 1px solid var(--line);
            border-radius: 10px;
            padding: 11px 14px;
            font-size: 14px;
            background: #fff;
            color: var(--text);
            box-sizing: border-box;
            transition: border-color .2s;
        }
        .form-control:focus {
            outline: none;
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(52, 88, 212, 0.15);
        }
        .reporter-avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: var(--primary);
            color: #fff;
            display: grid;
            place-items: center;
            font-weight: 700;
            font-size: 13px;
            flex-shrink: 0;
        }
    </style>
</head>
<body>
<div class="app">
    <!-- Static Sidebar -->
    <aside id="sidebar">
        <div class="brand">
            <div class="brand-logo">N</div>
            <div>
                <b>NTMCS</b>
                <small>Hệ Thống Tòa Soạn Hội Tụ</small>
            </div>
        </div>
        <nav>
            <div class="nav-group">
                <div class="nav-label">BAN BIÊN TẬP (WF-02)</div>
                <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="nav-item">
                    <i>📋</i> Hàng đợi chờ thẩm định
                </a>
                <a href="${pageContext.request.contextPath}/editorial/directive-topic" class="nav-item active">
                    <i>⚡</i> Đặt hàng đề tài chỉ đạo
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
            <span class="demo">SE2066 — NHÓM 4</span>
            <small>Phân hệ Biên tập (WF-02)</small>
        </div>
    </aside>

    <!-- Main Workspace -->
    <div class="main">
        <!-- Topbar -->
        <header class="topbar">
            <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
            <div style="font-weight: 700; color: var(--text);">
                🏛️ ${desk != null ? desk.deskName : 'Ban Thời sự - Chính trị'}
            </div>
            <div class="account-chip">
                <div class="avatar">TR</div>
                <div>
                    <b>Trường</b>
                    <small>Trưởng ban Biên tập</small>
                </div>
            </div>
        </header>

        <!-- Main Content -->
        <main>
            <!-- Page Header -->
            <div class="page-head">
                <div>
                    <div class="eyebrow">EDITORIAL WORKFLOW / SCR-2.5</div>
                    <h1>Điều Động Phóng Viên & Cân Bằng Tải</h1>
                    <p>Đánh giá ma trận tải công việc và phát lệnh tác nghiệp đề tài chỉ đạo xuống Trưởng nhóm phóng viên.</p>
                </div>
                <div class="page-actions">
                    <a href="${pageContext.request.contextPath}/editorial/directive-topic?topicId=${topic.topicId}" class="btn" style="border: 1px solid var(--line);">
                        &larr; Sửa nội dung đề tài
                    </a>
                </div>
            </div>

            <!-- 2-Step Workflow Stepper -->
            <div class="step-progress">
                <a href="${pageContext.request.contextPath}/editorial/directive-topic?topicId=${topic.topicId}" class="step-item completed">
                    <div class="step-num">✓</div>
                    <div>
                        <div>BƯỚC 1: SOẠN THẢO CHỈ ĐẠO</div>
                        <small style="font-weight: normal; color: #10b981;">Đã hoàn thành nội dung (SCR-2.4)</small>
                    </div>
                </a>
                <div class="step-arrow">&rarr;</div>
                <div class="step-item active">
                    <div class="step-num">2</div>
                    <div>
                        <div>BƯỚC 2: ĐIỀU ĐỘNG PHÓNG VIÊN</div>
                        <small style="font-weight: normal; color: var(--primary);">Bàn cân bằng tải & Giao việc (SCR-2.5)</small>
                    </div>
                </div>
            </div>

            <!-- Error Banner -->
            <c:if test="${not empty error}">
                <div style="margin-bottom: 18px; padding: 14px 18px; border-radius: 12px; font-weight: 600; display: flex; align-items: center; gap: 12px; background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce;">
                    <span style="font-size: 20px;">⚠️</span>
                    <div>
                        <b>Lỗi giao việc:</b> ${error}
                    </div>
                </div>
            </c:if>

            <!-- Topic Dossier Summary Card -->
            <div class="dossier-card">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; margin-bottom: 10px;">
                    <div>
                        <span class="badge badge-info" style="font-size: 11px; margin-right: 6px;">⚡ ĐẶT HÀNG TÒA SOẠN</span>
                        <span class="badge badge-neutral" style="font-size: 11px;">Mã bản thảo: #${topic.topicId}</span>
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/editorial/directive-topic?topicId=${topic.topicId}" class="btn btn-secondary btn-sm" style="font-size: 12px; padding: 4px 10px;">
                            ✏️ Sửa đề tài
                        </a>
                    </div>
                </div>
                <h3 style="margin: 0 0 10px 0; font-size: 18px; color: var(--text);">${topic.title}</h3>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; font-size: 13px; color: var(--muted);">
                    <div>
                        <b>Chuyên mục:</b> <span style="color: var(--text);">${categoryName}</span>
                    </div>
                    <div>
                        <b>Hạn chót tác nghiệp:</b> 
                        <span style="color: #bd3344; font-weight: 700;">
                            <fmt:formatDate value="${topic.deadline}" pattern="dd/MM/yyyy"/>
                        </span>
                    </div>
                    <div style="grid-column: 1 / -1;">
                        <b>Góc tiếp cận & Định hướng:</b> 
                        <span style="color: var(--text);">${topic.angle}</span>
                    </div>
                </div>
            </div>

            <!-- Form & Layout -->
            <form method="post" action="${pageContext.request.contextPath}/editorial/reporter-dispatch" id="dispatchForm" onsubmit="return validateCommission(event);">
                <input type="hidden" name="topicId" value="${topic.topicId}">

                <div class="dispatch-layout">
                    <!-- LEFT COLUMN: Workload Matrix & Commissioning -->
                    <div>
                        <div class="card" style="padding: 22px; margin-bottom: 22px;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                                <div>
                                    <h3 style="margin: 0; font-size: 16px;">📊 Ma Trận Tải Công Việc Phóng Viên</h3>
                                    <p style="margin: 4px 0 0 0; font-size: 13px; color: var(--muted);">
                                        Chọn 01 phóng viên đảm nhận vai trò <b>Trưởng nhóm tác nghiệp (Lead Reporter)</b>
                                    </p>
                                </div>
                                <span class="badge badge-neutral">${reporters.size()} phóng viên khả dụng</span>
                            </div>

                            <div style="overflow-x: auto;">
                                <table class="workload-table">
                                    <thead>
                                        <tr>
                                            <th style="width: 44px; text-align: center;">Chọn</th>
                                            <th>Phóng viên</th>
                                            <th style="text-align: center;">Đang làm</th>
                                            <th style="text-align: center;">Quá hạn</th>
                                            <th style="text-align: center;">Nghiệm thu</th>
                                            <th>Tình trạng tải</th>
                                            <th>Chi tiết đề tài đang chạy</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${reporters}" var="rep" varStatus="st">
                                            <tr onclick="selectReporterRow(${rep.userId})" id="row-${rep.userId}">
                                                <td style="text-align: center;">
                                                    <input type="radio" name="leadReporterId" id="rep-${rep.userId}" value="${rep.userId}"
                                                           data-name="${rep.fullName}" data-status="${rep.workloadStatus}"
                                                           ${st.first ? 'checked' : ''} onchange="highlightSelectedRow()">
                                                </td>
                                                <td>
                                                    <div style="display: flex; align-items: center; gap: 10px;">
                                                        <div class="reporter-avatar">
                                                            ${rep.fullName.substring(0, 1)}
                                                        </div>
                                                        <div>
                                                            <b style="color: var(--text);">${rep.fullName}</b>
                                                            <div style="font-size: 11px; color: var(--muted);">${rep.email}</div>
                                                            <c:if test="${rep.deskId == desk.deskId}">
                                                                <span class="badge badge-info" style="font-size: 10px; padding: 2px 6px; margin-top: 2px;">Cùng Ban</span>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td style="text-align: center;">
                                                    <b style="font-size: 14px; color: var(--text);">${rep.activeTopicsCount}</b>
                                                </td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${rep.overdueTopicsCount > 0}">
                                                            <span style="color: #bd3344; font-weight: 800;">${rep.overdueTopicsCount}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="color: var(--muted);">0</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: center;">
                                                    <span style="color: #10b981; font-weight: 700;">${rep.completedTopicsCount}</span>
                                                </td>
                                                <td>
                                                    <span class="badge ${rep.workloadBadgeClass}">
                                                        ${rep.workloadStatusLabel}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty rep.activeTopicTitles}">
                                                            <ul style="margin: 0; padding-left: 16px; font-size: 11px; color: var(--muted);">
                                                                <c:forEach items="${rep.activeTopicTitles}" var="tTitle">
                                                                    <li><c:out value="${tTitle}"/></li>
                                                                </c:forEach>
                                                            </ul>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="font-size: 12px; color: var(--muted); font-style: italic;">
                                                                Không có đề tài tồn đọng
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Commission Directives Box -->
                            <div style="margin-top: 16px;">
                                <label class="form-label" for="commissionNotes" style="font-weight: 700; font-size: 13px; margin-bottom: 8px; display: block;">
                                    📝 Chỉ đạo tác nghiệp bổ sung của Trưởng ban (tùy chọn)
                                </label>
                                <textarea id="commissionNotes" name="commissionNotes" class="form-control" rows="3"
                                          placeholder="Nhập yêu cầu chuyên môn cụ thể: Nguồn tin cần phỏng vấn, chứng cứ hiện trường cần thu thập, phối hợp thiết bị chuyên dụng...">${commissionNotes}</textarea>
                                <div style="font-size: 12px; color: var(--muted); margin-top: 6px;">
                                    Chỉ đạo này sẽ được gửi trực tiếp vào hệ thống Thông báo (In-App Notification) của phóng viên được chỉ định.
                                </div>
                            </div>
                        </div>

                        <!-- Action Submission Bar -->
                        <div style="display: flex; gap: 12px; justify-content: flex-end; align-items: center;">
                            <a href="${pageContext.request.contextPath}/editorial/directive-topic?topicId=${topic.topicId}" class="btn" style="border: 1px solid var(--line); font-weight: 600;">
                                &larr; Quay lại soạn thảo
                            </a>
                            <button type="submit" id="btnCommission" class="btn" style="background: linear-gradient(135deg, #10b981, #059669); color: #fff; font-weight: 700; padding: 12px 24px; border: none; border-radius: 10px; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 14px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);">
                                <span>Phát Lệnh Đặt Hàng & Kích Hoạt Tác Nghiệp</span>
                                <span style="font-size: 16px;">🚀</span>
                            </button>
                        </div>
                    </div>

                    <!-- RIGHT COLUMN: Guidelines & Role Rules -->
                    <div>
                        <!-- Workload Balancing Principles -->
                        <div class="card" style="padding: 20px; margin-bottom: 20px;">
                            <h4 style="margin: 0 0 12px 0; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                                <span>⚖️</span> Nguyên Tắc Cân Bằng Tải
                            </h4>
                            <ul style="margin: 0; padding-left: 18px; font-size: 13px; color: var(--muted); line-height: 1.6;">
                                <li><b>Ưu tiên phóng viên sẵn sàng:</b> Khuyến khích giao việc cho phóng viên có dưới 2 đề tài đang chạy để bảo đảm độ sâu bài viết.</li>
                                <li><b>Tránh dồn việc:</b> Phóng viên có từ 4 đề tài trở lên hoặc có đề tài quá hạn được đánh dấu <b>Quá tải</b>.</li>
                                <li><b>Đúng chuyên môn:</b> Đề tài thuộc Ban Thời sự - Chính trị nên ưu tiên phóng viên thuộc ban để bảo đảm am hiểu địa bàn.</li>
                            </ul>
                        </div>

                        <!-- Lead Reporter Rights Card -->
                        <div class="card" style="padding: 20px; margin-bottom: 20px;">
                            <h4 style="margin: 0 0 12px 0; font-size: 14px; display: flex; align-items: center; gap: 8px;">
                                <span>⭐</span> Quyền Hạn Trưởng Nhóm
                            </h4>
                            <p style="font-size: 13px; color: var(--muted); margin: 0 0 10px 0; line-height: 1.5;">
                                Khi phát lệnh đặt hàng, phóng viên được chỉ định sẽ giữ vai trò <b>LEAD_REPORTER</b>:
                            </p>
                            <ul style="margin: 0; padding-left: 18px; font-size: 13px; color: var(--muted); line-height: 1.6;">
                                <li>Nhận thông báo lập tức trong hệ thống.</li>
                                <li>Được quyền lập dự toán kinh phí tác nghiệp.</li>
                                <li>Được quyền yêu cầu điều phối xe chuyên dụng và thiết bị (SCR-1.5 / SCR-3.5).</li>
                                <li>Phụ trách nộp bài và giải trình tiến độ với Ban Biên Tập.</li>
                            </ul>
                        </div>

                        <!-- Atomic Transaction Assurance -->
                        <div style="background: #eef2ff; border: 1px solid #c7d2fe; border-radius: 12px; padding: 16px; font-size: 12px; color: #3730a3; line-height: 1.5;">
                            <b>🔒 Bảo đảm giao dịch ACID:</b> Khi phát lệnh, hệ thống sẽ tự động cấp mã đề tài chính thức (TOPIC-YYYY-XXX), chuyển trạng thái sang <b>ĐÃ DUYỆT (APPROVED)</b>, và gán thành viên vào cơ sở dữ liệu đồng thời.
                        </div>
                    </div>
                </div>
            </form>
        </main>
    </div>
</div>

<script>
    function selectReporterRow(userId) {
        var radio = document.getElementById('rep-' + userId);
        if (radio) {
            radio.checked = true;
            highlightSelectedRow();
        }
    }

    function highlightSelectedRow() {
        var radios = document.getElementsByName('leadReporterId');
        for (var i = 0; i < radios.length; i++) {
            var row = document.getElementById('row-' + radios[i].value);
            if (row) {
                if (radios[i].checked) {
                    row.classList.add('selected');
                } else {
                    row.classList.remove('selected');
                }
            }
        }
    }

    function validateCommission(event) {
        var radios = document.getElementsByName('leadReporterId');
        var selectedRadio = null;
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked) {
                selectedRadio = radios[i];
                break;
            }
        }

        if (!selectedRadio) {
            alert('Vui lòng chọn một phóng viên từ ma trận tải công việc để giao nhiệm vụ Trưởng nhóm.');
            event.preventDefault();
            return false;
        }

        var status = selectedRadio.getAttribute('data-status');
        var name = selectedRadio.getAttribute('data-name');

        if (status === 'OVERLOADED') {
            var confirmed = confirm('CẢNH BÁO CÂN BẰNG TẢI:\n\nPhóng viên ' + name + ' hiện đang có tải công việc cao hoặc đề tài quá hạn.\n\nBạn có chắc chắn muốn giao thêm đề tài chỉ đạo này không?');
            if (!confirmed) {
                event.preventDefault();
                return false;
            }
        }

        return true;
    }

    // Initialize highlight on load
    document.addEventListener('DOMContentLoaded', function() {
        highlightSelectedRow();
    });
</script>
</body>
</html>
