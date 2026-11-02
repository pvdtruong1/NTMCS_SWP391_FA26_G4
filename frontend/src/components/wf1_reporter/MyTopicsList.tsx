/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.2: My Topics List (Hưng - WF-01)
 */

import React, { useState, useMemo } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, TopicStatus } from '../../types';
import { TopicStatusBadge } from '../common/StatusBadge';
import { TopicDetailModal } from './TopicDetailModal';
import { DeadlineExtensionModal } from './DeadlineExtensionModal';
import { AssetRequestModal } from './AssetRequestModal';
import {
  Search,
  Filter,
  Plus,
  Eye,
  Edit2,
  Send,
  RotateCcw,
  Clock,
  ChevronLeft,
  ChevronRight,
  FileText,
  Truck,
} from 'lucide-react';

interface MyTopicsListProps {
  onOpenCreate: () => void;
  onEditTopic: (topic: Topic) => void;
}

export const MyTopicsList: React.FC<MyTopicsListProps> = ({ onOpenCreate, onEditTopic }) => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());

  // Search & Filter
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 6;

  // Selected for modals
  const [detailTopic, setDetailTopic] = useState<Topic | null>(null);
  const [extensionTopic, setExtensionTopic] = useState<Topic | null>(null);
  const [feedbackNotice, setFeedbackNotice] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  // Filter topics for current user (author or team member)
  const myTopics = useMemo(() => {
    const memberTopicIds = dbState.topicMembers
      .filter(m => m.user_id === currentUser.user_id)
      .map(m => m.topic_id);

    return dbState.topics.filter(
      t => t.author_user_id === currentUser.user_id || memberTopicIds.includes(t.topic_id)
    );
  }, [dbState.topics, dbState.topicMembers, currentUser.user_id]);

  const filteredTopics = useMemo(() => {
    let result = [...myTopics];

    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      result = result.filter(
        t =>
          t.title.toLowerCase().includes(q) ||
          (t.topic_code && t.topic_code.toLowerCase().includes(q)) ||
          (t.angle && t.angle.toLowerCase().includes(q))
      );
    }

    if (statusFilter !== 'ALL') {
      result = result.filter(t => t.status === statusFilter);
    }

    if (categoryFilter !== 'ALL') {
      result = result.filter(t => t.category_id === Number(categoryFilter));
    }

    return result;
  }, [myTopics, searchQuery, statusFilter, categoryFilter]);

  const totalPages = Math.ceil(filteredTopics.length / pageSize) || 1;
  const paginatedTopics = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredTopics.slice(start, start + pageSize);
  }, [filteredTopics, currentPage, pageSize]);

  // Resubmit topic that was in REVISION_REQUESTED (SCR-1.7)
  const handleResubmit = (topic: Topic) => {
    try {
      dbStore.updateTopic(
        topic.topic_id,
        {
          title: topic.title,
          angle: topic.angle || '',
          category_id: topic.category_id,
          deadline: topic.deadline || '',
          status: 'SUBMITTED',
        },
        dbState.topicMembers.filter(m => m.topic_id === topic.topic_id),
        dbState.topicEstimates.filter(e => e.topic_id === topic.topic_id),
        currentUser.user_id
      );
      setFeedbackNotice(`Đã nộp lại đề tài "${topic.title}" cho Ban Biên Tập thẩm định thành công!`);
      setTimeout(() => setFeedbackNotice(null), 4000);
    } catch (err: any) {
      alert(err.message || 'Nộp lại thất bại.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Phóng viên</span>
            <span>·</span>
            <span>SCR-1.2</span>
            <span>·</span>
            <span>{currentUser.full_name}</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Danh sách Đề tài của Tôi
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Quản lý toàn bộ đề tài bạn chủ trì hoặc tham gia tác nghiệp hiện trường.
          </p>
        </div>

        <button
          onClick={onOpenCreate}
          className="ntmcs-btn-primary flex items-center gap-2 text-xs shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>Đăng ký đề tài mới (SCR-1.4)</span>
        </button>
      </div>

      {feedbackNotice && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium">
          {feedbackNotice}
        </div>
      )}

      {/* Filter toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div className="relative md:col-span-2">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Tìm theo tiêu đề đề tài, mã số hoặc từ khóa góc tiếp cận..."
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
              <option value="ALL">-- Tất cả trạng thái --</option>
              <option value="DRAFT">Bản thảo (DRAFT)</option>
              <option value="SUBMITTED">Chờ duyệt (SUBMITTED)</option>
              <option value="REVISION_REQUESTED">Yêu cầu sửa đổi (REVISION)</option>
              <option value="PENDING_EXECUTIVE_APPROVAL">Chờ TBT duyệt (ESCALATED)</option>
              <option value="APPROVED">Đã duyệt (APPROVED)</option>
              <option value="IN_PROGRESS">Đang tác nghiệp (IN_PROGRESS)</option>
              <option value="DELIVERED">Đã nộp bài (DELIVERED)</option>
              <option value="ACCEPTED_READY_FOR_CMS">Đạt chuẩn CMS</option>
              <option value="REJECTED">Từ chối (REJECTED)</option>
            </select>
          </div>

          <div>
            <select
              value={categoryFilter}
              onChange={e => {
                setCategoryFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="ALL">-- Tất cả chuyên mục --</option>
              {dbState.categories.map(c => (
                <option key={c.category_id} value={c.category_id}>
                  {c.category_name}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs text-slate-500">
          <div>
            Hiển thị <span className="font-semibold text-slate-800">{filteredTopics.length}</span> đề tài của bạn
          </div>
          {(searchQuery || statusFilter !== 'ALL' || categoryFilter !== 'ALL') && (
            <button
              onClick={() => {
                setSearchQuery('');
                setStatusFilter('ALL');
                setCategoryFilter('ALL');
                setCurrentPage(1);
              }}
              className="text-red-700 hover:text-red-800 font-medium cursor-pointer flex items-center gap-1"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Xóa bộ lọc
            </button>
          )}
        </div>
      </div>

      {/* Topics Cards / Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Mã số đề tài</th>
                <th className="py-3 px-4">Tiêu đề đề tài & Góc tiếp cận</th>
                <th className="py-3 px-4">Chuyên mục</th>
                <th className="py-3 px-4">Hạn chót (Deadline)</th>
                <th className="py-3 px-4">Trạng thái phê duyệt</th>
                <th className="py-3 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {paginatedTopics.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-10 text-center text-slate-400">
                    Bạn chưa có đề tài nào phù hợp với bộ lọc hiện tại. Hãy tạo đề tài mới!
                  </td>
                </tr>
              ) : (
                paginatedTopics.map((topic) => {
                  const cat = dbState.categories.find(c => c.category_id === topic.category_id);
                  const isDraft = topic.status === 'DRAFT';
                  const isRevision = topic.status === 'REVISION_REQUESTED';
                  const isApproved = topic.status === 'APPROVED' || topic.status === 'IN_PROGRESS';

                  return (
                    <tr key={topic.topic_id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3.5 px-4 font-mono font-medium text-slate-900 whitespace-nowrap">
                        {topic.topic_code || `#${topic.topic_id}`}
                      </td>
                      <td className="py-3.5 px-4 max-w-md">
                        <div className="font-semibold text-slate-900 text-sm leading-snug">{topic.title}</div>
                        {topic.angle && (
                          <div className="text-[11px] text-slate-500 mt-0.5 line-clamp-1">{topic.angle}</div>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-slate-600 whitespace-nowrap">
                        {cat?.category_name || 'N/A'}
                      </td>
                      <td className="py-3.5 px-4 font-mono text-slate-700 whitespace-nowrap">
                        {topic.deadline || 'Chưa ấn định'}
                      </td>
                      <td className="py-3.5 px-4 whitespace-nowrap">
                        <TopicStatusBadge status={topic.status} />
                      </td>
                      <td className="py-3.5 px-4 text-right whitespace-nowrap">
                        <div className="inline-flex items-center gap-1.5 justify-end">
                          <button
                            onClick={() => setDetailTopic(topic)}
                            title="Xem chi tiết hồ sơ (SCR-1.6)"
                            className="p-1.5 text-slate-400 hover:text-slate-800 hover:bg-slate-100 rounded"
                          >
                            <Eye className="w-4 h-4" />
                          </button>

                          {(isDraft || isRevision) && (
                            <button
                              onClick={() => onEditTopic(topic)}
                              title="Chỉnh sửa nội dung đề tài"
                              className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded"
                            >
                              <Edit2 className="w-4 h-4" />
                            </button>
                          )}

                          {isRevision && (
                            <button
                              onClick={() => handleResubmit(topic)}
                              title="Hoàn thiện và Nộp lại cho BTV (SCR-1.7)"
                              className="px-2 py-1 bg-orange-600 hover:bg-orange-700 text-white rounded font-medium text-[11px] flex items-center gap-1"
                            >
                              <Send className="w-3 h-3" /> Nộp lại
                            </button>
                          )}

                          {isApproved && (
                            <button
                              onClick={() => setExtensionTopic(topic)}
                              title="Xin lùi hạn nộp (SCR-1.8)"
                              className="p-1.5 text-slate-400 hover:text-amber-700 hover:bg-amber-50 rounded"
                            >
                              <Clock className="w-4 h-4" />
                            </button>
                          )}
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

      {/* Modals */}
      <TopicDetailModal
        isOpen={!!detailTopic}
        onClose={() => setDetailTopic(null)}
        topic={detailTopic}
        onOpenExtensionModal={() => {
          setExtensionTopic(detailTopic);
          setDetailTopic(null);
        }}
      />

      <DeadlineExtensionModal
        isOpen={!!extensionTopic}
        onClose={() => setExtensionTopic(null)}
        topic={extensionTopic}
        onSuccess={() => {
          setFeedbackNotice('Đã gửi yêu cầu gia hạn deadline đến Ban Biên Tập thành công!');
          setTimeout(() => setFeedbackNotice(null), 4000);
        }}
      />
    </div>
  );
};
