package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LowStockVariantAlertDTO {
    private Integer sanPhamId;
    private Integer bienTheId;
    private String tenSanPham;
    private String mauSac;
    private String kichCo;
    private Integer soLuongTon;
    private String hinhAnh;
}
