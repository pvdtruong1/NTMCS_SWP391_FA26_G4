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
            <jsp:param name="title" value="Bảng Điều Hành & Giám Sát Tòa Soạn"/>
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
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <h2 style="margin: 0; font-size: 16px; font-weight: 700;">Tiến Độ Hoàn Thành Chỉ Tiêu Phòng Ban (Quota)</h2>
                            <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">Kỳ ${dash.period}</span>
                        </div>
                        <small style="color: var(--muted); margin-top: 3px; display: block;">Số lượng đề tài đã duyệt có hạn chót trong kỳ <c:out value="${dash.period}" /> so với chỉ tiêu giao.</small>
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
                                <th style="width: 32%;">PHÒNG BAN CHUYÊN MÔN</th>
                                <th style="width: 18%; text-align: center;">CHỈ TIÊU (QUOTA)</th>
                                <th style="width: 18%; text-align: center;">ĐÃ ĐẠT (ACTUAL)</th>
                                <th style="width: 32%; min-width: 200px;">TIẾN ĐỘ THỰC HIỆN</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="q" items="${dash.quota_progress}">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 8px;">
                                            <span style="font-size: 16px;">🏛️</span>
                                            <strong style="color: var(--text);"><c:out value="${q.desk_name}" /></strong>
                                        </div>
                                    </td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${not empty q.quota_count}">
                                                <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700; font-size: 12px; padding: 4px 10px;">${q.quota_count}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: var(--surface2); color: var(--muted); font-style: italic;">Chưa giao</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="badge" style="background: #ecfdf5; color: #047857; font-weight: 700; font-size: 12px; padding: 4px 10px;">${q.actual}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${q.percent == null}">
                                                <span style="color: var(--muted);">-</span>
                                            </c:when>
                                            <c:otherwise>
                                                <c:set var="barColor" value="${q.percent >= 100 ? '#10b981' : (q.percent >= 60 ? '#3458d4' : '#f59e0b')}" />
                                                <div style="display: flex; align-items: center; gap: 12px;">
                                                    <div style="flex: 1; height: 8px; background: #e2e8f0; border-radius: 99px; overflow: hidden;">
                                                        <div style="height: 100%; border-radius: 99px; background: ${barColor}; width: ${q.percent > 100 ? 100 : q.percent}%; transition: width 0.3s ease;"></div>
                                                    </div>
                                                    <span style="font-weight: 700; min-width: 48px; text-align: right; color: ${barColor}; font-size: 12.5px;">${q.percent}%</span>
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
                <!-- Card 1: Status Distribution -->
                <div class="card" style="display: flex; flex-direction: column;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="font-size: 16px;">📊</span>
                            <h2 style="margin: 0; font-size: 15px; font-weight: 700;">Phân Bố Đề Tài Theo Trạng Thái</h2>
                        </div>
                        <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">${dash.total_topics} đề tài</span>
                    </div>

                    <div class="table-scroll">
                        <table class="table-compact" style="width: 100%; min-width: 100%;">
                            <thead>
                                <tr>
                                    <th style="width: 58%;">TRẠNG THÁI</th>
                                    <th style="width: 42%; text-align: right;">SỐ LƯỢNG &amp; TỶ LỆ</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="s" items="${dash.status_counts}">
                                    <c:choose>
                                        <c:when test="${s.status eq 'SUBMITTED'}">
                                            <c:set var="statusLabel" value="Chờ BTV duyệt" />
                                            <c:set var="statusCls" value="b-submitted" />
                                            <c:set var="statusDot" value="#b45309" />
                                        </c:when>
                                        <c:when test="${s.status eq 'REVISION_REQUESTED'}">
                                            <c:set var="statusLabel" value="Yêu cầu sửa đổi" />
                                            <c:set var="statusCls" value="b-revision_requested" />
                                            <c:set var="statusDot" value="#7e22ce" />
                                        </c:when>
                                        <c:when test="${s.status eq 'PENDING_EXECUTIVE_APPROVAL'}">
                                            <c:set var="statusLabel" value="Chờ TBT phê duyệt" />
                                            <c:set var="statusCls" value="b-pending_executive_approval" />
                                            <c:set var="statusDot" value="#c2410c" />
                                        </c:when>
                                        <c:when test="${s.status eq 'APPROVED'}">
                                            <c:set var="statusLabel" value="Đã phê duyệt" />
                                            <c:set var="statusCls" value="b-approved" />
                                            <c:set var="statusDot" value="#15803d" />
                                        </c:when>
                                        <c:when test="${s.status eq 'IN_PROGRESS'}">
                                            <c:set var="statusLabel" value="Đang tác nghiệp" />
                                            <c:set var="statusCls" value="b-in_progress" />
                                            <c:set var="statusDot" value="#1d4ed8" />
                                        </c:when>
                                        <c:when test="${s.status eq 'DELIVERED'}">
                                            <c:set var="statusLabel" value="Đã nộp bản thảo" />
                                            <c:set var="statusCls" value="b-delivered" />
                                            <c:set var="statusDot" value="#0f766e" />
                                        </c:when>
                                        <c:when test="${s.status eq 'ACCEPTED_READY_FOR_CMS'}">
                                            <c:set var="statusLabel" value="Sẵn sàng xuất bản" />
                                            <c:set var="statusCls" value="b-accepted_ready_for_cms" />
                                            <c:set var="statusDot" value="#047857" />
                                        </c:when>
                                        <c:when test="${s.status eq 'REJECTED'}">
                                            <c:set var="statusLabel" value="Từ chối đề tài" />
                                            <c:set var="statusCls" value="b-rejected" />
                                            <c:set var="statusDot" value="#b91c1c" />
                                        </c:when>
                                        <c:when test="${s.status eq 'ABORTED'}">
                                            <c:set var="statusLabel" value="Hủy tác nghiệp" />
                                            <c:set var="statusCls" value="b-aborted" />
                                            <c:set var="statusDot" value="#9f1239" />
                                        </c:when>
                                        <c:when test="${s.status eq 'DRAFT'}">
                                            <c:set var="statusLabel" value="Bản nháp" />
                                            <c:set var="statusCls" value="b-draft" />
                                            <c:set var="statusDot" value="#64748b" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="statusLabel" value="${s.status}" />
                                            <c:set var="statusCls" value="b-draft" />
                                            <c:set var="statusDot" value="#64748b" />
                                        </c:otherwise>
                                    </c:choose>
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 8px;">
                                                <span class="badge ${statusCls}">
                                                    ${statusLabel}
                                                </span>
                                                <small style="color: var(--muted); font-size: 10px; font-family: monospace;">(${s.status})</small>
                                            </div>
                                        </td>
                                        <td style="text-align: right;">
                                            <c:set var="ratio" value="${dash.total_topics > 0 ? (s.count * 100 / dash.total_topics) : 0}" />
                                            <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px;">
                                                <div style="width: 55px; height: 6px; background: #e2e8f0; border-radius: 99px; overflow: hidden;">
                                                    <div style="height: 100%; border-radius: 99px; background: ${statusDot}; width: ${ratio}%;"></div>
                                                </div>
                                                <span class="badge" style="min-width: 32px; text-align: center; font-weight: 800; background: ${s.count > 0 ? '#eef2ff' : 'var(--surface2)'}; color: ${s.count > 0 ? 'var(--primary)' : 'var(--muted)'};">
                                                    ${s.count}
                                                </span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Card 2: Executive Approval Queue -->
                <div class="card" style="display: flex; flex-direction: column;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                        <div style="display: flex; align-items: center; gap: 8px;">
                            <span style="font-size: 16px;">👑</span>
                            <h2 style="margin: 0; font-size: 15px; font-weight: 700;">Hàng Đợi Trình Duyệt Tổng Biên Tập</h2>
                        </div>
                        <c:choose>
                            <c:when test="${not empty dash.executive_queue}">
                                <span class="badge b-pending_executive_approval" style="font-weight: 700;">${dash.executive_queue.size()} đề tài chờ duyệt</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge b-approved" style="font-weight: 700;">Đã xử lý hết</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:choose>
                        <c:when test="${empty dash.executive_queue}">
                            <div style="padding: 36px 20px; text-align: center; color: var(--muted); background: var(--surface2); border: 1px dashed var(--line); border-radius: 12px; margin: auto 0;">
                                <div style="font-size: 32px; margin-bottom: 10px;">✨</div>
                                <strong style="display: block; color: var(--text); margin-bottom: 4px; font-size: 14px;">Không có đề tài chờ thẩm định</strong>
                                <span style="font-size: 12px;">Tất cả các đề tài trình duyệt cấp cao đều đã được xử lý.</span>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-scroll">
                                <table class="table-compact" style="width: 100%; min-width: 100%;">
                                    <thead>
                                        <tr>
                                            <th style="width: 50%;">ĐỀ TÀI &amp; TÁC GIẢ</th>
                                            <th style="width: 25%;">BAN CHUYÊN MÔN</th>
                                            <th style="width: 25%; text-align: right;">HẠN NỘP</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${dash.executive_queue}">
                                            <tr>
                                                <td>
                                                    <div style="display: flex; flex-direction: column; gap: 5px;">
                                                        <a href="${base}/reporter/detail?id=${t.topic_id}" style="color: var(--text); font-weight: 700; line-height: 1.4; text-decoration: none;" title="<c:out value='${t.title}' />">
                                                            <c:out value="${t.title}" />
                                                        </a>
                                                        <div style="display: flex; align-items: center; gap: 8px; flex-wrap: wrap;">
                                                            <span class="badge" style="background: #eef2ff; color: var(--primary); font-size: 10px; font-weight: 700; padding: 2px 6px; font-family: monospace;">
                                                                <c:out value="${not empty t.topic_code ? t.topic_code : ('#' += t.topic_id)}" />
                                                            </span>
                                                            <span style="color: var(--muted); font-size: 11.5px; display: inline-flex; align-items: center; gap: 4px;">
                                                                👤 Phóng viên: <strong style="color: var(--text);"><c:out value="${t.author_name}" default="Chưa rõ" /></strong>
                                                            </span>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <span class="badge" style="background: var(--surface2); color: var(--text); border: 1px solid var(--line); font-weight: 600;">
                                                        🏛️ <c:out value="${t.desk_name}" />
                                                    </span>
                                                </td>
                                                <td style="text-align: right;">
                                                    <span style="white-space: nowrap; font-size: 12px; font-weight: 600; color: var(--muted);">
                                                        📅 <c:out value="${t.deadline}" default="-" />
                                                    </span>
                                                </td>
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
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; flex-wrap: wrap; gap: 10px;">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <span style="font-size: 18px;">⚠️</span>
                        <h2 style="margin: 0; font-size: 15px; font-weight: 700; color: ${not empty dash.overdue_topics ? 'var(--danger)' : 'var(--text)'};">
                            Đề Tài Quá Hạn Tác Nghiệp (Overdue Topics)
                        </h2>
                    </div>
                    <c:if test="${not empty dash.overdue_topics}">
                        <span class="badge b-rejected" style="font-weight: 700; font-size: 11px;">
                            ${dash.overdue_topics.size()} đề tài trễ hạn
                        </span>
                    </c:if>
                </div>

                <c:choose>
                    <c:when test="${empty dash.overdue_topics}">
                        <div style="padding: 22px; text-align: center; color: var(--success); background: #e8f7ef; border: 1px solid #bbf7d0; border-radius: 12px; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <span style="font-size: 18px;">✓</span>
                            <span>Tuyệt vời! Hiện tại không có đề tài nào bị quá hạn nộp bài trong tòa soạn.</span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-scroll">
                            <table>
                                <thead>
                                    <tr>
                                        <th style="width: 12%;">MÃ ĐỀ TÀI</th>
                                        <th style="width: 38%;">TIÊU ĐỀ ĐỀ TÀI</th>
                                        <th style="width: 15%;">BAN CHUYÊN MÔN</th>
                                        <th style="width: 15%;">PHÓNG VIÊN</th>
                                        <th style="width: 10%;">TRẠNG THÁI</th>
                                        <th style="width: 10%; color: var(--danger); text-align: right;">HẠN CHÓT</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="t" items="${dash.overdue_topics}">
                                        <tr>
                                            <td>
                                                <span class="badge" style="font-family: monospace; font-weight: 700; background: #fee2e2; color: #991b1b; border: 1px solid #fecaca;">
                                                    <c:out value="${t.topic_code}" default="-" />
                                                </span>
                                            </td>
                                            <td>
                                                <a href="${base}/reporter/detail?id=${t.topic_id}" style="color: var(--text); font-weight: 700; text-decoration: none;" title="<c:out value='${t.title}' />">
                                                    <c:out value="${t.title}" />
                                                </a>
                                            </td>
                                            <td>
                                                <span class="badge" style="background: var(--surface2); color: var(--text); border: 1px solid var(--line);">
                                                    🏛️ <c:out value="${t.desk_name}" />
                                                </span>
                                            </td>
                                            <td>
                                                <span style="font-weight: 600; color: var(--text);">
                                                    👤 <c:out value="${t.author_name}" />
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge b-${t.status.toLowerCase()}">
                                                    ${t.status}
                                                </span>
                                            </td>
                                            <td style="text-align: right;">
                                                <span style="color: var(--danger); font-weight: 700; white-space: nowrap; display: inline-flex; align-items: center; gap: 4px;">
                                                    ⏰ ${t.deadline}
                                                </span>
                                            </td>
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
