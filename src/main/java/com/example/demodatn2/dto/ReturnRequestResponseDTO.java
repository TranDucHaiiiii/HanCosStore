package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ReturnRequestResponseDTO {
    private Integer id;
    private Integer orderId;
    private String orderCode;
    private String customerName;
    private String reason;
    private String status;
    private String refundMethod;
    private Instant createdAt;
    private List<String> imageUrls;
}
