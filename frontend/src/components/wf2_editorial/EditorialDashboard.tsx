/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-2.1: Editorial Dashboard (Trường - WF-02)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { CheckSquare, ShieldAlert, Award, AlertCircle, ArrowRight, TrendingUp, Users } from 'lucide-react';

interface EditorialDashboardProps {
  onNavigate: (screen: string) => void;
  onOpenDirectedPitch: () => void;
}

export const EditorialDashboard: React.FC<EditorialDashboardProps> = ({
  onNavigate,
  onOpenDirectedPitch,
}) => {
  const [dbState] = useState(dbStore.getState());

  const topics = dbState.topics;
  const pendingDesk = topics.filter(t => t.status === 'SUBMITTED');
  const pendingExecutive = topics.filter(t => t.status === 'PENDING_EXECUTIVE_APPROVAL');
  const revision = topics.filter(t => t.status === 'REVISION_REQUESTED');
  const approved = topics.filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Ban Biên Tập</span>
            <span>·</span>
            <span>SCR-2.1</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Bảng Điều hành Thẩm định & Định hướng Báo chí
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Tổng hợp luồng thẩm định rubric, cảnh báo rủi ro pháp lý và điều hành sản xuất đề tài theo Ban.
          </p>
        </div>

        <button
          onClick={onOpenDirectedPitch}
          className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors shrink-0"
        >
          BTV giao đề tài định hướng (SCR-2.4)
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          onClick={() => onNavigate('scr_2_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Chờ BTV thẩm định</span>
            <CheckSquare className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-700">{pendingDesk.length}</div>
          <div className="text-[11px] text-amber-600 mt-1">Đề xuất mới từ phóng viên</div>
        </div>

        <div
          onClick={() => onNavigate('scr_2_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Chờ Tổng Biên Tập duyệt</span>
            <ShieldAlert className="w-4 h-4 text-purple-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-purple-700">{pendingExecutive.length}</div>
          <div className="text-[11px] text-purple-600 mt-1">Rủi ro pháp lý &ge; 8.0 (Escalated)</div>
        </div>

        <div
          onClick={() => onNavigate('scr_2_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Yêu cầu sửa đổi</span>
            <AlertCircle className="w-4 h-4 text-orange-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-orange-700">{revision.length}</div>
          <div className="text-[11px] text-orange-600 mt-1">Phóng viên đang hoàn thiện lại</div>
        </div>

        <div
          onClick={() => onNavigate('scr_2_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đã duyệt & Đang tác nghiệp</span>
            <Award className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-700">{approved.length}</div>
          <div className="text-[11px] text-emerald-600 mt-1">Đã cấp mã TOPIC chính thức</div>
        </div>
      </div>

      {/* Desk Progress vs Quota Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 text-sm flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-red-700" />
              Tình hình thực hiện Định mức Đề tài theo Ban (Desk Quota vs Actual)
            </h3>
            <button
              onClick={() => onNavigate('scr_5_2')}
              className="text-xs text-red-700 hover:text-red-800 font-medium inline-flex items-center gap-1 cursor-pointer"
            >
              Quản lý định mức <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-3 pt-2">
            {dbState.desks.map(desk => {
              const quota = dbState.deskQuotas.find(q => q.desk_id === desk.desk_id && q.period === '2026-10');
              const target = quota ? quota.quota_count : 15;

              const categoriesInDesk = dbState.categories.filter(c => c.desk_id === desk.desk_id).map(c => c.category_id);
              const actualCount = dbState.topics.filter(
                t => categoriesInDesk.includes(t.category_id) && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ACCEPTED_READY_FOR_CMS')
              ).length;

              const percent = Math.min(100, Math.round((actualCount / target) * 100));

              return (
                <div key={desk.desk_id} className="p-3 rounded-lg border border-slate-200 bg-slate-50/50 space-y-2 text-xs">
                  <div className="flex justify-between items-center">
                    <span className="font-semibold text-slate-900 text-sm">{desk.desk_name}</span>
                    <span className="font-mono text-slate-700 font-bold">
                      {actualCount} / {target} đề tài ({percent}%)
                    </span>
                  </div>
                  <div className="w-full bg-slate-200 h-2 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-300 ${
                        percent >= 80 ? 'bg-emerald-600' : percent >= 50 ? 'bg-amber-500' : 'bg-red-600'
                      }`}
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Quick action panel */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <h3 className="font-semibold text-slate-900 text-sm">Truy cập nghiệp vụ nhanh</h3>
          <div className="space-y-2 text-xs">
            <button
              onClick={() => onNavigate('scr_2_2')}
              className="w-full p-2.5 rounded-lg border border-slate-200 hover:bg-slate-50 flex items-center justify-between text-left cursor-pointer"
            >
              <div>
                <div className="font-semibold text-slate-800">Duyệt hàng đợi Rubric (SCR-2.2)</div>
                <div className="text-[11px] text-slate-500">{pendingDesk.length + pendingExecutive.length} đề tài đang chờ quyết định</div>
              </div>
              <ArrowRight className="w-4 h-4 text-slate-400" />
            </button>

            <button
              onClick={() => onNavigate('scr_2_5')}
              className="w-full p-2.5 rounded-lg border border-slate-200 hover:bg-slate-50 flex items-center justify-between text-left cursor-pointer"
            >
              <div>
                <div className="font-semibold text-slate-800">Cân bằng tải phóng viên (SCR-2.5)</div>
                <div className="text-[11px] text-slate-500">Xem khối lượng đề tài và điều phối nhân lực</div>
              </div>
              <Users className="w-4 h-4 text-slate-400" />
            </button>

            <button
              onClick={() => onNavigate('scr_2_8')}
              className="w-full p-2.5 rounded-lg border border-slate-200 hover:bg-slate-50 flex items-center justify-between text-left cursor-pointer"
            >
              <div>
                <div className="font-semibold text-slate-800">Kho lưu trữ & Tái kích hoạt (SCR-2.8)</div>
                <div className="text-[11px] text-slate-500">Xem đề tài đã từ chối hoặc đưa lại hàng đợi</div>
              </div>
              <ArrowRight className="w-4 h-4 text-slate-400" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
