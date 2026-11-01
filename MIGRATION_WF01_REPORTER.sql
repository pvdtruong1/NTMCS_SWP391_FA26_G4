-- =========================================================================
-- MIGRATION SCRIPT: PHÂN HỆ PHÓNG VIÊN (WF-01) - NTMCS
-- Tác giả: Phạm Vũ Đình Trường & Nhóm 4 (SE2066)
-- Mô tả:
--   1. Tự động kiểm tra & bỏ qua nếu đã có (Idempotent execution).
--   2. Bổ sung an toàn 4 cột nghiệp vụ vào bảng Topic: summary, genre, priority, created_date.
--   3. Xóa bỏ an toàn cột thừa [rejection_reason] (cùng ràng buộc mặc định nếu có) để đồng bộ với DB chuẩn.
--   4. Đảm bảo vai trò REPORTER sở hữu quyền TOPIC_PITCH.
-- =========================================================================

USE NTMCS;
GO

-- 1. Bổ sung 4 cột nghiệp vụ cho phân hệ Phóng viên (Bỏ qua nếu đã tồn tại)
IF COL_LENGTH('Topic', 'summary') IS NULL
BEGIN
    ALTER TABLE Topic ADD [summary] NVARCHAR(MAX) NULL;
    PRINT 'Đã bổ sung cột [summary] vào bảng Topic.';
END
ELSE
BEGIN
    PRINT 'Cột [summary] đã tồn tại (bỏ qua).';
END
GO

IF COL_LENGTH('Topic', 'genre') IS NULL
BEGIN
    ALTER TABLE Topic ADD [genre] NVARCHAR(100) NULL;
    PRINT 'Đã bổ sung cột [genre] vào bảng Topic.';
END
ELSE
BEGIN
    PRINT 'Cột [genre] đã tồn tại (bỏ qua).';
END
GO

IF COL_LENGTH('Topic', 'priority') IS NULL
BEGIN
    ALTER TABLE Topic ADD [priority] NVARCHAR(20) NOT NULL CONSTRAINT DF_Topic_priority DEFAULT 'NORMAL';
    PRINT 'Đã bổ sung cột [priority] vào bảng Topic (mặc định NORMAL).';
END
ELSE
BEGIN
    PRINT 'Cột [priority] đã tồn tại (bỏ qua).';
END
GO

IF COL_LENGTH('Topic', 'created_date') IS NULL
BEGIN
    ALTER TABLE Topic ADD [created_date] DATETIME2 NOT NULL CONSTRAINT DF_Topic_created_date DEFAULT SYSDATETIME();
    PRINT 'Đã bổ sung cột [created_date] vào bảng Topic (mặc định SYSDATETIME).';
END
ELSE
BEGIN
    PRINT 'Cột [created_date] đã tồn tại (bỏ qua).';
END
GO

-- 2. Xóa bỏ an toàn cột thừa [rejection_reason] nếu có (Dành cho DB của Hưng hoặc các máy đã tự ý thêm cột này)
IF COL_LENGTH('Topic', 'rejection_reason') IS NOT NULL
BEGIN
    -- Tìm và xóa mọi constraint (DEFAULT constraint) gắn với cột rejection_reason nếu có
    DECLARE @ConstraintName NVARCHAR(200);
    SELECT @ConstraintName = d.name
    FROM sys.default_constraints d
    JOIN sys.columns c ON d.parent_column_id = c.column_id AND d.parent_object_id = c.object_id
    WHERE d.parent_object_id = OBJECT_ID('Topic') AND c.name = 'rejection_reason';

    IF @ConstraintName IS NOT NULL
    BEGIN
        EXEC('ALTER TABLE Topic DROP CONSTRAINT ' + @ConstraintName);
        PRINT 'Đã xóa ràng buộc mặc định ' + @ConstraintName + ' trên cột rejection_reason.';
    END

    ALTER TABLE Topic DROP COLUMN [rejection_reason];
    PRINT 'Đã xóa an toàn cột thừa [rejection_reason] khỏi bảng Topic (Lý do từ chối được lấy chuẩn qua bảng Topic_Evaluation).';
END
ELSE
BEGIN
    PRINT 'Cột [rejection_reason] không tồn tại trong bảng Topic (bỏ qua).';
END
GO

-- 3. Đảm bảo Role REPORTER sở hữu Permission TOPIC_PITCH (Bỏ qua nếu đã có)
IF NOT EXISTS (
    SELECT 1 FROM Role_Permission rp
    JOIN Role r ON rp.role_id = r.role_id
    JOIN Permission p ON rp.permission_id = p.permission_id
    WHERE r.role_name = 'REPORTER' AND p.permission_code = 'TOPIC_PITCH'
)
BEGIN
    INSERT INTO Role_Permission (role_id, permission_id)
    SELECT r.role_id, p.permission_id
    FROM Role r, Permission p
    WHERE r.role_name = 'REPORTER' AND p.permission_code = 'TOPIC_PITCH';
    PRINT 'Đã cấp quyền TOPIC_PITCH cho vai trò REPORTER.';
END
ELSE
BEGIN
    PRINT 'Vai trò REPORTER đã có quyền TOPIC_PITCH (bỏ qua).';
END
GO
