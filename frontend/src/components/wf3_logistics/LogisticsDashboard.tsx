/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-3.1: Logistics Dashboard (Lộc - WF-03)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { Box, Truck, CheckCircle2, Clock, Wrench, AlertTriangle, ArrowRight } from 'lucide-react';
import { AssetStatusBadge } from '../common/StatusBadge';

interface LogisticsDashboardProps {
  onNavigate: (screen: string) => void;
}

export const LogisticsDashboard: React.FC<LogisticsDashboardProps> = ({ onNavigate }) => {
  const [dbState] = useState(dbStore.getState());

  const totalAssets = dbState.assets.length;
  const availableAssets = dbState.assets.filter(a => a.asset_status === 'AVAILABLE').length;
  const checkedOutAssets = dbState.assets.filter(a => a.asset_status === 'CHECKED_OUT').length;
  const maintenanceAssets = dbState.assets.filter(a => a.asset_status === 'MAINTENANCE').length;

  const pendingRequests = dbState.assetRequests.filter(r => r.status === 'PENDING');
  const activeCheckedOut = dbState.assetRequests.filter(r => r.status === 'CHECKED_OUT');

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hậu cần & Phương tiện</span>
            <span>·</span>
            <span>SCR-3.1</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Bảng Điều hành Hậu cần & Quản trị Thiết bị
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Tổng quan năng lực trang thiết bị, phương tiện chuyên dụng và tình trạng luân chuyển tác nghiệp.
          </p>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Tổng thiết bị trong kho</span>
            <Box className="w-4 h-4 text-slate-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-900">{totalAssets}</div>
          <div className="text-[11px] text-slate-400 mt-1">Đầy đủ 6 danh mục chuyên ngành</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Sẵn sàng tác nghiệp</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-emerald-700">{availableAssets}</div>
          <div className="text-[11px] text-emerald-600 mt-1">Khả dụng cấp phát ngay</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Đang xuất kho hiện trường</span>
            <Truck className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-blue-700">{checkedOutAssets}</div>
          <div className="text-[11px] text-blue-600 mt-1">Đang phục vụ đề tài tác nghiệp</div>
        </div>

        <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 text-xs mb-1">
            <span>Bảo dưỡng / Sửa chữa</span>
            <Wrench className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold font-mono text-amber-700">{maintenanceAssets}</div>
          <div className="text-[11px] text-amber-600 mt-1">Tạm dừng bàn giao kỹ thuật</div>
        </div>
      </div>

      {/* Grid of Pending Requests & Active Checkout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending Requests needing approval */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 text-sm flex items-center gap-2">
              <Clock className="w-4 h-4 text-amber-600" />
              Yêu cầu mượn thiết bị chờ duyệt ({pendingRequests.length})
            </h3>
            <button
              onClick={() => onNavigate('scr_3_5')}
              className="text-xs text-red-700 hover:text-red-800 font-medium inline-flex items-center gap-1 cursor-pointer"
            >
              Xem tất cả <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-2">
            {pendingRequests.length === 0 ? (
              <div className="py-6 text-center text-slate-400 text-xs">
                Không có yêu cầu mượn thiết bị nào đang chờ duyệt.
              </div>
            ) : (
              pendingRequests.map(req => {
                const topic = dbState.topics.find(t => t.topic_id === req.topic_id);
                const asset = dbState.assets.find(a => a.asset_id === req.asset_id);
                const user = dbState.users.find(u => u.user_id === req.requester_user_id);
                return (
                  <div key={req.request_id} className="p-3 rounded-lg border border-slate-200 bg-slate-50 text-xs flex justify-between items-center">
                    <div>
                      <div className="font-semibold text-slate-900">
                        {asset?.asset_name} ({asset?.asset_code})
                      </div>
                      <div className="text-slate-500 text-[11px]">
                        Đề tài: {topic?.topic_code || '#' + req.topic_id} · Người mượn: {user?.full_name}
                      </div>
                      <div className="text-[10px] font-mono text-slate-400 mt-1">
                        Từ {req.start_time} đến {req.end_time}
                      </div>
                    </div>
                    <button
                      onClick={() => onNavigate('scr_3_5')}
                      className="px-2.5 py-1 bg-red-700 text-white rounded text-[11px] font-semibold hover:bg-red-800"
                    >
                      Xử lý
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Currently Checked Out Devices */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="font-semibold text-slate-900 text-sm flex items-center gap-2">
              <Truck className="w-4 h-4 text-blue-600" />
              Thiết bị & Xe đang tác nghiệp ngoài hiện trường
            </h3>
            <button
              onClick={() => onNavigate('scr_3_2')}
              className="text-xs text-red-700 hover:text-red-800 font-medium inline-flex items-center gap-1 cursor-pointer"
            >
              Kho thiết bị <ArrowRight className="w-3 h-3" />
            </button>
          </div>

          <div className="space-y-2">
            {dbState.assets.filter(a => a.asset_status === 'CHECKED_OUT').map(asset => {
              const activeReq = dbState.assetRequests.find(r => r.asset_id === asset.asset_id && r.status === 'CHECKED_OUT');
              const topic = activeReq ? dbState.topics.find(t => t.topic_id === activeReq.topic_id) : null;
              const user = activeReq ? dbState.users.find(u => u.user_id === activeReq.requester_user_id) : null;

              return (
                <div key={asset.asset_id} className="p-3 rounded-lg border border-slate-200 bg-slate-50 text-xs flex justify-between items-center">
                  <div>
                    <div className="font-semibold text-slate-900">{asset.asset_name}</div>
                    <div className="text-slate-500 text-[11px]">
                      Mã: <span className="font-mono">{asset.asset_code}</span> · Biển/Serial: <span className="font-mono">{asset.serial_number || 'N/A'}</span>
                    </div>
                    {topic && (
                      <div className="text-[11px] text-blue-700 font-medium mt-1">
                        Phục vụ: {topic.topic_code || '#' + topic.topic_id} ({user?.full_name})
                      </div>
                    )}
                  </div>
                  <AssetStatusBadge status={asset.asset_status} />
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
