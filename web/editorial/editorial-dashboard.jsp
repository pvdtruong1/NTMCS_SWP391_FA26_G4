<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NTMCS - SCR-2.1: Editorial Board Executive Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }
        .kpi-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 16px 20px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
            transition: transform 0.15s ease, box-shadow 0.15s ease;
        }
        .kpi-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        .kpi-card.highlight {
            border-color: #93c5fd;
            background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
        }
        .kpi-card.warning-card {
            border-color: #fde68a;
            background: linear-gradient(180deg, #fffbeb 0%, #ffffff 100%);
        }
        .kpi-icon {
            font-size: 22px;
            margin-bottom: 8px;
        }
        .kpi-title {
            font-size: 13px;
            font-weight: 600;
            color: var(--muted);
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .kpi-value {
            font-size: 28px;
            font-weight: 700;
            color: var(--text);
            margin: 6px 0;
            font-feature-settings: "tnum";
        }
        .kpi-sub {
            font-size: 12px;
            color: var(--muted);
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .rule01-tag {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 600;
        }
        .rule01-pass {
            background: #ecfdf5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }
        .rule01-blocked {
            background: #fef2f2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
        .token-chip {
            font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
            background: #f1f5f9;
            color: #0f172a;
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            border: 1px solid #cbd5e1;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .token-copy-btn {
            background: none;
            border: none;
            cursor: pointer;
            padding: 2px 4px;
            color: var(--primary);
            font-size: 13px;
            border-radius: 4px;
        }
        .token-copy-btn:hover {
            background: #e2e8f0;
        }
        .desk-selector {
            padding: 6px 12px;
            border-radius: 8px;
            border: 1px solid var(--line);
            background: var(--surface);
            font-size: 13px;
            font-weight: 600;
            color: var(--text);
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="/WEB-INF/views/sidebar.jsp">
        <jsp:param name="active" value="editorial-dashboard"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="/WEB-INF/views/topbar.jsp">
            <jsp:param name="title" value="🏛️ ${metrics.deskName != null ? metrics.deskName : 'Ban Thời sự - Chính trị'}"/>
        </jsp:include>

        <!-- Main Body -->
        <main>
            <!-- Page Header -->
            <div class="page-head" style="margin-bottom: 20px;">
                <div>
                    <span class="eyebrow">EDITORIAL WORKFLOW / SCR-2.1 & RULE-02</span>
                    <h1>Dashboard Điều Phối Ban Biên Tập & Bàn Giao CMS</h1>
                    <p>Giám sát chỉ số KPI sản xuất tin bài, thẩm định nghiệm thu đề tài hoàn thành và phát hành mã bảo mật HMAC-SHA256 Token chuyển giao CMS.</p>
                </div>
                <div class="page-actions" style="display:flex; align-items:center; gap:12px; flex-wrap:wrap;">
                    <c:if test="${isExecutive}">
                        <form method="get" action="${pageContext.request.contextPath}/editorial/dashboard" id="deskFilterForm" style="margin:0;">
                            <input type="hidden" name="as" value="tbt">
                            <label for="deskIdSelect" style="font-size:12px; font-weight:700; color:var(--muted); margin-right:4px;">PHẠM VI ĐIỀU HÀNH:</label>
                            <select name="deskId" id="deskIdSelect" class="desk-selector" onchange="document.getElementById('deskFilterForm').submit();">
                                <option value="0" ${selectedDeskId == 0 ? 'selected' : ''}>🏢 Toàn Bộ Tòa Soạn (Tất Cả Các Ban)</option>
                                <c:forEach var="d" items="${deskList}">
                                    <option value="${d.deskId}" ${selectedDeskId == d.deskId ? 'selected' : ''}>🏛️ ${d.deskName}</option>
                                </c:forEach>
                            </select>
                        </form>
                    </c:if>
                    <div style="font-size: 11px; color: var(--muted); background: var(--surface2); padding: 6px 10px; border-radius: 8px; border: 1px solid var(--line);">
                        Giả lập: 
                        <a href="${pageContext.request.contextPath}/editorial/dashboard?as=editor" style="font-weight:600; color:var(--primary); text-decoration:none;">BTV Trường</a> | 
                        <a href="${pageContext.request.contextPath}/editorial/dashboard?as=tbt" style="font-weight:600; color:var(--primary); text-decoration:none;">TBT Cấp Cao</a>
                    </div>
                </div>
            </div>

            <!-- PRG Feedback Alert Banners -->
            <c:if test="${not empty msg and msg eq 'accepted'}">
                <div class="alert alert-success" style="margin-bottom: 20px; display:flex; flex-direction:column; gap:8px;">
                    <div style="display:flex; align-items:center; gap:8px;">
                        <span style="font-size: 18px;">✅</span>
                        <strong>Nghiệm thu thành công!</strong> Đề tài <b>#${code}</b> đã đạt chuẩn chất lượng và được chuyển sang trạng thái <b>ACCEPTED_READY_FOR_CMS</b>.
                    </div>
                    <c:if test="${not empty token}">
                        <div style="background: rgba(255,255,255,0.7); padding: 8px 12px; border-radius: 6px; border: 1px solid #a7f3d0; display:flex; align-items:center; justify-content:space-between;">
                            <div>
                                <span style="font-size:12px; color:#065f46; font-weight:600;">MÃ MẬT MÃ BÀN GIAO CMS (HMAC-SHA256 TOKEN):</span><br>
                                <span class="token-chip" id="tokenAlertValue">${token}</span>
                            </div>
                            <button type="button" class="btn btn-secondary btn-sm" onclick="copyTokenToClipboard('${token}')">📋 Sao Chép Token</button>
                        </div>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${not empty msg and msg eq 'revision_requested'}">
                <div class="alert alert-info" style="margin-bottom: 20px;">
                    <span style="font-size: 18px;">ℹ️</span>
                    Đã chuyển đề tài <b>#${code}</b> về trạng thái <b>Yêu Cầu Hoàn Thiện Lại (REVISION_REQUESTED)</b> kèm ý kiến chỉ đạo gửi tới Phóng viên.
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger" style="margin-bottom: 20px;">
                    <span style="font-size: 18px;">⚠️</span>
                    <strong>Lỗi thao tác:</strong> ${error}
                </div>
            </c:if>

            <!-- 5 KPI Summary Cards -->
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-icon">📋</div>
                    <div class="kpi-title">Chờ Thẩm Định Cấp 1 & 2</div>
                    <div class="kpi-value">${metrics.pendingReviewCount}</div>
                    <div class="kpi-sub">
                        <a href="${pageContext.request.contextPath}/editorial/pending-queue" style="color:var(--primary); font-weight:600; text-decoration:none;">Vào hàng đợi thẩm định →</a>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-icon">🏃</div>
                    <div class="kpi-title">Đang Tác Nghiệp Hiện Trường</div>
                    <div class="kpi-value">${metrics.activeFieldworkCount}</div>
                    <div class="kpi-sub">Tiến độ sản xuất thực địa</div>
                </div>

                <div class="kpi-card highlight">
                    <div class="kpi-icon">📦</div>
                    <div class="kpi-title">Chờ Nghiệm Thu CMS</div>
                    <div class="kpi-value" style="color: #1d4ed8;">${metrics.pendingHandoverCount}</div>
                    <div class="kpi-sub">
                        <span style="color:#2563eb; font-weight:600;">Cần Trưởng ban duyệt bài nộp</span>
                    </div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-icon">🚀</div>
                    <div class="kpi-title">Đã Xuất Bản / Bàn Giao CMS</div>
                    <div class="kpi-value" style="color: #059669;">${metrics.acceptedCmsCount}</div>
                    <div class="kpi-sub">Đã cấp Token HMAC-SHA256</div>
                </div>

                <div class="kpi-card warning-card">
                    <div class="kpi-icon">⚡</div>
                    <div class="kpi-title">Hiệu Suất Ban Biên Tập</div>
                    <div class="kpi-value" style="font-size:22px;">${metrics.formattedApprovalRate}</div>
                    <div class="kpi-sub">Thời gian duyệt TB: ~${metrics.formattedLatency}h</div>
                </div>
            </div>

            <!-- Section: Đề Xuất Mới Chờ Thẩm Định Rubric (SUBMITTED / PENDING_EXECUTIVE_APPROVAL) -->
            <div class="card" style="margin-bottom: 28px;">
                <div class="card-head" style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px;">
                    <div>
                        <h2 style="font-size: 16px; margin:0; display:flex; align-items:center; gap:8px;">
                            <span>📋</span> Hàng Đợi Chờ Thẩm Định Đề Tài (Đề Xuất Mới Từ Phóng Viên)
                        </h2>
                        <small style="color:var(--muted);">Tiếp nhận đề cương ban đầu từ Phóng viên cần thẩm định 4 tiêu chí Rubric cấp 1</small>
                    </div>
                    <div style="display:flex; align-items:center; gap:10px;">
                        <span class="badge ${metrics.pendingReviewCount > 0 ? 'badge-primary' : 'badge-secondary'}">
                            ${metrics.pendingReviewCount} đề tài chờ thẩm định
                        </span>
                        <a href="${pageContext.request.contextPath}/editorial/pending-queue" class="btn sm" style="font-weight: 600; text-decoration: none;">
                            Vào hàng đợi đầy đủ &rarr;
                        </a>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${not empty pendingPitches}">
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 80px;">ID</th>
                                        <th>Tiêu Đề &amp; Góc Tiếp Cận</th>
                                        <th>Chuyên Mục</th>
                                        <th>Phóng Viên Đề Xuất</th>
                                        <th>Hạn Dự Kiến</th>
                                        <th>Trạng Thái</th>
                                        <th style="text-align: right; min-width: 140px;">Hành Động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="p" items="${pendingPitches}">
                                        <tr>
                                            <td><b>#${p.topicId}</b></td>
                                            <td>
                                                <div style="font-weight: 600; color: var(--text);">${p.title}</div>
                                                <div style="font-size: 12px; color: var(--muted); margin-top: 3px;">
                                                    <c:out value="${p.angle}" />
                                                </div>
                                            </td>
                                            <td>
                                                <span class="badge badge-secondary">${p.categoryName}</span>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;">${p.authorName}</div>
                                                <small style="color: var(--muted);">${p.authorEmail}</small>
                                            </td>
                                            <td>
                                                <span style="font-size: 12px;">${p.deadline}</span>
                                            </td>
                                            <td>
                                                <span class="badge b-submitted">Chờ Thẩm Định</span>
                                            </td>
                                            <td style="text-align: right;">
                                                <a href="${pageContext.request.contextPath}/editorial/criteria-scoring?topicId=${p.topicId}" class="btn primary sm" style="text-decoration:none;">
                                                    Thẩm định &rarr;
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="padding: 28px 20px; text-align: center;">
                            <p style="color: var(--muted); margin: 0; font-size: 13px;">
                                Hiện không có đề xuất đề tài mới nào cần thẩm định cấp 1.
                            </p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Section A: Bàn Nghiệm Thu & Bàn Giao CMS (DELIVERED topics) -->
            <c:set var="deliverables" value="${not empty deliverables ? deliverables : pendingDeliverables}" />
            <div class="card" style="margin-bottom: 28px;">
                <div class="card-head" style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <h2 style="font-size: 16px; margin:0; display:flex; align-items:center; gap:8px;">
                            <span>📦</span> Hàng Đợi Chờ Thẩm Định Nghiệm Thu Bản Thảo (DELIVERED)
                        </h2>
                        <small style="color:var(--muted);">Kiểm tra chốt chặn tài sản (RULE-01) và chất lượng bài viết trước khi cấp Token chuyển giao</small>
                    </div>
                    <span class="badge ${deliverables.size() > 0 ? 'badge-primary' : 'badge-secondary'}">
                        ${deliverables.size()} đề tài chờ nghiệm thu
                    </span>
                </div>

                <c:choose>
                    <c:when test="${not empty deliverables}">
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 110px;">Mã Đề Tài</th>
                                        <th>Tiêu Đề & Góc Tiếp Cận</th>
                                        <th>Chuyên Mục / Ban</th>
                                        <th>Trưởng Nhóm / Tác Giả</th>
                                        <th>Hạn Nộp</th>
                                        <th>Chốt Chặn Xe & Máy (RULE-01)</th>
                                        <th style="text-align: right; min-width: 230px;">Hành Động Nghiệm Thu</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${deliverables}">
                                        <tr>
                                            <td>
                                                <b style="color:var(--primary); font-family:monospace;">
                                                    ${not empty item.topicCode ? item.topicCode : ('#TOPIC-' + item.topicId)}
                                                </b>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600; color: var(--text);">${item.title}</div>
                                                <div style="font-size: 12px; color: var(--muted); margin-top: 3px;">
                                                    ${item.angle}
                                                </div>
                                            </td>
                                            <td>
                                                <span class="badge badge-secondary">${item.categoryName}</span>
                                                <div style="font-size: 11px; color: var(--muted); margin-top: 2px;">${item.deskName}</div>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;">${item.leadReporterName}</div>
                                                <small style="color: var(--muted);">${item.authorEmail}</small>
                                            </td>
                                            <td>
                                                <span style="font-size: 12px;">${item.deadline}</span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.rule01Passed}">
                                                        <span class="rule01-tag rule01-pass" title="Tất cả thiết bị và phương tiện mượn đã được hoàn trả về kho an toàn.">
                                                            ✓ Đã trả đủ tài sản
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="rule01-tag rule01-blocked" title="${item.unreturnedAssetsSummary}">
                                                            ✕ Còn ${item.unreturnedAssetCount} tài sản chưa trả
                                                        </span>
                                                        <div style="font-size: 10px; color: #b91c1c; margin-top: 2px; max-width: 170px; word-break: break-word;">
                                                            ${item.unreturnedAssetsSummary}
                                                        </div>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td style="text-align: right; white-space: nowrap;">
                                                <a href="${pageContext.request.contextPath}/editorial/deliverable-review?topicId=${item.topicId}"
                                                   class="btn primary sm" style="text-decoration:none; font-weight:700; display:inline-flex; align-items:center; gap:6px;">
                                                    🔍 Thẩm Định &amp; Nghiệm Thu &rarr;
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="padding: 40px 20px; text-align: center;">
                            <div style="font-size: 36px; margin-bottom: 10px;">☕</div>
                            <h3 style="margin: 0 0 6px 0; font-size: 16px;">Không có đề tài nào chờ nghiệm thu</h3>
                            <p style="color: var(--muted); margin: 0; font-size: 13px;">
                                Tất cả bài viết nộp từ Phóng viên hiện tại đã được xử lý hoàn tất hoặc đang trong giai đoạn tác nghiệp.
                            </p>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Section B: Recent CMS Handovers (ACCEPTED_READY_FOR_CMS with HMAC Token) -->
            <div class="card">
                <div class="card-head" style="display:flex; justify-content:space-between; align-items:center;">
                    <div>
                        <h2 style="font-size: 16px; margin:0; display:flex; align-items:center; gap:8px;">
                            <span>🔐</span> Danh Sách Đề Tài Đã Nghiệm Thu & Sẵn Sàng Bàn Giao CMS (ACCEPTED_READY_FOR_CMS)
                        </h2>
                        <small style="color:var(--muted);">Bằng chứng mật mã bất biến được lưu trữ trong Audit Log (Zero DB Mutation - RULE-02)</small>
                    </div>
                    <span class="badge badge-success">${recentHandovers.size()} đề tài READY_FOR_CMS</span>
                </div>

                <c:choose>
                    <c:when test="${not empty recentHandovers}">
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th style="width: 120px;">Mã Đề Tài</th>
                                        <th>Tiêu Đề Bài Viết</th>
                                        <th>Chuyên Mục / Ban</th>
                                        <th>Người Nghiệm Thu</th>
                                        <th>Thời Điểm Cấp</th>
                                        <th>Chu Kỳ</th>
                                        <th style="width: 250px;">Mã Mật Mã (HMAC-SHA256 Token)</th>
                                        <th style="text-align: right;">Trạng Thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="h" items="${recentHandovers}">
                                        <tr>
                                            <td>
                                                <b style="color:var(--primary); font-family:monospace;">
                                                    ${not empty h.topicCode ? h.topicCode : ('#TOPIC-' + h.topicId)}
                                                </b>
                                            </td>
                                            <td>
                                                <div style="font-weight: 600;">${h.title}</div>
                                                <c:if test="${not empty h.note}">
                                                    <small style="color: var(--muted);">Ghi chú: ${h.note}</small>
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="badge badge-secondary">${h.categoryName}</span>
                                                <div style="font-size:11px; color:var(--muted);">${h.deskName}</div>
                                            </td>
                                            <td>
                                                <b>${h.editorName}</b>
                                            </td>
                                            <td>
                                                <span style="font-size: 12px; color: var(--muted);">${h.mintedAt}</span>
                                            </td>
                                            <td>
                                                <span class="badge badge-secondary">v${h.cycleNumber}</span>
                                            </td>
                                            <td>
                                                <div style="display:flex; align-items:center; gap:4px;">
                                                    <span class="token-chip" title="${h.tokenHash}">${h.shortToken}</span>
                                                    <button type="button" class="token-copy-btn" title="Sao chép toàn bộ Token 64 ký tự"
                                                            onclick="copyTokenToClipboard('${h.tokenHash}')">
                                                        📋
                                                    </button>
                                                </div>
                                            </td>
                                            <td style="text-align: right;">
                                                <span class="badge badge-success">READY_FOR_CMS</span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="padding: 30px; text-align: center; color: var(--muted);">
                            Chưa có đề tài nào được cấp Token bàn giao CMS.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
        <footer style="padding: 20px 28px; color: var(--muted); font-size: 12px; border-top: 1px solid var(--line); display: flex; justify-content: space-between; flex-wrap: wrap; gap: 12px; align-items: center; margin-top: 32px;">
            <div>
                <strong>NTMCS</strong> — Newsroom Topic Management &amp; Coordination System &middot; <em>Phân hệ BTV Chuyên Môn (WF-02)</em>
            </div>
            <div>
                SE2066 &middot; Nhóm 4 &middot; BTV: <strong>Phạm Vũ Đình Trường</strong>
            </div>
        </footer>
    </div>
</div>

<script>
    function copyTokenToClipboard(tokenText) {
        if (!tokenText) return;
        navigator.clipboard.writeText(tokenText).then(() => {
            alert('Đã sao chép HMAC-SHA256 Token vào clipboard:\n' + tokenText);
        }).catch(err => {
            prompt('Sao chép mã Token tại đây:', tokenText);
        });
    }
</script>
</body>
</html>
