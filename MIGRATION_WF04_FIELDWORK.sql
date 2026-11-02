/*
    NTMCS - SCR-4.3 Fieldwork Deliverable Migration
    Tác giả: Phân hệ WF-04 (Đạt) & Đồng dẫn dắt Kiến trúc NTMCS
    Mục đích: Bổ sung bảng lưu trữ sản phẩm tác nghiệp hiện trường (File / Link).
    Đặc tính: Tương thích 100% với code Java DAO của WF-04, Idempotent, An toàn tuyệt đối.
*/

USE [NTMCS];
GO

SET NOCOUNT ON;
GO

-- 1. Kiểm tra môi trường an toàn (Safety Guards)
IF DB_NAME() <> N'NTMCS'
BEGIN
    THROW 50001, 'Sai Database. Migration bắt buộc phải chạy trên NTMCS.', 1;
END;
GO

IF OBJECT_ID(N'dbo.Fieldwork_Task', N'U') IS NULL
BEGIN
    THROW 50002, 'Bảng cha dbo.Fieldwork_Task chưa tồn tại. Dừng migration.', 1;
END;
GO

IF OBJECT_ID(N'dbo.[User]', N'U') IS NULL
BEGIN
    THROW 50003, 'Bảng cha dbo.[User] chưa tồn tại. Dừng migration.', 1;
END;
GO

-- 2. Tạo bảng Fieldwork_Deliverable (Giữ nguyên 100% cấu trúc 11 cột gốc của Đạt)
IF OBJECT_ID(N'dbo.Fieldwork_Deliverable', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Fieldwork_Deliverable
    (
        deliverable_id       INT IDENTITY(1,1) NOT NULL,
        task_id              INT NOT NULL,
        submitted_by_user_id INT NOT NULL,

        submission_type      NVARCHAR(10) NOT NULL,
        deliverable_note     NVARCHAR(MAX) NULL,

        -- FILE submission metadata (File vật lý lưu trên server / storage)
        original_file_name   NVARCHAR(255) NULL,
        stored_file_path     NVARCHAR(500) NULL,
        content_type         NVARCHAR(100) NULL,
        file_size_bytes      BIGINT NULL,

        -- LINK submission target
        external_url         NVARCHAR(2048) NULL,

        submitted_at         DATETIME2(0) NOT NULL
                             CONSTRAINT df_fieldwork_deliverable_submitted_at
                             DEFAULT SYSDATETIME(),

        CONSTRAINT pk_fieldwork_deliverable
            PRIMARY KEY (deliverable_id),

        CONSTRAINT fk_deliverable_task
            FOREIGN KEY (task_id)
            REFERENCES dbo.Fieldwork_Task(task_id)
            ON DELETE CASCADE,

        CONSTRAINT fk_deliverable_submitter
            FOREIGN KEY (submitted_by_user_id)
            REFERENCES dbo.[User](user_id)
            ON DELETE NO ACTION,

        CONSTRAINT ck_deliverable_submission_type
            CHECK (submission_type IN (N'FILE', N'LINK')),

        -- Phân định rõ ràng giữa FILE và LINK (Tương thích hoàn toàn với DAO của Đạt)
        CONSTRAINT ck_deliverable_submission_payload
            CHECK
            (
                (
                    submission_type = N'FILE'
                    AND original_file_name IS NOT NULL
                    AND stored_file_path IS NOT NULL
                    AND external_url IS NULL
                )
                OR
                (
                    submission_type = N'LINK'
                    AND external_url IS NOT NULL
                    AND original_file_name IS NULL
                    AND stored_file_path IS NULL
                )
            ),

        CONSTRAINT ck_deliverable_file_size
            CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0)
    );
    PRINT N'Đã khởi tạo thành công bảng dbo.Fieldwork_Deliverable.';
END
ELSE
BEGIN
    PRINT N'Bảng dbo.Fieldwork_Deliverable đã tồn tại, bỏ qua tạo mới.';
END;
GO

-- 3. Index phục vụ truy vấn của WF-04 (Theo Task và sắp xếp theo thời gian nộp)
IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_fieldwork_deliverable_task_submitted_at'
      AND object_id = OBJECT_ID(N'dbo.Fieldwork_Deliverable')
)
BEGIN
    CREATE INDEX ix_fieldwork_deliverable_task_submitted_at
        ON dbo.Fieldwork_Deliverable(task_id, submitted_at DESC);
    PRINT N'Đã tạo Index ix_fieldwork_deliverable_task_submitted_at.';
END;
GO

-- 4. Tối ưu ngầm: Bổ sung Index cho Foreign Key submitted_by_user_id (Không ảnh hưởng Java code)
IF NOT EXISTS
(
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_fieldwork_deliverable_submitter'
      AND object_id = OBJECT_ID(N'dbo.Fieldwork_Deliverable')
)
BEGIN
    CREATE INDEX ix_fieldwork_deliverable_submitter
        ON dbo.Fieldwork_Deliverable(submitted_by_user_id);
    PRINT N'Đã tạo Index ix_fieldwork_deliverable_submitter.';
END;
GO

PRINT N'Hoàn tất migration SCR-4.3 cho WF-04 an toàn 100%.';
GO
