package com.pet.vo;

import lombok.Data;

import java.util.Map;

@Data
public class HealthStatisticsVO {
    private Long totalRecords;
    private Map<String, Long> recordTypeDistribution;
    private Long abnormalCount;
    private Long upcomingVisits;
}
