package com.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.dto.ProductQueryRequest;
import com.pet.dto.ProductRequest;
import com.pet.vo.ProductVO;

import java.util.List;

public interface ProductService {
    
    /**
     * 创建商品
     */
    ProductVO createProduct(ProductRequest request);
    
    /**
     * 更新商品
     */
    ProductVO updateProduct(Long id, ProductRequest request);
    
    /**
     * 删除商品
     */
    void deleteProduct(Long id);
    
    /**
     * 获取商品详情
     */
    ProductVO getProductById(Long id);
    
    /**
     * 获取商品列表
     */
    Page<ProductVO> getProductList(ProductQueryRequest request);

    /**
     * 管理员获取所有商品（含下架）
     */
    Page<ProductVO> getAdminProductList(ProductQueryRequest request);
    
    /**
     * 上架/下架商品
     */
    void updateProductStatus(Long id, Integer status);
    
    /**
     * 获取热销商品
     */
    List<ProductVO> getHotProducts(Integer limit);
    
    /**
     * 获取新品推荐
     */
    List<ProductVO> getNewProducts(Integer limit);

    /**
     * 按类目查询热销商品（Agent专用）
     */
    ProductVO searchByCategory(String category, String subCategory);
}
