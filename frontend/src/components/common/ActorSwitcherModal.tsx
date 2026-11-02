/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Actor & Role Switcher Modal
 * Provides transparent role-based access control switching and explicit actor boundaries
 */

import React from 'react';
import { useAuth, ROLE_DEFINITIONS } from '../../context/AuthContext';
import { dbStore } from '../../services/dbStore';
import {
  Users,
  CheckCircle2,
  Shield,
  ArrowRight,
  X,
  FileEdit,
  CheckSquare,
  Truck,
  Trello,
  Settings,
  Award,
  KeyRound,
} from 'lucide-react';

interface ActorSwitcherModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const ActorSwitcherModal: React.FC<ActorSwitcherModalProps> = ({ isOpen, onClose }) => {
  const { currentUser, loginAsUser } = useAuth();
  const dbState = dbStore.getState();

  if (!isOpen) return null;

  const actorGroups = [
    {
      actor_id: 2,
      name: 'Hưng',
      role_code: 'REPORTER',
      title: 'Phóng viên Tác nghiệp',
      workflow: 'WF-01 · Đăng ký & Đề xuất Đề tài',
      desk: 'Ban Thời sự - Chính trị',
      icon: FileEdit,
      color: 'bg-blue-600',
      badgeClass: 'b-in_progress',
      features: [
        'Khởi tạo và nộp đề xuất đề tài mới (SCR-1.4)',
        'Lập bảng dự toán kinh phí và đăng ký tổ tác nghiệp',
        'Kiểm tra và ngăn chặn trùng lặp đề tài tức thời (SCR-1.3)',
        'Quản lý vòng đời bài viết cá nhân & xin gia hạn deadline (SCR-1.8)',
      ],
      restriction: 'Chỉ xem và sửa đề tài của chính mình. Chỉ mời phóng viên vào tổ tác nghiệp.',
    },
    {
      actor_id: 1,
      name: 'Trường',
      role_code: 'DESK_EDITOR',
      title: 'Biên tập viên Ban (Trưởng Ban)',
      workflow: 'WF-02 · Thẩm định Rubric & Phê duyệt',
      desk: 'Ban Thời sự - Chính trị',
      icon: CheckSquare,
      color: 'bg-indigo-600',
      badgeClass: 'b-approved',
      features: [
        'Thẩm định đề tài theo Rubric 4 tiêu chí chuẩn (SCR-2.3)',
        'Phân luồng rủi ro pháp lý (Tự động chuyển TBT nếu điểm rủi ro ≥ 8.0)',
        'Chủ động khởi tạo đề tài định hướng giao cấp dưới (SCR-2.4)',
        'Cân bằng tải phóng viên và phân công tác nghiệp (SCR-2.5)',
      ],
      restriction: 'Chỉ thẩm định đề tài và phân công phóng viên thuộc Ban Thời sự - Chính trị.',
    },
    {
      actor_id: 3,
      name: 'Lộc',
      role_code: 'LOGISTICS_COORDINATOR',
      title: 'Điều phối viên Hậu cần & Xe máy',
      workflow: 'WF-03 · Kho Tài sản & Thiết bị',
      desk: 'Phòng Hậu cần - Kỹ thuật',
      icon: Truck,
      color: 'bg-emerald-600',
      badgeClass: 'b-available',
      features: [
        'Quản lý kho tài sản 6 nhóm: Máy quay, Ống kính, Âm thanh, Ánh sáng, Flycam, Xe',
        'Duyệt yêu cầu mượn thiết bị cho đề tài đã APPROVED (SCR-3.5)',
        'Kiểm tra xung đột lịch và phân bổ phương tiện (SCR-3.6)',
        'Thực hiện thủ tục Xuất kho (Check-out) và Thu hồi (Return) (SCR-3.7)',
      ],
      restriction: 'Ràng buộc CSDL: Cấm xóa thiết bị đang ở trạng thái CHECKED_OUT.',
    },
    {
      actor_id: 4,
      name: 'Đạt',
      role_code: 'FIELDWORK_COORDINATOR',
      title: 'Điều phối viên Hiện trường & SLA',
      workflow: 'WF-04 · Hiện trường Kanban & SLA',
      desk: 'Trung tâm Điều phối Hiện trường',
      icon: Trello,
      color: 'bg-amber-600',
      badgeClass: 'b-pending',
      features: [
        'Bảng Kanban theo dõi tiến độ sản xuất hiện trường',
        'Theo dõi check-in GPS và nhận báo cáo tiến độ phóng viên',
        'Tiếp nhận và xử lý tín hiệu báo động sự cố hiện trường khẩn cấp',
        'Giám sát hạn chót và quy chuẩn SLA giao nộp sản phẩm',
      ],
      restriction: 'Chỉ điều phối tiến độ hiện trường và ghi nhận nhật ký vận hành sản xuất.',
    },
    {
      actor_id: 5,
      name: 'Cường',
      role_code: 'BUSINESS_ADMINISTRATOR',
      title: 'Quản trị viên Nghiệp vụ Tòa soạn',
      workflow: 'WF-05 · Quản trị Nghiệp vụ & Định mức',
      desk: 'Phòng Kế hoạch & Nghiệp vụ',
      icon: Settings,
      color: 'bg-purple-600',
      badgeClass: 'b-revision_requested',
      features: [
        'Giao và giám sát chỉ tiêu định mức đề tài cho từng Ban (SCR-5.2)',
        'Cấu hình trọng số Rubric thẩm định (Đảm bảo tổng trọng số = 100%) (SCR-5.3)',
        'Quản lý danh mục chuyên mục và cơ cấu phòng ban tòa soạn (SCR-5.4)',
        'Xuất báo cáo thống kê năng suất và phân tích đề tài (SCR-5.5)',
      ],
      restriction: 'Phụ trách cấu hình quy chuẩn nghiệp vụ, không trực tiếp duyệt đề tài.',
    },
    {
      actor_id: 6,
      name: 'Tổng Biên Tập',
      role_code: 'EXECUTIVE_EDITOR',
      title: 'Tổng Biên Tập (Ban Biên Tập Tối Cao)',
      workflow: 'Hội đồng Tổng Biên Tập · Thẩm định Vượt cấp',
      desk: 'Ban Lãnh đạo Tòa soạn',
      icon: Award,
      color: 'bg-rose-700',
      badgeClass: 'b-escalated',
      features: [
        'Thẩm định vượt cấp các đề tài có rủi ro pháp lý cao (Legal Risk ≥ 8.0) (SCR-2.3)',
        'Bảng chỉ huy điều hành tối cao toàn bộ tòa soạn (SCR-5.1)',
        'Phê duyệt xuất bản các phóng sự điều tra quy mô lớn',
        'Theo dõi tổng thể hạn ngạch và chất lượng nội dung toàn tòa soạn',
      ],
      restriction: 'Chỉ can thiệp thẩm định các đề tài thuộc diện vượt cấp hoặc nhạy cảm.',
    },
    {
      actor_id: 7,
      name: 'Hệ Thống Quản Trị',
      role_code: 'SYSTEM_ADMINISTRATOR',
      title: 'Quản trị viên Hệ thống & Bảo mật',
      workflow: 'Quản trị Hệ thống & Phân quyền RBAC',
      desk: 'Trung tâm Công nghệ Thông tin',
      icon: Shield,
      color: 'bg-slate-800',
      badgeClass: 'b-maintenance',
      features: [
        'Quản lý hồ sơ tài khoản nhân sự và kích hoạt/khóa tài khoản (SCR-5.6)',
        'Ma trận phân quyền chi tiết (Role-Permission Matrix) (SCR-5.7)',
        'Tra cứu toàn bộ nhật ký kiểm toán hệ thống (Audit Trail) (SCR-5.8)',
      ],
      restriction: 'Toàn quyền kỹ thuật và phân quyền; không tham gia nghiệp vụ báo chí.',
    },
  ];

