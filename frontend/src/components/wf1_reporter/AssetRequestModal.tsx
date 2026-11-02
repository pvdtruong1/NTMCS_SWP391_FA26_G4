/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-1.5: Phiếu Đăng ký Yêu cầu Trang thiết bị Tác nghiệp (NTMCS)
 * Supports Batch / Multi-Asset Selection in a single request submission.
 */

import React, { useState, useEffect, useMemo } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Modal } from '../common/Modal';
import { Asset, Topic, AssetRequest } from '../../types';
import { AssetStatusBadge, getAssetCategoryLabel } from '../common/StatusBadge';
import {
  Truck,
  Box,
  Calendar,
  Clock,
  AlertTriangle,
  CheckCircle2,
  AlertCircle,
  FileText,
  Plus,
  Trash2,
  Check,
  Search,
} from 'lucide-react';

interface AssetRequestModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultTopicId?: number | null;
  onSuccess?: () => void;
}

export const AssetRequestModal: React.FC<AssetRequestModalProps> = ({
  isOpen,
  onClose,
  defaultTopicId,
  onSuccess,
}) => {
  const { currentUser } = useAuth();
  const dbState = dbStore.getState();

  // Find all approved / assigned topics for current user
  const approvedTopics = useMemo(() => {
    return dbState.topics.filter(t => {
      const isAuthorOrMember =
        t.author_user_id === currentUser.user_id ||
        dbState.topicMembers.some(m => m.topic_id === t.topic_id && m.user_id === currentUser.user_id);
      const isApprovedStatus = t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED';
      return isAuthorOrMember && isApprovedStatus;
    });
  }, [dbState.topics, dbState.topicMembers, currentUser.user_id]);

  // Form states
  const [selectedTopicId, setSelectedTopicId] = useState<number>(() => {
    if (defaultTopicId && approvedTopics.some(t => t.topic_id === defaultTopicId)) {
      return defaultTopicId;
    }
    return approvedTopics[0]?.topic_id || 0;
  });

  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [assetSearchQuery, setAssetSearchQuery] = useState('');
  
  // Selected multiple assets
  const [selectedAssetIds, setSelectedAssetIds] = useState<number[]>([dbState.assets[0]?.asset_id || 1]);

  const [startTime, setStartTime] = useState<string>('2026-10-25 08:00:00');
  const [endTime, setEndTime] = useState<string>('2026-10-26 18:00:00');
  const [purposeNote, setPurposeNote] = useState('');

  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Update selected topic if defaultTopicId changes
  useEffect(() => {
    if (defaultTopicId && approvedTopics.some(t => t.topic_id === defaultTopicId)) {
      setSelectedTopicId(defaultTopicId);
    } else if (approvedTopics.length > 0 && !selectedTopicId) {
      setSelectedTopicId(approvedTopics[0].topic_id);
    }
  }, [defaultTopicId, approvedTopics, selectedTopicId]);

  // Filter available assets
  const filteredAssets = useMemo(() => {
    return dbState.assets.filter(a => {
      if (categoryFilter !== 'ALL' && a.asset_category !== categoryFilter) return false;
      if (assetSearchQuery.trim()) {
        const q = assetSearchQuery.toLowerCase();
        return (
          a.asset_name.toLowerCase().includes(q) ||
          a.asset_code.toLowerCase().includes(q) ||
          (a.serial_number && a.serial_number.toLowerCase().includes(q))
        );
      }
      return true;
    });
  }, [dbState.assets, categoryFilter, assetSearchQuery]);

  // Toggle asset in basket
  const toggleAssetSelection = (assetId: number) => {
    if (selectedAssetIds.includes(assetId)) {
      setSelectedAssetIds(selectedAssetIds.filter(id => id !== assetId));
    } else {
      setSelectedAssetIds([...selectedAssetIds, assetId]);
    }
  };

  const removeAssetFromBasket = (assetId: number) => {
    setSelectedAssetIds(selectedAssetIds.filter(id => id !== assetId));
  };

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!selectedTopicId) {
      setErrorMessage('Vui lòng chọn đề tài báo chí đã được phê duyệt (APPROVED/IN_PROGRESS).');
      return;
    }

    if (selectedAssetIds.length === 0) {
      setErrorMessage('Vui lòng chọn ít nhất một thiết bị hoặc phương tiện cần mượn.');
      return;
    }

    try {
      dbStore.createBatchAssetRequests({
        topic_id: selectedTopicId,
        asset_ids: selectedAssetIds,
        requester_user_id: currentUser.user_id,
        start_time: startTime,
        end_time: endTime,
        purpose_note: purposeNote,
      });

      setSuccessMessage(
        `Đã gửi thành công đơn đăng ký mượn ${selectedAssetIds.length} thiết bị/phương tiện! Hồ sơ đã được chuyển tới Hàng đợi Bộ phận Hậu cần (Lộc).`
      );

      setTimeout(() => {
        if (onSuccess) onSuccess();
        onClose();
      }, 1500);
    } catch (err: any) {
      setErrorMessage(err.message || 'Không thể tạo yêu cầu mượn thiết bị.');
    }
  };

  const chosenTopic = dbState.topics.find(t => t.topic_id === selectedTopicId);

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="SCR-1.5: Phiếu Đăng ký Yêu cầu Trang thiết bị & Phương tiện Tác nghiệp"
      subtitle="Đăng ký nhiều thiết bị cùng lúc trong 1 đơn cho đề tài đã được phê duyệt"
      maxWidth="3xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {successMessage && (
          <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 font-semibold flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>{successMessage}</span>
          </div>
        )}

        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 font-semibold flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
            <span>{errorMessage}</span>
          </div>
        )}

        {/* Business Rule Banner */}
        <div className="p-3 bg-blue-50/70 border border-blue-200 rounded-lg text-blue-900 flex items-start gap-2.5">
          <Truck className="w-4 h-4 text-blue-600 shrink-0 mt-0.5" />
          <div className="space-y-1">
            <div className="font-bold">Quy tắc Nghiệp vụ Điều phối Hậu cần & Thiết bị:</div>
            <p className="m-0 leading-relaxed text-blue-800 text-[11px]">
              Trong 1 lần gửi đơn, phóng viên có thể chọn <strong>nhiều thiết bị & phương tiện cùng lúc</strong> (Xe ô tô, Máy quay, Flycam, Bộ đàm). Hệ thống tự động kiểm tra trùng lịch từng món thiết bị và chuyển thẳng tới Hậu cần tòa soạn để xuất kho.
            </p>
          </div>
        </div>

        {/* 1. Chọn Đề tài Approved */}
        <div className="space-y-1.5">
          <label className="font-semibold text-slate-800 block">
            1. Đề tài tác nghiệp đã được phê duyệt <span className="text-red-500">*</span>
          </label>
          {approvedTopics.length === 0 ? (
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-800">
              Hiện bạn chưa có đề tài nào ở trạng thái <strong>APPROVED</strong>. Hãy chờ BTV thẩm định duyệt đề tài hoặc tạo phiếu đề xuất mới.
            </div>
          ) : (
            <select
              value={selectedTopicId}
              onChange={e => setSelectedTopicId(Number(e.target.value))}
              className="ntmcs-input"
              required
            >
              {approvedTopics.map(t => (
                <option key={t.topic_id} value={t.topic_id}>
                  {t.topic_code || `#${t.topic_id}`} - {t.title} (Hạn nộp: {t.deadline})
                </option>
              ))}
            </select>
          )}
          {chosenTopic && (
            <div className="text-[11px] text-slate-500 flex items-center gap-2 bg-slate-50 p-2 rounded border border-slate-200">
              <FileText className="w-3.5 h-3.5 text-blue-600" />
              <span>Góc tiếp cận: <strong>{chosenTopic.angle || 'Tác nghiệp thực tế'}</strong></span>
            </div>
          )}
        </div>

        {/* 2. Thời gian mượn thiết bị */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div className="space-y-1">
            <label className="font-semibold text-slate-800 block">
              Thời gian bắt đầu mượn <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type="text"
                value={startTime}
                onChange={e => setStartTime(e.target.value)}
                placeholder="YYYY-MM-DD HH:mm:ss"
                className="ntmcs-input font-mono"
                required
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="font-semibold text-slate-800 block">
              Thời gian dự kiến hoàn trả <span className="text-red-500">*</span>
            </label>
            <div className="relative">
              <input
                type="text"
                value={endTime}
                onChange={e => setEndTime(e.target.value)}
                placeholder="YYYY-MM-DD HH:mm:ss"
                className="ntmcs-input font-mono"
                required
              />
            </div>
          </div>
        </div>

        {/* 3. Danh sách thiết bị đã chọn trong đơn (Basket) */}
        <div className="space-y-2 pt-1">
          <div className="flex items-center justify-between">
            <label className="font-semibold text-slate-900 block text-xs">
              3. Danh sách thiết bị & phương tiện yêu cầu ({selectedAssetIds.length} thiết bị đã chọn):
            </label>
            <span className="text-[11px] text-slate-500">
              Tick chọn bên dưới để thêm/bớt thiết bị vào đơn
            </span>
          </div>

          {selectedAssetIds.length === 0 ? (
            <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-800 text-[11px] text-center">
              Chưa chọn thiết bị nào. Hãy tick chọn thiết bị trong danh mục bên dưới.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-48 overflow-y-auto p-1 border border-slate-200 rounded-lg bg-slate-50">
              {selectedAssetIds.map(assetId => {
                const asset = dbState.assets.find(a => a.asset_id === assetId);
                if (!asset) return null;
                const conflict = dbStore.checkScheduleConflict(asset.asset_id, startTime, endTime);

                return (
                  <div
                    key={assetId}
                    className={`p-2 rounded-lg border bg-white flex items-center justify-between text-xs transition-colors ${
                      conflict ? 'border-amber-300 bg-amber-50/50' : 'border-slate-200'
                    }`}
                  >
                    <div className="min-w-0 flex-1 pr-2">
                      <div className="font-bold text-slate-900 truncate">{asset.asset_name}</div>
                      <div className="text-[10px] text-slate-500 font-mono flex items-center gap-1.5 mt-0.5">
                        <span className="bg-slate-100 px-1 py-0.2 rounded font-semibold text-slate-700">{asset.asset_code}</span>
                        <span>·</span>
                        <span>{getAssetCategoryLabel(asset.asset_category)}</span>
                      </div>
                      {conflict && (
                        <div className="text-[10px] text-amber-800 font-semibold mt-1 flex items-center gap-1">
                          <AlertTriangle className="w-3 h-3 text-amber-600 shrink-0" />
                          <span>Trùng lịch từ {conflict.start_time.substring(5, 16)}</span>
                        </div>
                      )}
                    </div>

                    <button
                      type="button"
                      onClick={() => removeAssetFromBasket(assetId)}
                      className="p-1 hover:bg-rose-50 text-slate-400 hover:text-rose-600 rounded cursor-pointer shrink-0"
                      title="Bỏ chọn thiết bị này"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* 4. Kho thiết bị: Bộ lọc & Chọn thêm thiết bị */}
        <div className="space-y-2 pt-2 border-t border-slate-200">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
            <span className="font-semibold text-slate-800">Chọn thiết bị từ kho tòa soạn:</span>
            <div className="flex items-center gap-2">
              <select
                value={categoryFilter}
                onChange={e => setCategoryFilter(e.target.value)}
                className="px-2 py-1 bg-white border border-slate-300 rounded text-xs"
              >
                <option value="ALL">-- Tất cả danh mục --</option>
                <option value="CAMERA_EQUIPMENT">Máy quay (Camera)</option>
                <option value="PHOTO_EQUIPMENT">Flycam & Ảnh</option>
                <option value="VEHICLE">Xe ô tô chuyên dụng</option>
                <option value="AUDIO_EQUIPMENT">Âm thanh & Thu âm</option>
                <option value="ACCESSORIES">Trang bị bảo hộ</option>
                <option value="IT_EQUIPMENT">IT & Truyền dẫn</option>
              </select>

              <input
                type="text"
                placeholder="Tìm tên thiết bị..."
                value={assetSearchQuery}
                onChange={e => setAssetSearchQuery(e.target.value)}
                className="px-2 py-1 bg-white border border-slate-300 rounded text-xs w-36"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-40 overflow-y-auto p-1">
            {filteredAssets.map(a => {
              const isSelected = selectedAssetIds.includes(a.asset_id);
              const isBusy = a.asset_status === 'CHECKED_OUT';

              return (
                <div
                  key={a.asset_id}
                  onClick={() => toggleAssetSelection(a.asset_id)}
                  className={`p-2 rounded-lg border text-xs cursor-pointer transition-all flex items-center justify-between gap-1.5 ${
                    isSelected
                      ? 'border-blue-600 bg-blue-50/80 shadow-xs ring-1 ring-blue-500'
                      : 'border-slate-200 bg-white hover:bg-slate-50'
                  }`}
                >
                  <div className="min-w-0 flex-1">
                    <div className="font-semibold text-slate-900 truncate text-[11px]">{a.asset_name}</div>
                    <div className="text-[10px] text-slate-400 font-mono truncate">{a.asset_code}</div>
                  </div>
                  <div className="shrink-0">
                    {isSelected ? (
                      <div className="w-5 h-5 rounded-md bg-blue-600 text-white flex items-center justify-center">
                        <Check className="w-3.5 h-3.5" />
                      </div>
                    ) : (
                      <div className="w-5 h-5 rounded-md border border-slate-300 hover:border-blue-500 flex items-center justify-center text-slate-400">
                        <Plus className="w-3 h-3" />
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div>
          <label className="font-semibold text-slate-800 block mb-1">
            Ghi chú mục đích sử dụng & Địa bàn tác nghiệp
          </label>
          <input
            type="text"
            placeholder="VD: Quay phóng sự hiện trường tại huyện Củ Chi, cần flycam khảo sát địa hình..."
            value={purposeNote}
            onChange={e => setPurposeNote(e.target.value)}
            className="ntmcs-input"
          />
        </div>

        {/* Modal actions */}
        <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-lg cursor-pointer"
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            disabled={selectedAssetIds.length === 0}
            className={`px-5 py-2 font-semibold rounded-lg shadow-xs flex items-center gap-1.5 cursor-pointer ${
              selectedAssetIds.length > 0
                ? 'bg-blue-600 hover:bg-blue-700 text-white'
                : 'bg-slate-300 text-slate-500 cursor-not-allowed'
            }`}
          >
            <Truck className="w-4 h-4" />
            <span>Gửi đơn mượn ({selectedAssetIds.length} thiết bị)</span>
          </button>
        </div>
      </form>
    </Modal>
  );
};
