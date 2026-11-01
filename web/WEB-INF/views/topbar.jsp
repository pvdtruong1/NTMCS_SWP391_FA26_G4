<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<header class="topbar">
    <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
    <div class="brand-desk-info">
        <strong><c:out value="${not empty param.title ? param.title : 'Hệ Thống Tòa Soạn Hội Tụ'}" /></strong>
    </div>
    <div style="margin-left: auto; display: flex; align-items: center; gap: 12px;">
        <div class="account-chip">
            <div class="avatar"><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.fullName.substring(0, 1).toUpperCase() : (not empty sessionScope.user ? sessionScope.user.fullName.substring(0, 1).toUpperCase() : 'U')}" /></div>
            <div>
                <b><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.fullName : (not empty sessionScope.user ? sessionScope.user.fullName : 'Người dùng')}" /></b>
                <small>
                    <c:choose>
                        <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'DESK_EDITOR'}">Desk Editor (BTV Chuyên Môn)</c:when>
                        <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'EXECUTIVE_EDITOR'}">Tổng Biên Tập</c:when>
                        <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'BUSINESS_ADMINISTRATOR'}">Quản Trị Nghiệp Vụ</c:when>
                        <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'SYSTEM_ADMINISTRATOR'}">Quản Trị Hệ Thống</c:when>
                        <c:when test="${(not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : sessionScope.user.roleName) == 'REPORTER'}">Phóng Viên Tác Nghiệp</c:when>
                        <c:otherwise><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.roleName : (not empty sessionScope.user ? sessionScope.user.roleName : 'Thành viên')}" /></c:otherwise>
                    </c:choose>
                </small>
            </div>
        </div>
        <a href="${base}/logout" class="btn sm danger" style="padding: 6px 14px; font-size: 12px; font-weight: 700; text-decoration: none; border-radius: 8px;">Đăng xuất</a>
    </div>
</header>
