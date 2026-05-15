package com.pet.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物信息VO
 */
@Data
public class PetVO {
    
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private String name;
    
    private String type;
    
    private String breed;
    
    private Integer gender;
    
    private LocalDate birthDate;
    
    private Integer age;
    
    private String color;
    
    private Double weight;
    
    private String photo;
    
    private String chipNumber;
    
    private String allergy;
    
    private String specialNotes;
    
    private Integer status;
    
    private LocalDateTime createTime;
}
