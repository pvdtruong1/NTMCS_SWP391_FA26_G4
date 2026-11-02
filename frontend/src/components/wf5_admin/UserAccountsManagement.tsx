/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.6: User Accounts Management & Full CRUD (System Administrator)
 * Supports Create, Read, Update, Delete, Quick Role Switching, and Actor Sign-in.
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { User, RoleName } from '../../types';
import { Modal } from '../common/Modal';
import {
  UserCheck,
  UserX,
  Search,
  Shield,
  Building,
  UserPlus,
  Edit2,
  Trash2,
  LogIn,
  CheckCircle2,
  AlertTriangle,
  RotateCcw,
  Sparkles,
} from 'lucide-react';

export const UserAccountsManagement: React.FC = () => {
  const { currentUser, loginAsUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Modals state
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deletingUser, setDeletingUser] = useState<User | null>(null);

  // Form states for Create/Edit
  const [formName, setFormName] = useState('');
  const [formEmail, setFormEmail] = useState('');
  const [formPassword, setFormPassword] = useState('123');
  const [formRoleId, setFormRoleId] = useState<number>(1);
  const [formDeskId, setFormDeskId] = useState<number | null>(1);
  const [formActive, setFormActive] = useState<boolean>(true);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const notifySuccess = (msg: string) => {
    setSuccessMessage(msg);
    setErrorMessage(null);
    setTimeout(() => setSuccessMessage(null), 3500);
  };

  const notifyError = (msg: string) => {
    setErrorMessage(msg);
    setTimeout(() => setErrorMessage(null), 4000);
  };

  // Toggle active status
  const handleToggleStatus = (user: User) => {
    try {
      const nextStatus = !user.active_status;
      dbStore.updateUserStatus(user.user_id, nextStatus, currentUser.user_id);
      notifySuccess(`Đã ${nextStatus ? 'mở khóa' : 'khóa'} tài khoản ${user.email} thành công.`);
    } catch (err: any) {
      notifyError(err.message || 'Cập nhật thất bại.');
    }
  };

  // Open Create Modal
  const openCreateModal = () => {
    setFormName('');
    setFormEmail('');
    setFormPassword('123');
    setFormRoleId(1);
    setFormDeskId(1);
    setFormActive(true);
    setIsCreateModalOpen(true);
  };

  // Submit Create
  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    try {
      dbStore.createUser(
        {
          full_name: formName,
          email: formEmail,
          password_hash: formPassword,
          role_id: formRoleId,
          desk_id: formDeskId,
          active_status: formActive,
        },
        currentUser.user_id
      );
      setIsCreateModalOpen(false);
      notifySuccess(`Đã thêm mới tài khoản nhân viên ${formName} thành công.`);
    } catch (err: any) {
      notifyError(err.message || 'Thêm tài khoản thất bại.');
    }
  };

  // Open Edit Modal
  const openEditModal = (user: User) => {
    setEditingUser(user);
    setFormName(user.full_name);
    setFormEmail(user.email);
    setFormRoleId(user.role_id);
    setFormDeskId(user.desk_id);
    setFormActive(user.active_status);
  };

  // Submit Edit
  const handleEditSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;
    try {
      dbStore.updateUser(
        editingUser.user_id,
        {
          full_name: formName,
          email: formEmail,
          role_id: formRoleId,
          desk_id: formDeskId,
          active_status: formActive,
        },
        currentUser.user_id
      );
      setEditingUser(null);
      notifySuccess(`Đã cập nhật hồ sơ nhân sự [${formName}] thành công.`);
    } catch (err: any) {
      notifyError(err.message || 'Cập nhật hồ sơ thất bại.');
    }
  };

  // Delete User
  const handleDeleteConfirm = () => {
    if (!deletingUser) return;
    try {
      dbStore.deleteUser(deletingUser.user_id, currentUser.user_id);
      notifySuccess(`Đã xóa tài khoản nhân sự [${deletingUser.full_name}] thành công.`);
      setDeletingUser(null);
    } catch (err: any) {
      notifyError(err.message || 'Xóa nhân sự thất bại.');
    }
  };

  // Quick Role Change from table dropdown
  const handleQuickRoleChange = (userId: number, newRoleId: number) => {
    try {
      dbStore.changeUserRole(userId, newRoleId, currentUser.user_id);
      notifySuccess(`Đã cập nhật vai trò mới cho nhân sự.`);
    } catch (err: any) {
      notifyError(err.message || 'Chuyển vai trò thất bại.');
    }
  };

  const filteredUsers = dbState.users.filter(u => {
    if (roleFilter !== 'ALL' && u.role_id !== Number(roleFilter)) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.trim().toLowerCase();
      return u.full_name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
    }
    return true;
  });

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Quản trị Hệ thống</span>
            <span>·</span>
            <span className="font-semibold text-blue-600">SCR-5.6</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Quản trị Nhân sự & Tài khoản Người dùng (CRUD)
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Quản lý toàn diện hồ sơ nhân viên tòa soạn, thêm mới, sửa, xóa, chuyển nhanh vai trò và kiểm soát quyền truy cập.
          </p>
        </div>

        <button
          onClick={openCreateModal}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-lg shadow-xs flex items-center gap-1.5 transition-colors cursor-pointer shrink-0"
        >
          <UserPlus className="w-4 h-4" />
          <span>Thêm Nhân viên mới (Create)</span>
        </button>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-semibold flex items-center gap-2 shadow-xs">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{successMessage}</span>
        </div>
      )}

      {errorMessage && (
        <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 text-xs font-semibold flex items-center gap-2 shadow-xs">
          <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Filter toolbar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm theo họ tên hoặc email..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white"
          />
        </div>

        <div className="w-full sm:w-auto flex items-center gap-2">
          <select
            value={roleFilter}
            onChange={e => setRoleFilter(e.target.value)}
            className="w-full sm:w-auto px-3 py-1.5 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-600"
          >
            <option value="ALL">-- Tất cả vai trò ({dbState.roles.length}) --</option>
            {dbState.roles.map(r => (
              <option key={r.role_id} value={r.role_id}>
                {r.role_name}
              </option>
            ))}
          </select>

          <span className="text-xs text-slate-500 font-medium whitespace-nowrap">
            Tổng: <strong>{filteredUsers.length}</strong> nhân sự
          </span>
        </div>
      </div>

      {/* User Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider text-[11px]">
                <th className="py-3 px-4">Mã NV</th>
                <th className="py-3 px-4">Họ và Tên</th>
                <th className="py-3 px-4">Email Đăng nhập</th>
                <th className="py-3 px-4 min-w-[170px]">Chuyển nhanh vai trò</th>
                <th className="py-3 px-4">Ban chuyên môn</th>
                <th className="py-3 px-4">Trạng thái</th>
                <th className="py-3 px-4 text-right">Hành động CRUD & Switch</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filteredUsers.map(user => {
                const role = dbState.roles.find(r => r.role_id === user.role_id);
                const desk = dbState.desks.find(d => d.desk_id === user.desk_id);
                const isCurrentActor = user.user_id === currentUser.user_id;

                return (
                  <tr key={user.user_id} className={`hover:bg-slate-50/70 transition-colors ${isCurrentActor ? 'bg-blue-50/40' : ''}`}>
                    <td className="py-3 px-4 font-mono font-medium text-slate-800 whitespace-nowrap">
                      USR-{String(user.user_id).padStart(3, '0')}
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-blue-100 text-blue-700 font-bold text-xs flex items-center justify-center shrink-0">
                          {user.full_name.charAt(0)}
                        </div>
                        <div>
                          <div className="font-semibold text-slate-900 flex items-center gap-1.5">
                            <span>{user.full_name}</span>
                            {isCurrentActor && (
                              <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-blue-600 text-white">
                                Bạn đang đăng nhập
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-600 whitespace-nowrap">
                      {user.email}
                    </td>
                    <td className="py-3 px-4">
                      {/* Fast Role Change Dropdown */}
                      <select
                        value={user.role_id}
                        onChange={e => handleQuickRoleChange(user.user_id, Number(e.target.value))}
                        className="px-2 py-1 bg-white border border-slate-300 rounded text-xs font-semibold text-slate-800 focus:outline-none focus:ring-1 focus:ring-blue-600 hover:border-blue-400 cursor-pointer shadow-2xs"
                        title="Đổi nhanh vai trò cho nhân sự này"
                      >
                        {dbState.roles.map(r => (
                          <option key={r.role_id} value={r.role_id}>
                            {r.role_name}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="py-3 px-4 text-slate-600 whitespace-nowrap">
                      {desk?.desk_name || <span className="text-slate-400 italic">Khối Điều hành / Trực thuộc</span>}
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold border ${
                        user.active_status ? 'bg-emerald-50 text-emerald-800 border-emerald-200' : 'bg-rose-50 text-rose-800 border-rose-200'
                      }`}>
                        {user.active_status ? 'Hoạt động' : 'Đã khóa'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right whitespace-nowrap">
                      <div className="flex items-center justify-end gap-1.5">
                        {/* Switch / Login as this user */}
                        <button
                          onClick={() => loginAsUser(user.user_id)}
                          className={`px-2 py-1 rounded text-[11px] font-semibold flex items-center gap-1 transition-colors cursor-pointer ${
                            isCurrentActor
                              ? 'bg-blue-100 text-blue-800 border border-blue-200'
                              : 'bg-slate-100 hover:bg-blue-50 text-slate-700 hover:text-blue-700'
                          }`}
                          title="Đăng nhập ngay với tư cách tài khoản này (Switch Actor)"
                        >
                          <LogIn className="w-3 h-3 text-blue-600" />
                          <span>Đăng nhập vai trò này</span>
                        </button>

                        {/* Edit */}
                        <button
                          onClick={() => openEditModal(user)}
                          className="p-1 hover:bg-slate-100 rounded text-slate-600 hover:text-blue-600 cursor-pointer"
                          title="Sửa thông tin hồ sơ (Update)"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>

                        {/* Lock / Unlock */}
                        <button
                          onClick={() => handleToggleStatus(user)}
                          className={`p-1 rounded cursor-pointer ${
                            user.active_status ? 'hover:bg-rose-50 text-slate-400 hover:text-rose-600' : 'hover:bg-emerald-50 text-rose-500 hover:text-emerald-600'
                          }`}
                          title={user.active_status ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
                        >
                          {user.active_status ? <UserX className="w-3.5 h-3.5" /> : <UserCheck className="w-3.5 h-3.5" />}
                        </button>

                        {/* Delete */}
                        {!isCurrentActor && (
                          <button
                            onClick={() => setDeletingUser(user)}
                            className="p-1 hover:bg-rose-50 rounded text-slate-400 hover:text-rose-600 cursor-pointer"
                            title="Xóa nhân sự khỏi hệ thống (Delete)"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* CREATE USER MODAL */}
      {isCreateModalOpen && (
        <Modal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          title="Thêm Mới Hồ Sơ & Tài Khoản Nhân Viên (SCR-5.6 Create)"
          subtitle="Tạo mới nhân sự và cấp tài khoản truy cập vào hệ thống NTMCS"
          maxWidth="md"
        >
          <form onSubmit={handleCreateSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-800 mb-1">
                Họ và Tên Nhân Viên <span className="text-rose-600">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="VD: Nguyễn Văn A"
                value={formName}
                onChange={e => setFormName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-800 mb-1">
                Email Đăng Nhập (Nội bộ) <span className="text-rose-600">*</span>
              </label>
              <input
                type="email"
                required
                placeholder="VD: nva.reporter@newsroom.vn"
                value={formEmail}
                onChange={e => setFormEmail(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white font-mono"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-800 mb-1">
                Mật khẩu khởi tạo
              </label>
              <input
                type="text"
                value={formPassword}
                onChange={e => setFormPassword(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white font-mono"
              />
              <span className="text-[10px] text-slate-400">Mặc định: 123</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-800 mb-1">
                  Vai trò chức danh (Role) <span className="text-rose-600">*</span>
                </label>
                <select
                  value={formRoleId}
                  onChange={e => setFormRoleId(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600"
                >
                  {dbState.roles.map(r => (
                    <option key={r.role_id} value={r.role_id}>
                      {r.role_name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-800 mb-1">
                  Ban Chuyên môn trực thuộc
                </label>
                <select
                  value={formDeskId || ''}
                  onChange={e => setFormDeskId(e.target.value ? Number(e.target.value) : null)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600"
                >
                  <option value="">-- Không trực thuộc (Khối Điều hành) --</option>
                  {dbState.desks.map(d => (
                    <option key={d.desk_id} value={d.desk_id}>
                      {d.desk_name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-1">
              <input
                type="checkbox"
                id="formActiveCheck"
                checked={formActive}
                onChange={e => setFormActive(e.target.checked)}
                className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500"
              />
              <label htmlFor="formActiveCheck" className="font-semibold text-slate-800 text-xs cursor-pointer">
                Kích hoạt tài khoản ngay (Cho phép đăng nhập)
              </label>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-200">
              <button
                type="button"
                onClick={() => setIsCreateModalOpen(false)}
                className="px-4 py-2 border border-slate-300 hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-xs cursor-pointer"
              >
                Hủy bỏ
              </button>
              <button
                type="submit"
                className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg text-xs flex items-center gap-1.5 cursor-pointer shadow-xs"
              >
                <UserPlus className="w-3.5 h-3.5" />
                <span>Tạo tài khoản mới</span>
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* EDIT USER MODAL */}
      {editingUser && (
        <Modal
          isOpen={!!editingUser}
          onClose={() => setEditingUser(null)}
          title={`Chỉnh Sửa Hồ Sơ Nhân Sự [USR-${String(editingUser.user_id).padStart(3, '0')}]`}
          subtitle={editingUser.full_name}
          maxWidth="md"
        >
          <form onSubmit={handleEditSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block font-semibold text-slate-800 mb-1">
                Họ và Tên Nhân Viên <span className="text-rose-600">*</span>
              </label>
              <input
                type="text"
                required
                value={formName}
                onChange={e => setFormName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white"
              />
            </div>

            <div>
              <label className="block font-semibold text-slate-800 mb-1">
                Email Đăng Nhập <span className="text-rose-600">*</span>
              </label>
              <input
                type="email"
                required
                value={formEmail}
                onChange={e => setFormEmail(e.target.value)}
                className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600 focus:bg-white font-mono"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-slate-800 mb-1">
                  Vai trò (Role)
                </label>
                <select
                  value={formRoleId}
                  onChange={e => setFormRoleId(Number(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600"
                >
                  {dbState.roles.map(r => (
                    <option key={r.role_id} value={r.role_id}>
                      {r.role_name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-800 mb-1">
                  Ban Chuyên môn
                </label>
                <select
                  value={formDeskId || ''}
                  onChange={e => setFormDeskId(e.target.value ? Number(e.target.value) : null)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-xs focus:outline-none focus:ring-1 focus:ring-blue-600"
                >
                  <option value="">-- Không trực thuộc --</option>
                  {dbState.desks.map(d => (
                    <option key={d.desk_id} value={d.desk_id}>
                      {d.desk_name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-1">
              <input
                type="checkbox"
                id="editFormActiveCheck"
                checked={formActive}
                onChange={e => setFormActive(e.target.checked)}
                className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500"
              />
              <label htmlFor="editFormActiveCheck" className="font-semibold text-slate-800 text-xs cursor-pointer">
                Tài khoản hoạt động (Active)
              </label>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-200">
              <button
                type="button"
                onClick={() => setEditingUser(null)}
                className="px-4 py-2 border border-slate-300 hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-xs cursor-pointer"
              >
                Hủy bỏ
              </button>
              <button
                type="submit"
                className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg text-xs flex items-center gap-1.5 cursor-pointer shadow-xs"
              >
                <Edit2 className="w-3.5 h-3.5" />
                <span>Lưu thay đổi hồ sơ</span>
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {deletingUser && (
        <Modal
          isOpen={!!deletingUser}
          onClose={() => setDeletingUser(null)}
          title="Xác Nhận Xóa Nhân Sự (SCR-5.6 Delete)"
          subtitle={`Xác nhận xóa tài khoản: ${deletingUser.full_name}`}
          maxWidth="sm"
        >
          <div className="space-y-4 text-xs">
            <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg text-rose-800 space-y-1">
              <div className="font-bold flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0" />
                <span>Hành động này không thể hoàn tác!</span>
              </div>
              <p className="text-[11px] leading-relaxed">
                Bạn có chắc chắn muốn xóa tài khoản <strong>{deletingUser.full_name}</strong> ({deletingUser.email}) khỏi hệ thống NTMCS?
              </p>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-slate-200">
              <button
                type="button"
                onClick={() => setDeletingUser(null)}
                className="px-4 py-2 border border-slate-300 hover:bg-slate-50 text-slate-700 font-semibold rounded-lg text-xs cursor-pointer"
              >
                Hủy bỏ
              </button>
              <button
                type="button"
                onClick={handleDeleteConfirm}
                className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white font-semibold rounded-lg text-xs flex items-center gap-1.5 cursor-pointer shadow-xs"
              >
                <Trash2 className="w-3.5 h-3.5" />
                <span>Xóa vĩnh viễn</span>
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
