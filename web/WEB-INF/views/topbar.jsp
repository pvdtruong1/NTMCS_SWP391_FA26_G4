<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="base" value="${pageContext.request.contextPath}" />
<c:set var="topUser" value="${not empty sessionScope.loggedUser ? sessionScope.loggedUser : (not empty sessionScope.user ? sessionScope.user : requestScope.currentUser)}" />
<header class="topbar">
    <button class="icon-btn mobile-menu" onclick="document.getElementById('sidebar').classList.toggle('open')">☰</button>
    <div class="brand-desk-info">
        <strong><c:out value="${not empty param.title ? param.title : (not empty topUser.deskName ? topUser.deskName : 'Hệ Thống Tòa Soạn Hội Tụ')}" /></strong>
    </div>
    <div style="margin-left: auto; display: flex; align-items: center; gap: 12px;">
        <!-- Notification Bell Button -->
        <button type="button" class="icon-btn notif" id="notifTriggerBtn" onclick="openNotificationsModal()" title="Xem thông báo mới" style="cursor: pointer; display: flex; align-items: center; justify-content: center; position: relative;">
            <span style="font-size: 16px;">🔔</span>
            <em id="notifBadge" style="display: none;">0</em>
        </button>

        <div class="account-chip">
            <div class="avatar"><c:out value="${not empty topUser ? topUser.fullName.substring(0, 1).toUpperCase() : 'U'}" /></div>
            <div>
                <b><c:out value="${not empty topUser ? topUser.fullName : 'Người dùng'}" /></b>
                <small>
                    <c:choose>
                        <c:when test="${topUser.roleName == 'DESK_EDITOR'}">Desk Editor (BTV Chuyên Môn)</c:when>
                        <c:when test="${topUser.roleName == 'EXECUTIVE_EDITOR'}">Tổng Biên Tập</c:when>
                        <c:when test="${topUser.roleName == 'BUSINESS_ADMINISTRATOR'}">Quản Trị Nghiệp Vụ</c:when>
                        <c:when test="${topUser.roleName == 'SYSTEM_ADMINISTRATOR'}">Quản Trị Hệ Thống</c:when>
                        <c:when test="${topUser.roleName == 'REPORTER'}">Phóng Viên Tác Nghiệp</c:when>
                        <c:otherwise><c:out value="${not empty topUser.roleName ? topUser.roleName : 'Thành viên'}" /></c:otherwise>
                    </c:choose>
                </small>
            </div>
        </div>
        <a href="${base}/logout" class="btn sm danger" style="padding: 6px 14px; font-size: 12px; font-weight: 700; text-decoration: none; border-radius: 8px;">Đăng xuất</a>
    </div>
</header>

<!-- Notification Center Modal (In-App Notifications) -->
<div id="notifModalBackdrop" class="modal-backdrop" style="display: none;" onclick="handleNotifBackdropClick(event)">
    <div class="modal notif-modal" onclick="event.stopPropagation()">
        <div class="modal-head notif-modal-header" style="padding: 16px 20px;">
            <div class="notif-tabs">
                <button type="button" class="notif-tab-btn active" id="notifTabAll" onclick="switchNotifTab('all')">Tất cả (0)</button>
                <button type="button" class="notif-tab-btn" id="notifTabUnread" onclick="switchNotifTab('unread')">Chưa đọc (0)</button>
            </div>
            <button type="button" class="notif-mark-all-btn" id="notifMarkAllBtn" onclick="markAllNotificationsRead()" title="Đánh dấu tất cả thông báo là đã đọc">
                <span>✔</span> <span>Đánh dấu đã đọc tất cả</span>
            </button>
        </div>

        <div class="modal-body" id="notifListContainer" style="max-height: 440px; overflow-y: auto; padding: 18px 20px;">
            <div style="text-align: center; padding: 36px 16px; color: var(--muted, #64748b);">
                <div style="font-size: 24px; margin-bottom: 8px;">⏳</div>
                <div>Đang tải thông báo từ hệ thống...</div>
            </div>
        </div>

        <div class="modal-foot" style="padding: 12px 20px; display: flex; justify-content: flex-end;">
            <button type="button" class="btn" onclick="closeNotificationsModal()" style="background: #f1f5f9; color: #1e293b; border: 1px solid #cbd5e1; border-radius: 8px; padding: 7px 20px; font-weight: 600;">Đóng</button>
        </div>
    </div>
</div>

