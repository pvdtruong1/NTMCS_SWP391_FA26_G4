/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-3.3: Create and Update Asset Form Modal (Lộc - WF-03)
 */

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Asset, AssetStatus, AssetCategory } from '../../types';
import { Modal } from '../common/Modal';

interface AssetFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  assetToEdit: Asset | null;
  onSuccess: (message: string) => void;
}

export const AssetFormModal: React.FC<AssetFormModalProps> = ({
  isOpen,
  onClose,
  assetToEdit,
  onSuccess,
}) => {
  const { currentUser } = useAuth();

  const [assetName, setAssetName] = useState('');
  const [assetCode, setAssetCode] = useState('');
  const [assetCategory, setAssetCategory] = useState<AssetCategory>('CAMERA_EQUIPMENT');
  const [assetStatus, setAssetStatus] = useState<AssetStatus>('AVAILABLE');
  const [serialNumber, setSerialNumber] = useState('');
  const [description, setDescription] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (assetToEdit) {
      setAssetName(assetToEdit.asset_name);
      setAssetCode(assetToEdit.asset_code);
      setAssetCategory(
        ['CAMERA_EQUIPMENT', 'PHOTO_EQUIPMENT', 'AUDIO_EQUIPMENT', 'ACCESSORIES', 'IT_EQUIPMENT', 'VEHICLE'].includes(assetToEdit.asset_category)
          ? (assetToEdit.asset_category as AssetCategory)
          : 'ACCESSORIES'
      );
      setAssetStatus(assetToEdit.asset_status);
      setSerialNumber(assetToEdit.serial_number || '');
      setDescription(assetToEdit.description || '');
    } else {
      setAssetName('');
      setAssetCode('');
      setAssetCategory('CAMERA_EQUIPMENT');
      setAssetStatus('AVAILABLE');
      setSerialNumber('');
      setDescription('');
    }
    setErrorMessage(null);
  }, [assetToEdit, isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    // Validation
    if (!assetName.trim()) {
      setErrorMessage('Tên thiết bị không được để trống.');
      return;
    }
    if (!assetCode.trim()) {
      setErrorMessage('Mã thiết bị không được để trống.');
      return;
    }

    try {
      if (assetToEdit) {
        dbStore.updateAsset(
          assetToEdit.asset_id,
          {
            asset_name: assetName,
            asset_code: assetCode,
            asset_category: assetCategory,
            asset_status: assetStatus,
            serial_number: serialNumber,
            description: description,
          },
          currentUser.user_id
        );
        onSuccess(`Cập nhật thành công thiết bị [${assetCode.toUpperCase()}] ${assetName}`);
      } else {
        dbStore.createAsset(
          {
            asset_name: assetName,
            asset_code: assetCode,
            asset_category: assetCategory,
            asset_status: assetStatus,
            serial_number: serialNumber,
            description: description,
          },
          currentUser.user_id
        );
        onSuccess(`Đã thêm mới thiết bị [${assetCode.toUpperCase()}] ${assetName} vào kho.`);
      }
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Lưu dữ liệu thất bại.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={assetToEdit ? 'Chỉnh sửa thông tin thiết bị' : 'Đăng ký thêm mới thiết bị vào kho'}
      subtitle={assetToEdit ? `Mã tài sản: ${assetToEdit.asset_code}` : 'Điền đầy đủ thông tin kỹ thuật và phân loại theo danh mục chuẩn'}
      maxWidth="xl"
    >
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {errorMessage && (
          <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 font-medium">
            {errorMessage}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Asset Code */}
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Mã thiết bị <span className="text-rose-600">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="VD: CAM-SONY-02, VEH-4WD-02"
              value={assetCode}
              onChange={e => setAssetCode(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg font-mono focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600 uppercase"
            />
          </div>

          {/* Asset Category (The 6 Confirmed categories) */}
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Danh mục thiết bị <span className="text-rose-600">*</span>
            </label>
            <select
              value={assetCategory}
              onChange={e => setAssetCategory(e.target.value as AssetCategory)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="CAMERA_EQUIPMENT">Thiết bị quay phim</option>
              <option value="PHOTO_EQUIPMENT">Thiết bị chụp ảnh</option>
              <option value="AUDIO_EQUIPMENT">Thiết bị âm thanh</option>
              <option value="ACCESSORIES">Phụ kiện tác nghiệp</option>
              <option value="IT_EQUIPMENT">Thiết bị công nghệ</option>
              <option value="VEHICLE">Phương tiện vận chuyển</option>
            </select>
          </div>
        </div>

        {/* Asset Name */}
        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Tên gọi thiết bị / Phương tiện <span className="text-rose-600">*</span>
          </label>
          <input
            type="text"
            required
            placeholder="VD: Máy quay phim Sony FX3 kèm lens 24-70mm GM II"
            value={assetName}
            onChange={e => setAssetName(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Serial Number */}
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Số Serial / Biển kiểm soát
            </label>
            <input
              type="text"
              placeholder="VD: 51H-992.81 hoặc SN-2026-9901"
              value={serialNumber}
              onChange={e => setSerialNumber(e.target.value)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg font-mono focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
            />
          </div>

          {/* Status */}
          <div>
            <label className="block font-semibold text-slate-700 mb-1">
              Trạng thái ban đầu <span className="text-rose-600">*</span>
            </label>
            <select
              value={assetStatus}
              onChange={e => setAssetStatus(e.target.value as AssetStatus)}
              className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="AVAILABLE">Sẵn sàng sử dụng</option>
              <option value="CHECKED_OUT">Đang được sử dụng</option>
              <option value="MAINTENANCE">Đang bảo trì</option>
            </select>
          </div>
        </div>

        {/* Description */}
        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Mô tả thông số kỹ thuật & Ghi chú kiểm kê
          </label>
          <textarea
            rows={3}
            placeholder="Chi tiết phụ kiện đi kèm (pin, sạc, thẻ nhớ, hộp chống sốc), tình trạng ngoại quan..."
            value={description}
            onChange={e => setDescription(e.target.value)}
            className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:bg-white focus:outline-none focus:ring-1 focus:ring-red-600 resize-none leading-relaxed"
          />
        </div>

        {/* Buttons */}
        <div className="flex items-center justify-end gap-2 pt-4 border-t border-slate-100">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg transition-colors"
          >
            Hủy bỏ
          </button>
          <button
            type="submit"
            className="ntmcs-btn-primary text-xs"
          >
            {assetToEdit ? 'Lưu thay đổi' : 'Tạo mới thiết bị'}
          </button>
        </div>
      </form>
    </Modal>
  );
};
