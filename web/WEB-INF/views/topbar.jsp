<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<header class="topbar">
    <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
    <div class="brand-desk-info">
        <strong><c:out value="${not empty param.title ? param.title : 'Hệ Thống Tòa Soạn Hội Tụ'}" /></strong>
    </div>
    <div style="margin-left: auto; display: flex; align-items: center; gap: 12px;">

        <!-- Notification Bell & Dropdown -->
        <div style="position: relative;">
            <button class="icon-btn notif" id="topbarNotifBtn" type="button" title="Thông báo hệ thống" onclick="toggleTopbarNotif()" style="display:flex;align-items:center;justify-content:center;cursor:pointer;position:relative;background:var(--surface);border:1px solid var(--line);border-radius:10px;width:40px;height:40px;font-size:18px;">
                🔔
                <em id="topbarNotifBadge" style="display:none;position:absolute;top:-4px;right:-3px;background:var(--danger);color:#fff;border-radius:99px;font-style:normal;font-size:10px;min-width:17px;padding:2px 4px;font-weight:700;line-height:1;text-align:center;">0</em>
            </button>
            <div id="topbarNotifDropdown" style="display:none; position:absolute; right:0; top:48px; width:360px; background:#fff; border:1px solid var(--line); border-radius:12px; box-shadow:0 12px 35px rgba(21,34,61,.18); z-index:1000; overflow:hidden;">
                <div style="padding:12px 16px; border-bottom:1px solid var(--line); display:flex; justify-content:space-between; align-items:center; background:var(--surface2);">
                    <div style="font-weight:700; font-size:13px; color:var(--text); display:flex; align-items:center; gap:6px;">
                        <span>🔔 Thông Báo Của Bạn</span>
                        <span id="topbarUnreadTag" class="badge b-pending" style="display:none; font-size:10px;">0 mới</span>
                    </div>
                    <button type="button" onclick="markAllTopNotifsRead()" style="background:none; border:none; color:var(--primary); font-size:11px; font-weight:700; cursor:pointer;">Đã đọc tất cả</button>
                </div>
                <div id="topbarNotifList" style="max-height:360px; overflow-y:auto; padding:6px;">
                    <div style="padding:20px; text-align:center; color:var(--muted); font-size:12px;">Đang tải thông báo...</div>
                </div>
            </div>
        </div>

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

<script>
(function() {
    const base = '${base}';

    window.toggleTopbarNotif = function() {
        const dd = document.getElementById('topbarNotifDropdown');
        if (!dd) return;
        const isShown = dd.style.display === 'block';
        dd.style.display = isShown ? 'none' : 'block';
        if (!isShown) {
            loadTopNotifs();
        }
    };

    window.loadTopNotifs = function() {
        fetch(base + '/api-notifications.jsp')
            .then(r => r.json())
            .then(data => {
                const badge = document.getElementById('topbarNotifBadge');
                const tag = document.getElementById('topbarUnreadTag');
                const list = document.getElementById('topbarNotifList');
                if (badge) {
                    if (data.unreadCount > 0) {
                        badge.innerText = data.unreadCount > 99 ? '99+' : data.unreadCount;
                        badge.style.display = 'inline-block';
                        if (tag) {
                            tag.innerText = data.unreadCount + ' mới';
                            tag.style.display = 'inline-block';
                        }
                    } else {
                        badge.style.display = 'none';
                        if (tag) tag.style.display = 'none';
                    }
                }
                if (list) {
                    if (!data.notifications || data.notifications.length === 0) {
                        list.innerHTML = '<div style="padding:24px 16px;text-align:center;color:var(--muted);font-size:12px;">Chưa có thông báo nào.</div>';
                    } else {
                        list.innerHTML = data.notifications.map(n => {
                            const isUnread = !n.isRead;
                            const topicLink = n.topicId ? (base + '/reporter/requests?topicId=' + n.topicId) : '#';
                            return '<div style="padding:10px 12px;border-bottom:1px solid var(--line);border-radius:8px;margin-bottom:4px;background:' + (isUnread ? '#f0f5ff' : '#fff') + ';font-size:12px;line-height:1.4;">'
                                 + '<div style="display:flex;justify-content:space-between;gap:8px;align-items:flex-start;">'
                                 + '<div style="flex:1;color:var(--text);font-weight:' + (isUnread ? '600' : '400') + ';">' + escapeHtml(n.message) + '</div>'
                                 + (isUnread ? '<span style="width:8px;height:8px;border-radius:50%;background:var(--primary);flex-shrink:0;margin-top:4px;"></span>' : '')
                                 + '</div>'
                                 + (n.topicId ? '<div style="margin-top:6px;"><a href="' + topicLink + '" style="color:var(--primary);font-size:11px;font-weight:700;">Xem chi tiết đề xuất &rarr;</a></div>' : '')
                                 + '</div>';
                        }).join('');
                    }
                }
            })
            .catch(err => {
                const list = document.getElementById('topbarNotifList');
                if (list) list.innerHTML = '<div style="padding:16px;text-align:center;color:var(--danger);font-size:12px;">Lỗi tải thông báo</div>';
            });
    };

    window.markAllTopNotifsRead = function() {
        fetch(base + '/api-notifications.jsp?action=markAllRead', { method: 'POST' })
            .then(r => r.json())
            .then(res => {
                const badge = document.getElementById('topbarNotifBadge');
                const tag = document.getElementById('topbarUnreadTag');
                if (badge) badge.style.display = 'none';
                if (tag) tag.style.display = 'none';
                loadTopNotifs();
            });
    };

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadTopNotifs);
    } else {
        loadTopNotifs();
    }

    document.addEventListener('click', function(e) {
        const btn = document.getElementById('topbarNotifBtn');
        const dd = document.getElementById('topbarNotifDropdown');
        if (btn && dd && !btn.contains(e.target) && !dd.contains(e.target)) {
            dd.style.display = 'none';
        }
    });
})();
</script>
