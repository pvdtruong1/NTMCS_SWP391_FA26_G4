/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-3.2: Asset Inventory and CRUD List (Lộc - WF-03)
 * Full CRUD, real-time search, filters, pagination, delete restrictions
 */

import React, { useState, useMemo } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Asset, AssetStatus, AssetCategory } from '../../types';
import { AssetStatusBadge, getAssetCategoryLabel } from '../common/StatusBadge';
import { Modal } from '../common/Modal';
import { AssetFormModal } from './AssetFormModal';
import {
  Search,
  Filter,
  Plus,
  Edit2,
  Trash2,
  Eye,
  RotateCcw,
  AlertOctagon,
  ChevronLeft,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react';

export const AssetInventoryList: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());

  // Search & Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Pagination state
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 5;

  // Sorting state
  const [sortBy, setSortBy] = useState<'asset_name' | 'asset_code' | 'asset_status'>('asset_name');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');

  // Modals state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);
  const [detailAsset, setDetailAsset] = useState<Asset | null>(null);
  const [deletingAsset, setDeletingAsset] = useState<Asset | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Sync state on db changes
  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const resetFilters = () => {
    setSearchQuery('');
    setCategoryFilter('ALL');
    setStatusFilter('ALL');
    setCurrentPage(1);
  };

  // Filtered & Sorted Assets
  const filteredAssets = useMemo(() => {
    let result = [...dbState.assets];

    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      result = result.filter(
        a =>
          a.asset_name.toLowerCase().includes(q) ||
          a.asset_code.toLowerCase().includes(q) ||
          (a.serial_number && a.serial_number.toLowerCase().includes(q))
      );
    }

    // Category filter
    if (categoryFilter !== 'ALL') {
      result = result.filter(a => a.asset_category === categoryFilter);
    }

    // Status filter
    if (statusFilter !== 'ALL') {
      result = result.filter(a => a.asset_status === statusFilter);
    }

    // Sort
    result.sort((a, b) => {
      const fieldA = a[sortBy] || '';
      const fieldB = b[sortBy] || '';
      const cmp = fieldA.localeCompare(fieldB);
      return sortOrder === 'asc' ? cmp : -cmp;
    });

    return result;
  }, [dbState.assets, searchQuery, categoryFilter, statusFilter, sortBy, sortOrder]);

  // Pagination bounds
  const totalPages = Math.ceil(filteredAssets.length / pageSize) || 1;
  const paginatedAssets = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredAssets.slice(start, start + pageSize);
  }, [filteredAssets, currentPage, pageSize]);

  // Adjust page if out of bounds after filtering
  React.useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(1);
    }
  }, [totalPages, currentPage]);

  // Handle Delete Confirmation
  const confirmDelete = () => {
    if (!deletingAsset) return;
    try {
      dbStore.deleteAsset(deletingAsset.asset_id, currentUser.user_id);
      setSuccessMessage(`Đã xóa thành công thiết bị [${deletingAsset.asset_code}] ${deletingAsset.asset_name}`);
      setDeletingAsset(null);
      setErrorMessage(null);
      setTimeout(() => setSuccessMessage(null), 4000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Xóa thiết bị thất bại.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hậu cần & Phương tiện</span>
            <span>·</span>
            <span>SCR-3.2</span>
            <span>·</span>
            <span>Kho thiết bị tác nghiệp</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Quản lý Danh mục Thiết bị & Xe máy
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Theo dõi hiện trạng, khả dụng và phục vụ điều phối phương tiện tác nghiệp hiện trường.
          </p>
        </div>

        <button
          onClick={() => {
            setEditingAsset(null);
            setIsFormOpen(true);
          }}
          className="ntmcs-btn-primary flex items-center gap-2 text-xs shrink-0"
        >
          <Plus className="w-4 h-4" />
          <span>Thêm thiết bị mới (SCR-3.3)</span>
        </button>
      </div>

      {/* Notifications / Alerts */}
      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center justify-between">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-600 hover:text-emerald-900">×</button>
        </div>
      )}
      {errorMessage && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs font-medium flex items-center justify-between">
          <span className="flex items-center gap-2">
            <AlertOctagon className="w-4 h-4 text-rose-600 shrink-0" />
            {errorMessage}
          </span>
          <button onClick={() => setErrorMessage(null)} className="text-rose-600 hover:text-rose-900">×</button>
        </div>
      )}

      {/* Search & Filter Toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-3">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          {/* Search Input */}
          <div className="relative md:col-span-2">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            <input
              type="text"
              placeholder="Tìm theo mã thiết bị, tên gọi, số serial..."
              value={searchQuery}
              onChange={e => {
                setSearchQuery(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600 focus:bg-white"
            />
          </div>

          {/* Category Filter */}
          <div>
            <select
              value={categoryFilter}
              onChange={e => {
                setCategoryFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="w-full px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
            >
              <option value="ALL">-- Tất cả danh mục --</option>
              <option value="CAMERA_EQUIPMENT">Thiết bị quay phim</option>
              <option value="PHOTO_EQUIPMENT">Thiết bị chụp ảnh</option>
              <option value="AUDIO_EQUIPMENT">Thiết bị âm thanh</option>
              <option value="ACCESSORIES">Phụ kiện tác nghiệp</option>
              <option value="IT_EQUIPMENT">Thiết bị công nghệ</option>
              <option value="VEHICLE">Phương tiện vận chuyển</option>
            </select>
          </div>

          {/* Status Filter */}
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
              <option value="AVAILABLE">Sẵn sàng sử dụng</option>
              <option value="CHECKED_OUT">Đang được sử dụng</option>
              <option value="MAINTENANCE">Đang bảo trì</option>
            </select>
          </div>
        </div>

        {/* Filter stats & Reset */}
        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs text-slate-500">
          <div>
            Hiển thị <span className="font-semibold text-slate-800">{filteredAssets.length}</span> / {dbState.assets.length} thiết bị trong kho
          </div>
          {(searchQuery || categoryFilter !== 'ALL' || statusFilter !== 'ALL') && (
            <button
              onClick={resetFilters}
              className="inline-flex items-center gap-1 text-red-700 hover:text-red-800 font-medium cursor-pointer"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Xóa bộ lọc</span>
            </button>
          )}
        </div>
      </div>

      {/* Asset Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Mã tài sản</th>
                <th className="py-3 px-4">Tên thiết bị / Phương tiện</th>
                <th className="py-3 px-4">Danh mục</th>
                <th className="py-3 px-4">Số Serial / Biển số</th>
                <th className="py-3 px-4">Trạng thái hiện tại</th>
                <th className="py-3 px-4 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {paginatedAssets.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-400">
                    Không tìm thấy thiết bị nào phù hợp với điều kiện tìm kiếm.
                  </td>
                </tr>
              ) : (
                paginatedAssets.map((asset) => {
                  const isCheckedOut = asset.asset_status === 'CHECKED_OUT';
                  return (
                    <tr key={asset.asset_id} className="hover:bg-slate-50/70 transition-colors">
                      <td className="py-3 px-4 font-mono font-medium text-slate-900 whitespace-nowrap">
                        {asset.asset_code}
                      </td>
                      <td className="py-3 px-4">
                        <div className="font-medium text-slate-900">{asset.asset_name}</div>
                        {asset.description && (
                          <div className="text-[11px] text-slate-400 line-clamp-1">{asset.description}</div>
                        )}
                      </td>
                      <td className="py-3 px-4 text-slate-600 whitespace-nowrap">
                        {getAssetCategoryLabel(asset.asset_category)}
                      </td>
                      <td className="py-3 px-4 font-mono text-slate-500 whitespace-nowrap">
                        {asset.serial_number || '—'}
                      </td>
                      <td className="py-3 px-4 whitespace-nowrap">
                        <AssetStatusBadge status={asset.asset_status} />
                      </td>
                      <td className="py-3 px-4 text-right whitespace-nowrap">
                        <div className="inline-flex items-center gap-1.5 justify-end">
                          <button
                            onClick={() => setDetailAsset(asset)}
                            title="Xem chi tiết"
                            className="p-1 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded"
                          >
                            <Eye className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => {
                              setEditingAsset(asset);
                              setIsFormOpen(true);
                            }}
                            title="Chỉnh sửa thiết bị"
                            className="p-1 text-slate-400 hover:text-blue-700 hover:bg-blue-50 rounded"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          
                          {/* DELETE BUTTON WITH CONFIRMED CHECKED_OUT RESTRICTION */}
                          <div className="relative group inline-block">
                            <button
                              disabled={isCheckedOut}
                              onClick={() => {
                                setErrorMessage(null);
                                setDeletingAsset(asset);
                              }}
                              title={isCheckedOut ? 'Không thể xóa thiết bị đang được sử dụng.' : 'Xóa thiết bị'}
                              className={`p-1 rounded transition-colors ${
                                isCheckedOut
                                  ? 'text-slate-300 cursor-not-allowed bg-slate-50'
                                  : 'text-slate-400 hover:text-rose-600 hover:bg-rose-50'
                              }`}
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                            {isCheckedOut && (
                              <div className="absolute right-0 bottom-full mb-1 hidden group-hover:block z-50 w-60 bg-slate-900 text-white text-[11px] p-2 rounded shadow-lg pointer-events-none text-left">
                                <span className="font-semibold text-rose-300 flex items-center gap-1">
                                  <ShieldAlert className="w-3.5 h-3.5" /> Quy định nghiệp vụ:
                                </span>
                                Không thể xóa thiết bị đang được sử dụng.
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Controls */}
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

      {/* Asset Form Modal (Create & Update) */}
      <AssetFormModal
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setEditingAsset(null);
        }}
        assetToEdit={editingAsset}
        onSuccess={(msg) => {
          setSuccessMessage(msg);
          setTimeout(() => setSuccessMessage(null), 4000);
        }}
      />

      {/* View Detail Modal */}
      {detailAsset && (
        <Modal
          isOpen={!!detailAsset}
          onClose={() => setDetailAsset(null)}
          title={`Chi tiết thiết bị: ${detailAsset.asset_code}`}
          subtitle={detailAsset.asset_name}
        >
          <div className="space-y-4 text-xs">
            <div className="grid grid-cols-2 gap-4 p-3 bg-slate-50 rounded-lg border border-slate-100">
              <div>
                <span className="text-slate-400 block mb-0.5">Mã thiết bị</span>
                <span className="font-mono font-bold text-slate-800 text-sm">{detailAsset.asset_code}</span>
              </div>
              <div>
                <span className="text-slate-400 block mb-0.5">Trạng thái</span>
                <AssetStatusBadge status={detailAsset.asset_status} />
              </div>
              <div>
                <span className="text-slate-400 block mb-0.5">Danh mục</span>
                <span className="font-medium text-slate-800">{getAssetCategoryLabel(detailAsset.asset_category)}</span>
              </div>
              <div>
                <span className="text-slate-400 block mb-0.5">Số Serial / Biển kiểm soát</span>
                <span className="font-mono font-medium text-slate-800">{detailAsset.serial_number || 'Không có'}</span>
              </div>
            </div>

            <div>
              <span className="text-slate-400 block mb-1">Mô tả thông số kỹ thuật & Công năng</span>
              <p className="text-slate-700 bg-white p-3 rounded border border-slate-200 leading-relaxed">
                {detailAsset.description || 'Chưa có thông tin mô tả chi tiết.'}
              </p>
            </div>

            {/* Request history for this asset */}
            <div>
              <span className="text-slate-700 font-semibold block mb-2">Lịch sử cấp phát & mượn thiết bị:</span>
              {dbState.assetRequests.filter(r => r.asset_id === detailAsset.asset_id).length === 0 ? (
                <div className="text-slate-400 italic">Chưa có lượt mượn nào được ghi nhận.</div>
              ) : (
                <div className="space-y-2">
                  {dbState.assetRequests
                    .filter(r => r.asset_id === detailAsset.asset_id)
                    .map(req => {
                      const topic = dbState.topics.find(t => t.topic_id === req.topic_id);
                      return (
                        <div key={req.request_id} className="p-2.5 rounded border border-slate-200 bg-slate-50/50 flex justify-between items-center">
                          <div>
                            <div className="font-medium text-slate-800">
                              Đề tài: {topic?.topic_code || '#' + req.topic_id} - {topic?.title}
                            </div>
                            <div className="text-slate-500 text-[11px] font-mono">
                              {req.start_time} → {req.end_time}
                            </div>
                          </div>
                          <span className="font-medium text-[11px] px-2 py-0.5 rounded bg-white border border-slate-200">
                            {req.status}
                          </span>
                        </div>
                      );
                    })}
                </div>
              )}
            </div>

            <div className="pt-4 border-t border-slate-100 flex justify-end">
              <button
                onClick={() => setDetailAsset(null)}
                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-medium rounded-lg"
              >
                Đóng
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* Delete Confirmation Modal */}
      {deletingAsset && (
        <Modal
          isOpen={!!deletingAsset}
          onClose={() => setDeletingAsset(null)}
          title="Xác nhận xóa thiết bị"
          maxWidth="md"
        >
          <div className="space-y-4 text-xs">
            <p className="text-slate-700 leading-relaxed">
              Bạn có chắc chắn muốn xóa thiết bị <strong className="text-slate-900 font-mono">[{deletingAsset.asset_code}]</strong> {deletingAsset.asset_name} khỏi hệ thống không?
            </p>
            <div className="p-3 bg-amber-50 rounded border border-amber-200 text-amber-900 text-[11px]">
              <strong>Lưu ý:</strong> Thao tác này sẽ cập nhật cơ sở dữ liệu SQL Server. Không thể xóa nếu thiết bị đang được sử dụng hoặc có ràng buộc khóa ngoại chưa thanh lý.
            </div>

            {errorMessage && (
              <div className="p-2.5 bg-rose-50 border border-rose-200 rounded text-rose-800 font-medium">
                {errorMessage}
              </div>
            )}

            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                onClick={() => setDeletingAsset(null)}
                className="px-3.5 py-1.5 text-slate-600 hover:bg-slate-100 font-medium rounded-lg"
              >
                Hủy bỏ
              </button>
              <button
                onClick={confirmDelete}
                className="px-3.5 py-1.5 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-lg shadow-xs"
              >
                Xác nhận xóa
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
