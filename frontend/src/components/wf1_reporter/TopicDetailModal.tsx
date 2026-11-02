/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.6: Topic Detail Modal & Comprehensive Lifecycle View
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Topic, TopicMember, TopicEstimate, TopicEvaluation, EvaluationDetail } from '../../types';
import { TopicStatusBadge, AssetStatusBadge, RequestStatusBadge } from '../common/StatusBadge';
import { Modal } from '../common/Modal';
import {
  FileText,
  Users,
  DollarSign,
  Award,
  Truck,
  Clock,
  Plus,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';

interface TopicDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  topic: Topic | null;
  onOpenAssetRequestModal?: () => void;
  onOpenExtensionModal?: () => void;
}

export const TopicDetailModal: React.FC<TopicDetailModalProps> = ({
  isOpen,
  onClose,
  topic,
  onOpenAssetRequestModal,
  onOpenExtensionModal,
}) => {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<'info' | 'crew_cost' | 'evaluations' | 'assets'>('info');

  if (!topic) return null;

  const dbState = dbStore.getState();
  const category = dbState.categories.find(c => c.category_id === topic.category_id);
  const desk = category ? dbState.desks.find(d => d.desk_id === category.desk_id) : null;
  const author = dbState.users.find(u => u.user_id === topic.author_user_id);

  const members = dbState.topicMembers.filter(m => m.topic_id === topic.topic_id);
  const estimates = dbState.topicEstimates.filter(e => e.topic_id === topic.topic_id);
  const totalCost = estimates.reduce((sum, item) => sum + Number(item.amount), 0);

  const evaluations = dbState.topicEvaluations.filter(e => e.topic_id === topic.topic_id);
  const assetRequests = dbState.assetRequests.filter(r => r.topic_id === topic.topic_id);
  const extensions = dbState.extensions.filter(e => e.topic_id === topic.topic_id);

  // Can request asset only if topic is APPROVED
  const isApproved = topic.status === 'APPROVED' || topic.status === 'IN_PROGRESS';

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={topic.topic_code ? `Hồ sơ đề tài: ${topic.topic_code}` : `Đề tài #${topic.topic_id}`}
      subtitle={topic.title}
      maxWidth="3xl"
    >
      <div className="space-y-4 text-xs">
        {/* Navigation Tabs */}
        <div className="flex items-center gap-1 border-b border-slate-200 pb-2">
          <button
            onClick={() => setActiveTab('info')}
            className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
              activeTab === 'info' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Thông tin đề tài & SLA
          </button>
          <button
            onClick={() => setActiveTab('crew_cost')}
            className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
              activeTab === 'crew_cost' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Tổ tác nghiệp & Dự toán ({members.length} người · {totalCost.toLocaleString('vi-VN')} đ)
          </button>
          <button
            onClick={() => setActiveTab('evaluations')}
            className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
              activeTab === 'evaluations' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Kết quả Thẩm định Rubric ({evaluations.length})
          </button>
          <button
            onClick={() => setActiveTab('assets')}
            className={`px-3 py-1.5 rounded-md font-medium transition-colors ${
              activeTab === 'assets' ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Thiết bị & Phương tiện ({assetRequests.length})
          </button>
        </div>

        {/* TAB 1: General Info */}
        {activeTab === 'info' && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-3 bg-slate-50 rounded-lg border border-slate-200">
              <div>
                <span className="text-slate-400 block text-[11px]">Trạng thái</span>
                <TopicStatusBadge status={topic.status} />
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Tác giả đề xuất</span>
                <span className="font-semibold text-slate-800">{author?.full_name || 'N/A'}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Ban chuyên môn</span>
                <span className="font-medium text-slate-800">{desk?.desk_name || 'N/A'}</span>
              </div>
              <div>
                <span className="text-slate-400 block text-[11px]">Chuyên mục</span>
                <span className="font-medium text-slate-800">{category?.category_name || 'N/A'}</span>
              </div>
            </div>

            <div>
              <span className="font-semibold text-slate-700 block mb-1">Góc tiếp cận & Khung nội dung khai thác:</span>
              <p className="p-3 bg-white border border-slate-200 rounded-lg leading-relaxed text-slate-800">
                {topic.angle || 'Chưa cung cấp góc tiếp cận chi tiết.'}
              </p>
            </div>

            <div className="p-3 bg-slate-50 rounded-lg border border-slate-200 flex items-center justify-between">
              <div>
                <span className="text-slate-500 block text-[11px]">Hạn chót nộp bản thảo (Deadline):</span>
                <span className="font-mono font-bold text-slate-900 text-sm">
                  {topic.deadline || 'Chưa ấn định'}
                </span>
              </div>
              {onOpenExtensionModal && (
                <button
                  onClick={onOpenExtensionModal}
                  className="px-3 py-1.5 bg-white border border-slate-300 hover:bg-slate-100 text-slate-700 font-medium rounded-lg shadow-2xs"
                >
                  Xin gia hạn deadline (SCR-1.8)
                </button>
              )}
            </div>

            {/* Extension History if any */}
            {extensions.length > 0 && (
              <div className="space-y-2">
                <span className="font-semibold text-slate-700 block">Lịch sử đề nghị gia hạn:</span>
                {extensions.map(ext => (
                  <div key={ext.request_id} className="p-2.5 rounded border border-slate-200 bg-white">
                    <div className="flex justify-between items-center">
                      <span className="font-mono text-slate-800">
                        {ext.current_deadline} → <strong>{ext.requested_deadline}</strong>
                      </span>
                      <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-slate-100 text-slate-700">
                        {ext.status}
                      </span>
                    </div>
                    <div className="text-slate-500 text-[11px] mt-1">Lý do: {ext.reason}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: Crew & Estimates */}
        {activeTab === 'crew_cost' && (
          <div className="space-y-4">
            <div>
              <h4 className="font-semibold text-slate-800 mb-2 flex items-center gap-1.5">
                <Users className="w-4 h-4 text-slate-500" /> Thành viên tổ tác nghiệp hiện trường:
              </h4>
              {members.length === 0 ? (
                <div className="text-slate-400 italic">Chưa có thành viên nào được phân công.</div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {members.map(m => {
                    const u = dbState.users.find(usr => usr.user_id === m.user_id);
                    return (
                      <div key={m.member_id} className="p-2.5 rounded-lg border border-slate-200 bg-slate-50 flex justify-between items-center">
                        <div>
                          <div className="font-semibold text-slate-900">{u?.full_name}</div>
                          <div className="text-[11px] text-slate-500">{u?.email}</div>
                        </div>
                        <span className="text-[10px] font-mono font-medium px-2 py-0.5 rounded bg-white border border-slate-200 text-slate-700">
                          {m.role_in_team}
                        </span>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            <div>
              <h4 className="font-semibold text-slate-800 mb-2 flex items-center gap-1.5">
                <DollarSign className="w-4 h-4 text-slate-500" /> Bảng dự toán chi phí tác nghiệp:
              </h4>
              {estimates.length === 0 ? (
                <div className="text-slate-400 italic">Không có dự toán phát sinh cho đề tài này.</div>
              ) : (
                <div className="border border-slate-200 rounded-lg overflow-hidden">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                      <tr>
                        <th className="py-2 px-3">Hạng mục chi phí</th>
                        <th className="py-2 px-3 text-right">Số tiền dự toán (VNĐ)</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {estimates.map(e => (
                        <tr key={e.estimate_id}>
                          <td className="py-2 px-3 text-slate-800">{e.cost_item}</td>
                          <td className="py-2 px-3 text-right font-mono font-medium text-slate-900">
                            {Number(e.amount).toLocaleString('vi-VN')} đ
                          </td>
                        </tr>
                      ))}
                      <tr className="bg-slate-50/80 font-bold">
                        <td className="py-2.5 px-3 text-slate-900">TỔNG CỘNG DỰ TOÁN:</td>
                        <td className="py-2.5 px-3 text-right font-mono text-red-700 text-sm">
                          {totalCost.toLocaleString('vi-VN')} đ
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 3: Evaluations */}
        {activeTab === 'evaluations' && (
          <div className="space-y-4">
            {evaluations.length === 0 ? (
              <div className="p-6 text-center text-slate-400">
                Đề tài hiện đang ở trạng thái chờ thẩm định, chưa có biên bản đánh giá rubric.
              </div>
            ) : (
              evaluations.map(ev => {
                const evaluator = dbState.users.find(u => u.user_id === ev.evaluator_user_id);
                const details = dbState.evaluationDetails.filter(d => d.evaluation_id === ev.evaluation_id);

                return (
                  <div key={ev.evaluation_id} className="p-4 rounded-xl border border-slate-200 bg-white space-y-3">
                    <div className="flex items-center justify-between pb-2 border-b border-slate-100">
                      <div>
                        <span className="font-bold text-slate-900">
                          {ev.evaluation_stage === 'DESK_REVIEW' ? 'Thẩm định cấp Ban (Desk Review)' : 'Phê duyệt Hội đồng TBT (Executive)'}
                        </span>
                        <div className="text-[11px] text-slate-500">Người thẩm định: {evaluator?.full_name}</div>
                      </div>
                      <span className={`px-2.5 py-1 rounded text-xs font-bold border ${
                        ev.decision === 'APPROVED' ? 'bg-emerald-50 text-emerald-800 border-emerald-200' :
                        ev.decision === 'ESCALATED' ? 'bg-purple-50 text-purple-800 border-purple-200' :
                        ev.decision === 'REVISION_REQUESTED' ? 'bg-orange-50 text-orange-800 border-orange-200' :
                        'bg-rose-50 text-rose-800 border-rose-200'
                      }`}>
                        {ev.decision}
                      </span>
                    </div>

                    {/* Rubric Score details */}
                    {details.length > 0 && (
                      <div className="space-y-1.5">
                        <div className="font-semibold text-slate-700 text-[11px]">Chi tiết điểm số Rubric:</div>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                          {details.map(d => {
                            const crit = dbState.criteriaConfigs.find(c => c.criteria_id === d.criteria_id);
                            return (
                              <div key={d.detail_id} className="p-2 bg-slate-50 rounded border border-slate-100 flex justify-between items-center">
                                <div>
                                  <div className="text-[11px] font-medium text-slate-800 truncate max-w-[200px]">{crit?.criteria_name}</div>
                                  {d.item_comment && <div className="text-[10px] text-slate-500 line-clamp-1">{d.item_comment}</div>}
                                </div>
                                <span className="font-mono font-bold text-slate-900 bg-white px-1.5 py-0.5 rounded border border-slate-200">
                                  {d.score}/10
                                </span>
                              </div>
                            );
                          })}
                        </div>
                      </div>
                    )}

                    {ev.comments && (
                      <div className="text-slate-700 bg-slate-50 p-2.5 rounded border border-slate-100 text-[11px] leading-relaxed">
                        <strong>Ý kiến BTV:</strong> {ev.comments}
                      </div>
                    )}
                  </div>
                );
              })
            )}
          </div>
        )}

        {/* TAB 4: Asset Requests */}
        {activeTab === 'assets' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="font-semibold text-slate-700">Thiết bị & Phương tiện đã đăng ký:</span>
              {onOpenAssetRequestModal && (
                <button
                  disabled={!isApproved}
                  onClick={onOpenAssetRequestModal}
                  title={!isApproved ? 'Chỉ có thể tạo yêu cầu mượn khi đề tài đã được phê duyệt (APPROVED).' : ''}
                  className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold shadow-2xs ${
                    isApproved
                      ? 'bg-red-700 hover:bg-red-800 text-white cursor-pointer'
                      : 'bg-slate-100 text-slate-400 cursor-not-allowed border border-slate-200'
                  }`}
                >
                  <Plus className="w-3.5 h-3.5" />
                  Đăng ký mượn thiết bị mới
                </button>
              )}
            </div>

            {!isApproved && (
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-amber-600 shrink-0" />
                <span>
                  <strong>Quy tắc nghiệp vụ xác nhận:</strong> Chỉ có thể tạo yêu cầu mượn thiết bị / xe máy khi đề tài đã được <strong>PHÊ DUYỆT (APPROVED)</strong>.
                </span>
              </div>
            )}

            {assetRequests.length === 0 ? (
              <div className="text-slate-400 italic py-4 text-center">
                Chưa có yêu cầu mượn thiết bị nào cho đề tài này.
              </div>
            ) : (
              <div className="space-y-2">
                {assetRequests.map(r => {
                  const asset = dbState.assets.find(a => a.asset_id === r.asset_id);
                  return (
                    <div key={r.request_id} className="p-3 rounded-lg border border-slate-200 bg-slate-50 flex items-center justify-between">
                      <div>
                        <div className="font-semibold text-slate-900">{asset?.asset_name}</div>
                        <div className="text-[11px] text-slate-500 font-mono">
                          Mã: {asset?.asset_code} · {r.start_time} đến {r.end_time}
                        </div>
                      </div>
                      <RequestStatusBadge status={r.status} />
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        <div className="flex justify-end pt-3 border-t border-slate-100">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg"
          >
            Đóng
          </button>
        </div>
      </div>
    </Modal>
  );
};
