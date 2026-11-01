<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chỉ Tiêu Phòng Ban (Desk Quota) — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="quota"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Quản Lý Chỉ Tiêu Sản Xuất (Quota)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">PRODUCTION PLANNING / WF-05</span>
                    <h1>Chỉ Tiêu Sản Xuất Phòng Ban (Desk Quotas)</h1>
                    <p>Thiết lập và phân bổ định mức số lượng đề tài cần xuất bản cho từng ban chuyên môn theo từng kỳ (Tháng/Quý).</p>
                </div>
                <div class="page-actions">
                    <a href="${base}/quota?action=new" class="btn primary" style="text-decoration: none;">
                        + Giao Chỉ Tiêu Mới
                    </a>
                </div>
            </div>

            <!-- Notifications -->
            <c:if test="${param.msg == 'created'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã thiết lập chỉ tiêu mới thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'updated'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã cập nhật chỉ tiêu phòng ban thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'deleted'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã xóa bản ghi chỉ tiêu thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'nochange'}">
                <div style="background: #fff4dd; color: #9a6200; border: 1px solid #f4ddb2; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ℹ Không có thay đổi nào được ghi nhận.
                </div>
            </c:if>

            <div class="card">
                <c:choose>
                    <c:when test="${empty quotaList}">
                        <div style="padding: 30px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 12px;">
                            Chưa có dữ liệu chỉ tiêu nào được thiết lập. Hãy bấm "+ Giao Chỉ Tiêu Mới" để bắt đầu.
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-scroll">
                            <table>
                                <thead>
                                    <tr>
                                        <th>PHÒNG BAN (DESK)</th>
                                        <th>KỲ SẢN XUẤT (PERIOD)</th>
                                        <th style="text-align: center;">CHỈ TIÊU (ĐỀ TÀI)</th>
                                        <th style="text-align: right;">THAO TÁC</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="q" items="${quotaList}">
                                        <tr>
                                            <td><strong>🏛️ <c:out value="${q.deskName}" /></strong></td>
                                            <td>
                                                <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">
                                                    📅 <c:out value="${q.period}" />
                                                </span>
                                            </td>
                                            <td style="text-align: center;">
                                                <strong style="font-size: 16px;">${q.quotaCount}</strong> <span style="color: var(--muted); font-size: 12px;">bài</span>
                                            </td>
                                            <td style="text-align: right;">
                                                <div style="display: inline-flex; gap: 6px;">
                                                    <a href="${base}/quota?action=edit&id=${q.quotaId}" class="btn sm" style="text-decoration: none;">
                                                        ✏️ Sửa
                                                    </a>
                                                    <form method="post" action="${base}/quota" style="display:inline; margin: 0;"
                                                          onsubmit="return confirm('Bạn có chắc chắn muốn xóa chỉ tiêu này?');">
                                                        <input type="hidden" name="action" value="delete">
                                                        <input type="hidden" name="id" value="${q.quotaId}">
                                                        <button type="submit" class="btn sm danger">
                                                            🗑️ Xóa
                                                        </button>
                                                    </form>
                                                </div>
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
