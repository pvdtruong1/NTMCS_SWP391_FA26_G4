/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.4: Phiếu Đề Xuất Đề Tài Mới (NTMCS)
 * Adheres strictly to the requested user HTML structure, CSS classes, fieldsets, and table interactions.
 */

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, TeamRole } from '../../types';

interface TopicProposalFormProps {
  topicToEdit?: Topic | null;
  onSuccess: (topic: Topic) => void;
  onCancel: () => void;
}

export const TopicProposalForm: React.FC<TopicProposalFormProps> = ({
  topicToEdit,
  onSuccess,
  onCancel,
}) => {
  const { currentUser } = useAuth();
  const dbState = dbStore.getState();

  const [title, setTitle] = useState(topicToEdit?.title || '');
  const [angle, setAngle] = useState(topicToEdit?.angle || '');
  const [categoryId, setCategoryId] = useState<string>(topicToEdit?.category_id ? String(topicToEdit.category_id) : '1');
  const [deadline, setDeadline] = useState(topicToEdit?.deadline || '2026-11-15');

  // Vệ tinh: Dự toán kinh phí (Topic_Estimate)
  const [estimates, setEstimates] = useState<Array<{ cost_item: string; amount: string }>>(() => {
    if (topicToEdit) {
      const existing = dbState.topicEstimates.filter(e => e.topic_id === topicToEdit.topic_id);
      if (existing.length > 0) {
        return existing.map(e => ({ cost_item: e.cost_item, amount: String(e.amount) }));
      }
    }
    return [
      { cost_item: 'Chi phí di chuyển & nhiên liệu xe hiện trường', amount: '1500000' },
      { cost_item: 'Bồi dưỡng cộng tác viên & trinh sát địa bàn', amount: '2000000' }
    ];
  });

  // Vệ tinh: Thành viên tổ tác nghiệp (Topic_Member)
  const [members, setMembers] = useState<Array<{ user_id: string; role_in_team: TeamRole }>>(() => {
    if (topicToEdit) {
      const existing = dbState.topicMembers.filter(m => m.topic_id === topicToEdit.topic_id);
      if (existing.length > 0) {
        return existing.map(m => ({ user_id: String(m.user_id), role_in_team: m.role_in_team }));
      }
    }
    return [
      { user_id: '2', role_in_team: 'LEAD_REPORTER' },
      { user_id: '4', role_in_team: 'PHOTO_REPORTER' }
    ];
  });

  const [duplicateWarning, setDuplicateWarning] = useState<Topic[]>([]);
  const [alertSuccess, setAlertSuccess] = useState<string | null>(null);
  const [alertError, setAlertError] = useState<string | null>(null);

  // Live duplicate checking by keyword only (SCR-1.3)
  useEffect(() => {
    if (title.trim().length >= 4) {
      const found = dbStore.searchDuplicateTopics(title).filter(
        t => (topicToEdit ? t.topic_id !== topicToEdit.topic_id : true)
      );
      setDuplicateWarning(found);
    } else {
      setDuplicateWarning([]);
    }
  }, [title, topicToEdit]);

  // Thêm dòng mới cho phần dự toán chi phí
  const addEstimateRow = () => {
    setEstimates([...estimates, { cost_item: '', amount: '500000' }]);
  };

  // Xóa dòng dự toán
  const removeEstimateRow = (index: number) => {
    if (estimates.length > 1) {
      setEstimates(estimates.filter((_, i) => i !== index));
    } else {
      alert('Phải giữ lại ít nhất một dòng dữ liệu.');
    }
  };

  // Thêm dòng mới cho phần thành viên tác nghiệp
  const addMemberRow = () => {
    setMembers([...members, { user_id: '1', role_in_team: 'ASSISTANT' }]);
  };

  // Xóa dòng thành viên
  const removeMemberRow = (index: number) => {
    if (members.length > 1) {
      setMembers(members.filter((_, i) => i !== index));
    } else {
      alert('Phải giữ lại ít nhất một dòng dữ liệu.');
    }
  };

  // Save as DRAFT or SUBMITTED
  const handleSaveTopic = (status: 'DRAFT' | 'SUBMITTED') => {
    setAlertError(null);

    if (!title.trim()) {
      setAlertError('Vui lòng nhập tiêu đề đề tài.');
      return;
    }
    if (!categoryId) {
      setAlertError('Vui lòng chọn chuyên mục phân loại.');
      return;
    }
    if (!deadline) {
      setAlertError('Vui lòng chọn thời hạn hoàn thành dự kiến.');
      return;
    }

    try {
      const formattedMembers = members.map(m => ({
        user_id: Number(m.user_id),
        role_in_team: m.role_in_team,
      }));

      const formattedEstimates = estimates
        .filter(e => e.cost_item.trim())
        .map(e => ({
          cost_item: e.cost_item.trim(),
          amount: parseFloat(e.amount) || 0,
        }));

      // BUSINESS RULE: Biên tập viên khi giao đề tài (SCR-2.4) sẽ giao THẲNG cho phóng viên,
      // trạng thái lập tức là APPROVED / ASSIGNED mà không cần qua duyệt!
      const isEditor = currentUser.role_id === 2 || currentUser.role_id === 3;
      const finalStatus = status === 'SUBMITTED' ? (isEditor ? 'APPROVED' : 'SUBMITTED') : 'DRAFT';

      let savedTopic: Topic;
      if (topicToEdit) {
        savedTopic = dbStore.updateTopic(
          topicToEdit.topic_id,
          {
            title: title.trim(),
            angle: angle.trim(),
            category_id: Number(categoryId),
            deadline,
            status: finalStatus,
          },
          formattedMembers,
          formattedEstimates,
          currentUser.user_id
        );
      } else {
        savedTopic = dbStore.createTopic(
          {
            title: title.trim(),
            angle: angle.trim(),
            category_id: Number(categoryId),
            deadline,
            status: finalStatus,
          },
          formattedMembers,
          formattedEstimates,
          currentUser.user_id
        );
      }

      // If directly assigned by editor, ensure topic_code and notify crew
      if (isEditor && status === 'SUBMITTED') {
        if (!savedTopic.topic_code) {
          const year = new Date().getFullYear();
          const codeNum = String(savedTopic.topic_id).padStart(3, '0');
          savedTopic.topic_code = `TOPIC-${year}-${codeNum}`;
        }
        // Notify all assigned crew members
        formattedMembers.forEach(m => {
          dbStore.addNotification(
            m.user_id,
            `BTV ${currentUser.full_name} đã giao trực tiếp đề tài "${savedTopic.title}" cho bạn. Đề tài đã được duyệt thẳng và sẵn sàng tác nghiệp.`
          );
        });
      }

      setAlertSuccess(
        status === 'SUBMITTED'
          ? isEditor
            ? `BTV đã giao thẳng đề tài thành công cho phóng viên (Mã: ${savedTopic.topic_code || '#' + savedTopic.topic_id}, Trạng thái: APPROVED - Không cần qua duyệt)!`
            : 'Đề xuất đề tài đã được gửi thành công lên Ban Biên Tập (Trạng thái: SUBMITTED)!'
          : 'Đã lưu bản thảo thành công (Trạng thái: DRAFT)!'
      );
      window.scrollTo({ top: 0, behavior: 'smooth' });

      setTimeout(() => {
        onSuccess(savedTopic);
      }, 1500);
    } catch (err: any) {
      setAlertError(err.message || 'Lưu đề tài thất bại.');
    }
  };

  const totalEstimate = estimates.reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);

  return (
    <div className="max-w-[850px] mx-auto bg-white p-[30px] rounded-[8px] shadow-[0_4px_6px_rgba(0,0,0,0.1)] border border-[#d1d5db]">
      <h2 className="text-[#1e3a8a] text-xl font-bold mt-0 pb-[10px] border-b-2 border-[#e5e7eb]">
        {topicToEdit ? 'Chỉnh sửa Đề Xuất Đề Tài (SCR-1.4)' : 'SCR-1.4: Phiếu Đề Xuất Đề Tài Mới (NTMCS)'}
      </h2>

      {alertSuccess && (
        <div className="p-3 mb-5 rounded bg-[#d1fae5] text-[#065f46] border border-[#a7f3d0] font-semibold text-sm">
          {alertSuccess}
        </div>
      )}

      {alertError && (
        <div className="p-3 mb-5 rounded bg-rose-50 text-rose-800 border border-rose-200 font-semibold text-sm">
          {alertError}
        </div>
      )}

      {/* Cảnh báo trùng lặp (SCR-1.3) */}
      {duplicateWarning.length > 0 && (
        <div className="p-3 mb-5 rounded bg-amber-50 text-amber-900 border border-amber-200 text-xs space-y-1">
          <div className="font-bold">
            ⚠ Cảnh báo trùng lặp đề tài (SCR-1.3): Phát hiện {duplicateWarning.length} đề tài tương đồng:
          </div>
          <ul className="list-disc pl-5">
            {duplicateWarning.slice(0, 3).map(d => (
              <li key={d.topic_id}>
                <strong>{d.topic_code || `#${d.topic_id}`}:</strong> {d.title} ({d.status})
              </li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={(e) => { e.preventDefault(); handleSaveTopic('SUBMITTED'); }} className="space-y-5 text-sm">
        {/* Thông tin cơ bản */}
        <div className="form-group">
          <label htmlFor="title" className="block font-semibold mb-2 text-[#1f2937]">
            Tiêu đề đề tài <span className="text-red-600">*</span>
          </label>
          <input
            type="text"
            id="title"
            name="title"
            required
            placeholder="Nhập tiêu đề đề tài rõ ràng, mang tính thời sự..."
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="ntmcs-input"
          />
        </div>

        <div className="form-group">
          <label htmlFor="angle" className="block font-semibold mb-2 text-[#1f2937]">
            Góc tiếp cận / Trọng tâm
          </label>
          <textarea
            id="angle"
            name="angle"
            placeholder="Mô tả góc nhìn độc quyền, điểm nhấn nội dung..."
            value={angle}
            onChange={(e) => setAngle(e.target.value)}
            className="ntmcs-input resize-y h-[100px]"
          />
        </div>

        <div className="form-group">
          <label htmlFor="categoryId" className="block font-semibold mb-2 text-[#1f2937]">
            Chuyên mục phân loại <span className="text-red-600">*</span>
          </label>
          <select
            id="categoryId"
            name="category_id"
            required
            value={categoryId}
            onChange={(e) => setCategoryId(e.target.value)}
            className="ntmcs-input"
          >
            <option value="">-- Chọn chuyên mục --</option>
            <option value="1">Chính trị - Ngoại giao (Ban Thời sự)</option>
            <option value="2">Đô thị & Dân sinh (Ban Thời sự)</option>
            <option value="3">Tài chính - Ngân hàng (Ban Kinh tế)</option>
            <option value="4">Doanh nghiệp & Thị trường (Ban Kinh tế)</option>
            <option value="5">Điều tra tiêu cực (Ban Pháp luật)</option>
            <option value="6">Pháp đình & Trật tự an ninh (Ban Pháp luật)</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="deadline" className="block font-semibold mb-2 text-[#1f2937]">
            Thời hạn hoàn thành dự kiến <span className="text-red-600">*</span>
          </label>
          <input
            type="date"
            id="deadline"
            name="deadline"
            required
            value={deadline}
            onChange={(e) => setDeadline(e.target.value)}
            className="ntmcs-input font-mono"
          />
        </div>

        {/* Vệ tinh: Dự toán kinh phí (Topic_Estimate) */}
        <fieldset className="ntmcs-fieldset">
          <legend className="ntmcs-legend">Dự toán kinh phí tác nghiệp (Topic_Estimate)</legend>
          <table className="ntmcs-table" id="estimateTable">
            <thead>
              <tr>
                <th>Khoản mục chi phí</th>
                <th style={{ width: '220px' }}>Số tiền (VNĐ)</th>
                <th style={{ width: '80px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {estimates.map((est, idx) => (
                <tr key={idx}>
                  <td>
                    <input
                      type="text"
                      name="cost_item[]"
                      placeholder="Ví dụ: Chi phí di chuyển & nhiên liệu"
                      value={est.cost_item}
                      onChange={(e) => {
                        const updated = [...estimates];
                        updated[idx].cost_item = e.target.value;
                        setEstimates(updated);
                      }}
                      className="ntmcs-input"
                    />
                  </td>
                  <td>
                    <input
                      type="number"
                      name="amount[]"
                      placeholder="0"
                      min="0"
                      step="1000"
                      value={est.amount}
                      onChange={(e) => {
                        const updated = [...estimates];
                        updated[idx].amount = e.target.value;
                        setEstimates(updated);
                      }}
                      className="ntmcs-input font-mono"
                    />
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      className="ntmcs-btn-danger"
                      onClick={() => removeEstimateRow(idx)}
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="flex items-center justify-between mt-2">
            <button
              type="button"
              className="ntmcs-btn-secondary"
              onClick={addEstimateRow}
            >
              + Thêm khoản chi
            </button>
            <div className="font-semibold text-slate-800 text-sm">
              Tổng cộng: <span className="text-[#1e3a8a] font-mono font-bold">{totalEstimate.toLocaleString('vi-VN')} VNĐ</span>
            </div>
          </div>
        </fieldset>

        {/* Vệ tinh: Thành viên nhóm (Topic_Member) */}
        <fieldset className="ntmcs-fieldset">
          <legend className="ntmcs-legend">Thành viên tổ tác nghiệp (Topic_Member)</legend>
          <table className="ntmcs-table" id="memberTable">
            <thead>
              <tr>
                <th>Nhân sự</th>
                <th>Vai trò trong nhóm</th>
                <th style={{ width: '80px', textAlign: 'center' }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {members.map((mem, idx) => (
                <tr key={idx}>
                  <td>
                    <select
                      name="member_user_id[]"
                      value={mem.user_id}
                      onChange={(e) => {
                        const updated = [...members];
                        updated[idx].user_id = e.target.value;
                        setMembers(updated);
                      }}
                      className="ntmcs-input"
                    >
                      {dbState.users
                        .filter(u => u.active_status && (u.role_id === 1 || u.role_id === 5 || u.desk_id === currentUser.desk_id))
                        .map(u => {
                          const role = dbState.roles.find(r => r.role_id === u.role_id);
                          return (
                            <option key={u.user_id} value={u.user_id}>
                              {u.full_name} ({role?.role_name})
                            </option>
                          );
                        })}
                    </select>

                    {/* Schedule Conflict / Workload Status Indicator */}
                    {(() => {
                      const conflicts = dbStore.checkUserScheduleConflicts(Number(mem.user_id));
                      if (conflicts.length > 0) {
                        return (
                          <div className="text-[11px] text-amber-800 bg-amber-50 border border-amber-200 rounded px-2 py-1 mt-1 flex items-start gap-1 font-medium">
                            <span className="font-bold shrink-0">⚠️ Bận lịch ({conflicts.length} đề tài):</span>
                            <span className="truncate">
                              {conflicts.map(c => `${c.topic_code || '#' + c.topic_id} (${c.topic_title})`).join('; ')}
                            </span>
                          </div>
                        );
                      }
                      return (
                        <div className="text-[11px] text-emerald-700 bg-emerald-50 border border-emerald-200 rounded px-2 py-0.5 mt-1 flex items-center gap-1 font-medium">
                          <span>✅ Lịch tác nghiệp trống: Sẵn sàng nhận nhiệm vụ</span>
                        </div>
                      );
                    })()}
                  </td>
                  <td>
                    <select
                      name="role_in_team[]"
                      value={mem.role_in_team}
                      onChange={(e) => {
                        const updated = [...members];
                        updated[idx].role_in_team = e.target.value as TeamRole;
                        setMembers(updated);
                      }}
                      className="ntmcs-input font-mono"
                    >
                      <option value="LEAD_REPORTER">Phóng viên chính (Lead Reporter)</option>
                      <option value="PHOTO_REPORTER">Phóng viên ảnh (Photo Reporter)</option>
                      <option value="CAMERAMAN">Quay phim (Cameraman)</option>
                      <option value="ASSISTANT">Trợ lý (Assistant)</option>
                    </select>
                  </td>
                  <td style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      className="ntmcs-btn-danger"
                      onClick={() => removeMemberRow(idx)}
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <button
            type="button"
            className="ntmcs-btn-secondary"
            onClick={addMemberRow}
          >
            + Thêm thành viên
          </button>
        </fieldset>

        <div className="flex justify-end gap-2.5 pt-4 border-t border-[#e5e7eb]">
          <button
            type="button"
            className="px-4 py-2 border border-[#d1d5db] text-[#4b5563] hover:bg-slate-100 rounded font-semibold cursor-pointer"
            onClick={onCancel}
          >
            Hủy bỏ
          </button>
          <button
            type="button"
            className="ntmcs-btn-secondary"
            onClick={() => handleSaveTopic('DRAFT')}
          >
            Lưu bản thảo (DRAFT)
          </button>
          <button
            type="submit"
            className="ntmcs-btn-primary"
          >
            Gửi duyệt đề xuất (SUBMITTED)
          </button>
        </div>
      </form>
    </div>
  );
};
