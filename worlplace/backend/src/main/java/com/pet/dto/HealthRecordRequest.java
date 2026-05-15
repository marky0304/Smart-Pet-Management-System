package com.pet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthRecordRequest {
    private Long id;
    private Long petId;
    private String recordType;
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
}
