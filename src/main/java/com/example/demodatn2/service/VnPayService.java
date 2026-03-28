package com.example.demodatn2.service;

import com.example.demodatn2.config.VnPayProperties;
import com.example.demodatn2.dto.PaymentReturnResultDTO;
import com.example.demodatn2.entity.DonHang;
import com.example.demodatn2.entity.GiaoDichThanhToan;
import com.example.demodatn2.repository.DonHangRepository;
import com.example.demodatn2.repository.GiaoDichThanhToanRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final VnPayProperties properties;
    private final DonHangRepository donHangRepository;
    private final GiaoDichThanhToanRepository giaoDichThanhToanRepository;

    public boolean isEnabled() {
        return properties.isEnabled()
                && notBlank(properties.getTmnCode())
                && notBlank(properties.getHashSecret())
                && notBlank(properties.getPayUrl())
                && notBlank(properties.getReturnUrl());
    }

    public String createPaymentUrl(DonHang donHang, HttpServletRequest request) {
        if (!isEnabled()) {
            throw new IllegalStateException("Cổng thanh toán VNPay chưa được cấu hình.");
        }

        String txnRef = donHang.getMaDonHang();
        long amount = donHang.getTongTien().multiply(BigDecimal.valueOf(100)).longValue();
        Instant now = Instant.now();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don " + donHang.getMaDonHang());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", resolveClientIp(request));
        params.put("vnp_CreateDate", VNPAY_TIME.format(now));
        params.put("vnp_ExpireDate", VNPAY_TIME.format(now.plusSeconds(15 * 60)));

        String hashData = buildQuery(params, false);
        String query = buildQuery(params, true);
        String secureHash = hmacSha512(properties.getHashSecret(), hashData);

        return UriComponentsBuilder.fromHttpUrl(properties.getPayUrl())
                .query(query)
                .queryParam("vnp_SecureHash", secureHash)
                .build(true)
                .toUriString();
    }

    @Transactional
    public PaymentReturnResultDTO processReturn(Map<String, String> inputParams) {
        String secureHash = inputParams.get("vnp_SecureHash");
        if (!notBlank(secureHash)) {
            return PaymentReturnResultDTO.builder()
                    .success(false)
                    .message("Thiếu chữ ký xác thực từ VNPay")
                    .build();
        }

        Map<String, String> filtered = inputParams.entrySet().stream()
                .filter(e -> notBlank(e.getKey()) && notBlank(e.getValue()))
                .filter(e -> !"vnp_SecureHash".equals(e.getKey()) && !"vnp_SecureHashType".equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, TreeMap::new));

        String generatedHash = hmacSha512(properties.getHashSecret(), buildQuery(filtered, false));
        if (!generatedHash.equalsIgnoreCase(secureHash)) {
            return PaymentReturnResultDTO.builder()
                    .success(false)
                    .message("Chữ ký không hợp lệ")
                    .build();
        }

        String txnRef = filtered.get("vnp_TxnRef");
        if (!notBlank(txnRef)) {
            return PaymentReturnResultDTO.builder()
                    .success(false)
                    .message("Thiếu mã đơn hàng từ VNPay")
                    .build();
        }

        DonHang donHang = donHangRepository.findByMaDonHangIgnoreCase(txnRef)
                .orElse(null);
        if (donHang == null) {
            return PaymentReturnResultDTO.builder()
                    .success(false)
                    .message("Không tìm thấy đơn hàng: " + txnRef)
                    .build();
        }

        String responseCode = filtered.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = filtered.getOrDefault("vnp_TransactionStatus", "");
        boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);

        GiaoDichThanhToan giaoDich = giaoDichThanhToanRepository
                .findFirstByDonHangAndNhaCungCapOrderByNgayTaoDesc(donHang, "VNPAY")
                .orElseGet(GiaoDichThanhToan::new);
        giaoDich.setDonHang(donHang);
        giaoDich.setSoTien(donHang.getTongTien());
        giaoDich.setNhaCungCap("VNPAY");
        giaoDich.setMaGiaoDich(filtered.get("vnp_TransactionNo"));
        giaoDich.setTrangThai(success ? "SUCCESS" : "FAILED");
        giaoDich.setThoiGianThanhToan(Instant.now());
        giaoDich.setNgayTao(giaoDich.getNgayTao() == null ? Instant.now() : giaoDich.getNgayTao());
        giaoDich.setDuLieuRaw(filtered.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("\n")));
        giaoDichThanhToanRepository.save(giaoDich);

        if (success) {
            if (!"CHO_XAC_NHAN".equalsIgnoreCase(donHang.getTrangThai())) {
                donHang.setTrangThai("CHO_XAC_NHAN");
                donHang.setNgayCapNhat(Instant.now());
                donHangRepository.save(donHang);
            }
            return PaymentReturnResultDTO.builder()
                    .success(true)
                    .message("Thanh toán VNPay thành công")
                    .orderId(donHang.getId())
                    .orderCode(donHang.getMaDonHang())
                    .build();
        }

        return PaymentReturnResultDTO.builder()
                .success(false)
                .message("Thanh toán VNPay thất bại hoặc bị hủy")
                .orderId(donHang.getId())
                .orderCode(donHang.getMaDonHang())
                .build();
    }

    private static String buildQuery(Map<String, String> params, boolean encodeValue) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + (encodeValue ? urlEncode(e.getValue()) : e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo chữ ký VNPay", e);
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (notBlank(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (notBlank(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
