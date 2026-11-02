/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.2: Desk Quota Management (Cường - WF-05)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { DeskQuota } from '../../types';
import { Target, Plus, Edit2, CheckCircle2 } from 'lucide-react';

export const DeskQuotaManagement: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());

  const [period, setPeriod] = useState('2026-10');
  const [selectedDeskId, setSelectedDeskId] = useState<number>(dbState.desks[0]?.desk_id || 1);
  const [quotaCount, setQuotaCount] = useState<number>(15);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    try {
      dbStore.setDeskQuota(selectedDeskId, period, quotaCount, currentUser.user_id);
      setSuccessMessage(`Đã thiết lập định mức kỳ ${period} cho Ban thành công (${quotaCount} đề tài).`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      alert(err.message || 'Lưu định mức thất bại.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
          <span>Quản trị Nghiệp vụ</span>
          <span>·</span>
          <span>SCR-5.2</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Quản trị Định mức Sản xuất Đề tài (Desk Quota)
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Giao chỉ tiêu sản xuất đề tài theo tháng hoặc quý cho từng Ban chuyên môn (Thời sự, Kinh tế, Pháp luật...).
        </p>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          {successMessage}
        </div>
      )}

      {/* Form */}
      <form onSubmit={handleSave} className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-4 text-xs">
        <h3 className="font-semibold text-slate-900 text-sm">Thiết lập / Cập nhật chỉ tiêu Ban</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block font-semibold text-slate-700 mb-1">Ban chuyên môn</label>
            <select
              value={selectedDeskId}
              onChange={e => setSelectedDeskId(Number(e.target.value))}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white"
            >
              {dbState.desks.map(d => (
                <option key={d.desk_id} value={d.desk_id}>
                  {d.desk_name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">Kỳ định mức (Tháng hoặc Quý)</label>
            <input
              type="text"
              required
              placeholder="VD: 2026-10, 2026-11, 2026-Q4"
              value={period}
              onChange={e => setPeriod(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg font-mono focus:bg-white"
            />
          </div>

          <div>
            <label className="block font-semibold text-slate-700 mb-1">Số lượng đề tài mục tiêu</label>
            <input
              type="number"
              min="1"
              required
              value={quotaCount}
              onChange={e => setQuotaCount(Number(e.target.value))}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg font-mono focus:bg-white font-bold"
            />
          </div>
        </div>

        <div className="flex justify-end pt-2 border-t border-slate-100">
          <button
            type="submit"
            className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white font-semibold rounded-lg shadow-xs"
          >
            Lưu định mức
          </button>
        </div>
      </form>

      {/* List of quotas */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <div className="px-4 py-3 bg-slate-50 border-b border-slate-200 font-semibold text-slate-700">
          Danh sách Định mức đã ban hành (Bảng Desk_Quota)
        </div>
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/50 border-b border-slate-200 text-slate-500 font-semibold">
              <th className="py-2.5 px-4">Ban chuyên môn</th>
              <th className="py-2.5 px-4">Kỳ áp dụng</th>
              <th className="py-2.5 px-4">Định mức (Đề tài)</th>
              <th className="py-2.5 px-4">Tiến độ thực tế</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {dbState.deskQuotas.map(q => {
              const desk = dbState.desks.find(d => d.desk_id === q.desk_id);
              const catIds = dbState.categories.filter(c => c.desk_id === q.desk_id).map(c => c.category_id);
              const actual = dbState.topics.filter(
                t => catIds.includes(t.category_id) && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS')
              ).length;
              const percent = Math.min(100, Math.round((actual / q.quota_count) * 100));

              return (
                <tr key={q.quota_id} className="hover:bg-slate-50">
                  <td className="py-3 px-4 font-semibold text-slate-900">{desk?.desk_name}</td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-700">{q.period}</td>
                  <td className="py-3 px-4 font-mono font-bold text-slate-900">{q.quota_count} đề tài</td>
                  <td className="py-3 px-4">
                    <span className="font-mono text-slate-600 font-medium">{actual} đề tài ({percent}%)</span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
