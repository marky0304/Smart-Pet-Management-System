package com.pet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pet.entity.ProductReview;
import com.pet.vo.ProductReviewVO;

public interface ProductReviewService {

    ProductReviewVO create(ProductReview review, Long userId);

    IPage<ProductReviewVO> getByProduct(Long productId, Integer page, Integer size);

    IPage<ProductReviewVO> getByUser(Long userId, Integer page, Integer size);

    boolean delete(Long reviewId, Long userId);

    Double getAvgRating(Long productId);
}
