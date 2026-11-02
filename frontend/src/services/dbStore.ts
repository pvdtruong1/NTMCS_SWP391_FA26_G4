/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 * NTMCS - Database Simulation & Persistence Layer mirroring SQL Server 2022
 */

import {
  Role,
  Permission,
  RolePermission,
  Desk,
  Category,
  User,
  Topic,
  TopicStatus,
  TopicMember,
  TopicEstimate,
  CriteriaConfig,
  TopicEvaluation,
  EvaluationDetail,
  Asset,
  AssetRequest,
  FieldworkTask,
  Notification,
  DeskQuota,
  RequestDeadlineExtension,
  AuditLog,
  SLAConfig,
  BroadcastNotification,
  BroadcastRecipient,
  BroadcastUrgency,
} from '../types';

const STORAGE_KEY = 'NTMCS_DATABASE_STATE_V1';

// Seed data from the verified T-SQL script
export const SEED_ROLES: Role[] = [
  { role_id: 1, role_name: 'REPORTER' },
  { role_id: 2, role_name: 'DESK_EDITOR' },
  { role_id: 3, role_name: 'EXECUTIVE_EDITOR' },
  { role_id: 4, role_name: 'LOGISTICS_COORDINATOR' },
  { role_id: 5, role_name: 'FIELDWORK_COORDINATOR' },
  { role_id: 6, role_name: 'BUSINESS_ADMINISTRATOR' },
  { role_id: 7, role_name: 'SYSTEM_ADMINISTRATOR' },
];

export const SEED_PERMISSIONS: Permission[] = [
  { permission_id: 1, permission_code: 'USER_MANAGE', description: 'SCR-5.6 Quản lý hồ sơ người dùng & tài khoản nội bộ' },
  { permission_id: 2, permission_code: 'RBAC_MANAGE', description: 'SCR-5.7 Cấu hình ma trận phân quyền vai trò (RBAC)' },
  { permission_id: 3, permission_code: 'AUDIT_VIEW', description: 'SCR-5.8 Xem và tra cứu nhật ký kiểm toán hệ thống' },
  { permission_id: 4, permission_code: 'TOPIC_PITCH', description: 'SCR-1.4 Soạn thảo, lập dự toán và nộp đề xuất đề tài mới' },
  { permission_id: 5, permission_code: 'TOPIC_EVALUATE', description: 'SCR-2.3 Thẩm định rubric tiêu chí và phê duyệt đề tài' },
  { permission_id: 6, permission_code: 'LOGISTICS_MANAGE', description: 'SCR-3.2 & SCR-3.6 Quản lý kho xe, thiết bị và giải quyết trùng lịch' },
  { permission_id: 7, permission_code: 'FIELDWORK_TRACK', description: 'Quản lý và điều phối tác nghiệp hiện trường Kanban' },
  { permission_id: 8, permission_code: 'QUOTA_MANAGE', description: 'SCR-5.2 Thiết lập và phê duyệt định mức sản xuất đề tài các ban' },
];

export const SEED_ROLE_PERMISSIONS: RolePermission[] = [
  { role_id: 7, permission_id: 1 }, { role_id: 7, permission_id: 2 }, { role_id: 7, permission_id: 3 },
  { role_id: 7, permission_id: 4 }, { role_id: 7, permission_id: 5 }, { role_id: 7, permission_id: 6 },
  { role_id: 7, permission_id: 7 }, { role_id: 7, permission_id: 8 },
  { role_id: 6, permission_id: 1 }, { role_id: 6, permission_id: 2 }, { role_id: 6, permission_id: 3 }, { role_id: 6, permission_id: 8 },
  { role_id: 3, permission_id: 3 }, { role_id: 3, permission_id: 5 }, { role_id: 3, permission_id: 8 },
  { role_id: 2, permission_id: 3 }, { role_id: 2, permission_id: 5 },
  { role_id: 1, permission_id: 4 },
  { role_id: 4, permission_id: 6 },
  { role_id: 5, permission_id: 7 },
];

export const SEED_DESKS: Desk[] = [
  { desk_id: 1, desk_name: 'Ban Thời sự - Chính trị' },
  { desk_id: 2, desk_name: 'Ban Kinh tế - Xã hội' },
  { desk_id: 3, desk_name: 'Ban Pháp luật - Điều tra' },
];

export const SEED_CATEGORIES: Category[] = [
  { category_id: 1, category_name: 'Chính trị - Ngoại giao', parent_category_id: null, desk_id: 1 },
  { category_id: 2, category_name: 'Đô thị & Dân sinh', parent_category_id: null, desk_id: 1 },
  { category_id: 3, category_name: 'Tài chính - Ngân hàng', parent_category_id: null, desk_id: 2 },
  { category_id: 4, category_name: 'Doanh nghiệp & Thị trường', parent_category_id: 3, desk_id: 2 },
  { category_id: 5, category_name: 'Điều tra tiêu cực', parent_category_id: null, desk_id: 3 },
  { category_id: 6, category_name: 'Pháp đình & Trật tự an ninh', parent_category_id: 5, desk_id: 3 },
];

export const SEED_USERS: User[] = [
  { user_id: 1, full_name: 'Trường', email: 'truong.editor@newsroom.vn', password_hash: '123', role_id: 2, active_status: true, desk_id: 1 },
  { user_id: 2, full_name: 'Hưng', email: 'hung.reporter@newsroom.vn', password_hash: '123', role_id: 1, active_status: true, desk_id: 1 },
  { user_id: 3, full_name: 'Lộc', email: 'loc.logistics@newsroom.vn', password_hash: '123', role_id: 4, active_status: true, desk_id: null },
  { user_id: 4, full_name: 'Đạt', email: 'dat.fieldwork@newsroom.vn', password_hash: '123', role_id: 5, active_status: true, desk_id: null },
  { user_id: 5, full_name: 'Cường', email: 'cuong.admin@newsroom.vn', password_hash: '123', role_id: 6, active_status: true, desk_id: null },
  { user_id: 6, full_name: 'Tổng Biên Tập', email: 'tbt.exec@newsroom.vn', password_hash: '123', role_id: 3, active_status: true, desk_id: null },
  { user_id: 7, full_name: 'Hệ Thống Quản Trị', email: 'admin.system@newsroom.vn', password_hash: '123', role_id: 7, active_status: true, desk_id: null },
];

export const SEED_CRITERIA_CONFIGS: CriteriaConfig[] = [
  { criteria_id: 1, criteria_name: 'Tính thời sự & Tính độc quyền (Exclusivity & Timeliness)', weight_percent: 30.00 },
  { criteria_id: 2, criteria_name: 'Tác động xã hội & Giá trị thông tin (Impact & Informational Value)', weight_percent: 30.00 },
  { criteria_id: 3, criteria_name: 'Tính khả thi & Kế hoạch hậu cần (Feasibility & Logistics Plan)', weight_percent: 20.00 },
  { criteria_id: 4, criteria_name: 'Đạo đức báo chí & Kiểm chứng nguồn tin (Journalistic Ethics & Verification)', weight_percent: 20.00 },
];

export const SEED_TOPICS: Topic[] = [
  {
    topic_id: 1,
    topic_code: null,
    title: 'Dự án cải tạo nút giao thông An Phú: Tiến độ thi công và những bất cập hạ tầng',
    angle: 'Phân tích nguyên nhân ùn tắc kéo dài, đối thoại cùng cư dân và đơn vị thi công',
    status: 'SUBMITTED',
    deadline: '2026-10-25',
    category_id: 2,
    author_user_id: 2,
  },
  {
    topic_id: 2,
    topic_code: null,
    title: 'Chuyển đổi số trong dịch vụ hành chính công: Đột phá hồ sơ trực tuyến cấp phường',
    angle: 'Khảo sát thực tế thời gian chờ đợi của người dân tại 5 quận trọng điểm',
    status: 'SUBMITTED',
    deadline: '2026-10-30',
    category_id: 1,
    author_user_id: 2,
  },
  {
    topic_id: 3,
    topic_code: null,
    title: 'Điều tra sai phạm quản lý đất công quy mô lớn: Rủi ro tranh chấp và hồ sơ pháp lý phức tạp',
    angle: 'Hồ sơ nhạy cảm đụng chạm nhiều pháp nhân lớn, tiềm ẩn rủi ro khiếu kiện pháp lý cao (Legal Risk >= 8)',
    status: 'PENDING_EXECUTIVE_APPROVAL',
    deadline: '2026-11-05',
    category_id: 1,
    author_user_id: 2,
  },
  {
    topic_id: 4,
    topic_code: 'TOPIC-2026-001',
    title: 'Phóng sự truyền hình: Đêm trắng cùng lực lượng tuần tra bảo vệ môi trường đô thị',
    angle: 'Tác nghiệp báo chí đa phương tiện, trải nghiệm thực tế cùng tổ kiểm tra liên ngành',
    status: 'APPROVED',
    deadline: '2026-10-20',
    category_id: 2,
    author_user_id: 2,
  },
  {
    topic_id: 5,
    topic_code: 'TOPIC-2026-002',
    title: 'Tác động của thuế tối thiểu toàn cầu đến làn sóng đầu tư FDI công nghệ cao',
    angle: 'Phỏng vấn các chuyên gia kinh tế đầu ngành và đại diện hiệp hội doanh nghiệp quốc tế',
    status: 'IN_PROGRESS',
    deadline: '2026-10-28',
    category_id: 1,
    author_user_id: 2,
  },
  {
    topic_id: 6,
    topic_code: null,
    title: 'Ý tưởng thô: Xu hướng sống xanh và giảm phát thải rác nhựa trong giới trẻ',
    angle: 'Góc nhìn phong cách sống hiện đại (Bản thảo đang viết dở)',
    status: 'DRAFT',
    deadline: '2026-11-15',
    category_id: 2,
    author_user_id: 2,
  },
  {
    topic_id: 7,
    topic_code: null,
    title: 'Thực trạng giải ngân vốn đầu tư công các dự án metro: Cần làm rõ nguồn số liệu tài chính',
    angle: 'BTV yêu cầu bổ sung bảng đối chiếu dự toán và ý kiến của Sở Giao thông Vận tải',
    status: 'REVISION_REQUESTED',
    deadline: '2026-10-27',
    category_id: 1,
    author_user_id: 2,
  },
  {
    topic_id: 8,
    topic_code: null,
    title: 'Đề xuất đề tài sự kiện lễ hội ẩm thực quốc tế cuối tuần',
    angle: 'Ý tưởng thiếu chiều sâu thời sự và trùng lặp với ấn phẩm số trước',
    status: 'REJECTED',
    deadline: '2026-10-15',
    category_id: 2,
    author_user_id: 2,
  },
  {
    topic_id: 9,
    topic_code: null,
    title: 'Biến động lãi suất huy động cuối năm và phản ứng của thị trường bất động sản',
    angle: 'Phân tích thanh khoản ngân hàng thương mại (Thuộc Ban Kinh tế - Desk 2)',
    status: 'SUBMITTED',
    deadline: '2026-10-29',
    category_id: 3,
    author_user_id: 2,
  },
  {
    topic_id: 10,
    topic_code: 'TOPIC-2026-003',
    title: 'Loạt bài chuyên đề: Hoàn tất bàn giao mặt bằng Vành đai 3 - Ký ức ngày di dời',
    angle: 'Đã hoàn thành toàn bộ tác nghiệp, thẩm định nghiệm thu đạt tiêu chuẩn bàn giao CMS',
    status: 'ACCEPTED_READY_FOR_CMS',
    deadline: '2026-10-10',
    category_id: 2,
    author_user_id: 2,
  },
];

