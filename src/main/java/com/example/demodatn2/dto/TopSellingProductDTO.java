package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingProductDTO {
    private Integer sanPhamId;
    private String tenSanPham;
    private Long tongSoLuongBan;
    private BigDecimal tongDoanhThu;
    private Long soDonHang;
    private String hinhAnh;
}
