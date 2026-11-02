import { Notification } from '../types';

interface BackendNotification {
  notificationId: number;
  userId: number;
  topicId: number | null;
  message: string;
  isRead: boolean;
  createdAt: string;
}

export async function getNotifications(userId: number): Promise<Notification[]> {
  const response = await fetch(`/api/notifications?userId=${encodeURIComponent(userId)}`);
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.error || `Không tải được thông báo (${response.status}).`);
  }
  const data: BackendNotification[] = await response.json();
  return data.map((item) => ({
    notification_id: item.notificationId,
    user_id: item.userId,
    topic_id: item.topicId,
    message: item.message,
    is_read: item.isRead,
    created_at: item.createdAt,
  }));
}

export async function markNotificationAsRead(notificationId: number, userId: number): Promise<void> {
  const response = await fetch('/api/notifications', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ notificationId, userId }),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.error || `Không cập nhật được thông báo (${response.status}).`);
  }
}

export async function markAllNotificationsAsRead(userId: number): Promise<void> {
  const response = await fetch('/api/notifications', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, markAll: true }),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.error || `Không cập nhật được thông báo (${response.status}).`);
  }
}

export function notifyNotificationCountChanged(): void {
  window.dispatchEvent(new Event('ntmcs-notifications-updated'));
}
