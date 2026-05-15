package com.pet.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    
    @NotBlank(message = "商品名称不能为空")
    private String name;
    
    @NotBlank(message = "商品分类不能为空")
    private String category;
    
    private String description;
    
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格不能小于0")
    private BigDecimal price;
    
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;
    
    private String image;
    
    private String images;
    
    private Integer status = 1;
}
