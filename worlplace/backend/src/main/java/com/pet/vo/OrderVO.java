package com.pet.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private String statusName;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String shippingAddress;
    private String shippingPhone;
    private String shippingName;
    private String trackingNumber;
    private String notes;
    private String returnReason;
    private LocalDateTime returnTime;
    private BigDecimal refundAmount;
    private LocalDateTime refundTime;
    private String refundNotes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItemVO> items;
    
    @Data
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
        private Boolean reviewed;
    }
}
