package com.example.demodatn2.service;

import com.example.demodatn2.dto.DoanhThuDTO;
import com.example.demodatn2.dto.LowStockVariantAlertDTO;
import com.example.demodatn2.dto.PotentialCustomerDTO;
import com.example.demodatn2.dto.TopSellingProductDTO;
import com.example.demodatn2.entity.BienTheSanPham;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.repository.BienTheSanPhamRepository;
import com.example.demodatn2.repository.ChiTietDonHangRepository;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.HinhAnhSanPhamRepository;
import com.example.demodatn2.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
// Service thống kê doanh thu, số đơn và xuất báo cáo Excel theo khoảng thời gian.
public class ThongKeService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int LOW_STOCK_ALERT_LIMIT = 5;
    private static final int TOP_PRODUCT_LIMIT = 5;
    private static final int POTENTIAL_CUSTOMER_LIMIT = 5;
    private static final int POTENTIAL_CUSTOMER_DAYS = 90;

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;

    public DoanhThuDTO getDoanhThuTongHop() {
        BigDecimal tongDoanhThu = donHangRepository.sumTongDoanhThu();
        Long soDonHang = donHangRepository.countDonHangThanhCong();
        Long soSPDaBan = chiTietDonHangRepository.sumSoLuongDaBan();
        Long soKhachHang = taiKhoanRepository.countCustomers();

        // Lấy doanh thu hôm nay
        Instant dauNgay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        BigDecimal doanhThuHomNay = donHangRepository.sumDoanhThuTuNgay(dauNgay);
        Long soDonHomNay = donHangRepository.countDonHangTuNgay(dauNgay);

        Long soDonChuaXuLy = donHangRepository.countByTrangThaiIn(List.of("CHO_XAC_NHAN", "PENDING"));
        Long soSanPhamSapHetHang = bienTheSanPhamRepository.countActiveLowStock(LOW_STOCK_THRESHOLD);
        Long soDonBiHuyHomNay = donHangRepository.countByTrangThaiInAndNgayDatGreaterThanEqual(
            List.of("DA_HUY", "CANCELLED"), dauNgay);
        Long tongDonHomNay = donHangRepository.countByNgayDatGreaterThanEqual(dauNgay);
        List<BienTheSanPham> lowStockVariants = bienTheSanPhamRepository.findLowStockActiveVariants(
            LOW_STOCK_THRESHOLD,
            PageRequest.of(0, LOW_STOCK_ALERT_LIMIT)
        );
        List<LowStockVariantAlertDTO> bienTheSapHetHang = lowStockVariants.stream()
            .map(v -> LowStockVariantAlertDTO.builder()
                .sanPhamId(v.getSanPham().getId())
                .bienTheId(v.getId())
                .tenSanPham(v.getSanPham().getTen())
                .mauSac(v.getMauSac())
                .kichCo(v.getKichCo())
                .soLuongTon(v.getSoLuongTon())
                .hinhAnh(hinhAnhSanPhamRepository
                    .findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(v.getSanPham().getId())
                    .map(img -> img.getDuongDanAnh())
                    .orElse("/images/no-image.png"))
                .build())
            .toList();
        BigDecimal tyLeHuyHomNay = BigDecimal.ZERO;
        if (tongDonHomNay != null && tongDonHomNay > 0) {
            tyLeHuyHomNay = BigDecimal.valueOf(soDonBiHuyHomNay != null ? soDonBiHuyHomNay : 0L)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(tongDonHomNay), 1, java.math.RoundingMode.HALF_UP);
        }

        List<TopSellingProductDTO> topSanPhamBanChay = chiTietDonHangRepository
            .findTopSellingProducts(PageRequest.of(0, TOP_PRODUCT_LIMIT))
            .stream()
            .map(row -> {
                Integer sanPhamId = row[0] != null ? ((Number) row[0]).intValue() : null;
                return TopSellingProductDTO.builder()
                    .sanPhamId(sanPhamId)
                    .tenSanPham(row[1] != null ? String.valueOf(row[1]) : "-")
                    .tongSoLuongBan(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                    .tongDoanhThu(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                    .soDonHang(row[4] != null ? ((Number) row[4]).longValue() : 0L)
                    .hinhAnh(resolveProductImage(sanPhamId))
                    .build();
            })
            .toList();

        Instant potentialFromDate = LocalDate.now()
            .minusDays(POTENTIAL_CUSTOMER_DAYS)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();
        List<PotentialCustomerDTO> khachHangTiemNang = donHangRepository
            .findPotentialCustomers(potentialFromDate, PageRequest.of(0, POTENTIAL_CUSTOMER_LIMIT))
            .stream()
            .map(row -> PotentialCustomerDTO.builder()
                .taiKhoanId(row[0] != null ? ((Number) row[0]).intValue() : null)
                .hoTen(row[1] != null ? String.valueOf(row[1]) : "Khách hàng")
                .soDienThoai(row[2] != null ? String.valueOf(row[2]) : "-")
                .soDonThanhCong(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                .tongChiTieu(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .lanMuaGanNhat(row[5] instanceof Instant ? (Instant) row[5] : null)
                .build())
            .toList();

        return DoanhThuDTO.builder()
                .tongDoanhThu(tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO)
                .soDonHang(soDonHang != null ? soDonHang : 0L)
                .soSanPhamDaBan(soSPDaBan != null ? soSPDaBan : 0L)
                .doanhThuHomNay(doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO)
                .soDonHomNay(soDonHomNay != null ? soDonHomNay : 0L)
            .soKhachHang(soKhachHang != null ? soKhachHang : 0L)
            .soDonChuaXuLy(soDonChuaXuLy != null ? soDonChuaXuLy : 0L)
            .soSanPhamSapHetHang(soSanPhamSapHetHang != null ? soSanPhamSapHetHang : 0L)
            .soDonBiHuyHomNay(soDonBiHuyHomNay != null ? soDonBiHuyHomNay : 0L)
            .tyLeHuyHomNay(tyLeHuyHomNay)
            .bienTheSapHetHang(bienTheSapHetHang)
            .topSanPhamBanChay(topSanPhamBanChay)
            .khachHangTiemNang(khachHangTiemNang)
                .build();
    }

    private String resolveProductImage(Integer sanPhamId) {
        if (sanPhamId == null) {
            return "/images/no-image.png";
        }

        return hinhAnhSanPhamRepository
            .findFirstBySanPham_IdOrderByLaAnhChinhDescThuTuAscIdAsc(sanPhamId)
            .map(img -> img.getDuongDanAnh())
            .orElse("/images/no-image.png");
    }

    public List<Map<String, Object>> getDoanhThuTheoNgay(Instant tuNgay, Instant denNgay) {
        List<Object[]> results = donHangRepository.getDoanhThuTheoNgay(tuNgay, denNgay);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("ngay", row[0].toString());
            map.put("doanhThu", row[1]);
            data.add(map);
        }
        return data;
    }

    public Map<String, Object> getDoanhThuTrongKhoang(Instant tuNgay, Instant denNgay) {
        BigDecimal doanhThuThucTe = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("HOAN_THANH", "COMPLETED", "DELIVERED"), tuNgay, denNgay);
        BigDecimal doanhThuTamTinh = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "PENDING", "CONFIRMED", "SHIPPING", "PROCESSING", "SHIPPED"), tuNgay, denNgay);
        BigDecimal doanhThuThatThoat = donHangRepository.sumTongTienByTrangThaiInRange(
            List.of("DA_HUY", "CANCELLED"), tuNgay, denNgay);
        Long soDon = donHangRepository.countDonHangTrongKhoang(tuNgay, denNgay);

        Map<String, Object> stats = new HashMap<>();
        stats.put("doanhThuThucTe", doanhThuThucTe != null ? doanhThuThucTe : BigDecimal.ZERO);
