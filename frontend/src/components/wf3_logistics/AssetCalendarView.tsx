/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-3.4: Asset Calendar and Availability View (Lộc - WF-03)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { AssetStatusBadge, getAssetCategoryLabel } from '../common/StatusBadge';
import { Calendar as CalendarIcon, Clock, CheckCircle2, AlertCircle } from 'lucide-react';

export const AssetCalendarView: React.FC = () => {
  const [dbState] = useState(dbStore.getState());
  const [selectedAssetId, setSelectedAssetId] = useState<number | 'ALL'>('ALL');

  const assets = dbState.assets;
  const requests = dbState.assetRequests.filter(r => r.status === 'APPROVED' || r.status === 'CHECKED_OUT');

  const displayedAssets = selectedAssetId === 'ALL'
    ? assets
    : assets.filter(a => a.asset_id === selectedAssetId);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Hậu cần & Phương tiện</span>
            <span>·</span>
            <span>SCR-3.4</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Lịch Điều phối & Khả dụng Thiết bị
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Trực quan hóa lịch trình sử dụng xe máy và thiết bị tác nghiệp, phục vụ tra cứu khả dụng theo mốc thời gian.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs text-slate-500 font-medium">Lọc theo thiết bị:</label>
          <select
            value={selectedAssetId}
            onChange={e => setSelectedAssetId(e.target.value === 'ALL' ? 'ALL' : Number(e.target.value))}
            className="px-3 py-1.5 text-xs bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-red-600"
          >
            <option value="ALL">-- Tất cả thiết bị --</option>
            {assets.map(a => (
              <option key={a.asset_id} value={a.asset_id}>
                [{a.asset_code}] {a.asset_name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Timeline Grid */}
      <div className="space-y-4">
        {displayedAssets.map(asset => {
          const assetBookings = requests.filter(r => r.asset_id === asset.asset_id);
          const isBusy = asset.asset_status === 'CHECKED_OUT';

          return (
            <div key={asset.asset_id} className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3">
              <div className="flex items-center justify-between flex-wrap gap-2">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-slate-100 flex items-center justify-center font-bold font-mono text-xs text-slate-700">
                    {asset.asset_code.split('-')[0]}
                  </div>
                  <div>
                    <h3 className="font-semibold text-slate-900 text-sm">
                      [{asset.asset_code}] {asset.asset_name}
                    </h3>
                    <div className="text-xs text-slate-500">
                      {getAssetCategoryLabel(asset.asset_category)} · Serial: <span className="font-mono">{asset.serial_number || 'N/A'}</span>
                    </div>
                  </div>
                </div>
                <AssetStatusBadge status={asset.asset_status} />
              </div>

              {/* Bookings timeline */}
              <div className="pt-2 border-t border-slate-100">
                <div className="text-xs font-semibold text-slate-700 mb-2 flex items-center gap-1.5">
                  <CalendarIcon className="w-3.5 h-3.5 text-slate-400" />
                  Lịch trình tác nghiệp hiện trường đã cấp phát:
                </div>

                {assetBookings.length === 0 ? (
                  <div className="p-3 bg-emerald-50/50 rounded-lg border border-emerald-100 text-emerald-800 text-xs flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                    Thiết bị hiện hoàn toàn trống lịch trong các đợt tác nghiệp sắp tới.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                    {assetBookings.map(b => {
                      const topic = dbState.topics.find(t => t.topic_id === b.topic_id);
                      const user = dbState.users.find(u => u.user_id === b.requester_user_id);
                      return (
                        <div key={b.request_id} className="p-2.5 rounded-lg border border-slate-200 bg-slate-50 text-xs flex flex-col justify-between">
                          <div>
                            <div className="font-medium text-slate-800">
                              {topic?.topic_code || `Đề tài #${b.topic_id}`}: {topic?.title}
                            </div>
                            <div className="text-slate-500 text-[11px] mt-0.5">
                              Người mượn: {user?.full_name}
                            </div>
                          </div>
                          <div className="mt-2 text-[11px] font-mono text-slate-600 flex items-center gap-1 bg-white px-2 py-1 rounded border border-slate-200 w-fit">
                            <Clock className="w-3 h-3 text-slate-400" />
                            {b.start_time} → {b.end_time}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
