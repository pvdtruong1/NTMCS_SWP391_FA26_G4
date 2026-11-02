/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Topbar Header Component
 */

import React, { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { getNotifications } from '../../services/notificationApi';
import {
  Search, Bell, ChevronDown, LogOut,
} from 'lucide-react';

interface HeaderProps {
  onOpenNotificationModal: () => void;
  onOpenActorSwitcher: () => void;
  onSearchClick?: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  onOpenNotificationModal,
  onOpenActorSwitcher,
  onSearchClick,
}) => {
  const { currentUser, currentRole, roleMeta, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);

  const refreshUnreadCount = useCallback(async () => {
    try {
      const notifications = await getNotifications(currentUser.user_id);
      setUnreadCount(notifications.filter((item) => !item.is_read).length);
    } catch (error) {
      console.error('Không tải được số lượng thông báo chưa đọc:', error);
    }
  }, [currentUser.user_id]);

  useEffect(() => {
    refreshUnreadCount();
    const handleUpdated = () => refreshUnreadCount();
    const handleFocus = () => refreshUnreadCount();
    window.addEventListener('ntmcs-notifications-updated', handleUpdated);
    window.addEventListener('focus', handleFocus);
    return () => {
      window.removeEventListener('ntmcs-notifications-updated', handleUpdated);
      window.removeEventListener('focus', handleFocus);
    };
  }, [refreshUnreadCount]);


  const deskName = dbStore.getState().desks.find((desk) => desk.desk_id === currentUser.desk_id)?.desk_name;

  return (
    <header className="topbar">
      <button onClick={onSearchClick} className="search cursor-pointer focus:outline-none" title="Tìm kiếm thông tin đề tài, phóng viên, thiết bị">
        <Search className="w-4 h-4 text-slate-400" />
        <span className="text-slate-500 text-xs truncate">Tìm kiếm đề tài, phóng viên, trang thiết bị, mã số...</span>
        <kbd className="hidden sm:inline-block">⌘K</kbd>
      </button>

      <div className="flex items-center gap-2">
        <button onClick={onOpenNotificationModal} className="icon-btn notif" title="Thông báo nội bộ tòa soạn">
          <Bell className="w-4 h-4 text-slate-600" />
          {unreadCount > 0 && <em>{unreadCount}</em>}
        </button>
        <button onClick={onOpenActorSwitcher} className="account-chip cursor-pointer hover:bg-slate-50" title="Nhấp để phân lại quyền hoặc chuyển đổi nhanh Actor">
          <div className="avatar">{currentUser.full_name.charAt(0)}</div>
          <div className="text-left hidden sm:block"><b>{currentUser.full_name}</b><small>{roleMeta.display_title}{deskName ? ` · ${deskName}` : ''}</small></div>
          <div className="pl-1"><span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 border border-blue-200">{currentRole.role_name}</span></div>
          <ChevronDown className="w-3.5 h-3.5 text-slate-400 ml-0.5" />
        </button>
        <button onClick={logout} className="icon-btn text-rose-600 hover:text-rose-700 hover:bg-rose-50" title="Đăng xuất khỏi hệ thống"><LogOut className="w-4 h-4" /></button>
      </div>
    </header>
  );
};
