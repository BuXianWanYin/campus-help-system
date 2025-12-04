package com.server.campushelpserver.service.impl.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.address.Address;
import com.server.campushelpserver.entity.address.dto.AddressDTO;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.address.AddressMapper;
import com.server.campushelpserver.service.address.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
    
    @Autowired
    private AddressMapper addressMapper;
    
    @Override
    public List<Address> getAddressList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        wrapper.orderByDesc(Address::getCreateTime);
        return addressMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAddress(AddressDTO dto, Long userId) {
        // 检查地址数量限制（最多10个）
        long count = addressMapper.selectCount(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
        );
        if (count >= 10) {
            throw new BusinessException("最多只能保存10个收货地址");
        }
        
        // 如果设置为默认地址，取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.update(null, 
                new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDefault, 1)
                    .set(Address::getIsDefault, 0)
            );
        }
        
        // 创建地址
        Address address = new Address();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);
        // 拼接完整地址
        address.setFullAddress(dto.getProvince() + dto.getCity() + dto.getDistrict() + dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : 0);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());
        
        addressMapper.insert(address);
        return address.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Long addressId, AddressDTO dto, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        
        // 如果设置为默认地址，取消其他默认地址
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressMapper.update(null, 
                new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDefault, 1)
                    .set(Address::getIsDefault, 0)
            );
        }
        
        // 更新地址信息
        BeanUtils.copyProperties(dto, address, "id", "userId", "createTime");
        address.setFullAddress(dto.getProvince() + dto.getCity() + dto.getDistrict() + dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : address.getIsDefault());
        address.setUpdateTime(LocalDateTime.now());
        
        addressMapper.updateById(address);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        
        // 使用MyBatis Plus逻辑删除
        this.removeById(addressId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在或无权操作");
        }
        
        // 取消其他默认地址
        addressMapper.update(null, 
            new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0)
        );
        
        // 设置当前地址为默认
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.updateById(address);
    }
}

