package com.pet.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderRequest {
    
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemRequest> items;

    private Long addressId;

    private String shippingAddress;

    private String shippingPhone;

    private String shippingName;
    
    private String paymentMethod = "ONLINE";
    
    private String notes;
    
    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