export const SEED_TOPIC_MEMBERS: TopicMember[] = [
  { member_id: 1, topic_id: 4, user_id: 2, role_in_team: 'LEAD_REPORTER' },
  { member_id: 2, topic_id: 4, user_id: 4, role_in_team: 'PHOTO_REPORTER' },
  { member_id: 3, topic_id: 5, user_id: 2, role_in_team: 'LEAD_REPORTER' },
];

export const SEED_TOPIC_ESTIMATES: TopicEstimate[] = [
  { estimate_id: 1, topic_id: 4, cost_item: 'Chi phí di chuyển & nhiên liệu xe chuyên dụng', amount: 1500000.00 },
  { estimate_id: 2, topic_id: 4, cost_item: 'Bồi dưỡng cộng tác viên & trinh sát hiện trường ban đêm', amount: 2000000.00 },
  { estimate_id: 3, topic_id: 4, cost_item: 'Thuê trang thiết bị quay chụp ánh sáng yếu', amount: 1200000.00 },
  { estimate_id: 4, topic_id: 5, cost_item: 'Tài liệu báo cáo tài chính chuyên sâu từ tổ chức quốc tế', amount: 3500000.00 },
];

export const SEED_TOPIC_EVALUATIONS: TopicEvaluation[] = [
  {
    evaluation_id: 1,
    topic_id: 4,
    evaluator_user_id: 1,
    evaluation_stage: 'DESK_REVIEW',
    decision: 'APPROVED',
    comments: 'Đề tài giàu tính thời sự, kế hoạch tác nghiệp hiện trường chi tiết và khả thi cao. Đã duyệt cấp mã TOPIC-2026-001.',
  },
  {
    evaluation_id: 2,
    topic_id: 3,
    evaluator_user_id: 1,
    evaluation_stage: 'DESK_REVIEW',
    decision: 'ESCALATED',
    comments: 'Đề tài liên quan đến hồ sơ pháp lý đất công lớn, có khả năng khiếu nại kiện tụng cao (Legal Risk >= 8). Chuyển TBT xem xét phê duyệt thẩm quyền tối cao.',
  },
  {
    evaluation_id: 3,
    topic_id: 8,
    evaluator_user_id: 1,
    evaluation_stage: 'DESK_REVIEW',
    decision: 'REJECTED',
    comments: 'Nội dung quá chung chung, không có phát hiện mới, chuyển vào Kho lưu trữ ý tưởng để phóng viên hoàn thiện sau.',
  },
];

export const SEED_EVALUATION_DETAILS: EvaluationDetail[] = [
  { detail_id: 1, evaluation_id: 1, criteria_id: 1, score: 9.00, item_comment: 'Góc tiếp cận đêm trắng cùng cảnh sát môi trường rất độc đáo và kịp thời' },
  { detail_id: 2, evaluation_id: 1, criteria_id: 2, score: 8.50, item_comment: 'Tác động xã hội tích cực, nâng cao ý thức chấp hành luật môi trường' },
  { detail_id: 3, evaluation_id: 1, criteria_id: 3, score: 9.00, item_comment: 'Đã có sẵn danh sách liên hệ tổ công tác, kế hoạch an toàn tác nghiệp rõ ràng' },
  { detail_id: 4, evaluation_id: 1, criteria_id: 4, score: 8.50, item_comment: 'Nguồn tin chính thống, tuân thủ đúng quy chế bảo vệ bí mật nghiệp vụ' },
];

export const SEED_ASSETS: Asset[] = [
  {
    asset_id: 1,
    asset_name: 'Xe bán tải chuyên dụng 2 cầu Ford Ranger (VEH-4WD-01)',
    asset_status: 'AVAILABLE',
    asset_code: 'VEH-4WD-01',
    asset_category: 'VEHICLE',
    serial_number: '51D-882.91',
    description: 'Xe bán tải địa hình 2 cầu gầm cao, trang bị tời cứu hộ và thùng chống nước tác nghiệp mưa bão hiện trường',
  },
  {
    asset_id: 2,
    asset_name: 'Xe chở đoàn tác nghiệp 7 chỗ Toyota Fortuner (VEH-7S-02)',
    asset_status: 'CHECKED_OUT',
    asset_code: 'VEH-7S-02',
    asset_category: 'VEHICLE',
    serial_number: '51F-639.14',
    description: 'Xe SUV 7 chỗ chở tổ phóng viên tác nghiệp liên tỉnh, khoang hành lý rộng chứa trang thiết bị chuyên dụng',
  },
  {
    asset_id: 3,
    asset_name: 'Flycam chuyên dụng DJI Mavic 3 Cine 4K (DRONE-DJI-01)',
    asset_status: 'AVAILABLE',
    asset_code: 'DRONE-DJI-01',
    asset_category: 'PHOTO_EQUIPMENT',
    serial_number: 'DJI-M3C-2024-883',
    description: 'Flycam quay phim 4K Apple ProRes, trang bị cảm biến chướng ngại vật đa hướng và 3 pin bay dự phòng',
  },
  {
    asset_id: 4,
    asset_name: 'Máy quay phim chuyên dụng Sony FX6 Cinema (CAM-SONY-01)',
    asset_status: 'AVAILABLE',
    asset_code: 'CAM-SONY-01',
    asset_category: 'CAMERA_EQUIPMENT',
    serial_number: 'SONY-FX6-990142',
    description: 'Máy quay full-frame 4K 120fps chuyên dụng thời sự, kèm ống kính GM 24-70mm f/2.8 và bộ micro phỏng vấn không dây',
  },
  {
    asset_id: 5,
    asset_name: 'Bộ áo giáp phản quang & nón bảo hộ tác nghiệp (GEAR-PROT-01)',
    asset_status: 'AVAILABLE',
    asset_code: 'GEAR-PROT-01',
    asset_category: 'ACCESSORIES',
    serial_number: 'PROT-SET-2025-05',
    description: 'Set trang bị bảo hộ hiện trường: áo giáp cấp độ IIIA chống đâm, nón bảo hiểm Kevlar tác nghiệp điểm nóng',
  },
  {
    asset_id: 6,
    asset_name: 'Máy ghi âm đa kênh Zoom H6 Handy Recorder (AUDIO-ZOOM-01)',
    asset_status: 'MAINTENANCE',
    asset_code: 'AUDIO-ZOOM-01',
    asset_category: 'AUDIO_EQUIPMENT',
    serial_number: 'ZH6-2023-4410',
    description: 'Máy ghi âm 6 kênh hiện trường, đang bảo dưỡng thay thế capsule micro định hướng X/Y',
  },
];

export const SEED_ASSET_REQUESTS: AssetRequest[] = [
  { request_id: 1, topic_id: 4, asset_id: 1, requester_user_id: 2, start_time: '2026-10-20 18:00:00', end_time: '2026-10-21 06:00:00', status: 'APPROVED' },
  { request_id: 2, topic_id: 5, asset_id: 2, requester_user_id: 2, start_time: '2026-10-18 07:00:00', end_time: '2026-10-22 17:00:00', status: 'CHECKED_OUT' },
  { request_id: 3, topic_id: 4, asset_id: 3, requester_user_id: 2, start_time: '2026-10-20 20:00:00', end_time: '2026-10-20 23:00:00', status: 'PENDING' },
  { request_id: 4, topic_id: 10, asset_id: 4, requester_user_id: 2, start_time: '2026-10-08 08:00:00', end_time: '2026-10-09 18:00:00', status: 'RETURNED' },
];

