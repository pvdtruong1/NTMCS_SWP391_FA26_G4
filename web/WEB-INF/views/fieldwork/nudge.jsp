<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bàn Đôn Đốc Tiến Độ Hiện Trường (Nudge) — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .nudge-grid {
            display: grid;
            grid-template-columns: 420px 1fr;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 992px) {
            .nudge-grid {
                grid-template-columns: 1fr;
            }
        }
        .nudge-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.04);
        }
        .template-btn {
            background: #f1f5f9;
            border: 1px solid var(--line);
            border-radius: 6px;
            padding: 4px 10px;
            font-size: 11px;
            font-weight: 600;
            color: #334155;
            cursor: pointer;
            transition: all 0.15s ease;
        }
        .template-btn:hover {
            background: #e2e8f0;
            color: #0f172a;
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="../sidebar.jsp">
        <jsp:param name="active" value="fieldwork-nudge"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="../topbar.jsp">
            <jsp:param name="title" value="Bàn Đôn Đốc Tiến Độ Hiện Trường (SCR-4.7)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">ĐIỀU PHỐI &amp; GIÁM SÁT / WF-04</span>
                    <h1>Bàn Đôn Đốc Tiến Độ Tác Nghiệp (SCR-4.7)</h1>
                    <p>Hỗ trợ Trưởng ban Biên tập &amp; Điều phối viên phát lệnh nhắc việc (Nudge) tới phóng viên tác nghiệp hiện trường theo quy chuẩn SLA.</p>
                </div>
            </div>

            <div class="nudge-grid">
                <!-- Left: Nudge Form -->
                <div class="nudge-card">
                    <h3 style="margin-top:0; margin-bottom:16px; font-size:16px; display:flex; align-items:center; gap:8px;">
                        🔔 Soạn Lệnh Đôn Đốc
                    </h3>

                    <form id="nudgeForm" onsubmit="submitNudge(event)">
                        <div style="margin-bottom:14px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Chọn Đề Tài &amp; Phóng Viên Nhận Lệnh</label>
                            <select id="nudgeSelect" required style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;" onchange="onSelectAssignment()">
                                <option value="">-- Chọn phân công tác nghiệp --</option>
                                <c:forEach var="a" items="${assignments}">
                                    <option value="${a[0]}_${a[3]}" data-topic-id="${a[0]}" data-topic-code="${a[1]}" data-reporter-id="${a[3]}" data-reporter-name="${a[4]}">
                                        [${a[1]}] ${a[4]} (${a[5]}) &bull; ${a[2]}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <input type="hidden" id="nudgeTopicId" value=""/>
                        <input type="hidden" id="nudgeReporterId" value=""/>

                        <div style="margin-bottom:14px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Mức Độ Khẩn Nguy</label>
                            <select id="nudgeUrgency" style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px;">
                                <option value="NORMAL">Thông thường (NORMAL) — Nhắc lịch định kỳ</option>
                                <option value="HIGH" selected>Ưu tiên cao (HIGH) — Cảnh báo sắp tới hạn SLA</option>
                                <option value="EMERGENCY">Khẩn cấp (EMERGENCY) — Quá hạn tác nghiệp / Báo động</option>
                            </select>
                        </div>

                        <div style="margin-bottom:14px;">
                            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                                <label style="font-size:12px; font-weight:600;">Mẫu Lời Nhắc Nhanh</label>
                            </div>
                            <div style="display:flex; flex-wrap:wrap; gap:6px;">
                                <button type="button" class="template-btn" onclick="applyTemplate('SLA')">⚡ Cảnh báo SLA</button>
                                <button type="button" class="template-btn" onclick="applyTemplate('SUPPLEMENT')">📸 Bổ sung tư liệu</button>
                                <button type="button" class="template-btn" onclick="applyTemplate('OVERDUE')">🚨 Quá hạn nộp bài</button>
                            </div>
                        </div>

                        <div style="margin-bottom:14px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Tiêu Đề Nhắc Việc</label>
                            <input type="text" id="nudgeTitle" required placeholder="Đôn đốc tiến độ tác nghiệp hiện trường" style="width:100%; padding:9px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"/>
                        </div>

                        <div style="margin-bottom:18px;">
                            <label style="display:block; font-size:12px; font-weight:600; margin-bottom:6px;">Nội Dung Đôn Đốc</label>
                            <textarea id="nudgeMessage" rows="4" required placeholder="Nhập lời nhắc chi tiết gửi tới chuông thông báo phóng viên..." style="width:100%; padding:10px 12px; border:1px solid var(--line); border-radius:8px; font-size:13px; box-sizing:border-box;"></textarea>
                        </div>

                        <button type="submit" class="btn primary full" id="btnSendNudge" style="padding:11px; font-weight:700;">
                            🚀 Phát Lệnh Đôn Đốc
                        </button>
                    </form>
                </div>

                <!-- Right: Active Assignments Table -->
                <div class="nudge-card" style="padding:0; overflow:hidden;">
                    <div style="padding:18px 24px; border-bottom:1px solid var(--line); display:flex; justify-content:space-between; align-items:center;">
                        <h3 style="margin:0; font-size:16px;">Danh Sách Tác Nghiệp Hiện Trường</h3>
                        <span style="font-size:12px; color:var(--muted); font-weight:600;">${assignments.size()} phân công hoạt động</span>
                    </div>

                    <table class="table" style="width:100%; margin:0;">
                        <thead>
                            <tr>
                                <th>Mã Đề Tài</th>
                                <th>Tên Đề Tài</th>
                                <th>Phóng Viên Đảm Nhận</th>
                                <th>Vai Trò</th>
                                <th style="text-align:right;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${assignments}">
                                <tr>
                                    <td>
                                        <span style="font-family:monospace; font-weight:700; color:var(--primary); font-size:12px;">${a[1]}</span>
                                    </td>
                                    <td style="font-weight:600; color:#1e293b; max-width:280px;">
                                        <c:out value="${a[2]}" />
                                    </td>
                                    <td>
                                        <b><c:out value="${a[4]}" /></b>
                                    </td>
                                    <td>
                                        <span style="font-size:11px; padding:2px 8px; border-radius:10px; background:#f1f5f9; color:#475569; font-weight:600;">
                                            ${a[5]}
                                        </span>
                                    </td>
                                    <td style="text-align:right;">
                                        <button type="button" class="btn" style="padding:4px 10px; font-size:11px;" onclick="selectRow('${a[0]}', '${a[1]}', '${a[3]}', '${a[4]}')">
                                            ⚡ Đôn đốc
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty assignments}">
                                <tr>
                                    <td colspan="5" style="text-align:center; padding:30px; color:var(--muted);">
                                        Hiện không có phân công tác nghiệp nào đang hoạt động.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
