package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthRecordVO {
    private Long id;
    private Long petId;
    private String petName;
    private String recordType;
    private String recordTypeName;
    private LocalDateTime recordDate;
    private BigDecimal weight;
    private BigDecimal temperature;
    private String symptom;
    private String diagnosis;
    private String treatment;
    private String hospital;
    private String doctor;
    private String medicine;
    private BigDecimal cost;
    private LocalDate nextVisitDate;
    private String notes;
    private LocalDateTime createTime;
}
