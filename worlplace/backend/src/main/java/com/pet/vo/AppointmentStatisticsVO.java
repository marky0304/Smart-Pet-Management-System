package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class AppointmentStatisticsVO {
    private Long totalAppointments;
    private Long pendingCount;
    private Long confirmedCount;
    private Long completedCount;
    private Long cancelledCount;
    private BigDecimal totalRevenue;
    private Map<String, Long> categoryDistribution;
}
