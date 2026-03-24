package com.example.demodatn2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GhtkFeeResponse {
    private boolean success;
    private String message;
    private Fee fee;

    @Data
    public static class Fee {
        private String name;
        private Integer fee;

        @JsonProperty("insurance_fee")
        private Integer insuranceFee;

        @JsonProperty("delivery_type")
        private String deliveryType;

        private Integer a;
        private String dt;

        @JsonProperty("extFees")
        private List<ExtFee> extFees;

        private Boolean delivery;
    }

    @Data
    public static class ExtFee {
        private String display;
        private String title;
        private Integer amount;
        private String type;
    }
}
