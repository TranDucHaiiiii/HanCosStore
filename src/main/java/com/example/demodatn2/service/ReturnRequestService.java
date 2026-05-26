package com.example.demodatn2.service;

import com.example.demodatn2.dto.ReturnOrderItemDTO;
import com.example.demodatn2.dto.ReturnRequestItemDTO;
import com.example.demodatn2.dto.ReturnRequestResponseDTO;
import com.example.demodatn2.entity.*;
import com.example.demodatn2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnRequestService {
    public static final String STATUS_CHO_DUYET = "CHO_DUYET";
    public static final String STATUS_DA_DUYET = "DA_DUYET";
    public static final String STATUS_TU_CHOI = "TU_CHOI";
    public static final String STATUS_CHO_KIEM_DINH = "CHO_KIEM_DINH";
    public static final String STATUS_DA_HOAN_TIEN = "DA_HOAN_TIEN";
    public static final String STATUS_HOAN_TAT = "HOAN_TAT";
    public static final String STATUS_KHACH_HUY = "KHACH_HUY";

    public static final String INSPECTION_PENDING = "PENDING_INSPECTION";
    public static final String INSPECTION_PASSED = "PASSED";
    public static final String INSPECTION_FAILED = "FAILED";

    private static final Set<String> VALID_STATUSES = Set.of(
            STATUS_CHO_DUYET, STATUS_DA_DUYET, STATUS_TU_CHOI,
            STATUS_CHO_KIEM_DINH, STATUS_DA_HOAN_TIEN, STATUS_HOAN_TAT,
            STATUS_KHACH_HUY
    );

    private final DonHangRepository donHangRepository;
    private final ChiTietDonHangRepository chiTietDonHangRepository;
    private final ChiTietDoiTraRepository chiTietDoiTraRepository;
    private final YeuCauDoiTraRepository yeuCauDoiTraRepository;
    private final HinhAnhDoiTraRepository hinhAnhDoiTraRepository;
    private final LichSuXuLyDoiTraRepository lichSuXuLyDoiTraRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final GiaoDichTonKhoRepository giaoDichTonKhoRepository;
    private final ReturnNotificationService notificationService;
    private final KhoHangHoanService khoHangHoanService;
    private final RefundTransactionRepository refundTransactionRepository;

    @Transactional(readOnly = true)
    public List<ReturnOrderItemDTO> getReturnableItems(Integer orderId, Integer customerId) {
        DonHang order = getOwnedCompletedOrder(orderId, customerId);
        return chiTietDonHangRepository.findByDonHang(order).stream()
                .map(item -> ReturnOrderItemDTO.builder()
                        .orderItemId(item.getId())
                        .productName(item.getTenSanPham())
                        .color(item.getMauSac())
                        .size(item.getKichCo())
                        .orderedQuantity(item.getSoLuong())
                        .build())
                .toList();
    }

    @Transactional
    public YeuCauDoiTra createRequest(Integer orderId,
                                      Integer customerId,
                                      String reason,
                                      String description,
                                      String refundMethod,
                                      List<ReturnRequestItemDTO> items,
                                      List<String> imageUrls) {
        DonHang order = getOwnedCompletedOrder(orderId, customerId);
        validateCreateRequest(reason, refundMethod, items, imageUrls);

        if (yeuCauDoiTraRepository.existsByDonHangId(orderId)) {
            throw new RuntimeException("Đơn hàng này đã có yêu cầu trả hàng.");
        }

        TaiKhoan customer = taiKhoanRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        YeuCauDoiTra request = new YeuCauDoiTra();
        request.setDonHang(order);
        request.setTaiKhoan(customer);
        request.setLyDo(reason.trim());
        request.setMoTaChiTiet(clean(description));
        request.setPhuongThucHoanTien(refundMethod.trim());
        request.setAnhMinhChung(imageUrls.get(0));
        request.setTrangThai(STATUS_CHO_DUYET);
        request.setNgayTao(Instant.now());
        request.setNgayCapNhat(Instant.now());
        request = yeuCauDoiTraRepository.save(request);

        List<Integer> requestedItemIds = items.stream()
                .map(ReturnRequestItemDTO::getOrderItemId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (requestedItemIds.size() != items.size()) {
            throw new RuntimeException("Sản phẩm trả không hợp lệ.");
        }
        Map<Integer, ChiTietDonHang> orderItemsById = chiTietDonHangRepository
                .findByDonHangIdAndIdIn(orderId, requestedItemIds)
                .stream()
                .collect(Collectors.toMap(ChiTietDonHang::getId, Function.identity()));

        for (ReturnRequestItemDTO dto : items) {
            ChiTietDonHang orderItem = orderItemsById.get(dto.getOrderItemId());
            if (orderItem == null) {
                throw new RuntimeException("Sản phẩm trả không tồn tại hoặc không thuộc đơn hàng này.");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0 || dto.getQuantity() > orderItem.getSoLuong()) {
                throw new RuntimeException("Số lượng trả không hợp lệ cho sản phẩm " + orderItem.getTenSanPham() + ".");
            }

            ChiTietDoiTra detail = new ChiTietDoiTra();
            detail.setYeuCauDoiTra(request);
            detail.setChiTietDonHang(orderItem);
            detail.setSoLuong(dto.getQuantity());
            detail.setInspectionStatus(INSPECTION_PENDING);
            chiTietDoiTraRepository.save(detail);
        }

        for (String url : imageUrls) {
            HinhAnhDoiTra image = new HinhAnhDoiTra();
            image.setYeuCauDoiTra(request);
            image.setDuongDanAnh(url);
            image.setNgayTao(Instant.now());
            hinhAnhDoiTraRepository.save(image);
        }

        order.setTrangThai("TRA_HANG");
        order.setNgayCapNhat(Instant.now());
        donHangRepository.save(order);

        addHistory(request, customer, "TAO_YEU_CAU", "Khách hàng gửi yêu cầu trả hàng.");
        return request;
    }

    @Transactional(readOnly = true)
    public List<YeuCauDoiTra> findAll(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return yeuCauDoiTraRepository.findAllByOrderByNgayTaoDesc();
        }
        return yeuCauDoiTraRepository.findByTrangThaiOrderByNgayTaoDesc(status.trim().toUpperCase());
    }

    @Transactional(readOnly = true)
    public YeuCauDoiTra getById(Integer id) {
        return yeuCauDoiTraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu trả hàng."));
    }

    public List<String> getNextValidStatuses(String currentStatus) {
        String normalized = normalizeExistingStatus(currentStatus);
        return switch (normalized) {
            case STATUS_CHO_DUYET -> List.of(STATUS_DA_DUYET, STATUS_TU_CHOI);
            case STATUS_DA_DUYET -> List.of(STATUS_CHO_KIEM_DINH);
            case STATUS_CHO_KIEM_DINH -> List.of(STATUS_DA_HOAN_TIEN);
            case STATUS_DA_HOAN_TIEN -> List.of(STATUS_HOAN_TAT);
            case STATUS_TU_CHOI, STATUS_HOAN_TAT, STATUS_KHACH_HUY -> List.of();
            default -> List.of();
        };
    }

    @Transactional
    public YeuCauDoiTra cancelByCustomer(Integer requestId, Integer customerId, String reason) {
        YeuCauDoiTra request = getById(requestId);
        if (request.getTaiKhoan() == null || !request.getTaiKhoan().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền hủy yêu cầu trả hàng này.");
        }

        String currentStatus = normalizeExistingStatus(request.getTrangThai());
        if (!STATUS_CHO_DUYET.equals(currentStatus)) {
            throw new RuntimeException("Chỉ có thể hủy yêu cầu trả hàng khi đang chờ duyệt.");
        }

        String cleanReason = clean(reason);
        request.setTrangThai(STATUS_KHACH_HUY);
        request.setGhiChuXuLy(cleanReason);
        request.setNgayCapNhat(Instant.now());
        yeuCauDoiTraRepository.save(request);

        DonHang order = request.getDonHang();
        if (order != null && "TRA_HANG".equalsIgnoreCase(order.getTrangThai())) {
            order.setTrangThai("HOAN_THANH");
            order.setNgayCapNhat(Instant.now());
            donHangRepository.save(order);
        }

        addHistory(request, request.getTaiKhoan(), STATUS_KHACH_HUY,
                cleanReason != null ? cleanReason : "Khách hàng hủy yêu cầu trả hàng.");
        return request;
    }

    @Transactional
    public YeuCauDoiTra updateStatus(Integer requestId, String status, String note, Integer actorId) {
        return updateStatus(requestId, status, note, actorId, null, null, null);
    }

    @Transactional
    public YeuCauDoiTra updateStatus(Integer requestId,
                                     String status,
                                     String note,
                                     Integer actorId,
                                     BigDecimal refundAmount,
                                     String refundMethod,
                                     String refundTransactionCode) {
        String normalized = normalizeStatus(status);
        YeuCauDoiTra request = getById(requestId);
        TaiKhoan actor = actorId != null ? taiKhoanRepository.findById(actorId).orElse(null) : null;
        String currentStatus = normalizeExistingStatus(request.getTrangThai());

        if (currentStatus.equals(normalized)) {
            return request;
        }

        List<String> nextValidStatuses = getNextValidStatuses(currentStatus);
        if (!nextValidStatuses.contains(normalized)) {
            throw new RuntimeException("Phải cập nhật trạng thái trả hàng theo đúng thứ tự. Trạng thái kế tiếp hợp lệ: "
                    + nextValidStatuses.stream().map(this::statusLabel).toList());
        }

        if (STATUS_DA_HOAN_TIEN.equals(normalized)) {
            ensureAllItemsInspected(requestId);
            createRefundTransaction(request, actor, refundAmount, refundMethod, refundTransactionCode, note);
        }

        request.setTrangThai(normalized);
        request.setGhiChuXuLy(clean(note));
        request.setNgayCapNhat(Instant.now());
        yeuCauDoiTraRepository.save(request);
        addHistory(request, actor, normalized, note);

        if (STATUS_DA_DUYET.equals(normalized)) {
            sendStatusNotification(request, "Yêu cầu trả hàng đã được duyệt",
                    "Yêu cầu trả hàng cho đơn " + request.getDonHang().getMaDonHang() + " đã được duyệt.");
        } else if (STATUS_TU_CHOI.equals(normalized)) {
            request.getDonHang().setTrangThai("HOAN_THANH");
            request.getDonHang().setNgayCapNhat(Instant.now());
            donHangRepository.save(request.getDonHang());
            sendStatusNotification(request, "Yêu cầu trả hàng bị từ chối",
                    "Yêu cầu trả hàng cho đơn " + request.getDonHang().getMaDonHang() + " bị từ chối. Ghi chú: " + clean(note));
        } else if (STATUS_DA_HOAN_TIEN.equals(normalized)) {
            sendStatusNotification(request, "Đơn trả hàng đã hoàn tiền",
                    "Hancos đã ghi nhận hoàn tiền cho đơn " + request.getDonHang().getMaDonHang() + ".");
        } else if (STATUS_HOAN_TAT.equals(normalized)) {
            request.getDonHang().setTrangThai("RETURNED");
            request.getDonHang().setNgayCapNhat(Instant.now());
            donHangRepository.save(request.getDonHang());
            sendStatusNotification(request, "Yêu cầu trả hàng hoàn tất",
                    "Yêu cầu trả hàng cho đơn " + request.getDonHang().getMaDonHang() + " đã hoàn tất.");
        }

        return request;
    }

    private void sendStatusNotification(YeuCauDoiTra request, String subject, String message) {
        if (request == null || request.getTaiKhoan() == null) {
            return;
        }
        notificationService.notifyStatus(request.getTaiKhoan().getEmail(), subject, message);
    }

    @Transactional
    public ChiTietDoiTra updateInspection(Integer detailId, String inspectionStatus, String note, Integer actorId) {
        String normalized = normalizeInspection(inspectionStatus);
        ChiTietDoiTra detail = chiTietDoiTraRepository.findWithReturnRequestById(detailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm kiểm định."));
        if (!INSPECTION_PENDING.equals(detail.getInspectionStatus())) {
            throw new RuntimeException("Sản phẩm này đã được kiểm định.");
        }

        YeuCauDoiTra request = detail.getYeuCauDoiTra();
        String requestStatus = normalizeExistingStatus(request.getTrangThai());
        if (!STATUS_DA_DUYET.equals(requestStatus) && !STATUS_CHO_KIEM_DINH.equals(requestStatus)) {
            throw new RuntimeException("Chi duoc kiem dinh sau khi yeu cau tra hang da duoc duyet.");
        }

        BienTheSanPham variant = detail.getChiTietDonHang().getBienTheSanPham();
        int quantity = detail.getSoLuong();
        if (INSPECTION_PASSED.equals(normalized)) {
            khoHangHoanService.createFromPassedInspection(detail, note);
        } else if (INSPECTION_FAILED.equals(normalized)) {
            variant.setSoLuongLoi((variant.getSoLuongLoi() != null ? variant.getSoLuongLoi() : 0) + quantity);
            bienTheSanPhamRepository.save(variant);
        }

        detail.setInspectionStatus(normalized);
        detail.setGhiChu(clean(note));
        chiTietDoiTraRepository.save(detail);

        request.setTrangThai(STATUS_CHO_KIEM_DINH);
        request.setNgayCapNhat(Instant.now());
        yeuCauDoiTraRepository.save(request);

        TaiKhoan actor = actorId != null ? taiKhoanRepository.findById(actorId).orElse(null) : null;
        addHistory(request, actor, "KIEM_DINH_" + normalized, note);
        return detail;
    }

    public ReturnRequestResponseDTO toResponse(YeuCauDoiTra request) {
        List<String> images = hinhAnhDoiTraRepository.findByYeuCauDoiTraIdOrderByIdAsc(request.getId()).stream()
                .map(HinhAnhDoiTra::getDuongDanAnh)
                .toList();
        return ReturnRequestResponseDTO.builder()
                .id(request.getId())
                .orderId(request.getDonHang().getId())
                .orderCode(request.getDonHang().getMaDonHang())
                .customerName(request.getTaiKhoan().getHoTen())
                .reason(request.getLyDo())
                .status(request.getTrangThai())
                .refundMethod(request.getPhuongThucHoanTien())
                .createdAt(request.getNgayTao())
                .imageUrls(images)
                .build();
    }

    private DonHang getOwnedCompletedOrder(Integer orderId, Integer customerId) {
        DonHang order = donHangRepository.findOwnedCompletedForReturn(orderId, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng."));
        Instant returnWindowStart = order.getNgayCapNhat() != null ? order.getNgayCapNhat() : order.getNgayDat();
        if (returnWindowStart != null && returnWindowStart.plus(7, ChronoUnit.DAYS).isBefore(Instant.now())) {
            throw new RuntimeException("Đã quá 7 ngày kể từ ngày hoàn thành đơn hàng, không thể yêu cầu trả hàng.");
        }
        return order;
    }

    private void validateCreateRequest(String reason, String refundMethod, List<ReturnRequestItemDTO> items, List<String> imageUrls) {
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Vui lòng chọn lý do trả hàng.");
        }
        if (refundMethod == null || refundMethod.isBlank()) {
            throw new RuntimeException("Vui lòng chọn phương thức hoàn tiền.");
        }
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn sản phẩm và số lượng trả.");
        }
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new RuntimeException("Vui lòng upload ảnh minh chứng.");
        }
    }

    private String normalizeStatus(String status) {
        String normalized = status != null ? status.trim().toUpperCase() : "";
        if (!VALID_STATUSES.contains(normalized)) {
            throw new RuntimeException("Trạng thái trả hàng không hợp lệ.");
        }
        return normalized;
    }

    private String normalizeExistingStatus(String status) {
        String normalized = status != null ? status.trim().toUpperCase() : "";
        return switch (normalized) {
            case "PENDING" -> STATUS_CHO_DUYET;
            case "APPROVED" -> STATUS_DA_DUYET;
            case "REJECTED" -> STATUS_TU_CHOI;
            case "CUSTOMER_CANCELLED", "CUSTOMER_CANCELED" -> STATUS_KHACH_HUY;
            default -> normalized;
        };
    }

    private void ensureAllItemsInspected(Integer requestId) {
        if (chiTietDoiTraRepository.existsByYeuCauDoiTraIdAndInspectionStatus(requestId, INSPECTION_PENDING)) {
            throw new RuntimeException("Phai kiem dinh tat ca san pham tra truoc khi cap nhat da hoan tien.");
        }
    }

    private void createRefundTransaction(YeuCauDoiTra request,
                                         TaiKhoan actor,
                                         BigDecimal refundAmount,
                                         String refundMethod,
                                         String refundTransactionCode,
                                         String note) {
        if (refundTransactionRepository.existsByYeuCauDoiTra_Id(request.getId())) {
            throw new RuntimeException("Yeu cau nay da co giao dich hoan tien.");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Vui long nhap so tien hoan hop le.");
        }
        BigDecimal maxRefundAmount = calculateMaxRefundAmount(request.getId());
        if (refundAmount.compareTo(maxRefundAmount) > 0) {
            throw new RuntimeException("So tien hoan khong duoc vuot qua gia tri san pham tra dat kiem dinh: "
                    + maxRefundAmount.toPlainString());
        }

        String method = clean(refundMethod);
        if (method == null) {
            method = clean(request.getPhuongThucHoanTien());
        }
        if (method == null) {
            throw new RuntimeException("Vui long chon phuong thuc hoan tien.");
        }
        if ("BANK_TRANSFER".equalsIgnoreCase(method) && clean(refundTransactionCode) == null) {
            throw new RuntimeException("Vui long nhap ma giao dich ngan hang.");
        }

        RefundTransaction tx = new RefundTransaction();
        tx.setYeuCauDoiTra(request);
        tx.setSoTienHoan(refundAmount);
        tx.setPhuongThucHoanTien(method.trim().toUpperCase());
        tx.setMaGiaoDich(clean(refundTransactionCode));
        tx.setNguoiXuLy(actor);
        tx.setThoiGianHoan(Instant.now());
        tx.setGhiChu(clean(note));
        refundTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateMaxRefundAmount(Integer requestId) {
        return chiTietDoiTraRepository.findByYeuCauDoiTraId(requestId).stream()
                .map(this::calculateReturnDetailAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateReturnDetailAmount(ChiTietDoiTra detail) {
        ChiTietDonHang orderItem = detail.getChiTietDonHang();
        BigDecimal unitPrice = orderItem.getDonGia();
        if (unitPrice == null && orderItem.getThanhTien() != null && orderItem.getSoLuong() != null && orderItem.getSoLuong() > 0) {
            unitPrice = orderItem.getThanhTien().divide(BigDecimal.valueOf(orderItem.getSoLuong()), 2, java.math.RoundingMode.HALF_UP);
        }
        if (unitPrice == null || detail.getSoLuong() == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(detail.getSoLuong()));
    }

    public String statusLabel(String status) {
        return switch (normalizeExistingStatus(status)) {
            case STATUS_CHO_DUYET -> "Chờ duyệt";
            case STATUS_DA_DUYET -> "Đã duyệt";
            case STATUS_TU_CHOI -> "Từ chối";
            case STATUS_CHO_KIEM_DINH -> "Chờ kiểm định";
            case STATUS_DA_HOAN_TIEN -> "Đã hoàn tiền";
            case STATUS_HOAN_TAT -> "Hoàn tất";
            case STATUS_KHACH_HUY -> "Khách hủy";
            default -> status;
        };
    }

    private String normalizeInspection(String status) {
        String normalized = status != null ? status.trim().toUpperCase() : "";
        if (!Set.of(INSPECTION_PASSED, INSPECTION_FAILED).contains(normalized)) {
            throw new RuntimeException("Trạng thái kiểm định không hợp lệ.");
        }
        return normalized;
    }

    private void addHistory(YeuCauDoiTra request, TaiKhoan actor, String action, String note) {
        LichSuXuLyDoiTra history = new LichSuXuLyDoiTra();
        history.setYeuCauDoiTra(request);
        history.setNguoiXuLy(actor);
        history.setHanhDong(action);
        history.setGhiChu(clean(note));
        history.setThoiGian(Instant.now());
        lichSuXuLyDoiTraRepository.save(history);
    }

    private void logStockTransaction(BienTheSanPham variant, String type, Integer quantity, DonHang order, String note) {
        GiaoDichTonKho tx = new GiaoDichTonKho();
        tx.setBienTheSanPham(variant);
        tx.setLoai(type);
        tx.setSoLuong(quantity);
        tx.setThamChieuLoai("TRA_HANG");
        tx.setThamChieuId(order.getId());
        tx.setGhiChu(note);
        tx.setNgayTao(Instant.now());
        giaoDichTonKhoRepository.save(tx);
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
