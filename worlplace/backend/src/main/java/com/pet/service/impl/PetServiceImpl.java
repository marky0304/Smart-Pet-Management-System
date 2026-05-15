package com.pet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.entity.Pet;
import com.pet.mapper.PetMapper;
import com.pet.service.PetService;
import org.springframework.stereotype.Service;

/**
 * 宠物服务实现类
 */
@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements PetService {
}