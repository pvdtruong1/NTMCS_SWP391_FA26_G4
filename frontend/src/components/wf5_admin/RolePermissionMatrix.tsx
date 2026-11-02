/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * SCR-5.7: Role and Permission Matrix Management (Cường - WF-05)
 */

import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { ShieldCheck, Check, X } from 'lucide-react';

export const RolePermissionMatrix: React.FC = () => {
  const { currentUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const roles = dbState.roles;
  const permissions = dbState.permissions;

  const hasPerm = (roleId: number, permissionId: number) => {
    return dbState.rolePermissions.some(rp => rp.role_id === roleId && rp.permission_id === permissionId);
  };

  const handleToggle = (roleId: number, permissionId: number) => {
    const current = hasPerm(roleId, permissionId);
    dbStore.toggleRolePermission(roleId, permissionId, !current, currentUser.user_id);
    setSuccessMessage(`Đã cập nhật quyền thành công.`);
    setTimeout(() => setSuccessMessage(null), 2000);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 text-xs text-slate-500 mb-1">
            <span>Bảo mật & Phân quyền</span>
            <span>·</span>
            <span>SCR-5.7</span>
          </div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">
            Ma trận Phân quyền Vai trò (RBAC Matrix)
          </h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Cấu hình liên kết giữa 7 vai trò chuẩn và 8 quyền nghiệp vụ trong hệ thống NTMCS.
          </p>
        </div>
      </div>

      {successMessage && (
        <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-lg text-emerald-800 text-xs font-medium">
          {successMessage}
        </div>
      )}

      {/* Matrix Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden text-xs">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-slate-700 font-semibold text-[11px]">
                <th className="py-3 px-4 min-w-[240px]">Quyền nghiệp vụ (Permission Code)</th>
                {roles.map(r => (
                  <th key={r.role_id} className="py-3 px-3 text-center whitespace-nowrap font-mono">
                    {r.role_name}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {permissions.map(perm => (
                <tr key={perm.permission_id} className="hover:bg-slate-50/70">
                  <td className="py-3 px-4">
                    <div className="font-mono font-bold text-slate-900">{perm.permission_code}</div>
                    <div className="text-[11px] text-slate-500">{perm.description}</div>
                  </td>
                  {roles.map(role => {
                    const active = hasPerm(role.role_id, perm.permission_id);
                    const isAdmin = role.role_name === 'SYSTEM_ADMINISTRATOR';

                    return (
                      <td key={role.role_id} className="py-3 px-3 text-center">
                        <button
                          disabled={isAdmin}
                          onClick={() => handleToggle(role.role_id, perm.permission_id)}
                          title={`${active ? 'Thu hồi quyền' : 'Cấp quyền'} cho ${role.role_name}`}
                          className={`w-7 h-7 rounded-lg inline-flex items-center justify-center transition-colors ${
                            active
                              ? 'bg-emerald-600 text-white shadow-2xs hover:bg-emerald-700'
                              : 'bg-slate-100 text-slate-300 hover:bg-slate-200'
                          } ${isAdmin ? 'opacity-70 cursor-not-allowed' : 'cursor-pointer'}`}
                        >
                          {active ? <Check className="w-4 h-4 stroke-[3]" /> : <X className="w-4 h-4" />}
                        </button>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
