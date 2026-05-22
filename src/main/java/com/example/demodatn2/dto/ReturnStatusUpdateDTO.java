package com.example.demodatn2.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReturnStatusUpdateDTO {
    private String status;
    private String note;
    private BigDecimal refundAmount;
    private String refundMethod;
    private String refundTransactionCode;
}