<script>
(function() {
    let currentTab = 'all';
    let notifState = { unreadCount: 0, totalCount: 0, notifications: [] };
    const apiEndpoint = '${base}/api/notifications';

    function updateBadge(count) {
        const badge = document.getElementById('notifBadge');
        if (!badge) return;
        if (!count || count <= 0) {
            badge.style.display = 'none';
            badge.textContent = '0';
        } else {
            badge.style.display = 'block';
            badge.textContent = count > 9 ? '9+' : String(count);
        }
    }

    function renderNotifications() {
        const container = document.getElementById('notifListContainer');
        const tabAll = document.getElementById('notifTabAll');
        const tabUnread = document.getElementById('notifTabUnread');
        if (!container || !tabAll || !tabUnread) return;

        tabAll.textContent = 'Tất cả (' + notifState.totalCount + ')';
        tabUnread.textContent = 'Chưa đọc (' + notifState.unreadCount + ')';

        if (currentTab === 'all') {
            tabAll.classList.add('active');
            tabUnread.classList.remove('active');
        } else {
            tabAll.classList.remove('active');
            tabUnread.classList.add('active');
        }

        const items = currentTab === 'all'
            ? notifState.notifications
            : notifState.notifications.filter(n => !n.isRead);

        if (items.length === 0) {
            container.innerHTML =
                '<div style="text-align: center; padding: 42px 16px; color: var(--muted, #64748b);">' +
                '<div style="font-size: 32px; margin-bottom: 8px;">' + (currentTab === 'unread' ? '✓' : '🔔') + '</div>' +
                '<div style="font-weight: 600; font-size: 14px;">' +
                (currentTab === 'unread' ? 'Tuyệt vời! Bạn không còn thông báo chưa đọc nào.' : 'Chưa có thông báo nào.') +
                '</div></div>';
            return;
        }

        let html = '';
        for (let i = 0; i < items.length; i++) {
            const n = items[i];
            const isUnread = !n.isRead;
            html += '<div class="notif-card-item ' + (isUnread ? 'unread' : 'read') + '">' +
                '<div class="notif-card-title">' + escapeHtml(n.message) + '</div>' +
                '<div class="notif-card-meta">' +
                '<span style="display: inline-flex; align-items: center; gap: 6px;">' +
                '<span>🕒</span><span>' + (n.createdAt || '') + '</span>' +
                '</span>';

            if (n.topicId) {
                var detailUrl = '${base}/reporter/detail?id=' + n.topicId;
                <c:choose>
                    <c:when test="${not empty sessionScope.permissions and sessionScope.permissions.contains('TOPIC_EVALUATE')}">
                        detailUrl = '${base}/editorial/criteria-scoring?topicId=' + n.topicId;
                    </c:when>
                    <c:when test="${not empty sessionScope.permissions and sessionScope.permissions.contains('FIELDWORK_TRACK')}">
                        detailUrl = '${base}/fieldwork/kanban';
                    </c:when>
                </c:choose>
                html += '<a href="' + detailUrl + '" class="notif-detail-link" onclick="handleDetailClick(event, ' + n.id + ', \x27' + detailUrl + '\x27)">' +
                    'Xem hồ sơ đề tài #' + n.topicId + ' ↗</a>';
            } else if (isUnread) {
                html += '<button type="button" class="btn sm" style="padding: 3px 8px; font-size: 11px;" onclick="markSingleRead(' + n.id + ')">Đánh dấu đã đọc</button>';
            }

            html += '</div></div>';
        }
        container.innerHTML = html;
    }

    function escapeHtml(text) {
        if (!text) return '';
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(text).replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    window.openNotificationsModal = function() {
        const backdrop = document.getElementById('notifModalBackdrop');
        if (backdrop) backdrop.style.display = 'grid';
        loadNotifications();
    };

    window.closeNotificationsModal = function() {
        const backdrop = document.getElementById('notifModalBackdrop');
        if (backdrop) backdrop.style.display = 'none';
    };

    window.handleNotifBackdropClick = function(event) {
        if (event.target === document.getElementById('notifModalBackdrop')) {
            window.closeNotificationsModal();
        }
    };

    window.switchNotifTab = function(tab) {
        currentTab = tab;
        renderNotifications();
    };

    function loadNotifications() {
        fetch(apiEndpoint)
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && data.notifications) {
                    notifState = data;
                    updateBadge(data.unreadCount);
                    renderNotifications();
                }
            })
            .catch(function(err) {
                console.error('Lỗi tải thông báo:', err);
                const container = document.getElementById('notifListContainer');
                if (container) {
                    container.innerHTML = '<div style="color:var(--danger); padding:24px; text-align:center;">Không thể kết nối đến máy chủ để lấy thông báo.</div>';
                }
            });
    }

    window.markAllNotificationsRead = function() {
        fetch(apiEndpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=markAllRead'
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data && data.success) {
                for (let i = 0; i < notifState.notifications.length; i++) {
                    notifState.notifications[i].isRead = true;
                }
                notifState.unreadCount = 0;
                updateBadge(0);
                renderNotifications();
            }
        })
        .catch(function(err) {
            console.error('Lỗi đánh dấu đã đọc tất cả:', err);
        });
    };

    window.markSingleRead = function(notifId) {
        fetch(apiEndpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=markRead&id=' + encodeURIComponent(notifId)
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            for (let i = 0; i < notifState.notifications.length; i++) {
                if (String(notifState.notifications[i].id) === String(notifId)) {
                    notifState.notifications[i].isRead = true;
                    break;
                }
            }
            if (data && typeof data.unreadCount !== 'undefined') {
                notifState.unreadCount = data.unreadCount;
            } else {
                notifState.unreadCount = Math.max(0, notifState.unreadCount - 1);
            }
            updateBadge(notifState.unreadCount);
            renderNotifications();
        })
        .catch(function(err) {
            console.error('Lỗi đánh dấu đã đọc:', err);
        });
    };

    window.handleDetailClick = function(event, notifId, targetUrl) {
        event.preventDefault();
        fetch(apiEndpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'action=markRead&id=' + encodeURIComponent(notifId)
        }).finally(function() {
            window.location.href = targetUrl;
        });
    };

    // Auto-fetch unread count on initial page load
    document.addEventListener('DOMContentLoaded', function() {
        fetch(apiEndpoint)
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data && typeof data.unreadCount !== 'undefined') {
                    updateBadge(data.unreadCount);
                }
            })
            .catch(function() {});
    });
})();
</script>