//        stats.put("doanhThuTamTinh", doanhThuTamTinh != null ? doanhThuTamTinh : BigDecimal.ZERO);
//        stats.put("doanhThuThatThoat", doanhThuThatThoat != null ? doanhThuThatThoat : BigDecimal.ZERO);
        stats.put("soDon", soDon != null ? soDon : 0L);
        return stats;
    }

    public byte[] exportDoanhThuToExcel(Instant tu, Instant den) throws IOException {
        List<DonHang> orders = donHangRepository.findAllDeliveredInRange(tu, den);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo doanh thu");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Mã đơn hàng", "Ngày đặt", "Khách hàng", "Số điện thoại", "Phương thức thanh toán", "Tổng tiền"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Date Format Style
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            // Money Format Style
            CellStyle moneyStyle = workbook.createCellStyle();
            moneyStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0\"₫\""));

            // Create Data Rows
            int rowIdx = 1;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (DonHang order : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(order.getMaDonHang());
                
                Cell dateCell = row.createCell(1);
                dateCell.setCellValue(java.util.Date.from(order.getNgayDat()));
                dateCell.setCellStyle(dateStyle);
                
                row.createCell(2).setCellValue(order.getHoTenNhan());
                row.createCell(3).setCellValue(order.getSoDienThoaiNhan());
                row.createCell(4).setCellValue(order.getPhuongThucThanhToan());
                
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(order.getTongTien().doubleValue());
                amountCell.setCellStyle(moneyStyle);
                
                totalRevenue = totalRevenue.add(order.getTongTien());
            }

            // Total Row
            Row totalRow = sheet.createRow(rowIdx);
            Cell labelCell = totalRow.createCell(4);
            labelCell.setCellValue("TỔNG CỘNG:");
            labelCell.setCellStyle(headerStyle);
            
            Cell totalCell = totalRow.createCell(5);
            totalCell.setCellValue(totalRevenue.doubleValue());
            totalCell.setCellStyle(moneyStyle);

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
