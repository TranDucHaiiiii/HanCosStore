package com.example.demodatn2.service;

import com.example.demodatn2.entity.ChiTietDonHang;
import com.example.demodatn2.entity.DonHang;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OrderConfirmationEmailService {
    private static final Locale VIETNAM = Locale.forLanguageTag("vi-VN");
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(VIETNAM_ZONE);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Async("mailNotificationExecutor")
    public void sendOrderConfirmation(DonHang order, List<ChiTietDonHang> items) {
        try {
            if (order == null || order.getEmailNhan() == null || order.getEmailNhan().isBlank()) {
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setTo(order.getEmailNhan().trim());
            message.setSubject("HancosStore - Xác nhận đơn hàng " + order.getMaDonHang());
            message.setText(buildMessage(order, items));
            mailSender.send(message);
        } catch (Exception ignored) {
            // Email failure must not make a successful order fail.
        }
    }

    private String buildMessage(DonHang order, List<ChiTietDonHang> items) {
        StringBuilder body = new StringBuilder();
        body.append("Kính chào quý khách,\n\n");
        body.append("Cảm ơn quý khách đã đặt hàng tại HancosStore.\n\n");
        body.append("Đơn hàng của quý khách đã được hệ thống ghi nhận thành công với thông tin như sau:\n\n");
        body.append("Mã đơn hàng: ").append(order.getMaDonHang()).append('\n');
        if (order.getNgayDat() != null) {
            body.append("Ngày đặt hàng: ").append(DATE_FORMATTER.format(order.getNgayDat())).append('\n');
        }
        body.append("Phương thức thanh toán: ").append(paymentLabel(order.getPhuongThucThanhToan())).append('\n');
        body.append("Trạng thái đơn hàng: ").append(statusLabel(order.getTrangThai())).append("\n\n");

        body.append("Thông tin sản phẩm:\n\n");
        if (items != null && !items.isEmpty()) {
            for (ChiTietDonHang item : items) {
                body.append("* ")
                        .append(valueOrDefault(item.getTenSanPham(), "Sản phẩm"))
                        .append(" - Số lượng: ").append(item.getSoLuong() != null ? item.getSoLuong() : 0)
                        .append(" - Giá: ").append(formatMoney(item.getThanhTien()))
                        .append('\n');
            }
        }

        body.append('\n');
        body.append("Tổng tiền thanh toán: ").append(formatMoney(order.getTongTien())).append("\n\n");

        body.append("Thông tin nhận hàng:\n");
        body.append("Họ tên: ").append(valueOrDefault(order.getHoTenNhan(), "")).append('\n');
        body.append("Số điện thoại: ").append(valueOrDefault(order.getSoDienThoaiNhan(), "")).append('\n');
        body.append("Địa chỉ: ").append(valueOrDefault(order.getDiaChiNhan(), "")).append("\n\n");
        body.append("HancosStore sẽ sớm xác nhận và xử lý đơn hàng của quý khách. ");
        body.append("Quý khách có thể theo dõi trạng thái đơn hàng trong mục \"Lịch sử đơn hàng\" trên website.\n\n");
        body.append("Trân trọng,\n");
        body.append("HancosStore");
        return body.toString();
    }

    private String formatMoney(BigDecimal amount) {
        BigDecimal safeAmount = amount != null ? amount : BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(VIETNAM).format(safeAmount);
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    private String statusLabel(String status) {
        if (status == null) {
            return "Chờ xác nhận";
        }
        return switch (status.trim().toUpperCase()) {
            case "PENDING" -> "Chờ thanh toán";
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DA_XAC_NHAN", "CONFIRMED", "PAID" -> "Đã xác nhận";
            case "DANG_GIAO", "SHIPPING" -> "Đang giao hàng";
            case "HOAN_THANH", "DELIVERED", "COMPLETED" -> "Hoàn thành";
            case "DA_HUY", "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private String paymentLabel(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "COD";
        }
        String normalized = paymentMethod.trim().toUpperCase();
        return switch (normalized) {
            case "COD" -> "COD";
            case "SEPAY", "TRANSFER", "CHUYEN_KHOAN", "CHUYENKHOAN" -> "Chuyển khoản";
            case "CASH" -> "Tiền mặt";
            default -> paymentMethod;
        };
    }
}
