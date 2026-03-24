package com.example.demodatn2.service;

import com.example.demodatn2.dto.PosOrderRequestDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Service đơn hàng: tạo đơn online/POS, cập nhật trạng thái, hủy đơn và xử lý đổi trả.
public class OrderService {

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final GioHangRepository gioHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final MaGiamGiaRepository maGiamGiaRepository;
    private final LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    private final YeuCauDoiTraRepository yeuCauDoiTraRepository;
    @Getter
    private final VoucherService voucherService;

    @Transactional(readOnly = true)
    public List<DonHang> getAllOrders() {
        return donHangRepository.findAllByOrderByNgayDatDesc();
    }

    @Transactional(readOnly = true)
    public List<DonHang> searchOrders(String keyword, String status) {
        String normalizedStatus = normalizeStatus(status);

        List<DonHang> sourceOrders;
        if (keyword == null || keyword.trim().isEmpty()) {
            sourceOrders = getAllOrders();
        } else {
            sourceOrders = donHangRepository.search(keyword.trim());
        }

        return filterOrdersByStatus(sourceOrders, normalizedStatus);
    }

    @Transactional(readOnly = true)
    public List<DonHang> getOrdersByStatus(String status) {
        return filterOrdersByStatus(getAllOrders(), normalizeStatus(status));
    }

