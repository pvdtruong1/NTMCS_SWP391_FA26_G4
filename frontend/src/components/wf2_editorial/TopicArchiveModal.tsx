/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-2.8: Topic Archive and Reactivation View (Trường - WF-02)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic } from '../../types';
import { TopicStatusBadge } from '../common/StatusBadge';
import { TopicDetailModal } from '../wf1_reporter/TopicDetailModal';
import { Archive, RotateCcw, Eye, Search, AlertCircle } from 'lucide-react';

export const TopicArchiveView: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [searchQuery, setSearchQuery] = useState('');
  const [detailTopic, setDetailTopic] = useState<Topic | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const archivedTopics = dbState.topics.filter(
    t => t.status === 'REJECTED' || t.status === 'ABORTED' || t.status === 'ACCEPTED_READY_FOR_CMS'
  );

  const filtered = archivedTopics.filter(t => {
    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      return (
        t.title.toLowerCase().includes(q) ||
        (t.topic_code && t.topic_code.toLowerCase().includes(q)) ||
        (t.angle && t.angle.toLowerCase().includes(q))
      );
    }
    return true;
  });

  const handleReactivate = (topic: Topic) => {
    if (window.confirm(`Bạn có chắc chắn muốn TÁI KÍCH HOẠT đề tài "${topic.title}" trở lại hàng đợi thẩm định không?`)) {
      try {
        dbStore.reactivateTopic(topic.topic_id, currentUser.user_id);
        setSuccessMessage(`Đã tái kích hoạt đề tài "${topic.title}" trở lại trạng thái SUBMITTED thành công.`);
        setTimeout(() => setSuccessMessage(null), 3000);
      } catch (err: any) {
        alert(err.message || 'Tái kích hoạt thất bại.');
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Ban Biên Tập</span>
            <span>·</span>
            <span>SCR-2.8</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Kho Lưu trữ Đề tài & Tái kích hoạt Ý tưởng
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Quản lý các đề tài đã từ chối, đề tài nghiệm thu hoàn tất hoặc phục hồi các ý tưởng có giá trị thời điểm mới.
          </p>
        </div>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium">
          {successMessage}
        </div>
      )}

      {/* Search toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center justify-between">
        <div className="relative w-full max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm kiếm trong kho lưu trữ đề tài..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600 focus:bg-white"
          />
        </div>
        <div className="text-xs text-slate-500">
          Tổng cộng <span className="font-semibold text-slate-800">{filtered.length}</span> đề tài lưu trữ
        </div>
      </div>

      {/* List */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden text-xs">
        {filtered.length === 0 ? (
          <div className="p-10 text-center text-slate-400">
            Không có đề tài nào trong kho lưu trữ.
          </div>
        ) : (
          filtered.map(topic => {
            const author = dbState.users.find(u => u.user_id === topic.author_user_id);
            const cat = dbState.categories.find(c => c.category_id === topic.category_id);
            const canReactivate = topic.status === 'REJECTED' || topic.status === 'ABORTED';

            return (
              <div key={topic.topic_id} className="p-4 hover:bg-slate-50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-slate-800">
                      {topic.topic_code || `#${topic.topic_id}`}
                    </span>
                    <TopicStatusBadge status={topic.status} />
                    <span className="text-slate-400">·</span>
                    <span className="text-slate-500">{cat?.category_name}</span>
                  </div>
                  <h3 className="font-semibold text-slate-900 text-sm leading-snug">{topic.title}</h3>
                  {topic.angle && <p className="text-slate-500 text-[11px]">{topic.angle}</p>}
                  <div className="text-[10px] text-slate-400">Tác giả đề xuất: {author?.full_name}</div>
                </div>

                <div className="flex items-center gap-2 self-start sm:self-center shrink-0">
                  <button
                    onClick={() => setDetailTopic(topic)}
                    className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded font-medium flex items-center gap-1"
                  >
                    <Eye className="w-3.5 h-3.5" /> Chi tiết
                  </button>

                  {canReactivate && (
                    <button
                      onClick={() => handleReactivate(topic)}
                      className="px-3 py-1.5 bg-red-700 hover:bg-red-800 text-white rounded font-medium flex items-center gap-1 shadow-2xs"
                    >
                      <RotateCcw className="w-3.5 h-3.5" /> Tái kích hoạt
                    </button>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>

      <TopicDetailModal
        isOpen={!!detailTopic}
        onClose={() => setDetailTopic(null)}
        topic={detailTopic}
      />
    </div>
  );
};
