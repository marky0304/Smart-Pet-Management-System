package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private Long petId;
    private String petName;
    private String petType;
    private Long serviceId;
    private String serviceName;
    private String serviceCategory;
    private String serviceType;
    private String serviceTypeName;
    private LocalDateTime appointmentDatetime;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String statusName;
    private BigDecimal totalPrice;
    private String notes;
    private Integer rating;
    private String review;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
