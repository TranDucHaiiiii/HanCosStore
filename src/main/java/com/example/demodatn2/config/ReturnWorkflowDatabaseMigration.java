package com.example.demodatn2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnWorkflowDatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        execute("""
            IF COL_LENGTH('dbo.BIEN_THE_SAN_PHAM', 'SoLuongLoi') IS NULL
                ALTER TABLE dbo.BIEN_THE_SAN_PHAM ADD SoLuongLoi INT NOT NULL CONSTRAINT DF_BTSP_SoLuongLoi DEFAULT (0)
            """);

        execute("""
            IF COL_LENGTH('dbo.YEU_CAU_DOI_TRA', 'AnhMinhChung') IS NULL
                ALTER TABLE dbo.YEU_CAU_DOI_TRA ADD AnhMinhChung NVARCHAR(255) NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.YEU_CAU_DOI_TRA', 'MoTaChiTiet') IS NULL
                ALTER TABLE dbo.YEU_CAU_DOI_TRA ADD MoTaChiTiet NVARCHAR(1000) NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.YEU_CAU_DOI_TRA', 'PhuongThucHoanTien') IS NULL
                ALTER TABLE dbo.YEU_CAU_DOI_TRA ADD PhuongThucHoanTien NVARCHAR(50) NOT NULL CONSTRAINT DF_YCDT_PhuongThucHoanTien DEFAULT (N'BANK_TRANSFER')
            """);

        execute("""
            IF COL_LENGTH('dbo.YEU_CAU_DOI_TRA', 'GhiChuXuLy') IS NULL
                ALTER TABLE dbo.YEU_CAU_DOI_TRA ADD GhiChuXuLy NVARCHAR(1000) NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.YEU_CAU_DOI_TRA', 'NgayCapNhat') IS NULL
                ALTER TABLE dbo.YEU_CAU_DOI_TRA ADD NgayCapNhat DATETIME2 NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.CHI_TIET_DOI_TRA', 'YeuCauDoiTraId') IS NULL
                ALTER TABLE dbo.CHI_TIET_DOI_TRA ADD YeuCauDoiTraId INT NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.CHI_TIET_DOI_TRA', 'ChiTietDonHangId') IS NULL
                ALTER TABLE dbo.CHI_TIET_DOI_TRA ADD ChiTietDonHangId INT NULL
            """);

        execute("""
            IF COL_LENGTH('dbo.CHI_TIET_DOI_TRA', 'InspectionStatus') IS NULL
                ALTER TABLE dbo.CHI_TIET_DOI_TRA ADD InspectionStatus NVARCHAR(30) NOT NULL CONSTRAINT DF_CTDT_InspectionStatus DEFAULT (N'PENDING_INSPECTION')
            """);

        execute("""
            IF OBJECT_ID('dbo.HINH_ANH_DOI_TRA', 'U') IS NULL
                CREATE TABLE dbo.HINH_ANH_DOI_TRA (
                    Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    YeuCauDoiTraId INT NOT NULL,
                    DuongDanAnh NVARCHAR(500) NOT NULL,
                    NgayTao DATETIME2 NOT NULL CONSTRAINT DF_HADT_NgayTao DEFAULT (SYSDATETIME())
                )
            """);

        execute("""
            IF OBJECT_ID('dbo.LICH_SU_XU_LY_DOI_TRA', 'U') IS NULL
                CREATE TABLE dbo.LICH_SU_XU_LY_DOI_TRA (
                    Id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    YeuCauDoiTraId INT NOT NULL,
                    NguoiXuLyId INT NULL,
                    HanhDong NVARCHAR(80) NOT NULL,
                    GhiChu NVARCHAR(1000) NULL,
                    ThoiGian DATETIME2 NOT NULL CONSTRAINT DF_LSXLDT_ThoiGian DEFAULT (SYSDATETIME())
                )
            """);

        execute("""
            IF OBJECT_ID('dbo.KHO_HANG_HOAN', 'U') IS NULL
                CREATE TABLE dbo.KHO_HANG_HOAN (
                    Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    SanPhamChiTietId INT NOT NULL,
                    YeuCauDoiTraId INT NULL,
                    ChiTietDoiTraId INT NULL,
                    SoLuong INT NOT NULL,
                    TinhTrang NVARCHAR(50) NULL,
                    HuongXuLy NVARCHAR(50) NULL,
                    TrangThai NVARCHAR(50) NULL,
                    GhiChu NVARCHAR(500) NULL,
                    NgayNhap DATETIME2 NOT NULL CONSTRAINT DF_KHH_NgayNhap DEFAULT (SYSDATETIME())
                )
            """);

        execute("""
            IF OBJECT_ID('dbo.REFUND_TRANSACTION', 'U') IS NULL
                CREATE TABLE dbo.REFUND_TRANSACTION (
                    Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                    YeuCauDoiTraId INT NOT NULL,
                    SoTienHoan DECIMAL(18,2) NOT NULL,
                    PhuongThucHoanTien NVARCHAR(50) NOT NULL,
                    MaGiaoDich NVARCHAR(100) NULL,
                    NguoiXuLyId INT NULL,
                    ThoiGianHoan DATETIME2 NOT NULL CONSTRAINT DF_REFUND_TRANSACTION_ThoiGianHoan DEFAULT (SYSDATETIME()),
                    GhiChu NVARCHAR(500) NULL
                )
            """);

        execute("""
            IF OBJECT_ID('dbo.KHO_HANG_HOAN', 'U') IS NOT NULL
               AND COL_LENGTH('dbo.KHO_HANG_HOAN', 'ChiTietDoiTraId') IS NULL
                ALTER TABLE dbo.KHO_HANG_HOAN ADD ChiTietDoiTraId INT NULL
            """);

        execute("""
            IF OBJECT_ID('dbo.HINH_ANH_DOI_TRA', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_HADT_YeuCauDoiTraId' AND object_id = OBJECT_ID('dbo.HINH_ANH_DOI_TRA'))
                CREATE INDEX IX_HADT_YeuCauDoiTraId ON dbo.HINH_ANH_DOI_TRA(YeuCauDoiTraId, Id)
            """);

        execute("""
            IF OBJECT_ID('dbo.LICH_SU_XU_LY_DOI_TRA', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_LSXLDT_YeuCauDoiTraId_ThoiGian' AND object_id = OBJECT_ID('dbo.LICH_SU_XU_LY_DOI_TRA'))
                CREATE INDEX IX_LSXLDT_YeuCauDoiTraId_ThoiGian ON dbo.LICH_SU_XU_LY_DOI_TRA(YeuCauDoiTraId, ThoiGian)
            """);

        execute("""
            IF OBJECT_ID('dbo.CHI_TIET_DOI_TRA', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_CTDT_YeuCauDoiTraId' AND object_id = OBJECT_ID('dbo.CHI_TIET_DOI_TRA'))
                CREATE INDEX IX_CTDT_YeuCauDoiTraId ON dbo.CHI_TIET_DOI_TRA(YeuCauDoiTraId)
            """);

        execute("""
            IF OBJECT_ID('dbo.KHO_HANG_HOAN', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_KHH_TrangThai_NgayNhap' AND object_id = OBJECT_ID('dbo.KHO_HANG_HOAN'))
                CREATE INDEX IX_KHH_TrangThai_NgayNhap ON dbo.KHO_HANG_HOAN(TrangThai, NgayNhap DESC)
            """);

        execute("""
            IF OBJECT_ID('dbo.KHO_HANG_HOAN', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_KHH_ChiTietDoiTraId' AND object_id = OBJECT_ID('dbo.KHO_HANG_HOAN'))
                CREATE UNIQUE INDEX UX_KHH_ChiTietDoiTraId ON dbo.KHO_HANG_HOAN(ChiTietDoiTraId) WHERE ChiTietDoiTraId IS NOT NULL
            """);

        execute("""
            IF OBJECT_ID('dbo.REFUND_TRANSACTION', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_REFUND_TRANSACTION_YeuCauDoiTraId' AND object_id = OBJECT_ID('dbo.REFUND_TRANSACTION'))
                CREATE INDEX IX_REFUND_TRANSACTION_YeuCauDoiTraId ON dbo.REFUND_TRANSACTION(YeuCauDoiTraId, ThoiGianHoan DESC)
            """);

        execute("""
            IF OBJECT_ID('dbo.YEU_CAU_DOI_TRA', 'U') IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_YCDT_DonHangId' AND object_id = OBJECT_ID('dbo.YEU_CAU_DOI_TRA'))
               AND NOT EXISTS (
                   SELECT DonHangId
                   FROM dbo.YEU_CAU_DOI_TRA
                   WHERE DonHangId IS NOT NULL
                   GROUP BY DonHangId
                   HAVING COUNT(*) > 1
               )
                CREATE UNIQUE INDEX UX_YCDT_DonHangId ON dbo.YEU_CAU_DOI_TRA(DonHangId)
            """);
    }

    private void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}
