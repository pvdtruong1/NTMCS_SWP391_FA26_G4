/* NTMCS demo data. Field names mirror the supplied SQL Server schema exactly. */
window.NTMCS_SEED = {
  Desk: [
    {desk_id:1, desk_name:'Thời sự'},
    {desk_id:2, desk_name:'Phóng sự'},
    {desk_id:3, desk_name:'Văn hóa - Giáo dục'}
  ],
  Category: [
    {category_id:1, category_name:'Chính trị - Xã hội', parent_category_id:null, desk_id:1},
    {category_id:2, category_name:'Giao thông', parent_category_id:1, desk_id:1},
    {category_id:3, category_name:'Phóng sự ảnh', parent_category_id:null, desk_id:2},
    {category_id:4, category_name:'Giáo dục', parent_category_id:null, desk_id:3},
    {category_id:5, category_name:'Văn hóa', parent_category_id:null, desk_id:3}
  ],
  User: [
    {user_id:1, full_name:'Lộc Nguyễn', email:'loc@ntmcs.local', role:'REPORTER', desk_id:1},
    {user_id:2, full_name:'Minh Anh', email:'minhanh@ntmcs.local', role:'DESK_EDITOR', desk_id:1},
    {user_id:3, full_name:'Thu Hà', email:'thuha@ntmcs.local', role:'DESK_EDITOR', desk_id:1},
    {user_id:4, full_name:'Quang Minh', email:'quangminh@ntmcs.local', role:'EXECUTIVE_EDITOR', desk_id:null},
    {user_id:5, full_name:'Ngọc Lan', email:'ngoclan@ntmcs.local', role:'REPORTER', desk_id:3},
    {user_id:6, full_name:'Đức Long', email:'duclong@ntmcs.local', role:'LOGISTICS_COORDINATOR', desk_id:null},
    {user_id:7, full_name:'Hoàng Nam', email:'hoangnam@ntmcs.local', role:'BUSINESS_ADMINISTRATOR', desk_id:null},
    {user_id:8, full_name:'Thanh Bình', email:'thanhbinh@ntmcs.local', role:'FIELDWORK_COORDINATOR', desk_id:null},
    {user_id:9, full_name:'System Admin', email:'admin@ntmcs.local', role:'SYSTEM_ADMINISTRATOR', desk_id:null},
    {user_id:10, full_name:'Tuấn Anh', email:'tuananh@ntmcs.local', role:'REPORTER', desk_id:2}
  ],
  Topic: [
    {topic_id:1001, topic_code:'TS-2026-001', title:'Mô hình giao thông xanh tại các quận nội đô Hà Nội', angle:'Tác động tới người dân và khả năng mở rộng', status:'SUBMITTED', deadline:'2026-10-02', category_id:2, author_user_id:1},
    {topic_id:1002, topic_code:null, title:'Chuyển đổi số trong trường nghề', angle:'Từ hồ sơ giấy đến dữ liệu mở', status:'REVISION_REQUESTED', deadline:'2026-10-05', category_id:4, author_user_id:5},
    {topic_id:1003, topic_code:'PS-2026-002', title:'Đường đi của một cuốn sách đến thư viện vùng cao', angle:'Theo chân hành trình sách đến điểm trường', status:'IN_PROGRESS', deadline:'2026-09-30', category_id:3, author_user_id:10},
    {topic_id:1004, topic_code:null, title:'Giải pháp chống ngập cục bộ sau mưa lớn', angle:'Dữ liệu, chuyên gia và câu chuyện người dân', status:'PENDING_EXECUTIVE_APPROVAL', deadline:'2026-10-07', category_id:1, author_user_id:1}
  ],
  Topic_Member: [
    {member_id:1, topic_id:1003, user_id:10, role_in_team:'LEAD_REPORTER'},
    {member_id:2, topic_id:1003, user_id:6, role_in_team:'CAMERAMAN'},
    {member_id:3, topic_id:1003, user_id:1, role_in_team:'ASSISTANT'}
  ],
  Topic_Estimate: [
    {estimate_id:1, topic_id:1003, cost_item:'Công tác phí', amount:2500000},
    {estimate_id:2, topic_id:1003, cost_item:'Lưu trú', amount:1800000},
    {estimate_id:3, topic_id:1004, cost_item:'Di chuyển', amount:1200000}
  ],
  Criteria_Config: [
    {criteria_id:1, criteria_name:'Tính thời sự', weight_percent:30},
    {criteria_id:2, criteria_name:'Giá trị công chúng', weight_percent:30},
    {criteria_id:3, criteria_name:'Tính khả thi', weight_percent:20},
    {criteria_id:4, criteria_name:'Tính độc đáo', weight_percent:20}
  ],
  Topic_Evaluation: [
    {evaluation_id:1, topic_id:1002, evaluator_user_id:2, evaluation_stage:'DESK_REVIEW', decision:'REVISION_REQUESTED', comments:'Bổ sung số liệu và nguồn chuyên gia.'},
    {evaluation_id:2, topic_id:1003, evaluator_user_id:2, evaluation_stage:'DESK_REVIEW', decision:'APPROVED', comments:'Đạt vòng phòng ban.'}
  ],
  Evaluation_Criteria_Score: [
    {evaluation_id:1, criteria_id:1, score:8},
    {evaluation_id:1, criteria_id:2, score:8},
    {evaluation_id:1, criteria_id:3, score:6},
    {evaluation_id:1, criteria_id:4, score:7},
    {evaluation_id:2, criteria_id:1, score:9},
    {evaluation_id:2, criteria_id:2, score:9},
    {evaluation_id:2, criteria_id:3, score:8},
    {evaluation_id:2, criteria_id:4, score:8}
  ],
  Asset: [
    {asset_id:1, asset_name:'Sony FX3 + Mic không dây', asset_status:'AVAILABLE'},
    {asset_id:2, asset_name:'Tripod + LED Light', asset_status:'AVAILABLE'},
    {asset_id:3, asset_name:'Máy ảnh Canon R6', asset_status:'CHECKED_OUT'},
    {asset_id:4, asset_name:'Xe 7 chỗ 29A-123.45', asset_status:'AVAILABLE'}
  ],
  Asset_Request: [
    {request_id:501, topic_id:1003, asset_id:1, requester_user_id:10, start_time:'2026-09-28T08:00:00', end_time:'2026-09-29T20:00:00', status:'PENDING'},
    {request_id:502, topic_id:1003, asset_id:4, requester_user_id:10, start_time:'2026-09-29T06:00:00', end_time:'2026-09-29T20:00:00', status:'APPROVED'}
  ],
  Fieldwork_Task: [
    {task_id:701, topic_id:1003, assignee_user_id:10, task_status:'IN_PROGRESS', description:'Phỏng vấn thủ thư và học sinh'},
    {task_id:702, topic_id:1003, assignee_user_id:6, task_status:'TODO', description:'Quay b-roll tại điểm trường'},
    {task_id:703, topic_id:1004, assignee_user_id:1, task_status:'TODO', description:'Xin số liệu ngập cục bộ'},
    {task_id:704, topic_id:1002, assignee_user_id:5, task_status:'DONE', description:'Bổ sung danh sách trường khảo sát'}
  ],
  Notification: [
    {notification_id:9001, user_id:6, topic_id:1003, message:'Asset request #501 đang chờ xử lý.', is_read:0},
    {notification_id:9002, user_id:1, topic_id:1002, message:'Đề tài #1002 đã được yêu cầu chỉnh sửa.', is_read:0},
    {notification_id:9003, user_id:4, topic_id:1004, message:'Đề tài #1004 đang chờ phê duyệt cấp Tổng biên tập.', is_read:0},
    {notification_id:9004, user_id:10, topic_id:1003, message:'Yêu cầu xe #502 đã được phê duyệt.', is_read:1}
  ],
  Desk_Quota: [
    {quota_id:1, desk_id:1, period:'2026-09', quota_count:20},
    {quota_id:2, desk_id:2, period:'2026-09', quota_count:12},
    {quota_id:3, desk_id:3, period:'2026-09', quota_count:15}
  ],
  Request_Deadline_Extension: [
    {request_id:3001, topic_id:1002, requested_by_user_id:5, current_deadline:'2026-10-05', requested_deadline:'2026-10-10', reason:'Cần thêm thời gian phỏng vấn các trường.', status:'PENDING'}
  ]
};

