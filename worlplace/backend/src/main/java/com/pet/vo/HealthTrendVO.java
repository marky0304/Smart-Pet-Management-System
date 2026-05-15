package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HealthTrendVO {
    private List<TrendPoint> weightTrend;
    private List<TrendPoint> temperatureTrend;
    
    @Data
    public static class TrendPoint {
        private String date;
        private BigDecimal value;
    }
}
