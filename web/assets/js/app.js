(() => {
  'use strict';

  const DARK_KEY = 'ntmcs_dark_v2';

  // --- Dark Mode Initialization ---
  function initDarkMode() {
    const isDark = localStorage.getItem(DARK_KEY) === '1';
    if (isDark) {
      document.body.classList.add('dark');
    } else {
      document.body.classList.remove('dark');
    }
    const toggles = document.querySelectorAll('.dark-mode-toggle');
    toggles.forEach(toggle => {
      if (toggle.type === 'checkbox') {
        toggle.checked = isDark;
      }
    });
  }

  function toggleDarkMode() {
    const isDark = document.body.classList.toggle('dark');
    localStorage.setItem(DARK_KEY, isDark ? '1' : '0');
    const toggles = document.querySelectorAll('.dark-mode-toggle');
    toggles.forEach(toggle => {
      if (toggle.type === 'checkbox') {
        toggle.checked = isDark;
      }
    });
    showToast(isDark ? 'Đã bật chế độ Dark Mode' : 'Đã chuyển sang Light Mode', 'success');
  }

  // --- Toast Notifications ---
  function showToast(message, type = 'success') {
    let container = document.getElementById('toast-root');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-root';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    const icon = type === 'success' ? '✓' : type === 'error' ? '×' : '!';
    toast.innerHTML = `<b>${icon}</b><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(8px)';
      setTimeout(() => toast.remove(), 250);
    }, 3200);
  }

  // --- Mobile Sidebar Toggle ---
  function initMobileMenu() {
    const menuBtn = document.querySelector('[data-menu]');
    const sidebar = document.getElementById('sidebar');
    if (menuBtn && sidebar) {
      menuBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        sidebar.classList.toggle('open');
      });
      document.addEventListener('click', (e) => {
        if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && !menuBtn.contains(e.target)) {
          sidebar.classList.remove('open');
        }
      });
    }
  }

  // --- Global Quick Search (Ctrl+K) ---
  function openSearchModal(contextPath) {
    document.getElementById('search-modal-backdrop')?.remove();

    const backdrop = document.createElement('div');
    backdrop.id = 'search-modal-backdrop';
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = `
      <div class="modal">
        <div class="modal-head">
          <div>
            <div class="eyebrow">NTMCS GLOBAL SEARCH</div>
            <h2>Tìm kiếm nhanh hệ thống</h2>
          </div>
          <button class="icon-btn" data-modal-close>×</button>
        </div>
        <div class="modal-body">
          <input id="global-search-input" class="big-input" style="width:100%" placeholder="Nhập từ khóa tìm kiếm đề tài, chuyên mục, ý tưởng..." autofocus>
          <div class="search-results" id="global-search-results">
            <a href="${contextPath}/reporter/compose"><strong>✍️ Soạn đề tài mới</strong><small>Tạo bản nháp hoặc nộp đề xuất</small></a>
            <a href="${contextPath}/reporter/duplicate-search"><strong>🔍 Tra cứu trùng lặp</strong><small>Kiểm tra độ trùng với các đề tài trong 30 ngày gần đây</small></a>
            <a href="${contextPath}/reporter/topics"><strong>📑 Đề tài của tôi</strong><small>Xem và quản lý tất cả đề xuất đề tài</small></a>
            <a href="${contextPath}/reporter/dashboard"><strong>📊 Reporter Dashboard</strong><small>Tổng quan thống kê trạng thái đề tài</small></a>
          </div>
        </div>
        <div class="modal-foot">
          <small class="hint" style="margin-right:auto;align-self:center">Mẹo: Nhấn Enter để tìm kiếm trong danh sách đề tài</small>
          <button class="btn" data-modal-close>Đóng</button>
        </div>
      </div>
    `;

    document.body.appendChild(backdrop);

    const input = backdrop.querySelector('#global-search-input');
    input.focus();

    input.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        const val = input.value.trim();
        if (val) {
          window.location.href = `${contextPath}/reporter/topics?keyword=${encodeURIComponent(val)}`;
        }
      }
      if (e.key === 'Escape') {
        backdrop.remove();
      }
    });

    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop || e.target.closest('[data-modal-close]')) {
        backdrop.remove();
      }
    });
  }

  // --- Helper to escape HTML ---
  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // --- Badge notification updater ---
  function updateNotifBadge(count) {
    const notifBtn = document.querySelector('[data-notif-trigger]');
    if (!notifBtn) return;
    let badge = notifBtn.querySelector('em');
    if (!count || count <= 0) {
      if (badge) badge.remove();
    } else {
      if (!badge) {
        badge = document.createElement('em');
        notifBtn.appendChild(badge);
      }
      badge.textContent = count > 9 ? '9+' : String(count);
    }
  }

  // --- Notifications Modal ---
  function openNotificationsModal(contextPath) {
    document.getElementById('notif-modal-backdrop')?.remove();

    const backdrop = document.createElement('div');
    backdrop.id = 'notif-modal-backdrop';
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = `
      <div class="modal notif-modal">
        <div class="notif-modal-header">
          <div class="notif-tabs">
            <button type="button" class="notif-tab-btn active" id="notif-tab-all">Tất cả (...)</button>
            <button type="button" class="notif-tab-btn" id="notif-tab-unread">Chưa đọc (...)</button>
          </div>
          <button type="button" class="notif-mark-all-btn" id="notif-mark-all-btn" title="Đánh dấu tất cả thông báo là đã đọc">
            <span>✔</span> <span>Đánh dấu đã đọc tất cả</span>
          </button>
        </div>

        <div class="notif-modal-body" id="notif-list-container" style="max-height: 440px; overflow-y: auto; padding-right: 4px;">
          <div style="text-align: center; padding: 36px 16px; color: var(--muted, #64748b);">
            <div style="font-size: 24px; margin-bottom: 8px;">⏳</div>
            <div>Đang tải thông báo từ hệ thống...</div>
          </div>
        </div>

        <div class="modal-foot" style="margin-top: 14px; padding-top: 12px; border-top: 1px solid var(--line, #e2e8f0); display: flex; justify-content: flex-end;">
          <button class="btn" data-modal-close style="background: #f1f5f9; color: #1e293b; border: 1px solid #cbd5e1; border-radius: 8px; padding: 7px 22px; font-weight: 500;">Đóng</button>
        </div>
      </div>
    `;

    document.body.appendChild(backdrop);
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop || e.target.closest('[data-modal-close]')) {
        backdrop.remove();
      }
    });

    let currentTab = 'all';
    let notifData = { unreadCount: 0, totalCount: 0, notifications: [] };

    function renderList() {
      const container = document.getElementById('notif-list-container');
      const tabAll = document.getElementById('notif-tab-all');
      const tabUnread = document.getElementById('notif-tab-unread');
      if (!container || !tabAll || !tabUnread) return;

      tabAll.textContent = `Tất cả (${notifData.totalCount})`;
      tabUnread.textContent = `Chưa đọc (${notifData.unreadCount})`;

      if (currentTab === 'all') {
        tabAll.classList.add('active');
        tabUnread.classList.remove('active');
      } else {
        tabAll.classList.remove('active');
        tabUnread.classList.add('active');
      }

      const items = currentTab === 'all'
        ? notifData.notifications
        : notifData.notifications.filter(n => !n.isRead);

      if (items.length === 0) {
        container.innerHTML = `
          <div style="text-align: center; padding: 42px 16px; color: var(--muted, #64748b);">
            <div style="font-size: 32px; margin-bottom: 8px;">${currentTab === 'unread' ? '✓' : '🔔'}</div>
            <div style="font-weight: 600; font-size: 14px;">
              ${currentTab === 'unread' ? 'Tuyệt vời! Bạn không còn thông báo chưa đọc nào.' : 'Chưa có thông báo nào.'}
            </div>
          </div>
        `;
        return;
      }

      container.innerHTML = items.map(n => `
        <div class="notif-card-item ${n.isRead ? 'read' : 'unread'}">
          <div class="notif-card-title">
            ${escapeHtml(n.message)}
          </div>
          <div class="notif-card-meta">
            <span style="display: inline-flex; align-items: center; gap: 6px;">
              <span>🕒</span>
              <span>${n.createdAt || ''}</span>
            </span>
            ${n.topicId ? `
              <a href="${contextPath}/reporter/detail?id=${n.topicId}" class="notif-detail-link" data-notif-id="${n.id}">
                Xem hồ sơ đề tài #${n.topicId} ↗
              </a>
            ` : ''}
          </div>
        </div>
      `).join('');

      // Đánh dấu đã đọc khi phóng viên bấm xem hồ sơ đề tài
      container.querySelectorAll('.notif-detail-link').forEach(link => {
        link.addEventListener('click', () => {
          const id = link.getAttribute('data-notif-id');
          if (id) {
            fetch(`${contextPath}/reporter/notifications`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
              body: `action=markRead&id=${encodeURIComponent(id)}`
            }).then(() => {
              const matched = notifData.notifications.find(item => String(item.id) === String(id));
              if (matched && !matched.isRead) {
                matched.isRead = true;
                notifData.unreadCount = Math.max(0, notifData.unreadCount - 1);
                updateNotifBadge(notifData.unreadCount);
              }
            }).catch(() => {});
          }
        });
      });
    }

    // Sự kiện chuyển tab Tất cả / Chưa đọc
    document.getElementById('notif-tab-all')?.addEventListener('click', () => {
      currentTab = 'all';
      renderList();
    });

    document.getElementById('notif-tab-unread')?.addEventListener('click', () => {
      currentTab = 'unread';
      renderList();
    });

    // Sự kiện nút "Đánh dấu đã đọc tất cả"
    document.getElementById('notif-mark-all-btn')?.addEventListener('click', async () => {
      try {
        const res = await fetch(`${contextPath}/reporter/notifications`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: 'action=markAllRead'
        });
        const data = await res.json();
        if (data.success) {
          notifData.notifications.forEach(n => { n.isRead = true; });
          notifData.unreadCount = 0;
          updateNotifBadge(0);
          renderList();
          showToast('Đã đánh dấu tất cả thông báo là đã đọc', 'success');
        }
      } catch (err) {
        console.error('Không thể đánh dấu đọc tất cả:', err);
      }
    });

    // Tải dữ liệu thông báo từ Database
    fetch(`${contextPath}/reporter/notifications`)
      .then(r => r.json())
      .then(data => {
        notifData = data;
        updateNotifBadge(data.unreadCount);
        renderList();
      })
      .catch(err => {
        console.error('Lỗi tải thông báo:', err);
        const container = document.getElementById('notif-list-container');
        if (container) {
          container.innerHTML = '<div style="color:var(--danger); padding:24px; text-align:center;">Không thể kết nối đến máy chủ lấy thông báo.</div>';
        }
      });
  }

  // --- Account Info Modal ---
  function openAccountModal(contextPath) {
    document.getElementById('account-modal-backdrop')?.remove();

    const backdrop = document.createElement('div');
    backdrop.id = 'account-modal-backdrop';
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = `
      <div class="modal">
        <div class="modal-head">
          <div>
            <div class="eyebrow">TÀI KHOẢN HIỆN TẠI</div>
            <h2>Thông tin Phóng viên</h2>
          </div>
          <button class="icon-btn" data-modal-close>×</button>
        </div>
        <div class="modal-body">
          <div style="display:flex;align-items:center;gap:18px;margin-bottom:20px;padding:18px;background:var(--surface2);border-radius:14px;border:1px solid var(--line)">
            <div class="avatar xl">PV</div>
            <div>
              <h3 style="margin:0 0 4px;font-size:18px">Mai Khánh Hùng</h3>
              <p style="margin:0;color:var(--muted)">hung@ntmcs.local · <strong>REPORTER</strong></p>
              <div style="margin-top:8px"><span class="badge b-in_progress">Ban Thời sự</span></div>
            </div>
          </div>
          <div class="card" style="box-shadow:none;border-color:var(--line);background:var(--surface)">
            <h3 style="margin:0 0 10px;font-size:14px">Quyền hạn trong Iteration 1 (Reporter WF1):</h3>
            <ul style="margin:0;padding-left:20px;color:var(--text);font-size:13px;line-height:1.7">
              <li>Soạn thảo ý tưởng và lưu bản nháp đề tài (DRAFT)</li>
              <li>Nộp đề xuất đề tài lên Ban biên tập (SUBMITTED)</li>
              <li>Tra cứu đề tài trùng lặp trong 30 ngày gần đây</li>
              <li>Theo dõi tiến độ quy trình đề tài (Workflow 7 bước)</li>
              <li>Chỉnh sửa và nộp lại khi có yêu cầu chỉnh sửa (REVISION_REQUESTED)</li>
            </ul>
          </div>
        </div>
        <div class="modal-foot">
          <a class="btn primary" href="${contextPath}/reporter/reset" onclick="return confirm('Bạn có muốn đặt lại dữ liệu mẫu về ban đầu không?')">Đặt lại dữ liệu mẫu (Reset)</a>
          <button class="btn" data-modal-close>Đóng</button>
        </div>
      </div>
    `;

    document.body.appendChild(backdrop);
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop || e.target.closest('[data-modal-close]')) {
        backdrop.remove();
      }
    });
  }

  // --- Duplicate Check Assistant in Compose Form ---
  function initDuplicateAssistant(contextPath) {
    const btn = document.getElementById('btn-check-duplicate');
    const titleInput = document.getElementById('title');
    if (!btn || !titleInput) return;

    btn.addEventListener('click', () => {
      const query = titleInput.value.trim();
      if (!query) {
        showToast('Vui lòng nhập tiêu đề để kiểm tra trùng lặp', 'warning');
        titleInput.focus();
        return;
      }
      window.open(`${contextPath}/reporter/duplicate-search?keyword=${encodeURIComponent(query)}`, '_blank');
    });
  }

  // --- DOM Ready Hook ---
  document.addEventListener('DOMContentLoaded', () => {
    initDarkMode();
    initMobileMenu();

    const rootPath = window.NTMCS_CONTEXT_PATH || '';

    // Dark mode buttons
    document.querySelectorAll('[data-dark-toggle]').forEach(el => {
      el.addEventListener('click', toggleDarkMode);
    });

    // Global search button & keyboard shortcut
    document.querySelectorAll('[data-search-trigger]').forEach(el => {
      el.addEventListener('click', () => openSearchModal(rootPath));
    });

    document.addEventListener('keydown', (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        openSearchModal(rootPath);
      }
    });

    // Notifications button
    document.querySelectorAll('[data-notif-trigger]').forEach(el => {
      el.addEventListener('click', () => openNotificationsModal(rootPath));
    });

    // Account chip click
    document.querySelectorAll('[data-account-trigger]').forEach(el => {
      el.addEventListener('click', () => openAccountModal(rootPath));
    });

    // Duplicate assistant
    initDuplicateAssistant(rootPath);

    // Confirmation on delete draft
    document.querySelectorAll('[data-confirm-delete]').forEach(el => {
      el.addEventListener('click', (e) => {
        if (!confirm('Bạn có chắc chắn muốn xóa bản nháp này không?')) {
          e.preventDefault();
        }
      });
    });

    // Show URL parameter messages as toast
    const params = new URLSearchParams(window.location.search);
    const msg = params.get('message');
    if (msg === 'submitted') showToast('Đã nộp đề xuất thành công! Đề tài đang chờ thẩm định.', 'success');
    if (msg === 'draftSaved') showToast('Đã lưu bản nháp đề tài.', 'success');
    if (msg === 'deleted') showToast('Đã xóa bản nháp đề tài.', 'success');
    if (msg === 'reset') showToast('Đã khôi phục dữ liệu mẫu ban đầu.', 'success');
  });

  window.NTMCS = {
    showToast,
    toggleDarkMode,
    openSearchModal
  };
})();
