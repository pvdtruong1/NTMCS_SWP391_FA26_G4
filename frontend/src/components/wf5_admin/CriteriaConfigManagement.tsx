/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.3: Criteria Configuration & Weight Management (Cường - WF-05)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { Award, AlertTriangle, CheckCircle2 } from 'lucide-react';

export const CriteriaConfigManagement: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [weights, setWeights] = useState<Record<number, number>>(() => {
    const map: Record<number, number> = {};
    dbState.criteriaConfigs.forEach(c => {
      map[c.criteria_id] = c.weight_percent;
    });
    return map;
  });

  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const totalWeight = Object.values(weights).reduce((sum, w) => sum + (Number(w) || 0), 0);
  const is100Percent = Math.abs(totalWeight - 100) < 0.01;

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!is100Percent) {
      setErrorMessage(`Tổng trọng số hiện tại là ${totalWeight.toFixed(1)}%. Bắt buộc tổng trọng số của 4 tiêu chí phải đúng bằng 100.0%.`);
      return;
    }

    try {
      dbState.criteriaConfigs.forEach(c => {
        const newWeight = weights[c.criteria_id];
        if (newWeight !== undefined) {
          dbStore.updateCriteriaConfig(c.criteria_id, newWeight, currentUser.user_id);
        }
      });
      setSuccessMessage('Đã cập nhật cấu hình trọng số tiêu chí Rubric thành công.');
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err: any) {
      setErrorMessage(err.message || 'Lưu thất bại.');
    }
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div className="border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
          <span>Quản trị Nghiệp vụ</span>
          <span>·</span>
          <span>SCR-5.3</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Cấu hình Tiêu chí Thẩm định Rubric & Trọng số Điểm
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Quy định 4 tiêu chuẩn chấm điểm và phân bổ tỷ lệ phần trăm (Tổng trọng số bắt buộc = 100%).
        </p>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs font-medium flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-rose-600" />
          {errorMessage}
        </div>
      )}

      <form onSubmit={handleSave} className="bg-white p-6 rounded-xl border border-slate-200 shadow-xs space-y-6 text-xs">
        <div className="space-y-4">
          {dbState.criteriaConfigs.map(c => {
            const val = weights[c.criteria_id] !== undefined ? weights[c.criteria_id] : c.weight_percent;
            return (
              <div key={c.criteria_id} className="p-3.5 bg-slate-50 rounded-lg border border-slate-200 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="space-y-0.5">
                  <div className="font-semibold text-slate-900 text-sm">
                    Tiêu chí #{c.criteria_id}: {c.criteria_name}
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Thang điểm đánh giá: 0 đến 10 điểm
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <label className="text-slate-500 font-medium">Trọng số (%):</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="5"
                    required
                    value={val}
                    onChange={e => setWeights({ ...weights, [c.criteria_id]: parseFloat(e.target.value) || 0 })}
                    className="w-20 px-3 py-1.5 bg-white border border-slate-300 rounded-lg font-mono font-bold text-center text-sm focus:outline-none focus:ring-1 focus:ring-red-600"
                  />
                  <span className="font-mono text-slate-500 font-bold">%</span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Sum Indicator */}
        <div className={`p-4 rounded-xl border flex items-center justify-between font-bold text-sm ${
          is100Percent ? 'bg-emerald-50 border-emerald-200 text-emerald-900' : 'bg-rose-50 border-rose-200 text-rose-900'
        }`}>
          <span>TỔNG CỘNG TRỌNG SỐ:</span>
          <span className="font-mono text-base">
            {totalWeight.toFixed(1)}% / 100.0% {is100Percent ? '✓ Hợp lệ' : '✗ Chưa đúng 100%'}
          </span>
        </div>

        <div className="flex justify-end pt-2 border-t border-slate-100">
          <button
            type="submit"
            disabled={!is100Percent}
            className="px-5 py-2 bg-red-700 hover:bg-red-800 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold rounded-lg shadow-xs"
          >
            Lưu thay đổi trọng số Rubric
          </button>
        </div>
      </form>
    </div>
  );
};
