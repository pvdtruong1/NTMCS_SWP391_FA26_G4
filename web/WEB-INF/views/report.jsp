<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo Cáo Hiệu Suất — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="report"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Báo Cáo &amp; Phân Tích Hiệu Suất"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">ANALYTICS &amp; REPORTING / WF-05</span>
                    <h1>Trình Tạo Báo Cáo Hiệu Suất (Performance Report Builder)</h1>
                    <p>Tùy biến nhóm dữ liệu, khoảng thời gian hạn chót và các cột chỉ số cần thống kê rồi kết xuất báo cáo đa định dạng.</p>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-weight: 600;">
                    ✕ <c:out value="${error}" />
                </div>
            </c:if>

            <!-- Report Filter Configuration Card -->
            <div class="card" style="margin-bottom: 24px;">
                <h2 style="font-size: 15px; margin: 0 0 16px;">Cấu Hình Tiêu Chí Lọc Báo Cáo</h2>
                <form method="get" action="${base}/report">
                    <div class="form-grid">
                        <div class="field">
                            <span>NHÓM DỮ LIỆU THEO (GROUP BY)</span>
                            <select name="group">
                                <c:forEach var="g" items="${groups}">
                                    <option value="${g[0]}" ${g[0] == report.groupBy ? 'selected' : ''}>${g[1]}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="field">
                            <span>BAN CHUYÊN MÔN (DESK)</span>
                            <select name="desk_id">
                                <option value="0">-- Toàn bộ các ban --</option>
                                <c:forEach var="d" items="${deskList}">
                                    <option value="${d.deskId}" ${d.deskId == report.deskId ? 'selected' : ''}>🏛️ <c:out value="${d.deskName}" /></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="field">
                            <span>HẠN CHÓT TỪ NGÀY</span>
                            <input type="date" name="from" value="<c:out value='${report.fromText}' />">
                        </div>

                        <div class="field">
                            <span>HẠN CHÓT ĐẾN NGÀY</span>
                            <input type="date" name="to" value="<c:out value='${report.toText}' />">
                        </div>

                        <div class="field full">
                            <span>CHỈ SỐ BÁO CÁO CẦN XUẤT (COLUMNS)</span>
                            <div style="display: flex; flex-wrap: wrap; gap: 14px; padding: 12px; background: var(--surface2); border: 1px solid var(--line); border-radius: 10px; margin-top: 4px;">
                                <c:forEach var="m" items="${metrics}">
                                    <label style="display: inline-flex; align-items: center; gap: 8px; font-size: 13px; font-weight: 500; cursor: pointer;">
                                        <input type="checkbox" name="metric" value="${m[0]}" id="m_${m[0]}"
                                               style="width: 17px; height: 17px; cursor: pointer;"
                                               ${report.metrics.contains(m[0]) ? 'checked' : ''}>
                                        ${m[1]}
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <div style="display: flex; gap: 10px; margin-top: 24px; padding-top: 18px; border-top: 1px solid var(--line); flex-wrap: wrap;">
                        <button type="submit" class="btn primary">
                            📊 Tạo Báo Cáo
                        </button>
                        <button type="submit" name="format" value="xlsx" class="btn success">
                            📥 Xuất File Excel (.xlsx)
                        </button>
                        <button type="submit" name="format" value="print" formtarget="_blank" class="btn">
                            🖨️ In / Xuất PDF
                        </button>
                        <a href="${base}/report" class="btn" style="text-decoration: none;">
                            ✕ Đặt lại
                        </a>
                    </div>
                </form>
            </div>

            <!-- Report Results Card -->
            <c:if test="${not empty result}">
                <div class="card">
                    <div style="margin-bottom: 16px;">
                        <h2 style="font-size: 16px; margin: 0 0 4px;">Kết Quả Báo Cáo Hiệu Suất</h2>
                        <small style="color: var(--muted);"><c:out value="${result.description}" />. (Lưu ý: Đề tài ở trạng thái Bản nháp DRAFT không tính vào báo cáo).</small>
                    </div>

                    <c:choose>
                        <c:when test="${empty result.rows}">
                            <div style="padding: 30px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 10px;">
                                Không tìm thấy dữ liệu đề tài nào phù hợp với bộ lọc đã chọn.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-scroll">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>${report.groupLabel}</th>
                                            <c:forEach var="col" items="${report.metricColumns}">
                                                <th style="text-align: right;">${col[1]}</th>
                                            </c:forEach>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${result.rows}">
                                            <tr>
                                                <td><strong><c:out value="${row.group_label}" /></strong></td>
                                                <c:forEach var="col" items="${report.metricColumns}">
                                                    <td style="text-align: right;"><c:out value="${row[col[0]]}" default="-" /></td>
                                                </c:forEach>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                    <tfoot>
                                        <tr style="background: var(--surface2); font-weight: 800;">
                                            <td>TỔNG CỘNG</td>
                                            <c:forEach var="col" items="${report.metricColumns}">
                                                <td style="text-align: right;"><c:out value="${result.total[col[0]]}" default="-" /></td>
                                            </c:forEach>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>
        </main>
    </div>
</div>
</body>
</html>
