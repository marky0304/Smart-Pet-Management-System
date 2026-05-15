package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 宠物统计VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetStatisticsVO {
    
    private Long totalCount;
    
    private Map<String, Long> typeDistribution;
    
    private Map<String, Long> breedDistribution;
    
    private Map<String, Long> ageDistribution;
    
    private Map<String, Long> genderDistribution;
}
