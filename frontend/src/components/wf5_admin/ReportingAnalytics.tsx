/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.5: Reporting and Operational Analytics (Cường - WF-05)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { BarChart3, Download, FileSpreadsheet, PieChart, TrendingUp, DollarSign } from 'lucide-react';

export const ReportingAnalytics: React.FC = () => {
  const [dbState] = useState(dbStore.getState());

  const topics = dbState.topics;
  const totalTopics = topics.length;

  const approvedTopics = topics.filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS');
  const rejectedTopics = topics.filter(t => t.status === 'REJECTED');
  const submittedTopics = topics.filter(t => t.status === 'SUBMITTED' || t.status === 'PENDING_EXECUTIVE_APPROVAL');

  const approvalRate = totalTopics > 0 ? Math.round((approvedTopics.length / totalTopics) * 100) : 0;
  const totalEstimatesAmount = dbState.topicEstimates.reduce((sum, e) => sum + Number(e.amount), 0);

  // Export to CSV
  const exportCsv = () => {
    let csv = 'TopicID,TopicCode,Title,Status,Category,Author,Deadline\n';
    topics.forEach(t => {
      const cat = dbState.categories.find(c => c.category_id === t.category_id)?.category_name || '';
      const author = dbState.users.find(u => u.user_id === t.author_user_id)?.full_name || '';
      csv += `${t.topic_id},"${t.topic_code || ''}","${t.title.replace(/"/g, '""')}","${t.status}","${cat}","${author}","${t.deadline || ''}"\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `NTMCS_Report_Topics_${new Date().toISOString().substring(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Báo cáo & Thống kê</span>
            <span>·</span>
            <span>SCR-5.5</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Báo cáo Tình hình Sản xuất & Thẩm định Toàn Tòa soạn
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Dữ liệu tổng hợp thực tế từ SQL Server phục vụ công tác giao ban tòa soạn và hoạch định nguồn lực.
          </p>
        </div>

        <button
          onClick={exportCsv}
          className="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-black text-white text-xs font-semibold rounded-lg shadow-xs transition-colors shrink-0"
        >
          <Download className="w-4 h-4" />
          <span>Xuất Báo cáo CSV (Excel)</span>
        </button>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Tổng đề tài đăng ký</span>
            <BarChart3 className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-900">{totalTopics}</div>
          <div className="text-[11px] text-slate-400 mt-1">Toàn bộ kỳ sản xuất</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Tỷ lệ phê duyệt Rubric</span>
            <TrendingUp className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-700">{approvalRate}%</div>
          <div className="text-[11px] text-emerald-600 mt-1">{approvedTopics.length} đề tài thông qua</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Tổng kinh phí dự toán</span>
            <DollarSign className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-xl font-bold font-mono text-blue-700">{totalEstimatesAmount.toLocaleString('vi-VN')} đ</div>
          <div className="text-[11px] text-blue-600 mt-1">{dbState.topicEstimates.length} khoản mục phát sinh</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Lượt mượn xe máy & thiết bị</span>
            <FileSpreadsheet className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-700">{dbState.assetRequests.length}</div>
          <div className="text-[11px] text-amber-600 mt-1">Được điều phối hiện trường</div>
        </div>
      </div>

      {/* Breakdown by Desk Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <div className="px-4 py-3 bg-slate-50 border-b border-slate-200 font-semibold text-slate-700">
          Phân bổ đề tài theo từng Ban chuyên môn (Desk Breakdown)
        </div>
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-slate-50/50 border-b border-slate-200 text-slate-500 font-semibold text-[11px]">
              <th className="py-2.5 px-4">Tên Ban chuyên môn</th>
              <th className="py-2.5 px-4 text-center">Tổng đề xuất</th>
              <th className="py-2.5 px-4 text-center">Đã duyệt</th>
              <th className="py-2.5 px-4 text-center">Yêu cầu sửa</th>
              <th className="py-2.5 px-4 text-center">Từ chối</th>
              <th className="py-2.5 px-4 text-right">Tổng dự toán (VNĐ)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {dbState.desks.map(desk => {
              const catIds = dbState.categories.filter(c => c.desk_id === desk.desk_id).map(c => c.category_id);
              const deskTopics = dbState.topics.filter(t => catIds.includes(t.category_id));

              const app = deskTopics.filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS').length;
              const rev = deskTopics.filter(t => t.status === 'REVISION_REQUESTED').length;
              const rej = deskTopics.filter(t => t.status === 'REJECTED').length;

              const topicIds = deskTopics.map(t => t.topic_id);
              const cost = dbState.topicEstimates
                .filter(e => topicIds.includes(e.topic_id))
                .reduce((s, e) => s + Number(e.amount), 0);

              return (
                <tr key={desk.desk_id} className="hover:bg-slate-50">
                  <td className="py-3 px-4 font-semibold text-slate-900">{desk.desk_name}</td>
                  <td className="py-3 px-4 text-center font-mono font-medium">{deskTopics.length}</td>
                  <td className="py-3 px-4 text-center font-mono text-emerald-700 font-bold">{app}</td>
                  <td className="py-3 px-4 text-center font-mono text-orange-700 font-bold">{rev}</td>
                  <td className="py-3 px-4 text-center font-mono text-rose-700 font-bold">{rej}</td>
                  <td className="py-3 px-4 text-right font-mono font-semibold text-slate-900">
                    {cost.toLocaleString('vi-VN')} đ
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
