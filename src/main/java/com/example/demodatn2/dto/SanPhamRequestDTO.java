package com.example.demodatn2.dto;

import lombok.Data;

import java.util.List;

@Data
public class SanPhamRequestDTO {
    private Integer id;
    private String maSanPham;
    private String ten;
    private String moTaNgan;
    private String moTa;
    private String chatLieu;
    private String gioiTinh;
    private Integer danhMucId;
    private Integer thuongHieuId;

    private List<BienTheRequestDTO> bienThes;
    private List<HinhAnhSanPhamDTO> hinhAnhSanPhams;
    private List<HinhAnhMauSacDTO> hinhAnhMauSacs;
}
