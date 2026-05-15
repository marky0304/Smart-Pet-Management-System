package com.pet.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CreateOrderByAgentRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;

    private Long addressId;
}
