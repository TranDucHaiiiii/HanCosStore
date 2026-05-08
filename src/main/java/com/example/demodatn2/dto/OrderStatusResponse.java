package com.example.demodatn2.dto;

import java.math.BigDecimal;

// Response cho API kiểm tra trạng thái đơn hàng.
public class OrderStatusResponse {
    private String code;
    private BigDecimal amount;
    private String status;

    public OrderStatusResponse(String code, BigDecimal amount, String status) {
        this.code = code;
        this.amount = amount;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}

