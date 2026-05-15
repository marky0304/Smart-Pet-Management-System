package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.exception.BusinessException;
import com.pet.entity.Product;
import com.pet.entity.ProductReview;
import com.pet.entity.User;
import com.pet.mapper.ProductMapper;
import com.pet.mapper.ProductReviewMapper;
import com.pet.service.ProductReviewService;
import com.pet.service.UserService;
import com.pet.vo.ProductReviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewMapper reviewMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public ProductReviewVO create(ProductReview review, Long userId) {
        if (!reviewMapper.hasUserPurchased(userId, review.getProductId())) {
            throw new BusinessException("您购买过该商品且订单完成后才能评价");
        }

        User user = userService.getById(userId);
        review.setUserId(userId);
        review.setUsername(user != null ? user.getUsername() : "匿名用户");
        review.setUserAvatar(user != null ? user.getAvatar() : null);
        review.setLikesCount(0);
        review.setStatus(1);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());

        reviewMapper.insert(review);

        updateProductRating(review.getProductId());

        return toVO(review);
    }

    @Override
    public IPage<ProductReviewVO> getByProduct(Long productId, Integer page, Integer size) {
        Page<ProductReview> pageParam = new Page<>(page, size);
        QueryWrapper<ProductReview> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId)
               .eq("status", 1)
               .orderByDesc("create_time");
        IPage<ProductReview> result = reviewMapper.selectPage(pageParam, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    public IPage<ProductReviewVO> getByUser(Long userId, Integer page, Integer size) {
        Page<ProductReview> pageParam = new Page<>(page, size);
        QueryWrapper<ProductReview> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("status", 1)
               .orderByDesc("create_time");
        IPage<ProductReview> result = reviewMapper.selectPage(pageParam, wrapper);
        return result.convert(this::toVO);
    }

    @Override
    @Transactional
    public boolean delete(Long reviewId, Long userId) {
        ProductReview review = reviewMapper.selectById(reviewId);
        if (review != null && review.getUserId().equals(userId)) {
            review.setStatus(0);
            review.setUpdateTime(LocalDateTime.now());
            reviewMapper.updateById(review);
            updateProductRating(review.getProductId());
            return true;
        }
        return false;
    }

    @Override
    public Double getAvgRating(Long productId) {
        return reviewMapper.getAvgRating(productId);
    }

    private void updateProductRating(Long productId) {
        Double avgRating = reviewMapper.getAvgRating(productId);
        int reviewCount = reviewMapper.selectByProductId(productId).size();

        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setAvgRating(avgRating != null ? avgRating : 0.0);
            product.setReviewCount(reviewCount);
            productMapper.updateById(product);
        }
    }

    private ProductReviewVO toVO(ProductReview review) {
        if (review == null) return null;
        ProductReviewVO vo = new ProductReviewVO();
        BeanUtils.copyProperties(review, vo, "isAnonymous");
        vo.setIsAnonymous(review.getIsAnonymous() != null && review.getIsAnonymous() == 1);
        return vo;
    }
}
