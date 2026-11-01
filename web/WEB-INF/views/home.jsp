<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang Chủ Điều Hành — NTMCS</title>
    <link rel="stylesheet" href="${base}/css/style.css">
</head>
<body>
<div class="app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="home"/>
    </jsp:include>
    <div class="main">
        <jsp:include page="topbar.jsp">
            <jsp:param name="title" value="Trung Tâm Điều Hành Tòa Soạn"/>
        </jsp:include>
        <main>
            <div class="page-head">
                <div>
                    <span class="eyebrow">NEWSROOM MANAGEMENT &amp; COORDINATION</span>
                    <h1>Xin chào, <c:out value="${sessionScope.loggedUser.fullName}" /></h1>
                    <p>
                        Vai trò hệ thống: <b><c:out value="${sessionScope.loggedUser.roleName}" /></b>
                        <c:if test="${not empty sessionScope.loggedUser.deskName}">
                            &bull; Phòng ban chuyên môn: <b><c:out value="${sessionScope.loggedUser.deskName}" /></b>
                        </c:if>
                    </p>
                </div>
            </div>

            <div class="card" style="margin-bottom: 24px;">
                <h2 style="font-size: 16px; margin: 0 0 16px;">Danh mục các phân hệ nghiệp vụ được cấp phép</h2>
                <c:choose>
                    <c:when test="${empty screens}">
                        <div style="padding: 30px; text-align: center; color: var(--muted); background: var(--surface2); border-radius: 12px; border: 1px dashed var(--line);">
                            <p style="font-size: 14px; margin: 0 0 6px;">Tài khoản của bạn hiện chưa được phân quyền truy cập chức năng nào.</p>
                            <small>Vui lòng liên hệ Quản trị viên (Cường) để thiết lập phân quyền RBAC phù hợp.</small>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px;">
                            <c:forEach var="s" items="${screens}">
                                <a href="${base}${s[1]}" style="display: flex; align-items: center; justify-content: space-between; padding: 18px 20px; background: var(--surface2); border: 1px solid var(--line); border-radius: 12px; color: var(--text); text-decoration: none; transition: all .15s ease;" onmouseover="this.style.borderColor='var(--primary)'; this.style.transform='translateY(-2px)'" onmouseout="this.style.borderColor='var(--line)'; this.style.transform='none'">
                                    <span style="font-weight: 600; font-size: 14px;">${s[0]}</span>
                                    <span style="color: var(--primary); font-size: 18px; font-weight: 800;">&rarr;</span>
                                </a>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
</body>
</html>
