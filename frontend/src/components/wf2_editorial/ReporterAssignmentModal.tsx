/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-2.5: Reporter Assignment and Workload Balancing Modal (Trường - WF-02)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, TeamRole } from '../../types';
import { Modal } from '../common/Modal';
import { Users, Plus, Trash2, CheckCircle2 } from 'lucide-react';

interface ReporterAssignmentModalProps {
  isOpen: boolean;
  onClose: () => void;
  topic: Topic | null;
  onSuccess: () => void;
}

export const ReporterAssignmentModal: React.FC<ReporterAssignmentModalProps> = ({
  isOpen,
  onClose,
  topic,
  onSuccess,
}) => {
  const { currentUser } = useAuth();
  const dbState = dbStore.getState();

  const [members, setMembers] = useState<Array<{ user_id: number; role_in_team: TeamRole }>>(() => {
    if (topic) {
      const existing = dbState.topicMembers.filter(m => m.topic_id === topic.topic_id);
      if (existing.length > 0) {
        return existing.map(m => ({ user_id: m.user_id, role_in_team: m.role_in_team }));
      }
      return [{ user_id: topic.author_user_id, role_in_team: 'LEAD_REPORTER' }];
    }
    return [];
  });

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  if (!topic) return null;

  // Calculate workloads for each reporter
  const getWorkload = (userId: number) => {
    const activeTopicsCount = dbState.topics.filter(
      t => (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'SUBMITTED') &&
           (t.author_user_id === userId || dbState.topicMembers.some(m => m.topic_id === t.topic_id && m.user_id === userId))
    ).length;
    return activeTopicsCount;
  };

  const handleAddMember = () => {
    // Only pick users with reporter or fieldwork role or in this desk
    const candidates = dbState.users.filter(u => u.active_status && (u.role_id === 1 || u.role_id === 5 || u.desk_id === currentUser.desk_id));
    const unselected = candidates.find(u => !members.some(m => m.user_id === u.user_id));
    if (unselected) {
      setMembers([...members, { user_id: unselected.user_id, role_in_team: 'PHOTO_REPORTER' }]);
    }
  };

  const handleRemoveMember = (idx: number) => {
    setMembers(members.filter((_, i) => i !== idx));
  };

  const handleSave = (directApprove: boolean = false) => {
    if (members.length === 0) {
      setErrorMessage('Đề tài phải có ít nhất một phóng viên đảm nhiệm.');
      return;
    }

    try {
      const isEditor = currentUser.role_id === 2 || currentUser.role_id === 3;
      const finalStatus = directApprove || (isEditor && topic.status === 'SUBMITTED') ? 'APPROVED' : topic.status;

      const updatedTopic = dbStore.updateTopic(
        topic.topic_id,
        {
          title: topic.title,
          angle: topic.angle || '',
          category_id: topic.category_id,
          deadline: topic.deadline || '',
          status: finalStatus,
        },
        members,
        dbState.topicEstimates.filter(e => e.topic_id === topic.topic_id),
        currentUser.user_id
      );

      if (finalStatus === 'APPROVED' && !updatedTopic.topic_code) {
        const year = new Date().getFullYear();
        const codeNum = String(updatedTopic.topic_id).padStart(3, '0');
        updatedTopic.topic_code = `TOPIC-${year}-${codeNum}`;
      }

      // Notify new team members
      members.forEach(m => {
        dbStore.addNotification(
          m.user_id,
          directApprove
            ? `BTV đã giao thẳng đề tài "${topic.title}" cho bạn. Đề tài được phê duyệt sẵn sàng tác nghiệp.`
            : `BTV đã phân công bạn vào tổ tác nghiệp đề tài "${topic.title}" với vai trò ${m.role_in_team}.`,
          topic.topic_id
        );
      });

      onSuccess();
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Lưu phân công thất bại.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Phân công phóng viên & Cân bằng tải tác nghiệp (SCR-2.5)"
      subtitle={`Đề tài: ${topic.topic_code || '#' + topic.topic_id} - ${topic.title}`}
      maxWidth="2xl"
    >
      <div className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg space-y-1">
          <div className="font-semibold text-slate-800">Thống kê khối lượng công việc hiện tại của phóng viên (Workload):</div>
          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 mt-2">
            {dbState.users.filter(u => u.role_id === 1 || u.role_id === 2).map(u => {
              const count = getWorkload(u.user_id);
              const isHeavy = count >= 3;
              return (
                <div key={u.user_id} className="p-2 bg-white rounded border border-slate-200 flex justify-between items-center">
                  <span className="truncate text-slate-700">{u.full_name}</span>
                  <span className={`font-mono font-bold px-1.5 py-0.5 rounded text-[11px] ${
                    isHeavy ? 'bg-amber-100 text-amber-900' : 'bg-slate-100 text-slate-700'
                  }`}>
                    {count} đề tài
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-slate-800">Danh sách nhân sự phân công:</span>
            <button
              onClick={handleAddMember}
              className="px-2.5 py-1 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded flex items-center gap-1 cursor-pointer"
            >
              <Plus className="w-3.5 h-3.5" /> Thêm thành viên
            </button>
          </div>

          <div className="space-y-2">
            {members.map((m, idx) => {
              const conflicts = dbStore.checkUserScheduleConflicts(m.user_id);

              return (
                <div key={idx} className="p-2.5 rounded-lg border border-slate-200 bg-slate-50 space-y-1.5">
                  <div className="flex items-center gap-3">
                    <div className="flex-1">
                      <select
                        value={m.user_id}
                        onChange={e => {
                          const updated = [...members];
                          updated[idx].user_id = Number(e.target.value);
                          setMembers(updated);
                        }}
                        className="w-full px-2.5 py-1.5 bg-white border border-slate-200 rounded text-xs"
                      >
                        {dbState.users
                          .filter(u => u.active_status && (u.role_id === 1 || u.role_id === 5 || u.desk_id === currentUser.desk_id))
                          .map(u => (
                            <option key={u.user_id} value={u.user_id}>
                              {u.full_name} ({u.email})
                            </option>
                          ))}
                      </select>
                    </div>
                    <div className="w-48">
                      <select
                        value={m.role_in_team}
                        onChange={e => {
                          const updated = [...members];
                          updated[idx].role_in_team = e.target.value as TeamRole;
                          setMembers(updated);
                        }}
                        className="w-full px-2.5 py-1.5 bg-white border border-slate-200 rounded text-xs font-mono"
                      >
                        <option value="LEAD_REPORTER">Phóng viên chính (LEAD_REPORTER)</option>
                        <option value="PHOTO_REPORTER">Phóng viên ảnh (PHOTO_REPORTER)</option>
                        <option value="CAMERAMAN">Quay phim (CAMERAMAN)</option>
                        <option value="ASSISTANT">Trợ lý tác nghiệp (ASSISTANT)</option>
                      </select>
                    </div>
                    {members.length > 1 && (
                      <button
                        onClick={() => handleRemoveMember(idx)}
                        className="p-1.5 text-slate-400 hover:text-rose-600 rounded cursor-pointer"
                        title="Xóa"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>

                  {/* Schedule Conflict Warning */}
                  {conflicts.length > 0 ? (
                    <div className="text-[11px] text-amber-800 bg-amber-50 border border-amber-200 rounded px-2 py-0.5 flex items-center gap-1 font-medium">
                      <span>⚠️ Bận lịch ({conflicts.length} đề tài):</span>
                      <span className="truncate">
                        {conflicts.map(c => `${c.topic_code || '#' + c.topic_id} (${c.topic_title})`).join('; ')}
                      </span>
                    </div>
                  ) : (
                    <div className="text-[10px] text-emerald-700 bg-emerald-50 rounded px-2 py-0.5 flex items-center gap-1">
                      <span>✅ Lịch tác nghiệp trống: Sẵn sàng nhận việc</span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        <div className="flex items-center justify-between pt-4 border-t border-slate-100">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg cursor-pointer"
          >
            Hủy
          </button>
          <div className="flex items-center gap-2">
            <button
              onClick={() => handleSave(false)}
              className="px-3.5 py-2 bg-slate-200 hover:bg-slate-300 text-slate-800 font-semibold rounded-lg cursor-pointer"
            >
              Lưu phân công
            </button>
            <button
              onClick={() => handleSave(true)}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg shadow-xs flex items-center gap-1.5 cursor-pointer"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Giao thẳng cho PV (Duyệt ngay)</span>
            </button>
          </div>
        </div>
      </div>
    </Modal>
  );
};
