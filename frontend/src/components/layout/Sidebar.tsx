/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Dark Sleek Sidebar strictly adhering to User CSS & Role Boundaries
 */

import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import {
  FileText,
  Search,
  PlusCircle,
  Clock,
  CheckSquare,
  Award,
  Users,
  Archive,
  Truck,
  Box,
  Calendar,
  AlertTriangle,
  Trello,
  ListTodo,
  Sliders,
  BarChart3,
  ShieldCheck,
  UserCheck,
  History,
  FolderTree,
  Target,
  Shield,
  RefreshCw,
  Radio,
  BellRing,
  LogOut,
} from 'lucide-react';

interface SidebarProps {
  currentScreen: string;
  onSelectScreen: (screen: string) => void;
  onOpenActorSwitcher: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentScreen,
  onSelectScreen,
  onOpenActorSwitcher,
}) => {
  const { currentRole, currentUser, roleMeta, logout } = useAuth();
  const dbState = dbStore.getState();

  const deskName = dbState.desks.find(d => d.desk_id === currentUser.desk_id)?.desk_name;

  return (
    <aside id="sidebar">
      {/* Brand Header */}
      <div className="brand">
        <div className="brand-logo">NT</div>
        <div className="flex-1 min-w-0">
          <b>NTMCS</b>
          <small>Tòa soạn Báo chí</small>
        </div>
        <span className="demo" title="Mã vai trò hiện tại">
          {currentRole.role_name.replace('_', ' ')}
        </span>
      </div>

      {/* Navigation (Strictly segregated, zero mixing) */}
      <nav>
        {/* ROLE 1: REPORTER (Hưng) */}
        {currentRole.role_name === 'REPORTER' && (
          <div className="nav-group">
            <div className="nav-label">Phóng viên tác nghiệp</div>
            <button
              onClick={() => onSelectScreen('scr_1_2')}
              className={`nav-item ${currentScreen === 'scr_1_2' ? 'active' : ''}`}
            >
              <ListTodo className="w-4 h-4 shrink-0 text-blue-400" />
              <span>SCR-1.2: Đề tài của tôi</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_1_4')}
              className={`nav-item ${currentScreen === 'scr_1_4' ? 'active' : ''}`}
            >
              <PlusCircle className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>SCR-1.4: Phiếu đề xuất đề tài</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_1_1')}
              className={`nav-item ${currentScreen === 'scr_1_1' ? 'active' : ''}`}
            >
              <FileText className="w-4 h-4 shrink-0 text-indigo-400" />
              <span>SCR-1.1: Dashboard cá nhân</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_1_3')}
              className={`nav-item ${currentScreen === 'scr_1_3' ? 'active' : ''}`}
            >
              <Search className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-1.3: Tra cứu trùng lặp</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_1_5')}
              className={`nav-item ${currentScreen === 'scr_1_5' ? 'active' : ''}`}
            >
              <Truck className="w-4 h-4 shrink-0 text-cyan-400" />
              <span>SCR-1.5: Yêu cầu trang thiết bị</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_1_8')}
              className={`nav-item ${currentScreen === 'scr_1_8' ? 'active' : ''}`}
            >
              <Clock className="w-4 h-4 shrink-0 text-rose-400" />
              <span>SCR-1.8: Đề nghị gia hạn</span>
            </button>
          </div>
        )}

        {/* ROLE 2: DESK_EDITOR (Trường) */}
        {currentRole.role_name === 'DESK_EDITOR' && (
          <div className="nav-group">
            <div className="nav-label">Biên tập Ban ({deskName || 'Ban Thời sự'})</div>
            <button
              onClick={() => onSelectScreen('scr_2_2')}
              className={`nav-item ${currentScreen === 'scr_2_2' ? 'active' : ''}`}
            >
              <CheckSquare className="w-4 h-4 shrink-0 text-blue-400" />
              <span>SCR-2.2: Hàng đợi duyệt Rubric</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_1')}
              className={`nav-item ${currentScreen === 'scr_2_1' ? 'active' : ''}`}
            >
              <BarChart3 className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>SCR-2.1: Dashboard Thẩm định</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_4')}
              className={`nav-item ${currentScreen === 'scr_2_4' ? 'active' : ''}`}
            >
              <PlusCircle className="w-4 h-4 shrink-0 text-indigo-400" />
              <span>SCR-2.4: Giao đề tài định hướng</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_5')}
              className={`nav-item ${currentScreen === 'scr_2_5' ? 'active' : ''}`}
            >
              <Users className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-2.5: Cân bằng tải phóng viên</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_8')}
              className={`nav-item ${currentScreen === 'scr_2_8' ? 'active' : ''}`}
            >
              <Archive className="w-4 h-4 shrink-0 text-slate-400" />
              <span>SCR-2.8: Kho lưu trữ đề tài Ban</span>
            </button>

          </div>
        )}

        {/* ROLE 3: EXECUTIVE_EDITOR (Tổng Biên Tập) */}
        {currentRole.role_name === 'EXECUTIVE_EDITOR' && (
          <div className="nav-group">
            <div className="nav-label">Hội đồng Tổng Biên Tập</div>
            <button
              onClick={() => onSelectScreen('scr_5_1')}
              className={`nav-item ${currentScreen === 'scr_5_1' ? 'active' : ''}`}
            >
              <BarChart3 className="w-4 h-4 shrink-0 text-rose-400" />
              <span>SCR-5.1: Chỉ huy Tổng Biên Tập</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_2')}
              className={`nav-item ${currentScreen === 'scr_2_2' ? 'active' : ''}`}
            >
              <Award className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-2.3: Duyệt Vượt cấp (Rủi ro ≥ 8)</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_5')}
              className={`nav-item ${currentScreen === 'scr_5_5' ? 'active' : ''}`}
            >
              <BarChart3 className="w-4 h-4 shrink-0 text-indigo-400" />
              <span>SCR-5.5: Báo cáo Toàn Tòa soạn</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_2_8')}
              className={`nav-item ${currentScreen === 'scr_2_8' ? 'active' : ''}`}
            >
              <Archive className="w-4 h-4 shrink-0 text-slate-400" />
              <span>SCR-2.8: Kho Đề tài Toàn Tòa soạn</span>
            </button>
          </div>
        )}

        {/* ROLE 4: LOGISTICS_COORDINATOR (Lộc) */}
        {currentRole.role_name === 'LOGISTICS_COORDINATOR' && (
          <div className="nav-group">
            <div className="nav-label">Hậu cần & Thiết bị</div>
            <button
              onClick={() => onSelectScreen('scr_3_2')}
              className={`nav-item ${currentScreen === 'scr_3_2' ? 'active' : ''}`}
            >
              <Box className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>SCR-3.2: Danh mục Kho Tài sản</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_3_1')}
              className={`nav-item ${currentScreen === 'scr_3_1' ? 'active' : ''}`}
            >
              <Truck className="w-4 h-4 shrink-0 text-blue-400" />
              <span>SCR-3.1: Dashboard Hậu cần</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_3_4')}
              className={`nav-item ${currentScreen === 'scr_3_4' ? 'active' : ''}`}
            >
              <Calendar className="w-4 h-4 shrink-0 text-indigo-400" />
              <span>SCR-3.4: Lịch điều phối & Xung đột</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_3_5')}
              className={`nav-item ${currentScreen === 'scr_3_5' ? 'active' : ''}`}
            >
              <AlertTriangle className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-3.5: Hàng đợi cấp phát</span>
            </button>
          </div>
        )}

        {/* ROLE 5: FIELDWORK_COORDINATOR (Đạt) */}
        {currentRole.role_name === 'FIELDWORK_COORDINATOR' && (
          <div className="nav-group">
            <div className="nav-label">Hiện trường & SLA</div>
            <button
              onClick={() => onSelectScreen('scr_4_1')}
              className={`nav-item ${currentScreen === 'scr_4_1' ? 'active' : ''}`}
            >
              <Trello className="w-4 h-4 shrink-0 text-blue-400" />
              <span>Kanban Điều phối tác nghiệp</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_4_2')}
              className={`nav-item ${currentScreen === 'scr_4_2' ? 'active' : ''}`}
            >
              <ListTodo className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>Công việc cá nhân</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_4_6')}
              className={`nav-item ${currentScreen === 'scr_4_6' ? 'active' : ''}`}
            >
              <Sliders className="w-4 h-4 shrink-0 text-amber-400" />
              <span>Cấu hình quy chuẩn SLA</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_4_7')}
              className={`nav-item ${currentScreen === 'scr_4_7' ? 'active' : ''}`}
            >
              <BellRing className="w-4 h-4 shrink-0 text-rose-400" />
              <span>Bàn đôn đốc & nhắc việc</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_4_8')}
              className={`nav-item ${currentScreen === 'scr_4_8' ? 'active' : ''}`}
            >
              <Radio className="w-4 h-4 shrink-0 text-rose-400" />
              <span>Nhật ký phát thông báo & Tỷ lệ đọc</span>
            </button>
          </div>
        )}

        {/* ROLE 6: BUSINESS_ADMINISTRATOR (Cường) */}
        {currentRole.role_name === 'BUSINESS_ADMINISTRATOR' && (
          <div className="nav-group">
            <div className="nav-label">Quản trị Nghiệp vụ</div>
            <button
              onClick={() => onSelectScreen('scr_5_2')}
              className={`nav-item ${currentScreen === 'scr_5_2' ? 'active' : ''}`}
            >
              <Target className="w-4 h-4 shrink-0 text-blue-400" />
              <span>SCR-5.2: Định mức đề tài các Ban</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_1')}
              className={`nav-item ${currentScreen === 'scr_5_1' ? 'active' : ''}`}
            >
              <BarChart3 className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>SCR-5.1: Bảng chỉ huy điều hành</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_3')}
              className={`nav-item ${currentScreen === 'scr_5_3' ? 'active' : ''}`}
            >
              <Sliders className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-5.3: Trọng số Rubric (100%)</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_4')}
              className={`nav-item ${currentScreen === 'scr_5_4' ? 'active' : ''}`}
            >
              <FolderTree className="w-4 h-4 shrink-0 text-indigo-400" />
              <span>SCR-5.4: Chuyên mục & Phòng Ban</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_5')}
              className={`nav-item ${currentScreen === 'scr_5_5' ? 'active' : ''}`}
            >
              <BarChart3 className="w-4 h-4 shrink-0 text-purple-400" />
              <span>SCR-5.5: Thống kê & Phân tích</span>
            </button>
          </div>
        )}

        {/* ROLE 7: SYSTEM_ADMINISTRATOR (Hệ Thống Quản Trị) */}
        {currentRole.role_name === 'SYSTEM_ADMINISTRATOR' && (
          <div className="nav-group">
            <div className="nav-label">Quản trị Hệ thống & Bảo mật</div>
            <button
              onClick={() => onSelectScreen('scr_5_6')}
              className={`nav-item ${currentScreen === 'scr_5_6' ? 'active' : ''}`}
            >
              <UserCheck className="w-4 h-4 shrink-0 text-emerald-400" />
              <span>SCR-5.6: Quản lý Nhân sự & Tài khoản</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_7')}
              className={`nav-item ${currentScreen === 'scr_5_7' ? 'active' : ''}`}
            >
              <ShieldCheck className="w-4 h-4 shrink-0 text-blue-400" />
              <span>SCR-5.7: Ma trận Phân quyền RBAC</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_5_8')}
              className={`nav-item ${currentScreen === 'scr_5_8' ? 'active' : ''}`}
            >
              <History className="w-4 h-4 shrink-0 text-amber-400" />
              <span>SCR-5.8: Nhật ký Kiểm toán (Audit)</span>
            </button>

            <button
              onClick={() => onSelectScreen('scr_4_8')}
              className={`nav-item ${currentScreen === 'scr_4_8' ? 'active' : ''}`}
            >
              <Radio className="w-4 h-4 shrink-0 text-cyan-400" />
              <span>Báo cáo tỷ lệ đọc thông báo</span>
            </button>
          </div>
        )}

      </nav>

      {/* Sidebar Bottom: Current Actor Details & Quick Switch Button */}
      <div className="sidebar-bottom">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-blue-600 text-white font-bold text-xs flex items-center justify-center shrink-0">
            {currentUser.full_name.charAt(0)}
          </div>
          <div className="min-w-0 flex-1">
            <b className="text-white text-xs truncate block">{currentUser.full_name}</b>
            <small className="text-slate-400 text-[11px] truncate block">
              {roleMeta.display_title}
            </small>
          </div>
        </div>

        {deskName && (
          <div className="text-[10px] text-blue-300 truncate">
            🏢 {deskName}
          </div>
        )}

        <div className="grid grid-cols-2 gap-1.5 mt-1">
          <button
            onClick={onOpenActorSwitcher}
            className="py-1.5 px-2 bg-[#19243a] hover:bg-[#26375b] text-blue-200 hover:text-white rounded text-[11px] font-semibold border border-blue-500/20 flex items-center justify-center gap-1 transition-colors cursor-pointer"
            title="Đổi vai trò chuyên môn"
          >
            <RefreshCw className="w-3 h-3 text-blue-400" />
            <span>Đổi vai trò</span>
          </button>
          <button
            onClick={logout}
            className="py-1.5 px-2 bg-rose-950/40 hover:bg-rose-900/60 text-rose-300 hover:text-white rounded text-[11px] font-semibold border border-rose-500/30 flex items-center justify-center gap-1 transition-colors cursor-pointer"
            title="Đăng xuất khỏi hệ thống"
          >
            <LogOut className="w-3 h-3 text-rose-400" />
            <span>Đăng xuất</span>
          </button>
        </div>
      </div>
    </aside>
  );
};