export const SEED_FIELDWORK_TASKS: FieldworkTask[] = [
  { task_id: 1, topic_id: 4, assignee_user_id: 2, task_status: 'DONE', description: 'Liên hệ và xin giấy giới thiệu công tác với Đội Cảnh sát Môi trường' },
  { task_id: 2, topic_id: 4, assignee_user_id: 2, task_status: 'IN_PROGRESS', description: 'Theo chân tổ công tác ghi nhận trực tiếp tại 3 điểm nóng xả thải' },
  { task_id: 3, topic_id: 4, assignee_user_id: 4, task_status: 'TODO', description: 'Chụp bộ ảnh phóng sự cận cảnh công tác kiểm tra mẫu nước hiện trường' },
  { task_id: 4, topic_id: 5, assignee_user_id: 2, task_status: 'IN_PROGRESS', description: 'Phỏng vấn lãnh đạo Cục Đầu tư Nước ngoài về chính sách thuế tối thiểu' },
];

export const SEED_NOTIFICATIONS: Notification[] = [
  { notification_id: 1, user_id: 2, topic_id: 4, message: 'Đề tài "Đêm trắng cùng lực lượng tuần tra..." đã được BTV Trường phê duyệt cấp 1 thành công (Mã: TOPIC-2026-001).', is_read: true, created_at: '2026-10-10 10:30:00' },
  { notification_id: 2, user_id: 6, topic_id: 3, message: 'Đề tài "Điều tra sai phạm quản lý đất công..." đã được chuyển lên Tổng Biên Tập thẩm định vượt cấp (Rủi ro pháp lý cao).', is_read: false, created_at: '2026-10-11 16:45:00' },
  { notification_id: 3, user_id: 3, topic_id: 4, message: 'Phóng viên Hưng vừa tạo yêu cầu mượn xe bán tải chuyên dụng VEH-4WD-01 cho đề tài TOPIC-2026-001.', is_read: false, created_at: '2026-10-12 09:00:00' },
  { notification_id: 4, user_id: 4, topic_id: 4, message: 'Bạn được phân công làm phóng viên ảnh cho đề tài TOPIC-2026-001.', is_read: true, created_at: '2026-10-12 11:15:00' },
  { notification_id: 5, user_id: 2, topic_id: 5, message: '[Cảnh báo SLA] Đề tài TOPIC-2026-002 còn dưới 48 giờ nữa đến hạn chót nộp bản thảo.', is_read: false, created_at: '2026-10-18 08:00:00' },
];

export const SEED_DESK_QUOTAS: DeskQuota[] = [
  { quota_id: 1, desk_id: 1, period: '2026-10', quota_count: 15 },
  { quota_id: 2, desk_id: 2, period: '2026-10', quota_count: 12 },
  { quota_id: 3, desk_id: 3, period: '2026-10', quota_count: 10 },
  { quota_id: 4, desk_id: 1, period: '2026-Q4', quota_count: 45 },
  { quota_id: 5, desk_id: 2, period: '2026-Q4', quota_count: 36 },
  { quota_id: 6, desk_id: 3, period: '2026-Q4', quota_count: 30 },
];

export const SEED_EXTENSIONS: RequestDeadlineExtension[] = [
  {
    request_id: 1,
    topic_id: 5,
    requested_by_user_id: 2,
    current_deadline: '2026-10-28',
    requested_deadline: '2026-11-02',
    reason: 'Do lịch họp của chuyên gia kinh tế quốc tế bị lùi sang tuần sau nên phóng viên xin lùi hạn nộp thêm 5 ngày để kịp phỏng vấn độc quyền.',
    status: 'PENDING',
  },
];

export const SEED_AUDIT_LOGS: AuditLog[] = [
  { log_id: 1, actor_user_id: 7, action: 'SYSTEM_INIT', entity_type: 'SYSTEM', entity_id: null, details: 'Hệ thống NTMCS khởi tạo phân quyền RBAC và cấu hình bảo mật ban đầu.', created_at: '2026-10-01 08:00:00' },
  { log_id: 2, actor_user_id: 5, action: 'QUOTA_SET', entity_type: 'DESK_QUOTA', entity_id: 1, details: 'Quản trị viên Cường thiết lập định mức sản xuất tháng 2026-10 cho Ban Thời sự (15 đề tài).', created_at: '2026-10-02 09:15:00' },
  { log_id: 3, actor_user_id: 2, action: 'TOPIC_CREATE', entity_type: 'TOPIC', entity_id: 1, details: 'Phóng viên Hưng nộp đề xuất đề tài "Dự án cải tạo nút giao thông An Phú".', created_at: '2026-10-05 14:20:00' },
  { log_id: 4, actor_user_id: 1, action: 'TOPIC_APPROVE', entity_type: 'TOPIC', entity_id: 4, details: 'BTV Trường phê duyệt đề tài TOPIC-2026-001 (Đêm trắng cùng lực lượng tuần tra...) với điểm số 8.75/10.', created_at: '2026-10-10 10:30:00' },
  { log_id: 5, actor_user_id: 1, action: 'TOPIC_ESCALATE', entity_type: 'TOPIC', entity_id: 3, details: 'BTV Trường chuyển đề tài rủi ro pháp lý cao (Legal Risk >= 8) lên Tổng Biên Tập thẩm định vượt cấp.', created_at: '2026-10-11 16:45:00' },
];

export const SEED_SLA_CONFIG: SLAConfig = {
  threshold_hours_1: 24,
  threshold_hours_2: 12,
  threshold_hours_3: 2,
  enable_email: true,
  enable_in_app: true,
};

export const SEED_BROADCAST_NOTIFICATIONS: BroadcastNotification[] = [
  {
    broadcast_id: 1,
    sender_user_id: 4,
    sender_name: 'Hoàng Tiến Đạt (Điều phối viên Hiện trường)',
    title: '[CẢNH BÁO KHẨN] Áp thấp nhiệt đới và ngập lụt diện rộng khu vực miền Trung',
    content: 'Yêu cầu toàn bộ các tổ phóng viên đang tác nghiệp tại Đà Nẵng, Quảng Trị, Thừa Thiên Huế kiểm tra thiết bị an toàn, áo phao và duy trì liên lạc vệ tinh 24/7 với trung tâm chỉ huy.',
    urgency: 'EMERGENCY',
    scope: 'ALL',
    target_name: 'Toàn thể Tòa soạn',
    created_at: '2026-10-24 07:30:00',
    recipients: [
      { user_id: 1, user_name: 'Trần Văn Trường', email: 'truong.editor@newsroom.vn', desk_name: 'Ban Thời sự', is_read: true, read_at: '2026-10-24 07:35:12' },
      { user_id: 2, user_name: 'Nguyễn Văn Hưng', email: 'hung.reporter@newsroom.vn', desk_name: 'Ban Thời sự', is_read: true, read_at: '2026-10-24 07:42:05' },
      { user_id: 3, user_name: 'Phạm Tấn Lộc', email: 'loc.logistics@newsroom.vn', desk_name: 'Hậu cần', is_read: true, read_at: '2026-10-24 08:05:40' },
      { user_id: 5, user_name: 'Lê Văn Cường', email: 'cuong.admin@newsroom.vn', desk_name: 'Quản trị', is_read: true, read_at: '2026-10-24 08:10:19' },
      { user_id: 6, user_name: 'Tổng Biên Tập', email: 'tbt.exec@newsroom.vn', desk_name: 'Ban Biên Tập', is_read: false, read_at: null },
      { user_id: 7, user_name: 'Hệ Thống Quản Trị', email: 'admin.system@newsroom.vn', desk_name: 'Hệ thống', is_read: true, read_at: '2026-10-24 07:31:00' },
    ],
  },
  {
    broadcast_id: 2,
    sender_user_id: 1,
    sender_name: 'Trần Văn Trường (BTV Trưởng Ban Thời sự)',
    title: '[ĐIỀU PHỐI TIN NÓNG] Họp báo đột xuất của UBND TP về quy hoạch đất đai',
    content: 'Ban Thời sự cử gấp phóng viên và quay phim có mặt tại Trung tâm báo chí lúc 14h00 chiều nay. Hậu cần ưu tiên cấp máy bay quay Flycam và xe chuyên dụng.',
    urgency: 'HIGH',
    scope: 'DESK',
    target_name: 'Ban Thời sự - Chính trị & Hậu cần',
    created_at: '2026-10-25 10:15:00',
    recipients: [
      { user_id: 2, user_name: 'Nguyễn Văn Hưng', email: 'hung.reporter@newsroom.vn', desk_name: 'Ban Thời sự', is_read: true, read_at: '2026-10-25 10:22:45' },
      { user_id: 3, user_name: 'Phạm Tấn Lộc', email: 'loc.logistics@newsroom.vn', desk_name: 'Hậu cần', is_read: true, read_at: '2026-10-25 10:30:11' },
      { user_id: 4, user_name: 'Hoàng Tiến Đạt', email: 'dat.fieldwork@newsroom.vn', desk_name: 'Hiện trường', is_read: true, read_at: '2026-10-25 10:18:22' },
    ],
  },
  {
    broadcast_id: 3,
    sender_user_id: 4,
    sender_name: 'Hoàng Tiến Đạt (Điều phối viên Hiện trường)',
    title: '[NHẮC NHỞ HẠN CHÓT] Kiểm tra SLA nộp file raw đề tài TOPIC-2026-001',
    content: 'Nhắc nhở ekip tác nghiệp đề tài TOPIC-2026-001: Còn 2 giờ trước ngưỡng SLA kết thúc. Đề nghị upload video và ảnh gốc về máy chủ biên tập.',
    urgency: 'NORMAL',
    scope: 'CREW',
    target_name: 'Ekip tác nghiệp TOPIC-2026-001',
    created_at: '2026-10-25 15:00:00',
    recipients: [
      { user_id: 2, user_name: 'Nguyễn Văn Hưng', email: 'hung.reporter@newsroom.vn', desk_name: 'Ban Thời sự', is_read: false, read_at: null },
      { user_id: 1, user_name: 'Trần Văn Trường', email: 'truong.editor@newsroom.vn', desk_name: 'Ban Thời sự', is_read: true, read_at: '2026-10-25 15:12:00' },
    ],
  },
];

