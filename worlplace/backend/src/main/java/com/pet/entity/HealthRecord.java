package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康记录实体类
 */
@Data
@TableName("health_records")
public class HealthRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宠物ID
     */
    private Long petId;
    
    /**
     * 记录类型：体检、疫苗、治疗等
     */
    private String recordType;
    
    /**
     * 记录日期 - 兼容前端传来的 datetime 字符串，只取日期部分
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate recordDate;
    
    /**
     * 体重（kg）
     */
    private BigDecimal weight;
    
    /**
     * 体温（℃）
     */
    private BigDecimal temperature;
    
    /**
     * 症状
     */
    private String symptoms;
    
    /**
     * 诊断结果
     */
    private String diagnosis;
    
    /**
     * 治疗方案
     */
    private String treatment;
    
    /**
     * 医生姓名
     */
    private String doctorName;
    
    /**
     * 医院名称
     */
    private String hospitalName;
    
    /**
     * 备注
     */
    private String notes;
    
    /**
     * 用药记录
     */
    private String medicine;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 下次复诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate nextVisitDate;

    /**
     * 状态：1-正常，0-删除
     */
    @TableLogic(value = "1", delval = "0")
    private Integer status;

    /**
     * 宠物名称（非数据库字段，查询后填充）
     */
    @TableField(exist = false)
    private String petName;
    
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