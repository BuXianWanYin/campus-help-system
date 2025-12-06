package com.server.campushelpserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.Address;
import com.server.campushelpserver.entity.dto.AddressDTO;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService extends IService<Address> {
    
    /**
     * 获取当前用户的收货地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> getAddressList(Long userId);
    
    /**
     * 添加收货地址
     * @param dto 地址信息
     * @param userId 用户ID
     * @return 地址ID
     */
    Long addAddress(AddressDTO dto, Long userId);
    
    /**
     * 更新收货地址
     * @param addressId 地址ID
     * @param dto 地址信息
     * @param userId 用户ID
     */
    void updateAddress(Long addressId, AddressDTO dto, Long userId);
    
    /**
     * 删除收货地址
     * @param addressId 地址ID
     * @param userId 用户ID
     */
    void deleteAddress(Long addressId, Long userId);
    
    /**
     * 设置默认地址
     * @param addressId 地址ID
     * @param userId 用户ID
     */
    void setDefaultAddress(Long addressId, Long userId);
}

