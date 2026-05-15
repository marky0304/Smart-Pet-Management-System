package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物实体类
 */
@Data
@TableName("pet")
public class Pet {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 宠物名称
     */
    private String name;
    
    /**
     * 宠物类型：DOG-狗，CAT-猫，BIRD-鸟，OTHER-其他
     */
    private String type;
    
    /**
     * 品种
     */
    private String breed;
    
    /**
     * 性别：0-未知，1-公，2-母
     */
    private Integer gender;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 毛色
     */
    private String color;
    
    /**
     * 体重（kg）
     */
    private Double weight;
    
    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    
    /**
     * 芯片编号
     */
    private String chipNumber;
    
    /**
     * 过敏史
     */
    private String allergy;
    
    /**
     * 特殊说明
     */
    private String specialNotes;
    
    /**
     * 头像图片
     */
    private String avatar;
    
    /**
     * 状态：1-正常，0-删除
     */
    @TableLogic(value = "1", delval = "0")
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}