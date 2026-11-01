<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ma Trận Quyền (RBAC) — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="rbac"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Quản Trị Phân Quyền Vai Trò (RBAC)"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">GOVERNANCE &amp; SECURITY / WF-05</span>
                    <h1>Ma Trận Phân Quyền Vai Trò (RBAC Matrix)</h1>
                    <p>Cấu hình quyền truy cập và kiểm soát thao tác chi tiết theo ma trận Vai trò &mdash; Đặc quyền.</p>
                </div>
            </div>

            <c:if test="${param.msg == 'saved'}">
                <div style="background: #e8f7ef; color: #15734f; border: 1px solid #ccebdd; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✓ Đã cập nhật và lưu ma trận phân quyền thành công!
                </div>
            </c:if>
            <c:if test="${param.msg == 'failed'}">
                <div style="background: #ffedf0; color: #bd3344; border: 1px solid #f0c8ce; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; font-weight: 600;">
                    ✕ Không thể lưu thay đổi. Vui lòng kiểm tra lại kết nối.
                </div>
            </c:if>

            <div class="card">
                <p style="margin-top: 0; color: var(--muted); font-size: 13px;">
                    ℹ️ Tích chọn vào ô tương ứng để cấp quyền cho vai trò truy cập phân hệ. Cột <b>SYSTEM_ADMINISTRATOR</b> được khóa cố định nhằm đảm bảo tính sẵn sàng của hệ thống.
                </p>

                <form method="post" action="${base}/rbac">
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th style="min-width: 280px;">QUYỀN HẠN (PERMISSION)</th>
                                    <c:forEach var="r" items="${roleList}">
                                        <th style="text-align: center; min-width: 110px;">${r.roleName}</th>
                                    </c:forEach>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${permissionList}">
                                    <tr>
                                        <td>
                                            <strong style="color: var(--primary);">${p.permissionCode}</strong>
                                            <small style="display: block; color: var(--muted); margin-top: 3px;"><c:out value="${p.description}" /></small>
                                        </td>
                                        <c:forEach var="r" items="${roleList}">
                                            <c:set var="key" value="${r.roleId}-${p.permissionId}" />
                                            <td style="text-align: center;">
                                                <input type="checkbox" name="rp" value="${key}"
                                                       style="width: 18px; height: 18px; cursor: pointer;"
                                                       ${pairs.contains(key) ? 'checked' : ''}
                                                       ${r.roleName == 'SYSTEM_ADMINISTRATOR' ? 'disabled' : ''}>
                                            </td>
                                        </c:forEach>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
                        <button type="submit" class="btn primary">
                            💾 Lưu Thiết Lập Ma Trận Phân Quyền
                        </button>
                    </div>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
