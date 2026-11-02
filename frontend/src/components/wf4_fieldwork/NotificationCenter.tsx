/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.5: In-App Notification Center
 */

import React, { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { Notification } from '../../types';
import { Modal } from '../common/Modal';
import { Bell, CheckCheck, Clock, ExternalLink, Loader2 } from 'lucide-react';
import {
  getNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
  notifyNotificationCountChanged,
} from '../../services/notificationApi';
import { dbStore } from '../../services/dbStore';

interface NotificationCenterProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectTopic?: (topicId: number) => void;
}

export const NotificationCenter: React.FC<NotificationCenterProps> = ({
  isOpen,
  onClose,
  onSelectTopic,
}) => {
  const { currentUser } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filterType, setFilterType] = useState<'ALL' | 'UNREAD'>('ALL');
  const [isLoading, setIsLoading] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const refreshNotifications = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const rows = await getNotifications(currentUser.user_id);
      setNotifications(rows);
      notifyNotificationCountChanged();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Không tải được thông báo.');
    } finally {
      setIsLoading(false);
    }
  }, [currentUser.user_id]);

  useEffect(() => {
    if (isOpen) refreshNotifications();
  }, [isOpen, refreshNotifications]);

  const handleMarkAllRead = async () => {
    setIsUpdating(true);
    setErrorMessage(null);
    try {
      await markAllNotificationsAsRead(currentUser.user_id);
      // Preserve the separate SCR-4.8 local broadcast read-receipt behavior.
      dbStore.getState().broadcastNotifications?.forEach((broadcast) => {
        dbStore.markBroadcastAsRead(broadcast.broadcast_id, currentUser.user_id);
      });
      await refreshNotifications();
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : 'Không thể đánh dấu đã đọc.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleItemClick = async (notification: Notification) => {
    if (!notification.is_read) {
      setIsUpdating(true);
      setErrorMessage(null);
      try {
        await markNotificationAsRead(notification.notification_id, currentUser.user_id);
        setNotifications((items) => items.map((item) =>
          item.notification_id === notification.notification_id ? { ...item, is_read: true } : item
        ));
        notifyNotificationCountChanged();
      } catch (error) {
        setErrorMessage(error instanceof Error ? error.message : 'Không thể cập nhật thông báo.');
        setIsUpdating(false);
        return;
      }
      setIsUpdating(false);
    }
    if (notification.topic_id && onSelectTopic) {
      onSelectTopic(notification.topic_id);
      onClose();
    }
  };

  const unreadCount = notifications.filter((item) => !item.is_read).length;
  const filtered = notifications.filter((item) => filterType === 'UNREAD' ? !item.is_read : true);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Trung tâm Thông báo Nội bộ Tòa soạn"
      subtitle={`Hộp thư dành riêng cho ${currentUser.full_name} (${unreadCount} chưa đọc)`}
      maxWidth="xl"
    >
      <div className="space-y-4 text-xs">
        <div className="flex items-center justify-between pb-2 border-b border-slate-100 gap-2">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setFilterType('ALL')}
              className={`px-3 py-1 rounded font-medium ${filterType === 'ALL' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'}`}
            >Tất cả ({notifications.length})</button>
            <button
              onClick={() => setFilterType('UNREAD')}
              className={`px-3 py-1 rounded font-medium ${filterType === 'UNREAD' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'}`}
            >Chưa đọc ({unreadCount})</button>
          </div>
          <button
            onClick={handleMarkAllRead}
            disabled={isUpdating || unreadCount === 0}
            className="text-slate-600 hover:text-slate-900 font-medium flex items-center gap-1 disabled:opacity-40 disabled:cursor-not-allowed"
          >
            <CheckCheck className="w-3.5 h-3.5 text-emerald-600" /> Đánh dấu đã đọc tất cả
          </button>
        </div>

        {errorMessage && <div className="p-3 rounded border border-rose-200 bg-rose-50 text-rose-700">{errorMessage}</div>}

        <div className="space-y-2 max-h-96 overflow-y-auto">
          {isLoading ? (
            <div className="py-8 flex items-center justify-center gap-2 text-slate-400"><Loader2 className="w-4 h-4 animate-spin" /> Đang tải thông báo từ máy chủ...</div>
          ) : filtered.length === 0 ? (
            <div className="py-8 text-center text-slate-400"><Bell className="w-6 h-6 mx-auto mb-2 opacity-50" />Không có thông báo nào trong danh mục này.</div>
          ) : (
            filtered.map((notification) => {
              const isUnread = !notification.is_read;
              return (
                <div
                  key={notification.notification_id}
                  onClick={() => handleItemClick(notification)}
                  className={`p-3 rounded-lg border transition-all cursor-pointer ${isUnread ? 'bg-red-50/40 border-red-200 text-slate-900' : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'}`}
                >
                  <div className="flex justify-between items-start gap-2">
                    <p className={`leading-relaxed ${isUnread ? 'font-semibold text-slate-900' : 'text-slate-700'}`}>{notification.message}</p>
                    {isUnread && <span className="w-2 h-2 rounded-full bg-red-600 shrink-0 mt-1" />}
                  </div>
                  <div className="flex items-center justify-between text-[11px] text-slate-400 mt-2 pt-1 border-t border-slate-100 font-mono">
                    <span className="flex items-center gap-1"><Clock className="w-3 h-3 text-slate-400" />{notification.created_at || 'Vừa xong'}</span>
                    {notification.topic_id && <span className="text-red-700 font-sans font-semibold hover:underline flex items-center gap-0.5">Xem hồ sơ đề tài #{notification.topic_id} <ExternalLink className="w-3 h-3" /></span>}
                  </div>
                </div>
              );
            })
          )}
        </div>

        <div className="flex justify-end pt-2 border-t border-slate-100">
          <button onClick={onClose} className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg">Đóng</button>
        </div>
      </div>
    </Modal>
  );
};
