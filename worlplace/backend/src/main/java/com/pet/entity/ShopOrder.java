package com.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class ShopOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private LocalDateTime shipTime;
    private LocalDateTime completeTime;

    @TableField("receiver_name")
    private String shippingName;

    @TableField("receiver_phone")
    private String shippingPhone;

    @TableField("receiver_address")
    private String shippingAddress;

    @TableField("remark")
    private String notes;

    private String trackingNumber;

    private String returnReason;
    private LocalDateTime returnTime;
    private BigDecimal refundAmount;
    private LocalDateTime refundTime;
    private String refundNotes;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
