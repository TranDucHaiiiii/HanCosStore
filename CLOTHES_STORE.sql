USE [master]
GO
/****** Object:  Database [CLOTHES_STORE]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE DATABASE [CLOTHES_STORE]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'CLOTHES_STORE', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER01\MSSQL\DATA\CLOTHES_STORE.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'CLOTHES_STORE_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER01\MSSQL\DATA\CLOTHES_STORE_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT
GO
ALTER DATABASE [CLOTHES_STORE] SET COMPATIBILITY_LEVEL = 150
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [CLOTHES_STORE].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [CLOTHES_STORE] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET ARITHABORT OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [CLOTHES_STORE] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [CLOTHES_STORE] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET  ENABLE_BROKER 
GO
ALTER DATABASE [CLOTHES_STORE] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [CLOTHES_STORE] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET RECOVERY FULL 
GO
ALTER DATABASE [CLOTHES_STORE] SET  MULTI_USER 
GO
ALTER DATABASE [CLOTHES_STORE] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [CLOTHES_STORE] SET DB_CHAINING OFF 
GO
ALTER DATABASE [CLOTHES_STORE] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [CLOTHES_STORE] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [CLOTHES_STORE] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [CLOTHES_STORE] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'CLOTHES_STORE', N'ON'
GO
ALTER DATABASE [CLOTHES_STORE] SET QUERY_STORE = OFF
GO
USE [CLOTHES_STORE]
GO
/****** Object:  Table [dbo].[BIEN_THE_SAN_PHAM]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[BIEN_THE_SAN_PHAM](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[SanPhamId] [int] NOT NULL,
	[MaSKU] [nvarchar](80) NOT NULL,
	[MauSac] [nvarchar](50) NOT NULL,
	[KichCo] [nvarchar](20) NOT NULL,
	[Gia] [decimal](18, 2) NOT NULL,
	[GiaGoc] [decimal](18, 2) NULL,
	[SoLuongTon] [int] NOT NULL,
	[KhoiLuongGram] [int] NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CHI_TIET_DOI_TRA]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CHI_TIET_DOI_TRA](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[YeuCauDoiTraId] [int] NOT NULL,
	[ChiTietDonHangId] [int] NOT NULL,
	[SoLuong] [int] NOT NULL,
	[GhiChu] [nvarchar](300) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CHI_TIET_DON_HANG]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CHI_TIET_DON_HANG](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[DonHangId] [int] NOT NULL,
	[BienTheSanPhamId] [int] NOT NULL,
	[TenSanPham] [nvarchar](200) NOT NULL,
	[MauSac] [nvarchar](50) NOT NULL,
	[KichCo] [nvarchar](20) NOT NULL,
	[SoLuong] [int] NOT NULL,
	[DonGia] [decimal](18, 2) NOT NULL,
	[ThanhTien] [decimal](18, 2) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CHI_TIET_GIO_HANG]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CHI_TIET_GIO_HANG](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[GioHangId] [int] NOT NULL,
	[BienTheSanPhamId] [int] NOT NULL,
	[SoLuong] [int] NOT NULL,
	[DonGia] [decimal](18, 2) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DANH_GIA_SAN_PHAM]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DANH_GIA_SAN_PHAM](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[SanPhamId] [int] NOT NULL,
	[TaiKhoanId] [int] NOT NULL,
	[SoSao] [int] NOT NULL,
	[NoiDung] [nvarchar](1000) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DANH_MUC]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DANH_MUC](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Ma] [nvarchar](50) NOT NULL,
	[Ten] [nvarchar](150) NOT NULL,
	[DanhMucChaId] [int] NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DIA_CHI_GIAO_HANG]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DIA_CHI_GIAO_HANG](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[TaiKhoanId] [int] NOT NULL,
	[HoTenNhan] [nvarchar](150) NOT NULL,
	[SoDienThoaiNhan] [nvarchar](20) NOT NULL,
	[TinhThanh] [nvarchar](100) NOT NULL,
	[QuanHuyen] [nvarchar](100) NOT NULL,
	[PhuongXa] [nvarchar](100) NOT NULL,
	[DiaChiChiTiet] [nvarchar](300) NOT NULL,
	[LaMacDinh] [bit] NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DON_HANG]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DON_HANG](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MaDonHang] [nvarchar](50) NOT NULL,
	[TaiKhoanId] [int] NULL,
	[HoTenNhan] [nvarchar](150) NOT NULL,
	[SoDienThoaiNhan] [nvarchar](20) NOT NULL,
	[EmailNhan] [nvarchar](150) NULL,
	[DiaChiNhan] [nvarchar](500) NOT NULL,
	[GhiChu] [nvarchar](500) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[TamTinh] [decimal](18, 2) NOT NULL,
	[GiamGia] [decimal](18, 2) NOT NULL,
	[PhiVanChuyen] [decimal](18, 2) NOT NULL,
	[TongTien] [decimal](18, 2) NOT NULL,
	[MaGiamGiaId] [int] NULL,
	[NgayDat] [datetime2](7) NOT NULL,
	[NgayCapNhat] [datetime2](7) NULL,
	[PhuongThucThanhToan] [nvarchar](50) NULL,
	[LyDoHuy] [nvarchar](255) NULL,
	[LyDoLoiVanChuyen] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[GIAO_DICH_THANH_TOAN]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GIAO_DICH_THANH_TOAN](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[DonHangId] [int] NOT NULL,
	[PhuongThucThanhToanId] [int] NOT NULL,
	[SoTien] [decimal](18, 2) NOT NULL,
	[NhaCungCap] [nvarchar](50) NULL,
	[MaGiaoDich] [nvarchar](100) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[ThoiGianThanhToan] [datetime2](7) NULL,
	[DuLieuRaw] [nvarchar](max) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[GIAO_DICH_TON_KHO]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GIAO_DICH_TON_KHO](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[BienTheSanPhamId] [int] NOT NULL,
	[Loai] [nvarchar](20) NOT NULL,
	[SoLuong] [int] NOT NULL,
	[ThamChieuLoai] [nvarchar](30) NULL,
	[ThamChieuId] [int] NULL,
	[GhiChu] [nvarchar](300) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[GIO_HANG]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GIO_HANG](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[TaiKhoanId] [int] NULL,
	[SessionId] [nvarchar](100) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
	[NgayCapNhat] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HINH_ANH_MAU_SAC]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HINH_ANH_MAU_SAC](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[SanPhamId] [int] NOT NULL,
	[MauSac] [nvarchar](50) NOT NULL,
	[DuongDanAnh] [nvarchar](500) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HINH_ANH_SAN_PHAM]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HINH_ANH_SAN_PHAM](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[SanPhamId] [int] NOT NULL,
	[DuongDanAnh] [nvarchar](500) NOT NULL,
	[LaAnhChinh] [bit] NOT NULL,
	[ThuTu] [int] NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LICH_SU_NHAP_KHO]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LICH_SU_NHAP_KHO](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[BienTheSanPhamId] [int] NOT NULL,
	[SoLuongNhap] [int] NOT NULL,
	[ThoiGian] [datetime2](7) NOT NULL,
	[AdminThucHien] [nvarchar](100) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MaGiamGiaId] [int] NOT NULL,
	[TaiKhoanId] [int] NULL,
	[DonHangId] [int] NOT NULL,
	[ThoiGianSuDung] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[MA_GIAM_GIA]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[MA_GIAM_GIA](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Ma] [nvarchar](50) NOT NULL,
	[Loai] [nvarchar](20) NOT NULL,
	[GiaTri] [decimal](18, 2) NOT NULL,
	[GiaTriToiDa] [decimal](18, 2) NULL,
	[DonToiThieu] [decimal](18, 2) NULL,
	[SoLuongToiDa] [int] NULL,
	[SoLuongDaDung] [int] NOT NULL,
	[BatDauLuc] [datetime2](7) NOT NULL,
	[KetThucLuc] [datetime2](7) NOT NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PHUONG_THUC_THANH_TOAN]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PHUONG_THUC_THANH_TOAN](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Ma] [nvarchar](30) NOT NULL,
	[Ten] [nvarchar](100) NOT NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[RESET_PASSWORD_TOKEN]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[RESET_PASSWORD_TOKEN](
	[Id] [bigint] IDENTITY(1,1) NOT NULL,
	[Token] [nvarchar](64) NOT NULL,
	[TaiKhoanId] [int] NOT NULL,
	[ExpiresAt] [datetime2](7) NOT NULL,
	[Used] [bit] NOT NULL,
	[UsedAt] [datetime2](7) NULL,
	[CreatedAt] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SAN_PHAM]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SAN_PHAM](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MaSanPham] [nvarchar](50) NOT NULL,
	[Ten] [nvarchar](200) NOT NULL,
	[ThuongHieuId] [int] NULL,
	[DanhMucId] [int] NULL,
	[MoTaNgan] [nvarchar](500) NULL,
	[MoTa] [nvarchar](max) NULL,
	[ChatLieu] [nvarchar](100) NULL,
	[GioiTinh] [nvarchar](30) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[DaXoa] [bit] NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
	[NgayCapNhat] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TAI_KHOAN]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TAI_KHOAN](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[TenDangNhap] [nvarchar](50) NOT NULL,
	[MatKhauHash] [nvarchar](255) NOT NULL,
	[HoTen] [nvarchar](150) NOT NULL,
	[Email] [nvarchar](150) NULL,
	[SoDienThoai] [nvarchar](20) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
	[NgayCapNhat] [datetime2](7) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TAI_KHOAN_VAI_TRO]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TAI_KHOAN_VAI_TRO](
	[TaiKhoanId] [int] NOT NULL,
	[VaiTroId] [int] NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
 CONSTRAINT [PK_TAI_KHOAN_VAI_TRO] PRIMARY KEY CLUSTERED 
(
	[TaiKhoanId] ASC,
	[VaiTroId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[THUONG_HIEU]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[THUONG_HIEU](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Ma] [nvarchar](50) NULL,
	[Ten] [nvarchar](150) NOT NULL,
	[QuocGia] [nvarchar](100) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[VAI_TRO]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[VAI_TRO](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Ma] [nvarchar](30) NOT NULL,
	[Ten] [nvarchar](100) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[VAN_CHUYEN]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[VAN_CHUYEN](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[DonHangId] [int] NOT NULL,
	[DonViVanChuyen] [nvarchar](100) NOT NULL,
	[MaVanDon] [nvarchar](100) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayGui] [datetime2](7) NULL,
	[NgayGiao] [datetime2](7) NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[YEU_CAU_DOI_TRA]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[YEU_CAU_DOI_TRA](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[DonHangId] [int] NOT NULL,
	[TaiKhoanId] [int] NOT NULL,
	[LyDo] [nvarchar](500) NOT NULL,
	[AnhMinhChung] [nvarchar](255) NULL,
	[TrangThai] [nvarchar](30) NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[YEU_THICH_SAN_PHAM]    Script Date: 3/28/2026 2:42:00 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[YEU_THICH_SAN_PHAM](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[TaiKhoanId] [int] NOT NULL,
	[SanPhamId] [int] NOT NULL,
	[NgayTao] [datetime2](7) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[BIEN_THE_SAN_PHAM] ON 
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (1, 1, N'SP000001_DEN_M', N'Đen', N'M', CAST(129000.00 AS Decimal(18, 2)), NULL, 250, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4354711' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2, 1, N'SP000001_DEN_L', N'Đen', N'L', CAST(129000.00 AS Decimal(18, 2)), NULL, 130, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3, 1, N'SP000001_DEN_XL', N'Đen', N'XL', CAST(129000.00 AS Decimal(18, 2)), NULL, 132, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (4, 1, N'SP000001_DEN_XXL', N'Đen', N'XXL', CAST(129000.00 AS Decimal(18, 2)), NULL, 117, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (5, 1, N'SP000001_TRANG_M', N'Trắng', N'M', CAST(129000.00 AS Decimal(18, 2)), NULL, 248, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (6, 1, N'SP000001_TRANG_L', N'Trắng', N'L', CAST(129000.00 AS Decimal(18, 2)), NULL, 688, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7, 1, N'SP000001_TRANG_XL', N'Trắng', N'XL', CAST(129000.00 AS Decimal(18, 2)), NULL, 411, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4359846' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (8, 1, N'SP000001_TRANG_XXL', N'Trắng', N'XXL', CAST(129000.00 AS Decimal(18, 2)), NULL, 211, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4364913' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (13, 2, N'SP000002_XANH_DUONG_28', N'Xanh dương', N'28', CAST(299000.00 AS Decimal(18, 2)), NULL, 315, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4364913' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (14, 2, N'SP000002_XANH_DUONG_29', N'Xanh dương', N'29', CAST(299000.00 AS Decimal(18, 2)), NULL, 105, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4364913' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (15, 2, N'SP000002_XANH_DUONG_30', N'Xanh dương', N'30', CAST(299000.00 AS Decimal(18, 2)), NULL, 344, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4364913' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (16, 2, N'SP000002_XANH_DUONG_31', N'Xanh dương', N'31', CAST(299000.00 AS Decimal(18, 2)), NULL, 311, 300, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4364913' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (1002, 1, N'SP000001_NAU_M', N'Nâu', N'M', CAST(129000.00 AS Decimal(18, 2)), NULL, 222, 300, N'ACTIVE', CAST(N'2026-01-05T00:24:45.6428472' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (1003, 1, N'SP000001_NAU_L', N'Nâu', N'L', CAST(129000.00 AS Decimal(18, 2)), NULL, 26, 300, N'ACTIVE', CAST(N'2026-01-05T00:24:45.6428472' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (1004, 1, N'SP000001_NAU_XL', N'Nâu', N'XL', CAST(129000.00 AS Decimal(18, 2)), NULL, 49, 300, N'ACTIVE', CAST(N'2026-01-05T00:24:45.6428472' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (1005, 1, N'SP000001_NAU_XXL', N'Nâu', N'XXL', CAST(129000.00 AS Decimal(18, 2)), NULL, 111, 300, N'ACTIVE', CAST(N'2026-01-05T00:24:45.6428472' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2002, 1002, N'AO_PHAO_AKP_DEN_XS', N'Đen', N'XS', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 116, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5900000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2003, 1002, N'AO_PHAO_AKP_DEN_S', N'Đen', N'S', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 0, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5900000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2004, 1002, N'AO_PHAO_AKP_DEN_M', N'Đen', N'M', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 15, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5900000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2005, 1002, N'AO_PHAO_AKP_DEN_L', N'Đen', N'L', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 12, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5900000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2006, 1002, N'AO_PHAO_AKP_DEN_XL', N'Đen', N'XL', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 8, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5900000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2007, 1002, N'AO_PHAO_AKP_TRANG_XS', N'Trắng', N'XS', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 16, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5966667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2008, 1002, N'AO_PHAO_AKP_TRANG_S', N'Trắng', N'S', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 7, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5966667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2009, 1002, N'AO_PHAO_AKP_TRANG_M', N'Trắng', N'M', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 12, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5966667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2010, 1002, N'AO_PHAO_AKP_TRANG_L', N'Trắng', N'L', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 10, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5966667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2011, 1002, N'AO_PHAO_AKP_TRANG_XL', N'Trắng', N'XL', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 6, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5966667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2012, 1002, N'AO_PHAO_AKP_XAM_XS', N'Xám', N'XS', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 69, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.6000000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2013, 1002, N'AO_PHAO_AKP_XAM_S', N'Xám', N'S', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 7, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.6000000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2014, 1002, N'AO_PHAO_AKP_XAM_M', N'Xám', N'M', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 11, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.6000000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2015, 1002, N'AO_PHAO_AKP_XAM_L', N'Xám', N'L', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 9, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.6000000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (2016, 1002, N'AO_PHAO_AKP_XAM_XL', N'Xám', N'XL', CAST(645000.00 AS Decimal(18, 2)), CAST(750000.00 AS Decimal(18, 2)), 128, 300, N'ACTIVE', CAST(N'2026-01-05T18:56:50.6000000' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3002, 2002, N'SP13_NAU_L', N'Nâu', N'L', CAST(319000.00 AS Decimal(18, 2)), CAST(400000.00 AS Decimal(18, 2)), 121, 300, N'ACTIVE', CAST(N'2026-01-06T09:11:35.1111352' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3003, 2002, N'SP13_NAU_M', N'Nâu', N'M', CAST(319000.00 AS Decimal(18, 2)), CAST(400000.00 AS Decimal(18, 2)), 111, 300, N'ACTIVE', CAST(N'2026-01-06T09:13:16.4067982' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3004, 2003, N'GIAY_VAN_BEO_DEN_42', N'Đen', N'42', CAST(250000.00 AS Decimal(18, 2)), CAST(1500000.00 AS Decimal(18, 2)), 123, 300, N'ACTIVE', CAST(N'2026-01-06T09:30:13.8569448' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3005, 2003, N'GIAY_VAN_BEO_DEN_40', N'Đen', N'40', CAST(250000.00 AS Decimal(18, 2)), CAST(1500000.00 AS Decimal(18, 2)), 127, 300, N'ACTIVE', CAST(N'2026-01-06T09:31:24.6291767' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3006, 2002, N'SP13_DEN_L', N'Đen', N'L', CAST(319000.00 AS Decimal(18, 2)), NULL, 116, 300, N'ACTIVE', CAST(N'2026-01-06T12:02:14.0541112' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (3007, 2002, N'SP13_DEN_M', N'Đen', N'M', CAST(319000.00 AS Decimal(18, 2)), NULL, 111, 300, N'ACTIVE', CAST(N'2026-01-06T12:02:14.0601111' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (4002, 3002, N'SP14_XANH_DUONG_L', N'Xanh dương', N'L', CAST(250000.00 AS Decimal(18, 2)), NULL, 353, 300, N'ACTIVE', CAST(N'2026-01-09T12:58:28.3720441' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (4003, 3002, N'SP14_XANH_DUONG_S', N'Xanh dương', N'S', CAST(250000.00 AS Decimal(18, 2)), NULL, 210, 300, N'ACTIVE', CAST(N'2026-01-09T12:58:28.3770814' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (4005, 3002, N'SP14_XANH_DUONG_XXL', N'Xanh dương', N'XXL', CAST(250000.00 AS Decimal(18, 2)), NULL, 162, 300, N'ACTIVE', CAST(N'2026-01-09T13:15:36.0877344' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (5003, 2003, N'GIAY_VAN_BEO_DEN_41', N'Đen', N'41', CAST(250000.00 AS Decimal(18, 2)), NULL, 115, 300, N'ACTIVE', CAST(N'2026-01-19T08:22:28.5800195' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (6002, 4002, N'KL12_DEN_L', N'Đen', N'L', CAST(150000.00 AS Decimal(18, 2)), NULL, 99, 300, N'ACTIVE', CAST(N'2026-01-19T10:04:29.1798888' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (6003, 4002, N'KL12_DEN_S', N'Đen', N'S', CAST(150000.00 AS Decimal(18, 2)), NULL, 122, 300, N'ACTIVE', CAST(N'2026-01-19T10:14:25.4004081' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (6004, 4002, N'KL12_DEN_XL', N'Đen', N'XL', CAST(150000.00 AS Decimal(18, 2)), NULL, 1111, 300, N'ACTIVE', CAST(N'2026-01-19T10:14:25.4024021' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7002, 5002, N'ATINO_CARDIGAN_524_DEN_M', N'Đen', N'M', CAST(159000.00 AS Decimal(18, 2)), NULL, 25, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7003, 5002, N'ATINO_CARDIGAN_524_DEN_L', N'Đen', N'L', CAST(159000.00 AS Decimal(18, 2)), NULL, 14, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7004, 5002, N'ATINO_CARDIGAN_524_XAM_M', N'Xám', N'M', CAST(159000.00 AS Decimal(18, 2)), NULL, 15, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7005, 5002, N'ATINO_CARDIGAN_524_XAM_L', N'Xám', N'L', CAST(159000.00 AS Decimal(18, 2)), NULL, 15, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7006, 5003, N'ATINO_HOODIE_6982_DEN_M', N'Đen', N'M', CAST(349000.00 AS Decimal(18, 2)), NULL, 394, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7007, 5003, N'ATINO_HOODIE_6982_DEN_L', N'Đen', N'L', CAST(349000.00 AS Decimal(18, 2)), NULL, 349, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7008, 5003, N'ATINO_HOODIE_6982_DEN_XL', N'Đen', N'XL', CAST(349000.00 AS Decimal(18, 2)), NULL, 300, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7009, 5003, N'ATINO_HOODIE_6982_TRANG_M', N'Trắng', N'M', CAST(349000.00 AS Decimal(18, 2)), NULL, 250, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7010, 5004, N'ATINO_SWEATSHIRT_7862_BE_M', N'Be', N'M', CAST(129000.00 AS Decimal(18, 2)), NULL, 35, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7011, 5004, N'ATINO_SWEATSHIRT_7862_BE_L', N'Be', N'L', CAST(129000.00 AS Decimal(18, 2)), NULL, 35, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7012, 5004, N'ATINO_SWEATSHIRT_7862_DEN_M', N'Đen', N'M', CAST(129000.00 AS Decimal(18, 2)), NULL, 30, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7013, 5004, N'ATINO_SWEATSHIRT_7862_DEN_L', N'Đen', N'L', CAST(129000.00 AS Decimal(18, 2)), NULL, 25, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7014, 5005, N'ATINO_JACKET_8310_DEN_M', N'Đen', N'M', CAST(479000.00 AS Decimal(18, 2)), NULL, 20, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7015, 5005, N'ATINO_JACKET_8310_DEN_L', N'Đen', N'L', CAST(479000.00 AS Decimal(18, 2)), NULL, 15, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7016, 5006, N'ATINO_JACKET_8931_XAM_M', N'Xám', N'M', CAST(749000.00 AS Decimal(18, 2)), NULL, 32, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7017, 5006, N'ATINO_JACKET_8931_XAM_L', N'Xám', N'L', CAST(749000.00 AS Decimal(18, 2)), NULL, 33, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7018, 5007, N'ATINO_PHAO_8561_BE_M', N'Be', N'M', CAST(599000.00 AS Decimal(18, 2)), NULL, 112, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7019, 5007, N'ATINO_PHAO_8561_BE_L', N'Be', N'L', CAST(599000.00 AS Decimal(18, 2)), NULL, 59, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7020, 5008, N'ATINO_JACKET_8893_NAU_M', N'Nâu', N'M', CAST(669000.00 AS Decimal(18, 2)), NULL, 130, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7021, 5008, N'ATINO_JACKET_8893_NAU_L', N'Nâu', N'L', CAST(669000.00 AS Decimal(18, 2)), NULL, 147, 300, N'ACTIVE', CAST(N'2026-03-06T15:45:29.3066667' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7023, 5002, N'ATINO_CARDIGAN_524_XANH_LA_L', N'Xanh lá', N'L', CAST(159000.00 AS Decimal(18, 2)), NULL, 92, 300, N'ACTIVE', CAST(N'2026-03-06T08:58:24.7194674' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7024, 5009, N'CHUCK70S_XANH_DUONG_40', N'Xanh dương', N'40', CAST(950000.00 AS Decimal(18, 2)), NULL, 99, 300, N'ACTIVE', CAST(N'2026-03-06T12:56:01.6008817' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7025, 5009, N'CHUCK70S_XANH_DUONG_41', N'Xanh dương', N'41', CAST(950000.00 AS Decimal(18, 2)), NULL, 99, 300, N'ACTIVE', CAST(N'2026-03-06T12:56:01.6048835' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (7026, 5009, N'CHUCK70S_XANH_DUONG_42', N'Xanh dương', N'42', CAST(950000.00 AS Decimal(18, 2)), NULL, 99, 300, N'ACTIVE', CAST(N'2026-03-06T12:56:01.6058821' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (8002, 5003, N'ATINO_HOODIE_6982_TRANG_L', N'Trắng', N'L', CAST(349000.00 AS Decimal(18, 2)), NULL, 110, 300, N'ACTIVE', CAST(N'2026-03-11T16:33:41.9816181' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (8003, 5003, N'ATINO_HOODIE_6982_TRANG_XL', N'Trắng', N'XL', CAST(349000.00 AS Decimal(18, 2)), NULL, 334, 300, N'ACTIVE', CAST(N'2026-03-11T16:33:42.0294618' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9002, 6002, N'CVER05_TRANG_35', N'Trắng', N'35', CAST(1700000.00 AS Decimal(18, 2)), NULL, 122, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2566887' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9003, 6002, N'CVER05_TRANG_36', N'Trắng', N'36', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2617514' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9004, 6002, N'CVER05_TRANG_37', N'Trắng', N'37', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2646630' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9005, 6002, N'CVER05_TRANG_38', N'Trắng', N'38', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2666583' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9006, 6002, N'CVER05_TRANG_39', N'Trắng', N'39', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2686499' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9007, 6002, N'CVER05_TRANG_40', N'Trắng', N'40', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2716419' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9008, 6002, N'CVER05_TRANG_41', N'Trắng', N'41', CAST(1700000.00 AS Decimal(18, 2)), NULL, 1234, 300, N'ACTIVE', CAST(N'2026-03-13T10:56:03.2736364' AS DateTime2))
GO
INSERT [dbo].[BIEN_THE_SAN_PHAM] ([Id], [SanPhamId], [MaSKU], [MauSac], [KichCo], [Gia], [GiaGoc], [SoLuongTon], [KhoiLuongGram], [TrangThai], [NgayTao]) VALUES (9009, 6003, N'JENAE1_DEN_28', N'Đen', N'28', CAST(5666666.00 AS Decimal(18, 2)), NULL, 1000, 300, N'ACTIVE', CAST(N'2026-03-13T12:31:03.9234875' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[BIEN_THE_SAN_PHAM] OFF
GO
SET IDENTITY_INSERT [dbo].[CHI_TIET_DON_HANG] ON 
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (1, 1, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 2, CAST(645000.00 AS Decimal(18, 2)), CAST(1290000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (2, 2, 3003, N'Áo Len L.5.5082', N'Nâu', N'M', 1, CAST(319000.00 AS Decimal(18, 2)), CAST(319000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (3, 3, 2008, N'Áo Phao Atino Jacket', N'Trắng', N'S', 1, CAST(645000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (4, 3, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 1, CAST(645000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (5, 4, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 1, CAST(645000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (6, 5, 15, N'Quan jean slim basic', N'Xanh', N'30', 1, CAST(299000.00 AS Decimal(18, 2)), CAST(299000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (7, 6, 3005, N'Giày Vans Skool', N'Đen', N'40', 6, CAST(1000000.00 AS Decimal(18, 2)), CAST(6000000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8, 7, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 1, CAST(645000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (9, 8, 6, N'Ao ni Fitted L5.7854', N'Trắng', N'L', 1, CAST(129000.00 AS Decimal(18, 2)), CAST(129000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (2002, 2003, 6, N'Ao ni Fitted L5.7854', N'Trắng', N'L', 1, CAST(129000.00 AS Decimal(18, 2)), CAST(129000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (3003, 3004, 4002, N'Ao khoac denim', N'Xanh dương', N'L', 4, CAST(250000.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (6004, 6005, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 3, CAST(645000.00 AS Decimal(18, 2)), CAST(1935000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (7004, 7005, 6, N'Áo nỉ Fitted', N'Trắng', N'L', 16, CAST(129000.00 AS Decimal(18, 2)), CAST(2064000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (7007, 7007, 4002, N'Áo Khoác Denim', N'Xanh dương', N'L', 4, CAST(250000.00 AS Decimal(18, 2)), CAST(1000000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (7008, 7007, 3006, N'Áo Len Frence', N'Đen', N'L', 1, CAST(319000.00 AS Decimal(18, 2)), CAST(319000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (7009, 7007, 6, N'Áo nỉ Fitted', N'Trắng', N'L', 1, CAST(129000.00 AS Decimal(18, 2)), CAST(129000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8004, 8005, 2003, N'Áo Phao Atino Jacket', N'Đen', N'S', 1, CAST(645000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8005, 8006, 6, N'Áo nỉ Fitted', N'Trắng', N'L', 4, CAST(129000.00 AS Decimal(18, 2)), CAST(516000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8006, 8007, 2002, N'Áo Phao Atino Jacket', N'Đen', N'XS', 5, CAST(645000.00 AS Decimal(18, 2)), CAST(3225000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8007, 8007, 13, N'Quần Jean SlimFit', N'Xanh dương', N'28', 11, CAST(299000.00 AS Decimal(18, 2)), CAST(3289000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (8008, 8008, 13, N'Quần Jean SlimFit', N'Xanh dương', N'28', 15, CAST(299000.00 AS Decimal(18, 2)), CAST(4485000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (11004, 11005, 6002, N'Áo Sơ Mi Dài Regular', N'Đen', N'L', 1, CAST(150000.00 AS Decimal(18, 2)), CAST(150000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (11005, 11005, 6, N'Áo nỉ Fitted', N'Trắng', N'L', 3, CAST(129000.00 AS Decimal(18, 2)), CAST(387000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (11006, 11006, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 9, CAST(159000.00 AS Decimal(18, 2)), CAST(1431000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (11007, 11007, 8002, N'Áo Hoodie Loose', N'Trắng', N'L', 8, CAST(349000.00 AS Decimal(18, 2)), CAST(2792000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (12004, 12005, 7024, N'Chuck 70s High Navy', N'Xanh dương', N'40', 1, CAST(950000.00 AS Decimal(18, 2)), CAST(950000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (12005, 12006, 7016, N'Áo Jacket XL', N'Xám', N'M', 1, CAST(749000.00 AS Decimal(18, 2)), CAST(749000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (13004, 13005, 7010, N'Áo Nỉ Fitted', N'Be', N'M', 5, CAST(129000.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (13005, 13005, 7004, N'Áo Cardigan Regular', N'Xám', N'M', 5, CAST(159000.00 AS Decimal(18, 2)), CAST(795000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (13006, 13006, 7025, N'Chuck 70s High Navy', N'Xanh dương', N'41', 1, CAST(950000.00 AS Decimal(18, 2)), CAST(950000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (14004, 14005, 7007, N'Áo Hoodie Loose', N'Đen', N'L', 1, CAST(349000.00 AS Decimal(18, 2)), CAST(349000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (14005, 14006, 4002, N'Áo Khoác Denim', N'Xanh dương', N'L', 6, CAST(250000.00 AS Decimal(18, 2)), CAST(1500000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15004, 15005, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 5, CAST(159000.00 AS Decimal(18, 2)), CAST(795000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15005, 15006, 5003, N'Giày Vans Skool', N'Đen', N'41', 1, CAST(250000.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15006, 15007, 5003, N'Giày Vans Skool', N'Đen', N'41', 7, CAST(250000.00 AS Decimal(18, 2)), CAST(1750000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15007, 15008, 7008, N'Áo Hoodie Loose', N'Đen', N'XL', 1, CAST(349000.00 AS Decimal(18, 2)), CAST(349000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15008, 15009, 9004, N'Chuck Taylor All Star', N'Trắng', N'37', 1, CAST(1700000.00 AS Decimal(18, 2)), CAST(1700000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15009, 15010, 9002, N'Chuck Taylor All Star', N'Trắng', N'35', 1, CAST(1700000.00 AS Decimal(18, 2)), CAST(1700000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15010, 15011, 3006, N'Áo Len Frence', N'Đen', N'L', 5, CAST(319000.00 AS Decimal(18, 2)), CAST(1595000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (15011, 15012, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 2, CAST(159000.00 AS Decimal(18, 2)), CAST(318000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (16004, 16005, 9002, N'Chuck Taylor All Star', N'Trắng', N'35', 2, CAST(1700000.00 AS Decimal(18, 2)), CAST(3400000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (17004, 17005, 1003, N'Áo nỉ Fitted', N'Nâu', N'L', 7, CAST(129000.00 AS Decimal(18, 2)), CAST(903000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (17005, 17006, 9002, N'Chuck Taylor All Star', N'Trắng', N'35', 1, CAST(1700000.00 AS Decimal(18, 2)), CAST(1700000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (17006, 17007, 7018, N'Áo Phao', N'Be', N'M', 1, CAST(599000.00 AS Decimal(18, 2)), CAST(599000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (18004, 18007, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 5, CAST(159000.00 AS Decimal(18, 2)), CAST(795000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (18005, 18008, 7006, N'Áo Hoodie Loose', N'Đen', N'M', 5, CAST(349000.00 AS Decimal(18, 2)), CAST(1745000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (18006, 18009, 7006, N'Áo Hoodie Loose', N'Đen', N'M', 5, CAST(349000.00 AS Decimal(18, 2)), CAST(1745000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (18007, 18010, 7020, N'Áo Jacket', N'Nâu', N'M', 131, CAST(669000.00 AS Decimal(18, 2)), CAST(87639000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (18008, 18012, 7020, N'Áo Jacket', N'Nâu', N'M', 1, CAST(669000.00 AS Decimal(18, 2)), CAST(669000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19004, 19007, 7002, N'Áo Cardigan Regular', N'Đen', N'M', 4, CAST(159000.00 AS Decimal(18, 2)), CAST(636000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19005, 19007, 7006, N'Áo Hoodie Loose', N'Đen', N'M', 1, CAST(349000.00 AS Decimal(18, 2)), CAST(349000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19006, 19007, 8002, N'Áo Hoodie Loose', N'Trắng', N'L', 4, CAST(349000.00 AS Decimal(18, 2)), CAST(1396000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19007, 19008, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 7, CAST(159000.00 AS Decimal(18, 2)), CAST(1113000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19008, 19009, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 1, CAST(159000.00 AS Decimal(18, 2)), CAST(159000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19009, 19010, 7016, N'Áo Jacket XL', N'Xám', N'M', 5, CAST(749000.00 AS Decimal(18, 2)), CAST(3745000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19010, 19010, 9004, N'Chuck Taylor All Star', N'Trắng', N'37', 1, CAST(1700000.00 AS Decimal(18, 2)), CAST(1700000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19011, 19011, 7005, N'Áo Cardigan Regular', N'Xám', N'L', 5, CAST(159000.00 AS Decimal(18, 2)), CAST(795000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19012, 19012, 7023, N'Áo Cardigan Regular', N'Xanh lá', N'L', 1, CAST(159000.00 AS Decimal(18, 2)), CAST(159000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[CHI_TIET_DON_HANG] ([Id], [DonHangId], [BienTheSanPhamId], [TenSanPham], [MauSac], [KichCo], [SoLuong], [DonGia], [ThanhTien]) VALUES (19013, 19013, 7002, N'Áo Cardigan Regular', N'Đen', N'M', 1, CAST(159000.00 AS Decimal(18, 2)), CAST(159000.00 AS Decimal(18, 2)))
GO
SET IDENTITY_INSERT [dbo].[CHI_TIET_DON_HANG] OFF
GO
SET IDENTITY_INSERT [dbo].[CHI_TIET_GIO_HANG] ON 
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (1, 1, 2, 1, CAST(129000.00 AS Decimal(18, 2)), CAST(N'2026-01-06T08:18:09.9060423' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (3, 6, 2, 1, CAST(129000.00 AS Decimal(18, 2)), CAST(N'2026-01-06T09:33:26.5468775' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (5004, 6011, 2003, 1, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-01-18T12:10:33.6701289' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (5007, 6015, 3006, 4, CAST(319000.00 AS Decimal(18, 2)), CAST(N'2026-01-18T13:27:21.2940904' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (5011, 6015, 13, 3, CAST(299000.00 AS Decimal(18, 2)), CAST(N'2026-01-18T13:32:20.3736301' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (5012, 6015, 2003, 1, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-01-18T13:33:46.4933970' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (6007, 8031, 2008, 1, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-01-20T14:30:23.1809005' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (6009, 8032, 3006, 3, CAST(319000.00 AS Decimal(18, 2)), CAST(N'2026-01-20T14:31:25.6687009' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (6010, 8033, 2003, 1, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-01-20T14:33:17.0368071' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (8008, 10012, 2013, 3, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-02-27T13:15:04.7205893' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (8011, 10014, 13, 15, CAST(299000.00 AS Decimal(18, 2)), CAST(N'2026-02-27T14:07:53.6957898' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (8013, 10016, 13, 20, CAST(299000.00 AS Decimal(18, 2)), CAST(N'2026-02-27T16:43:27.0829769' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (9004, 11009, 2013, 6, CAST(645000.00 AS Decimal(18, 2)), CAST(N'2026-02-28T08:03:47.1036998' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (13006, 16017, 3007, 10, CAST(319000.00 AS Decimal(18, 2)), CAST(N'2026-03-11T12:42:09.1446791' AS DateTime2))
GO
INSERT [dbo].[CHI_TIET_GIO_HANG] ([Id], [GioHangId], [BienTheSanPhamId], [SoLuong], [DonGia], [NgayTao]) VALUES (20015, 25025, 7005, 4, CAST(159000.00 AS Decimal(18, 2)), CAST(N'2026-03-20T16:56:54.0457539' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[CHI_TIET_GIO_HANG] OFF
GO
SET IDENTITY_INSERT [dbo].[DANH_MUC] ON 
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (3, N'Quần', N'Quần', NULL, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4349580' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (4, N'QUAN_JEAN', N'Quần Jean', 3, N'ACTIVE', CAST(N'2026-01-04T22:59:43.4354711' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1002, N'AO_KHOAC', N'ÁO KHOÁC', 1003, N'ACTIVE', CAST(N'2026-01-05T18:56:50.5233333' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1003, N'AO_THU_DONG', N'ÁO THU ĐÔNG', NULL, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4354900' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1004, N'AO_BLAZER_AO_MANG_TO', N'ÁO BLAZER / ÁO MĂNG TÔ', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1005, N'AO_HOODIE', N'ÁO HOODIE', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1006, N'AO_LEN', N'ÁO LEN', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1007, N'AO_NI_AO_THUN_DAI_TAY', N'ÁO NỈ / ÁO THUN DÀI TAY', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1008, N'BO_THE_THAO_THU_DONG', N'BỘ THỂ THAO THU ĐÔNG', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1009, N'CARDIGAN', N'CARDIGAN', 1003, N'ACTIVE', CAST(N'2026-01-05T20:17:10.4468928' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1010, N'AO_XUAN_HE', N'ÁO XUÂN HÈ', NULL, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4412095' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1011, N'AO_PHONG', N'ÁO PHÔNG', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1012, N'AO_POLO', N'ÁO POLO', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1013, N'AO_SO_MI_DAI_TAY', N'ÁO SƠ MI DÀI TAY', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1014, N'AO_SO_MI_NGAN_TAY', N'ÁO SƠ MI NGẮN TAY', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1015, N'AO_TANK_TOP', N'ÁO TANK TOP', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1016, N'BO_THE_THAO_HE', N'BỘ THỂ THAO HÈ', 1010, N'ACTIVE', CAST(N'2026-01-05T20:18:49.4482097' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1017, N'QUAN_DAI', N'QUẦN DÀI', 3, N'ACTIVE', CAST(N'2026-01-05T20:19:28.5863040' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (1018, N'QUAN_SHORT', N'QUẦN SHORT', 3, N'ACTIVE', CAST(N'2026-01-05T20:19:28.5863040' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (2002, N'GA1', N'Giày Thể Thao', NULL, N'ACTIVE', CAST(N'2026-01-06T16:25:20.5455137' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (2004, N'GA1E', N'Giày Vans', 2002, N'ACTIVE', CAST(N'2026-01-06T16:26:51.6953635' AS DateTime2))
GO
INSERT [dbo].[DANH_MUC] ([Id], [Ma], [Ten], [DanhMucChaId], [TrangThai], [NgayTao]) VALUES (3002, N'GAI2', N'Giày Converse', 2002, N'ACTIVE', CAST(N'2026-03-06T19:52:18.6605130' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[DANH_MUC] OFF
GO
SET IDENTITY_INSERT [dbo].[DIA_CHI_GIAO_HANG] ON 
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (1, 2, N'Trần Đức Hải', N'0911111111', N'Thành phố Hà Nội', N'Quận Ba Đình', N'Phường Phúc Xá', N'9 Hồng Hà', 1, CAST(N'2026-03-12T00:12:17.8966667' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (3, 3003, N'Trần Đức Hải', N'0977989847', N'Hà Nội', N'Thanh Xuân', N'Khương Trung', N'12 Nguyễn Trãi', 1, CAST(N'2026-03-12T00:12:17.8966667' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (4, 3004, N'Người dùng demo', N'0988888888', N'Hồ Chí Minh', N'Quận 1', N'Bến Nghé', N'45 Lê Lợi', 1, CAST(N'2026-03-12T00:12:17.8966667' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (5, 3004, N'Người dùng demo', N'0988888888', N'Hồ Chí Minh', N'Quận 7', N'Tân Phú', N'Chung cư Sunrise City', 0, CAST(N'2026-03-12T00:12:17.8966667' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (1002, 1, N'Nguyen', N'023940983204', N'Tỉnh Hoà Bình', N'Huyện Lương Sơn', N'Thị trấn Lương Sơn', N'wrwqewe', 1, CAST(N'2026-03-12T11:43:31.8504368' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (1003, 3005, N'Ly Bach', N'0585816963', N'Thành phố Hà Nội', N'Huyện Sóc Sơn', N'Xã Hiền Ninh', N'56 Duong Giua Yen Ninh', 1, CAST(N'2026-03-13T04:57:45.6057527' AS DateTime2))
GO
INSERT [dbo].[DIA_CHI_GIAO_HANG] ([Id], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [TinhThanh], [QuanHuyen], [PhuongXa], [DiaChiChiTiet], [LaMacDinh], [NgayTao]) VALUES (2003, 3005, N'Ly Bach', N'023940983204', N'Hà Nội', N'Thanh Xuân', N'Nhân Chính', N'145 Quan Nhan', 0, CAST(N'2026-03-13T12:27:04.1910584' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[DIA_CHI_GIAO_HANG] OFF
GO
SET IDENTITY_INSERT [dbo].[DON_HANG] ON 
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (1, N'DH-262946C3', 2, N'Tran Duc Hai', N'0979789847', N'khach01@gmail.com', N'145 quan nhan', N'12', N'DELIVERED', CAST(1290000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1290000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T08:20:49.0148508' AS DateTime2), CAST(N'2026-01-07T10:10:30.0712986' AS DateTime2), NULL, NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (2, N'DH-A349E602', NULL, N'Trần Đức Hải', N'0979789847', N'duchai.40net@gmail.com', N'thanh xuân', N'', N'COMPLETED', CAST(319000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(319000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T09:41:28.2034537' AS DateTime2), CAST(N'2026-01-12T10:13:22.7746134' AS DateTime2), NULL, NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (3, N'DH-7490B9DC', 2, N'Tran Duc Hai', N'0979789847', N'khach01@gmail.com', N'aa', N'', N'COMPLETED', CAST(1290000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1290000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T09:49:35.3168453' AS DateTime2), CAST(N'2026-01-19T08:07:20.9958816' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (4, N'DH-9A85A980', 2, N'Trần Đức Hải', N'0979789847', N'khach01@gmail.com', N'kkkk', N'', N'DELIVERED', CAST(645000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T09:50:17.3759421' AS DateTime2), CAST(N'2026-01-19T08:07:08.1850465' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (5, N'DH-E0363837', 2, N'Tran Duc Hai', N'0585816963', N'khach01@gmail.com', N'đ', N'', N'COMPLETED', CAST(299000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(299000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T09:52:08.3414762' AS DateTime2), CAST(N'2026-03-12T06:17:59.8797556' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (6, N'DH-A3B11726', NULL, N'tRA', N'0585816963', N'duchai.40net@gmail.com', N'kk
', N'', N'DELIVERED', CAST(6000000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(6000000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T11:00:08.3919270' AS DateTime2), CAST(N'2026-01-07T10:10:41.9382168' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (7, N'DH-7BD6F52D', NULL, N'Trần Đức Hải', N'0979789847', N'duchai.40net@gmail.com', N'a', N'a', N'CANCELLED', CAST(645000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T12:55:28.4337644' AS DateTime2), CAST(N'2026-01-07T10:10:52.4548710' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (8, N'DH-555803F6', 2, N'Tran Duc Hai', N'0766128057', N'khach01@gmail.com', N'145 QUAN Nhân', N'', N'COMPLETED', CAST(129000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(129000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-06T13:34:30.4640532' AS DateTime2), CAST(N'2026-01-12T10:13:37.1078823' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (2003, N'DH-39FF6847', NULL, N'Trần Đức Hải', N'0979789847', N'duchai.40net@gmail.com', N'145 Quan Nhân', N'Giao vào 14-17h', N'COMPLETED', CAST(129000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(129000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-08T13:13:50.0141130' AS DateTime2), CAST(N'2026-01-12T10:36:35.6935901' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (3004, N'DH-DAEF13F8', 2, N'Tran Duc Hai', N'0911111111', N'khach01@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'COMPLETED', CAST(1000000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(950000.00 AS Decimal(18, 2)), 1, CAST(N'2026-01-11T13:17:04.1556353' AS DateTime2), CAST(N'2026-01-12T10:13:48.0589105' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (6005, N'DH-2BF38175', 3003, N'Trần Đức Hải', N'0979789847', N'duchai.40net@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Trúc Bạch, Quận Ba Đình, Thành phố Hà Nội', N'', N'DELIVERED', CAST(1935000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1885000.00 AS Decimal(18, 2)), 2, CAST(N'2026-01-20T13:02:23.0382265' AS DateTime2), CAST(N'2026-03-12T06:21:21.2992063' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (7005, N'DH-BE0F81BF', NULL, N'Bùi Đức Duy Anh', N'0900000000', N'duchai.40net@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Vĩnh Phúc, Quận Ba Đình, Thành phố Hà Nội', N'', N'COMPLETED', CAST(2064000.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1814000.00 AS Decimal(18, 2)), 1002, CAST(N'2026-01-22T13:09:55.0707994' AS DateTime2), CAST(N'2026-01-22T13:16:14.4646829' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (7007, N'DH-51B187E0', 2, N'Tran Duc Hai', N'0911111111', N'khach01@gmail.com', N'56 xóm chùa ngoài thôn Yên Ninh, Phường Trúc Bạch, Quận Ba Đình, Thành phố Hà Nội', N'', N'CANCELLED', CAST(1448000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1448000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-22T13:11:32.0810478' AS DateTime2), CAST(N'2026-01-22T13:12:30.0577904' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (8005, N'DH-0202A8FD', NULL, N'Tran Duc Hai', N'0979789847', N'haitdph41477@fpt.edu.vn', N'42A ngõ 145 Quan Nhân, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'COMPLETED', CAST(645000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(645000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-02-27T13:08:42.8218872' AS DateTime2), CAST(N'2026-03-11T14:44:18.6817341' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (8006, N'DH-E0DF6DAA', NULL, N'Nhân viên demo', N'0979789847', N'duchai.40net@gmail.com', N'42A ngõ 145 Quan Nhân, Xã Lũng Cú, Huyện Đồng Văn, Tỉnh Hà Giang', N'', N'HOAN_THANH', CAST(516000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(516000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-02-27T13:09:57.5936188' AS DateTime2), CAST(N'2026-03-12T15:33:46.8515644' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (8007, N'DH-C90A47EF', 3004, N'Người dùng demo', N'0979789847', N'user@gmail.com', N'asdasdadas, Phường Trúc Bạch, Quận Ba Đình, Thành phố Hà Nội', N'', N'CANCELLED', CAST(6514000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(6514000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-02-27T13:13:35.0056926' AS DateTime2), CAST(N'2026-02-27T13:14:14.0798730' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (8008, N'DH-32EEED2F', 2, N'Tran Duc Hai', N'0911111111', N'khach01@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'CANCELLED', CAST(4485000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(4435000.00 AS Decimal(18, 2)), 1, CAST(N'2026-02-27T14:09:42.0395742' AS DateTime2), CAST(N'2026-02-27T16:39:17.9142631' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (11005, N'DH-F6DDE340', 2, N'Tran Duc Hai', N'0911111111', N'khach01@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'COMPLETED', CAST(537000.00 AS Decimal(18, 2)), CAST(110.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(536890.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-11T11:51:23.6758856' AS DateTime2), CAST(N'2026-03-11T11:51:51.0703518' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (11006, N'POS-4FC137F0', NULL, N'áaaa', N'12141414132', NULL, N'Mua tại quầy', NULL, N'COMPLETED', CAST(1431000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1431000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-11T15:43:53.1537194' AS DateTime2), CAST(N'2026-03-11T15:43:53.1587026' AS DateTime2), N'transfer', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (11007, N'POS-2E6F0889', 1002, N'Nhân viên demo', N'0979789847', N'nhanvien@gmail.com', N'Mua tại quầy', N'TEST', N'COMPLETED', CAST(2792000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(2792000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-11T17:05:51.5546890' AS DateTime2), CAST(N'2026-03-11T17:05:51.5646609' AS DateTime2), N'transfer', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (12005, N'DH-60AE0040', 2, N'Trần Đức Hải', N'0911111111', N'khach01@gmail.com', N'9 Hồng Hà, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'CANCELLED', CAST(950000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(920000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-12T05:59:03.7230846' AS DateTime2), CAST(N'2026-03-12T05:59:12.7992601' AS DateTime2), N'COD', N'adcada', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (12006, N'DH-36BF05EC', 2, N'Trần Đức Hải', N'0911111111', N'khach01@gmail.com', N'9 Hồng Hà, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(749000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(719000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-12T06:00:02.7323321' AS DateTime2), CAST(N'2026-03-12T15:27:22.6782000' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (13005, N'DH-7B0EA79D', 2, N'Trần Đức Hải', N'0911111111', N'khach01@gmail.com', N'9 Hồng Hà, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(1440000.00 AS Decimal(18, 2)), CAST(110.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1439890.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-12T14:06:42.9819245' AS DateTime2), CAST(N'2026-03-12T15:18:47.5916391' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (13006, N'POS-8400F798', 1, N'Chủ Shop Đẹp Zai', N'0900000000', N'admin@hancos.vn', N'Mua tại quầy', NULL, N'HOAN_THANH', CAST(950000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(950000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-12T17:17:04.3315995' AS DateTime2), CAST(N'2026-03-12T17:17:04.3366008' AS DateTime2), N'transfer', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (14005, N'DH-088859A3', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Tung Chung Phố, Huyện Mường Khương, Tỉnh Lào Cai', N'', N'HOAN_THANH', CAST(349000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(20000.00 AS Decimal(18, 2)), CAST(339000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T05:01:23.9585062' AS DateTime2), CAST(N'2026-03-13T06:39:08.7424225' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (14006, N'DH-D7CF668C', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'', N'DA_HUY', CAST(1500000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1470000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T06:31:20.6042510' AS DateTime2), CAST(N'2026-03-13T06:31:39.7169006' AS DateTime2), N'VIETQR', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15005, N'POS-74CF1FDF', 1, N'Chủ Shop Đẹp Zai', N'0900000000', N'admin@hancos.vn', N'Mua tại quầy', N'test', N'HOAN_THANH', CAST(795000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(765000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T12:13:10.4041982' AS DateTime2), CAST(N'2026-03-13T12:13:10.4164777' AS DateTime2), N'cash', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15006, N'POS-8832B12A', NULL, N'Khách lẻ', N'N/A', NULL, N'Mua tại quầy', NULL, N'HOAN_THANH', CAST(250000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-13T12:13:46.4548723' AS DateTime2), CAST(N'2026-03-13T12:13:46.4578634' AS DateTime2), N'cash', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15007, N'POS-FC64C629', 3003, N'Trần Đức Hải', N'0979789847', N'duchai.40net@gmail.com', N'Mua tại quầy', NULL, N'HOAN_THANH', CAST(1750000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1750000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-13T12:14:46.2090844' AS DateTime2), CAST(N'2026-03-13T12:14:46.2126225' AS DateTime2), N'cash', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15008, N'POS-601ED194', 1, N'Chủ Shop Đẹp Zai', N'0900000000', N'admin@hancos.vn', N'Mua tại quầy', NULL, N'TRA_HANG', CAST(349000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(349000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-13T12:15:41.4541892' AS DateTime2), CAST(N'2026-03-16T11:50:11.8896089' AS DateTime2), N'cash', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15009, N'DH-DAAEBD68', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'16h', N'DA_HUY', CAST(1700000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1670000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T12:26:28.5306647' AS DateTime2), CAST(N'2026-03-13T12:27:28.2404503' AS DateTime2), N'VIETQR', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15010, N'DH-87E1C4B8', 3005, N'Ly Bach', N'023940983204', N'duchai.20net@gmail.com', N'11111, Phường Phúc Tân, Quận Hoàn Kiếm, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(1700000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1700000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-13T12:27:56.7918200' AS DateTime2), CAST(N'2026-03-13T12:29:39.6374752' AS DateTime2), N'VIETQR', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15011, N'POS-13D68FCC', 3005, N'LyBach', N'0585816963', N'duchai.20net@gmail.com', N'Mua tại quầy', NULL, N'HOAN_THANH', CAST(1595000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1565000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T12:34:38.3836330' AS DateTime2), CAST(N'2026-03-13T12:34:38.3886129' AS DateTime2), N'transfer', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (15012, N'DH-2DF06F71', 1, N'Nguyen', N'023940983204', N'admin@hancos.vn', N'wrwqewe, Thị trấn Lương Sơn, Huyện Lương Sơn, Tỉnh Hoà Bình', N'', N'HOAN_THANH', CAST(318000.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(20000.00 AS Decimal(18, 2)), CAST(308000.00 AS Decimal(18, 2)), 2003, CAST(N'2026-03-13T12:54:47.0461225' AS DateTime2), CAST(N'2026-03-16T12:40:55.2016366' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (16005, N'DH-311FF1F5', 1, N'Nguyen', N'023940983204', N'admin@hancos.vn', N'wrwqewe, Thị trấn Lương Sơn, Huyện Lương Sơn, Tỉnh Hoà Bình', N'', N'DA_HUY', CAST(3400000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(3385000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-14T15:24:20.4591466' AS DateTime2), CAST(N'2026-03-14T15:24:31.2897756' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (17005, N'POS-689CACB1', NULL, N'Khách lẻ', N'N/A', NULL, N'Mua tại quầy', NULL, N'HOAN_THANH', CAST(903000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(903000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-16T11:45:31.0912328' AS DateTime2), CAST(N'2026-03-16T11:45:31.0932292' AS DateTime2), N'cash', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (17006, N'DH-11F58008', 1, N'Nguyen', N'023940983204', N'admin@hancos.vn', N'wrwqewe, Thị trấn Lương Sơn, Huyện Lương Sơn, Tỉnh Hoà Bình', N'', N'TRA_HANG', CAST(1700000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1685000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-16T11:52:11.2682884' AS DateTime2), CAST(N'2026-03-16T11:53:00.8632039' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (17007, N'DH-8A0488FD', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'', N'DA_HUY', CAST(599000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(584000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-16T12:45:29.6674294' AS DateTime2), CAST(N'2026-03-20T16:14:46.7365231' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (18007, N'DH-9105F30D', 1, N'Nguyen', N'023940983204', N'admin@hancos.vn', N'wrwqewe, Thị trấn Lương Sơn, Huyện Lương Sơn, Tỉnh Hoà Bình', N'', N'HOAN_THANH', CAST(795000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(780000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-20T14:08:14.3911410' AS DateTime2), CAST(N'2026-03-24T07:14:11.1582909' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (18008, N'DH-B9C9B6D3', 2, N'Trần Đức Hải', N'0911111111', N'khach01@gmail.com', N'9 Hồng Hà, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'DA_HUY', CAST(1745000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1730000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-20T16:11:49.0051834' AS DateTime2), CAST(N'2026-03-20T16:13:48.9726096' AS DateTime2), N'COD', N'adcada', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (18009, N'DH-C4D60862', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(1745000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1730000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-20T16:11:50.4794262' AS DateTime2), CAST(N'2026-03-24T07:14:04.8999172' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (18010, N'DH-13DDE62B', 2, N'Trần Đức Hải', N'0911111111', N'khach01@gmail.com', N'9 Hồng Hà, Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội', N'', N'DA_HUY', CAST(87639000.00 AS Decimal(18, 2)), CAST(21909750.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(65729250.00 AS Decimal(18, 2)), 1003, CAST(N'2026-03-20T16:12:52.1209166' AS DateTime2), CAST(N'2026-03-20T16:13:44.8360015' AS DateTime2), N'COD', N'adcada', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (18012, N'DH-7BBA80D2', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(669000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(654000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-20T16:14:02.4172193' AS DateTime2), CAST(N'2026-03-24T07:13:26.9019495' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19007, N'DH-DBE32CFD', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'42A ngõ 145 Quan Nhân, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(2381000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(2381000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-24T06:10:58.2079198' AS DateTime2), CAST(N'2026-03-24T07:13:53.7612486' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19008, N'DH-F17990F8', 3005, N'Ly Bach', N'023940983204', N'duchai.20net@gmail.com', N'145 Quan Nhan, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(1113000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(1098000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-24T07:09:26.1392182' AS DateTime2), CAST(N'2026-03-24T07:13:46.4859389' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19009, N'DH-C7D8219B', 1, N'Nguyen', N'023940983204', N'admin@hancos.vn', N'wrwqewe, Thị trấn Lương Sơn, Huyện Lương Sơn, Tỉnh Hoà Bình', N'', N'HOAN_THANH', CAST(159000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(189000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-24T08:14:52.9769530' AS DateTime2), CAST(N'2026-03-24T08:39:17.9691464' AS DateTime2), N'COD', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19010, N'DH-32142CDB', 3005, N'Ly Bach', N'0585816963', N'duchai.20net@gmail.com', N'56 Duong Giua Yen Ninh, Xã Hiền Ninh, Huyện Sóc Sơn, Thành phố Hà Nội', N'', N'DA_HUY', CAST(5445000.00 AS Decimal(18, 2)), CAST(1361250.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(4083750.00 AS Decimal(18, 2)), 1003, CAST(N'2026-03-25T05:14:25.0211903' AS DateTime2), CAST(N'2026-03-25T05:14:36.1983911' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19011, N'DH-8EE85F14', 3005, N'Ly Bach', N'023940983204', N'duchai.20net@gmail.com', N'145 Quan Nhan, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'DA_HUY', CAST(795000.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(780000.00 AS Decimal(18, 2)), 1, CAST(N'2026-03-27T11:55:21.1372579' AS DateTime2), CAST(N'2026-03-27T12:49:16.8845118' AS DateTime2), N'COD', N'Muốn Thay Đổi Địa CHỉ', NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19012, N'DH-EE95AA5E', 3005, N'Ly Bach', N'023940983204', N'duchai.20net@gmail.com', N'145 Quan Nhan, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'PENDING_PAYMENT', CAST(159000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(189000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-27T15:27:06.4184882' AS DateTime2), CAST(N'2026-03-27T15:27:06.4194890' AS DateTime2), N'VNPAY', NULL, NULL)
GO
INSERT [dbo].[DON_HANG] ([Id], [MaDonHang], [TaiKhoanId], [HoTenNhan], [SoDienThoaiNhan], [EmailNhan], [DiaChiNhan], [GhiChu], [TrangThai], [TamTinh], [GiamGia], [PhiVanChuyen], [TongTien], [MaGiamGiaId], [NgayDat], [NgayCapNhat], [PhuongThucThanhToan], [LyDoHuy], [LyDoLoiVanChuyen]) VALUES (19013, N'DH-1AAEE634', 3005, N'Ly Bach', N'023940983204', N'duchai.20net@gmail.com', N'145 Quan Nhan, Phường Nhân Chính, Quận Thanh Xuân, Thành phố Hà Nội', N'', N'HOAN_THANH', CAST(159000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(30000.00 AS Decimal(18, 2)), CAST(189000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-03-27T15:30:27.6628333' AS DateTime2), CAST(N'2026-03-28T07:18:23.7435167' AS DateTime2), N'VIETQR', NULL, NULL)
GO
SET IDENTITY_INSERT [dbo].[DON_HANG] OFF
GO
SET IDENTITY_INSERT [dbo].[GIAO_DICH_TON_KHO] ON 
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1, 1, N'NHAP', 3, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-06T09:40:47.4867866' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (2, 1, N'NHAP', 123, N'TAI_KHOAN', 1, N'Nhap cua atino', CAST(N'2026-03-06T09:41:06.6536700' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3, 13, N'NHAP', 3, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-06T13:42:35.7227926' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (4, 7024, N'XUAT', 1, N'DON_HANG', 9005, N'Ban tai quay - POS-32976A4C', CAST(N'2026-03-06T14:04:13.0539995' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (5, 1, N'NHAP', 2, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-06T14:12:31.4344875' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1002, 7018, N'NHAP', 92, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-09T12:17:49.2661906' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1003, 7019, N'NHAP', 44, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-09T12:17:55.9834917' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1004, 7020, N'NHAP', 111, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-13T11:23:14.3759960' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1005, 7021, N'NHAP', 132, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-13T11:23:23.4256672' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (1006, 2, N'NHAP', 1, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-13T12:31:30.7797836' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (2004, 7018, N'NHAP', 1, N'DON_HANG', 17007, N'Hoàn kho do lỗi vận chuyển - DH-8A0488FD', CAST(N'2026-03-20T16:14:46.7325372' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3004, 2016, N'NHAP', 123, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-27T12:11:59.5278412' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3005, 2012, N'NHAP', 22, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-27T12:12:22.4399835' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3006, 2012, N'NHAP', 44, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-27T12:12:33.2725561' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3007, 2007, N'NHAP', 12, N'TAI_KHOAN', 1, N'Nhap cua atino', CAST(N'2026-03-27T12:17:02.0207788' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3008, 2002, N'NHAP', 111, N'TAI_KHOAN', 1, N'Nhap cua atino', CAST(N'2026-03-27T12:18:19.7470116' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3010, 7003, N'XUAT', 23, N'TAI_KHOAN', 1, N'Nhap cua atino', CAST(N'2026-03-27T12:41:19.9498579' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3011, 7005, N'NHAP', 5, N'DON_HANG', 19011, N'Hoan kho tu don DH-8EE85F14', CAST(N'2026-03-27T12:49:16.8805238' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3012, 7003, N'NHAP', 12, N'TAI_KHOAN', 1, N'', CAST(N'2026-03-27T12:52:08.3284344' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3013, 7023, N'XUAT', 1, N'DON_HANG', 19012, N'Tru kho tu don DH-EE95AA5E', CAST(N'2026-03-27T15:27:06.4330937' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3014, 7002, N'XUAT', 1, N'DON_HANG', 19013, N'Tru kho tu don DH-1AAEE634', CAST(N'2026-03-27T15:30:27.6747938' AS DateTime2))
GO
INSERT [dbo].[GIAO_DICH_TON_KHO] ([Id], [BienTheSanPhamId], [Loai], [SoLuong], [ThamChieuLoai], [ThamChieuId], [GhiChu], [NgayTao]) VALUES (3015, 7002, N'XUAT', 1, N'DON_HANG', 19013, N'LOI_VAN_CHUYEN - Don DH-1AAEE634 gap su co van chuyen', CAST(N'2026-03-28T07:17:28.8767151' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[GIAO_DICH_TON_KHO] OFF
GO
SET IDENTITY_INSERT [dbo].[GIO_HANG] ON 
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (1, NULL, N'A7419233552ADBF2B80364786C405EDF', CAST(N'2026-01-06T08:07:46.5829082' AS DateTime2), CAST(N'2026-01-06T08:07:46.5829082' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (5, NULL, N'B26A24141B2AB9ED767DB5ED24E347FA', CAST(N'2026-01-06T08:51:36.5603935' AS DateTime2), CAST(N'2026-01-06T08:51:36.5603935' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6, NULL, N'BB9D0D9479FDC0F2B2235305DD3E8145', CAST(N'2026-01-06T09:33:21.4567323' AS DateTime2), CAST(N'2026-01-06T09:33:21.4567323' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8, NULL, N'A59033A571A51A09C7A6593649D2761D', CAST(N'2026-01-06T09:46:17.6868188' AS DateTime2), CAST(N'2026-01-06T09:46:17.6868188' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (12, NULL, N'F96A9C43503BC93619BB273EDC85CBAD', CAST(N'2026-01-06T12:25:10.1811623' AS DateTime2), CAST(N'2026-01-06T12:25:10.1811623' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13, NULL, N'7385ED5417578D8EB77F8BF4E860E2FA', CAST(N'2026-01-06T12:45:59.7206826' AS DateTime2), CAST(N'2026-01-06T12:45:59.7206826' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (15, NULL, N'17E8D5C34260E1B2EFAF8140DD104C0F', CAST(N'2026-01-06T13:17:11.3545319' AS DateTime2), CAST(N'2026-01-06T13:17:11.3545319' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17, NULL, N'5FF2CDA77925BE59B9FF314B2A3EB5F8', CAST(N'2026-01-07T02:59:27.2022409' AS DateTime2), CAST(N'2026-01-07T02:59:27.2022409' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (1002, NULL, N'5BC14DB44B9CCDF2FB48764BD628DD21', CAST(N'2026-01-07T09:21:42.9041466' AS DateTime2), CAST(N'2026-01-07T09:21:42.9041466' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (2003, NULL, N'2E06FF191666F8544A86369366F06656', CAST(N'2026-01-08T13:22:37.7655424' AS DateTime2), CAST(N'2026-01-08T13:22:37.7655424' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3002, NULL, N'81F645C7BE45C6AEF6CCC75678F503CD', CAST(N'2026-01-10T12:52:22.1643071' AS DateTime2), CAST(N'2026-01-10T12:52:22.1643071' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3003, NULL, N'C67F2206E8A790732AB7790E808259CC', CAST(N'2026-01-11T00:54:58.7542994' AS DateTime2), CAST(N'2026-01-11T00:54:58.7542994' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3004, NULL, N'942CD92BFFE06587DAD661AC9046C499', CAST(N'2026-01-11T12:43:47.8797983' AS DateTime2), CAST(N'2026-01-11T12:43:47.8797983' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3005, NULL, N'F278AB063624F8C8FEA75FB8839A3F8E', CAST(N'2026-01-11T13:25:38.8458289' AS DateTime2), CAST(N'2026-01-11T13:25:38.8458289' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3006, 1002, NULL, CAST(N'2026-01-11T13:26:14.9137378' AS DateTime2), CAST(N'2026-01-11T13:26:14.9137378' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3009, NULL, N'2C26C969828FBAB5BAE966931692F65A', CAST(N'2026-01-12T10:08:13.3206283' AS DateTime2), CAST(N'2026-01-12T10:08:13.3206283' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (3012, NULL, N'566C17CA9276BC34637341B4C78237CF', CAST(N'2026-01-12T10:27:38.6853451' AS DateTime2), CAST(N'2026-01-12T10:27:38.6853451' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (4009, NULL, N'C0624DF026403F584FC6E25F67AFB047', CAST(N'2026-01-14T09:56:15.0289249' AS DateTime2), CAST(N'2026-01-14T09:56:15.0289249' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (4011, NULL, N'8645C6EF61CF68DD76B4CC65B7F505DD', CAST(N'2026-01-14T10:05:52.5435213' AS DateTime2), CAST(N'2026-01-14T10:05:52.5435213' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (5009, NULL, N'1F7C5B7F63C6154DD9DE9886B4745268', CAST(N'2026-01-15T13:40:57.8615645' AS DateTime2), CAST(N'2026-01-15T13:40:57.8615645' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6009, NULL, N'97C775770EBB75F63707147C1CB1E78A', CAST(N'2026-01-18T06:20:50.0038885' AS DateTime2), CAST(N'2026-01-18T06:20:50.0038885' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6010, NULL, N'76B5EAAE6D2D271FF6AD02D1A0BB42FC', CAST(N'2026-01-18T11:39:22.1593472' AS DateTime2), CAST(N'2026-01-18T11:39:22.1593472' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6011, NULL, N'F934CF4515725F8C2986FF8A9DEE50B6', CAST(N'2026-01-18T12:10:18.3129593' AS DateTime2), CAST(N'2026-01-18T12:10:18.3129593' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6013, 1003, NULL, CAST(N'2026-01-18T12:12:25.6315104' AS DateTime2), CAST(N'2026-01-18T12:12:25.6315104' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6014, NULL, N'75506D18D10DAEF38F8CFD64085B9E17', CAST(N'2026-01-18T12:15:17.4375001' AS DateTime2), CAST(N'2026-01-18T12:15:17.4375001' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6015, NULL, N'36BE5E9B5DB4C29EF4C7373ADDAA07E8', CAST(N'2026-01-18T12:28:39.9477019' AS DateTime2), CAST(N'2026-01-18T12:28:39.9477019' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6016, NULL, N'5D65F31AA5CB0A9564B9C84D6953E6EE', CAST(N'2026-01-18T14:10:46.8216955' AS DateTime2), CAST(N'2026-01-18T14:10:46.8216955' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6017, NULL, N'4157AF77E96EB828F9D0D728114D7649', CAST(N'2026-01-18T14:18:41.8835988' AS DateTime2), CAST(N'2026-01-18T14:18:41.8835988' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6018, NULL, N'722544BB214D4E41F143202DDD175CE8', CAST(N'2026-01-18T14:31:19.0953511' AS DateTime2), CAST(N'2026-01-18T14:31:19.0953511' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6019, NULL, N'FC99B8B68CEF7255574E428A77ABFA2B', CAST(N'2026-01-18T14:39:06.6151713' AS DateTime2), CAST(N'2026-01-18T14:39:06.6151713' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6020, NULL, N'CDBEA71FEF2ECC799C950A6AECFEAD75', CAST(N'2026-01-18T15:23:48.6598857' AS DateTime2), CAST(N'2026-01-18T15:23:48.6598857' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6021, NULL, N'8A72C788CD77C6457B6000229B33ED32', CAST(N'2026-01-18T15:26:04.8979390' AS DateTime2), CAST(N'2026-01-18T15:26:04.8979390' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (6022, NULL, N'9E9C46B2333F9D64579F455F58BDF61B', CAST(N'2026-01-19T06:28:23.2108224' AS DateTime2), CAST(N'2026-01-19T06:28:23.2108224' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7009, NULL, N'2E5883F89C780EF6584C054B828478EE', CAST(N'2026-01-19T09:06:19.1885535' AS DateTime2), CAST(N'2026-01-19T09:06:19.1885535' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7010, NULL, N'D1A9F165097FA70D7D2B50D15D414190', CAST(N'2026-01-19T09:12:38.8095388' AS DateTime2), CAST(N'2026-01-19T09:12:38.8095388' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7011, NULL, N'97BBB0C526EEAD17044CAD027AF63A9D', CAST(N'2026-01-19T09:16:02.1968436' AS DateTime2), CAST(N'2026-01-19T09:16:02.1968436' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7012, NULL, N'5F4A091ABCBB80EA7268AB8976A6AE83', CAST(N'2026-01-19T09:16:14.5437712' AS DateTime2), CAST(N'2026-01-19T09:16:14.5437712' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7013, NULL, N'A1F44F112107F6E72A996EF225E532FD', CAST(N'2026-01-19T09:16:33.5020721' AS DateTime2), CAST(N'2026-01-19T09:16:33.5020721' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7014, NULL, N'B9739099293AE64ECAB02D690EDC6F7E', CAST(N'2026-01-19T09:19:15.2687557' AS DateTime2), CAST(N'2026-01-19T09:19:15.2687557' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7015, NULL, N'9DC9366CCD0B94102F5F055195CC91DD', CAST(N'2026-01-19T10:15:50.3108427' AS DateTime2), CAST(N'2026-01-19T10:15:50.3108427' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (7016, NULL, N'A18B5D61C66E67D58E4C19DB9DCAF8FB', CAST(N'2026-01-19T10:18:31.3712437' AS DateTime2), CAST(N'2026-01-19T10:18:31.3712437' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8010, NULL, N'DBF4BBF04267FF14184BC53EC7A98979', CAST(N'2026-01-20T11:11:10.1015130' AS DateTime2), CAST(N'2026-01-20T11:11:10.1015130' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8011, NULL, N'CD9918319CA1920FBD330B7058C52B2E', CAST(N'2026-01-20T11:21:58.6445690' AS DateTime2), CAST(N'2026-01-20T11:21:58.6445690' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8012, NULL, N'8F9644DF19F510BBCA600A31AB2F4E03', CAST(N'2026-01-20T11:36:26.6801077' AS DateTime2), CAST(N'2026-01-20T11:36:26.6801077' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8013, NULL, N'F2981752DCDE3E87C61566F9EC46E165', CAST(N'2026-01-20T11:42:03.0749614' AS DateTime2), CAST(N'2026-01-20T11:42:03.0749614' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8014, NULL, N'4E87AF6478C90DDF50B2680E8F5F0406', CAST(N'2026-01-20T11:42:33.6429082' AS DateTime2), CAST(N'2026-01-20T11:42:33.6429082' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8015, NULL, N'CADA7CB073985C98FB36F638E7C1D9DA', CAST(N'2026-01-20T12:07:37.8000257' AS DateTime2), CAST(N'2026-01-20T12:07:37.8000257' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8016, NULL, N'0E6F55428EDDD2BEDCF7B0B8CB3F44FC', CAST(N'2026-01-20T12:29:10.3600764' AS DateTime2), CAST(N'2026-01-20T12:29:10.3600764' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8017, NULL, N'4B2D9860E9F3F7AF033319660FD0C6B8', CAST(N'2026-01-20T12:35:56.8338467' AS DateTime2), CAST(N'2026-01-20T12:35:56.8338467' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8018, NULL, N'A83111851DED2432EC5F80233E9DAF47', CAST(N'2026-01-20T12:36:57.1625954' AS DateTime2), CAST(N'2026-01-20T12:36:57.1625954' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8019, NULL, N'A8734BB19D00A797BA5D63D15C63451A', CAST(N'2026-01-20T12:53:04.8041931' AS DateTime2), CAST(N'2026-01-20T12:53:04.8041931' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8020, NULL, N'BED097A9538AB2875A32DAFCE21CA7F6', CAST(N'2026-01-20T12:55:25.3220589' AS DateTime2), CAST(N'2026-01-20T12:55:25.3220589' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8021, NULL, N'FB70B001C0BD1EECB5C1C7C88EAD6C94', CAST(N'2026-01-20T13:08:32.1887640' AS DateTime2), CAST(N'2026-01-20T13:08:32.1887640' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8022, NULL, N'09378DBA19DF05D55ED4FEF50EA320AF', CAST(N'2026-01-20T13:11:23.1283359' AS DateTime2), CAST(N'2026-01-20T13:11:23.1283359' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8023, NULL, N'716ACEAF92054B73DDF0FE58BAED676A', CAST(N'2026-01-20T13:12:46.3461710' AS DateTime2), CAST(N'2026-01-20T13:12:46.3461710' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8024, NULL, N'A2E3AE44DA028B4FCD3EE84E4F637B4E', CAST(N'2026-01-20T13:53:07.5771386' AS DateTime2), CAST(N'2026-01-20T13:53:07.5771386' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8025, NULL, N'1485C29517DF6E4A7919A08F86ABAFF5', CAST(N'2026-01-20T13:56:38.9916744' AS DateTime2), CAST(N'2026-01-20T13:56:38.9916744' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8026, NULL, N'0E04A6866434972800D0FD2C18233760', CAST(N'2026-01-20T13:57:49.9380533' AS DateTime2), CAST(N'2026-01-20T13:57:49.9380533' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8027, NULL, N'C7FE2B47F0DAF9C092DCBA4D3B76BD78', CAST(N'2026-01-20T14:02:26.3084854' AS DateTime2), CAST(N'2026-01-20T14:02:26.3084854' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8028, NULL, N'D43353E67F87BC531E0BEE7E8C1859E5', CAST(N'2026-01-20T14:05:15.4353675' AS DateTime2), CAST(N'2026-01-20T14:05:15.4353675' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8029, NULL, N'814A297A12EE24B46C60DF17FFF7A19A', CAST(N'2026-01-20T14:08:40.9331082' AS DateTime2), CAST(N'2026-01-20T14:08:40.9331082' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8030, 3003, NULL, CAST(N'2026-01-20T14:10:30.1894712' AS DateTime2), CAST(N'2026-01-20T14:10:30.1894712' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8031, NULL, N'F503215193FF53EFCB4A5F1B2F5757A7', CAST(N'2026-01-20T14:30:15.9992088' AS DateTime2), CAST(N'2026-01-20T14:30:15.9992088' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8032, NULL, N'2DA2C89F0C20CE4209B1CB1500FB7EA1', CAST(N'2026-01-20T14:31:17.6975626' AS DateTime2), CAST(N'2026-01-20T14:31:17.6975626' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8033, NULL, N'DC85E56C154D6B1BC2C31B4B53B14424', CAST(N'2026-01-20T14:33:11.4232158' AS DateTime2), CAST(N'2026-01-20T14:33:11.4232158' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (8034, NULL, N'5FF24A6D3A25C33DCB6903B41ECAA5BF', CAST(N'2026-01-20T14:36:12.1766818' AS DateTime2), CAST(N'2026-01-20T14:36:12.1766818' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (9010, NULL, N'BD1A790FA30E9F4B9E55B2B50404A486', CAST(N'2026-01-22T13:10:23.7152463' AS DateTime2), CAST(N'2026-01-22T13:10:23.7152463' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10011, NULL, N'23EDE75D1047EDA07F19CF755C68C00F', CAST(N'2026-02-27T13:10:01.5172609' AS DateTime2), CAST(N'2026-02-27T13:10:01.5172609' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10012, 3004, NULL, CAST(N'2026-02-27T13:15:04.7147490' AS DateTime2), CAST(N'2026-02-27T13:15:04.7147490' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10013, NULL, N'F308567C9E794A4823AA117D41011D98', CAST(N'2026-02-27T13:43:21.6647504' AS DateTime2), CAST(N'2026-02-27T13:43:21.6647504' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10014, NULL, N'4090D4C92CB2A58447BE25AA4975BD77', CAST(N'2026-02-27T14:07:45.7917144' AS DateTime2), CAST(N'2026-02-27T14:07:45.7917144' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10015, NULL, N'E2C0A83BF5FF085661EEA3C1CD97BCF5', CAST(N'2026-02-27T16:35:12.5780178' AS DateTime2), CAST(N'2026-02-27T16:35:12.5780178' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (10016, NULL, N'F390C2801014E5FA63A07D2E50F00585', CAST(N'2026-02-27T16:43:27.0084423' AS DateTime2), CAST(N'2026-02-27T16:43:27.0084423' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (11009, NULL, N'3AED50F4831588C5D0D814B22D655C49', CAST(N'2026-02-28T08:03:30.7944187' AS DateTime2), CAST(N'2026-02-28T08:03:30.7944187' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13009, NULL, N'846127AB768612A7FB2A0783EDC8EF95', CAST(N'2026-03-03T10:52:16.0454361' AS DateTime2), CAST(N'2026-03-03T10:52:16.0454361' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13011, NULL, N'C6FE5BA9051ED253AFEE33AC438650C3', CAST(N'2026-03-06T08:11:08.5595452' AS DateTime2), CAST(N'2026-03-06T08:11:08.5595452' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13012, NULL, N'10CDC08D67960487215C8C079EC7A0EC', CAST(N'2026-03-06T09:25:12.8685953' AS DateTime2), CAST(N'2026-03-06T09:25:12.8685953' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13013, NULL, N'687332AAA891979CF1162F72CDFCE0A0', CAST(N'2026-03-06T12:02:18.6015858' AS DateTime2), CAST(N'2026-03-06T12:02:18.6015858' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13014, NULL, N'95AE0C7AC2535C1E041F906D1F5488AC', CAST(N'2026-03-06T12:31:33.9151860' AS DateTime2), CAST(N'2026-03-06T12:31:33.9151860' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (13015, NULL, N'15064719AED2BE5330BC74108BD9C06A', CAST(N'2026-03-06T12:57:57.7648780' AS DateTime2), CAST(N'2026-03-06T12:57:57.7648780' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (14011, NULL, N'5DB54F1DB59D01C56974BC885DA4C560', CAST(N'2026-03-09T05:39:09.3800223' AS DateTime2), CAST(N'2026-03-09T05:39:09.3800223' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (14012, NULL, N'247CF3ED12BCAD14837FAD9F951DA5AC', CAST(N'2026-03-09T05:39:25.2238482' AS DateTime2), CAST(N'2026-03-09T05:39:25.2238482' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (14013, NULL, N'E992AB245D57F200CFC08B856DB1374B', CAST(N'2026-03-09T08:36:06.5046212' AS DateTime2), CAST(N'2026-03-09T08:36:06.5046212' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (14014, NULL, N'2B05650D516066965A5C1842D3A439A0', CAST(N'2026-03-09T12:13:53.5082054' AS DateTime2), CAST(N'2026-03-09T12:13:53.5082054' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (14015, NULL, N'92CFEBE5134D32AC41826B445D69CB68', CAST(N'2026-03-10T11:28:49.0051520' AS DateTime2), CAST(N'2026-03-10T11:28:49.0051520' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (15015, NULL, N'29D4CC6C6C74601938561265C492A4F4', CAST(N'2026-03-11T05:06:41.4076923' AS DateTime2), CAST(N'2026-03-11T05:06:41.4076923' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (15016, NULL, N'2062F980C753CE25834F3B6A6C65E976', CAST(N'2026-03-11T06:22:46.4459404' AS DateTime2), CAST(N'2026-03-11T06:22:46.4459404' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16015, NULL, N'69A00FFF29B22FC816CB041864B9DAC9', CAST(N'2026-03-11T11:41:08.9031946' AS DateTime2), CAST(N'2026-03-11T11:41:08.9031946' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16017, NULL, N'15D0E6EAAA04A2B8399A4B8149E277AD', CAST(N'2026-03-11T12:42:09.1317219' AS DateTime2), CAST(N'2026-03-11T12:42:09.1317219' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16018, NULL, N'5576C0DE318409219B28827C43418B81', CAST(N'2026-03-11T12:42:27.6422131' AS DateTime2), CAST(N'2026-03-11T12:42:27.6422131' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16019, NULL, N'7C2B0114D1D655AA8600D2A822C327C5', CAST(N'2026-03-11T14:37:18.2002271' AS DateTime2), CAST(N'2026-03-11T14:37:18.2002271' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16020, NULL, N'EC333C3FBEC919F7D925909A4A973C30', CAST(N'2026-03-11T15:42:24.8022062' AS DateTime2), CAST(N'2026-03-11T15:42:24.8022062' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16021, NULL, N'D2C4799B09597B597E16CF58F72DF049', CAST(N'2026-03-11T17:24:21.8754767' AS DateTime2), CAST(N'2026-03-11T17:24:21.8754767' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (16022, NULL, N'1423548FDD0FB2E376346782AA392333', CAST(N'2026-03-11T17:25:31.8384064' AS DateTime2), CAST(N'2026-03-11T17:25:31.8384064' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17015, NULL, N'EDF011130C1DC074255A13746633BCDB', CAST(N'2026-03-12T05:24:43.5097378' AS DateTime2), CAST(N'2026-03-12T05:24:43.5097378' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17016, NULL, N'6F257AF0131D6570EC39461C99702CF3', CAST(N'2026-03-12T05:30:01.7551524' AS DateTime2), CAST(N'2026-03-12T05:30:01.7551524' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17017, NULL, N'A33654D9AA7DEF34EA866C01A83B0837', CAST(N'2026-03-12T05:57:53.7012033' AS DateTime2), CAST(N'2026-03-12T05:57:53.7012033' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17019, NULL, N'9BBBF2392923B87EAE2C4659D3E1CFE0', CAST(N'2026-03-12T06:17:08.0004520' AS DateTime2), CAST(N'2026-03-12T06:17:08.0004520' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (17021, NULL, N'EC4A6A824F99B228A3F576D144AC3956', CAST(N'2026-03-12T07:02:02.5798999' AS DateTime2), CAST(N'2026-03-12T07:02:02.5798999' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18015, NULL, N'B35958521BCD20DA522A36AF06CAEBB3', CAST(N'2026-03-12T11:21:33.3819193' AS DateTime2), CAST(N'2026-03-12T11:21:33.3819193' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18016, NULL, N'3EB11C7721880FE4C0DA31A64585B8CB', CAST(N'2026-03-12T12:14:33.2149463' AS DateTime2), CAST(N'2026-03-12T12:14:33.2149463' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18017, NULL, N'E049ABC0A58C25A2E788835047639E65', CAST(N'2026-03-12T12:16:27.2133110' AS DateTime2), CAST(N'2026-03-12T12:16:27.2133110' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18018, NULL, N'46EBAA640650AB8D895274AA7012475A', CAST(N'2026-03-12T14:05:45.5707968' AS DateTime2), CAST(N'2026-03-12T14:05:45.5707968' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18020, NULL, N'68F9D073FD0A25C6E2389217272587BF', CAST(N'2026-03-12T14:50:16.4289645' AS DateTime2), CAST(N'2026-03-12T14:50:16.4289645' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18021, NULL, N'F595ED185C9E70C96D2A8C45CF85F5BE', CAST(N'2026-03-12T15:34:18.3349464' AS DateTime2), CAST(N'2026-03-12T15:34:18.3349464' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18022, NULL, N'EB06510B13F84937C2B6913053210B76', CAST(N'2026-03-12T16:26:58.2338916' AS DateTime2), CAST(N'2026-03-12T16:26:58.2338916' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18023, NULL, N'970388E1CF21F7571324EACC463DE135', CAST(N'2026-03-12T16:50:09.5054828' AS DateTime2), CAST(N'2026-03-12T16:50:09.5054828' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18024, NULL, N'EBAAA9AA244F48985485489AEA63E378', CAST(N'2026-03-12T16:51:31.5849131' AS DateTime2), CAST(N'2026-03-12T16:51:31.5849131' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18025, NULL, N'9E30C14D6A35B314277D4AA08947696D', CAST(N'2026-03-12T16:51:57.5510351' AS DateTime2), CAST(N'2026-03-12T16:51:57.5510351' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (18026, NULL, N'EC83EAB95168BD034F481F21F301E9E0', CAST(N'2026-03-12T17:03:28.9435255' AS DateTime2), CAST(N'2026-03-12T17:03:28.9435255' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (19018, NULL, N'00B289D83BE043C75E8380D0B7A8C3B6', CAST(N'2026-03-13T02:41:19.5203465' AS DateTime2), CAST(N'2026-03-13T02:41:19.5203465' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (19019, NULL, N'709B42DCAC56BF5910B28F41215B6C2E', CAST(N'2026-03-13T03:15:31.1532653' AS DateTime2), CAST(N'2026-03-13T03:15:31.1532653' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (19020, NULL, N'2A2F23E77E3F76D183ADE3289C93DADB', CAST(N'2026-03-13T03:20:55.3290204' AS DateTime2), CAST(N'2026-03-13T03:20:55.3290204' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (19021, NULL, N'1EE98316CA29C1E20FC6D456B7BC0AA7', CAST(N'2026-03-13T03:27:14.7117986' AS DateTime2), CAST(N'2026-03-13T03:27:14.7117986' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (19022, NULL, N'728FE3ABDC3730635696E905480EF308', CAST(N'2026-03-13T04:02:28.1198539' AS DateTime2), CAST(N'2026-03-13T04:02:28.1198539' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20018, NULL, N'92883CD0BDEC017274336876412D7E1B', CAST(N'2026-03-13T04:54:24.3554625' AS DateTime2), CAST(N'2026-03-13T04:54:24.3554625' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20020, NULL, N'BC7B6FC12410495482A9BB5FEDE4AF3D', CAST(N'2026-03-13T04:58:11.2254672' AS DateTime2), CAST(N'2026-03-13T04:58:11.2254672' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20022, NULL, N'D411005E8C5436D056D682A3B9AB5A5A', CAST(N'2026-03-13T05:04:33.3431875' AS DateTime2), CAST(N'2026-03-13T05:04:33.3431875' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20023, NULL, N'68FE7A8CAFFEE8D5D1CF3A9980A2C101', CAST(N'2026-03-13T05:08:36.7601063' AS DateTime2), CAST(N'2026-03-13T05:08:36.7601063' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20024, NULL, N'91E470FF846300249CB50D39FEBFCC1C', CAST(N'2026-03-13T06:29:30.0449311' AS DateTime2), CAST(N'2026-03-13T06:29:30.0449311' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20025, NULL, N'FA8E23CD159F6F9FB2E7A62E5F382326', CAST(N'2026-03-13T06:46:03.2380249' AS DateTime2), CAST(N'2026-03-13T06:46:03.2380249' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (20026, NULL, N'686742EB47564130935E1A50E2B7942C', CAST(N'2026-03-13T06:46:45.4813825' AS DateTime2), CAST(N'2026-03-13T06:46:45.4813825' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21018, NULL, N'FC738C106DC9333129AC48BAC4E05027', CAST(N'2026-03-13T10:38:08.7164966' AS DateTime2), CAST(N'2026-03-13T10:38:08.7164966' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21019, NULL, N'09A6830E84B565F9947D7F6052128676', CAST(N'2026-03-13T12:12:20.9027605' AS DateTime2), CAST(N'2026-03-13T12:12:20.9027605' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21020, NULL, N'A656A3E90B63E78BC45A0AF008531277', CAST(N'2026-03-13T12:16:58.0066790' AS DateTime2), CAST(N'2026-03-13T12:16:58.0066790' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21024, NULL, N'C1A2A3DF82B97533F8F5075867B7BCF1', CAST(N'2026-03-13T12:37:08.2332381' AS DateTime2), CAST(N'2026-03-13T12:37:08.2332381' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21025, NULL, N'1A9A114BA818A3222844A9C2C0AD074B', CAST(N'2026-03-13T12:45:06.0401972' AS DateTime2), CAST(N'2026-03-13T12:45:06.0401972' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (21027, NULL, N'32DF56AAD37E58ED9E26F5C786DEF16F', CAST(N'2026-03-13T13:05:12.1876451' AS DateTime2), CAST(N'2026-03-13T13:05:12.1876451' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (22018, NULL, N'E0E7C6021F0BE08DCFEABD6A7AFB598D', CAST(N'2026-03-14T15:14:02.4956817' AS DateTime2), CAST(N'2026-03-14T15:14:02.4956817' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (23018, NULL, N'0613EAE796DDC08AFEC09EB8442C416E', CAST(N'2026-03-16T06:12:27.4075659' AS DateTime2), CAST(N'2026-03-16T06:12:27.4075659' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (23019, NULL, N'47D286583239CDA85F0EC65C200A93F3', CAST(N'2026-03-16T06:30:07.3501317' AS DateTime2), CAST(N'2026-03-16T06:30:07.3501317' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (24018, NULL, N'DC3A7EA449D3416A84B6F8329F9074B6', CAST(N'2026-03-16T11:35:05.5409366' AS DateTime2), CAST(N'2026-03-16T11:35:05.5409366' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25018, NULL, N'F7D78BEA06D6B7313A06B5C1AFD919C9', CAST(N'2026-03-20T14:04:11.5134647' AS DateTime2), CAST(N'2026-03-20T14:04:11.5134647' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25019, NULL, N'4E5EE3FB76F8872629438214204EE3D7', CAST(N'2026-03-20T14:07:01.4346827' AS DateTime2), CAST(N'2026-03-20T14:07:01.4346827' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25020, NULL, N'D83C2789FB57B381E50D48752D810A79', CAST(N'2026-03-20T16:08:26.3303382' AS DateTime2), CAST(N'2026-03-20T16:08:26.3303382' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25021, NULL, N'F26CFC21EB79CDE43176F362F3D606AE', CAST(N'2026-03-20T16:08:56.2316174' AS DateTime2), CAST(N'2026-03-20T16:08:56.2316174' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25025, 2, NULL, CAST(N'2026-03-20T16:13:34.8056691' AS DateTime2), CAST(N'2026-03-20T16:13:34.8056691' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25028, NULL, N'C7B26CD37AAD692CC484E4D95367EEB5', CAST(N'2026-03-20T16:45:40.0248865' AS DateTime2), CAST(N'2026-03-20T16:45:40.0248865' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (25029, NULL, N'18E0DE6A7FA3343F28D8C1B699FBB39E', CAST(N'2026-03-20T17:08:30.4471350' AS DateTime2), CAST(N'2026-03-20T17:08:30.4471350' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26018, NULL, N'EDE4E4095C5577C127E8789BDF6A01E9', CAST(N'2026-03-24T05:23:04.7552218' AS DateTime2), CAST(N'2026-03-24T05:23:04.7552218' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26019, NULL, N'3D533BB6431F774F8282608C1CC7E121', CAST(N'2026-03-24T05:41:22.3550420' AS DateTime2), CAST(N'2026-03-24T05:41:22.3550420' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26020, NULL, N'464936D63E9A8F455CB1CF7296768309', CAST(N'2026-03-24T06:06:07.0906453' AS DateTime2), CAST(N'2026-03-24T06:06:07.0906453' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26022, NULL, N'F895329827718DE1BF29DE7C98BF2A51', CAST(N'2026-03-24T07:49:07.3051886' AS DateTime2), CAST(N'2026-03-24T07:49:07.3051886' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26023, 1, NULL, CAST(N'2026-03-24T08:20:43.8785643' AS DateTime2), CAST(N'2026-03-24T08:20:43.8785643' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26024, NULL, N'3CE80EF69023CCCC6B516241373A60E8', CAST(N'2026-03-24T11:51:19.6352040' AS DateTime2), CAST(N'2026-03-24T11:51:19.6352040' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26026, NULL, N'C53ABEA67AEA1CF884B45962F7B081EB', CAST(N'2026-03-25T05:11:36.2002755' AS DateTime2), CAST(N'2026-03-25T05:11:36.2002755' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26027, NULL, N'D583074E9D478687FFF4169B5527B081', CAST(N'2026-03-27T11:37:29.8946180' AS DateTime2), CAST(N'2026-03-27T11:37:29.8946180' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26029, NULL, N'700848755C67D0D3254158A9CF386069', CAST(N'2026-03-27T13:22:04.7323264' AS DateTime2), CAST(N'2026-03-27T13:22:04.7323264' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26030, NULL, N'DE0B86A08A089CA44C00F26740F12E2A', CAST(N'2026-03-27T15:17:34.6064576' AS DateTime2), CAST(N'2026-03-27T15:17:34.6064576' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26033, NULL, N'EE849F24ED6AD06B65A42297E0C7987C', CAST(N'2026-03-28T07:15:48.6914298' AS DateTime2), CAST(N'2026-03-28T07:15:48.6914298' AS DateTime2))
GO
INSERT [dbo].[GIO_HANG] ([Id], [TaiKhoanId], [SessionId], [NgayTao], [NgayCapNhat]) VALUES (26034, 3005, NULL, CAST(N'2026-03-28T07:19:07.7558516' AS DateTime2), CAST(N'2026-03-28T07:19:07.7558516' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[GIO_HANG] OFF
GO
SET IDENTITY_INSERT [dbo].[HINH_ANH_MAU_SAC] ON 
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (7009, 3002, N'Xanh dương', N'/images/products/15b419da-f41f-4ca7-ad86-059ceacbd972.jpg', CAST(N'2026-01-19T09:56:05.3896940' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (7014, 4002, N'Đen', N'/images/products/0bbbd473-1a79-4631-8808-760ce28409b4.jpg', CAST(N'2026-01-19T10:20:00.1027177' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (9014, 2, N'Xanh dương', N'/images/products/Quan-Jeans.jpg', CAST(N'2026-02-27T16:47:08.4091490' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (10002, 5002, N'Đen', N'/images/products/aocardigan.jpeg', CAST(N'2026-03-06T08:58:24.7380657' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (10003, 5002, N'Xanh lá', N'/images/products/cadianxanh.jpeg', CAST(N'2026-03-06T08:58:24.7380657' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (10004, 5002, N'Xám', N'/images/products/cadianxam.jpeg', CAST(N'2026-03-06T08:58:24.7380657' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (10013, 5009, N'Xanh dương', N'/images/products/pasted_1772801809634_1.png', CAST(N'2026-03-06T12:56:51.1230640' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (10017, 2003, N'Đen', N'/images/products/pasted_1772802166277_1.png', CAST(N'2026-03-06T13:03:53.9815898' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (11002, 5005, N'Đen', N'/images/products/pasted_1772800181874_1.png', CAST(N'2026-03-09T12:14:41.3742846' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13004, 5007, N'Be', N'/images/products/d45e92b6-19b9-4322-94d7-1681c57eaf00.jpg', CAST(N'2026-03-12T11:22:56.5531096' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13005, 1, N'Đen', N'/images/products/d102a239-64de-4893-9624-f43afb0e61b5.jpg', CAST(N'2026-03-12T11:35:00.6778600' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13006, 1, N'Nâu', N'/images/products/ab146c91-07e4-4bac-bac3-0acd62aca275.jpg', CAST(N'2026-03-12T11:35:00.6778600' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13007, 1, N'Trắng', N'/images/products/28489c9e-fd64-43dd-a428-fff1256ac449.jpg', CAST(N'2026-03-12T11:35:00.6778600' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13024, 5003, N'Đen', N'/images/products/pasted_1773333184492_1.png', CAST(N'2026-03-12T16:33:09.2076515' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13025, 5003, N'Trắng', N'/images/products/HODDIElOOSEtRANG.jpeg', CAST(N'2026-03-12T16:33:09.2076515' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13027, 5008, N'Nâu', N'/images/products/pasted_1773333366891_1.png', CAST(N'2026-03-12T16:36:08.6044052' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (13032, 5006, N'Xám', N'/images/products/pasted_1772800838677_1.png', CAST(N'2026-03-12T17:18:11.3866773' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (14008, 1002, N'Đen', N'/images/products/e3bc591d-81e2-4bf2-b673-5a9b5e99c3e8.jpg', CAST(N'2026-03-13T03:13:55.3897334' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (14009, 1002, N'Trắng', N'/images/products/ed3f194c-cbd8-40e5-b6e3-d6614388c6ee.jpg', CAST(N'2026-03-13T03:13:55.3897334' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (14010, 1002, N'Xám', N'/images/products/65771a6e-34f6-4519-afaa-05306fb08dd3.jpg', CAST(N'2026-03-13T03:13:55.3897334' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (14013, 5004, N'Be', N'/images/products/pasted_1773335590395_1.png', CAST(N'2026-03-13T03:43:58.0860044' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (14014, 5004, N'Đen', N'/images/products/77a43b5a-fbcd-4e39-865a-0221937e995e.jpg', CAST(N'2026-03-13T03:43:58.0860044' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (15008, 6002, N'Trắng', N'/images/products/pasted_1773399163717_1.png', CAST(N'2026-03-13T10:56:03.2776207' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (15009, 6003, N'Đen', N'/images/products/15b419da-f41f-4ca7-ad86-059ceacbd972.jpg', CAST(N'2026-03-13T12:31:03.9274706' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (15010, 2002, N'Nâu', N'/images/products/ec9bd54b-a38d-41bc-84e8-7b45dce8dbd3.jpg', CAST(N'2026-03-13T12:32:02.9654079' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_MAU_SAC] ([Id], [SanPhamId], [MauSac], [DuongDanAnh], [NgayTao]) VALUES (15011, 2002, N'Đen', N'/images/products/77a43b5a-fbcd-4e39-865a-0221937e995e.jpg', CAST(N'2026-03-13T12:32:02.9654079' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[HINH_ANH_MAU_SAC] OFF
GO
SET IDENTITY_INSERT [dbo].[HINH_ANH_SAN_PHAM] ON 
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (7009, 3002, N'/images/products/5a1545c1-3872-4aeb-ab94-0b472f8648e1.jpg', 1, 1, CAST(N'2026-01-19T09:56:05.3847106' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (7015, 4002, N'/images/products/0bbbd473-1a79-4631-8808-760ce28409b4.jpg', 1, 1, CAST(N'2026-01-19T10:20:00.0897608' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (9014, 2, N'/images/products/Quan-Jeans.jpg', 1, 1, CAST(N'2026-02-27T16:47:08.3988170' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (10004, 5002, N'/images/products/aocardigan.jpeg', 1, 1, CAST(N'2026-03-06T08:58:24.7310670' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (10028, 5009, N'/images/products/pasted_1772801806563_1.png', 1, 1, CAST(N'2026-03-06T12:56:51.1180699' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (10032, 2003, N'/images/products/pasted_1772802151819_1.png', 1, 1, CAST(N'2026-03-06T13:03:53.9745920' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (10033, 2003, N'/images/products/pasted_1772802090448_1.png', 0, 2, CAST(N'2026-03-06T13:03:53.9745920' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (10034, 2003, N'/images/products/pasted_1772802126255_1.png', 0, 3, CAST(N'2026-03-06T13:03:53.9745920' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (11002, 5005, N'/images/products/pasted_1772799077860_1.png', 1, 1, CAST(N'2026-03-09T12:14:41.3582820' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13004, 5007, N'/images/products/d45e92b6-19b9-4322-94d7-1681c57eaf00.jpg', 1, 1, CAST(N'2026-03-12T11:22:56.5411502' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13005, 1, N'/images/products/3e640574-1a8a-4e76-9bb2-aea59ac57e8a.jpg', 1, 1, CAST(N'2026-03-12T11:35:00.6668965' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13006, 1, N'/images/products/53cde30b-add1-49f1-83b3-a86fcea57f22.jpg', 0, 2, CAST(N'2026-03-12T11:35:00.6668965' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13007, 1, N'/images/products/ab7692f9-ea54-42f2-81d8-572ff6fddd78.jpg', 0, 3, CAST(N'2026-03-12T11:35:00.6668965' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13016, 5003, N'/images/products/pasted_1773333181622_1.png', 1, 1, CAST(N'2026-03-12T16:33:09.1986559' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13018, 5008, N'/images/products/pasted_1773333365185_1.png', 1, 1, CAST(N'2026-03-12T16:36:08.5964043' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (13023, 5006, N'/images/products/pasted_1772800838677_1.png', 1, 2, CAST(N'2026-03-12T17:18:11.3806769' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (14008, 1002, N'/images/products/8c1d67dd-a851-4728-bb7f-6524230ebd75.jpg', 1, 1, CAST(N'2026-03-13T03:13:55.3737306' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (14009, 1002, N'/images/products/aophaoxam.jpg', 0, 4, CAST(N'2026-03-13T03:13:55.3737306' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (14010, 1002, N'/images/products/d45e92b6-19b9-4322-94d7-1681c57eaf00.jpg', 0, 5, CAST(N'2026-03-13T03:13:55.3737306' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (14013, 5004, N'/images/products/pasted_1773335588642_1.png', 1, 1, CAST(N'2026-03-13T03:43:58.0720062' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15008, 6002, N'/images/products/pasted_1773399173879_1.png', 1, 1, CAST(N'2026-03-13T10:56:03.2766893' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15009, 6002, N'/images/products/pasted_1773399186072_1.png', 0, 2, CAST(N'2026-03-13T10:56:03.2766893' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15010, 6002, N'/images/products/pasted_1773399199707_1.png', 0, 3, CAST(N'2026-03-13T10:56:03.2766893' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15011, 6003, N'/images/products/15b419da-f41f-4ca7-ad86-059ceacbd972.jpg', 1, 1, CAST(N'2026-03-13T12:31:03.9274706' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15012, 2002, N'/images/products/9165a77f-3bb1-4a67-976a-53ada22607c4.jpg', 1, 1, CAST(N'2026-03-13T12:32:02.9476415' AS DateTime2))
GO
INSERT [dbo].[HINH_ANH_SAN_PHAM] ([Id], [SanPhamId], [DuongDanAnh], [LaAnhChinh], [ThuTu], [NgayTao]) VALUES (15013, 2002, N'/images/products/9cc41919-7f8a-4670-bbde-81d20c661946.jpg', 0, 2, CAST(N'2026-03-13T12:32:02.9476415' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[HINH_ANH_SAN_PHAM] OFF
GO
SET IDENTITY_INSERT [dbo].[LICH_SU_NHAP_KHO] ON 
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1, 13, 20, CAST(N'2026-02-27T17:06:43.1212453' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (2, 13, 260, CAST(N'2026-02-27T17:06:51.4568981' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (3, 3, 23, CAST(N'2026-02-27T17:15:07.2429028' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (4, 3, 44, CAST(N'2026-02-27T17:15:09.5823567' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (5, 3, 55, CAST(N'2026-02-27T17:15:11.6140335' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (6, 4, 111, CAST(N'2026-02-27T17:15:30.2541185' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (7, 13, 12, CAST(N'2026-02-27T17:21:28.5904826' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1002, 2, 111, CAST(N'2026-03-03T10:52:58.5887908' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1003, 5, 222, CAST(N'2026-03-03T10:53:19.8072607' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1004, 3004, 111, CAST(N'2026-03-03T10:53:39.3982762' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1005, 3005, 111, CAST(N'2026-03-03T10:53:51.3544384' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1006, 5003, 12, CAST(N'2026-03-03T10:54:05.8084340' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1007, 4003, 99, CAST(N'2026-03-03T10:54:39.0188885' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1008, 4003, 99, CAST(N'2026-03-03T10:54:42.5070360' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1009, 4005, 150, CAST(N'2026-03-03T10:54:49.6276432' AS DateTime2), N'admin')
GO
INSERT [dbo].[LICH_SU_NHAP_KHO] ([Id], [BienTheSanPhamId], [SoLuongNhap], [ThoiGian], [AdminThucHien]) VALUES (1010, 4002, 234, CAST(N'2026-03-03T10:55:01.2757078' AS DateTime2), N'admin')
GO
SET IDENTITY_INSERT [dbo].[LICH_SU_NHAP_KHO] OFF
GO
SET IDENTITY_INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ON 
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (1, 1, 2, 3004, CAST(N'2026-01-11T13:17:04.1626364' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (2002, 2, 3003, 6005, CAST(N'2026-01-20T13:02:23.0511811' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (3002, 1002, NULL, 7005, CAST(N'2026-01-22T13:09:55.0868003' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (4002, 1, 2, 8008, CAST(N'2026-02-27T14:09:42.0581793' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (5002, 1, 2, 11005, CAST(N'2026-03-11T11:51:23.6868537' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (6002, 2003, 2, 12005, CAST(N'2026-03-12T05:59:03.7420204' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (6003, 2003, 2, 12006, CAST(N'2026-03-12T06:00:02.7452888' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (7002, 1, 2, 13005, CAST(N'2026-03-12T14:06:42.9926114' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (8002, 2003, 3005, 14005, CAST(N'2026-03-13T05:01:23.9704651' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (8003, 2003, 3005, 14006, CAST(N'2026-03-13T06:31:20.6162097' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (9002, 2003, 1, 15005, CAST(N'2026-03-13T12:13:10.4278466' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (9003, 2003, 3005, 15009, CAST(N'2026-03-13T12:26:28.5441700' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (9004, 2003, 3005, 15011, CAST(N'2026-03-13T12:34:38.3931460' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (9005, 2003, 1, 15012, CAST(N'2026-03-13T12:54:47.0530989' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (10002, 1, 1, 16005, CAST(N'2026-03-14T15:24:20.4681160' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (11002, 1, 1, 17006, CAST(N'2026-03-16T11:52:11.2762635' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (11003, 1, 3005, 17007, CAST(N'2026-03-16T12:45:29.6743997' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (12002, 1, 1, 18007, CAST(N'2026-03-20T14:08:14.4001196' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (12003, 1, 2, 18008, CAST(N'2026-03-20T16:11:49.0167493' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (12004, 1, 3005, 18009, CAST(N'2026-03-20T16:11:50.4834103' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (12005, 1003, 2, 18010, CAST(N'2026-03-20T16:12:52.1298868' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (12006, 1, 3005, 18012, CAST(N'2026-03-20T16:14:02.4222030' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (13002, 1, 3005, 19008, CAST(N'2026-03-24T07:09:26.1695886' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (13003, 1003, 3005, 19010, CAST(N'2026-03-25T05:14:25.0831908' AS DateTime2))
GO
INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ([Id], [MaGiamGiaId], [TaiKhoanId], [DonHangId], [ThoiGianSuDung]) VALUES (13004, 1, 3005, 19011, CAST(N'2026-03-27T11:55:21.1539317' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] OFF
GO
SET IDENTITY_INSERT [dbo].[MA_GIAM_GIA] ON 
GO
INSERT [dbo].[MA_GIAM_GIA] ([Id], [Ma], [Loai], [GiaTri], [GiaTriToiDa], [DonToiThieu], [SoLuongToiDa], [SoLuongDaDung], [BatDauLuc], [KetThucLuc], [TrangThai]) VALUES (1, N'GG10KK1', N'FIXED', CAST(15000.00 AS Decimal(18, 2)), NULL, CAST(200000.00 AS Decimal(18, 2)), 1000, 9, CAST(N'2026-01-04T22:59:00.0000000' AS DateTime2), CAST(N'2026-10-13T22:59:00.0000000' AS DateTime2), N'ACTIVE')
GO
INSERT [dbo].[MA_GIAM_GIA] ([Id], [Ma], [Loai], [GiaTri], [GiaTriToiDa], [DonToiThieu], [SoLuongToiDa], [SoLuongDaDung], [BatDauLuc], [KetThucLuc], [TrangThai]) VALUES (2, N'GIAM50K', N'FIXED', CAST(50000.00 AS Decimal(18, 2)), NULL, CAST(500000.00 AS Decimal(18, 2)), 300, 0, CAST(N'2026-01-04T22:59:00.0000000' AS DateTime2), CAST(N'2026-02-26T22:59:00.0000000' AS DateTime2), N'ACTIVE')
GO
INSERT [dbo].[MA_GIAM_GIA] ([Id], [Ma], [Loai], [GiaTri], [GiaTriToiDa], [DonToiThieu], [SoLuongToiDa], [SoLuongDaDung], [BatDauLuc], [KetThucLuc], [TrangThai]) VALUES (1002, N'KAN2W', N'FIXED', CAST(20000.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)), CAST(2000000.00 AS Decimal(18, 2)), 12, 0, CAST(N'2026-01-11T13:31:00.0000000' AS DateTime2), CAST(N'2026-02-26T13:31:00.0000000' AS DateTime2), N'ACTIVE')
GO
INSERT [dbo].[MA_GIAM_GIA] ([Id], [Ma], [Loai], [GiaTri], [GiaTriToiDa], [DonToiThieu], [SoLuongToiDa], [SoLuongDaDung], [BatDauLuc], [KetThucLuc], [TrangThai]) VALUES (1003, N'ASM1', N'PERCENT', CAST(25.00 AS Decimal(18, 2)), NULL, CAST(5000000.00 AS Decimal(18, 2)), 12, 2, CAST(N'2026-02-27T13:51:00.0000000' AS DateTime2), CAST(N'2026-12-27T13:51:00.0000000' AS DateTime2), N'ACTIVE')
GO
INSERT [dbo].[MA_GIAM_GIA] ([Id], [Ma], [Loai], [GiaTri], [GiaTriToiDa], [DonToiThieu], [SoLuongToiDa], [SoLuongDaDung], [BatDauLuc], [KetThucLuc], [TrangThai]) VALUES (2003, N'HJASJ12', N'PERCENT', CAST(15.00 AS Decimal(18, 2)), CAST(3000000.00 AS Decimal(18, 2)), CAST(20000000.00 AS Decimal(18, 2)), 15, 0, CAST(N'2026-03-11T12:25:00.0000000' AS DateTime2), CAST(N'2026-03-15T12:25:00.0000000' AS DateTime2), N'ACTIVE')
GO
SET IDENTITY_INSERT [dbo].[MA_GIAM_GIA] OFF
GO
SET IDENTITY_INSERT [dbo].[PHUONG_THUC_THANH_TOAN] ON 
GO
INSERT [dbo].[PHUONG_THUC_THANH_TOAN] ([Id], [Ma], [Ten], [TrangThai], [NgayTao]) VALUES (1, N'COD', N'Thanh toan khi nhan hang', N'ACTIVE', CAST(N'2026-01-04T22:09:34.9807547' AS DateTime2))
GO
INSERT [dbo].[PHUONG_THUC_THANH_TOAN] ([Id], [Ma], [Ten], [TrangThai], [NgayTao]) VALUES (2, N'BANK', N'Chuyen khoan ngan hang', N'ACTIVE', CAST(N'2026-01-04T22:09:34.9807547' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[PHUONG_THUC_THANH_TOAN] OFF
GO
SET IDENTITY_INSERT [dbo].[RESET_PASSWORD_TOKEN] ON 
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (1, N'95f2e12b9f6b478bb4c491a297b79d53', 3003, CAST(N'2026-01-20T18:30:06.1556176' AS DateTime2), 1, CAST(N'2026-01-20T18:15:07.5301642' AS DateTime2), CAST(N'2026-01-20T18:15:06.1556176' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (2, N'c0010cf4e14c48279818d2b426bfacd2', 3003, CAST(N'2026-01-20T18:30:10.1425148' AS DateTime2), 1, CAST(N'2026-01-20T18:15:40.5405802' AS DateTime2), CAST(N'2026-01-20T18:15:10.1425148' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (3, N'7be17c758ea449c4be9780494557255f', 3003, CAST(N'2026-01-20T18:30:40.5445709' AS DateTime2), 1, CAST(N'2026-01-20T18:20:38.7373021' AS DateTime2), CAST(N'2026-01-20T18:15:40.5445709' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (4, N'bd602e19662847359cbfbe74706c0d3d', 3003, CAST(N'2026-01-20T18:35:38.7422852' AS DateTime2), 1, CAST(N'2026-01-20T18:28:18.5115703' AS DateTime2), CAST(N'2026-01-20T18:20:38.7422852' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (5, N'ac96eddd1f3e4e3dbb465b0508678b06', 3003, CAST(N'2026-01-20T18:46:23.3652756' AS DateTime2), 1, CAST(N'2026-01-20T18:31:52.0939964' AS DateTime2), CAST(N'2026-01-20T18:31:23.3652756' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (10002, N'180ffafa35db431bb3e9465fadc086c2', 3003, CAST(N'2026-01-22T20:28:13.9096681' AS DateTime2), 1, CAST(N'2026-01-22T20:13:47.3267972' AS DateTime2), CAST(N'2026-01-22T20:13:13.9096681' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (20002, N'ab7499778f9e459c980c6f44fb4c673b', 3005, CAST(N'2026-03-13T12:11:01.2441457' AS DateTime2), 1, CAST(N'2026-03-13T11:57:01.4439744' AS DateTime2), CAST(N'2026-03-13T11:56:01.2441457' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (30002, N'bf02db09005945cdb36c1371f1a81b57', 3003, CAST(N'2026-03-13T19:51:11.8685646' AS DateTime2), 1, CAST(N'2026-03-13T19:36:51.0560423' AS DateTime2), CAST(N'2026-03-13T19:36:11.8685646' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (40002, N'db2fbb7114c34f1d9e45bd6d8c3a7750', 3003, CAST(N'2026-03-28T14:33:50.9756393' AS DateTime2), 1, CAST(N'2026-03-28T14:18:53.5417875' AS DateTime2), CAST(N'2026-03-28T14:18:50.9756393' AS DateTime2))
GO
INSERT [dbo].[RESET_PASSWORD_TOKEN] ([Id], [Token], [TaiKhoanId], [ExpiresAt], [Used], [UsedAt], [CreatedAt]) VALUES (40003, N'9792a71554274f2697695f24181f62d7', 3003, CAST(N'2026-03-28T14:33:55.9338695' AS DateTime2), 0, NULL, CAST(N'2026-03-28T14:18:55.9338695' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[RESET_PASSWORD_TOKEN] OFF
GO
SET IDENTITY_INSERT [dbo].[SAN_PHAM] ON 
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (1, N'SP000001', N'Áo nỉ Fitted', 1, 1007, N'Ao ni fitted, form dep, mac thoai mai.', N'', N'Ni', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-04T22:59:43.4354711' AS DateTime2), CAST(N'2026-03-12T11:35:00.6868348' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (2, N'SP000002', N'Quần Jean SlimFit', 2, 4, N'Quan jean slim, co gian nhe.', N'', N'Denim', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-04T22:59:43.4354711' AS DateTime2), CAST(N'2026-02-27T16:47:08.4148061' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (1002, N'AO_PHAO_AKP', N'Áo Phao Atino Jacket', 1, 1002, N'Áo phao ấm áp với thiết kế tối giản và hiện đại', N'Áo phao cao cấp từ Atino với chất liệu bông 100% giữ ấm tuyệt vời. Thiết kế tối giản với màu đen cổ điển, phù hợp với mọi phong cách. Có móc túi tiện lợi và mũ có thể tháo rời. Lót bên trong mềm mại và thoáng khí.', N'100% Polyester', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-05T18:56:50.5800000' AS DateTime2), CAST(N'2026-03-13T03:13:55.4057291' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (2002, N'SP13', N'Áo Len Frence', NULL, 1006, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-06T09:11:35.1031396' AS DateTime2), CAST(N'2026-03-13T12:32:02.9713875' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (2003, N'Giay_Van_Beo', N'Giày Vans Skool', NULL, 2004, N'Giày Vans Thể Thảo phù hợp mọi giởi tính và lứa tuổi', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-06T09:30:13.8509461' AS DateTime2), CAST(N'2026-03-06T13:03:53.9875932' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (3002, N'SP14', N'Áo Khoác Denim', NULL, 1002, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-01-09T12:58:28.3419477' AS DateTime2), CAST(N'2026-01-19T09:56:05.3926841' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (4002, N'KL12', N'Áo Sơ Mi Dài Regular', NULL, 1013, N'', N'', N'Denim', N'NAM', N'ACTIVE', 0, CAST(N'2026-01-19T10:04:29.1729127' AS DateTime2), CAST(N'2026-01-19T10:20:00.1126854' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5002, N'ATINO_CARDIGAN_524', N'Áo Cardigan Regular', 1, 1009, N'', N'', N'Ni', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-06T08:58:24.7478458' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5003, N'ATINO_HOODIE_6982', N'Áo Hoodie Loose', 1, 1005, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-12T16:33:09.2146510' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5004, N'ATINO_SWEATSHIRT_7862', N'Áo Nỉ Fitted', 1, 1007, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-13T03:43:58.0983388' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5005, N'ATINO_JACKET_8310', N'Áo Khoác', 1, 1002, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-09T12:14:41.3822838' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5006, N'ATINO_JACKET_8931', N'Áo Jacket XL', 1, 1002, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-12T17:18:11.3896763' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5007, N'ATINO_PHAO_8561', N'Áo Phao', 1, 1002, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-12T11:22:56.5620800' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5008, N'ATINO_JACKET_8893', N'Áo Jacket', 1, 1002, N'', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T15:43:25.7333333' AS DateTime2), CAST(N'2026-03-12T16:36:08.6094047' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (5009, N'CHUCK70S', N'Chuck 70s High Navy', NULL, 3002, N'Converse 1970s là 1 trong những dòng sản phẩm bán chạy nhất của Converse.

Sunflower là một trong những phối màu hot nhất của dòng Converse 1970s, rất đẹp và dễ phối đồ, đồng thời có 2 bản là cao cổ và thấp cổ,', N'', N'', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-06T12:56:01.5948819' AS DateTime2), CAST(N'2026-03-06T12:56:51.1260685' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (6002, N'CVER05', N'Chuck Taylor All Star', NULL, 3002, N'Chucks get playful with metallic studs.', N'Chuck Taylor All Star - Product Details
POP OUT

Sometimes your ‘fit needs a little something extra. Mini metallic studs add just the touch you’re looking for.

Features and Benefits
Canvas upper for that classic Chucks look and feel
OrthoLite cushioning helps provide optimal underfoot comfort
Mini metallic studs add a pop of color
Diamond-pattern rubber outsole
Iconic Chuck Taylor tongue patch and All Star license plate

Note that this is a unisex product that follows Men''s Sizing, therefore please refer to the provided size chart below for your size reference', N'Vải', N'UNISEX', N'ACTIVE', 0, CAST(N'2026-03-13T10:56:03.2477186' AS DateTime2), CAST(N'2026-03-13T10:56:03.2507101' AS DateTime2))
GO
INSERT [dbo].[SAN_PHAM] ([Id], [MaSanPham], [Ten], [ThuongHieuId], [DanhMucId], [MoTaNgan], [MoTa], [ChatLieu], [GioiTinh], [TrangThai], [DaXoa], [NgayTao], [NgayCapNhat]) VALUES (6003, N'JENAE1', N'Quan jean slim basic', NULL, 4, N'', N'', N'', N'UNISEX', N'INACTIVE', 1, CAST(N'2026-03-13T12:31:03.9163971' AS DateTime2), CAST(N'2026-03-13T12:48:59.5209387' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[SAN_PHAM] OFF
GO
SET IDENTITY_INSERT [dbo].[TAI_KHOAN] ON 
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (1, N'admin', N'123456', N'Chủ Shop Đẹp Zai', N'admin@hancos.vn', N'0900000000', N'ACTIVE', CAST(N'2026-01-06T14:38:37.3096586' AS DateTime2), CAST(N'2026-01-06T20:33:02.5125136' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (2, N'khach01', N'123456', N'Tran Duc Hai', N'khach01@gmail.com', N'0911111111', N'ACTIVE', CAST(N'2026-01-06T14:38:37.3096586' AS DateTime2), CAST(N'2026-01-20T17:35:00.0197628' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (1002, N'nhanvien', N'$2a$10$dWoNEoh4GlDkbNlTQjZkNOds4BPF4.32NUZjOmZgpDfRHZc6ipTrK', N'Nhân viên demo', N'nhanvien@gmail.com', N'0979789847', N'ACTIVE', CAST(N'2026-01-11T19:43:29.2687023' AS DateTime2), CAST(N'2026-01-20T17:42:22.6801493' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (1003, N'staff', N'$2a$10$7C4GLBP90P6PkP2Ix3ywj.0dZzj2rOVNdGqXL4TfDLMsFFVhOE39C', N'Nhân viên demo', N'staff@hancos.com', N'', N'ACTIVE', CAST(N'2026-01-18T19:08:43.6383793' AS DateTime2), CAST(N'2026-01-20T17:42:26.1798794' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (3003, N'Duchai08', N'$2a$10$ELGlqA4bEJVPzt8l5.5X6OApY5T2u2ADb1C5yOApcMY9ApRBoDeeq', N'Trần Đức Hải', N'duchai.40net@gmail.com', N'0979789847', N'ACTIVE', CAST(N'2026-01-20T17:43:27.5193665' AS DateTime2), CAST(N'2026-03-13T19:36:51.0570395' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (3004, N'user', N'$2a$10$rcGfrgSRcRwSuS2f6N9xZuC3kWZvN5ajpi0eYJnYPdUIYaXgAOq9y', N'Người dùng demo', N'user@gmail.com', N'', N'ACTIVE', CAST(N'2026-01-20T17:51:41.3669935' AS DateTime2), CAST(N'2026-03-28T14:29:07.3892651' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN] ([Id], [TenDangNhap], [MatKhauHash], [HoTen], [Email], [SoDienThoai], [TrangThai], [NgayTao], [NgayCapNhat]) VALUES (3005, N'LyBach10z', N'$2a$10$NJ5u6EnhXmQvuG5kcDqd4u6MCNH0nysSGtxg/874vzSA6ia4P.DQ.', N'LyBach', N'duchai.20net@gmail.com', N'0585816963', N'ACTIVE', CAST(N'2026-03-13T11:55:38.1967371' AS DateTime2), CAST(N'2026-03-13T11:58:00.8060776' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[TAI_KHOAN] OFF
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (1, 1, CAST(N'2026-01-06T14:38:37.3096586' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (2, 2004, CAST(N'2026-01-20T17:35:00.0245010' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (1002, 2003, CAST(N'2026-01-20T17:42:22.6851327' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (1003, 2003, CAST(N'2026-01-20T17:42:26.1838660' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (3003, 2003, CAST(N'2026-01-20T21:29:34.7663027' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (3004, 2004, CAST(N'2026-03-28T14:29:07.3932494' AS DateTime2))
GO
INSERT [dbo].[TAI_KHOAN_VAI_TRO] ([TaiKhoanId], [VaiTroId], [NgayTao]) VALUES (3005, 2004, CAST(N'2026-03-13T11:55:38.2156725' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[THUONG_HIEU] ON 
GO
INSERT [dbo].[THUONG_HIEU] ([Id], [Ma], [Ten], [QuocGia], [NgayTao]) VALUES (1, N'ATINO', N'Atino', N'Viet Nam', CAST(N'2026-01-04T22:59:43.4344443' AS DateTime2))
GO
INSERT [dbo].[THUONG_HIEU] ([Id], [Ma], [Ten], [QuocGia], [NgayTao]) VALUES (2, N'NO_BRAND', N'No Brand', NULL, CAST(N'2026-01-04T22:59:43.4349580' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[THUONG_HIEU] OFF
GO
SET IDENTITY_INSERT [dbo].[VAI_TRO] ON 
GO
INSERT [dbo].[VAI_TRO] ([Id], [Ma], [Ten], [NgayTao]) VALUES (1, N'ADMIN', N'Quan tri', CAST(N'2026-01-04T22:09:34.9797465' AS DateTime2))
GO
INSERT [dbo].[VAI_TRO] ([Id], [Ma], [Ten], [NgayTao]) VALUES (2003, N'STAFF', N'Nhân viên', CAST(N'2026-01-18T19:08:43.5588656' AS DateTime2))
GO
INSERT [dbo].[VAI_TRO] ([Id], [Ma], [Ten], [NgayTao]) VALUES (2004, N'CUSTOMER', N'Khách hàng', CAST(N'2026-01-18T19:08:43.5618501' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[VAI_TRO] OFF
GO
SET IDENTITY_INSERT [dbo].[YEU_CAU_DOI_TRA] ON 
GO
INSERT [dbo].[YEU_CAU_DOI_TRA] ([Id], [DonHangId], [TaiKhoanId], [LyDo], [AnhMinhChung], [TrangThai], [NgayTao]) VALUES (1, 5, 2, N'Không vừa size', NULL, N'REJECTED', CAST(N'2026-03-12T06:13:57.4578796' AS DateTime2))
GO
INSERT [dbo].[YEU_CAU_DOI_TRA] ([Id], [DonHangId], [TaiKhoanId], [LyDo], [AnhMinhChung], [TrangThai], [NgayTao]) VALUES (2, 15010, 3005, N'Không vừa size', NULL, N'REJECTED', CAST(N'2026-03-13T12:29:02.9939387' AS DateTime2))
GO
INSERT [dbo].[YEU_CAU_DOI_TRA] ([Id], [DonHangId], [TaiKhoanId], [LyDo], [AnhMinhChung], [TrangThai], [NgayTao]) VALUES (1002, 15008, 1, N'Sản phẩm lỗi / hư hỏng', NULL, N'APPROVED', CAST(N'2026-03-16T11:49:58.5946479' AS DateTime2))
GO
INSERT [dbo].[YEU_CAU_DOI_TRA] ([Id], [DonHangId], [TaiKhoanId], [LyDo], [AnhMinhChung], [TrangThai], [NgayTao]) VALUES (1003, 17006, 1, N'Sản phẩm lỗi / hư hỏng', NULL, N'APPROVED', CAST(N'2026-03-16T11:52:52.0145779' AS DateTime2))
GO
SET IDENTITY_INSERT [dbo].[YEU_CAU_DOI_TRA] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_BT_SKU]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] ADD  CONSTRAINT [UQ_BT_SKU] UNIQUE NONCLUSTERED 
(
	[MaSKU] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_BT_SP_MAU_SIZE]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] ADD  CONSTRAINT [UQ_BT_SP_MAU_SIZE] UNIQUE NONCLUSTERED 
(
	[SanPhamId] ASC,
	[MauSac] ASC,
	[KichCo] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_BT_SanPhamId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_BT_SanPhamId] ON [dbo].[BIEN_THE_SAN_PHAM]
(
	[SanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_CTDH_DonHangId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_CTDH_DonHangId] ON [dbo].[CHI_TIET_DON_HANG]
(
	[DonHangId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_CTGH_GH_BT]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] ADD  CONSTRAINT [UQ_CTGH_GH_BT] UNIQUE NONCLUSTERED 
(
	[GioHangId] ASC,
	[BienTheSanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_CTGH_GioHangId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_CTGH_GioHangId] ON [dbo].[CHI_TIET_GIO_HANG]
(
	[GioHangId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_DANH_MUC_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[DANH_MUC] ADD  CONSTRAINT [UQ_DANH_MUC_Ma] UNIQUE NONCLUSTERED 
(
	[Ma] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_DCGH_TaiKhoanId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_DCGH_TaiKhoanId] ON [dbo].[DIA_CHI_GIAO_HANG]
(
	[TaiKhoanId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_DH_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[DON_HANG] ADD  CONSTRAINT [UQ_DH_Ma] UNIQUE NONCLUSTERED 
(
	[MaDonHang] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_DH_TaiKhoanId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_DH_TaiKhoanId] ON [dbo].[DON_HANG]
(
	[TaiKhoanId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_GDTT_DonHangId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_GDTT_DonHangId] ON [dbo].[GIAO_DICH_THANH_TOAN]
(
	[DonHangId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_GDTK_BienTheId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_GDTK_BienTheId] ON [dbo].[GIAO_DICH_TON_KHO]
(
	[BienTheSanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_HAMS_SP_MAU]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[HINH_ANH_MAU_SAC] ADD  CONSTRAINT [UQ_HAMS_SP_MAU] UNIQUE NONCLUSTERED 
(
	[SanPhamId] ASC,
	[MauSac] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_HAMS_SanPhamId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_HAMS_SanPhamId] ON [dbo].[HINH_ANH_MAU_SAC]
(
	[SanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_HASP_SanPhamId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_HASP_SanPhamId] ON [dbo].[HINH_ANH_SAN_PHAM]
(
	[SanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_MGG_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[MA_GIAM_GIA] ADD  CONSTRAINT [UQ_MGG_Ma] UNIQUE NONCLUSTERED 
(
	[Ma] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_PTTT_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[PHUONG_THUC_THANH_TOAN] ADD  CONSTRAINT [UQ_PTTT_Ma] UNIQUE NONCLUSTERED 
(
	[Ma] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__RESET_PA__1EB4F8173BA3E5F4]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[RESET_PASSWORD_TOKEN] ADD UNIQUE NONCLUSTERED 
(
	[Token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_RESET_PASSWORD_TOKEN_TAIKHOAN]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_RESET_PASSWORD_TOKEN_TAIKHOAN] ON [dbo].[RESET_PASSWORD_TOKEN]
(
	[TaiKhoanId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_SAN_PHAM_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[SAN_PHAM] ADD  CONSTRAINT [UQ_SAN_PHAM_Ma] UNIQUE NONCLUSTERED 
(
	[MaSanPham] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_SP_DanhMucId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_SP_DanhMucId] ON [dbo].[SAN_PHAM]
(
	[DanhMucId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_SP_ThuongHieuId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_SP_ThuongHieuId] ON [dbo].[SAN_PHAM]
(
	[ThuongHieuId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_TAI_KHOAN_Email]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[TAI_KHOAN] ADD  CONSTRAINT [UQ_TAI_KHOAN_Email] UNIQUE NONCLUSTERED 
(
	[Email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_TAI_KHOAN_TenDangNhap]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[TAI_KHOAN] ADD  CONSTRAINT [UQ_TAI_KHOAN_TenDangNhap] UNIQUE NONCLUSTERED 
(
	[TenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_TK_VT]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO] ADD  CONSTRAINT [UQ_TK_VT] UNIQUE NONCLUSTERED 
(
	[TaiKhoanId] ASC,
	[VaiTroId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_THUONG_HIEU_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[THUONG_HIEU] ADD  CONSTRAINT [UQ_THUONG_HIEU_Ma] UNIQUE NONCLUSTERED 
(
	[Ma] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_VAI_TRO_Ma]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[VAI_TRO] ADD  CONSTRAINT [UQ_VAI_TRO_Ma] UNIQUE NONCLUSTERED 
(
	[Ma] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [IX_VC_DonHangId]    Script Date: 3/28/2026 2:42:00 PM ******/
