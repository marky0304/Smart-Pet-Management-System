package com.pet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.dto.ServiceRequest;
import com.pet.vo.ServiceVO;

import java.util.List;

public interface ServiceService {
    
    ServiceVO createService(ServiceRequest request);
    
    ServiceVO updateService(ServiceRequest request);
    
    void deleteService(Long id);
    
    ServiceVO getServiceById(Long id);
    
    Page<ServiceVO> getServiceList(String category, Integer status, Integer pageNum, Integer pageSize);
    
    List<ServiceVO> getAllActiveServices();
}
