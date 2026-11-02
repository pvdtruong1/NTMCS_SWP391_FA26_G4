/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.1: Executive Dashboard (Bảng chỉ huy điều hành tòa soạn)
 * Enhanced with Comprehensive File Export (.CSV / Excel & Print Report) for Business Administrator
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { TopicStatusBadge } from '../common/StatusBadge';
import {
  BarChart3,
  TrendingUp,
  ShieldAlert,
  Award,
  Users,
  Box,
  Truck,
  ArrowRight,
  CheckCircle2,
  Download,
  FileSpreadsheet,
  Printer,
  Calendar,
  Layers,
  ChevronDown,
} from 'lucide-react';

interface ExecutiveDashboardProps {
  onNavigate: (screen: string) => void;
}

export const ExecutiveDashboard: React.FC<ExecutiveDashboardProps> = ({ onNavigate }) => {
  const [dbState] = useState(dbStore.getState());
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const totalTopics = dbState.topics.length;
  const approvedTopics = dbState.topics.filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS').length;
  const pendingExecutive = dbState.topics.filter(t => t.status === 'PENDING_EXECUTIVE_APPROVAL');
  const totalAssets = dbState.assets.length;
  const activeAssets = dbState.assets.filter(a => a.asset_status === 'CHECKED_OUT').length;
  const totalCost = dbState.topicEstimates.reduce((sum, e) => sum + Number(e.amount), 0);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // Export CSV Report
  const handleExportCSV = () => {
    const today = new Date().toISOString().substring(0, 10);
    const lines: string[] = [];

    // Header
    lines.push('BÁO CÁO ĐIỀU HÀNH & SẢN XUẤT ĐỀ TÀI TOÀN TÒA SOẠN (NTMCS)');
    lines.push(`Thời điểm trích xuất: ${new Date().toLocaleString('vi-VN')}`);
    lines.push(`Người xuất: Quản trị viên Nghiệp vụ Tòa soạn`);
    lines.push('');

    // Summary Section
    lines.push('--- 1. CHỈ SỐ TỔNG QUAN ---');
    lines.push(`Tổng số đề tài toàn tòa soạn,${totalTopics}`);
    lines.push(`Đề tài đã thông qua phê duyệt / Đang tác nghiệp,${approvedTopics}`);
    lines.push(`Đề tài có rủi ro pháp lý cao (Chờ Tổng Biên Tập duyệt),${pendingExecutive.length}`);
    lines.push(`Tổng dự toán kinh phí tác nghiệp,${totalCost} VNĐ`);
    lines.push(`Phương tiện & máy móc đang hoạt động,${activeAssets} / ${totalAssets}`);
    lines.push('');

    // Quota Section
    lines.push('--- 2. TÌNH HÌNH THỰC HIỆN ĐỊNH MỨC CÁC BAN CHUYÊN MÔN ---');
    lines.push('Mã Ban,Tên Ban Chuyên Môn,Chỉ Tiêu Định Mức,Thực Tế Đã Đạt,Tỷ Lệ Hoàn Thành (%)');
    dbState.desks.forEach(desk => {
      const quota = dbState.deskQuotas.find(q => q.desk_id === desk.desk_id && q.period === '2026-10');
      const target = quota ? quota.quota_count : 15;
      const categoriesInDesk = dbState.categories.filter(c => c.desk_id === desk.desk_id).map(c => c.category_id);
      const actualCount = dbState.topics.filter(
        t => categoriesInDesk.includes(t.category_id) && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS')
      ).length;
      const percent = Math.min(100, Math.round((actualCount / target) * 100));
      lines.push(`DESK-${desk.desk_id},"${desk.desk_name}",${target},${actualCount},${percent}%`);
    });
    lines.push('');

    // Topics Detail Section
    lines.push('--- 3. DANH MỤC CHI TIẾT CÁC ĐỀ TÀI ---');
    lines.push('Mã Đề Tài,Tiêu Đề Đề Tài,Góc Tiếp Cận,Chuyên Mục,Ban Phụ Trách,Tác Giả / Phóng Viên,Hạn Chót,Trạng Thái,Dự Toán (VNĐ)');
    dbState.topics.forEach(t => {
      const cat = dbState.categories.find(c => c.category_id === t.category_id);
      const desk = dbState.desks.find(d => d.desk_id === cat?.desk_id);
      const author = dbState.users.find(u => u.user_id === t.author_user_id);
      const estimates = dbState.topicEstimates.filter(e => e.topic_id === t.topic_id);
      const cost = estimates.reduce((s, e) => s + Number(e.amount), 0);

      lines.push([
        t.topic_code || `TOPIC-${t.topic_id}`,
        `"${t.title.replace(/"/g, '""')}"`,
        `"${(t.angle || '').replace(/"/g, '""')}"`,
        `"${cat?.category_name || ''}"`,
        `"${desk?.desk_name || ''}"`,
        `"${author?.full_name || ''}"`,
        t.deadline || '---',
        t.status,
        cost,
      ].join(','));
    });

    const csvContent = '\uFEFF' + lines.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `NTMCS_BangChiHuy_DieuHanh_${today}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Đã xuất file báo cáo Bảng chỉ huy điều hành (.CSV) thành công!');
  };

  const handlePrint = () => {
    window.print();
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Quản trị Nghiệp vụ Tòa soạn & Lãnh đạo</span>
            <span>·</span>
            <span className="font-semibold text-blue-600">SCR-5.1</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Bảng Chỉ huy Điều hành Toàn diện Tòa soạn (Executive Dashboard)
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Giám sát thời gian thực toàn bộ chuỗi sản xuất nội dung, định mức các Ban và an toàn pháp lý.
          </p>
        </div>

        {/* Export File Actions */}
        <div className="flex items-center gap-2">
          <button
            onClick={handlePrint}
            className="px-3.5 py-2 bg-white hover:bg-slate-50 border border-slate-300 text-slate-700 font-semibold text-xs rounded-lg shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer"
            title="In ấn hoặc lưu dạng PDF"
          >
            <Printer className="w-3.5 h-3.5 text-slate-500" />
            <span>In / PDF</span>
          </button>

          <button
            onClick={handleExportCSV}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer"
            title="Xuất dữ liệu toàn diện ra file Excel/CSV"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Xuất file Bảng chỉ huy (.csv)</span>
          </button>
        </div>
      </div>

      {toastMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-semibold flex items-center gap-2 shadow-xs">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Quy mô Đề tài sản xuất</span>
            <BarChart3 className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-900">{totalTopics}</div>
          <div className="text-[11px] text-slate-500 mt-1">{approvedTopics} đề tài đã thông qua thẩm định</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-purple-200 bg-purple-50/20 shadow-xs">
          <div className="flex items-center justify-between text-purple-700 text-xs mb-1">
            <span>Chờ TBT Thẩm định vượt cấp</span>
            <ShieldAlert className="w-4 h-4 text-purple-700" />
          </div>
          <div className="text-2xl font-bold font-mono text-purple-800">{pendingExecutive.length}</div>
          <div className="text-[11px] text-purple-600 mt-1">Rủi ro pháp lý &ge; 8.0 cần quyết định</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Tổng ngân sách dự toán</span>
            <TrendingUp className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-xl font-bold font-mono text-emerald-700">{totalCost.toLocaleString('vi-VN')} đ</div>
          <div className="text-[11px] text-emerald-600 mt-1">Dành cho tác nghiệp & hiện trường</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Năng lực phương tiện & xe</span>
            <Truck className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-blue-700">{activeAssets} / {totalAssets}</div>
          <div className="text-[11px] text-blue-600 mt-1">Phương tiện đang bám sát hiện trường</div>
        </div>
      </div>

      {/* Desk Quota Progress Breakdown */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-slate-900 text-sm flex items-center gap-2">
            <Layers className="w-4 h-4 text-blue-600" />
            Tình hình Thực hiện Định mức Sản xuất Đề tài theo Ban (Kỳ 2026-10)
          </h3>
          <button
            onClick={() => onNavigate('scr_5_2')}
            className="text-xs text-blue-600 hover:text-blue-700 font-semibold inline-flex items-center gap-1 cursor-pointer"
          >
            Quản trị định mức (SCR-5.2) <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2">
          {dbState.desks.map(desk => {
            const quota = dbState.deskQuotas.find(q => q.desk_id === desk.desk_id && q.period === '2026-10');
            const target = quota ? quota.quota_count : 15;
            const categoriesInDesk = dbState.categories.filter(c => c.desk_id === desk.desk_id).map(c => c.category_id);
            const actualCount = dbState.topics.filter(
              t => categoriesInDesk.includes(t.category_id) && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS')
            ).length;
            const percent = Math.min(100, Math.round((actualCount / target) * 100));

            return (
              <div key={desk.desk_id} className="p-3.5 rounded-lg border border-slate-200 bg-slate-50/60 space-y-2">
                <div className="flex justify-between items-center text-xs">
                  <span className="font-bold text-slate-800">{desk.desk_name}</span>
                  <span className="font-mono text-slate-700 font-bold">
                    {actualCount} / {target} ({percent}%)
                  </span>
                </div>
                <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-300 ${
                      percent >= 80 ? 'bg-emerald-600' : percent >= 50 ? 'bg-blue-600' : 'bg-amber-500'
                    }`}
                    style={{ width: `${percent}%` }}
                  />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* High-Risk topics requiring Executive Action */}
      {pendingExecutive.length > 0 && (
        <div className="bg-purple-50/60 rounded-xl border border-purple-200 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-purple-950 text-sm flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 text-purple-700" />
              Đề tài có rủi ro pháp lý cao (Legal Risk &ge; 8.0) chờ Tổng Biên Tập quyết định
            </h3>
            <button
              onClick={() => onNavigate('scr_2_2')}
              className="text-xs text-purple-800 hover:text-purple-950 font-bold underline cursor-pointer"
            >
              Mở hàng đợi phê duyệt
            </button>
          </div>

          <div className="space-y-2 text-xs">
            {pendingExecutive.map(t => {
              const cat = dbState.categories.find(c => c.category_id === t.category_id);
              const author = dbState.users.find(u => u.user_id === t.author_user_id);

              return (
                <div key={t.topic_id} className="p-3 bg-white rounded-lg border border-purple-200 flex items-center justify-between">
                  <div>
                    <div className="font-bold text-slate-900">{t.title}</div>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      Góc nhìn: {t.angle} · Phóng viên: {author?.full_name} · {cat?.category_name}
                    </div>
                  </div>
                  <button
                    onClick={() => onNavigate('scr_2_2')}
                    className="px-3 py-1.5 bg-purple-700 hover:bg-purple-800 text-white rounded font-semibold text-xs shadow-2xs cursor-pointer"
                  >
                    Thẩm định ngay
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Grid of Workflows shortcuts */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-xs">
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <h4 className="font-semibold text-slate-900 text-sm">WF-02 · Ban Biên Tập</h4>
          <p className="text-slate-500">Thẩm định theo 4 tiêu chí rubric, giao đề tài định hướng và cân bằng tải phóng viên.</p>
          <button
            onClick={() => onNavigate('scr_2_1')}
            className="text-blue-600 font-semibold flex items-center gap-1 hover:underline cursor-pointer"
          >
            Vào bàn BTV <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <h4 className="font-semibold text-slate-900 text-sm">WF-03 · Hậu cần & Xe máy</h4>
          <p className="text-slate-500">Quản lý kho thiết bị máy quay, xe chuyên dụng và giải quyết xung đột lịch công tác.</p>
          <button
            onClick={() => onNavigate('scr_3_1')}
            className="text-blue-600 font-semibold flex items-center gap-1 hover:underline cursor-pointer"
          >
            Vào kho hậu cần <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <h4 className="font-semibold text-slate-900 text-sm">WF-04 · Hiện trường & SLA</h4>
          <p className="text-slate-500">Giám sát Kanban hiện trường, tiếp nhận báo cáo sự cố và nhật ký tỷ lệ đọc thông báo.</p>
          <button
            onClick={() => onNavigate('scr_4_1')}
            className="text-blue-600 font-semibold flex items-center gap-1 hover:underline cursor-pointer"
          >
            Vào bảng hiện trường <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  );
};