    @Transactional(readOnly = true)
    public long getPendingConfirmationCount() {
        return donHangRepository.countByTrangThaiIn(List.of("CHO_XAC_NHAN", "PENDING"));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStatusCounts() {
        List<DonHang> orders = getAllOrders();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ALL", (long) orders.size());
        counts.put("CHO_XAC_NHAN", 0L);
        counts.put("DA_XAC_NHAN", 0L);
        counts.put("DANG_GIAO", 0L);
        counts.put("LOI_VAN_CHUYEN", 0L);
        counts.put("HOAN_THANH", 0L);
        counts.put("DA_HUY", 0L);
        counts.put("TRA_HANG", 0L);

        for (DonHang order : orders) {
            String normalized = normalizeStatus(order.getTrangThai());
            if (counts.containsKey(normalized)) {
                counts.put(normalized, counts.get(normalized) + 1);
            }
        }
        return counts;
    }

    private List<DonHang> filterOrdersByStatus(List<DonHang> orders, String normalizedStatus) {
        if (normalizedStatus == null || normalizedStatus.isEmpty() || "ALL".equals(normalizedStatus)) {
            return orders;
        }
        return orders.stream()
                .filter(order -> normalizedStatus.equals(normalizeStatus(order.getTrangThai())))
                .toList();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ALL";
        }

        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PENDING" -> "CHO_XAC_NHAN";
            case "CONFIRMED" -> "DA_XAC_NHAN";
            case "SHIPPING" -> "DANG_GIAO";
            case "LOST" -> "LOI_VAN_CHUYEN";
            case "DELIVERED", "COMPLETED" -> "HOAN_THANH";
            case "CANCELLED" -> "DA_HUY";
            case "RETURN_REQUESTED", "RETURNED" -> "TRA_HANG";
            default -> normalized;
        };
    }

    @Transactional(readOnly = true)
    public DonHang getOrderById(Integer id) {
        return donHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<ChiTietDonHang> getOrderItems(Integer orderId) {
        DonHang donHang = getOrderById(orderId);
        return chiTietDonHangRepository.findByDonHangWithDetails(donHang);
    }

    @Transactional(readOnly = true)
    public List<DonHang> getOrdersByAccount(TaiKhoan taiKhoan) {
        return donHangRepository.findByTaiKhoanOrderByNgayDatDesc(taiKhoan);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, String newStatus) {
        DonHang donHang = getOrderById(orderId);
        String currentStatus = normalizeStatus(donHang.getTrangThai());

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new RuntimeException("Trạng thái mới không hợp lệ.");
        }
        newStatus = normalizeStatus(newStatus);

        if (currentStatus == null || currentStatus.trim().isEmpty() || "ALL".equals(currentStatus)) {
            throw new RuntimeException("Trạng thái hiện tại không hợp lệ.");
        }

        if (currentStatus.equals(newStatus)) {
            return;
        }

        if ("DA_HUY".equals(currentStatus)) {
            throw new RuntimeException("Không thể cập nhật trạng thái cho đơn hàng đã hủy.");
        }

        if ("DA_HUY".equals(newStatus)) {
            restoreStock(donHang);
        }

        donHang.setTrangThai(newStatus);
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);
    }

    @Transactional
    public void cancelOrder(Integer orderId, String reason, boolean isAdmin) {
        DonHang donHang = getOrderById(orderId);
        String currentStatus = normalizeStatus(donHang.getTrangThai());

        if ("DA_HUY".equals(currentStatus)) {
            throw new RuntimeException("Đơn hàng đã được hủy trước đó.");
        }
        if ("HOAN_THANH".equals(currentStatus) || "TRA_HANG".equals(currentStatus)) {
            throw new RuntimeException("Không thể hủy đơn hàng đã kết thúc.");
        }

        if (!isAdmin) {
            // Khách hàng chỉ được hủy khi chờ xác nhận hoặc đã xác nhận.
            if (!"CHO_XAC_NHAN".equals(currentStatus) && !"DA_XAC_NHAN".equals(currentStatus)) {
                throw new RuntimeException("Bạn không thể hủy đơn hàng ở trạng thái: " + currentStatus);
            }
        }
        // Admin được hủy mọi trạng thái chưa kết thúc (đã check ở trên).

        donHang.setTrangThai("DA_HUY");
        donHang.setLyDoHuy(reason);
        donHang.setNgayCapNhat(Instant.now());
        
        restoreStock(donHang);
        
        donHangRepository.save(donHang);
    }

    @Transactional
    public int autoCompleteDeliveredOrders(int days) {
        if (days <= 0) {
            return 0;
        }

        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        List<DonHang> deliveredOrders = donHangRepository.findByTrangThaiAndLastUpdateBefore("DANG_GIAO", threshold);

        for (DonHang donHang : deliveredOrders) {
            if ("DA_HUY".equalsIgnoreCase(normalizeStatus(donHang.getTrangThai()))) {
                continue;
            }
            donHang.setTrangThai("HOAN_THANH");
            donHang.setNgayCapNhat(Instant.now());
            donHangRepository.save(donHang);
        }

        return deliveredOrders.size();
    }

    private void restoreStock(DonHang donHang) {
        List<ChiTietDonHang> items = chiTietDonHangRepository.findByDonHang(donHang);
        for (ChiTietDonHang item : items) {
            BienTheSanPham bt = item.getBienTheSanPham();
            if (bt != null) {
                bt.setSoLuongTon(bt.getSoLuongTon() + item.getSoLuong());
                bienTheSanPhamRepository.save(bt);
            }
        }
    }

    @Transactional
    public void updateOrderAddress(Integer orderId, String hoTen, String soDienThoai, String diaChi) {
        DonHang donHang = getOrderById(orderId);
        String status = normalizeStatus(donHang.getTrangThai());

        if (!"CHO_XAC_NHAN".equals(status) && !"DA_XAC_NHAN".equals(status)) {
            throw new RuntimeException("Không thể thay đổi địa chỉ cho đơn hàng ở trạng thái: " + status);
        }

        donHang.setHoTenNhan(hoTen);
        donHang.setSoDienThoaiNhan(soDienThoai);
        donHang.setDiaChiNhan(diaChi);
        donHang.setNgayCapNhat(Instant.now());

        donHangRepository.save(donHang);
    }

    @Transactional
    public DonHang createOrder(String hoTen, String soDienThoai, String email, String diaChi, String ghiChu, String paymentMethod, HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        TaiKhoan taiKhoan = null;
        if (loginUser != null) {
            taiKhoan = taiKhoanRepository.findById(loginUser.getId()).orElse(null);
        }

        String sessionId = session.getId();
        GioHang gioHang = loginUser != null ? 
                gioHangRepository.findByTaiKhoan(taiKhoan).orElseThrow(() -> new RuntimeException("Giỏ hàng trống")) :
                gioHangRepository.findBySessionId(sessionId).orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));

        if (gioHang.getChiTiets().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        DonHang donHang = new DonHang();
        donHang.setMaDonHang("DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setTaiKhoan(taiKhoan);
        donHang.setHoTenNhan(hoTen);
        donHang.setSoDienThoaiNhan(soDienThoai);
        donHang.setEmailNhan(email);
        donHang.setDiaChiNhan(diaChi);
        donHang.setGhiChu(ghiChu);
        donHang.setPhuongThucThanhToan(paymentMethod);
        
        // Đơn online khởi tạo ở trạng thái chờ xác nhận.
        if ("VIETQR".equals(paymentMethod)) {
            donHang.setTrangThai("CHO_XAC_NHAN");
        } else {
            donHang.setTrangThai("CHO_XAC_NHAN");
        }
        
        donHang.setNgayDat(Instant.now());
        
        BigDecimal tamTinh = BigDecimal.ZERO;
        for (ChiTietGioHang item : gioHang.getChiTiets()) {
            tamTinh = tamTinh.add(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
        }
        donHang.setTamTinh(tamTinh);
        donHang.setPhiVanChuyen(CartService.calculateShippingFee(tamTinh));

        // Áp dụng voucher từ session nếu có
        BigDecimal giamGiaAmount = BigDecimal.ZERO;
        MaGiamGia appliedVoucher = null;
        String voucherCode = (String) session.getAttribute("APPLIED_VOUCHER_CODE");
        
        if (voucherCode != null) {
            var voucherOpt = voucherService.validateVoucher(voucherCode, tamTinh);
            if (voucherOpt.isPresent()) {
                appliedVoucher = voucherOpt.get();
                giamGiaAmount = voucherService.calculateDiscount(appliedVoucher, tamTinh);
            }
        }
        
        donHang.setGiamGia(giamGiaAmount);
        donHang.setMaGiamGia(appliedVoucher);
        donHang.setTongTien(tamTinh.subtract(giamGiaAmount).add(donHang.getPhiVanChuyen()));

        donHang = donHangRepository.save(donHang);

        // Nếu có voucher, lưu lịch sử sử dụng và cập nhật số lượng
        if (appliedVoucher != null) {
            appliedVoucher.setSoLuongDaDung(appliedVoucher.getSoLuongDaDung() + 1);
            maGiamGiaRepository.save(appliedVoucher);
            
            LichSuSuDungMaGiamGia lichSu = new LichSuSuDungMaGiamGia();
            lichSu.setDonHang(donHang);
            lichSu.setTaiKhoan(taiKhoan);
            lichSu.setMaGiamGia(appliedVoucher);
            lichSu.setThoiGianSuDung(Instant.now());
            lichSuSuDungMaGiamGiaRepository.save(lichSu);
        }

        for (ChiTietGioHang item : gioHang.getChiTiets()) {
            BienTheSanPham bt = item.getBienTheSanPham();
            
            // Kiểm tra tồn kho lần cuối
            if (bt.getSoLuongTon() < item.getSoLuong()) {
                throw new RuntimeException("Sản phẩm " + bt.getSanPham().getTen() + " không đủ số lượng trong kho");
            }

            ChiTietDonHang ctdh = new ChiTietDonHang();
            ctdh.setDonHang(donHang);
            ctdh.setBienTheSanPham(bt);
            ctdh.setTenSanPham(bt.getSanPham().getTen());
            ctdh.setMauSac(bt.getMauSac());
            ctdh.setKichCo(bt.getKichCo());
            ctdh.setSoLuong(item.getSoLuong());
            ctdh.setDonGia(item.getDonGia());
            ctdh.setThanhTien(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
            
            chiTietDonHangRepository.save(ctdh);

            // Trừ tồn kho
            bt.setSoLuongTon(bt.getSoLuongTon() - item.getSoLuong());
            bienTheSanPhamRepository.save(bt);
        }
        
        // Xóa giỏ hàng sau khi đặt thành công
        gioHangRepository.delete(gioHang);

        // Xóa thông tin voucher khỏi session
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");

        return donHang;
    }

    /**
     * Tạo đơn hàng từ POS (bán hàng tại quầy) — không qua giỏ hàng.
     */
    @Transactional
    public DonHang createPosOrder(PosOrderRequestDTO req, TaiKhoanDTO staffUser) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng POS trống");
        }
        if (req.getCustomerName() == null || req.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên khách hàng");
        }
        if (req.getCustomerPhone() == null || req.getCustomerPhone().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số điện thoại khách hàng");
        }

        // Tìm tài khoản khách hàng nếu có
        TaiKhoan customer = null;
        if (req.getCustomerId() != null) {
            customer = taiKhoanRepository.findById(req.getCustomerId()).orElse(null);
        }

        DonHang donHang = new DonHang();
        donHang.setMaDonHang("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setTaiKhoan(customer);
        donHang.setHoTenNhan(req.getCustomerName().trim());
        donHang.setSoDienThoaiNhan(req.getCustomerPhone().trim());
        donHang.setEmailNhan(customer != null ? customer.getEmail() : null);
        donHang.setDiaChiNhan("Mua tại quầy");
        donHang.setGhiChu(req.getNote());
        donHang.setPhuongThucThanhToan(req.getPaymentMethod() != null ? req.getPaymentMethod() : "cash");
        donHang.setTrangThai("HOAN_THANH"); // POS = thanh toán xong ngay
        donHang.setPhiVanChuyen(BigDecimal.ZERO); // Không ship
        donHang.setNgayDat(Instant.now());

        BigDecimal tamTinh = BigDecimal.ZERO;

        // Validate & tính tạm tính
        for (PosOrderRequestDTO.PosItemDTO item : req.getItems()) {
            BienTheSanPham bt = bienTheSanPhamRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + item.getVariantId()));
            if (bt.getSoLuongTon() < item.getQty()) {
                throw new RuntimeException("Sản phẩm " + bt.getSanPham().getTen() +
                        " (" + bt.getMauSac() + "/" + bt.getKichCo() + ") chỉ còn " + bt.getSoLuongTon());
            }
            tamTinh = tamTinh.add(item.getPrice().multiply(new BigDecimal(item.getQty())));
        }
        donHang.setTamTinh(tamTinh);

        // Áp dụng voucher
        BigDecimal giamGiaAmount = BigDecimal.ZERO;
        MaGiamGia appliedVoucher = null;
        if (req.getVoucherCode() != null && !req.getVoucherCode().trim().isEmpty()) {
            var voucherOpt = voucherService.validateVoucher(req.getVoucherCode().trim(), tamTinh);
            if (voucherOpt.isPresent()) {
                appliedVoucher = voucherOpt.get();
                giamGiaAmount = voucherService.calculateDiscount(appliedVoucher, tamTinh);
            }
        }
        donHang.setGiamGia(giamGiaAmount);
        donHang.setMaGiamGia(appliedVoucher);
        donHang.setTongTien(tamTinh.subtract(giamGiaAmount));

        donHang = donHangRepository.save(donHang);

        // Lưu lịch sử sử dụng voucher
        if (appliedVoucher != null) {
            appliedVoucher.setSoLuongDaDung(appliedVoucher.getSoLuongDaDung() + 1);
            maGiamGiaRepository.save(appliedVoucher);

            LichSuSuDungMaGiamGia lichSu = new LichSuSuDungMaGiamGia();
            lichSu.setDonHang(donHang);
            lichSu.setTaiKhoan(customer);
            lichSu.setMaGiamGia(appliedVoucher);
            lichSu.setThoiGianSuDung(Instant.now());
            lichSuSuDungMaGiamGiaRepository.save(lichSu);
        }

        // Tạo chi tiết đơn hàng + trừ kho
        for (PosOrderRequestDTO.PosItemDTO item : req.getItems()) {
            BienTheSanPham bt = bienTheSanPhamRepository.findById(item.getVariantId()).orElseThrow();

            ChiTietDonHang ctdh = new ChiTietDonHang();
            ctdh.setDonHang(donHang);
            ctdh.setBienTheSanPham(bt);
            ctdh.setTenSanPham(bt.getSanPham().getTen());
            ctdh.setMauSac(bt.getMauSac());
            ctdh.setKichCo(bt.getKichCo());
            ctdh.setSoLuong(item.getQty());
            ctdh.setDonGia(item.getPrice());
            ctdh.setThanhTien(item.getPrice().multiply(new BigDecimal(item.getQty())));
            chiTietDonHangRepository.save(ctdh);

            bt.setSoLuongTon(bt.getSoLuongTon() - item.getQty());
            bienTheSanPhamRepository.save(bt);
        }

        return donHang;
    }

    @Transactional
    public YeuCauDoiTra createReturnRequest(Integer orderId, Integer taiKhoanId, String lyDo) {
        DonHang donHang = getOrderById(orderId);

        if (!donHang.getTaiKhoan().getId().equals(taiKhoanId)) {
            throw new RuntimeException("Bạn không có quyền yêu cầu trả hàng cho đơn này.");
        }

        String orderStatus = normalizeStatus(donHang.getTrangThai());
        if (!"HOAN_THANH".equals(orderStatus)) {
            throw new RuntimeException("Chỉ có thể yêu cầu trả hàng khi đơn đã giao hoặc hoàn thành.");
        }

        // Chỉ cho phép trả hàng trong vòng 7 ngày kể từ ngày đặt
        if (donHang.getNgayDat().plus(7, ChronoUnit.DAYS).isBefore(Instant.now())) {
            throw new RuntimeException("Đã quá 7 ngày kể từ ngày đặt hàng, không thể yêu cầu trả hàng.");
        }

        if (yeuCauDoiTraRepository.findByDonHangId(orderId).isPresent()) {
            throw new RuntimeException("Đơn hàng này đã có yêu cầu trả hàng.");
        }

        TaiKhoan taiKhoan = taiKhoanRepository.findById(taiKhoanId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        YeuCauDoiTra yeuCau = new YeuCauDoiTra();
        yeuCau.setDonHang(donHang);
        yeuCau.setTaiKhoan(taiKhoan);
        yeuCau.setLyDo(lyDo);
        yeuCau.setTrangThai("PENDING");
        yeuCau.setNgayTao(Instant.now());

        donHang.setTrangThai("TRA_HANG");
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);

        return yeuCauDoiTraRepository.save(yeuCau);
    }

    @Transactional
    public void approveReturnRequest(Integer requestId) {
        YeuCauDoiTra yeuCau = yeuCauDoiTraRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu trả hàng."));

        if (!"PENDING".equals(yeuCau.getTrangThai())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý.");
        }

        yeuCau.setTrangThai("APPROVED");
        yeuCauDoiTraRepository.save(yeuCau);

        DonHang donHang = yeuCau.getDonHang();
        donHang.setTrangThai("TRA_HANG");
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);

        restoreStock(donHang);
    }

    @Transactional
    public void rejectReturnRequest(Integer requestId, String lyDoTuChoi) {
        YeuCauDoiTra yeuCau = yeuCauDoiTraRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu trả hàng."));

        if (!"PENDING".equals(yeuCau.getTrangThai())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý.");
        }

        yeuCau.setTrangThai("REJECTED");
        yeuCauDoiTraRepository.save(yeuCau);

        DonHang donHang = yeuCau.getDonHang();
        donHang.setTrangThai("HOAN_THANH");
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<YeuCauDoiTra> getReturnRequest(Integer donHangId) {
        return yeuCauDoiTraRepository.findByDonHangId(donHangId);
    }
}
