-- =========================================================================
-- MIGRATION SCRIPT: PHÂN HỆ PHÓNG VIÊN (WF-01) - NTMCS
-- Tác giả: Nhóm 4 - SE2066 (SWP391)
-- Mô tả: Bổ sung an toàn 4 cột nghiệp vụ vào bảng Topic và cấp quyền TOPIC_PITCH
-- =========================================================================

USE NTMCS;
GO

-- 1. Bổ sung an toàn 4 cột vào bảng Topic (nếu chưa có)
IF COL_LENGTH('Topic', 'summary') IS NULL
BEGIN
    ALTER TABLE Topic ADD [summary] NVARCHAR(MAX) NULL;
    PRINT 'Đã bổ sung cột [summary] vào bảng Topic.';
END
ELSE
BEGIN
    PRINT 'Cột [summary] đã tồn tại.';
END
GO

IF COL_LENGTH('Topic', 'genre') IS NULL
BEGIN
    ALTER TABLE Topic ADD [genre] NVARCHAR(100) NULL;
    PRINT 'Đã bổ sung cột [genre] vào bảng Topic.';
END
ELSE
BEGIN
    PRINT 'Cột [genre] đã tồn tại.';
END
GO

IF COL_LENGTH('Topic', 'priority') IS NULL
BEGIN
    ALTER TABLE Topic ADD [priority] NVARCHAR(20) NOT NULL CONSTRAINT DF_Topic_priority DEFAULT 'NORMAL';
    PRINT 'Đã bổ sung cột [priority] vào bảng Topic (mặc định NORMAL).';
END
ELSE
BEGIN
    PRINT 'Cột [priority] đã tồn tại.';
END
GO

IF COL_LENGTH('Topic', 'created_date') IS NULL
BEGIN
    ALTER TABLE Topic ADD [created_date] DATETIME2 NOT NULL CONSTRAINT DF_Topic_created_date DEFAULT SYSDATETIME();
    PRINT 'Đã bổ sung cột [created_date] vào bảng Topic (mặc định SYSDATETIME).';
END
ELSE
BEGIN
    PRINT 'Cột [created_date] đã tồn tại.';
END
GO

-- 2. Đảm bảo Role REPORTER sở hữu Permission TOPIC_PITCH
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
    PRINT 'Vai trò REPORTER đã có quyền TOPIC_PITCH.';
END
GO
