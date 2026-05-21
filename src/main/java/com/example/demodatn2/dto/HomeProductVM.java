package com.example.demodatn2.dto;
import com.example.demodatn2.entity.DanhMuc;

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
public class HomeProductVM {
    private Integer id;
    private String ten;
    private String anhChinh;
    private BigDecimal giaMin;
    private BigDecimal giaMax;
    private String thuongHieu;
    private List<String> mauSacs;
    private List<String> kichCos;
    private String maDanhMuc;
    private Integer idDanhMuc;
    private DanhMuc danhMuc;
    private List<BienTheNhanhVM> bienThes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BienTheNhanhVM {
        private Integer id;
        private String mauSac;
        private String kichCo;
        private Integer soLuongTon;
        private String anhMauSac;
    }

}
