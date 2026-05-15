package com.pet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pet.entity.Address;
import com.pet.vo.AddressVO;
import java.util.List;

public interface AddressService extends IService<Address> {
    List<AddressVO> getAddressList(Long userId);
    AddressVO getAddressById(Long id, Long userId);
    AddressVO createAddress(Address address, Long userId);
    AddressVO updateAddress(Long id, Address address, Long userId);
    void deleteAddress(Long id, Long userId);
    void setDefault(Long id, Long userId);
}
