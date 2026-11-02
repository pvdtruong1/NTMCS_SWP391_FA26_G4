/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.5: Quản lý & Đăng ký Yêu cầu Trang thiết bị Tác nghiệp (Hưng - WF-01)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { AssetRequestModal } from './AssetRequestModal';
import { RequestStatusBadge, AssetStatusBadge } from '../common/StatusBadge';
import {
  Truck,
  Plus,
  Clock,
  CheckCircle2,
  AlertCircle,
  FileText,
  Search,
  Filter,
} from 'lucide-react';

export const ReporterAssetRequestsView: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [feedbackNotice, setFeedbackNotice] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  // Filter requests for current reporter
  const myRequests = dbState.assetRequests.filter(r => {
    const isMine = r.requester_user_id === currentUser.user_id;
    const isMyTopic = dbState.topics.some(
      t => t.topic_id === r.topic_id && (t.author_user_id === currentUser.user_id || dbState.topicMembers.some(m => m.topic_id === t.topic_id && m.user_id === currentUser.user_id))
    );
    return isMine || isMyTopic;
  });

  const filtered = myRequests.filter(r => {
    if (statusFilter !== 'ALL' && r.status !== statusFilter) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const topic = dbState.topics.find(t => t.topic_id === r.topic_id);
      const asset = dbState.assets.find(a => a.asset_id === r.asset_id);
      return (
        topic?.title.toLowerCase().includes(q) ||
        (topic?.topic_code && topic.topic_code.toLowerCase().includes(q)) ||
        asset?.asset_name.toLowerCase().includes(q) ||
        asset?.asset_code.toLowerCase().includes(q)
      );
    }
    return true;
  });

  const pendingCount = myRequests.filter(r => r.status === 'PENDING').length;
  const approvedCount = myRequests.filter(r => r.status === 'APPROVED').length;
  const checkedOutCount = myRequests.filter(r => r.status === 'CHECKED_OUT').length;
  const returnedCount = myRequests.filter(r => r.status === 'RETURNED').length;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Phân hệ Phóng viên (WF-01)</span>
            <span>·</span>
            <span>SCR-1.5</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <Truck className="w-6 h-6 text-blue-700" />
            <span>Yêu cầu Cấp phát Trang thiết bị Tác nghiệp</span>
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Đăng ký xe ô tô chuyên dụng, flycam, máy quay và thiết bị hiện trường cho các đề tài đã được phê duyệt.
          </p>
        </div>

        <button
          onClick={() => setIsModalOpen(true)}
          className="btn primary"
        >
          <Plus className="w-4 h-4" />
          <span>Đăng ký mượn thiết bị mới</span>
        </button>
      </div>

      {feedbackNotice && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{feedbackNotice}</span>
        </div>
      )}

      {/* Stats row */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="card p-4 m-0">
          <span className="text-slate-500 text-xs block">Chờ Hậu cần duyệt</span>
          <div className="text-2xl font-bold font-mono text-amber-600 mt-1">{pendingCount}</div>
        </div>
        <div className="card p-4 m-0">
          <span className="text-slate-500 text-xs block">Đã duyệt (Chờ nhận)</span>
          <div className="text-2xl font-bold font-mono text-blue-600 mt-1">{approvedCount}</div>
        </div>
        <div className="card p-4 m-0">
          <span className="text-slate-500 text-xs block">Đang xuất kho sử dụng</span>
          <div className="text-2xl font-bold font-mono text-indigo-600 mt-1">{checkedOutCount}</div>
        </div>
        <div className="card p-4 m-0">
          <span className="text-slate-500 text-xs block">Đã hoàn tất thu hồi</span>
          <div className="text-2xl font-bold font-mono text-emerald-600 mt-1">{returnedCount}</div>
        </div>
      </div>

      {/* Filter toolbar */}
      <div className="card p-3 m-0 flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm theo mã đề tài, tên thiết bị..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="ntmcs-input pl-9"
          />
        </div>

        <div className="w-full sm:w-auto">
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="ntmcs-input w-full sm:w-auto"
          >
            <option value="ALL">-- Tất cả trạng thái --</option>
            <option value="PENDING">Chờ duyệt (PENDING)</option>
            <option value="APPROVED">Đã duyệt (APPROVED)</option>
            <option value="CHECKED_OUT">Đang mượn (CHECKED_OUT)</option>
            <option value="RETURNED">Đã hoàn trả (RETURNED)</option>
            <option value="REJECTED">Bị từ chối (REJECTED)</option>
          </select>
        </div>
      </div>

      {/* Request Table */}
      <div className="card p-0 m-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase text-[11px]">
              <tr>
                <th className="py-3 px-4">Mã yêu cầu</th>
                <th className="py-3 px-4">Đề tài gắn kết (Approved)</th>
                <th className="py-3 px-4">Thiết bị mượn</th>
                <th className="py-3 px-4">Khung giờ tác nghiệp</th>
                <th className="py-3 px-4 text-center">Trạng thái cấp phát</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-400">
                    Không tìm thấy yêu cầu mượn thiết bị nào. Nhấn <strong>"Đăng ký mượn thiết bị mới"</strong> để gửi phiếu đề nghị đến Bộ phận Hậu cần.
                  </td>
                </tr>
              ) : (
                filtered.map(req => {
                  const topic = dbState.topics.find(t => t.topic_id === req.topic_id);
                  const asset = dbState.assets.find(a => a.asset_id === req.asset_id);

                  return (
                    <tr key={req.request_id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3 px-4 font-mono font-bold text-slate-800">
                        #{req.request_id}
                      </td>
                      <td className="py-3 px-4 max-w-xs">
                        <div className="font-semibold text-slate-900 truncate">
                          {topic?.title}
                        </div>
                        <div className="text-[11px] text-blue-700 font-mono">
                          {topic?.topic_code || `#${topic?.topic_id}`}
                        </div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="font-medium text-slate-800">
                          {asset?.asset_name}
                        </div>
                        <div className="text-[11px] text-slate-500 font-mono">
                          Mã: {asset?.asset_code} · {asset?.serial_number ? `SN: ${asset.serial_number}` : ''}
                        </div>
                      </td>
                      <td className="py-3 px-4 font-mono text-[11px] text-slate-700">
                        <div>Từ: {req.start_time}</div>
                        <div>Đến: {req.end_time}</div>
                      </td>
                      <td className="py-3 px-4 text-center">
                        <RequestStatusBadge status={req.status} />
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal */}
      <AssetRequestModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={() => {
          setFeedbackNotice('Yêu cầu cấp phát thiết bị đã được gửi thành công đến Bộ phận Hậu cần (Lộc).');
          setTimeout(() => setFeedbackNotice(null), 4000);
        }}
      />
    </div>
  );
};
