package com.example.demodatn2.service;

import com.example.demodatn2.dto.PosOrderRequestDTO;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final GioHangRepository gioHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final MaGiamGiaRepository maGiamGiaRepository;
    private final LichSuSuDungMaGiamGiaRepository lichSuSuDungMaGiamGiaRepository;
    private final YeuCauDoiTraRepository yeuCauDoiTraRepository;
    private final GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final OrderConfirmationEmailService orderConfirmationEmailService;
    @Getter
    private final VoucherService voucherService;

    @Transactional(readOnly = true)
    public List<DonHang> getAllOrders() {
        return donHangRepository.findAllByOrderByNgayDatDesc();
    }

    @Transactional(readOnly = true)
    public List<DonHang> searchOrders(String keyword, String status, String channel) {
        String normalizedStatus = normalizeStatus(status);
        String normalizedChannel = normalizeChannel(channel);

        List<DonHang> sourceOrders;
        if (keyword == null || keyword.trim().isEmpty()) {
            sourceOrders = getAllOrders();
        } else {
            sourceOrders = donHangRepository.timKiemTheoTuKhoa(keyword.trim());
        }

        List<DonHang> statusFiltered = filterOrdersByStatus(sourceOrders, normalizedStatus);
        List<DonHang> channelFiltered = filterOrdersByChannel(statusFiltered, normalizedChannel);
        return filterOrdersForDefaultAdminView(channelFiltered, normalizedStatus);
    }

    @Transactional(readOnly = true)
    public Page<DonHang> searchOrdersPage(String keyword, String status, String channel, int page, int size) {
        List<DonHang> filtered = searchOrders(keyword, status, channel);
        return paginateOrders(filtered, page, size);
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
        counts.put("ALL", orders.stream()
                .filter(order -> !isCancelledPosTransferOrder(order))
                .count());
        counts.put("CHO_XAC_NHAN", 0L);
        counts.put("DA_XAC_NHAN", 0L);
        counts.put("DANG_GIAO", 0L);
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

    private List<DonHang> filterOrdersForDefaultAdminView(List<DonHang> orders, String normalizedStatus) {
        if (!"ALL".equals(normalizedStatus)) {
            return orders;
        }
        return orders.stream()
                .filter(order -> !isCancelledPosTransferOrder(order))
                .toList();
    }

    private List<DonHang> filterOrdersByStatus(List<DonHang> orders, String normalizedStatus) {
        if (normalizedStatus == null || normalizedStatus.isEmpty() || "ALL".equals(normalizedStatus)) {
            return orders;
        }
        return orders.stream()
                .filter(order -> normalizedStatus.equals(normalizeStatus(order.getTrangThai())))
                .toList();
    }

    private List<DonHang> filterOrdersByChannel(List<DonHang> orders, String normalizedChannel) {
        if (normalizedChannel == null || normalizedChannel.isEmpty() || "ALL".equals(normalizedChannel)) {
            return orders;
        }
        if ("POS".equals(normalizedChannel)) {
            return orders.stream()
                    .filter(this::isPosCounterOrder)
                    .toList();
        }
        if ("ONLINE".equals(normalizedChannel)) {
            return orders.stream()
                    .filter(order -> !isPosCounterOrder(order))
                    .toList();
        }
        return orders;
    }

    private String normalizeChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            return "ALL";
        }
        return channel.trim().toUpperCase();
    }

    private boolean isPosCounterOrder(DonHang order) {
        return order != null
                && order.getDiaChiNhan() != null
                && "Mua tại quầy".equalsIgnoreCase(order.getDiaChiNhan().trim());
    }

    private boolean isCancelledPosTransferOrder(DonHang order) {
        return order != null
                && "DA_HUY".equals(normalizeStatus(order.getTrangThai()))
                && isPosCounterOrder(order)
                && isBankTransferPayment(order.getPhuongThucThanhToan());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ALL";
        }

        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PENDING" -> "CHO_XAC_NHAN";
            case "PAID" -> "DA_XAC_NHAN";
            case "CONFIRMED" -> "DA_XAC_NHAN";
            case "SHIPPING" -> "DANG_GIAO";
            case "LOI_VAN_CHUYEN", "LOST" -> "DA_HUY";
            case "DELIVERED", "COMPLETED" -> "HOAN_THANH";
            case "CANCELLED" -> "DA_HUY";
            case "RETURN_REQUESTED", "RETURNED" -> "TRA_HANG";
            default -> normalized;
        };
    }

    private Page<DonHang> paginateOrders(List<DonHang> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int start = Math.min(safePage * safeSize, items.size());
        int end = Math.min(start + safeSize, items.size());
        return new PageImpl<>(items.subList(start, end), PageRequest.of(safePage, safeSize), items.size());
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

    /**
     * Lấy danh sách trạng thái hợp lệ tiếp theo từ trạng thái hiện tại
     */
    public List<String> getNextValidStatuses(String currentStatus) {
        String normalized = normalizeStatus(currentStatus);
        return switch (normalized) {
            case "CHO_XAC_NHAN" -> List.of("DA_XAC_NHAN", "DA_HUY");
            case "DA_XAC_NHAN" -> List.of("DANG_GIAO", "DA_HUY");
            case "DANG_GIAO" -> List.of("HOAN_THANH");
            case "HOAN_THANH" -> List.of();      // Locked - no transitions
            case "DA_HUY" -> List.of();          // Locked - no transitions
            case "TRA_HANG" -> List.of();        // Locked - no transitions
            default -> List.of();
        };
    }

    /**
     * Convert status code to Vietnamese label
     */
    public String getStatusLabel(String status) {
        return switch (status) {
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DA_XAC_NHAN" -> "Đã xác nhận";
            case "PAID" -> "Đã thanh toán";
            case "DANG_GIAO" -> "Đang giao hàng";
            case "HOAN_THANH", "COMPLETED", "DELIVERED" -> "Hoàn thành";
            case "DA_HUY", "CANCELLED" -> "Đã hủy";
            case "TRA_HANG", "RETURN_REQUESTED", "RETURNED" -> "Trả hàng";
            default -> status;
        };
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

        // Kiểm soát trạng thái hợp lệ
        List<String> validNextStatuses = getNextValidStatuses(currentStatus);
        if (validNextStatuses.isEmpty() || !validNextStatuses.contains(newStatus)) {
            throw new RuntimeException("Không thể chuyển từ trạng thái '" + currentStatus + 
                    "' sang '" + newStatus + "'. Các trạng thái hợp lệ: " + validNextStatuses);
        }

        if ("DA_XAC_NHAN".equals(newStatus)
                && isBankTransferPayment(donHang.getPhuongThucThanhToan())
                && !hasPaidTransaction(donHang)) {
            throw new RuntimeException("Đơn chuyển khoản chưa được SePay xác nhận thanh toán.");
        }

        if ("DA_HUY".equals(newStatus)) {
            restoreStock(donHang);
            restoreVoucherUsage(donHang);
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

        if (!"CHO_XAC_NHAN".equals(currentStatus) && !"DA_XAC_NHAN".equals(currentStatus)) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái: " + currentStatus);
        }

        donHang.setTrangThai("DA_HUY");
        donHang.setLyDoHuy(reason);
        donHang.setNgayCapNhat(Instant.now());
        
        restoreStock(donHang);
        restoreVoucherUsage(donHang);
        donHangRepository.save(donHang);
    }

    @Transactional
    public int autoCompleteDeliveredOrders(int days) {
        if (days <= 0) {
            return 0;
        }

        Instant threshold = Instant.now().minus(days, ChronoUnit.DAYS);
        List<DonHang> deliveredOrders = donHangRepository.timTheoTrangThaiVaCapNhatTruoc("DANG_GIAO", threshold);

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

    @Transactional
    public int cleanupExpiredPendingPosOrders(int minutes) {
        return cleanupExpiredPendingTransferOrders(minutes);
    }

    @Transactional
    public int cleanupExpiredPendingTransferOrders(int minutes) {
        if (minutes <= 0) {
            return 0;
        }

        Instant threshold = Instant.now().minus(minutes, ChronoUnit.MINUTES);
        List<DonHang> expiredOrders = new java.util.ArrayList<>();
        expiredOrders.addAll(donHangRepository.timTheoTrangThaiVaCapNhatTruoc("PENDING", threshold));
        expiredOrders.addAll(donHangRepository.timTheoTrangThaiVaCapNhatTruoc("CHO_XAC_NHAN", threshold));

        int cleaned = 0;
        for (DonHang donHang : expiredOrders) {
            String currentStatus = normalizeStatus(donHang.getTrangThai());
            if (!"PENDING".equalsIgnoreCase(donHang.getTrangThai()) && !"CHO_XAC_NHAN".equals(currentStatus)) {
                continue;
            }
            if (!isBankTransferPayment(donHang.getPhuongThucThanhToan())) {
                continue;
            }

            donHang.setTrangThai("DA_HUY");
            donHang.setLyDoHuy("Quá hạn chờ thanh toán chuyển khoản");
            donHang.setNgayCapNhat(Instant.now());
            restoreStock(donHang);
            restoreVoucherUsage(donHang);
            donHangRepository.save(donHang);
            cleaned++;
        }

        return cleaned;
    }

    private void restoreStock(DonHang donHang) {
        List<ChiTietDonHang> items = chiTietDonHangRepository.findByDonHang(donHang);
        for (ChiTietDonHang item : items) {
            BienTheSanPham bt = item.getBienTheSanPham();
            if (bt != null) {
                bt.setSoLuongTon(bt.getSoLuongTon() + item.getSoLuong());
                bienTheSanPhamRepository.save(bt);
                logStockTransaction(bt, "NHAP", item.getSoLuong(), donHang,
                        "Hoan kho tu don " + donHang.getMaDonHang());
            }
        }
    }

    private void restoreVoucherUsage(DonHang donHang) {
        if (donHang == null || donHang.getMaGiamGia() == null || donHang.getMaGiamGia().getId() == null) {
            return;
        }
        maGiamGiaRepository.decrementUsageAfterOrderCancel(donHang.getMaGiamGia().getId());
    }

    private void logStockTransaction(BienTheSanPham bienThe,
                                     String loai,
                                     Integer soLuong,
                                     DonHang donHang,
                                     String ghiChu) {
        if (bienThe == null || soLuong == null || soLuong <= 0) {
            return;
        }

        GiaoDichTonKho giaoDich = new GiaoDichTonKho();
        giaoDich.setBienTheSanPham(bienThe);
        giaoDich.setLoai(loai);
        giaoDich.setSoLuong(soLuong);
        giaoDich.setThamChieuLoai("DON_HANG");
        giaoDich.setThamChieuId(donHang != null ? donHang.getId() : null);
        giaoDich.setGhiChu(ghiChu);
        giaoDich.setNgayTao(Instant.now());
        giaoDichTonKhoRepository.save(giaoDich);
    }

    @Transactional
    public void updateOrderAddress(Integer orderId, String hoTen, String soDienThoai, String diaChi) {
        updateOrderAddress(orderId, hoTen, soDienThoai, diaChi, null);
    }

    @Transactional
    public void updateOrderAddress(Integer orderId, String hoTen, String soDienThoai, String diaChi, BigDecimal shippingFee) {
        DonHang donHang = getOrderById(orderId);
        String status = normalizeStatus(donHang.getTrangThai());

        if (!"CHO_XAC_NHAN".equals(status) && !"DA_XAC_NHAN".equals(status)) {
            throw new RuntimeException("Không thể thay đổi địa chỉ cho đơn hàng ở trạng thái: " + status);
        }

        if ("DA_XAC_NHAN".equals(status) && isBankTransferPayment(donHang.getPhuongThucThanhToan())) {
            throw new RuntimeException("Không thể thay đổi địa chỉ cho đơn hàng chuyển khoản đã xác nhận.");
        }

        donHang.setHoTenNhan(hoTen);
        donHang.setSoDienThoaiNhan(soDienThoai);
        donHang.setDiaChiNhan(diaChi);
        if (shippingFee != null && shippingFee.compareTo(BigDecimal.ZERO) >= 0) {
            donHang.setPhiVanChuyen(shippingFee);
            BigDecimal tamTinh = donHang.getTamTinh() != null ? donHang.getTamTinh() : BigDecimal.ZERO;
            BigDecimal giamGia = donHang.getGiamGia() != null ? donHang.getGiamGia() : BigDecimal.ZERO;
            donHang.setTongTien(tamTinh.subtract(giamGia).add(shippingFee));
        }
        donHang.setNgayCapNhat(Instant.now());

        donHangRepository.save(donHang);
    }

    private boolean isBankTransferPayment(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }

        String normalized = paymentMethod.trim().toUpperCase();
        return normalized.contains("TRANSFER")
                || normalized.contains("CHUYEN_KHOAN")
                || normalized.contains("CHUYENKHOAN")
                || normalized.contains("CHUYEN KHOAN")
                || normalized.contains("SEPAY");
    }

    private boolean hasPaidTransaction(DonHang donHang) {
        return giaoDichThanhToanRepository
                .findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(donHang, "SEPAY")
                .filter(tx -> "PAID".equalsIgnoreCase(tx.getTrangThai()))
                .isPresent();
    }

    @Transactional
    public void updateOrderPaymentMethod(Integer orderId, String paymentMethod) {
        DonHang donHang = getOrderById(orderId);
        String status = normalizeStatus(donHang.getTrangThai());

        if (!"CHO_XAC_NHAN".equals(status) && !"PENDING".equals(status)) {
            throw new RuntimeException("Không thể thay đổi phương thức thanh toán cho đơn hàng ở trạng thái: " + status);
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn phương thức thanh toán");
        }

        donHang.setPhuongThucThanhToan(paymentMethod.trim().toUpperCase());
        donHang.setNgayCapNhat(Instant.now());
        donHangRepository.save(donHang);
    }

    @Transactional
    public DonHang createOrder(String hoTen, String soDienThoai, String email, String diaChi, String ghiChu, String paymentMethod, BigDecimal shippingFeeFromForm, HttpSession session) {
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
        java.util.Set<Integer> selectedIds = cartSelectedIds(session);
        if (session.getAttribute(CartService.SELECTED_CART_ITEM_IDS) == null) {
            selectedIds = gioHang.getChiTiets().stream()
                    .map(ChiTietGioHang::getId)
                    .collect(java.util.stream.Collectors.toSet());
        }
        java.util.Set<Integer> checkoutItemIds = selectedIds;
        List<ChiTietGioHang> selectedCartItems = gioHang.getChiTiets().stream()
                .filter(item -> checkoutItemIds.contains(item.getId()))
                .toList();
        if (selectedCartItems.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
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
        if ("SEPAY".equalsIgnoreCase(paymentMethod)) {
            donHang.setTrangThai("PENDING");
        } else {
            donHang.setTrangThai("CHO_XAC_NHAN");
        }
        
        donHang.setNgayDat(Instant.now());
        
        BigDecimal tamTinh = BigDecimal.ZERO;
        for (ChiTietGioHang item : selectedCartItems) {
            tamTinh = tamTinh.add(item.getDonGia().multiply(new BigDecimal(item.getSoLuong())));
        }
        donHang.setTamTinh(tamTinh);
        // Phí ship chỉ lấy từ GHTK/form; không dùng công thức theo giá trị đơn hàng.
        BigDecimal phiVanChuyen = (shippingFeeFromForm != null && shippingFeeFromForm.compareTo(BigDecimal.ZERO) > 0)
            ? shippingFeeFromForm
            : BigDecimal.ZERO;
        donHang.setPhiVanChuyen(phiVanChuyen);
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
            int updatedVoucher = maGiamGiaRepository.incrementUsageIfAvailable(appliedVoucher.getId());
            if (updatedVoucher == 0) {
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
            }
            
            LichSuSuDungMaGiamGia lichSu = new LichSuSuDungMaGiamGia();
            lichSu.setDonHang(donHang);
            lichSu.setTaiKhoan(taiKhoan);
            lichSu.setMaGiamGia(appliedVoucher);
            lichSu.setThoiGianSuDung(Instant.now());
            lichSuSuDungMaGiamGiaRepository.save(lichSu);
        }

        List<ChiTietDonHang> orderItems = new ArrayList<>();
        for (ChiTietGioHang item : selectedCartItems) {
            BienTheSanPham bt = item.getBienTheSanPham();

            int updatedStock = bienTheSanPhamRepository.decrementStockIfEnough(bt.getId(), item.getSoLuong());
            if (updatedStock == 0) {
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
            
            ChiTietDonHang savedItem = chiTietDonHangRepository.save(ctdh);
            orderItems.add(savedItem);

            logStockTransaction(bt, "XUAT", item.getSoLuong(), donHang,
                    "Tru kho tu don " + donHang.getMaDonHang());
        }
        
        // Chỉ xóa những sản phẩm đã chọn thanh toán, giữ lại các sản phẩm chưa chọn trong giỏ.
        chiTietGioHangRepository.deleteAll(selectedCartItems);
        if (selectedCartItems.size() == gioHang.getChiTiets().size()) {
            gioHangRepository.delete(gioHang);
        }

        // Xóa thông tin voucher khỏi session
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        session.removeAttribute("DISCOUNT_AMOUNT");
        session.removeAttribute(CartService.SELECTED_CART_ITEM_IDS);

        orderConfirmationEmailService.sendOrderConfirmation(donHang, orderItems);

        return donHang;
    }

    private java.util.Set<Integer> cartSelectedIds(HttpSession session) {
        Object raw = session.getAttribute(CartService.SELECTED_CART_ITEM_IDS);
        if (raw instanceof java.util.Set<?> rawSet) {
            return rawSet.stream()
                    .filter(Integer.class::isInstance)
                    .map(Integer.class::cast)
                    .collect(java.util.stream.Collectors.toSet());
        }
        return java.util.Set.of();
    }

    /**
     * Tạo đơn hàng từ POS (bán hàng tại quầy) — không qua giỏ hàng.
     */
    @Transactional
    public DonHang createPosOrder(PosOrderRequestDTO req, TaiKhoanDTO staffUser) {
        DonHang convertedOrder = convertPendingTransferToCashCheckout(req);
        if (convertedOrder != null) {
            return convertedOrder;
        }
        return buildPosOrder(req, staffUser, "HOAN_THANH", null);
    }

    /**
     * Tạo đơn POS chuyển khoản ở trạng thái chờ xác nhận, dùng mã đơn đã reserve trước.
     */
    @Transactional
    public DonHang createPendingPosTransferOrder(PosOrderRequestDTO req, TaiKhoanDTO staffUser) {
        String orderCode = req.getOrderCode();
        if (orderCode == null || orderCode.trim().isEmpty()) {
            orderCode = "DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        req.setOrderCode(orderCode);
        req.setPaymentMethod("transfer");
        return buildPosOrder(req, staffUser, "PENDING", orderCode);
    }

    @Transactional
    public boolean cancelPendingPosTransferOrder(String orderCode, String reason) {
        if (orderCode == null || orderCode.trim().isEmpty()) {
            return false;
        }

        DonHang donHang = donHangRepository.findByMaDonHangIgnoreCase(orderCode.trim())
                .orElse(null);
        if (donHang == null) {
            return false;
        }

        String currentStatus = normalizeStatus(donHang.getTrangThai());
        if ("DA_HUY".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            return false;
        }
        if (!"PENDING".equals(currentStatus) && !"CHO_XAC_NHAN".equals(currentStatus)) {
            throw new RuntimeException("Đơn chuyển khoản đã được xử lý, không thể hủy.");
        }
        if (!isBankTransferPayment(donHang.getPhuongThucThanhToan())) {
            return false;
        }
        if (hasPaidTransaction(donHang)) {
            throw new RuntimeException("Đơn chuyển khoản đã được SePay xác nhận thanh toán.");
        }

        donHang.setTrangThai("DA_HUY");
        donHang.setLyDoHuy(reason != null && !reason.isBlank()
                ? reason.trim()
                : "Hủy đơn POS chuyển khoản chờ thanh toán");
        donHang.setNgayCapNhat(Instant.now());
        restoreStock(donHang);
        restoreVoucherUsage(donHang);
        donHangRepository.save(donHang);
        return true;
    }

    private DonHang convertPendingTransferToCashCheckout(PosOrderRequestDTO req) {
        if (req == null || req.getPendingTransferOrderCode() == null || req.getPendingTransferOrderCode().isBlank()) {
            return null;
        }
        String paymentMethod = req.getPaymentMethod() != null ? req.getPaymentMethod().trim().toLowerCase() : "cash";
        if (!"cash".equals(paymentMethod)) {
            return null;
        }

        DonHang donHang = donHangRepository.findByMaDonHangIgnoreCase(req.getPendingTransferOrderCode().trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn chuyển khoản cần đổi sang tiền mặt"));
        String currentStatus = normalizeStatus(donHang.getTrangThai());
        if (!"PENDING".equals(currentStatus) && !"CHO_XAC_NHAN".equals(currentStatus)) {
            throw new RuntimeException("Đơn chuyển khoản đã được xử lý, không thể đổi sang tiền mặt.");
        }
        if (!isBankTransferPayment(donHang.getPhuongThucThanhToan())) {
            throw new RuntimeException("Đơn cần đổi không phải đơn chuyển khoản.");
        }
        if (hasPaidTransaction(donHang)) {
            throw new RuntimeException("Đơn chuyển khoản đã được SePay xác nhận thanh toán.");
        }

        BigDecimal cashGiven = req.getCashGiven() != null ? req.getCashGiven() : BigDecimal.ZERO;
        BigDecimal total = donHang.getTongTien() != null ? donHang.getTongTien() : BigDecimal.ZERO;
        if (cashGiven.compareTo(total) < 0) {
            throw new RuntimeException("Tiền khách đưa chưa đủ!");
        }

        TaiKhoan customer = null;
        if (req.getCustomerId() != null) {
            customer = taiKhoanRepository.findById(req.getCustomerId()).orElse(null);
        }
        donHang.setTaiKhoan(customer);
        donHang.setHoTenNhan(req.getCustomerName().trim());
        donHang.setSoDienThoaiNhan(req.getCustomerPhone().trim());
        donHang.setEmailNhan(customer != null ? customer.getEmail() : donHang.getEmailNhan());
        donHang.setGhiChu(req.getNote());
        donHang.setPhuongThucThanhToan("cash");
        donHang.setTrangThai("HOAN_THANH");
        donHang.setLyDoHuy(null);
        donHang.setNgayCapNhat(Instant.now());
        return donHangRepository.save(donHang);
    }

    private DonHang buildPosOrder(PosOrderRequestDTO req, TaiKhoanDTO staffUser, String trangThai, String orderCode) {
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
        // Tạo mã đơn hàng cùng format với web: DH-XXXXXXXX, hoặc dùng mã transfer đã reserve trước.
        donHang.setMaDonHang(orderCode != null && !orderCode.trim().isEmpty()
            ? orderCode.trim()
            : "DH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donHang.setTaiKhoan(customer);
        donHang.setHoTenNhan(req.getCustomerName().trim());
        donHang.setSoDienThoaiNhan(req.getCustomerPhone().trim());
        donHang.setEmailNhan(customer != null ? customer.getEmail() : null);
        donHang.setDiaChiNhan("Mua tại quầy");
        donHang.setGhiChu(req.getNote());
        donHang.setPhuongThucThanhToan(req.getPaymentMethod() != null ? req.getPaymentMethod() : "cash");
        donHang.setTrangThai(trangThai); // POS thường hoàn thành ngay, transfer có thể chờ webhook xác nhận
        donHang.setPhiVanChuyen(BigDecimal.ZERO); // Không ship
        donHang.setNgayDat(Instant.now());

        BigDecimal tamTinh = BigDecimal.ZERO;

        // Validate & tính tạm tính
        for (PosOrderRequestDTO.PosItemDTO item : req.getItems()) {
            if (item.getVariantId() == null || item.getQty() == null || item.getQty() <= 0) {
                throw new RuntimeException("Sản phẩm hoặc số lượng không hợp lệ");
            }
            BienTheSanPham bt = bienTheSanPhamRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Biến thể không tồn tại: " + item.getVariantId()));
            if (bt.getSoLuongTon() < item.getQty()) {
                throw new RuntimeException("Sản phẩm " + bt.getSanPham().getTen() +
                        " (" + bt.getMauSac() + "/" + bt.getKichCo() + ") chỉ còn " + bt.getSoLuongTon());
            }
            BigDecimal currentPrice = bt.getGia() != null ? bt.getGia() : BigDecimal.ZERO;
            item.setPrice(currentPrice);
            tamTinh = tamTinh.add(currentPrice.multiply(new BigDecimal(item.getQty())));
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
            int updatedVoucher = maGiamGiaRepository.incrementUsageIfAvailable(appliedVoucher.getId());
            if (updatedVoucher == 0) {
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
            }

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

            int updatedStock = bienTheSanPhamRepository.decrementStockIfEnough(bt.getId(), item.getQty());
            if (updatedStock == 0) {
                throw new RuntimeException("Sản phẩm " + bt.getSanPham().getTen() +
                        " (" + bt.getMauSac() + "/" + bt.getKichCo() + ") không đủ số lượng trong kho");
            }

            ChiTietDonHang ctdh = new ChiTietDonHang();
            ctdh.setDonHang(donHang);
            ctdh.setBienTheSanPham(bt);
            ctdh.setTenSanPham(bt.getSanPham().getTen());
            ctdh.setMauSac(bt.getMauSac());
            ctdh.setKichCo(bt.getKichCo());
            ctdh.setSoLuong(item.getQty());
            BigDecimal currentPrice = bt.getGia() != null ? bt.getGia() : BigDecimal.ZERO;
            ctdh.setDonGia(currentPrice);
            ctdh.setThanhTien(currentPrice.multiply(new BigDecimal(item.getQty())));
            chiTietDonHangRepository.save(ctdh);

            logStockTransaction(bt, "XUAT", item.getQty(), donHang,
                    "Tru kho tu don POS " + donHang.getMaDonHang());
        }

        return donHang;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<YeuCauDoiTra> getReturnRequest(Integer donHangId) {
        return yeuCauDoiTraRepository.findByDonHangId(donHangId);
    }
}
