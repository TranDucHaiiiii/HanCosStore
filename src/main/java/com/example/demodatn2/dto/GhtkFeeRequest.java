package com.example.demodatn2.dto;

import lombok.Data;

import java.util.List;

@Data
public class GhtkFeeRequest {
    private String pickAddressId;
    private String pickAddress;
    private String pickProvince;
    private String pickDistrict;
    private String pickWard;
    private String pickStreet;

    private String address;
    private String province;
    private String district;
    private String ward;
    private String street;

    private Integer weight;
    private Integer value;
    private String transport;
    private List<Integer> tags;
}
