-- Migration script for CHAT_LIEU table and ChatLieuId foreign key in SAN_PHAM

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHAT_LIEU')
BEGIN
    CREATE TABLE dbo.CHAT_LIEU (
        Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        TenChatLieu NVARCHAR(100) NOT NULL UNIQUE,
        TrangThai NVARCHAR(20) DEFAULT 'ACTIVE'
    );

    INSERT INTO dbo.CHAT_LIEU (TenChatLieu, TrangThai) VALUES 
    (N'Cotton', 'ACTIVE'),
    (N'Polyester', 'ACTIVE'),
    (N'Kaki', 'ACTIVE'),
    (N'Jean', 'ACTIVE'),
    (N'Linen', 'ACTIVE');
END
GO

IF COL_LENGTH('dbo.SAN_PHAM', 'ChatLieuId') IS NULL
BEGIN
    ALTER TABLE dbo.SAN_PHAM
    ADD ChatLieuId INT NULL;
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_SAN_PHAM_CHAT_LIEU')
BEGIN
    ALTER TABLE dbo.SAN_PHAM
    ADD CONSTRAINT FK_SAN_PHAM_CHAT_LIEU
    FOREIGN KEY (ChatLieuId) REFERENCES dbo.CHAT_LIEU(Id);
END
GO

-- Populate any existing text materials if missing from CHAT_LIEU
IF NOT EXISTS (SELECT 1 FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Nỉ')
    INSERT INTO dbo.CHAT_LIEU (TenChatLieu, TrangThai) VALUES (N'Nỉ', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Denim')
    INSERT INTO dbo.CHAT_LIEU (TenChatLieu, TrangThai) VALUES (N'Denim', 'ACTIVE');

IF NOT EXISTS (SELECT 1 FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Vải')
    INSERT INTO dbo.CHAT_LIEU (TenChatLieu, TrangThai) VALUES (N'Vải', 'ACTIVE');
GO

-- Map existing records in SAN_PHAM
UPDATE dbo.SAN_PHAM SET ChatLieuId = (SELECT TOP 1 Id FROM dbo.CHAT_LIEU WHERE TenChatLieu = 'Polyester') WHERE ChatLieu LIKE '%Polyester%' AND ChatLieuId IS NULL;
UPDATE dbo.SAN_PHAM SET ChatLieuId = (SELECT TOP 1 Id FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Nỉ') WHERE ChatLieu LIKE 'Ni%' AND ChatLieuId IS NULL;
UPDATE dbo.SAN_PHAM SET ChatLieuId = (SELECT TOP 1 Id FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Denim') WHERE ChatLieu LIKE 'Denim%' AND ChatLieuId IS NULL;
UPDATE dbo.SAN_PHAM SET ChatLieuId = (SELECT TOP 1 Id FROM dbo.CHAT_LIEU WHERE TenChatLieu = N'Vải') WHERE ChatLieu LIKE 'V%' AND ChatLieuId IS NULL;
GO

