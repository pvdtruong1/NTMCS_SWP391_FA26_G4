/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Global Spotlight Search Dialog (⌘K)
 */

import React, { useState, useEffect } from 'react';
import { dbStore } from '../../services/dbStore';
import { Topic, Asset, User } from '../../types';
import { Search, X, FileText, Box, User as UserIcon, ArrowRight } from 'lucide-react';
import { TopicStatusBadge, AssetStatusBadge } from './StatusBadge';

interface GlobalSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectTopic: (topicId: number) => void;
  onSelectScreen?: (screen: string) => void;
}

export const GlobalSearchModal: React.FC<GlobalSearchModalProps> = ({
  isOpen,
  onClose,
  onSelectTopic,
  onSelectScreen,
}) => {
  const [query, setQuery] = useState('');
  const dbState = dbStore.getState();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        // Toggle or open modal
      }
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const trimmed = query.trim().toLowerCase();

  const matchingTopics: Topic[] = trimmed.length >= 2
    ? dbState.topics.filter(
        t =>
          t.title.toLowerCase().includes(trimmed) ||
          (t.topic_code && t.topic_code.toLowerCase().includes(trimmed)) ||
          (t.angle && t.angle.toLowerCase().includes(trimmed))
      )
    : [];

  const matchingAssets: Asset[] = trimmed.length >= 2
    ? dbState.assets.filter(
        a =>
          a.asset_name.toLowerCase().includes(trimmed) ||
          (a.serial_number && a.serial_number.toLowerCase().includes(trimmed)) ||
          a.asset_code.toLowerCase().includes(trimmed) ||
          a.asset_category.toLowerCase().includes(trimmed)
      )
    : [];

  const matchingUsers: User[] = trimmed.length >= 2
    ? dbState.users.filter(
        u =>
          u.full_name.toLowerCase().includes(trimmed) ||
          u.email.toLowerCase().includes(trimmed)
      )
    : [];

  return (
    <div className="modal-backdrop">
      <div className="modal" style={{ width: '680px' }}>
        <div className="p-4 border-b border-slate-200 flex items-center gap-3 bg-white">
          <Search className="w-5 h-5 text-blue-600 shrink-0" />
          <input
            autoFocus
            type="text"
            placeholder="Tìm nhanh đề tài, phóng viên, thiết bị (gõ ít nhất 2 ký tự)..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="flex-1 outline-none text-sm text-slate-800 placeholder-slate-400 bg-transparent"
          />
          <kbd className="text-[10px] bg-slate-100 px-2 py-0.5 rounded border border-slate-200 text-slate-500 font-mono">
            ESC để đóng
          </kbd>
          <button onClick={onClose} className="p-1 text-slate-400 hover:text-slate-600">
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-4 max-h-[60vh] overflow-y-auto space-y-4">
          {trimmed.length < 2 && (
            <div className="text-center py-8 text-xs text-slate-400">
              Nhập từ khóa tìm kiếm để tra cứu tức thì toàn bộ CSDL tòa soạn...
            </div>
          )}

          {trimmed.length >= 2 && matchingTopics.length === 0 && matchingAssets.length === 0 && matchingUsers.length === 0 && (
            <div className="text-center py-8 text-xs text-slate-500">
              Không tìm thấy kết quả nào phù hợp với từ khóa <strong className="text-slate-800">"{query}"</strong>
            </div>
          )}

          {/* Topics matches */}
          {matchingTopics.length > 0 && (
            <div>
              <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <FileText className="w-3.5 h-3.5 text-blue-500" />
                <span>Đề tài báo chí ({matchingTopics.length})</span>
              </div>
              <div className="space-y-1.5">
                {matchingTopics.map(t => (
                  <button
                    key={t.topic_id}
                    onClick={() => {
                      onSelectTopic(t.topic_id);
                      onClose();
                    }}
                    className="w-full text-left p-2.5 rounded-lg border border-slate-100 hover:border-blue-300 hover:bg-blue-50/50 transition-colors flex items-center justify-between gap-3 cursor-pointer"
                  >
                    <div className="min-w-0">
                      <div className="text-xs font-bold text-slate-800 truncate">
                        {t.title}
                      </div>
                      <div className="text-[11px] text-slate-500 font-mono">
                        {t.topic_code || `#${t.topic_id}`} · Hạn nộp: {t.deadline}
                      </div>
                    </div>
                    <TopicStatusBadge status={t.status} />
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Assets matches */}
          {matchingAssets.length > 0 && (
            <div>
              <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <Box className="w-3.5 h-3.5 text-emerald-500" />
                <span>Trang thiết bị & Xe máy ({matchingAssets.length})</span>
              </div>
              <div className="space-y-1.5">
                {matchingAssets.map(a => (
                  <div
                    key={a.asset_id}
                    className="p-2.5 rounded-lg border border-slate-100 flex items-center justify-between gap-3 bg-slate-50/50"
                  >
                    <div>
                      <div className="text-xs font-bold text-slate-800">{a.asset_name}</div>
                      <div className="text-[11px] text-slate-500 font-mono">
                        Mã: {a.asset_code} {a.serial_number ? `· SN: ${a.serial_number}` : ''} · Nhóm: {a.asset_category}
                      </div>
                    </div>
                    <AssetStatusBadge status={a.asset_status} />
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Users matches */}
          {matchingUsers.length > 0 && (
            <div>
              <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                <UserIcon className="w-3.5 h-3.5 text-purple-500" />
                <span>Nhân sự tòa soạn ({matchingUsers.length})</span>
              </div>
              <div className="space-y-1.5">
                {matchingUsers.map(u => {
                  const role = dbState.roles.find(r => r.role_id === u.role_id);
                  return (
                    <div
                      key={u.user_id}
                      className="p-2.5 rounded-lg border border-slate-100 flex items-center justify-between gap-3 bg-slate-50/50"
                    >
                      <div>
                        <div className="text-xs font-bold text-slate-800">{u.full_name}</div>
                        <div className="text-[11px] text-slate-500">{u.email}</div>
                      </div>
                      <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-blue-100 text-blue-800 font-bold">
                        {role?.role_name}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
