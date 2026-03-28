package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NhapKhoHistoryDTO {
    private Integer giaoDichId;
    private Integer bienTheId;
    private String maSKU;
    private String mauSac;
    private String kichCo;
    private Integer soLuongNhap;
    private String loaiGiaoDich;
    private String nguoiNhap;
    private String ghiChu;
    private Instant ngayTao;
}
