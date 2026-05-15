package com.pet.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentQueryRequest {
    private String status;
    private Long serviceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
