package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("services")
public class PetService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category; // BATH-洗澡，GROOM-美容，BOARD-寄养，MEDICAL-医疗，TRAIN-训练，OTHER-其他
    private String description;
    private BigDecimal price;
    private Integer duration; // 时长（分钟）
    private String petType; // 适用宠物类型：ALL, DOG, CAT, BIRD, OTHER
    private String imageUrl; // 服务图片URL
    private Integer status; // 0-下架，1-上架
    private Integer sortOrder; // 排序
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
