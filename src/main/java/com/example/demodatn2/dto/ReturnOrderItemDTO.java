package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReturnOrderItemDTO {
    private Integer orderItemId;
    private String productName;
    private String color;
    private String size;
    private Integer orderedQuantity;
}
