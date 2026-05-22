package com.example.demodatn2.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ProductReviewVM {
    private Integer id;
    private Integer soSao;
    private String noiDung;
    private Instant ngayTao;
    private String tenKhachHang;
}
