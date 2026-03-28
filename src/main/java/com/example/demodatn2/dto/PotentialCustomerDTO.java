package com.example.demodatn2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotentialCustomerDTO {
    private Integer taiKhoanId;
    private String hoTen;
    private String soDienThoai;
    private Long soDonThanhCong;
    private BigDecimal tongChiTieu;
    private Instant lanMuaGanNhat;
}
