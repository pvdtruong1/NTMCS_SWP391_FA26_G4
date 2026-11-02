/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.1: Reporter Dashboard (Hưng - WF-01)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { TopicStatusBadge } from '../common/StatusBadge';
import {
  FileText,
  Clock,
  CheckCircle2,
  AlertCircle,
  Plus,
  Search,
  ArrowRight,
  Bell,
  Calendar,
} from 'lucide-react';

interface ReporterDashboardProps {
  onNavigate: (screen: string) => void;
  onOpenCreate: () => void;
  onSelectTopic: (topicId: number) => void;
}

export const ReporterDashboard: React.FC<ReporterDashboardProps> = ({
  onNavigate,
  onOpenCreate,
  onSelectTopic,
}) => {
  const { currentUser } = useAuth();
  const [dbState] = useState(dbStore.getState());

  const myTopics = dbState.topics.filter(t => t.author_user_id === currentUser.user_id);
  const drafts = myTopics.filter(t => t.status === 'DRAFT');
  const submitted = myTopics.filter(t => t.status === 'SUBMITTED' || t.status === 'PENDING_EXECUTIVE_APPROVAL');
  const revision = myTopics.filter(t => t.status === 'REVISION_REQUESTED');
  const approved = myTopics.filter(t => t.status === 'APPROVED' || t.status === 'IN_PROGRESS');

  const myNotifications = dbState.notifications.filter(n => n.user_id === currentUser.user_id).slice(0, 4);

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Bàn làm việc Phóng viên</span>
            <span>·</span>
            <span>SCR-1.1</span>
            <span>·</span>
            <span className="font-semibold text-slate-700">{currentUser.full_name}</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Tổng quan Đề tài & Kế hoạch Tác nghiệp
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Theo dõi tiến độ duyệt đề tài, yêu cầu sửa đổi từ BTV và lịch trình nộp bản thảo.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => onNavigate('scr_1_3')}
            className="px-3.5 py-2 bg-white border border-slate-300 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg shadow-2xs flex items-center gap-1.5"
          >
            <Search className="w-4 h-4" /> Tra cứu trùng lặp (SCR-1.3)
          </button>
          <button
            onClick={onOpenCreate}
            className="px-4 py-2 bg-red-700 hover:bg-red-800 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Đăng ký đề tài (SCR-1.4)
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          onClick={() => onNavigate('scr_1_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Bản thảo dở dang</span>
            <FileText className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-900">{drafts.length}</div>
          <div className="text-[11px] text-slate-400 mt-1">Chưa nộp cho Ban Biên Tập</div>
        </div>

        <div
          onClick={() => onNavigate('scr_1_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đang chờ duyệt</span>
            <Clock className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-700">{submitted.length}</div>
          <div className="text-[11px] text-amber-600 mt-1">Trong hàng đợi thẩm định Rubric</div>
        </div>

        <div
          onClick={() => onNavigate('scr_1_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Cần chỉnh sửa (Feedback)</span>
            <AlertCircle className="w-4 h-4 text-orange-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-orange-700">{revision.length}</div>
          <div className="text-[11px] text-orange-600 mt-1">BTV yêu cầu bổ sung thông tin</div>
        </div>

        <div
          onClick={() => onNavigate('scr_1_2')}
          className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:border-slate-300 transition-colors"
        >
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đã duyệt / Đang tác nghiệp</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-700">{approved.length}</div>
          <div className="text-[11px] text-emerald-600 mt-1">Được cấp mã đề tài chính thức</div>
        </div>
      </div>

      {/* Grid: Ongoing topics & Notifications */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Active Topics */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 text-sm">
              Đề tài cần quan tâm xử lý gần đây
            </h3>
            <button
              onClick={() => onNavigate('scr_1_2')}
              className="text-xs text-red-700 hover:text-red-800 font-medium inline-flex items-center gap-1 cursor-pointer"
            >
              Xem tất cả ({myTopics.length}) <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-2.5">
            {myTopics.slice(0, 4).map(topic => {
              const cat = dbState.categories.find(c => c.category_id === topic.category_id);
              return (
                <div
                  key={topic.topic_id}
                  onClick={() => onNavigate('scr_1_2')}
                  className="p-3 rounded-lg border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-colors cursor-pointer text-xs space-y-1.5"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-bold text-slate-800">
                      {topic.topic_code || `#${topic.topic_id}`}
                    </span>
                    <TopicStatusBadge status={topic.status} />
                  </div>
                  <div className="font-semibold text-slate-900 text-sm leading-tight">
                    {topic.title}
                  </div>
                  <div className="flex items-center justify-between text-slate-500 text-[11px] pt-1 border-t border-slate-100">
                    <span>{cat?.category_name}</span>
                    <span className="font-mono flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-slate-400" /> Hạn: {topic.deadline || 'Chưa định'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Notifications */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 text-sm flex items-center gap-1.5">
              <Bell className="w-4 h-4 text-slate-400" />
              Thông báo gửi đến bạn
            </h3>
          </div>

          <div className="space-y-2 text-xs">
            {myNotifications.length === 0 ? (
              <div className="py-6 text-center text-slate-400">Chưa có thông báo mới.</div>
            ) : (
              myNotifications.map(n => (
                <div
                  key={n.notification_id}
                  className={`p-2.5 rounded-lg border ${
                    n.is_read ? 'bg-white border-slate-200 text-slate-600' : 'bg-red-50/50 border-red-200 text-slate-900 font-medium'
                  }`}
                >
                  <p className="leading-relaxed">{n.message}</p>
                  <div className="text-[10px] text-slate-400 mt-1 font-mono">{n.created_at || 'Vừa xong'}</div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
