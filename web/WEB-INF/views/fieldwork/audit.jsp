<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký Phát Thanh &amp; Đọc Thông Báo — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
    <style>
        .badge-read {
            background: #dcfce7;
            color: #15803d;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
        }
        .badge-unread {
            background: #fee2e2;
            color: #b91c1c;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 700;
        }
        .filter-btn {
            padding: 6px 14px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            border: 1px solid var(--line);
            background: var(--surface);
            color: var(--muted);
            cursor: pointer;
        }
        .filter-btn.active {
            background: var(--primary);
            color: #fff;
            border-color: var(--primary);
        }
    </style>
</head>
<body>
<div class="app">
    <jsp:include page="../sidebar.jsp">
        <jsp:param name="active" value="fieldwork-audit"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="../topbar.jsp">
            <jsp:param name="title" value="Nhật Ký Phát Thanh &amp; Báo Cáo Tỷ Lệ Đọc (SCR-4.8)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">GIÁM SÁT TRUYỀN THÔNG NỘI BỘ / WF-04</span>
                    <h1>Nhật Ký Phát Thanh &amp; Đôn Đốc Hiện Trường (SCR-4.8)</h1>
                    <p>Theo dõi tỷ lệ đọc thông báo, lịch sử phát thanh điều phối và thực hiện Ping trực tiếp đối với phóng viên chưa đọc.</p>
                </div>
            </div>

            <c:if test="${not empty permissionError}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${permissionError}" />
                </div>
            </c:if>

            <!-- Metrics -->
            <c:set var="totalCount" value="${auditLogs != null ? auditLogs.size() : 0}"/>
            <c:set var="readCount" value="0"/>
            <c:forEach var="log" items="${auditLogs}">
                <c:if test="${log[9] == true}"><c:set var="readCount" value="${readCount + 1}"/></c:if>
            </c:forEach>
            <c:set var="unreadCount" value="${totalCount - readCount}"/>
            <c:set var="readRate" value="${totalCount > 0 ? (readCount * 100 / totalCount) : 0}"/>

            <div class="stats" style="grid-template-columns: repeat(4, 1fr); margin-bottom: 24px;">
                <div class="stat">
                    <div class="stat-icon" style="background: #eef2ff; color: var(--primary);">📡</div>
                    <div>
                        <span>TỔNG THÔNG BÁO</span>
                        <strong>${totalCount}</strong>
                        <small style="color: var(--muted);">Lượt điều phối &amp; sự cố</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #dcfce7; color: #16a34a;">👁️</div>
                    <div>
                        <span>ĐÃ TIẾP NHẬN ĐỌC</span>
                        <strong>${readCount}</strong>
                        <small style="color: #16a34a;">Xác nhận tiếp thu lệnh</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #fee2e2; color: #dc2626;">⏳</div>
                    <div>
                        <span>CHƯA XEM</span>
                        <strong>${unreadCount}</strong>
                        <small style="color: #dc2626;">Cần đôn đốc nhắc nhở</small>
                    </div>
                </div>
                <div class="stat">
                    <div class="stat-icon" style="background: #fef3c7; color: #d97706;">📊</div>
                    <div>
                        <span>TỶ LỆ TIẾP THU</span>
                        <strong><fmt:formatNumber value="${readRate}" maxFractionDigits="1"/>%</strong>
                        <small style="color: var(--muted);">Hiệu lực truyền thông</small>
                    </div>
                </div>
            </div>

            <!-- Filters -->
            <div style="background:var(--surface); border:1px solid var(--line); border-radius:10px; padding:14px 18px; margin-bottom:16px; display:flex; justify-content:space-between; align-items:center; gap:16px; flex-wrap:wrap;">
                <div style="display:flex; gap:8px;">
                    <button type="button" class="filter-btn active" onclick="setAuditFilter('ALL', this)">Tất cả (${totalCount})</button>
                    <button type="button" class="filter-btn" onclick="setAuditFilter('READ', this)">Đã đọc (${readCount})</button>
                    <button type="button" class="filter-btn" onclick="setAuditFilter('UNREAD', this)">Chưa đọc (${unreadCount})</button>
                </div>
                <div style="flex:1; max-width:320px;">
                    <input type="text" id="auditSearchInput" placeholder="Tìm theo người gửi, người nhận, đề tài..." onkeyup="searchAuditLogs()" style="width:100%; padding:8px 12px; border:1px solid var(--line); border-radius:6px; font-size:13px; box-sizing:border-box;"/>
                </div>
            </div>

            <!-- Table of Audits -->
            <div style="background:var(--surface); border:1px solid var(--line); border-radius:12px; overflow:hidden;">
                <table class="table" style="width:100%; margin:0;" id="auditTable">
                    <thead>
                        <tr>
                            <th>Mã</th>
                            <th>Người Gửi</th>
                            <th>Người Nhận (Phòng/Ban)</th>
                            <th>Đề Tài</th>
                            <th>Mức Độ</th>
                            <th>Nội Dung Thông Báo</th>
                            <th>Thời Gian Gửi</th>
                            <th>Trạng Thái</th>
                            <th style="text-align:right;">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${auditLogs}">
                            <tr class="audit-row" data-read="${item[9]}">
                                <td><small style="color:var(--muted); font-family:monospace;">#${item[0]}</small></td>
                                <td><b><c:out value="${item[11]}" default="Hệ thống" /></b></td>
                                <td>
                                    <div><b><c:out value="${item[2]}" /></b></div>
                                    <small style="color:var(--muted);"><c:out value="${item[4]}" default="—" /></small>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty item[6]}">
                                            <span style="font-family:monospace; font-weight:700; color:var(--primary); font-size:11px;">[${item[6]}]</span>
                                            <div style="font-size:12px; color:#475569; max-width:180px;" class="truncate">${item[7]}</div>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color:var(--muted); font-size:11px;">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item[15] == 'EMERGENCY'}">
                                            <span style="background:#fee2e2; color:#b91c1c; padding:2px 8px; border-radius:8px; font-size:10px; font-weight:700;">🚨 KHẨN CẤP</span>
                                        </c:when>
                                        <c:when test="${item[15] == 'HIGH'}">
                                            <span style="background:#fef3c7; color:#b45309; padding:2px 8px; border-radius:8px; font-size:10px; font-weight:700;">⚡ ƯU TIÊN</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="background:#f1f5f9; color:#475569; padding:2px 8px; border-radius:8px; font-size:10px; font-weight:600;">THƯỜNG</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="font-size:12px; line-height:1.4; max-width:280px;">
                                    <c:out value="${item[8]}" />
                                </td>
                                <td style="font-size:12px; color:#64748b; white-space:nowrap;">
                                    ${item[12]}
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item[9] == true}">
                                            <span class="badge-read">✓ Đã đọc</span>
                                            <c:if test="${not empty item[13]}">
                                                <div style="font-size:10px; color:#15803d; margin-top:2px;">${item[13]}</div>
                                            </c:if>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge-unread">⏳ Chưa đọc</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align:right;">
                                    <c:if test="${item[9] == false}">
                                        <button type="button" class="btn" style="padding:4px 8px; font-size:11px; background:#eff6ff; color:var(--primary); border-color:#bfdbfe;" onclick="pingUnread(${item[0]}, ${item[1]}, '${item[2]}')">
                                            🔔 Ping Nhắc
                                        </button>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty auditLogs}">
                            <tr>
                                <td colspan="9" style="text-align:center; padding:30px; color:var(--muted);">
                                    Không có dữ liệu phát thanh thông báo hiện trường.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </main>
    </div>
