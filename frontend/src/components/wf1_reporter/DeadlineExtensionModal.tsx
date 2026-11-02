/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.8: Deadline Extension Request View & Modal (Phóng viên tác nghiệp)
 * Shows the full list of extension requests with explicit PENDING status waiting for editor approval.
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, RequestDeadlineExtension } from '../../types';
import { Modal } from '../common/Modal';
import { Clock, Plus, AlertCircle, CheckCircle2, AlertTriangle, FileText, Calendar, RotateCcw } from 'lucide-react';

interface DeadlineExtensionModalProps {
  isOpen: boolean;
  onClose: () => void;
  topic: Topic | null;
  onSuccess: () => void;
}

export const DeadlineExtensionModal: React.FC<DeadlineExtensionModalProps> = ({
  isOpen,
  onClose,
  topic,
  onSuccess,
}) => {
  const { currentUser } = useAuth();
  const [requestedDeadline, setRequestedDeadline] = useState('');
  const [reason, setReason] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!topic) return null;

  const currentDeadline = topic.deadline || '2026-10-25';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!requestedDeadline) {
      setErrorMessage('Vui lòng chọn hạn chót đề xuất mới.');
      return;
    }
    if (!reason.trim()) {
      setErrorMessage('Vui lòng giải trình rõ lý do xin gia hạn deadline.');
      return;
    }

    try {
      dbStore.requestDeadlineExtension({
        topic_id: topic.topic_id,
        requested_by_user_id: currentUser.user_id,
        current_deadline: currentDeadline,
        requested_deadline: requestedDeadline,
        reason,
      });
      onSuccess();
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Gửi yêu cầu gia hạn thất bại.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Đề nghị gia hạn hạn chót nộp bản thảo (SCR-1.8)"
      subtitle={`Đề tài: ${topic.topic_code || '#' + topic.topic_id} - ${topic.title}`}
      maxWidth="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        <div className="p-3 bg-slate-50 rounded-lg border border-slate-200">
          <div className="text-slate-500 text-[11px]">Hạn chót hiện tại theo phê duyệt:</div>
          <div className="font-mono font-bold text-slate-900 text-sm">{currentDeadline}</div>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Hạn chót đề xuất mới (Requested Deadline) <span className="text-rose-600">*</span>
          </label>
          <input
            type="date"
            required
            min={currentDeadline}
            value={requestedDeadline}
            onChange={e => setRequestedDeadline(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs font-mono focus:bg-white focus:outline-none focus:ring-1 focus:ring-blue-600"
          />
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Lý do & Khó khăn khách quan tại hiện trường <span className="text-rose-600">*</span>
          </label>
          <textarea
            rows={3}
            required
            placeholder="Giải trình cụ thể: VD: Lịch làm việc với cơ quan chức năng bị dời, thời tiết bão lũ gây gián đoạn ghi hình, cần thẩm định thêm chứng cứ độc quyền..."
            value={reason}
            onChange={e => setReason(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:bg-white focus:outline-none focus:ring-1 focus:ring-blue-600 resize-none leading-relaxed"
          />
        </div>

        <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 text-[11px] leading-relaxed">
          <strong>Lưu ý:</strong> Sau khi gửi, đơn xin gia hạn sẽ ở trạng thái <strong className="text-amber-800">PENDING (Chờ phê duyệt)</strong>. Hạn chót chỉ chính thức thay đổi sau khi Biên tập viên Ban duyệt chấp thuận.
        </div>

        <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg cursor-pointer"
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow-xs cursor-pointer"
          >
            Gửi yêu cầu gia hạn
          </button>
        </div>
      </form>
    </Modal>
  );
};

export const DeadlineExtensionView: React.FC<{ onNavigate?: (s: string) => void }> = ({ onNavigate }) => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [successNotice, setSuccessNotice] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const myTopics = dbState.topics.filter(
    t => t.author_user_id === currentUser.user_id || dbState.topicMembers.some(m => m.topic_id === t.topic_id && m.user_id === currentUser.user_id)
  );

  const extensions = dbState.extensions.filter(e => {
    return e.requested_by_user_id === currentUser.user_id || myTopics.some(t => t.topic_id === e.topic_id);
  });

  const pendingCount = extensions.filter(e => e.status === 'PENDING').length;
  const approvedCount = extensions.filter(e => e.status === 'APPROVED').length;
  const rejectedCount = extensions.filter(e => e.status === 'REJECTED').length;

  const handleOpenModal = () => {
    const candidate = myTopics.find(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'SUBMITTED') || myTopics[0];
    setSelectedTopic(candidate || dbState.topics[0]);
    setIsModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Phân hệ Phóng viên (WF-01)</span>
            <span>·</span>
            <span className="font-semibold text-blue-600">SCR-1.8</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <Clock className="w-6 h-6 text-blue-600" />
            <span>Theo dõi & Đề nghị Gia hạn Hạn chót Nộp bài (Deadline Extension)</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Các yêu cầu gia hạn sau khi gửi luôn ở trạng thái PENDING chờ Biên tập viên Ban xem xét và phê duyệt.
          </p>
        </div>

        <button
          onClick={handleOpenModal}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Gửi yêu cầu gia hạn mới (SCR-1.8)</span>
        </button>
      </div>

      {successNotice && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-semibold flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{successNotice}</span>
        </div>
      )}

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đang chờ duyệt (Pending)</span>
            <Clock className="w-4 h-4 text-amber-500" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-700">{pendingCount}</div>
          <div className="text-[11px] text-amber-600 mt-1">Chưa có ai duyệt (Ở trạng thái pending)</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đã được chấp thuận</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-700">{approvedCount}</div>
          <div className="text-[11px] text-emerald-600 mt-1">Hạn chót đề tài đã được lùi</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Bị từ chối</span>
            <AlertTriangle className="w-4 h-4 text-rose-500" />
          </div>
          <div className="text-2xl font-bold font-mono text-rose-700">{rejectedCount}</div>
          <div className="text-[11px] text-rose-600 mt-1">BTV giữ nguyên hạn chót ban đầu</div>
        </div>
      </div>

      {/* Extensions Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <div className="p-3 bg-slate-50 border-b border-slate-200 font-semibold text-slate-800 text-xs">
          Lịch sử các yêu cầu gia hạn đã gửi:
        </div>
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
              <th className="py-3 px-4">Mã đơn</th>
              <th className="py-3 px-4">Đề tài tác nghiệp</th>
              <th className="py-3 px-4">Hạn chót cũ</th>
              <th className="py-3 px-4">Hạn chót đề xuất</th>
              <th className="py-3 px-4">Lý do xin gia hạn</th>
              <th className="py-3 px-4">Trạng thái phê duyệt</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {extensions.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-8 text-center text-slate-400">
                  Chưa có yêu cầu gia hạn deadline nào.
                </td>
              </tr>
            ) : (
              extensions.map(ext => {
                const topic = dbState.topics.find(t => t.topic_id === ext.topic_id);

                return (
                  <tr key={ext.request_id} className="hover:bg-slate-50/70">
                    <td className="py-3 px-4 font-mono font-medium text-slate-800">
                      EXT-{String(ext.request_id).padStart(3, '0')}
                    </td>
                    <td className="py-3 px-4 max-w-xs">
                      <div className="font-semibold text-slate-900 line-clamp-1">{topic?.title}</div>
                      <div className="text-[11px] font-mono text-slate-400 mt-0.5">
                        {topic?.topic_code || `#${topic?.topic_id}`}
                      </div>
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-600">
                      {ext.current_deadline}
                    </td>
                    <td className="py-3 px-4 font-mono font-bold text-blue-700">
                      {ext.requested_deadline}
                    </td>
                    <td className="py-3 px-4 text-slate-600 max-w-sm">
                      <p className="line-clamp-2 leading-relaxed m-0">{ext.reason}</p>
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      {ext.status === 'PENDING' ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded text-[11px] font-bold bg-amber-50 text-amber-800 border border-amber-200">
                          <Clock className="w-3.5 h-3.5 text-amber-600" />
                          <span>Chờ BTV duyệt (PENDING)</span>
                        </span>
                      ) : ext.status === 'APPROVED' ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded text-[11px] font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
                          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                          <span>Đã chấp thuận</span>
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded text-[11px] font-bold bg-rose-50 text-rose-800 border border-rose-200">
                          <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />
                          <span>Từ chối</span>
                        </span>
                      )}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <DeadlineExtensionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        topic={selectedTopic}
        onSuccess={() => {
          setSuccessNotice('Yêu cầu gia hạn đã được gửi thành công và đang ở trạng thái PENDING chờ BTV phê duyệt.');
          setTimeout(() => setSuccessNotice(null), 4000);
        }}
      />
    </div>
  );
};
