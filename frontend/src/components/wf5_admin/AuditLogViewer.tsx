/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.8: System Audit Log Viewer (Cường - WF-05)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { History, Search, Filter, ShieldCheck, Clock } from 'lucide-react';

export const AuditLogViewer: React.FC = () => {
  const [dbState] = useState(dbStore.getState());
  const [searchQuery, setSearchQuery] = useState('');
  const [actionFilter, setActionFilter] = useState<string>('ALL');

  const logs = dbState.auditLogs;

  const filteredLogs = logs.filter(log => {
    if (actionFilter !== 'ALL' && !log.action.includes(actionFilter)) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      return (
        log.action.toLowerCase().includes(q) ||
        (log.details && log.details.toLowerCase().includes(q)) ||
        log.entity_type.toLowerCase().includes(q)
      );
    }
    return true;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Giám sát & Tuân thủ</span>
            <span>·</span>
            <span>SCR-5.8</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Nhật ký Kiểm toán Hệ thống (Audit Log)
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Ghi nhận mọi biến động trạng thái đề tài, thẩm định rubric, bàn giao tài sản và thay đổi cấu hình bảo mật.
          </p>
        </div>
      </div>

      {/* Filter toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm theo hành động hoặc nội dung kiểm toán..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600 focus:bg-white"
          />
        </div>

        <div>
          <select
            value={actionFilter}
            onChange={e => setActionFilter(e.target.value)}
            className="px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
          >
            <option value="ALL">-- Tất cả loại hành động --</option>
            <option value="TOPIC">Thao tác Đề tài (TOPIC)</option>
            <option value="ASSET">Thao tác Thiết bị (ASSET)</option>
            <option value="QUOTA">Định mức (QUOTA)</option>
            <option value="CRITERIA">Tiêu chí Rubric (CRITERIA)</option>
            <option value="SLA">Cấu hình SLA</option>
            <option value="SYSTEM">Hệ thống (SYSTEM)</option>
          </select>
        </div>
      </div>

      {/* Audit Log Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
              <th className="py-3 px-4">Log ID</th>
              <th className="py-3 px-4">Thời điểm</th>
              <th className="py-3 px-4">Người thực hiện</th>
              <th className="py-3 px-4">Hành động (Action)</th>
              <th className="py-3 px-4">Đối tượng</th>
              <th className="py-3 px-4">Nội dung chi tiết</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 font-mono">
            {filteredLogs.map(log => {
              const actor = dbState.users.find(u => u.user_id === log.actor_user_id);
              return (
                <tr key={log.log_id} className="hover:bg-slate-50/70 font-sans">
                  <td className="py-3 px-4 font-mono font-medium text-slate-500 whitespace-nowrap">
                    #{log.log_id}
                  </td>
                  <td className="py-3 px-4 font-mono text-slate-600 whitespace-nowrap text-[11px]">
                    {log.created_at}
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <span className="font-semibold text-slate-800">{actor?.full_name || `User #${log.actor_user_id}`}</span>
                  </td>
                  <td className="py-3 px-4 whitespace-nowrap">
                    <span className="px-2 py-0.5 rounded font-mono font-semibold text-[10px] bg-slate-100 border border-slate-200 text-slate-800">
                      {log.action}
                    </span>
                  </td>
                  <td className="py-3 px-4 font-mono text-slate-600 whitespace-nowrap text-[11px]">
                    {log.entity_type} {log.entity_id ? `(#${log.entity_id})` : ''}
                  </td>
                  <td className="py-3 px-4 text-slate-700 font-normal leading-relaxed">
                    {log.details || '—'}
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
