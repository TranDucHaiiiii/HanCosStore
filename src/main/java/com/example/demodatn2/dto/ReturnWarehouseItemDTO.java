package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnWarehouseItemDTO {
    private Long id;
    private Integer sanPhamId;
    private Integer bienTheId;
    private String tenSanPham;
    private String hinhAnh;
    private String maSKU;
    private String mauSac;
    private String kichCo;
    private Integer soLuong;
    private String tinhTrang;
    private String huongXuLy;
    private String trangThai;
    private String maDonHoan;
    private Instant ngayNhap;
    private String ghiChu;
}
