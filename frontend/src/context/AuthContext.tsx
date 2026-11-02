/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Authentication Context & Strict Role-Based Isolation
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role } from '../types';
import { dbStore } from '../services/dbStore';

export interface RoleMeta {
  role_name: string;
  display_title: string;
  workflow_label: string;
  description: string;
  default_screen: string;
  allowed_screens: string[];
}

export const ROLE_DEFINITIONS: Record<string, RoleMeta> = {
  REPORTER: {
    role_name: 'REPORTER',
    display_title: 'Phóng viên Tác nghiệp',
    workflow_label: 'WF-01 · Đăng ký & Quản lý Đề tài (Hưng)',
    description: 'Chủ động khởi tạo đề tài, lập dự toán kinh phí, phân công tổ tác nghiệp và nộp bản thảo.',
    default_screen: 'scr_1_2',
    allowed_screens: ['scr_1_1', 'scr_1_2', 'scr_1_3', 'scr_1_4', 'scr_1_5', 'scr_1_8'],
  },
  DESK_EDITOR: {
    role_name: 'DESK_EDITOR',
    display_title: 'Biên tập viên Ban (Trưởng Ban)',
    workflow_label: 'WF-02 · Thẩm định Rubric & Phê duyệt (Trường)',
    description: 'Thẩm định đề tài theo rubric 4 tiêu chí, phân luồng rủi ro pháp lý, giao đề tài định hướng và cân bằng tải.',
    default_screen: 'scr_2_2',
    allowed_screens: ['scr_2_1', 'scr_2_2', 'scr_2_4', 'scr_2_5', 'scr_2_8'],
  },
  EXECUTIVE_EDITOR: {
    role_name: 'EXECUTIVE_EDITOR',
    display_title: 'Tổng Biên Tập (Ban Biên Tập Cấp Cao)',
    workflow_label: 'Hội đồng Tổng Biên Tập · Thẩm định Vượt cấp',
    description: 'Phê duyệt các đề tài có rủi ro pháp lý cao (Legal Risk >= 8.0), kiểm soát hạn ngạch xuất bản toàn tòa soạn.',
    default_screen: 'scr_5_1',
    allowed_screens: ['scr_5_1', 'scr_2_2', 'scr_2_8', 'scr_5_5'],
  },
  LOGISTICS_COORDINATOR: {
    role_name: 'LOGISTICS_COORDINATOR',
    display_title: 'Điều phối viên Hậu cần & Xe máy',
    workflow_label: 'WF-03 · Quản lý Kho & Phương tiện (Lộc)',
    description: 'Quản lý kho máy quay, xe chuyên dụng, duyệt mượn thiết bị và kiểm tra xung đột lịch tác nghiệp.',
    default_screen: 'scr_3_2',
    allowed_screens: ['scr_3_1', 'scr_3_2', 'scr_3_3', 'scr_3_4', 'scr_3_5', 'scr_3_6', 'scr_3_7'],
  },
  FIELDWORK_COORDINATOR: {
    role_name: 'FIELDWORK_COORDINATOR',
    display_title: 'Điều phối viên Hiện trường & Giám sát SLA',
    workflow_label: 'WF-04 · Hiện trường Kanban & SLA (Đạt)',
    description: 'Điều phối bảng Kanban tác nghiệp, giám sát tiến độ nộp sản phẩm, tiếp nhận sự cố và nhật ký phát thông báo.',
    default_screen: 'scr_4_1',
    allowed_screens: ['scr_4_1', 'scr_4_2', 'scr_4_3', 'scr_4_4', 'scr_4_6', 'scr_4_7', 'scr_4_8'],
  },
  BUSINESS_ADMINISTRATOR: {
    role_name: 'BUSINESS_ADMINISTRATOR',
    display_title: 'Quản trị viên Nghiệp vụ Tòa soạn',
    workflow_label: 'WF-05 · Quản trị Nghiệp vụ & Định mức (Cường)',
    description: 'Giao định mức đề tài cho các ban, cấu hình trọng số rubric 100%, quản lý cây chuyên mục và xuất file bảng chỉ huy.',
    default_screen: 'scr_5_2',
    allowed_screens: ['scr_5_1', 'scr_5_2', 'scr_5_3', 'scr_5_4', 'scr_5_5'],
  },
  SYSTEM_ADMINISTRATOR: {
    role_name: 'SYSTEM_ADMINISTRATOR',
    display_title: 'Quản trị viên Hệ thống & Bảo mật',
    workflow_label: 'Quản trị Hệ thống & Ma trận Phân quyền',
    description: 'Quản trị hồ sơ tài khoản nhân sự, phân quyền RBAC, chuyển nhanh vai trò và tra cứu nhật ký kiểm toán hệ thống.',
    default_screen: 'scr_5_6',
    allowed_screens: ['scr_5_6', 'scr_5_7', 'scr_5_8', 'scr_4_8'],
  },
};

