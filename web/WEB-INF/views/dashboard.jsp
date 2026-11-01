<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Executive Dashboard — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="dashboard"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Bảng Điều Hành &amp; Giám Sát Tòa Soạn"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">EXECUTIVE GOVERNANCE / WF-05</span>
                    <h1>Bảng Điều Hành Toàn Cảnh Tòa Soạn</h1>
                    <p>Tổng quan chỉ số sản xuất, tỷ lệ phê duyệt, tiến độ chỉ tiêu và cảnh báo tác nghiệp theo thời gian thực.</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/report" class="btn primary" style="text-decoration: none;">
                        📈 Xuất Báo Cáo Hiệu Suất
                    </a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <!-- Metric Stats Grid -->
            <div class="stats" style="grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));">
                <div class="stat">
                    <div class="stat-icon" style="background: #eef2ff; color: var(--primary);">📰</div>
                    <div>
                        <span>TỔNG ĐỀ TÀI</span>
                        <strong>${dash.total_topics}</strong>
                        <small style="color: var(--warning);">${dash.pending_topics} đề tài chờ duyệt</small>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat-icon" style="background: #e8f7ef; color: var(--success);">✓</div>
                    <div>
                        <span>TỶ LỆ PHÊ DUYỆT</span>
                        <strong><c:out value="${dash.approval_rate}" default="-" />%</strong>
                        <small style="color: var(--muted);">${dash.approved_topics} đã duyệt &bull; ${dash.rejected_topics} từ chối</small>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat-icon" style="background: #e8f4ff; color: #2771a8;">⏱️</div>
                    <div>
                        <span>TỶ LỆ ĐÚNG HẠN</span>
                        <strong><c:out value="${dash.on_time_rate}" default="-" />%</strong>
                        <small style="color: ${dash.overdue_count > 0 ? 'var(--danger)' : 'var(--muted)'};">
                            ${dash.finished_topics} hoàn thành &bull; ${dash.overdue_count} quá hạn
                        </small>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat-icon" style="background: #fff4dd; color: #9a6200;">👑</div>
                    <div>
                        <span>CHỜ TỔNG BIÊN TẬP</span>
                        <strong>${dash.pending_executive}</strong>
                        <small style="color: var(--muted);">Đề tài vượt cấp / rủi ro pháp lý</small>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat-icon" style="background: #f1ebff; color: #6e4cb1;">💰</div>
                    <div>
                        <span>DỰ TOÁN NGÂN SÁCH</span>
                        <strong style="font-size: 20px;"><fmt:formatNumber value="${dash.estimate_total}" groupingUsed="true" /> đ</strong>
                        <small style="color: var(--muted);">Tổng dự toán toàn bộ đề tài</small>
                    </div>
                </div>

                <div class="stat">
                    <div class="stat-icon" style="background: #fff0df; color: #a76100;">🚗</div>
                    <div>
                        <span>THIẾT BỊ &amp; XE HIỆN TRƯỜNG</span>
                        <strong>${dash.assets_checked_out} / ${dash.assets_total}</strong>
                        <small style="color: var(--muted);">${dash.assets_maintenance} đang bảo dưỡng</small>
                    </div>
                </div>
            </div>

            <!-- Desk Quota Progress Card -->
            <div class="card" style="margin-top: 16px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px;">
                    <div>
                        <h2 style="margin: 0; font-size: 16px;">Tiến Độ Hoàn Thành Chỉ Tiêu Phòng Ban (Quota)</h2>
                        <small style="color: var(--muted);">Số lượng đề tài đã duyệt có hạn chót trong kỳ <c:out value="${dash.period}" /> so với chỉ tiêu giao.</small>
                    </div>
                    <form method="get" action="${base}/dashboard" class="filterbar" style="margin-bottom: 0;">
                        <input type="text" name="period" value="<c:out value='${dash.period}' />" placeholder="2026-10 hoặc 2026-Q4" style="width: 160px; min-width: 140px;">
                        <button type="submit" class="btn sm primary">Lọc Kỳ</button>
                    </form>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                            <tr>
                                <th>PHÒNG BAN CHUYÊN MÔN</th>
                                <th style="text-align: center;">CHỈ TIÊU (QUOTA)</th>
                                <th style="text-align: center;">ĐÃ ĐẠT (ACTUAL)</th>
                                <th style="min-width: 220px;">TIẾN ĐỘ THỰC HIỆN</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="q" items="${dash.quota_progress}">
                                <tr>
                                    <td><strong>🏛️ <c:out value="${q.desk_name}" /></strong></td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${not empty q.quota_count}">
                                                <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">${q.quota_count}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--muted); font-style: italic;">Chưa giao</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;"><strong>${q.actual}</strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${q.percent == null}">-</c:when>
                                            <c:otherwise>
                                                <div style="display: flex; align-items: center; gap: 10px;">
                                                    <div style="flex: 1; height: 8px; background: var(--line); border-radius: 99px; overflow: hidden;">
                                                        <div style="height: 8px; border-radius: 99px; background: ${q.percent >= 100 ? 'var(--success)' : 'var(--primary)'}; width: ${q.percent > 100 ? 100 : q.percent}%;"></div>
                                                    </div>
                                                    <span style="font-weight: 700; min-width: 45px; text-align: right; color: ${q.percent >= 100 ? 'var(--success)' : 'var(--text)'};">${q.percent}%</span>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Double Grid: Topics by Status & Waiting Executive -->
            <div class="grid2" style="margin-top: 16px;">
                <div class="card">
                    <h2 style="margin: 0 0 14px; font-size: 15px;">Phân Bố Đề Tài Theo Trạng Thái</h2>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr><th>TRẠNG THÁI</th><th style="text-align: right;">SỐ LƯỢNG</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${dash.status_counts}">
                                    <tr>
                                        <td>
                                            <span class="badge" style="background: #f1f3f7; color: var(--text);">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td style="text-align: right;"><strong>${s.count}</strong></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card">
                    <h2 style="margin: 0 0 14px; font-size: 15px;">Hàng Đợi Trình Duyệt Tổng Biên Tập</h2>
                    <c:choose>
                        <c:when test="${empty dash.executive_queue}">
                            <div style="padding: 24px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 10px;">
                                Không có đề tài nào đang chờ Tổng Biên Tập thẩm định.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-scroll">
                                <table>
                                    <thead>
                                        <tr><th>TIÊU ĐỀ</th><th>BAN</th><th>HẠN NỘP</th></tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${dash.executive_queue}">
                                            <tr>
                                                <td><strong><c:out value="${t.title}" /></strong></td>
                                                <td><c:out value="${t.desk_name}" /></td>
                                                <td><small style="color: var(--muted);"><c:out value="${t.deadline}" default="-" /></small></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Overdue Topics Alert Table -->
            <div class="card" style="margin-top: 16px;">
                <h2 style="margin: 0 0 14px; font-size: 15px; color: ${not empty dash.overdue_topics ? 'var(--danger)' : 'var(--text)'};">
                    ⚠️ Đề Tài Quá Hạn Tác Nghiệp (Overdue Topics)
                </h2>
                <c:choose>
                    <c:when test="${empty dash.overdue_topics}">
                        <div style="padding: 20px; text-align: center; color: var(--success); background: #e8f7ef; border-radius: 10px; font-weight: 600;">
                            ✓ Tuyệt vời! Hiện tại không có đề tài nào bị quá hạn nộp bài.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-scroll">
                            <table>
                                <thead>
                                    <tr>
                                        <th>MÃ</th>
                                        <th>TIÊU ĐỀ ĐỀ TÀI</th>
                                        <th>BAN</th>
                                        <th>PHÓNG VIÊN</th>
                                        <th>TRẠNG THÁI</th>
                                        <th style="color: var(--danger);">HẠN CHÓT</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="t" items="${dash.overdue_topics}">
                                        <tr>
                                            <td><strong><c:out value="${t.topic_code}" default="-" /></strong></td>
                                            <td><strong><c:out value="${t.title}" /></strong></td>
                                            <td><c:out value="${t.desk_name}" /></td>
                                            <td>👤 <c:out value="${t.author_name}" /></td>
                                            <td><span class="badge b-pending">${t.status}</span></td>
                                            <td style="color: var(--danger); font-weight: 700;">${t.deadline}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
</body>
</html>
