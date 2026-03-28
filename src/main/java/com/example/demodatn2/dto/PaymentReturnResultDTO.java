package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentReturnResultDTO {
    private boolean success;
    private String message;
    private Integer orderId;
    private String orderCode;
}
