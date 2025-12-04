package com.server.campushelpserver.mapper.address;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.address.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址Mapper接口
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}

