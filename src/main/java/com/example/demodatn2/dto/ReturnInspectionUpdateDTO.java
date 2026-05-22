package com.example.demodatn2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnInspectionUpdateDTO {
    private Integer returnItemId;
    private String inspectionStatus;
    private String note;
}
