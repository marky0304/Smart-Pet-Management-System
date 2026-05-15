package com.pet.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private Long id;
    
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    @NotNull(message = "服务ID不能为空")
    private Long serviceId;
    
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime appointmentDatetime;
    
    private String notes;
}