  return (
    <div className="modal-backdrop">
      <div className="modal" style={{ width: '920px', maxWidth: '95vw' }}>
        <div className="modal-head">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <h2>Phân Lại Quyền & Chuyển Đổi Vai Trò Nhân Sự (RBAC)</h2>
              <p className="text-xs text-slate-500 m-0">
                Mỗi vai trò (Role) tương ứng với Actor cụ thể, sở hữu không gian làm việc và quyền truy cập dữ liệu riêng biệt.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="icon-btn"
            title="Đóng"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="modal-body space-y-4">
          <div className="p-3.5 bg-blue-50/70 border border-blue-200 rounded-xl text-xs text-blue-900 flex items-start gap-3">
            <KeyRound className="w-5 h-5 text-blue-600 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <div className="font-bold">Quy chế Phân quyền Tòa soạn (Role & Actor Boundaries):</div>
              <p className="m-0 leading-relaxed text-blue-800">
                Khi đăng nhập với Actor nào, thanh menu bên trái và chức năng sẽ tự động chuyển sang chỉ hiển thị các màn hình và tài nguyên của Actor đó. Mật khẩu đồng bộ cho tất cả tài khoản mẫu là <code className="bg-white px-1.5 py-0.5 rounded border border-blue-300 font-bold font-mono text-slate-900">123</code>.
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
            {actorGroups.map((actor) => {
              const isCurrent = currentUser.user_id === actor.actor_id;
              const IconComp = actor.icon;

              return (
                <div
                  key={actor.actor_id}
                  className={`p-4 rounded-xl border transition-all relative flex flex-col justify-between ${
                    isCurrent
                      ? 'bg-blue-50/60 border-blue-500 shadow-md ring-2 ring-blue-500/20'
                      : 'bg-white hover:bg-slate-50/80 border-slate-200 hover:border-slate-300 shadow-xs'
                  }`}
                >
                  <div>
                    {/* Header: Name, Role Pill, Badge */}
                    <div className="flex items-start justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2.5">
                        <div className={`w-9 h-9 rounded-lg ${actor.color} text-white flex items-center justify-center font-bold text-sm shadow-xs`}>
                          <IconComp className="w-4 h-4" />
                        </div>
                        <div>
                          <div className="font-bold text-sm text-slate-900 flex items-center gap-2">
                            <span>{actor.name}</span>
                            <span className="font-mono text-[10px] px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 font-semibold border border-slate-200">
                              {actor.role_code}
                            </span>
                          </div>
                          <div className="text-xs text-blue-700 font-medium">{actor.title}</div>
                        </div>
                      </div>

                      {isCurrent ? (
                        <span className="text-[11px] font-bold text-emerald-700 bg-emerald-100 border border-emerald-200 px-2 py-0.5 rounded-full flex items-center gap-1 shrink-0">
                          <CheckCircle2 className="w-3 h-3" /> Đang dùng
                        </span>
                      ) : (
                        <span className="text-[10px] text-slate-400 font-mono">
                          ID: {actor.actor_id}
                        </span>
                      )}
                    </div>

                    <div className="text-[11px] text-slate-500 mb-2 font-medium">
                      🏢 {actor.desk} · <span className="text-slate-700 font-semibold">{actor.workflow}</span>
                    </div>

                    {/* Features list */}
                    <div className="bg-slate-50 rounded-lg p-2.5 border border-slate-100 mb-2">
                      <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider mb-1">
                        Quyền hạn chính:
                      </div>
                      <ul className="text-xs text-slate-700 space-y-1 list-disc pl-4">
                        {actor.features.map((feat, idx) => (
                          <li key={idx} className="leading-tight">{feat}</li>
                        ))}
                      </ul>
                    </div>

                    {/* Boundary restriction */}
                    <div className="text-[11px] text-rose-700 bg-rose-50/60 p-2 rounded border border-rose-100 mb-3">
                      <strong>Giới hạn vai trò:</strong> {actor.restriction}
                    </div>
                  </div>

                  {/* Action button */}
                  <div>
                    {isCurrent ? (
                      <button
                        disabled
                        className="w-full py-2 px-3 rounded-lg bg-emerald-50 text-emerald-700 border border-emerald-200 font-bold text-xs flex items-center justify-center gap-1.5 cursor-default"
                      >
                        <CheckCircle2 className="w-4 h-4" /> Bạn đang làm việc với vai trò này
                      </button>
                    ) : (
                      <button
                        onClick={() => {
                          loginAsUser(actor.actor_id);
                          onClose();
                        }}
                        className="w-full py-2 px-3 rounded-lg bg-[#3458d4] hover:bg-[#2746b5] text-white font-bold text-xs flex items-center justify-center gap-1.5 shadow-sm transition-colors cursor-pointer"
                      >
                        <span>Chuyển sang {actor.name} ({actor.role_code})</span>
                        <ArrowRight className="w-3.5 h-3.5" />
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div className="modal-foot">
          <button onClick={onClose} className="btn">
            Đóng cửa sổ
          </button>
        </div>
      </div>
    </div>
  );
};
