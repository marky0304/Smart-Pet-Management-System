package com.pet.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 宠物信息请求
 */
@Data
public class PetRequest {
    
    @NotBlank(message = "宠物名称不能为空")
    private String name;
    
    @NotBlank(message = "宠物类型不能为空")
    private String type;
    
    private String breed;
    
    private Integer gender;
    
    private LocalDate birthDate;
    
    private String color;
    
    private Double weight;
    
    private String photo;
    
    private String chipNumber;
    
    private String allergy;
    
    private String specialNotes;
}
