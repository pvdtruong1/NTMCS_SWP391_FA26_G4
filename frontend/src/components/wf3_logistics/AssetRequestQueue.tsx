/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-3.5 & SCR-3.6 & SCR-3.7: Asset Request Queue, Conflict Checking, Checkout & Return
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { AssetRequest, AssetRequestStatus } from '../../types';
import { RequestStatusBadge, AssetStatusBadge, getAssetCategoryLabel } from '../common/StatusBadge';
import { Modal } from '../common/Modal';
import {
  CheckCircle2,
  XCircle,
  Truck,
  RotateCcw,
  AlertTriangle,
  Calendar,
  Clock,
  Filter,
} from 'lucide-react';

export const AssetRequestQueue: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [selectedRequest, setSelectedRequest] = useState<AssetRequest | null>(null);
  const [returnCondition, setReturnCondition] = useState<'AVAILABLE' | 'MAINTENANCE'>('AVAILABLE');
  const [isReturnModalOpen, setIsReturnModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const filteredRequests = dbState.assetRequests.filter(r => {
    if (statusFilter !== 'ALL' && r.status !== statusFilter) return false;
    return true;
  });

  const handleApprove = (req: AssetRequest) => {
    try {
      setErrorMessage(null);
      dbStore.updateAssetRequestStatus(req.request_id, 'APPROVED', currentUser.user_id);
      setSuccessMessage(`Đã phê duyệt yêu cầu mượn #${req.request_id} thành công.`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Phê duyệt thất bại.');
    }
  };

  const handleReject = (req: AssetRequest) => {
    try {
      setErrorMessage(null);
      dbStore.updateAssetRequestStatus(req.request_id, 'REJECTED', currentUser.user_id);
      setSuccessMessage(`Đã từ chối yêu cầu mượn #${req.request_id}.`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Thao tác thất bại.');
    }
  };

  const handleCheckout = (req: AssetRequest) => {
    try {
      setErrorMessage(null);
      dbStore.updateAssetRequestStatus(req.request_id, 'CHECKED_OUT', currentUser.user_id);
      setSuccessMessage(`Đã xuất kho bàn giao thiết bị cho phóng viên thành công.`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Xuất kho thất bại.');
    }
  };

  const handleOpenReturnModal = (req: AssetRequest) => {
    setSelectedRequest(req);
    setReturnCondition('AVAILABLE');
    setIsReturnModalOpen(true);
  };

  const handleConfirmReturn = () => {
    if (!selectedRequest) return;
    try {
      setErrorMessage(null);
      dbStore.updateAssetRequestStatus(
        selectedRequest.request_id,
        'RETURNED',
        currentUser.user_id,
        returnCondition
      );
      setSuccessMessage(`Đã ghi nhận hoàn trả thiết bị thành công (Trạng thái thiết bị: ${returnCondition === 'MAINTENANCE' ? 'Bảo trì' : 'Sẵn sàng'}).`);
      setIsReturnModalOpen(false);
      setSelectedRequest(null);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Hoàn trả thất bại.');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hậu cần & Phương tiện</span>
            <span>·</span>
            <span>SCR-3.5 & SCR-3.6 & SCR-3.7</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Hàng đợi Phê duyệt & Điều phối Thiết bị Tác nghiệp
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Duyệt cấp thiết bị, giải quyết xung đột lịch tác nghiệp, thực hiện xuất kho (Checkout) và nghiệm thu hoàn trả (Return).
          </p>
        </div>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center justify-between">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 hover:text-emerald-900">×</button>
        </div>
      )}
      {errorMessage && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs font-medium flex items-center justify-between">
          <span>{errorMessage}</span>
          <button onClick={() => setErrorMessage(null)} className="text-rose-600 hover:text-rose-900">×</button>
        </div>
      )}

      {/* Filter tabs */}
      <div className="flex items-center gap-2 p-1 bg-slate-100 rounded-lg w-fit text-xs font-medium">
        <button
          onClick={() => setStatusFilter('ALL')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'ALL' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Tất cả ({dbState.assetRequests.length})
        </button>
        <button
          onClick={() => setStatusFilter('PENDING')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'PENDING' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Chờ duyệt ({dbState.assetRequests.filter(r => r.status === 'PENDING').length})
        </button>
        <button
          onClick={() => setStatusFilter('APPROVED')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'APPROVED' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Đã duyệt ({dbState.assetRequests.filter(r => r.status === 'APPROVED').length})
        </button>
        <button
          onClick={() => setStatusFilter('CHECKED_OUT')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'CHECKED_OUT' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Đang xuất kho ({dbState.assetRequests.filter(r => r.status === 'CHECKED_OUT').length})
        </button>
        <button
          onClick={() => setStatusFilter('RETURNED')}
          className={`px-3 py-1.5 rounded-md transition-colors ${
            statusFilter === 'RETURNED' ? 'bg-white text-slate-900 shadow-xs font-semibold' : 'text-slate-600 hover:text-slate-900'
          }`}
        >
          Đã trả ({dbState.assetRequests.filter(r => r.status === 'RETURNED').length})
        </button>
      </div>

      {/* Requests Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Yêu cầu #</th>
                <th className="py-3 px-4">Đề tài gắn kết (APPROVED)</th>
                <th className="py-3 px-4">Thiết bị yêu cầu</th>
                <th className="py-3 px-4">Phóng viên mượn</th>
                <th className="py-3 px-4">Khung giờ tác nghiệp</th>
                <th className="py-3 px-4">Kiểm tra trùng lịch</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4 text-right">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredRequests.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-8 text-center text-slate-400">
                    Không có yêu cầu nào trong danh sách.
                  </td>
                </tr>
              ) : (
                filteredRequests.map((req) => {
                  const topic = dbState.topics.find(t => t.topic_id === req.topic_id);
                  const asset = dbState.assets.find(a => a.asset_id === req.asset_id);
                  const requester = dbState.users.find(u => u.user_id === req.requester_user_id);
                  const conflict = dbStore.checkScheduleConflict(req.asset_id, req.start_time, req.end_time, req.request_id);

                  return (
                    <tr key={req.request_id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-4 font-mono font-medium text-slate-900">
                        #{req.request_id}
                      </td>
                      <td className="py-3 px-4 max-w-xs">
                        <div className="font-semibold text-slate-900 truncate">
                          {topic?.topic_code || `Đề tài #${req.topic_id}`}
                        </div>
                        <div className="text-[11px] text-slate-500 truncate">{topic?.title}</div>
                      </td>
                      <td className="py-3 px-4">
                        <div className="font-medium text-slate-900">{asset?.asset_name}</div>
                        <div className="text-[11px] font-mono text-slate-500">{asset?.asset_code}</div>
                      </td>
                      <td className="py-3 px-4 font-medium text-slate-800">
                        {requester?.full_name || `User #${req.requester_user_id}`}
                      </td>
                      <td className="py-3 px-4 text-slate-600 font-mono text-[11px] whitespace-nowrap">
                        <div>{req.start_time}</div>
                        <div className="text-slate-400">đến {req.end_time}</div>
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        {conflict ? (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-rose-700 bg-rose-50 px-2 py-0.5 rounded border border-rose-200">
                            <AlertTriangle className="w-3 h-3 text-rose-600" />
                            Trùng lịch YC #{conflict.request_id}
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                            <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                            Không trùng lịch
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <RequestStatusBadge status={req.status} />
                      </td>
                      <td className="py-3 px-4 text-right whitespace-nowrap">
                        <div className="inline-flex items-center gap-1.5 justify-end">
                          {req.status === 'PENDING' && (
                            <>
                              <button
                                onClick={() => handleApprove(req)}
                                className="px-2 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-medium text-[11px] shadow-2xs"
                              >
                                Duyệt cấp
                              </button>
                              <button
                                onClick={() => handleReject(req)}
                                className="px-2 py-1 bg-slate-100 hover:bg-rose-50 text-slate-600 hover:text-rose-700 rounded font-medium text-[11px]"
                              >
                                Từ chối
                              </button>
                            </>
                          )}
                          {req.status === 'APPROVED' && (
                            <button
                              onClick={() => handleCheckout(req)}
                              className="px-2.5 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded font-medium text-[11px] shadow-2xs flex items-center gap-1"
                            >
                              <Truck className="w-3 h-3" />
                              Xuất kho bàn giao
                            </button>
                          )}
                          {req.status === 'CHECKED_OUT' && (
                            <button
                              onClick={() => handleOpenReturnModal(req)}
                              className="px-2.5 py-1 bg-slate-800 hover:bg-slate-900 text-white rounded font-medium text-[11px] shadow-2xs flex items-center gap-1"
                            >
                              <RotateCcw className="w-3 h-3" />
                              Nghiệm thu hoàn trả
                            </button>
                          )}
                          {req.status === 'RETURNED' && (
                            <span className="text-slate-400 text-[11px]">Đã hoàn tất</span>
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
      </div>

      {/* Return Modal Dialog */}
      {isReturnModalOpen && selectedRequest && (
        <Modal
          isOpen={isReturnModalOpen}
          onClose={() => setIsReturnModalOpen(false)}
          title="Nghiệm thu thiết bị hoàn trả vào kho (SCR-3.7)"
          maxWidth="md"
        >
          <div className="space-y-4 text-xs">
            <p className="text-slate-700">
              Kiểm tra ngoại quan và hiện trạng hoạt động của thiết bị sau chuyến công tác hiện trường:
            </p>
            <div className="space-y-2">
              <label className="flex items-center gap-3 p-3 rounded-lg border border-slate-200 cursor-pointer hover:bg-slate-50">
                <input
                  type="radio"
                  name="returnCondition"
                  value="AVAILABLE"
                  checked={returnCondition === 'AVAILABLE'}
                  onChange={() => setReturnCondition('AVAILABLE')}
                  className="text-red-700 focus:ring-red-600"
                />
                <div>
                  <div className="font-semibold text-slate-900">Thiết bị hoạt động tốt (Sẵn sàng sử dụng)</div>
                  <div className="text-[11px] text-slate-500">Thiết bị nguyên vẹn, đầy đủ phụ kiện, nhập kho ngay.</div>
                </div>
              </label>

              <label className="flex items-center gap-3 p-3 rounded-lg border border-amber-200 bg-amber-50/40 cursor-pointer hover:bg-amber-50">
                <input
                  type="radio"
                  name="returnCondition"
                  value="MAINTENANCE"
                  checked={returnCondition === 'MAINTENANCE'}
                  onChange={() => setReturnCondition('MAINTENANCE')}
                  className="text-red-700 focus:ring-red-600"
                />
                <div>
                  <div className="font-semibold text-amber-900">Cần bảo dưỡng / Sửa chữa kỹ thuật</div>
                  <div className="text-[11px] text-amber-700">Chuyển sang trạng thái ĐANG BẢO TRÌ (MAINTENANCE) để kỹ thuật viên kiểm tra.</div>
                </div>
              </label>
            </div>

            <div className="flex items-center justify-end gap-2 pt-4 border-t border-slate-100">
              <button
                onClick={() => setIsReturnModalOpen(false)}
                className="px-4 py-2 bg-slate-100 text-slate-700 rounded-lg font-medium"
              >
                Hủy bỏ
              </button>
              <button
                onClick={handleConfirmReturn}
                className="px-4 py-2 bg-slate-900 hover:bg-black text-white rounded-lg font-semibold"
              >
                Xác nhận nhập kho
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
