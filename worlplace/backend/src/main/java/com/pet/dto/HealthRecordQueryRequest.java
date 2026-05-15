package com.pet.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthRecordQueryRequest {
    private Long petId;
    private String recordType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
