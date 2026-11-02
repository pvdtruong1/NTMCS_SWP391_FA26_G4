/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.4: Categories and Desk Structure Management (Cường - WF-05)
 */

import React, { useState } from 'react';
import { dbStore } from '../../services/dbStore';
import { Desk, Category } from '../../types';
import { FolderTree, Building, Layers } from 'lucide-react';

export const CategoriesDeskManagement: React.FC = () => {
  const [dbState] = useState(dbStore.getState());
  const [selectedDeskId, setSelectedDeskId] = useState<number>(1);

  const selectedDesk = dbState.desks.find(d => d.desk_id === selectedDeskId) || dbState.desks[0];
  const categoriesInDesk = dbState.categories.filter(c => c.desk_id === selectedDeskId);

  return (
    <div className="space-y-6">
      <div className="border-b border-slate-200 pb-4">
        <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
          <span>Quản trị Nghiệp vụ</span>
          <span>·</span>
          <span>SCR-5.4</span>
        </div>
        <h1 className="text-xl font-bold text-slate-900 tracking-tight">
          Cơ cấu Ban Biên tập & Cây Chuyên mục Báo chí
        </h1>
        <p className="text-xs text-slate-500 mt-0.5">
          Quản lý quan hệ phân cấp giữa Ban chuyên môn (Desk) và các Chuyên mục (Category) xuất bản bài viết.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Desks list */}
        <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-3 text-xs">
          <div className="font-bold text-slate-800 text-xs uppercase tracking-wider flex items-center gap-1.5 pb-2 border-b border-slate-100">
            <Building className="w-4 h-4 text-slate-500" /> Các Ban Chuyên Môn
          </div>

          <div className="space-y-1.5">
            {dbState.desks.map(d => {
              const count = dbState.categories.filter(c => c.desk_id === d.desk_id).length;
              const isSelected = d.desk_id === selectedDeskId;

              return (
                <button
                  key={d.desk_id}
                  onClick={() => setSelectedDeskId(d.desk_id)}
                  className={`w-full text-left p-3 rounded-lg border transition-colors flex items-center justify-between cursor-pointer ${
                    isSelected ? 'bg-red-50/70 border-red-300 font-semibold text-red-950' : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <span>{d.desk_name}</span>
                  <span className="text-[11px] font-mono bg-white px-2 py-0.5 rounded border border-slate-200 text-slate-600">
                    {count} mục
                  </span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Categories under selected desk */}
        <div className="md:col-span-2 bg-white rounded-xl border border-slate-200 p-4 shadow-xs space-y-4 text-xs">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <div>
              <span className="font-bold text-slate-900 text-sm">{selectedDesk?.desk_name}</span>
              <span className="text-slate-400 block text-[11px]">Danh sách chuyên mục và chuyên đề trực thuộc</span>
            </div>
            <span className="font-mono text-slate-500 bg-slate-100 px-2 py-1 rounded">
              Desk ID: {selectedDesk?.desk_id}
            </span>
          </div>

          <div className="space-y-2">
            {categoriesInDesk.length === 0 ? (
              <div className="py-8 text-center text-slate-400">Ban này chưa có chuyên mục nào.</div>
            ) : (
              categoriesInDesk.map(cat => {
                const parentCat = dbState.categories.find(c => c.category_id === cat.parent_category_id);
                const topicCount = dbState.topics.filter(t => t.category_id === cat.category_id).length;

                return (
                  <div key={cat.category_id} className="p-3 bg-slate-50 rounded-lg border border-slate-200 flex items-center justify-between">
                    <div className="space-y-0.5">
                      <div className="font-semibold text-slate-900 text-sm flex items-center gap-2">
                        <Layers className="w-4 h-4 text-slate-400" />
                        {cat.category_name}
                      </div>
                      {parentCat && (
                        <div className="text-[11px] text-slate-500 ml-6">
                          Chuyên đề nhánh con của: <strong className="text-slate-700">{parentCat.category_name}</strong>
                        </div>
                      )}
                    </div>
                    <div className="text-right font-mono text-[11px] text-slate-600">
                      <strong>{topicCount}</strong> đề tài trực thuộc
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