export interface DatabaseState {
  roles: Role[];
  permissions: Permission[];
  rolePermissions: RolePermission[];
  desks: Desk[];
  categories: Category[];
  users: User[];
  criteriaConfigs: CriteriaConfig[];
  topics: Topic[];
  topicMembers: TopicMember[];
  topicEstimates: TopicEstimate[];
  topicEvaluations: TopicEvaluation[];
  evaluationDetails: EvaluationDetail[];
  assets: Asset[];
  assetRequests: AssetRequest[];
  fieldworkTasks: FieldworkTask[];
  notifications: Notification[];
  deskQuotas: DeskQuota[];
  extensions: RequestDeadlineExtension[];
  auditLogs: AuditLog[];
  slaConfig: SLAConfig;
  broadcastNotifications: BroadcastNotification[];
}

class DatabaseStore {
  private state: DatabaseState;
  private listeners: Set<() => void> = new Set();

  constructor() {
    this.state = this.loadState();
  }

  private loadState(): DatabaseState {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (!parsed.broadcastNotifications) {
          parsed.broadcastNotifications = [...SEED_BROADCAST_NOTIFICATIONS];
        }
        return parsed;
      }
    } catch (e) {
      console.warn('Error reading stored state, using seed:', e);
    }
    return this.getDefaultSeed();
  }

  public getDefaultSeed(): DatabaseState {
    return {
      roles: [...SEED_ROLES],
      permissions: [...SEED_PERMISSIONS],
      rolePermissions: [...SEED_ROLE_PERMISSIONS],
      desks: [...SEED_DESKS],
      categories: [...SEED_CATEGORIES],
      users: [...SEED_USERS],
      criteriaConfigs: [...SEED_CRITERIA_CONFIGS],
      topics: [...SEED_TOPICS],
      topicMembers: [...SEED_TOPIC_MEMBERS],
      topicEstimates: [...SEED_TOPIC_ESTIMATES],
      topicEvaluations: [...SEED_TOPIC_EVALUATIONS],
      evaluationDetails: [...SEED_EVALUATION_DETAILS],
      assets: [...SEED_ASSETS],
      assetRequests: [...SEED_ASSET_REQUESTS],
      fieldworkTasks: [...SEED_FIELDWORK_TASKS],
      notifications: [...SEED_NOTIFICATIONS],
      deskQuotas: [...SEED_DESK_QUOTAS],
      extensions: [...SEED_EXTENSIONS],
      auditLogs: [...SEED_AUDIT_LOGS],
      slaConfig: { ...SEED_SLA_CONFIG },
      broadcastNotifications: [...SEED_BROADCAST_NOTIFICATIONS],
    };
  }

  private saveState(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.state));
    } catch (e) {
      console.error('Failed to save to localStorage:', e);
    }
    this.notify();
  }

  public subscribe(fn: () => void): () => void {
    this.listeners.add(fn);
    return () => this.listeners.delete(fn);
  }

  private notify(): void {
    this.listeners.forEach((fn) => fn());
  }

  public getState(): DatabaseState {
    return this.state;
  }

  public resetToSeed(): void {
    this.state = this.getDefaultSeed();
    this.saveState();
  }

  // --- Audit Log Helper ---
  public addAuditLog(actorUserId: number, action: string, entityType: string, entityId: number | null, details: string): void {
    const nextId = this.state.auditLogs.length > 0 ? Math.max(...this.state.auditLogs.map(l => l.log_id)) + 1 : 1;
    const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
    this.state.auditLogs.unshift({
      log_id: nextId,
      actor_user_id: actorUserId,
      action,
      entity_type: entityType,
      entity_id: entityId,
      details,
      created_at: now,
    });
    this.saveState();
  }

  // --- Notification Helper ---
  public addNotification(userId: number, message: string, topicId: number | null = null): void {
    const nextId = this.state.notifications.length > 0 ? Math.max(...this.state.notifications.map(n => n.notification_id)) + 1 : 1;
    const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
    this.state.notifications.unshift({
      notification_id: nextId,
      user_id: userId,
      topic_id: topicId,
      message,
      is_read: false,
      created_at: now,
    });
    this.saveState();
  }

  public markNotificationAsRead(notificationId: number): void {
    this.state.notifications = this.state.notifications.map(n =>
      n.notification_id === notificationId ? { ...n, is_read: true } : n
    );
    this.saveState();
  }

  public markAllNotificationsAsRead(userId: number): void {
    this.state.notifications = this.state.notifications.map(n =>
      n.user_id === userId ? { ...n, is_read: true } : n
    );
    this.saveState();
  }

  // ==========================================
  // WF-03: ASSET CRUD OPERATIONS & RESTRICTIONS
  // ==========================================

  public createAsset(
    assetData: Omit<Asset, 'asset_id'>,
    actorUserId: number
  ): Asset {
    // Check asset_code uniqueness
    if (this.state.assets.some(a => a.asset_code.toLowerCase() === assetData.asset_code.trim().toLowerCase())) {
      throw new Error(`Mã thiết bị "${assetData.asset_code}" đã tồn tại trong hệ thống.`);
    }

    const nextId = this.state.assets.length > 0 ? Math.max(...this.state.assets.map(a => a.asset_id)) + 1 : 1;
    const newAsset: Asset = {
      asset_id: nextId,
      asset_name: assetData.asset_name.trim(),
      asset_status: assetData.asset_status,
      asset_code: assetData.asset_code.trim().toUpperCase(),
      asset_category: assetData.asset_category,
      serial_number: assetData.serial_number?.trim() || null,
      description: assetData.description?.trim() || null,
    };

    this.state.assets.push(newAsset);
    this.addAuditLog(actorUserId, 'ASSET_CREATE', 'ASSET', nextId, `Tạo thiết bị mới: [${newAsset.asset_code}] ${newAsset.asset_name}`);
    this.saveState();
    return newAsset;
  }

  public updateAsset(
    assetId: number,
    updateData: Partial<Omit<Asset, 'asset_id'>>,
    actorUserId: number
  ): Asset {
    const existing = this.state.assets.find(a => a.asset_id === assetId);
    if (!existing) {
      throw new Error(`Không tìm thấy thiết bị với ID ${assetId}.`);
    }

    if (updateData.asset_code && updateData.asset_code.trim().toLowerCase() !== existing.asset_code.toLowerCase()) {
      if (this.state.assets.some(a => a.asset_id !== assetId && a.asset_code.toLowerCase() === updateData.asset_code!.trim().toLowerCase())) {
        throw new Error(`Mã thiết bị "${updateData.asset_code}" đã tồn tại.`);
      }
    }

    const updated: Asset = {
      ...existing,
      ...updateData,
      asset_id: assetId,
      asset_code: updateData.asset_code ? updateData.asset_code.trim().toUpperCase() : existing.asset_code,
    };

    this.state.assets = this.state.assets.map(a => (a.asset_id === assetId ? updated : a));
    this.addAuditLog(actorUserId, 'ASSET_UPDATE', 'ASSET', assetId, `Cập nhật thiết bị [${updated.asset_code}] ${updated.asset_name}`);
    this.saveState();
    return updated;
  }

  public deleteAsset(assetId: number, actorUserId: number): void {
    const existing = this.state.assets.find(a => a.asset_id === assetId);
    if (!existing) {
      throw new Error(`Không tìm thấy thiết bị với ID ${assetId}.`);
    }

    // MANDATORY BUSINESS RULE: Cannot delete CHECKED_OUT asset
    if (existing.asset_status === 'CHECKED_OUT') {
      throw new Error('Không thể xóa thiết bị đang được sử dụng.');
    }

    // Check FK constraint with active/pending asset requests
    const hasActiveRequests = this.state.assetRequests.some(
      r => r.asset_id === assetId && (r.status === 'PENDING' || r.status === 'APPROVED' || r.status === 'CHECKED_OUT')
    );
    if (hasActiveRequests) {
      throw new Error('Không thể xóa thiết bị đang có yêu cầu mượn chưa hoàn tất (PENDING/APPROVED/CHECKED_OUT).');
    }

    this.state.assets = this.state.assets.filter(a => a.asset_id !== assetId);
    this.addAuditLog(actorUserId, 'ASSET_DELETE', 'ASSET', assetId, `Xóa thiết bị [${existing.asset_code}] ${existing.asset_name}`);
    this.saveState();
  }

  // ==========================================
  // ASSET REQUESTS & CONFLICT CHECKING (SCR-3.5, 3.6, 3.7)
  // ==========================================

  public createAssetRequest(
    data: { topic_id: number; asset_id: number; requester_user_id: number; start_time: string; end_time: string }
  ): AssetRequest {
    const topic = this.state.topics.find(t => t.topic_id === data.topic_id);
    if (!topic) {
      throw new Error('Đề tài không tồn tại.');
    }

    // MANDATORY BUSINESS RULE: Topic status MUST be APPROVED
    if (topic.status !== 'APPROVED' && topic.status !== 'IN_PROGRESS') {
      throw new Error('Chỉ có thể tạo yêu cầu mượn thiết bị khi đề tài đã được PHÊ DUYỆT (APPROVED).');
    }

    const start = new Date(data.start_time);
    const end = new Date(data.end_time);
    if (isNaN(start.getTime()) || isNaN(end.getTime()) || end <= start) {
      throw new Error('Thời gian kết thúc mượn phải lớn hơn thời gian bắt đầu.');
    }

    // Overlap conflict detection
    const conflict = this.checkScheduleConflict(data.asset_id, data.start_time, data.end_time);
    if (conflict) {
      throw new Error(`Phát hiện xung đột lịch: Thiết bị này đã có lịch mượn trùng từ ${conflict.start_time} đến ${conflict.end_time} (Yêu cầu #${conflict.request_id}).`);
    }

    const nextId = this.state.assetRequests.length > 0 ? Math.max(...this.state.assetRequests.map(r => r.request_id)) + 1 : 1;
    const newRequest: AssetRequest = {
      request_id: nextId,
      topic_id: data.topic_id,
      asset_id: data.asset_id,
      requester_user_id: data.requester_user_id,
      start_time: data.start_time,
      end_time: data.end_time,
      status: 'PENDING',
    };

    this.state.assetRequests.push(newRequest);
    this.addAuditLog(data.requester_user_id, 'ASSET_REQUEST_CREATE', 'ASSET_REQUEST', nextId, `Đăng ký mượn thiết bị #${data.asset_id} cho đề tài #${data.topic_id}`);
    
    // Notify logistics coordinator (Lộc: user_id 3)
    this.addNotification(3, `Phóng viên vừa tạo yêu cầu mượn thiết bị #${data.asset_id} cho đề tài ${topic.topic_code || '#' + topic.topic_id}.`, topic.topic_id);
    
    this.saveState();
    return newRequest;
  }

  public checkScheduleConflict(assetId: number, startTime: string, endTime: string, excludeRequestId?: number): AssetRequest | null {
    const reqStart = new Date(startTime).getTime();
    const reqEnd = new Date(endTime).getTime();

    const existing = this.state.assetRequests.find(r => {
      if (r.asset_id !== assetId) return false;
      if (excludeRequestId && r.request_id === excludeRequestId) return false;
      if (r.status !== 'APPROVED' && r.status !== 'CHECKED_OUT') return false;

      const exStart = new Date(r.start_time).getTime();
      const exEnd = new Date(r.end_time).getTime();

      // Confirmed formula: requested_start < existing_end AND requested_end > existing_start
      return reqStart < exEnd && reqEnd > exStart;
    });

    return existing || null;
  }

  public updateAssetRequestStatus(
    requestId: number,
    newStatus: 'APPROVED' | 'REJECTED' | 'CHECKED_OUT' | 'RETURNED',
    actorUserId: number,
    returnCondition: 'AVAILABLE' | 'MAINTENANCE' = 'AVAILABLE'
  ): void {
    const req = this.state.assetRequests.find(r => r.request_id === requestId);
    if (!req) {
      throw new Error(`Không tìm thấy yêu cầu mượn #${requestId}`);
    }

    const asset = this.state.assets.find(a => a.asset_id === req.asset_id);
    if (!asset) {
      throw new Error('Thiết bị liên kết không tồn tại.');
    }

    if (newStatus === 'APPROVED') {
      const conflict = this.checkScheduleConflict(req.asset_id, req.start_time, req.end_time, req.request_id);
      if (conflict) {
        throw new Error(`Không thể phê duyệt vì trùng lịch với yêu cầu #${conflict.request_id}.`);
      }
      req.status = 'APPROVED';
      this.addNotification(req.requester_user_id, `Yêu cầu mượn thiết bị ${asset.asset_name} của bạn đã được PHÊ DUYỆT.`, req.topic_id);
    } else if (newStatus === 'REJECTED') {
      req.status = 'REJECTED';
      this.addNotification(req.requester_user_id, `Yêu cầu mượn thiết bị ${asset.asset_name} của bạn đã bị TỪ CHỐI.`, req.topic_id);
    } else if (newStatus === 'CHECKED_OUT') {
      if (asset.asset_status === 'CHECKED_OUT') {
        throw new Error('Thiết bị hiện đang được người khác sử dụng, không thể xuất kho.');
      }
      req.status = 'CHECKED_OUT';
      asset.asset_status = 'CHECKED_OUT';
      this.addNotification(req.requester_user_id, `Thiết bị ${asset.asset_name} đã được xuất kho bàn giao cho bạn.`, req.topic_id);
    } else if (newStatus === 'RETURNED') {
      req.status = 'RETURNED';
      asset.asset_status = returnCondition; // 'AVAILABLE' or 'MAINTENANCE'
      this.addNotification(req.requester_user_id, `Thiết bị ${asset.asset_name} đã được hoàn trả kho thành công. Trạng thái: ${returnCondition === 'MAINTENANCE' ? 'Đang bảo dưỡng' : 'Sẵn sàng'}.`, req.topic_id);
    }

    this.addAuditLog(actorUserId, `ASSET_REQUEST_${newStatus}`, 'ASSET_REQUEST', requestId, `Chuyển trạng thái yêu cầu mượn sang ${newStatus}`);
    this.saveState();
  }

  // ==========================================
  // WF-01: TOPIC PROPOSAL & DRAFT MANAGEMENT
  // ==========================================

  public createTopic(
    topicData: { title: string; angle: string; category_id: number; deadline: string; status: TopicStatus },
    members: Array<{ user_id: number; role_in_team: 'LEAD_REPORTER' | 'PHOTO_REPORTER' | 'CAMERAMAN' | 'ASSISTANT' }>,
    estimates: Array<{ cost_item: string; amount: number }>,
    actorUserId: number
  ): Topic {
    if (!topicData.title.trim()) {
      throw new Error('Tiêu đề đề tài không được để trống.');
    }
    if (!topicData.category_id) {
      throw new Error('Vui lòng chọn chuyên mục.');
    }

    const nextTopicId = this.state.topics.length > 0 ? Math.max(...this.state.topics.map(t => t.topic_id)) + 1 : 1;
    const newTopic: Topic = {
      topic_id: nextTopicId,
      topic_code: null,
      title: topicData.title.trim(),
      angle: topicData.angle.trim() || null,
      status: topicData.status,
      deadline: topicData.deadline || null,
      category_id: topicData.category_id,
      author_user_id: actorUserId,
    };

    this.state.topics.push(newTopic);

    // Save Members
    let nextMemberId = this.state.topicMembers.length > 0 ? Math.max(...this.state.topicMembers.map(m => m.member_id)) + 1 : 1;
    for (const mem of members) {
      this.state.topicMembers.push({
        member_id: nextMemberId++,
        topic_id: nextTopicId,
        user_id: mem.user_id,
        role_in_team: mem.role_in_team,
      });
    }

    // Save Estimates
    let nextEstimateId = this.state.topicEstimates.length > 0 ? Math.max(...this.state.topicEstimates.map(e => e.estimate_id)) + 1 : 1;
    for (const est of estimates) {
      if (est.cost_item.trim() && est.amount > 0) {
        this.state.topicEstimates.push({
          estimate_id: nextEstimateId++,
          topic_id: nextTopicId,
          cost_item: est.cost_item.trim(),
          amount: Number(est.amount),
        });
      }
    }

    this.addAuditLog(actorUserId, 'TOPIC_CREATE', 'TOPIC', nextTopicId, `Phóng viên nộp đề tài: "${newTopic.title}" (Trạng thái: ${newTopic.status})`);

    // If submitted, notify Desk Editor (Trường: user_id 1)
    if (newTopic.status === 'SUBMITTED') {
      this.addNotification(1, `Phóng viên đã nộp đề xuất đề tài mới: "${newTopic.title}".`, nextTopicId);
    }

    this.saveState();
    return newTopic;
  }

  public updateTopic(
    topicId: number,
    topicData: { title: string; angle: string; category_id: number; deadline: string; status?: Topic['status'] },
    members: Array<{ user_id: number; role_in_team: 'LEAD_REPORTER' | 'PHOTO_REPORTER' | 'CAMERAMAN' | 'ASSISTANT' }>,
    estimates: Array<{ cost_item: string; amount: number }>,
    actorUserId: number
  ): Topic {
    const existing = this.state.topics.find(t => t.topic_id === topicId);
    if (!existing) {
      throw new Error(`Không tìm thấy đề tài #${topicId}`);
    }

    // If topic is REVISION_REQUESTED and reporter resubmits, it returns to SUBMITTED
    const newStatus = topicData.status || (existing.status === 'REVISION_REQUESTED' ? 'SUBMITTED' : existing.status);

    existing.title = topicData.title.trim();
    existing.angle = topicData.angle.trim() || null;
    existing.category_id = topicData.category_id;
    existing.deadline = topicData.deadline || null;
    existing.status = newStatus;

    // Update Members (delete and re-insert)
    this.state.topicMembers = this.state.topicMembers.filter(m => m.topic_id !== topicId);
    let nextMemberId = this.state.topicMembers.length > 0 ? Math.max(...this.state.topicMembers.map(m => m.member_id)) + 1 : 1;
    for (const mem of members) {
      this.state.topicMembers.push({
        member_id: nextMemberId++,
        topic_id: topicId,
        user_id: mem.user_id,
        role_in_team: mem.role_in_team,
      });
    }

    // Update Estimates
    this.state.topicEstimates = this.state.topicEstimates.filter(e => e.topic_id !== topicId);
    let nextEstimateId = this.state.topicEstimates.length > 0 ? Math.max(...this.state.topicEstimates.map(e => e.estimate_id)) + 1 : 1;
    for (const est of estimates) {
      if (est.cost_item.trim() && est.amount > 0) {
        this.state.topicEstimates.push({
          estimate_id: nextEstimateId++,
          topic_id: topicId,
          cost_item: est.cost_item.trim(),
          amount: Number(est.amount),
        });
      }
    }

    this.addAuditLog(actorUserId, 'TOPIC_UPDATE', 'TOPIC', topicId, `Cập nhật đề tài: "${existing.title}" (Trạng thái mới: ${existing.status})`);

    if (existing.status === 'SUBMITTED') {
      this.addNotification(1, `Phóng viên đã hoàn thiện chỉnh sửa và nộp lại đề tài: "${existing.title}".`, topicId);
    }

    this.saveState();
    return existing;
  }

  // Duplicate Topic Search (SCR-1.3)
  public searchDuplicateTopics(query: string, categoryId?: number): Topic[] {
    const q = query.trim().toLowerCase();
    if (!q) return [];
    return this.state.topics.filter(t => {
      const matchText = t.title.toLowerCase().includes(q) || (t.angle && t.angle.toLowerCase().includes(q));
      if (!matchText) return false;
      if (categoryId && t.category_id !== categoryId) return false;
      return true;
    });
  }

  // ==========================================
  // WF-02: EDITORIAL RUBRIC EVALUATION & ESCALATION
  // ==========================================

  public evaluateTopic(
    topicId: number,
    evaluatorUserId: number,
    stage: 'DESK_REVIEW' | 'EXECUTIVE_APPROVAL',
    decision: 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED' | 'ESCALATED',
    comments: string,
    scores: Array<{ criteria_id: number; score: number; item_comment: string }>,
    legalRiskScore?: number
  ): void {
    const topic = this.state.topics.find(t => t.topic_id === topicId);
    if (!topic) {
      throw new Error(`Không tìm thấy đề tài #${topicId}`);
    }

    // Calculate weighted total score
    let totalWeightedScore = 0;
    for (const item of scores) {
      const crit = this.state.criteriaConfigs.find(c => c.criteria_id === item.criteria_id);
      const weight = crit ? crit.weight_percent : 25;
      totalWeightedScore += (item.score * weight) / 100;
    }
    totalWeightedScore = Math.round(totalWeightedScore * 100) / 100;

    // MANDATORY BUSINESS RULES:
    // 1. Direct approval threshold: >= 5.0 / 10
    if (decision === 'APPROVED' && totalWeightedScore < 5.0) {
      throw new Error(`Điểm thẩm định trung bình (${totalWeightedScore.toFixed(2)}/10) chưa đạt ngưỡng tối thiểu 5.0 để phê duyệt trực tiếp.`);
    }

    // 2. Legal risk >= 8.0 requires ESCALATION
    if (legalRiskScore && legalRiskScore >= 8.0 && decision === 'APPROVED' && stage === 'DESK_REVIEW') {
      throw new Error(`Điểm rủi ro pháp lý (${legalRiskScore}/10 >= 8.0) thuộc diện nhạy cảm đặc biệt. Bắt buộc phải chuyển thẩm định vượt cấp (ESCALATED) lên Tổng Biên Tập.`);
    }

    // Insert TopicEvaluation
    const nextEvalId = this.state.topicEvaluations.length > 0 ? Math.max(...this.state.topicEvaluations.map(e => e.evaluation_id)) + 1 : 1;
    const newEval: TopicEvaluation = {
      evaluation_id: nextEvalId,
      topic_id: topicId,
      evaluator_user_id: evaluatorUserId,
      evaluation_stage: stage,
      decision,
      comments: comments.trim() || null,
    };
    this.state.topicEvaluations.push(newEval);

    // Insert EvaluationDetail
    let nextDetailId = this.state.evaluationDetails.length > 0 ? Math.max(...this.state.evaluationDetails.map(d => d.detail_id)) + 1 : 1;
    for (const sc of scores) {
      this.state.evaluationDetails.push({
        detail_id: nextDetailId++,
        evaluation_id: nextEvalId,
        criteria_id: sc.criteria_id,
        score: sc.score,
        item_comment: sc.item_comment.trim() || null,
      });
    }

    // State transition
    if (decision === 'APPROVED') {
      topic.status = 'APPROVED';
      if (!topic.topic_code) {
        const year = new Date().getFullYear();
        const codeNum = String(topic.topic_id).padStart(3, '0');
        topic.topic_code = `TOPIC-${year}-${codeNum}`;
      }
      this.addNotification(topic.author_user_id, `Đề tài "${topic.title}" đã được PHÊ DUYỆT thành công (Mã: ${topic.topic_code}, Điểm: ${totalWeightedScore.toFixed(2)}/10).`, topic.topic_id);
    } else if (decision === 'REJECTED') {
      topic.status = 'REJECTED';
      this.addNotification(topic.author_user_id, `Đề tài "${topic.title}" đã bị TỪ CHỐI thẩm định. Nhận xét: ${comments}`, topic.topic_id);
    } else if (decision === 'REVISION_REQUESTED') {
      topic.status = 'REVISION_REQUESTED';
      this.addNotification(topic.author_user_id, `Đề tài "${topic.title}" yêu cầu BỔ SUNG / CHỈNH SỬA: ${comments}`, topic.topic_id);
    } else if (decision === 'ESCALATED') {
      topic.status = 'PENDING_EXECUTIVE_APPROVAL';
      // Notify Executive Editor (Tổng Biên Tập: user_id 6)
      this.addNotification(6, `Đề tài "${topic.title}" có rủi ro pháp lý cao được BTV chuyển lên Tổng Biên Tập thẩm định vượt cấp.`, topic.topic_id);
    }

    this.addAuditLog(evaluatorUserId, `TOPIC_${decision}`, 'TOPIC', topicId, `Thẩm định đề tài (${stage}): Quyết định ${decision}, Điểm tổng: ${totalWeightedScore.toFixed(2)}/10`);
    this.saveState();
  }

  // Reactivate archived/rejected topic (SCR-2.8)
  public reactivateTopic(topicId: number, actorUserId: number): void {
    const topic = this.state.topics.find(t => t.topic_id === topicId);
    if (!topic) throw new Error('Không tìm thấy đề tài.');
    if (topic.status !== 'REJECTED' && topic.status !== 'ABORTED') {
      throw new Error('Chỉ có thể kích hoạt lại đề tài đang ở trạng thái TỪ CHỐI hoặc HỦY.');
    }
    topic.status = 'SUBMITTED';
    this.addAuditLog(actorUserId, 'TOPIC_REACTIVATE', 'TOPIC', topicId, `Tái kích hoạt đề tài "${topic.title}" trở lại hàng đợi thẩm định.`);
    this.addNotification(topic.author_user_id, `Đề tài "${topic.title}" đã được BTV tái kích hoạt và đưa vào hàng đợi thẩm định.`, topicId);
    this.saveState();
  }

  // ==========================================
  // WF-04: FIELDWORK KANBAN & TASKS
  // ==========================================

  public createFieldworkTask(
    data: { topic_id: number; assignee_user_id: number; description: string; task_status?: 'TODO' | 'IN_PROGRESS' | 'DONE' },
    actorUserId: number
  ): FieldworkTask {
    const topic = this.state.topics.find(t => t.topic_id === data.topic_id);
    if (!topic) throw new Error('Đề tài không tồn tại.');

    const nextId = this.state.fieldworkTasks.length > 0 ? Math.max(...this.state.fieldworkTasks.map(t => t.task_id)) + 1 : 1;
    const newTask: FieldworkTask = {
      task_id: nextId,
      topic_id: data.topic_id,
      assignee_user_id: data.assignee_user_id,
      task_status: data.task_status || 'TODO',
      description: data.description.trim(),
    };

    this.state.fieldworkTasks.push(newTask);

    // If topic was approved, set to IN_PROGRESS when tasks start
    if (topic.status === 'APPROVED') {
      topic.status = 'IN_PROGRESS';
    }

    this.addNotification(data.assignee_user_id, `Bạn được phân công nhiệm vụ hiện trường mới cho đề tài ${topic.topic_code || '#' + topic.topic_id}: "${newTask.description}".`, topic.topic_id);
    this.addAuditLog(actorUserId, 'TASK_CREATE', 'FIELDWORK_TASK', nextId, `Tạo nhiệm vụ tác nghiệp cho người dùng #${data.assignee_user_id}`);
    this.saveState();
    return newTask;
  }

  public updateTaskStatus(taskId: number, newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE', deliverableNote?: string): void {
    const task = this.state.fieldworkTasks.find(t => t.task_id === taskId);
    if (!task) throw new Error('Không tìm thấy nhiệm vụ.');

    task.task_status = newStatus;
    if (deliverableNote) {
      task.deliverable_note = deliverableNote;
      task.submitted_at = new Date().toISOString().replace('T', ' ').substring(0, 19);
    }

    this.saveState();
  }

  // Emergency Incident Report (SCR-4.4)
  public reportEmergencyIncident(topicId: number, incidentDescription: string, actorUserId: number): void {
    const topic = this.state.topics.find(t => t.topic_id === topicId);
    if (!topic) throw new Error('Không tìm thấy đề tài.');

    this.addAuditLog(actorUserId, 'EMERGENCY_INCIDENT', 'TOPIC', topicId, `BÁO CÁO SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG: ${incidentDescription}`);
    // Broadcast urgent notifications to coordinators and desk editors
    this.addNotification(1, `[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG] Đề tài ${topic.topic_code || '#' + topic.topic_id}: ${incidentDescription}`, topicId);
    this.addNotification(4, `[SỰ CỐ KHẨN CẤP HIỆN TRƯỜNG] Đề tài ${topic.topic_code || '#' + topic.topic_id}: ${incidentDescription}`, topicId);
    this.saveState();
  }

  // Deadline Extension Request (SCR-1.8)
  public requestDeadlineExtension(
    data: { topic_id: number; requested_by_user_id: number; current_deadline: string; requested_deadline: string; reason: string }
  ): RequestDeadlineExtension {
    const current = new Date(data.current_deadline);
    const requested = new Date(data.requested_deadline);
    if (requested <= current) {
      throw new Error('Hạn chót gia hạn phải sau hạn chót hiện tại.');
    }

    const nextId = this.state.extensions.length > 0 ? Math.max(...this.state.extensions.map(e => e.request_id)) + 1 : 1;
    const newExt: RequestDeadlineExtension = {
      request_id: nextId,
      topic_id: data.topic_id,
      requested_by_user_id: data.requested_by_user_id,
      current_deadline: data.current_deadline,
      requested_deadline: data.requested_deadline,
      reason: data.reason.trim(),
      status: 'PENDING',
    };

    this.state.extensions.push(newExt);
    this.addNotification(1, `Phóng viên yêu cầu gia hạn deadline cho đề tài #${data.topic_id} đến ngày ${data.requested_deadline}.`, data.topic_id);
    this.addAuditLog(data.requested_by_user_id, 'EXTENSION_REQUEST', 'REQUEST_DEADLINE_EXTENSION', nextId, `Xin lùi hạn nộp đề tài #${data.topic_id} đến ${data.requested_deadline}`);
    this.saveState();
    return newExt;
  }

  public decideDeadlineExtension(requestId: number, decision: 'APPROVED' | 'REJECTED', actorUserId: number): void {
    const ext = this.state.extensions.find(e => e.request_id === requestId);
    if (!ext) throw new Error('Không tìm thấy yêu cầu gia hạn.');

    ext.status = decision;
    const topic = this.state.topics.find(t => t.topic_id === ext.topic_id);
    if (decision === 'APPROVED' && topic) {
      topic.deadline = ext.requested_deadline;
    }

    this.addNotification(ext.requested_by_user_id, `Yêu cầu gia hạn deadline cho đề tài #${ext.topic_id} đã được ${decision === 'APPROVED' ? 'CHẤP THUẬN' : 'TỪ CHỐI'}.`, ext.topic_id);
    this.addAuditLog(actorUserId, `EXTENSION_${decision}`, 'REQUEST_DEADLINE_EXTENSION', requestId, `Xử lý gia hạn: ${decision}`);
    this.saveState();
  }

  // ==========================================
  // WF-05: ADMIN, QUOTAS, CRITERIA & RBAC
  // ==========================================

  public setDeskQuota(deskId: number, period: string, count: number, actorUserId: number): void {
    const existing = this.state.deskQuotas.find(q => q.desk_id === deskId && q.period === period);
    if (existing) {
      existing.quota_count = count;
    } else {
      const nextId = this.state.deskQuotas.length > 0 ? Math.max(...this.state.deskQuotas.map(q => q.quota_id)) + 1 : 1;
      this.state.deskQuotas.push({
        quota_id: nextId,
        desk_id: deskId,
        period,
        quota_count: count,
      });
    }
    this.addAuditLog(actorUserId, 'QUOTA_UPDATE', 'DESK_QUOTA', deskId, `Cập nhật định mức Ban #${deskId} kỳ ${period}: ${count} đề tài`);
    this.saveState();
  }

  public updateCriteriaConfig(criteriaId: number, weightPercent: number, actorUserId: number): void {
    const crit = this.state.criteriaConfigs.find(c => c.criteria_id === criteriaId);
    if (!crit) throw new Error('Không tìm thấy tiêu chí.');
    crit.weight_percent = weightPercent;
    this.addAuditLog(actorUserId, 'CRITERIA_UPDATE', 'CRITERIA_CONFIG', criteriaId, `Cập nhật trọng số tiêu chí "${crit.criteria_name}" thành ${weightPercent}%`);
    this.saveState();
  }

  public updateSLAConfig(newConfig: SLAConfig, actorUserId: number): void {
    this.state.slaConfig = { ...newConfig };
    this.addAuditLog(actorUserId, 'SLA_CONFIG_UPDATE', 'SLA_CONFIG', null, `Cập nhật ngưỡng cảnh báo SLA: ${newConfig.threshold_hours_1}h / ${newConfig.threshold_hours_2}h / ${newConfig.threshold_hours_3}h`);
    this.saveState();
  }

  public updateUserStatus(userId: number, active: boolean, actorUserId: number): void {
    const user = this.state.users.find(u => u.user_id === userId);
    if (!user) throw new Error('Không tìm thấy người dùng.');
    user.active_status = active;
    this.addAuditLog(actorUserId, 'USER_STATUS_CHANGE', 'USER', userId, `Đổi trạng thái tài khoản ${user.email} thành ${active ? 'Hoạt động' : 'Khóa'}`);
    this.saveState();
  }

  public toggleRolePermission(roleId: number, permissionId: number, enabled: boolean, actorUserId: number): void {
    if (enabled) {
      if (!this.state.rolePermissions.some(rp => rp.role_id === roleId && rp.permission_id === permissionId)) {
        this.state.rolePermissions.push({ role_id: roleId, permission_id: permissionId });
      }
    } else {
      this.state.rolePermissions = this.state.rolePermissions.filter(
        rp => !(rp.role_id === roleId && rp.permission_id === permissionId)
      );
    }
    this.addAuditLog(actorUserId, 'RBAC_UPDATE', 'ROLE_PERMISSION', roleId, `Cập nhật quyền #${permissionId} cho vai trò #${roleId}: ${enabled ? 'Bật' : 'Tắt'}`);
    this.saveState();
  }

  // ==========================================
  // BATCH ASSET REQUESTS (Multiple items in single request)
  // ==========================================
  public createBatchAssetRequests(
    data: {
      topic_id: number;
      asset_ids: number[];
      requester_user_id: number;
      start_time: string;
      end_time: string;
      purpose_note?: string;
    }
  ): AssetRequest[] {
    const topic = this.state.topics.find(t => t.topic_id === data.topic_id);
    if (!topic) throw new Error('Đề tài không tồn tại.');
    if (topic.status !== 'APPROVED' && topic.status !== 'IN_PROGRESS' && topic.status !== 'ASSIGNED') {
      throw new Error('Chỉ có thể tạo yêu cầu mượn thiết bị khi đề tài đã được PHÊ DUYỆT (APPROVED/IN_PROGRESS).');
    }
    if (!data.asset_ids || data.asset_ids.length === 0) {
      throw new Error('Vui lòng chọn ít nhất 1 thiết bị hoặc phương tiện tác nghiệp.');
    }

    const start = new Date(data.start_time);
    const end = new Date(data.end_time);
    if (isNaN(start.getTime()) || isNaN(end.getTime()) || end <= start) {
      throw new Error('Thời gian kết thúc mượn phải lớn hơn thời gian bắt đầu.');
    }

    const results: AssetRequest[] = [];
    for (const assetId of data.asset_ids) {
      const conflict = this.checkScheduleConflict(assetId, data.start_time, data.end_time);
      if (conflict) {
        const item = this.state.assets.find(a => a.asset_id === assetId);
        throw new Error(`Xung đột lịch với "${item?.asset_name || assetId}": đã có lịch từ ${conflict.start_time} đến ${conflict.end_time}.`);
      }

      const nextId = this.state.assetRequests.length > 0 ? Math.max(...this.state.assetRequests.map(r => r.request_id)) + 1 : 1;
      const newReq: AssetRequest = {
        request_id: nextId,
        topic_id: data.topic_id,
        asset_id: assetId,
        requester_user_id: data.requester_user_id,
        start_time: data.start_time,
        end_time: data.end_time,
        status: 'PENDING',
      };
      this.state.assetRequests.push(newReq);
      results.push(newReq);
    }

    this.addAuditLog(
      data.requester_user_id,
      'ASSET_REQUEST_BATCH',
      'ASSET_REQUEST',
      results[0]?.request_id || null,
      `Đăng ký mượn ${data.asset_ids.length} thiết bị/xe cho đề tài #${data.topic_id}`
    );
    this.addNotification(3, `Phóng viên vừa gửi đơn đăng ký mượn ${data.asset_ids.length} thiết bị/phương tiện cho đề tài ${topic.topic_code || '#' + topic.topic_id}.`, topic.topic_id);
    this.saveState();
    return results;
  }

  // ==========================================
  // CREW MEMBER SCHEDULE CLASH CHECKING
  // ==========================================
  public checkUserScheduleConflicts(
    userId: number
  ): Array<{ topic_id: number; topic_title: string; topic_code: string | null; deadline: string | null; role_in_team: string; status: string }> {
    const memberEntries = this.state.topicMembers.filter(m => m.user_id === userId);
    const activeTopicIds = new Set<number>();

    this.state.topics.forEach(t => {
      if (t.author_user_id === userId && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED' || t.status === 'SUBMITTED')) {
        activeTopicIds.add(t.topic_id);
      }
    });

    memberEntries.forEach(m => {
      const t = this.state.topics.find(top => top.topic_id === m.topic_id);
      if (t && (t.status === 'APPROVED' || t.status === 'IN_PROGRESS' || t.status === 'ASSIGNED' || t.status === 'SUBMITTED')) {
        activeTopicIds.add(t.topic_id);
      }
    });

    const conflicts: Array<{ topic_id: number; topic_title: string; topic_code: string | null; deadline: string | null; role_in_team: string; status: string }> = [];
    activeTopicIds.forEach(tId => {
      const t = this.state.topics.find(top => top.topic_id === tId);
      if (t) {
        const memberRole = memberEntries.find(m => m.topic_id === tId)?.role_in_team || (t.author_user_id === userId ? 'LEAD_REPORTER' : 'MEMBER');
        conflicts.push({
          topic_id: t.topic_id,
          topic_title: t.title,
          topic_code: t.topic_code,
          deadline: t.deadline,
          role_in_team: memberRole,
          status: t.status,
        });
      }
    });

    return conflicts;
  }

  // ==========================================
  // USER FULL CRUD & ROLE FAST-SWITCH FOR SYSTEM ADMIN
  // ==========================================
  public createUser(
    userData: {
      full_name: string;
      email: string;
      password_hash: string;
      role_id: number;
      active_status: boolean;
      desk_id: number | null;
    },
    actorUserId: number
  ): User {
    if (!userData.full_name.trim()) throw new Error('Họ và tên nhân sự không được để trống.');
    if (!userData.email.trim()) throw new Error('Email tài khoản không được để trống.');
    if (this.state.users.some(u => u.email.toLowerCase() === userData.email.trim().toLowerCase())) {
      throw new Error(`Email "${userData.email}" đã tồn tại trong hệ thống.`);
    }

    const nextId = this.state.users.length > 0 ? Math.max(...this.state.users.map(u => u.user_id)) + 1 : 1;
    const newUser: User = {
      user_id: nextId,
      full_name: userData.full_name.trim(),
      email: userData.email.trim().toLowerCase(),
      password_hash: userData.password_hash || '123',
      role_id: userData.role_id,
      active_status: userData.active_status,
      desk_id: userData.desk_id,
    };

    this.state.users.push(newUser);
    this.addAuditLog(actorUserId, 'USER_CREATE', 'USER', nextId, `Tạo tài khoản nhân sự: [USR-${String(nextId).padStart(3, '0')}] ${newUser.full_name} (${newUser.email})`);
    this.saveState();
    return newUser;
  }

  public updateUser(
    userId: number,
    updateData: Partial<Omit<User, 'user_id'>>,
    actorUserId: number
  ): User {
    const existing = this.state.users.find(u => u.user_id === userId);
    if (!existing) throw new Error(`Không tìm thấy người dùng #${userId}`);

    if (updateData.email && updateData.email.trim().toLowerCase() !== existing.email.toLowerCase()) {
      if (this.state.users.some(u => u.user_id !== userId && u.email.toLowerCase() === updateData.email!.trim().toLowerCase())) {
        throw new Error(`Email "${updateData.email}" đã được người dùng khác sử dụng.`);
      }
    }

    const updated: User = {
      ...existing,
      ...updateData,
      user_id: userId,
    };

    this.state.users = this.state.users.map(u => (u.user_id === userId ? updated : u));
    this.addAuditLog(actorUserId, 'USER_UPDATE', 'USER', userId, `Cập nhật thông tin nhân viên [USR-${String(userId).padStart(3, '0')}] ${updated.full_name}`);
    this.saveState();
    return updated;
  }

  public deleteUser(userId: number, actorUserId: number): void {
    const existing = this.state.users.find(u => u.user_id === userId);
    if (!existing) throw new Error(`Không tìm thấy người dùng #${userId}`);
    if (userId === actorUserId) {
      throw new Error('Không thể tự xóa tài khoản bạn đang đăng nhập.');
    }

    this.state.users = this.state.users.filter(u => u.user_id !== userId);
    this.addAuditLog(actorUserId, 'USER_DELETE', 'USER', userId, `Xóa tài khoản nhân sự: ${existing.full_name} (${existing.email})`);
    this.saveState();
  }

  public changeUserRole(userId: number, newRoleId: number, actorUserId: number): void {
    const user = this.state.users.find(u => u.user_id === userId);
    if (!user) throw new Error(`Không tìm thấy người dùng #${userId}`);
    const oldRoleId = user.role_id;
    user.role_id = newRoleId;

    const roleName = this.state.roles.find(r => r.role_id === newRoleId)?.role_name || newRoleId;
    this.addAuditLog(actorUserId, 'USER_ROLE_CHANGE', 'USER', userId, `Chuyển vai trò nhân sự ${user.full_name} từ #${oldRoleId} sang ${roleName}`);
    this.saveState();
  }

  // ==========================================
  // SCR-4.8: BROADCAST AUDIT & READ RECEIPTS
  // ==========================================
  public createBroadcastNotification(
    data: {
      title: string;
      content: string;
      urgency: BroadcastUrgency;
      scope: 'ALL' | 'DESK' | 'CREW';
      target_desk_id?: number;
      target_topic_id?: number;
    },
    senderUserId: number
  ): BroadcastNotification {
    const sender = this.state.users.find(u => u.user_id === senderUserId);
    const senderName = sender ? `${sender.full_name}` : 'Trung tâm Điều phối';

    let targetUsers: User[] = [];
    let targetName = 'Toàn thể Tòa soạn';

    if (data.scope === 'DESK' && data.target_desk_id) {
      targetUsers = this.state.users.filter(u => u.desk_id === data.target_desk_id || u.role_id === 4 || u.role_id === 5);
      const desk = this.state.desks.find(d => d.desk_id === data.target_desk_id);
      targetName = desk ? desk.desk_name : 'Ban Chuyên môn';
    } else if (data.scope === 'CREW' && data.target_topic_id) {
      const topic = this.state.topics.find(t => t.topic_id === data.target_topic_id);
      const memberIds = this.state.topicMembers.filter(m => m.topic_id === data.target_topic_id).map(m => m.user_id);
      if (topic) memberIds.push(topic.author_user_id);
      targetUsers = this.state.users.filter(u => memberIds.includes(u.user_id));
      targetName = topic ? `Ekip đề tài ${topic.topic_code || '#' + topic.topic_id}` : 'Tổ Tác nghiệp';
    } else {
      targetUsers = this.state.users.filter(u => u.active_status);
      targetName = 'Toàn thể Tòa soạn';
    }

    const now = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const nextId = this.state.broadcastNotifications && this.state.broadcastNotifications.length > 0
      ? Math.max(...this.state.broadcastNotifications.map(b => b.broadcast_id)) + 1
      : 1;

    const recipients: BroadcastRecipient[] = targetUsers.map(u => {
      const desk = this.state.desks.find(d => d.desk_id === u.desk_id);
      const isSender = u.user_id === senderUserId;
      return {
        user_id: u.user_id,
        user_name: u.full_name,
        email: u.email,
        desk_name: desk?.desk_name || 'Khối Điều hành',
        is_read: isSender,
        read_at: isSender ? now : null,
      };
    });

    const newBroadcast: BroadcastNotification = {
      broadcast_id: nextId,
      sender_user_id: senderUserId,
      sender_name: senderName,
      title: data.title.trim(),
      content: data.content.trim(),
      urgency: data.urgency,
      scope: data.scope,
      target_name: targetName,
      created_at: now,
      recipients,
    };

    if (!this.state.broadcastNotifications) {
      this.state.broadcastNotifications = [];
    }
    this.state.broadcastNotifications.unshift(newBroadcast);

    targetUsers.forEach(u => {
      if (u.user_id !== senderUserId) {
        this.addNotification(u.user_id, `[${data.urgency === 'EMERGENCY' ? 'KHẨN CẤP' : 'THÔNG BÁO TÁC NGHIỆP'}] ${data.title}: ${data.content.substring(0, 80)}...`);
      }
    });

    this.addAuditLog(senderUserId, 'BROADCAST_SEND', 'BROADCAST_NOTIFICATION', nextId, `Phát thông báo [${data.urgency}] tới ${targetName}: "${data.title}"`);
    this.saveState();
    return newBroadcast;
  }

  public markBroadcastAsRead(broadcastId: number, userId: number): void {
    if (!this.state.broadcastNotifications) return;
    const broadcast = this.state.broadcastNotifications.find(b => b.broadcast_id === broadcastId);
    if (!broadcast) return;

    const recipient = broadcast.recipients.find(r => r.user_id === userId);
    if (recipient && !recipient.is_read) {
      recipient.is_read = true;
      recipient.read_at = new Date().toISOString().replace('T', ' ').substring(0, 19);
      this.saveState();
    }
  }

  public resendBroadcastNotification(broadcastId: number, actorUserId: number): void {
    if (!this.state.broadcastNotifications) return;
    const broadcast = this.state.broadcastNotifications.find(b => b.broadcast_id === broadcastId);
    if (!broadcast) return;

    const unreadRecipients = broadcast.recipients.filter(r => !r.is_read);
    unreadRecipients.forEach(r => {
      this.addNotification(r.user_id, `[NHẮC LẠI / PING] Bạn chưa xác nhận đã đọc thông báo: "${broadcast.title}". Vui lòng kiểm tra ngay.`);
    });

    this.addAuditLog(actorUserId, 'BROADCAST_PING', 'BROADCAST_NOTIFICATION', broadcastId, `Gửi nhắc nhở đọc thông báo tới ${unreadRecipients.length} nhân sự chưa xác nhận.`);
    this.saveState();
  }
}

export const dbStore = new DatabaseStore();

