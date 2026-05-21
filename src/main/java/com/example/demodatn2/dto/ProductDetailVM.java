package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ProductDetailVM {
    private Integer id;
    private String maSanPham;
    private String ten;
    private String moTaNgan;
    private String moTa;
    private String chatLieu;
    private String thuongHieu;
    private String gioiTinh;
    // Flat DanhMuc fields (avoid passing lazy entity outside transaction)
    private Integer danhMucId;
    private String danhMucTen;
    private Integer danhMucChaId;
    private String danhMucChaTen;

    private List<String> hinhAnhGallery;
    private Map<String, String> hinhAnhTheoMau; // Key: Tên màu, Value: Đường dẫn ảnh
    private List<BienTheVM> bienThes;

    @Getter
    @Setter
    @Builder
    public static class BienTheVM {
        private Integer id;
        private String maSKU;
        private String mauSac;
        private String kichCo;
        private BigDecimal gia;
        private BigDecimal giaGoc;
        private Integer soLuongTon;
    }
}