CREATE NONCLUSTERED INDEX [IX_VC_DonHangId] ON [dbo].[VAN_CHUYEN]
(
	[DonHangId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ_YTSP]    Script Date: 3/28/2026 2:42:00 PM ******/
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM] ADD  CONSTRAINT [UQ_YTSP] UNIQUE NONCLUSTERED 
(
	[TaiKhoanId] ASC,
	[SanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[DANH_MUC] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[DANH_MUC] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[DIA_CHI_GIAO_HANG] ADD  DEFAULT ((0)) FOR [LaMacDinh]
GO
ALTER TABLE [dbo].[DIA_CHI_GIAO_HANG] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT (N'PENDING_PAYMENT') FOR [TrangThai]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT ((0)) FOR [TamTinh]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT ((0)) FOR [GiamGia]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT ((0)) FOR [PhiVanChuyen]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT ((0)) FOR [TongTien]
GO
ALTER TABLE [dbo].[DON_HANG] ADD  DEFAULT (sysdatetime()) FOR [NgayDat]
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN] ADD  DEFAULT (N'PENDING') FOR [TrangThai]
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[GIO_HANG] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[HINH_ANH_MAU_SAC] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[HINH_ANH_SAN_PHAM] ADD  DEFAULT ((0)) FOR [LaAnhChinh]
GO
ALTER TABLE [dbo].[HINH_ANH_SAN_PHAM] ADD  DEFAULT ((0)) FOR [ThuTu]
GO
ALTER TABLE [dbo].[HINH_ANH_SAN_PHAM] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[LICH_SU_NHAP_KHO] ADD  CONSTRAINT [DF_LICH_SU_NHAP_KHO_ThoiGian]  DEFAULT (sysdatetime()) FOR [ThoiGian]
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] ADD  DEFAULT (sysdatetime()) FOR [ThoiGianSuDung]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] ADD  DEFAULT ((0)) FOR [SoLuongDaDung]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[PHUONG_THUC_THANH_TOAN] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[PHUONG_THUC_THANH_TOAN] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[RESET_PASSWORD_TOKEN] ADD  DEFAULT ((0)) FOR [Used]
GO
ALTER TABLE [dbo].[RESET_PASSWORD_TOKEN] ADD  DEFAULT (sysutcdatetime()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[SAN_PHAM] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[SAN_PHAM] ADD  DEFAULT ((0)) FOR [DaXoa]
GO
ALTER TABLE [dbo].[SAN_PHAM] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[TAI_KHOAN] ADD  DEFAULT (N'ACTIVE') FOR [TrangThai]
GO
ALTER TABLE [dbo].[TAI_KHOAN] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[THUONG_HIEU] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[VAI_TRO] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[VAN_CHUYEN] ADD  DEFAULT (N'CREATED') FOR [TrangThai]
GO
ALTER TABLE [dbo].[VAN_CHUYEN] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA] ADD  DEFAULT (N'PENDING') FOR [TrangThai]
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM] ADD  DEFAULT (sysdatetime()) FOR [NgayTao]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_BT_SP] FOREIGN KEY([SanPhamId])
REFERENCES [dbo].[SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] CHECK CONSTRAINT [FK_BT_SP]
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA]  WITH CHECK ADD  CONSTRAINT [FK_CTDT_CTDH] FOREIGN KEY([ChiTietDonHangId])
REFERENCES [dbo].[CHI_TIET_DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA] CHECK CONSTRAINT [FK_CTDT_CTDH]
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA]  WITH CHECK ADD  CONSTRAINT [FK_CTDT_YCDT] FOREIGN KEY([YeuCauDoiTraId])
REFERENCES [dbo].[YEU_CAU_DOI_TRA] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA] CHECK CONSTRAINT [FK_CTDT_YCDT]
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG]  WITH CHECK ADD  CONSTRAINT [FK_CTDH_BT] FOREIGN KEY([BienTheSanPhamId])
REFERENCES [dbo].[BIEN_THE_SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG] CHECK CONSTRAINT [FK_CTDH_BT]
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG]  WITH CHECK ADD  CONSTRAINT [FK_CTDH_DH] FOREIGN KEY([DonHangId])
REFERENCES [dbo].[DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG] CHECK CONSTRAINT [FK_CTDH_DH]
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG]  WITH CHECK ADD  CONSTRAINT [FK_CTGH_BT] FOREIGN KEY([BienTheSanPhamId])
REFERENCES [dbo].[BIEN_THE_SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] CHECK CONSTRAINT [FK_CTGH_BT]
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG]  WITH CHECK ADD  CONSTRAINT [FK_CTGH_GH] FOREIGN KEY([GioHangId])
REFERENCES [dbo].[GIO_HANG] ([Id])
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] CHECK CONSTRAINT [FK_CTGH_GH]
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_DGSP_SP] FOREIGN KEY([SanPhamId])
REFERENCES [dbo].[SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM] CHECK CONSTRAINT [FK_DGSP_SP]
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_DGSP_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM] CHECK CONSTRAINT [FK_DGSP_TK]
GO
ALTER TABLE [dbo].[DANH_MUC]  WITH CHECK ADD  CONSTRAINT [FK_DANH_MUC_CHA] FOREIGN KEY([DanhMucChaId])
REFERENCES [dbo].[DANH_MUC] ([Id])
GO
ALTER TABLE [dbo].[DANH_MUC] CHECK CONSTRAINT [FK_DANH_MUC_CHA]
GO
ALTER TABLE [dbo].[DIA_CHI_GIAO_HANG]  WITH CHECK ADD  CONSTRAINT [FK_DCGH_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[DIA_CHI_GIAO_HANG] CHECK CONSTRAINT [FK_DCGH_TK]
GO
ALTER TABLE [dbo].[DON_HANG]  WITH CHECK ADD  CONSTRAINT [FK_DH_MGG] FOREIGN KEY([MaGiamGiaId])
REFERENCES [dbo].[MA_GIAM_GIA] ([Id])
GO
ALTER TABLE [dbo].[DON_HANG] CHECK CONSTRAINT [FK_DH_MGG]
GO
ALTER TABLE [dbo].[DON_HANG]  WITH CHECK ADD  CONSTRAINT [FK_DH_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[DON_HANG] CHECK CONSTRAINT [FK_DH_TK]
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN]  WITH CHECK ADD  CONSTRAINT [FK_GDTT_DH] FOREIGN KEY([DonHangId])
REFERENCES [dbo].[DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN] CHECK CONSTRAINT [FK_GDTT_DH]
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN]  WITH CHECK ADD  CONSTRAINT [FK_GDTT_PTTT] FOREIGN KEY([PhuongThucThanhToanId])
REFERENCES [dbo].[PHUONG_THUC_THANH_TOAN] ([Id])
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN] CHECK CONSTRAINT [FK_GDTT_PTTT]
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO]  WITH CHECK ADD  CONSTRAINT [FK_GDTK_BT] FOREIGN KEY([BienTheSanPhamId])
REFERENCES [dbo].[BIEN_THE_SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO] CHECK CONSTRAINT [FK_GDTK_BT]
GO
ALTER TABLE [dbo].[GIO_HANG]  WITH CHECK ADD  CONSTRAINT [FK_GH_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[GIO_HANG] CHECK CONSTRAINT [FK_GH_TK]
GO
ALTER TABLE [dbo].[HINH_ANH_MAU_SAC]  WITH CHECK ADD  CONSTRAINT [FK_HAMS_SP] FOREIGN KEY([SanPhamId])
REFERENCES [dbo].[SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[HINH_ANH_MAU_SAC] CHECK CONSTRAINT [FK_HAMS_SP]
GO
ALTER TABLE [dbo].[HINH_ANH_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_HASP_SP] FOREIGN KEY([SanPhamId])
REFERENCES [dbo].[SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[HINH_ANH_SAN_PHAM] CHECK CONSTRAINT [FK_HASP_SP]
GO
ALTER TABLE [dbo].[LICH_SU_NHAP_KHO]  WITH CHECK ADD  CONSTRAINT [FK_LSNK_BienThe] FOREIGN KEY([BienTheSanPhamId])
REFERENCES [dbo].[BIEN_THE_SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[LICH_SU_NHAP_KHO] CHECK CONSTRAINT [FK_LSNK_BienThe]
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [FK_LS_DH] FOREIGN KEY([DonHangId])
REFERENCES [dbo].[DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] CHECK CONSTRAINT [FK_LS_DH]
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [FK_LS_MGG] FOREIGN KEY([MaGiamGiaId])
REFERENCES [dbo].[MA_GIAM_GIA] ([Id])
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] CHECK CONSTRAINT [FK_LS_MGG]
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [FK_LS_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[LICH_SU_SU_DUNG_MA_GIAM_GIA] CHECK CONSTRAINT [FK_LS_TK]
GO
ALTER TABLE [dbo].[RESET_PASSWORD_TOKEN]  WITH CHECK ADD  CONSTRAINT [FK_RESET_PASSWORD_TOKEN_TAIKHOAN] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[RESET_PASSWORD_TOKEN] CHECK CONSTRAINT [FK_RESET_PASSWORD_TOKEN_TAIKHOAN]
GO
ALTER TABLE [dbo].[SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_SP_DM] FOREIGN KEY([DanhMucId])
REFERENCES [dbo].[DANH_MUC] ([Id])
GO
ALTER TABLE [dbo].[SAN_PHAM] CHECK CONSTRAINT [FK_SP_DM]
GO
ALTER TABLE [dbo].[SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_SP_TH] FOREIGN KEY([ThuongHieuId])
REFERENCES [dbo].[THUONG_HIEU] ([Id])
GO
ALTER TABLE [dbo].[SAN_PHAM] CHECK CONSTRAINT [FK_SP_TH]
GO
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO]  WITH CHECK ADD  CONSTRAINT [FK_TKVT_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO] CHECK CONSTRAINT [FK_TKVT_TK]
GO
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO]  WITH CHECK ADD  CONSTRAINT [FK_TKVT_VT] FOREIGN KEY([VaiTroId])
REFERENCES [dbo].[VAI_TRO] ([Id])
GO
ALTER TABLE [dbo].[TAI_KHOAN_VAI_TRO] CHECK CONSTRAINT [FK_TKVT_VT]
GO
ALTER TABLE [dbo].[VAN_CHUYEN]  WITH CHECK ADD  CONSTRAINT [FK_VC_DH] FOREIGN KEY([DonHangId])
REFERENCES [dbo].[DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[VAN_CHUYEN] CHECK CONSTRAINT [FK_VC_DH]
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA]  WITH CHECK ADD  CONSTRAINT [FK_YCDT_DH] FOREIGN KEY([DonHangId])
REFERENCES [dbo].[DON_HANG] ([Id])
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA] CHECK CONSTRAINT [FK_YCDT_DH]
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA]  WITH CHECK ADD  CONSTRAINT [FK_YCDT_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[YEU_CAU_DOI_TRA] CHECK CONSTRAINT [FK_YCDT_TK]
GO
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_YTSP_SP] FOREIGN KEY([SanPhamId])
REFERENCES [dbo].[SAN_PHAM] ([Id])
GO
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM] CHECK CONSTRAINT [FK_YTSP_SP]
GO
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [FK_YTSP_TK] FOREIGN KEY([TaiKhoanId])
REFERENCES [dbo].[TAI_KHOAN] ([Id])
GO
ALTER TABLE [dbo].[YEU_THICH_SAN_PHAM] CHECK CONSTRAINT [FK_YTSP_TK]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [CK_BT_Gia] CHECK  (([Gia]>=(0)))
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] CHECK CONSTRAINT [CK_BT_Gia]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [CK_BT_GiaGoc] CHECK  (([GiaGoc] IS NULL OR [GiaGoc]>=(0)))
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] CHECK CONSTRAINT [CK_BT_GiaGoc]
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [CK_BT_Ton] CHECK  (([SoLuongTon]>=(0)))
GO
ALTER TABLE [dbo].[BIEN_THE_SAN_PHAM] CHECK CONSTRAINT [CK_BT_Ton]
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA]  WITH CHECK ADD  CONSTRAINT [CK_CTDT_SoLuong] CHECK  (([SoLuong]>(0)))
GO
ALTER TABLE [dbo].[CHI_TIET_DOI_TRA] CHECK CONSTRAINT [CK_CTDT_SoLuong]
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG]  WITH CHECK ADD  CONSTRAINT [CK_CTDH_SoLuong] CHECK  (([SoLuong]>(0)))
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG] CHECK CONSTRAINT [CK_CTDH_SoLuong]
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG]  WITH CHECK ADD  CONSTRAINT [CK_CTDH_Tien] CHECK  (([DonGia]>=(0) AND [ThanhTien]>=(0)))
GO
ALTER TABLE [dbo].[CHI_TIET_DON_HANG] CHECK CONSTRAINT [CK_CTDH_Tien]
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG]  WITH CHECK ADD  CONSTRAINT [CK_CTGH_DonGia] CHECK  (([DonGia]>=(0)))
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] CHECK CONSTRAINT [CK_CTGH_DonGia]
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG]  WITH CHECK ADD  CONSTRAINT [CK_CTGH_SoLuong] CHECK  (([SoLuong]>(0)))
GO
ALTER TABLE [dbo].[CHI_TIET_GIO_HANG] CHECK CONSTRAINT [CK_CTGH_SoLuong]
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM]  WITH CHECK ADD  CONSTRAINT [CK_DGSP_SoSao] CHECK  (([SoSao]>=(1) AND [SoSao]<=(5)))
GO
ALTER TABLE [dbo].[DANH_GIA_SAN_PHAM] CHECK CONSTRAINT [CK_DGSP_SoSao]
GO
ALTER TABLE [dbo].[DON_HANG]  WITH CHECK ADD  CONSTRAINT [CK_DH_Tien] CHECK  (([TamTinh]>=(0) AND [GiamGia]>=(0) AND [PhiVanChuyen]>=(0) AND [TongTien]>=(0)))
GO
ALTER TABLE [dbo].[DON_HANG] CHECK CONSTRAINT [CK_DH_Tien]
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN]  WITH CHECK ADD  CONSTRAINT [CK_GDTT_SoTien] CHECK  (([SoTien]>=(0)))
GO
ALTER TABLE [dbo].[GIAO_DICH_THANH_TOAN] CHECK CONSTRAINT [CK_GDTT_SoTien]
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO]  WITH CHECK ADD  CONSTRAINT [CK_GDTK_Loai] CHECK  (([Loai]=N'DIEU_CHINH' OR [Loai]=N'XUAT' OR [Loai]=N'NHAP'))
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO] CHECK CONSTRAINT [CK_GDTK_Loai]
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO]  WITH CHECK ADD  CONSTRAINT [CK_GDTK_SoLuong] CHECK  (([SoLuong]<>(0)))
GO
ALTER TABLE [dbo].[GIAO_DICH_TON_KHO] CHECK CONSTRAINT [CK_GDTK_SoLuong]
GO
ALTER TABLE [dbo].[GIO_HANG]  WITH CHECK ADD  CONSTRAINT [CK_GH_TK_OR_SESSION] CHECK  (([TaiKhoanId] IS NOT NULL OR [SessionId] IS NOT NULL))
GO
ALTER TABLE [dbo].[GIO_HANG] CHECK CONSTRAINT [CK_GH_TK_OR_SESSION]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [CK_MGG_DonToiThieu] CHECK  (([DonToiThieu] IS NULL OR [DonToiThieu]>=(0)))
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] CHECK CONSTRAINT [CK_MGG_DonToiThieu]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [CK_MGG_GiaTri] CHECK  (([GiaTri]>=(0)))
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] CHECK CONSTRAINT [CK_MGG_GiaTri]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [CK_MGG_GiaTriToiDa] CHECK  (([GiaTriToiDa] IS NULL OR [GiaTriToiDa]>=(0)))
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] CHECK CONSTRAINT [CK_MGG_GiaTriToiDa]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [CK_MGG_Loai] CHECK  (([Loai]=N'FIXED' OR [Loai]=N'PERCENT'))
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] CHECK CONSTRAINT [CK_MGG_Loai]
GO
ALTER TABLE [dbo].[MA_GIAM_GIA]  WITH CHECK ADD  CONSTRAINT [CK_MGG_SoLuong] CHECK  (([SoLuongToiDa] IS NULL OR [SoLuongToiDa]>=(0)))
GO
ALTER TABLE [dbo].[MA_GIAM_GIA] CHECK CONSTRAINT [CK_MGG_SoLuong]
GO
USE [master]
GO
ALTER DATABASE [CLOTHES_STORE] SET  READ_WRITE 
GO
