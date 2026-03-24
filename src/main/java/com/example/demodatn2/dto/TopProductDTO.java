package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Integer productId;
    private String tenSanPham;
    private Long soLuongDaBan;
}
