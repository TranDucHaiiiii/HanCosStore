package com.example.demodatn2.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PosOrderRequestDTO {

    private Integer customerId;        // null = khách lẻ
    private String customerMode;       // guest, existing, new
    private String customerName;       // Bắt buộc
    private String customerPhone;      // Bắt buộc
    private String paymentMethod;      // cash, card, qr, transfer
    private String orderCode;          // Mã đơn hàng đã reserve trước (optional)
    private String voucherCode;        // Mã voucher (optional)
    private String note;               // Ghi chú (optional)
    private BigDecimal cashGiven;      // Số tiền khách đưa khi thanh toán tiền mặt
    private Boolean transferConfirmed; // Nhân viên xác nhận đã nhận tiền chuyển khoản
    private List<PosItemDTO> items;    // Danh sách sản phẩm

    @Data
    public static class PosItemDTO {
        private Integer variantId;     // BienTheSanPham.id
        private Integer qty;
        private BigDecimal price;      // Đơn giá tại thời điểm bán
    }
}
