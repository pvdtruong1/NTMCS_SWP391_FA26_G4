<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="topbar">
  <button type="button" class="icon-btn mobile-menu" data-menu aria-label="Menu">☰</button>

  <button type="button" class="search" data-search-trigger>
    <span>⌕</span>
    <span>Tìm kiếm nhanh đề tài, chuyên mục, ý tưởng...</span>
    <kbd>Ctrl K</kbd>
  </button>

  <a class="btn primary sm" href="${pageContext.request.contextPath}/reporter/compose" style="display:inline-flex;align-items:center;gap:6px">
    <span>＋</span> <span>Tạo Topic</span>
  </a>

  <button type="button" class="icon-btn" data-dark-toggle title="Bật/Tắt chế độ tối">
    <span>🌓</span>
  </button>

  <button type="button" class="icon-btn notif" data-notif-trigger title="Xem thông báo mới">
    <span>🔔</span>
    <c:if test="${unreadNotifCount != null && unreadNotifCount > 0}">
      <em>${unreadNotifCount > 9 ? '9+' : unreadNotifCount}</em>
    </c:if>
  </button>

  <button type="button" class="account-chip" data-account-trigger>
    <span class="avatar">${userAvatar != null ? userAvatar : 'PV'}</span>
    <span>
      <b><c:out value="${currentUserName != null ? currentUserName : 'Mai Khánh Hùng'}"/></b>
      <small><c:out value="${currentUserRole != null ? currentUserRole : 'REPORTER'}"/> · <c:out value="${currentUserDesk != null ? currentUserDesk : 'Thời sự'}"/></small>
    </span>
    <span style="color:var(--muted);font-size:10px;margin-left:2px">⌄</span>
  </button>
</header>
<main>