interface AuthContextType {
  currentUser: User;
  currentRole: Role;
  roleMeta: RoleMeta;
  isLoggedIn: boolean;
  loginAsUser: (userId: number) => void;
  loginWithCredentials: (email: string, password_hash: string) => boolean;
  logout: () => void;
  isScreenAllowed: (screen: string) => boolean;
  allUsers: User[];
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const CURRENT_USER_KEY = 'NTMCS_CURRENT_USER_ID';
const LOGGED_IN_KEY = 'NTMCS_IS_LOGGED_IN';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [dbState, setDbState] = useState(dbStore.getState());
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(() => {
    const saved = localStorage.getItem(LOGGED_IN_KEY);
    return saved !== null ? saved === 'true' : true; // Default logged in for smooth preview
  });

  const [currentUserId, setCurrentUserId] = useState<number>(() => {
    const saved = localStorage.getItem(CURRENT_USER_KEY);
    return saved ? parseInt(saved, 10) : 2; // Default Hưng (REPORTER)
  });

  useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const currentUser = dbState.users.find(u => u.user_id === currentUserId) || dbState.users[0] || {
    user_id: 2,
    full_name: 'Hưng',
    email: 'hung.reporter@newsroom.vn',
    password_hash: '123',
    role_id: 1,
    active_status: true,
    desk_id: 1,
  };

  const currentRole = dbState.roles.find(r => r.role_id === currentUser.role_id) || {
    role_id: 1,
    role_name: 'REPORTER' as const,
  };

  const roleMeta = ROLE_DEFINITIONS[currentRole.role_name] || ROLE_DEFINITIONS.REPORTER;

  const loginAsUser = (userId: number) => {
    const state = dbStore.getState();
    const user = state.users.find(u => u.user_id === userId) || dbState.users.find(u => u.user_id === userId);
    if (user && user.active_status) {
      setCurrentUserId(userId);
      setIsLoggedIn(true);
      try {
        localStorage.setItem(CURRENT_USER_KEY, String(userId));
        localStorage.setItem(LOGGED_IN_KEY, 'true');
      } catch (e) {
        console.warn('LocalStorage error:', e);
      }
    }
  };

  const loginWithCredentials = (email: string, password: string): boolean => {
    const state = dbStore.getState();
    const user = (state.users || dbState.users).find(
      u => u.email.toLowerCase() === email.trim().toLowerCase() && u.active_status
    );
    if (user) {
      // In local demo, passwords are '123' or accept match
      if (user.password_hash === password || password === '123' || !user.password_hash) {
        loginAsUser(user.user_id);
        return true;
      }
    }
    return false;
  };

  const logout = () => {
    setIsLoggedIn(false);
    try {
      localStorage.setItem(LOGGED_IN_KEY, 'false');
    } catch (e) {
      console.warn('LocalStorage error:', e);
    }
  };

  const isScreenAllowed = (screen: string): boolean => {
    return roleMeta.allowed_screens.includes(screen);
  };

  return (
    <AuthContext.Provider
      value={{
        currentUser,
        currentRole,
        roleMeta,
        isLoggedIn,
        loginAsUser,
        loginWithCredentials,
        logout,
        isScreenAllowed,
        allUsers: dbState.users,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
