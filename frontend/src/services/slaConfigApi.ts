export interface SLAConfig {
  inAppEnabled: boolean;
  thresholdHours1: number;
  thresholdHours2: number;
  thresholdHours3: number;
  earlyWarningTemplate: string;
  warningTemplate: string;
  urgentTemplate: string;
  nudgeTemplate: string;
}

export const DEFAULT_SLA_CONFIG: SLAConfig = {
  inAppEnabled: true,
  thresholdHours1: 24,
  thresholdHours2: 12,
  thresholdHours3: 2,
  earlyWarningTemplate: 'Đề tài {topicCode} chỉ còn {hours} giờ nữa là đến hạn chót ({deadline}). Bạn có cần xin gia hạn thêm thời gian không?',
  warningTemplate: 'Đề tài {topicCode} chỉ còn {hours} giờ trước deadline ({deadline}). Vui lòng khẩn trương hoàn thiện tư liệu và bản thảo.',
  urgentTemplate: 'CẢNH BÁO KHẨN: Đề tài {topicCode} đã chạm ngưỡng cảnh báo đỏ. Deadline: {deadline}. Thời gian: {hours}. Vui lòng xử lý ngay.',
  nudgeTemplate: "Đôn đốc khẩn cấp: Vui lòng ưu tiên xử lý nhiệm vụ '{taskDescription}' của đề tài {topicCode} và cập nhật tiến độ sớm nhất.",
};

export async function getSLAConfig(): Promise<SLAConfig> {
  const response = await fetch('/api/sla-config');
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.error || `Không tải được cấu hình SLA (${response.status}).`);
  }
  return response.json();
}

export async function saveSLAConfig(config: SLAConfig): Promise<void> {
  const response = await fetch('/api/sla-config', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config),
  });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.error || `Không lưu được cấu hình SLA (${response.status}).`);
  }
}
