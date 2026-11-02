/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.3: Duplicate Topic Search & Anti-Duplicate Checking (Hưng - WF-01)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { Topic } from '../../types';
import { TopicStatusBadge } from '../common/StatusBadge';
import { TopicDetailModal } from './TopicDetailModal';
import { Search, AlertTriangle, Eye, CheckCircle2, RotateCcw } from 'lucide-react';

export const DuplicateTopicSearch: React.FC = () => {
  const [dbState] = useState(dbStore.getState());
  const [query, setQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Topic[] | null>(null);
  const [detailTopic, setDetailTopic] = useState<Topic | null>(null);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    // Tra cứu đề tài trùng lặp thuần túy bằng từ khóa trên toàn bộ dữ liệu tòa soạn
    const res = dbStore.searchDuplicateTopics(query);
    setSearchResults(res);
  };

  const handleReset = () => {
    setQuery('');
    setSearchResults(null);
  };

  return (
    <div className="space-y-6">
      <div className="border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
          <span>Phóng viên tác nghiệp</span>
          <span>·</span>
          <span className="font-semibold text-blue-600">SCR-1.3</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Tra cứu & Kiểm tra Trùng lặp Đề tài Báo chí (Anti-Duplicate Check)
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Quét cơ sở dữ liệu toàn bộ tòa soạn thuần túy bằng từ khóa để kiểm tra tính độc quyền và tránh trùng lặp ý tưởng với các đề tài đã xuất bản hoặc đang triển khai.
        </p>
      </div>

      {/* Search Box - Pure Keyword Search */}
      <form onSubmit={handleSearch} className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs space-y-4">
        <div>
          <label className="block font-semibold text-slate-800 text-xs mb-1.5">
            Từ khóa tra cứu tiêu đề / Góc nhìn / Nội dung tác nghiệp
          </label>
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Nhập từ khóa (VD: nút giao An Phú, thuế tối thiểu toàn cầu, đất công, metro, tuần tra đêm...)"
              value={query}
              onChange={e => setQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-2.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-blue-600"
            />
          </div>
          <div className="flex flex-wrap items-center gap-2 mt-2 text-[11px] text-slate-500">
            <span>Gợi ý nhanh:</span>
            {['An Phú', 'Thuế tối thiểu', 'Đất công', 'Tuần tra đêm', 'Chuyển đổi số'].map(kw => (
              <button
                key={kw}
                type="button"
                onClick={() => {
                  setQuery(kw);
                  setSearchResults(dbStore.searchDuplicateTopics(kw));
                }}
                className="px-2 py-0.5 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-700 cursor-pointer font-medium"
              >
                {kw}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          {searchResults !== null && (
            <button
              type="button"
              onClick={handleReset}
              className="px-4 py-2 text-xs text-slate-600 hover:text-slate-900 font-medium cursor-pointer flex items-center gap-1.5"
            >
              <RotateCcw className="w-3.5 h-3.5" /> Xóa tìm kiếm
            </button>
          )}
          <button
            type="submit"
            className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white font-semibold text-xs rounded-lg shadow-xs transition-colors flex items-center gap-2"
          >
            <Search className="w-4 h-4" />
            Kiểm tra đối chiếu đề tài
          </button>
        </div>
      </form>

      {/* Results view */}
      {searchResults !== null && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-slate-800">
              Kết quả đối chiếu: {searchResults.length} đề tài tương đồng
            </h2>
            {searchResults.length === 0 ? (
              <span className="text-xs text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded border border-emerald-200 font-semibold flex items-center gap-1">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                Không phát hiện trùng lặp đáng kể. Đề tài đạt tính mới & độc quyền cao!
              </span>
            ) : (
              <span className="text-xs text-amber-800 bg-amber-50 px-2.5 py-1 rounded border border-amber-200 font-semibold flex items-center gap-1">
                <AlertTriangle className="w-4 h-4 text-amber-600" />
                Cần lưu ý: Xem xét đổi mới góc tiếp cận để tránh trùng lặp.
              </span>
            )}
          </div>

          <div className="bg-white rounded-xl border border-slate-200 shadow-xs divide-y divide-slate-100 overflow-hidden text-xs">
            {searchResults.map(topic => {
              const cat = dbState.categories.find(c => c.category_id === topic.category_id);
              const author = dbState.users.find(u => u.user_id === topic.author_user_id);

              return (
                <div key={topic.topic_id} className="p-4 hover:bg-slate-50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-mono font-bold text-slate-900">
                        {topic.topic_code || `#${topic.topic_id}`}
                      </span>
                      <TopicStatusBadge status={topic.status} />
                      <span className="text-slate-400">·</span>
                      <span className="text-slate-600">{cat?.category_name}</span>
                    </div>
                    <h3 className="font-semibold text-slate-900 text-sm">{topic.title}</h3>
                    {topic.angle && <p className="text-slate-500 text-[11px]">{topic.angle}</p>}
                    <div className="text-[10px] text-slate-400">
                      Tác giả: {author?.full_name} · Deadline: {topic.deadline || 'Chưa ấn định'}
                    </div>
                  </div>

                  <button
                    onClick={() => setDetailTopic(topic)}
                    className="self-start sm:self-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-medium flex items-center gap-1.5 shrink-0"
                  >
                    <Eye className="w-3.5 h-3.5" /> Xem hồ sơ
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {detailTopic && (
        <TopicDetailModal
          isOpen={!!detailTopic}
          onClose={() => setDetailTopic(null)}
          topic={detailTopic}
        />
      )}
    </div>
  );
};