function onSelectAssignment() {
    const sel = document.getElementById('nudgeSelect');
    const opt = sel.options[sel.selectedIndex];
    if (opt && opt.value) {
        document.getElementById('nudgeTopicId').value = opt.getAttribute('data-topic-id');
        document.getElementById('nudgeReporterId').value = opt.getAttribute('data-reporter-id');
        const code = opt.getAttribute('data-topic-code');
        document.getElementById('nudgeTitle').value = 'Đôn đốc tiến độ tác nghiệp — ' + code;
    } else {
        document.getElementById('nudgeTopicId').value = '';
        document.getElementById('nudgeReporterId').value = '';
    }
}

function selectRow(topicId, topicCode, reporterId, reporterName) {
    const val = topicId + '_' + reporterId;
    const sel = document.getElementById('nudgeSelect');
    sel.value = val;
    document.getElementById('nudgeTopicId').value = topicId;
    document.getElementById('nudgeReporterId').value = reporterId;
    document.getElementById('nudgeTitle').value = 'Đôn đốc tiến độ tác nghiệp — ' + topicCode;
    applyTemplate('SLA');
    document.getElementById('nudgeTitle').focus();
}

function applyTemplate(type) {
    const sel = document.getElementById('nudgeSelect');
    const opt = sel.options[sel.selectedIndex];
    const code = opt && opt.value ? opt.getAttribute('data-topic-code') : '[ĐỀ TÀI]';
    const rep = opt && opt.value ? opt.getAttribute('data-reporter-name') : 'Đồng chí';

    if (type === 'SLA') {
        document.getElementById('nudgeMessage').value =
            'Đồng chí ' + rep + ' vui lòng khẩn trương hoàn thiện và cập nhật tiến độ đề tài ' + code + ' theo đúng hạn ngạch SLA đã cam kết.';
        document.getElementById('nudgeUrgency').value = 'HIGH';
    } else if (type === 'SUPPLEMENT') {
        document.getElementById('nudgeMessage').value =
            'Ban Biên Tập yêu cầu đồng chí ' + rep + ' bổ sung thêm tư liệu hiện trường (ảnh gốc độ phân giải cao + video phỏng vấn nhân chứng) cho đề tài ' + code + '.';
        document.getElementById('nudgeUrgency').value = 'NORMAL';
    } else if (type === 'OVERDUE') {
        document.getElementById('nudgeMessage').value =
            '[BÁO ĐỘNG QUÁ HẠN] Đề tài ' + code + ' đã vượt quá thời hạn tác nghiệp theo quy định. Yêu cầu đồng chí ' + rep + ' lập tức nộp sản phẩm hoàn thành hoặc báo cáo lý do chậm trễ trong hôm nay!';
        document.getElementById('nudgeUrgency').value = 'EMERGENCY';
    }
}

async function submitNudge(e) {
    e.preventDefault();
    const btn = document.getElementById('btnSendNudge');
    btn.disabled = true;

    try {
        const topicId = parseInt(document.getElementById('nudgeTopicId').value, 10);
        const reporterUserId = parseInt(document.getElementById('nudgeReporterId').value, 10);
        const title = document.getElementById('nudgeTitle').value.trim();
        const message = document.getElementById('nudgeMessage').value.trim();
        const urgency = document.getElementById('nudgeUrgency').value;

        if (!topicId || !reporterUserId) {
            alert('Vui lòng chọn đề tài và phóng viên nhận đôn đốc.');
            btn.disabled = false;
            return;
        }

        const resp = await fetch('${base}/api/task-nudges', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ topicId, reporterUserId, title, message, urgency })
        });
        const res = await resp.json();
        if (resp.ok) {
            alert(res.message || 'Đã gửi lời nhắc đôn đốc thành công!');
            document.getElementById('nudgeTitle').value = '';
            document.getElementById('nudgeMessage').value = '';
            document.getElementById('nudgeSelect').value = '';
            document.getElementById('nudgeTopicId').value = '';
            document.getElementById('nudgeReporterId').value = '';
        } else {
            alert(res.error || 'Gửi đôn đốc thất bại.');
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
    } finally {
        btn.disabled = false;
    }
}
</script>
</body>
</html>
