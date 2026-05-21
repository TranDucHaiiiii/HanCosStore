package com.example.demodatn2.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
@Data
public class SanPhamResponseDTO {
    private Integer id;
    private String maSanPham;
    private String ten;
    private String moTaNgan;
    private String moTa;
    private String chatLieu;
    private String gioiTinh;
    private String trangThai;
    private Instant ngayTao;
    private Instant ngayCapNhat;

    private Integer danhMucId;
    private Integer parentDanhMucId;
    private String tenParentDanhMuc;
    private String tenDanhMuc;
    private Integer thuongHieuId;
    private String tenThuongHieu;
    private Integer khoiLuongGram;
    private Integer tongSoLuongTon;
    private List<BienTheResponseDTO> bienThes;
    private List<HinhAnhSanPhamDTO> hinhAnhSanPhams;
    private List<HinhAnhMauSacDTO> hinhAnhMauSacs;
}
