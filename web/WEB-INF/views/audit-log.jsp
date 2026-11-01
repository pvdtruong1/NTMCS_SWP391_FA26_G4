<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nhật Ký Kiểm Toán (Audit Log) — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="audit"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Kiểm Toán &amp; Giám Sát Hệ Thống"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">GOVERNANCE &amp; AUDIT / WF-05</span>
                    <h1>Nhật Ký Kiểm Toán Hệ Thống (Audit Log)</h1>
                    <p>Truy vết toàn diện lịch sử tương tác, thẩm định và thay đổi cấu hình dữ liệu tòa soạn.</p>
                </div>
            </div>

            <div class="card" style="margin-bottom: 20px;">
                <form method="get" action="${base}/audit-log" class="filterbar" style="margin-bottom: 0;">
                    <input type="text" name="keyword" placeholder="Tìm theo người thực hiện / hành động / chi tiết..."
                           value="<c:out value='${keyword}' />" style="flex: 1; min-width: 280px;">
                    <button type="submit" class="btn primary">
                        🔍 Tìm Kiếm
                    </button>
                    <c:if test="${not empty keyword}">
                        <a href="${base}/audit-log" class="btn" style="text-decoration: none;">
                            ✕ Xóa Lọc
                        </a>
                    </c:if>
                </form>
            </div>

            <div class="card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px;">
                    <h3 style="margin: 0; font-size: 15px;">Dữ liệu nhật ký hệ thống (Hiển thị 200 bản ghi mới nhất)</h3>
                </div>

                <div class="table-scroll">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 170px;">THỜI GIAN</th>
                                <th>NGƯỜI THỰC HIỆN</th>
                                <th>HÀNH ĐỘNG</th>
                                <th>ĐỐI TƯỢNG (ENTITY)</th>
                                <th>CHI TIẾT THAY ĐỔI</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${logList}">
                                <tr>
                                    <td>
                                        <small style="color: var(--muted); font-family: monospace;">${log.createdAt}</small>
                                    </td>
                                    <td>
                                        <strong>👤 <c:out value="${log.actorName}" /></strong>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: #eef2ff; color: var(--primary); font-weight: 700;">
                                            ${log.action}
                                        </span>
                                    </td>
                                    <td>
                                        <span style="font-weight: 600;">${log.entityType}</span>
                                        <c:if test="${log.entityId != null}">
                                            <span style="color: var(--muted);">#${log.entityId}</span>
                                        </c:if>
                                    </td>
                                    <td>
                                        <span style="color: var(--text);"><c:out value="${log.details}" /></span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
