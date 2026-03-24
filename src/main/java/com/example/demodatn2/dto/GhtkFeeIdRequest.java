package com.example.demodatn2.dto;

import lombok.Data;

@Data
public class GhtkFeeIdRequest {
    private String provinceId;
    private String districtId;
    private String wardId;
    private String address;
    private Integer weight;
    private Integer value;
    private String transport;
}
