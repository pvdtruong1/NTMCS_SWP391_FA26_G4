<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<header class="topbar">
    <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
    <div class="brand-desk-info">
        <strong><c:out value="${not empty param.title ? param.title : 'Hệ Thống Tòa Soạn Hội Tụ'}" /></strong>
    </div>
    <div style="display: flex; align-items: center; gap: 10px; margin-left: auto;">
        <div class="account-chip">
            <div class="avatar">👤</div>
            <div>
                <b><c:out value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser.fullName : (not empty sessionScope.user ? sessionScope.user.fullName : 'Người dùng')}" /></b>
                <small><c:out value="${not empty sessionScope.loggedUser.roleName ? sessionScope.loggedUser.roleName : (not empty sessionScope.user.roleName ? sessionScope.user.roleName : 'Member')}" /></small>
            </div>
        </div>
        <a href="${base}/logout" class="btn sm danger" style="padding: 6px 12px; font-size: 12px; font-weight: 700; text-decoration: none; border-radius: 8px;">Đăng xuất</a>
    </div>
</header>
