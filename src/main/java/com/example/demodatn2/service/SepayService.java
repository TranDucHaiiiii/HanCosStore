package com.example.demodatn2.service;

import com.example.demodatn2.dto.OrderStatusResponse;
import com.example.demodatn2.dto.SepayWebhookRequest;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GiaoDichThanhToan;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.GiaoDichThanhToanRepository;
import com.example.demodatn2.repository.PhuongThucThanhToanRepository;
import com.example.demodatn2.entity.PhuongThucThanhToan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SepayService {
    private static final String PROVIDER = "SEPAY";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PAID = "PAID";
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)\\bDH-?[A-Z0-9]+\\b");

    private final DonHangRepository donHangRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void handleWebhook(SepayWebhookRequest payload) {
        BigDecimal amount = payload.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien khong hop le");
        }

        DonHang order = findOrderFromWebhook(payload)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang trong webhook SePay"));

        if (order.getTongTien() == null || order.getTongTien().compareTo(amount) != 0) {
            throw new IllegalArgumentException("So tien khong khop don hang");
        }

        Optional<GiaoDichThanhToan> existingTx = giaoDichThanhToanRepository
                .findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, PROVIDER);

        if (existingTx.isPresent() && STATUS_PAID.equalsIgnoreCase(existingTx.get().getTrangThai())) {
            return;
        }

        String orderStatus = order.getTrangThai() != null ? order.getTrangThai().toUpperCase() : "";
        if (!"PENDING".equals(orderStatus) && !"CHO_XAC_NHAN".equals(orderStatus)) {
            return;
        }

        GiaoDichThanhToan tx = existingTx.orElseGet(GiaoDichThanhToan::new);
        tx.setDonHang(order);
        tx.setSoTien(amount);
        // ensure payment method reference is set (DB has non-null PhuongThucThanhToanId)
        // Try to use an existing payment-method record. Prefer a record with Ma = PROVIDER
        // If not found, fall back to common existing methods (e.g. 'BANK'), then any available
        // record. Only create a new PHUONG_THUC_THANH_TOAN as a last resort.
        PhuongThucThanhToan pm = phuongThucThanhToanRepository.findByMa(PROVIDER)
                .orElseGet(() -> phuongThucThanhToanRepository.findByMa("BANK")
                        .orElseGet(() -> phuongThucThanhToanRepository.findAll().stream().findFirst()
                                .orElseGet(() -> {
                                    PhuongThucThanhToan p = new PhuongThucThanhToan();
                                    p.setMa(PROVIDER);
                                    p.setTen(PROVIDER);
                                    p.setTrangThai("ACTIVE");
                                    return phuongThucThanhToanRepository.save(p);
                                })));
        tx.setPhuongThucThanhToan(pm);
        tx.setNhaCungCap(PROVIDER);
        tx.setMaGiaoDich(payload.getTransactionId());
        tx.setTrangThai(STATUS_PAID);
        tx.setThoiGianThanhToan(Instant.now());
        tx.setNgayTao(Instant.now());
        tx.setDuLieuRaw(serializePayload(payload));
         giaoDichThanhToanRepository.save(tx);

         // Tự động xác nhận đơn sau khi thanh toán thành công (chuyển sang DA_XAC_NHAN)
         order.setTrangThai("DA_XAC_NHAN");
         order.setNgayCapNhat(Instant.now());
        donHangRepository.save(order);
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String code) {
        DonHang order = donHangRepository.findByMaDonHangIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay don hang: " + code));

        String status = STATUS_PENDING;
        Optional<GiaoDichThanhToan> txOpt = giaoDichThanhToanRepository
                .findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(order, PROVIDER);
        if (txOpt.isPresent() && STATUS_PAID.equalsIgnoreCase(txOpt.get().getTrangThai())) {
            status = STATUS_PAID;
        } else if ("DA_XAC_NHAN".equalsIgnoreCase(order.getTrangThai())) {
            status = STATUS_PAID;
        }

        return new OrderStatusResponse(order.getMaDonHang(), order.getTongTien(), status);
    }

    private Optional<DonHang> findOrderFromWebhook(SepayWebhookRequest payload) {
        // First: try to find by order code
        for (String candidate : extractOrderCodeCandidates(payload)) {
            Optional<DonHang> order = donHangRepository.findByMaDonHangIgnoreCase(candidate);
            if (order.isPresent()) {
                return order;
            }
        }

        // Fallback: if code not found, try to find by amount and time (for recently created orders)
        BigDecimal amount = payload.getAmount();
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            Instant recentTime = Instant.now().minus(java.time.Duration.ofHours(1));
            List<DonHang> candidates = donHangRepository.findByTongTienAndNgayDatGreaterThan(amount, recentTime);
            // Return the most recent pending/confirming order with matching amount
            return candidates.stream()
                    .filter(o -> "PENDING".equalsIgnoreCase(o.getTrangThai()) || "CHO_XAC_NHAN".equalsIgnoreCase(o.getTrangThai()))
                    .max((a, b) -> b.getNgayDat().compareTo(a.getNgayDat()));
        }

        return Optional.empty();
    }

    private Set<String> extractOrderCodeCandidates(SepayWebhookRequest payload) {
        Set<String> candidates = new LinkedHashSet<>();
        addOrderCodeCandidates(candidates, payload.getCode());
        addOrderCodeCandidates(candidates, payload.getContent());
        addOrderCodeCandidates(candidates, payload.getDescription());
        return candidates;
    }

    private void addOrderCodeCandidates(Set<String> candidates, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        addNormalizedOrderCode(candidates, text.trim());

        Matcher matcher = ORDER_CODE_PATTERN.matcher(text);
        while (matcher.find()) {
            addNormalizedOrderCode(candidates, matcher.group());
        }
    }

    private void addNormalizedOrderCode(Set<String> candidates, String rawCode) {
        if (rawCode == null) {
            return;
        }

        String code = rawCode.trim().toUpperCase();
        if (code.isEmpty()) {
            return;
        }

        candidates.add(code);
        if (code.startsWith("DH") && !code.startsWith("DH-") && code.length() > 2) {
            candidates.add("DH-" + code.substring(2));
        }
    }

    private String serializePayload(SepayWebhookRequest payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
