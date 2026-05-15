package com.pet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.common.exception.BusinessException;
import com.pet.entity.Address;
import com.pet.mapper.AddressMapper;
import com.pet.service.AddressService;
import com.pet.vo.AddressVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<AddressVO> getAddressList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .orderByDesc(Address::getIsDefault)
               .orderByDesc(Address::getUpdateTime);
        return list(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AddressVO getAddressById(Long id, Long userId) {
        Address address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404, "地址不存在");
        }
        return toVO(address);
    }

    @Override
    public AddressVO createAddress(Address address, Long userId) {
        address.setUserId(userId);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        if (address.getIsDefault() == null) address.setIsDefault(0);

        if (address.getIsDefault() == 1) {
            clearUserDefault(userId);
        } else {
            long count = count(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId));
            if (count == 0) address.setIsDefault(1);
        }

        save(address);
        return toVO(address);
    }

    @Override
    public AddressVO updateAddress(Long id, Address address, Long userId) {
        Address existing = getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(404, "地址不存在");
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearUserDefault(userId);
        }

        address.setId(id);
        address.setUserId(userId);
        address.setUpdateTime(LocalDateTime.now());
        updateById(address);

        Address updated = getById(id);
        return toVO(updated);
    }

    @Override
    public void deleteAddress(Long id, Long userId) {
        Address address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404, "地址不存在");
        }
        boolean wasDefault = address.getIsDefault() == 1;
        removeById(id);

        if (wasDefault) {
            List<Address> remaining = list(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .orderByDesc(Address::getUpdateTime));
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(1);
                newDefault.setUpdateTime(LocalDateTime.now());
                updateById(newDefault);
            }
        }
    }

    @Override
    public void setDefault(Long id, Long userId) {
        Address address = getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404, "地址不存在");
        }
        clearUserDefault(userId);
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        updateById(address);
    }

    private void clearUserDefault(Long userId) {
        update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0)
                .set(Address::getUpdateTime, LocalDateTime.now()));
    }

    private AddressVO toVO(Address address) {
        AddressVO vo = new AddressVO();
        BeanUtils.copyProperties(address, vo);
        return vo;
    }
}
