package com.pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.entity.HealthRecord;
import com.pet.mapper.HealthRecordMapper;
import com.pet.service.HealthRecordService;
import org.springframework.stereotype.Service;

/**
 * 健康记录服务实现类
 */
@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {
}