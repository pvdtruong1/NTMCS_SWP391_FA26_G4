# NTMCS Frontend — HTML/CSS/JS

Đây là frontend prototype chạy độc lập, không cần NetBeans, SQL Server, Node hay backend.

## 1. Cách chạy

Mở `index.html` trực tiếp bằng Chrome/Edge, hoặc mở thư mục bằng Visual Studio và chạy file HTML. Không có dependency ngoài.

## 2. Database contract

`js/data.js` bám theo schema SQL Server người dùng cung cấp, gồm đúng 14 bảng:

- Desk
- Category
- User
- Topic
- Topic_Member
- Topic_Estimate
- Criteria_Config
- Topic_Evaluation
- Evaluation_Criteria_Score
- Asset
- Asset_Request
- Fieldwork_Task
- Notification
- Desk_Quota
- Request_Deadline_Extension

Lưu ý: danh sách trên thực tế là **15 bảng** (vì `Desk`, `Category`, `User` + 12 bảng còn lại). Source SQL được dùng làm nguồn chuẩn; frontend không tự thêm bảng nghiệp vụ mới.

## 3. Quick account switch

Trang `Account / chuyển tài khoản` dùng trực tiếp record của `User`, chuyển actor tức thì bằng `localStorage`, không cần login/logout.

Database chỉ cho phép 7 giá trị `User.role`, không có `SECRETARY`. Vì vậy account `Thu Hà` được seed với role `DESK_EDITOR` để mô phỏng người xử lý trung gian mà không phá CHECK constraint của DB.

## 4. Workflow demo

Topic:

`DRAFT → SUBMITTED → PENDING_EXECUTIVE_APPROVAL → APPROVED → IN_PROGRESS → DELIVERED → ACCEPTED_READY_FOR_CMS`

Nhánh chỉnh sửa/từ chối:

`REVISION_REQUESTED`, `REJECTED`, `ABORTED`.

Asset:

`PENDING → APPROVED/REJECTED → CHECKED_OUT → RETURNED`.

Fieldwork task:

`TODO → IN_PROGRESS → DONE`.

Deadline extension:

`PENDING → APPROVED/REJECTED`.

## 5. Persistence

Mọi thao tác demo được lưu vào browser `localStorage`. Vì vậy có thể chuyển account, thao tác request/approval/task rồi reload trang mà dữ liệu vẫn còn.

Nút `Reset về seed DB` đưa frontend về dữ liệu ban đầu.

## 6. Quan trọng về scope

Schema SQL hiện tại **không có bảng Draft/Article/Review Version/CMS**. Vì vậy frontend không giả tạo một module Draft database. Điểm kết thúc đúng theo schema là `Topic.ACCEPTED_READY_FOR_CMS`, tức sẵn sàng bàn giao cho CMS/publishing ở hệ thống khác.
