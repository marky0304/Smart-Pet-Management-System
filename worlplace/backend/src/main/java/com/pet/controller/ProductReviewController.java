package com.pet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.common.result.Result;
import com.pet.entity.ProductReview;
import com.pet.service.ProductReviewService;
import com.pet.vo.ProductReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/product-review")
@CrossOrigin(origins = "*")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @PostMapping("/")
    public Result<ProductReviewVO> create(@RequestBody ProductReview review, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (review.getProductId() == null) {
            return Result.error(400, "商品ID不能为空");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return Result.error(400, "评分必须在1-5之间");
        }
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            return Result.error(400, "评价内容不能为空");
        }

        ProductReviewVO vo = reviewService.create(review, userId);
        return Result.success(vo);
    }

    @GetMapping("/product/{productId}")
    public Result<IPage<ProductReviewVO>> getByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductReviewVO> result = reviewService.getByProduct(productId, page, size);
        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<IPage<ProductReviewVO>> getByUser(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IPage<ProductReviewVO> result = reviewService.getByUser(userId, page, size);
        return Result.success(result);
    }

    @DeleteMapping("/{reviewId}")
    public Result<String> delete(@PathVariable Long reviewId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean success = reviewService.delete(reviewId, userId);
        return success ? Result.success("删除成功", null)
                       : Result.error(403, "无权限删除或评价不存在");
    }
}
