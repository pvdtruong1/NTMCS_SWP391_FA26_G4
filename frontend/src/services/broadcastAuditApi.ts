export interface FieldworkNotificationAudit {
  notificationId: number;
  recipientId: number;
  recipientName: string;
  recipientEmail: string;
  recipientDesk: string | null;
  topicId: number | null;
  topicCode: string | null;
  topicTitle: string | null;
  message: string;
  isRead: boolean;
  senderId: number;
  senderName: string;
  sentAt: string;
  readAt: string | null;
  urgency: 'NORMAL' | 'HIGH' | 'EMERGENCY';
  sourceType: string;
}

export async function getFieldworkNotificationAudit(userId: number): Promise<FieldworkNotificationAudit[]> {
  const response = await fetch(`/api/broadcast-audit?userId=${encodeURIComponent(userId)}`);
  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(payload?.error || `Không tải được nhật ký (${response.status}).`);
  }
  return Array.isArray(payload?.items) ? payload.items : [];
}


export async function pingUnreadNotification(input: {
  actorUserId: number;
  notificationId: number;
  recipientId: number;
}): Promise<void> {
  const response = await fetch('/api/broadcast-audit/ping', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  });
  const payload = await response.json().catch(() => null);
  if (!response.ok) throw new Error(payload?.error || `Không Ping được người nhận (${response.status}).`);
}