window.NTMCS = {
  roles: {
    REPORTER:{label:'Phóng viên',short:'PV'},
    DESK_EDITOR:{label:'Biên tập phòng / Trưởng phòng',short:'BP'},
    EXECUTIVE_EDITOR:{label:'Tổng biên tập',short:'TBT'},
    LOGISTICS_COORDINATOR:{label:'Điều phối tài nguyên',short:'ĐP'},
    BUSINESS_ADMINISTRATOR:{label:'Quản trị nghiệp vụ',short:'BA'},
    SYSTEM_ADMINISTRATOR:{label:'Quản trị hệ thống',short:'SA'},
    FIELDWORK_COORDINATOR:{label:'Điều phối tác nghiệp',short:'FW'}
  },
  teamRoles:{LEAD_REPORTER:'Phóng viên chính',PHOTO_REPORTER:'Phóng viên ảnh',CAMERAMAN:'Quay phim',ASSISTANT:'Trợ lý'},
  statuses:{
    DRAFT:'Bản nháp',SUBMITTED:'Đã gửi',REVISION_REQUESTED:'Yêu cầu chỉnh sửa',PENDING_EXECUTIVE_APPROVAL:'Chờ TBT duyệt',APPROVED:'Đã duyệt',IN_PROGRESS:'Đang thực hiện',DELIVERED:'Đã bàn giao',ACCEPTED_READY_FOR_CMS:'Sẵn sàng CMS',REJECTED:'Từ chối',ABORTED:'Đã hủy',
PENDING:'Chờ xử lý',REJECTED:'Từ chối',CHECKED_OUT:'Đang mượn',RETURNED:'Đã trả',AVAILABLE:'Sẵn sàng',MAINTENANCE:'Bảo trì',TODO:'Chưa làm',DONE:'Hoàn thành',
    DESK_REVIEW:'Duyệt phòng',EXECUTIVE_APPROVAL:'TBT duyệt'
  }
};
