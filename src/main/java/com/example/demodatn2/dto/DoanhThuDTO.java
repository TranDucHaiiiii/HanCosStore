package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoanhThuDTO {
    private BigDecimal tongDoanhThu;
    private Long soDonHang;
    private Long soSanPhamDaBan;
    private BigDecimal doanhThuHomNay;
    private Long soDonHomNay;
    private Long soKhachHang;
    private Long soDonChuaXuLy;
    private Long soSanPhamSapHetHang;
    private Long soDonBiHuyHomNay;
    private BigDecimal tyLeHuyHomNay;
    private List<LowStockVariantAlertDTO> bienTheSapHetHang;
    private List<TopSellingProductDTO> topSanPhamBanChay;
    private List<PotentialCustomerDTO> khachHangTiemNang;
}
