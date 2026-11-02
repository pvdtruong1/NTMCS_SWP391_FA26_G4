export interface TaskNudgeRecipient {
  topicId: number;
  topicCode: string | null;
  topicTitle: string;
  reporterId: number;
  reporterName: string;
  roleInTeam: string;
}

export async function getTaskNudgeRecipients(): Promise<TaskNudgeRecipient[]> {
  const response = await fetch('/api/task-nudges');
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.error || `Không tải được danh sách (${response.status}).`);
  return data as TaskNudgeRecipient[];
}

export async function sendTaskNudge(payload: {
  actorUserId: number;
  reporterUserId: number;
  topicId: number;
  title: string;
  message: string;
  urgency: 'NORMAL' | 'HIGH' | 'EMERGENCY';
}): Promise<void> {
  const response = await fetch('/api/task-nudges', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.error || `Không gửi được lời nhắc (${response.status}).`);
}
