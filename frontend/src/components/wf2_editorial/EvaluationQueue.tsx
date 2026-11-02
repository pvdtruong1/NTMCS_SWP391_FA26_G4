/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-2.2: Evaluation Queue (Trường - WF-02)
 */

import React, { useState, useMemo } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, TopicStatus } from '../../types';
import { TopicStatusBadge } from '../common/StatusBadge';
import { RubricEvaluationModal } from './RubricEvaluationModal';
import { ReporterAssignmentModal } from './ReporterAssignmentModal';
import { TopicDetailModal } from '../wf1_reporter/TopicDetailModal';
import {
  Search,
  CheckSquare,
  Award,
  Users,
  Eye,
  Filter,
  RotateCcw,
  ShieldAlert,
  ChevronLeft,
  ChevronRight,
  Clock,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';

export const EvaluationQueue: React.FC = () => {
  const { currentUser, currentRole } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());

  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('PENDING_ALL');
  const [deskFilter, setDeskFilter] = useState<string>('ALL');

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 6;

  // Tabs state: Rubric queue vs Deadline extensions review
  const [activeTab, setActiveTab] = useState<'RUBRIC_QUEUE' | 'DEADLINE_EXTENSIONS'>('RUBRIC_QUEUE');

  // Modals state
  const [evaluatingTopic, setEvaluatingTopic] = useState<Topic | null>(null);
  const [assigningTopic, setAssigningTopic] = useState<Topic | null>(null);
  const [detailTopic, setDetailTopic] = useState<Topic | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const pendingExtensions = dbState.extensions.filter(e => e.status === 'PENDING');

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const isExecutive = currentRole.role_name === 'EXECUTIVE_EDITOR' || currentRole.role_name === 'SYSTEM_ADMINISTRATOR';

  const filteredTopics = useMemo(() => {
    let list = [...dbState.topics];

    if (statusFilter === 'PENDING_ALL') {
      list = list.filter(
        t => t.status === 'SUBMITTED' || t.status === 'PENDING_EXECUTIVE_APPROVAL' || t.status === 'REVISION_REQUESTED'
      );
    } else if (statusFilter !== 'ALL') {
      list = list.filter(t => t.status === statusFilter);
    }

    if (deskFilter !== 'ALL') {
      const targetDeskId = Number(deskFilter);
      const categoryIdsInDesk = dbState.categories
        .filter(c => c.desk_id === targetDeskId)
        .map(c => c.category_id);
      list = list.filter(t => categoryIdsInDesk.includes(t.category_id));
    }

    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      list = list.filter(
        t =>
          t.title.toLowerCase().includes(q) ||
          (t.topic_code && t.topic_code.toLowerCase().includes(q)) ||
          (t.angle && t.angle.toLowerCase().includes(q))
      );
    }

    return list;
  }, [dbState.topics, statusFilter, deskFilter, searchQuery, dbState.categories]);

  const totalPages = Math.ceil(filteredTopics.length / pageSize) || 1;
  const paginatedTopics = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredTopics.slice(start, start + pageSize);
  }, [filteredTopics, currentPage, pageSize]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Ban Biên Tập</span>
            <span>·</span>
            <span>SCR-2.2</span>
            <span>·</span>
            <span>{isExecutive ? 'Hội đồng Tổng Biên Tập' : 'Bàn Thẩm định BTV'}</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Hàng đợi Thẩm định & Phê duyệt Đề tài
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Xem xét các đề xuất đề tài mới từ phóng viên, thẩm định theo rubric 4 tiêu chuẩn và kích hoạt phân luồng rủi ro.
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 gap-2">
        <button
          onClick={() => setActiveTab('RUBRIC_QUEUE')}
          className={`pb-2.5 px-3 text-xs font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer ${
            activeTab === 'RUBRIC_QUEUE'
              ? 'border-blue-600 text-blue-700 font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <CheckSquare className="w-4 h-4" />
          <span>Hàng đợi Thẩm định Rubric ({filteredTopics.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('DEADLINE_EXTENSIONS')}
          className={`pb-2.5 px-3 text-xs font-semibold flex items-center gap-2 border-b-2 transition-colors cursor-pointer ${
            activeTab === 'DEADLINE_EXTENSIONS'
              ? 'border-blue-600 text-blue-700 font-bold'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          <Clock className="w-4 h-4" />
          <span>Phê duyệt Gia hạn Deadline (SCR-1.8)</span>
          {pendingExtensions.length > 0 && (
            <span className="px-1.5 py-0.2 rounded-full text-[10px] bg-amber-500 text-white font-mono font-bold">
              {pendingExtensions.length}
            </span>
          )}
        </button>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center justify-between">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 hover:text-emerald-900 cursor-pointer">×</button>
        </div>
      )}

      {activeTab === 'DEADLINE_EXTENSIONS' ? (
        /* DEADLINE EXTENSIONS APPROVAL VIEW */
        <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs space-y-3 p-4">
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-slate-900 text-sm">
              Danh sách Đơn xin Gia hạn Deadline từ Phóng viên ({dbState.extensions.length} đơn):
            </h3>
            <span className="text-[11px] text-slate-500">
              Đơn chưa duyệt giữ trạng thái <strong>PENDING</strong>; sau khi duyệt hạn chót sẽ tự động cập nhật vào đề tài.
            </span>
          </div>

          <div className="border border-slate-200 rounded-lg overflow-hidden">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                  <th className="py-3 px-4">Mã đơn</th>
                  <th className="py-3 px-4">Đề tài</th>
                  <th className="py-3 px-4">Phóng viên đề xuất</th>
                  <th className="py-3 px-4">Hạn cũ → Hạn mới</th>
                  <th className="py-3 px-4 max-w-xs">Lý do giải trình</th>
                  <th className="py-3 px-4">Trạng thái</th>
                  <th className="py-3 px-4 text-right">Quyết định của BTV</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {dbState.extensions.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="py-8 text-center text-slate-400">
                      Chưa có đơn xin gia hạn nào.
                    </td>
                  </tr>
                ) : (
                  dbState.extensions.map(ext => {
                    const topic = dbState.topics.find(t => t.topic_id === ext.topic_id);
                    const reporter = dbState.users.find(u => u.user_id === ext.requested_by_user_id);

                    return (
                      <tr key={ext.request_id} className="hover:bg-slate-50/70">
                        <td className="py-3 px-4 font-mono font-medium text-slate-800 whitespace-nowrap">
                          EXT-{String(ext.request_id).padStart(3, '0')}
                        </td>
                        <td className="py-3 px-4">
                          <div className="font-semibold text-slate-900">{topic?.title}</div>
                          <div className="text-[10px] font-mono text-slate-400">{topic?.topic_code || '#' + topic?.topic_id}</div>
                        </td>
                        <td className="py-3 px-4 whitespace-nowrap">
                          <div className="font-medium text-slate-800">{reporter?.full_name}</div>
                          <div className="text-[10px] text-slate-400 font-mono">{reporter?.email}</div>
                        </td>
                        <td className="py-3 px-4 whitespace-nowrap font-mono text-[11px]">
                          <span className="text-slate-400 line-through mr-1.5">{ext.current_deadline}</span>
                          <span className="font-bold text-blue-700">→ {ext.requested_deadline}</span>
                        </td>
                        <td className="py-3 px-4 max-w-xs">
                          <p className="line-clamp-2 text-slate-600 m-0 leading-relaxed">{ext.reason}</p>
                        </td>
                        <td className="py-3 px-4 whitespace-nowrap">
                          {ext.status === 'PENDING' ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold bg-amber-50 text-amber-800 border border-amber-200">
                              <Clock className="w-3 h-3 text-amber-600" /> Chờ duyệt (Pending)
                            </span>
                          ) : ext.status === 'APPROVED' ? (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold bg-emerald-50 text-emerald-800 border border-emerald-200">
                              <CheckCircle2 className="w-3 h-3 text-emerald-600" /> Đã chấp thuận
                            </span>
                          ) : (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[11px] font-bold bg-rose-50 text-rose-800 border border-rose-200">
                              <AlertTriangle className="w-3 h-3 text-rose-600" /> Bị từ chối
                            </span>
                          )}
                        </td>
                        <td className="py-3 px-4 text-right whitespace-nowrap">
                          {ext.status === 'PENDING' ? (
                            <div className="flex items-center justify-end gap-1.5">
                              <button
                                onClick={() => {
                                  try {
                                    dbStore.decideDeadlineExtension(ext.request_id, 'APPROVED', currentUser.user_id);
                                    setSuccessMessage(`Đã phê duyệt gia hạn đề tài "${topic?.title}" đến ngày ${ext.requested_deadline}.`);
                                    setTimeout(() => setSuccessMessage(null), 3500);
                                  } catch (err: any) {
                                    alert(err.message || 'Thao tác thất bại.');
                                  }
                                }}
                                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded text-[11px] shadow-2xs cursor-pointer"
                              >
                                Chấp thuận
                              </button>
                              <button
                                onClick={() => {
                                  try {
                                    dbStore.decideDeadlineExtension(ext.request_id, 'REJECTED', currentUser.user_id);
                                    setSuccessMessage(`Đã từ chối đơn gia hạn đề tài "${topic?.title}".`);
                                    setTimeout(() => setSuccessMessage(null), 3500);
                                  } catch (err: any) {
                                    alert(err.message || 'Thao tác thất bại.');
                                  }
                                }}
                                className="px-2 py-1 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 font-semibold rounded text-[11px] border border-slate-200 cursor-pointer"
                              >
                                Từ chối
                              </button>
                            </div>
                          ) : (
                            <span className="text-[11px] text-slate-400 italic">Đã xử lý</span>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* RUBRIC EVALUATION QUEUE VIEW */
        <>
          {/* Filter toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="relative md:col-span-2">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Tìm kiếm theo tiêu đề đề tài, mã số hoặc từ khóa..."
              value={searchQuery}
              onChange={e => {
                setSearchQuery(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600 focus:bg-white"
            />
          </div>

          <div>
            <select
              value={statusFilter}
              onChange={e => {
                setStatusFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="PENDING_ALL">-- Chờ xử lý (Tất cả) --</option>
              <option value="SUBMITTED">Chờ BTV duyệt (SUBMITTED)</option>
              <option value="PENDING_EXECUTIVE_APPROVAL">Chờ TBT duyệt (ESCALATED)</option>
              <option value="REVISION_REQUESTED">Yêu cầu sửa đổi (REVISION)</option>
              <option value="APPROVED">Đã phê duyệt (APPROVED)</option>
              <option value="ALL">-- Toàn bộ trạng thái --</option>
            </select>
          </div>

          <div>
            <select
              value={deskFilter}
              onChange={e => {
                setDeskFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="ALL">-- Tất cả các Ban --</option>
              {dbState.desks.map(d => (
                <option key={d.desk_id} value={d.desk_id}>
                  {d.desk_name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs text-slate-500">
          <div>
            Hiển thị <span className="font-semibold text-slate-800">{filteredTopics.length}</span> đề tài trong danh sách thẩm định
          </div>
          {(searchQuery || statusFilter !== 'PENDING_ALL' || deskFilter !== 'ALL') && (
            <button
              onClick={() => {
                setSearchQuery('');
                setStatusFilter('PENDING_ALL');
                setDeskFilter('ALL');
                setCurrentPage(1);
              }}
              className="text-red-700 hover:text-red-800 font-medium cursor-pointer flex items-center gap-1"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Mặc định hàng đợi
            </button>
          )}
        </div>
      </div>

      {/* Queue Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Mã / ID</th>
                <th className="py-3 px-4">Tiêu đề đề tài & Góc tiếp cận</th>
                <th className="py-3 px-4">Tác giả nộp</th>
                <th className="py-3 px-4">Ban / Chuyên mục</th>
                <th className="py-3 px-4">Hạn chót</th>
                <th className="py-3 px-4">Hiện trạng</th>
                <th className="py-3 px-4 text-right">Thao tác thẩm định</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {paginatedTopics.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-10 text-center text-slate-400">
                    Không có đề tài nào đang chờ thẩm định theo điều kiện lọc.
                  </td>
                </tr>
              ) : (
                paginatedTopics.map((topic) => {
                  const author = dbState.users.find(u => u.user_id === topic.author_user_id);
                  const cat = dbState.categories.find(c => c.category_id === topic.category_id);
                  const desk = cat ? dbState.desks.find(d => d.desk_id === cat.desk_id) : null;
                  const isEscalated = topic.status === 'PENDING_EXECUTIVE_APPROVAL';

                  return (
                    <tr key={topic.topic_id} className={`hover:bg-slate-50/70 transition-colors ${
                      isEscalated ? 'bg-purple-50/20' : ''
                    }`}>
                      <td className="py-3.5 px-4 font-mono font-medium text-slate-900 whitespace-nowrap">
                        {topic.topic_code || `#${topic.topic_id}`}
                      </td>
                      <td className="py-3.5 px-4 max-w-sm">
                        <div className="font-semibold text-slate-900 text-sm leading-snug">
                          {topic.title}
                        </div>
                        {topic.angle && (
                          <div className="text-[11px] text-slate-500 mt-0.5 line-clamp-1">{topic.angle}</div>
                        )}
                        {isEscalated && (
                          <div className="mt-1 flex items-center gap-1 text-[10px] font-bold text-purple-700">
                            <ShieldAlert className="w-3.5 h-3.5" /> Cần Tổng Biên Tập thẩm định vượt cấp
                          </div>
                        )}
                      </td>
                      <td className="py-3.5 px-4 font-medium text-slate-800 whitespace-nowrap">
                        {author?.full_name}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600">
                        <div className="font-medium text-slate-800">{desk?.desk_name}</div>
                        <div className="text-[11px] text-slate-500">{cat?.category_name}</div>
                      </td>
                      <td className="py-3.5 px-4 font-mono text-slate-700 whitespace-nowrap">
                        {topic.deadline || 'Chưa định'}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <TopicStatusBadge status={topic.status} />
                      </td>
                      <td className="py-3.5 px-4 text-right whitespace-nowrap">
                        <div className="inline-flex items-center gap-1.5 justify-end">
                          <button
                            onClick={() => setDetailTopic(topic)}
                            title="Xem chi tiết"
                            className="p-1.5 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded"
                          >
                            <Eye className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => setAssigningTopic(topic)}
                            title="Phân công phóng viên & cân bằng tải (SCR-2.5)"
                            className="p-1.5 text-slate-400 hover:text-blue-700 hover:bg-blue-50 rounded"
                          >
                            <Users className="w-4 h-4" />
                          </button>

                          <button
                            onClick={() => setEvaluatingTopic(topic)}
                            title="Thẩm định chấm điểm Rubric (SCR-2.3)"
                            className="px-2.5 py-1 bg-[#1e3a8a] hover:bg-[#1e40af] text-white font-semibold rounded text-xs flex items-center gap-1 shadow-2xs"
                          >
                            <Award className="w-3.5 h-3.5" />
                            {isEscalated ? 'TBT Thẩm định' : 'Chấm Rubric'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="px-4 py-3 bg-slate-50/70 border-t border-slate-200 flex items-center justify-between text-xs text-slate-500">
          <div>
            Trang <span className="font-semibold text-slate-800">{currentPage}</span> / {totalPages}
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              disabled={currentPage === 1}
              className="p-1.5 rounded border border-slate-200 hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed text-slate-600"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(p => (
              <button
                key={p}
                onClick={() => setCurrentPage(p)}
                className={`w-7 h-7 rounded text-xs font-medium transition-colors ${
                  currentPage === p
                    ? 'bg-[#1e3a8a] text-white font-semibold'
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                {p}
              </button>
            ))}
            <button
              onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
              className="p-1.5 rounded border border-slate-200 hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed text-slate-600"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
      </>
      )}

      {/* Modals */}
      <RubricEvaluationModal
        isOpen={!!evaluatingTopic}
        onClose={() => setEvaluatingTopic(null)}
        topic={evaluatingTopic}
        isExecutiveMode={evaluatingTopic?.status === 'PENDING_EXECUTIVE_APPROVAL' || isExecutive}
        onSuccess={(msg) => {
          setSuccessMessage(msg);
          setTimeout(() => setSuccessMessage(null), 4000);
        }}
      />

      <ReporterAssignmentModal
        isOpen={!!assigningTopic}
        onClose={() => setAssigningTopic(null)}
        topic={assigningTopic}
        onSuccess={() => {
          setSuccessMessage('Đã cập nhật phân công tổ tác nghiệp thành công!');
          setTimeout(() => setSuccessMessage(null), 3000);
        }}
      />

      <TopicDetailModal
        isOpen={!!detailTopic}
        onClose={() => setDetailTopic(null)}
        topic={detailTopic}
      />
    </div>
  );
};