</div>

<script>
let currentReadFilter = 'ALL';

function setAuditFilter(type, btn) {
    currentReadFilter = type;
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    applyFilters();
}

function searchAuditLogs() {
    applyFilters();
}

function applyFilters() {
    const q = document.getElementById('auditSearchInput').value.toLowerCase().trim();
    const rows = document.querySelectorAll('.audit-row');

    rows.forEach(r => {
        const isRead = r.getAttribute('data-read') === 'true';
        let matchesType = true;
        if (currentReadFilter === 'READ' && !isRead) matchesType = false;
        if (currentReadFilter === 'UNREAD' && isRead) matchesType = false;

        let matchesSearch = true;
        if (q) {
            const text = r.innerText.toLowerCase();
            if (!text.includes(q)) matchesSearch = false;
        }

        r.style.display = (matchesType && matchesSearch) ? '' : 'none';
    });
}

async function pingUnread(notificationId, recipientUserId, recipientName) {
    if (!confirm('Gửi Ping đôn đốc đọc thông báo tới đồng chí ' + recipientName + '?')) {
        return;
    }

    try {
        const resp = await fetch('${base}/api/broadcast-audits/ping', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ notificationId: notificationId, recipientUserId: recipientUserId })
        });
        const res = await resp.json();
        if (resp.ok) {
            alert(res.message || 'Đã gửi Ping đôn đốc thành công!');
            window.location.reload();
        } else {
            alert(res.error || 'Lỗi gửi Ping.');
        }
    } catch (e) {
        alert('Lỗi kết nối: ' + e.message);
    }
}
</script>
</body>
</html>
