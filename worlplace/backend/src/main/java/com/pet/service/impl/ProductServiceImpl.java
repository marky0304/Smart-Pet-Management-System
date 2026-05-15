package com.pet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.exception.BusinessException;
import com.pet.dto.ProductQueryRequest;
import com.pet.dto.ProductRequest;
import com.pet.entity.Product;
import com.pet.mapper.ProductMapper;
import com.pet.service.ProductService;
import com.pet.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    
    private static final Map<String, String> CATEGORY_NAMES = new HashMap<String, String>() {{
        put("FOOD", "食品");
        put("TOY", "玩具");
        put("SUPPLY", "用品");
        put("MEDICINE", "药品");
        put("CLEAN", "清洁");
        put("OTHER", "其他");
    }};
    
    private static final Map<Integer, String> STATUS_NAMES = new HashMap<Integer, String>() {{
        put(0, "已下架");
        put(1, "已上架");
    }};

    @Override
    public ProductVO createProduct(ProductRequest request) {
        Product product = new Product();
        BeanUtil.copyProperties(request, product);
        product.setSales(0);
        
        productMapper.insert(product);
        
        return convertToVO(product);
    }

    @Override
    public ProductVO updateProduct(Long id, ProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        BeanUtil.copyProperties(request, product);
        productMapper.updateById(product);
        
        return convertToVO(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        productMapper.deleteById(id);
    }

    @Override
    public ProductVO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        return convertToVO(product);
    }

    @Override
    public Page<ProductVO> getProductList(ProductQueryRequest request) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 商城接口只显示上架商品
        if (request.getStatus() != null) {
            wrapper.eq(Product::getStatus, request.getStatus());
        } else {
            wrapper.eq(Product::getStatus, 1);
        }

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                .like(Product::getName, request.getKeyword())
                .or()
                .like(Product::getDescription, request.getKeyword())
            );
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(Product::getName, request.getName());
        }
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Product::getCategory, request.getCategory());
        }

        // 排序 - 商城列表
        if ("sales".equals(request.getSortBy())) {
            wrapper.orderBy(true, "ASC".equals(request.getSortOrder()), Product::getSales);
        } else if ("price".equals(request.getSortBy())) {
            wrapper.orderBy(true, "ASC".equals(request.getSortOrder()), Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

        Page<Product> page = productMapper.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()),
            wrapper
        );

        Page<ProductVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        int rows = productMapper.updateStatusById(id, status);
        if (rows == 0) {
            throw new BusinessException("商品不存在");
        }
    }

    @Override
    public Page<ProductVO> getAdminProductList(ProductQueryRequest request) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                .like(Product::getName, request.getKeyword())
                .or()
                .like(Product::getDescription, request.getKeyword())
            );
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(Product::getName, request.getName());
        }
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Product::getCategory, request.getCategory());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Product::getStatus, request.getStatus());
        }
        // 管理员接口不加 status=1 过滤，显示所有商品（含下架）
        wrapper.orderByDesc(Product::getCreateTime);

        Page<Product> page = productMapper.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()),
            wrapper
        );

        Page<ProductVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        List<Product> products = productMapper.getHotProducts(limit);
        return products.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> getNewProducts(Integer limit) {
        List<Product> products = productMapper.getNewProducts(limit);
        return products.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public ProductVO searchByCategory(String category, String subCategory) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .gt(Product::getStock, 0)
               .eq(Product::getIsHot, 1)
               .eq(Product::getCategory, category);
        if (StringUtils.hasText(subCategory)) {
            // 数据库 sub_category 编码异常，改用商品名模糊匹配
            wrapper.like(Product::getName, subCategory);
        }
        wrapper.orderByDesc(Product::getSales)
               .last("LIMIT 1");

        Product product = productMapper.selectOne(wrapper);
        if (product == null) {
            return null;
        }
        return convertToVO(product);
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setCategoryName(CATEGORY_NAMES.getOrDefault(product.getCategory(), product.getCategory()));
        vo.setStatusName(STATUS_NAMES.getOrDefault(product.getStatus(), "未知"));
        
        // 如果图片URL为空，设置默认图片
        if (vo.getImage() == null || vo.getImage().trim().isEmpty()) {
            String defaultImage = getSpecificImageById(product.getId(), product.getCategory());
            vo.setImage(defaultImage);
        }
        
        // 如果商品名称为空或乱码，设置默认名称
        if (vo.getName() == null || vo.getName().trim().isEmpty() || vo.getName().contains("?")) {
            vo.setName(getDefaultNameById(product.getId(), product.getCategory()));
        }
        
        if (vo.getOriginalPrice() == null && vo.getPrice() != null) {
            vo.setOriginalPrice(vo.getPrice().multiply(new java.math.BigDecimal("1.2")));
        }
        
        return vo;
    }
    
    private String getDefaultImageByCategory(String category) {
        switch (category) {
            case "FOOD":     return "/shop image/food-default.svg";
            case "TOY":      return "/shop image/toy-default.svg";
            case "SUPPLY":   return "/shop image/supply-default.svg";
            case "CLEAN":    return "/shop image/supply-default.svg";
            case "MEDICINE": return "/shop image/medicine-default.svg";
            default:         return "/shop image/default-product.svg";
        }
    }

    private String getSpecificImageById(Long id, String category) {
        // 优先使用数据库中的 image_url，如果为空则按分类返回默认图
        return getDefaultImageByCategory(category);
    }
    
    private String getDefaultNameById(Long id, String category) {
        // 根据ID和分类生成默认商品名称
        String[] foodNames = {"皇家狗粮成犬粮10kg", "猫粮幼猫粮5kg", "宠物零食鸡肉干500g", "营养罐头牛肉味375g", "冻干生骨肉200g"};
        String[] toyNames = {"互动逗猫棒", "智能电动球", "咬胶玩具套装", "飞盘训练玩具", "猫爬架豪华版"};
        String[] supplyNames = {"自动饮水机2L", "宠物窝冬季保暖", "外出便携包", "自动喂食器", "宠物牵引绳"};
        String[] cleanNames = {"宠物香波500ml", "除臭喷雾250ml", "宠物湿巾100抽", "猫砂膨润土10L", "宠物梳子套装"};
        String[] medicineNames = {"驱虫药片装", "营养膏120g", "益生菌粉30袋", "眼药水15ml", "皮肤喷雾100ml"};
        
        int index = (int) ((id - 1) % 5);
        
        switch (category) {
            case "FOOD":
                return foodNames[index];
            case "TOY":
                return toyNames[index];
            case "SUPPLY":
                return supplyNames[index];
            case "CLEAN":
                return cleanNames[index];
            case "MEDICINE":
                return medicineNames[index];
            default:
                return "宠物用品 #" + id;
        }
    }
    
}
