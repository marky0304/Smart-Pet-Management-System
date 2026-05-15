package com.pet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.common.exception.BusinessException;
import com.pet.dto.ServiceRequest;
import com.pet.entity.PetService;
import com.pet.mapper.ServiceMapper;
import com.pet.service.ServiceService;
import com.pet.vo.ServiceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    
    private final ServiceMapper serviceMapper;
    
    private static final Map<String, String> CATEGORY_NAMES = Map.of(
        "BATH", "洗澡",
        "GROOM", "美容",
        "BOARD", "寄养",
        "MEDICAL", "医疗",
        "TRAIN", "训练",
        "OTHER", "其他"
    );
    
    private static final Map<Integer, String> STATUS_NAMES = Map.of(
        0, "已下架",
        1, "已上架"
    );

    @Override
    public ServiceVO createService(ServiceRequest request) {
        PetService service = new PetService();
        BeanUtil.copyProperties(request, service);
        serviceMapper.insert(service);
        return convertToVO(service);
    }
    
    @Override
    public ServiceVO updateService(ServiceRequest request) {
        PetService service = serviceMapper.selectById(request.getId());
        if (service == null) {
            throw new BusinessException("服务不存在");
        }
        BeanUtil.copyProperties(request, service, "id", "createTime");
        serviceMapper.updateById(service);
        return convertToVO(service);
    }
    
    @Override
    public void deleteService(Long id) {
        PetService service = serviceMapper.selectById(id);
        if (service == null) {
            throw new BusinessException("服务不存在");
        }
        serviceMapper.deleteById(id);
    }
    
    @Override
    public ServiceVO getServiceById(Long id) {
        PetService service = serviceMapper.selectById(id);
        if (service == null) {
            throw new BusinessException("服务不存在");
        }
        return convertToVO(service);
    }
    
    @Override
    public Page<ServiceVO> getServiceList(String category, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PetService> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(PetService::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(PetService::getStatus, status);
        }
        
        wrapper.orderByDesc(PetService::getCreateTime);
        
        Page<PetService> page = serviceMapper.selectPage(
            new Page<>(pageNum, pageSize),
            wrapper
        );
        
        Page<ServiceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));
        
        return voPage;
    }
    
    @Override
    public List<ServiceVO> getAllActiveServices() {
        LambdaQueryWrapper<PetService> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PetService::getStatus, 1);
        wrapper.orderByAsc(PetService::getCategory);
        
        return serviceMapper.selectList(wrapper).stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }
    
    private ServiceVO convertToVO(PetService service) {
        ServiceVO vo = new ServiceVO();
        BeanUtil.copyProperties(service, vo);
        // imageUrl -> image 字段名不一致需手动映射
        vo.setImage(service.getImageUrl());
        vo.setCategoryName(CATEGORY_NAMES.getOrDefault(service.getCategory(), service.getCategory()));
        vo.setStatusName(STATUS_NAMES.getOrDefault(service.getStatus(), "未知"));
        return vo;
    }
}
