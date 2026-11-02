/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-4.4: Emergency Incident Report Modal (Đạt - WF-04)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Modal } from '../common/Modal';
import { AlertTriangle, Send, ShieldAlert } from 'lucide-react';

interface EmergencyIncidentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export const EmergencyIncidentModal: React.FC<EmergencyIncidentModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const { currentUser } = useAuth();
  const dbState = dbStore.getState();

  const [topicId, setTopicId] = useState<number>(dbState.topics[0]?.topic_id || 1);
  const [description, setDescription] = useState('');
  const [severity, setSeverity] = useState<'HIGH' | 'CRITICAL'>('HIGH');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!description.trim()) {
      setErrorMessage('Vui lòng mô tả chi tiết sự cố khẩn cấp.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const incidentDescription =
        `[${severity === 'CRITICAL' ? 'NGHIÊM TRỌNG ĐẶC BIỆT' : 'KHẨN CẤP'}] ${description.trim()}`;

      const response = await fetch('/api/emergency-incidents', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          topicId,
          incidentDescription,
          actorUserId: currentUser.user_id,
        }),
      });

      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.error || 'Gửi báo cáo thất bại.');
      }

      onSuccess();
      onClose();
      setDescription('');
      setSeverity('HIGH');
    } catch (err: any) {
      setErrorMessage(err.message || 'Không thể kết nối Backend.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Báo cáo Sự cố Hiện trường Khẩn cấp"
      subtitle="Kích hoạt báo động SLA và gửi thông báo trực tiếp đến BTV Ban và Điều phối viên"
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-900 flex items-start gap-2.5">
          <ShieldAlert className="w-5 h-5 text-rose-600 shrink-0 mt-0.5" />
          <div className="leading-relaxed text-[11px]">
            <strong>Quy chuẩn bảo đảm an toàn tác nghiệp:</strong> Sử dụng khi tổ phóng viên gặp sự cản trở tác nghiệp, tai nạn phương tiện, rủi ro pháp lý nóng hoặc hỏng hóc thiết bị quay chụp trọng yếu tại hiện trường.
          </div>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Đề tài phát sinh sự cố <span className="text-rose-600">*</span>
          </label>
          <select
            value={topicId}
            onChange={e => setTopicId(Number(e.target.value))}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
          >
            {dbState.topics
              .filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'SUBMITTED')
              .map(t => (
                <option key={t.topic_id} value={t.topic_id}>
                  {t.topic_code || `#${t.topic_id}`} - {t.title}
                </option>
              ))}
          </select>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Mức độ khẩn cấp (Severity)
          </label>
          <div className="grid grid-cols-2 gap-3">
            <label className="flex items-center gap-2 p-2.5 border border-amber-200 bg-amber-50/50 rounded-lg cursor-pointer">
              <input
                type="radio"
                name="severity"
                value="HIGH"
                checked={severity === 'HIGH'}
                onChange={() => setSeverity('HIGH')}
                className="text-red-700"
              />
              <span className="font-semibold text-amber-900">Mức cao (High)</span>
            </label>
            <label className="flex items-center gap-2 p-2.5 border border-rose-200 bg-rose-50/50 rounded-lg cursor-pointer">
              <input
                type="radio"
                name="severity"
                value="CRITICAL"
                checked={severity === 'CRITICAL'}
                onChange={() => setSeverity('CRITICAL')}
                className="text-red-700"
              />
              <span className="font-semibold text-rose-900">Đặc biệt nghiêm trọng</span>
            </label>
          </div>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Nội dung mô tả sự việc & Địa điểm hiện trường <span className="text-rose-600">*</span>
          </label>
          <textarea
            rows={4}
            required
            placeholder="Nêu rõ vị trí tác nghiệp, đối tượng gây cản trở hoặc mức độ thiệt hại của phương tiện, thiết bị..."
            value={description}
            onChange={e => setDescription(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600 leading-relaxed resize-none"
          />
        </div>

        <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={isSubmitting}
            className="px-4 py-2 bg-rose-700 hover:bg-rose-800 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold rounded-lg shadow-xs flex items-center gap-1.5"
          >
            <AlertTriangle className="w-4 h-4" />
            {isSubmitting ? 'Đang gửi báo cáo...' : 'Phát báo động khẩn cấp'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
