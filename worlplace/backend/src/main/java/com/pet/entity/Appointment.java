package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约实体
 * 注意：此表的status字段不是逻辑删除字段，而是预约状态字段
 * 因此需要在查询时明确指定不使用逻辑删除
 */
@Data
@TableName("appointments")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private Long serviceId;
    private String serviceType; // BATH-洗澡, GROOM-美容, BOARD-寄养, MEDICAL-医疗, TRAIN-训练, OTHER-其他
    private LocalDateTime appointmentDatetime; // 预约日期时间
    private LocalDate appointmentDate; // 预约日期
    private LocalTime appointmentTime; // 预约时间
    private String status; // PENDING-待确认，CONFIRMED-已确认，COMPLETED-已完成，CANCELLED-已取消
    private BigDecimal totalPrice;
    private String notes;
    private Integer rating; // 1-5
    private String review;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
