package com.pet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.result.Result;
import com.pet.dto.ProductQueryRequest;
import com.pet.dto.ProductRequest;
import com.pet.service.ProductService;
import com.pet.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品列表（商城用，只返回上架商品）
     */
    @GetMapping("/list")
    public Result<Page<ProductVO>> getProductList(ProductQueryRequest request) {
        return Result.success(productService.getProductList(request));
    }

    /**
     * 管理员获取商品列表（含下架）
     */
    @GetMapping("/admin/list")
    public Result<Page<ProductVO>> getAdminProductList(ProductQueryRequest request) {
        return Result.success(productService.getAdminProductList(request));
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }
    
    /**
     * 获取热销商品
     */
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotProducts(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }
    
    /**
     * 获取新品推荐
     */
    @GetMapping("/new")
    public Result<List<ProductVO>> getNewProducts(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.getNewProducts(limit));
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<Page<ProductVO>> search(@RequestParam String keyword,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        ProductQueryRequest request = new ProductQueryRequest();
        request.setKeyword(keyword);
        request.setPageNum(page);
        request.setPageSize(size);
        return Result.success(productService.getProductList(request));
    }

    /**
     * 按类目查询热销商品（Agent专用）
     */
    @GetMapping("/searchByCategory")
    public Result<ProductVO> searchByCategory(@RequestParam String category,
                                               @RequestParam(required = false) String subCategory) {
        ProductVO product = productService.searchByCategory(category, subCategory);
        if (product == null) {
            return Result.error(404, "该类目下暂无热销商品");
        }
        return Result.success(product);
    }

    // ==================== 管理员功能 ====================
    
    /**
     * 创建商品
     */
    @PostMapping("/admin")
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductRequest request) {
        return Result.success(productService.createProduct(request));
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/admin/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return Result.success(productService.updateProduct(id, request));
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
    
    /**
     * 上架/下架商品
     */
    @PutMapping("/admin/status/{id}")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateProductStatus(id, status);
        return Result.success();
    }
}
