/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Login Screen / Actor Portal
 */

import React, { useState } from 'react';
import { useAuth, ROLE_DEFINITIONS } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import { LogIn, KeyRound, ArrowRight, UserCheck, CheckCircle2, Shield, AlertCircle } from 'lucide-react';

export const LoginScreen: React.FC = () => {
  const { loginWithCredentials, loginAsUser } = useAuth();
  const [dbState, setDbState] = useState(dbStore.getState());

  React.useEffect(() => {
    return dbStore.subscribe(() => {
      setDbState({ ...dbStore.getState() });
    });
  }, []);

  const [activeTab, setActiveTab] = useState<'quick' | 'credentials'>('quick');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('123');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleCredentialsSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    const success = loginWithCredentials(email, password);
    if (!success) {
      setErrorMessage('Email hoặc mật khẩu không chính xác (hoặc tài khoản đã bị khóa). Mật khẩu mặc định là 123.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0f172a] via-[#1e293b] to-[#0f172a] flex flex-col justify-center items-center p-4 sm:p-6 text-slate-100">
      <div className="w-full max-w-4xl space-y-6">
        {/* Brand Banner */}
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-500 shadow-xl shadow-blue-500/25 mb-1">
            <span className="text-white font-black text-2xl tracking-tight">NT</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
            NTMCS <span className="text-blue-400 font-normal">Newsroom System</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-400 max-w-lg mx-auto">
            Hệ thống Quản trị & Điều phối Đề tài Báo chí — Phân quyền độc lập theo từng Actor & Vai trò chuyên trách.
          </p>
        </div>

        {/* Tab Toggle */}
        <div className="flex justify-center">
          <div className="bg-slate-800/80 p-1 rounded-xl border border-slate-700/60 inline-flex shadow-inner">
            <button
              onClick={() => setActiveTab('quick')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                activeTab === 'quick'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              ⚡ Đăng nhập nhanh theo Actor / Vai trò (Khuyên dùng)
            </button>
            <button
              onClick={() => setActiveTab('credentials')}
              className={`px-4 py-2 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                activeTab === 'credentials'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white'
              }`}
            >
              🔑 Đăng nhập bằng Email & Mật khẩu
            </button>
          </div>
        </div>

        {/* TAB 1: QUICK ACTOR SELECT CARDS */}
        {activeTab === 'quick' && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3.5">
            {dbState.users.map(u => {
              const role = dbState.roles.find(r => r.role_id === u.role_id);
              const roleMeta = ROLE_DEFINITIONS[role?.role_name || 'REPORTER'] || ROLE_DEFINITIONS.REPORTER;
              const desk = dbState.desks.find(d => d.desk_id === u.desk_id);

              return (
                <div
                  key={u.user_id}
                  onClick={() => loginAsUser(u.user_id)}
                  className="bg-slate-800/60 hover:bg-slate-800/90 border border-slate-700/60 hover:border-blue-500/50 rounded-xl p-4 transition-all duration-200 cursor-pointer group flex flex-col justify-between space-y-3 shadow-lg hover:shadow-blue-500/10"
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-blue-600/20 text-blue-400 border border-blue-500/30 flex items-center justify-center font-bold text-sm group-hover:bg-blue-600 group-hover:text-white transition-colors">
                        {u.full_name.charAt(0)}
                      </div>
                      <div>
                        <div className="font-bold text-white text-sm group-hover:text-blue-400 transition-colors">
                          {u.full_name}
                        </div>
                        <div className="text-[11px] font-mono text-slate-400">
                          {u.email}
                        </div>
                      </div>
                    </div>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-blue-500/10 border border-blue-500/30 text-blue-300 font-semibold uppercase">
                      {role?.role_name?.replace('_', ' ')}
                    </span>
                  </div>

                  <div className="text-xs text-slate-300 space-y-1">
                    <div className="font-medium text-slate-200 flex items-center gap-1.5">
                      <span>{roleMeta.display_title}</span>
                    </div>
                    <p className="text-[11px] text-slate-400 line-clamp-2 leading-relaxed">
                      {roleMeta.description}
                    </p>
                    {desk && (
                      <div className="text-[10px] text-blue-400/90 pt-1">
                        🏢 {desk.desk_name}
                      </div>
                    )}
                  </div>

                  <div className="pt-2 border-t border-slate-700/40 flex items-center justify-between text-xs text-blue-400 group-hover:text-blue-300 font-semibold">
                    <span>Đăng nhập vai trò này</span>
                    <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {/* TAB 2: CREDENTIALS LOGIN FORM */}
        {activeTab === 'credentials' && (
          <div className="max-w-md mx-auto w-full bg-slate-800/80 border border-slate-700/80 rounded-2xl p-6 shadow-2xl backdrop-blur-sm">
            <form onSubmit={handleCredentialsSubmit} className="space-y-4">
              <div className="text-center pb-2">
                <h3 className="text-base font-bold text-white">Xác thực tài khoản tòa soạn</h3>
                <p className="text-xs text-slate-400 mt-1">
                  Nhập địa chỉ email nội bộ và mật khẩu bảo mật được cấp.
                </p>
              </div>

              {errorMessage && (
                <div className="p-3 rounded-lg bg-rose-500/10 border border-rose-500/30 text-rose-300 text-xs flex items-start gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-400 mt-0.5" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Email đăng nhập
                </label>
                <input
                  type="email"
                  required
                  placeholder="VD: hung.reporter@newsroom.vn"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-white text-xs placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">
                  Mật khẩu
                </label>
                <input
                  type="password"
                  required
                  placeholder="Mật khẩu (mặc định 123)"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-white text-xs placeholder:text-slate-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs rounded-lg transition-colors flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-blue-600/30"
              >
                <LogIn className="w-4 h-4" />
                <span>Đăng nhập hệ thống NTMCS</span>
              </button>

              <div className="p-3 bg-slate-900/60 rounded-lg border border-slate-700/50 text-[11px] text-slate-400 space-y-1.5">
                <div className="font-semibold text-slate-300">Nhấp chọn nhanh tài khoản mẫu:</div>
                <div className="space-y-1">
                  {[
                    { email: 'hung.reporter@newsroom.vn', label: 'hung.reporter@newsroom.vn (Phóng viên Hưng)' },
                    { email: 'truong.editor@newsroom.vn', label: 'truong.editor@newsroom.vn (BTV Trưởng ban Trường)' },
                    { email: 'admin.system@newsroom.vn', label: 'admin.system@newsroom.vn (Quản trị hệ thống)' },
                  ].map(demo => (
                    <button
                      key={demo.email}
                      type="button"
                      onClick={() => {
                        setEmail(demo.email);
                        setPassword('123');
                        loginWithCredentials(demo.email, '123');
                      }}
                      className="block w-full text-left px-2 py-1 rounded bg-slate-800/80 hover:bg-slate-700/80 text-blue-300 hover:text-white transition-colors cursor-pointer"
                    >
                      • {demo.label}
                    </button>
                  ))}
                </div>
                <div className="text-slate-500 text-[10px] italic pt-1">Mật khẩu mặc định: 123 (nhấp vào tài khoản để đăng nhập ngay)</div>
              </div>
            </form>
          </div>
        )}

        <div className="text-center text-[11px] text-slate-500 pt-2">
          NTMCS v2.4 · Newsroom Topic Management & Coordination System · Đảm bảo an toàn thông tin & Phân quyền RBAC
        </div>
      </div>
    </div>
  );
};
