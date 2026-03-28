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
public class InventoryLogDTO {
    private String tenSanPham;
    private String maSKU;
    private String mauSac;
    private String kichCo;
    private String loai;
    private Integer soLuong;
    private String nguoiThucHien;
    private String ghiChu;
    private Instant ngayTao;
}
