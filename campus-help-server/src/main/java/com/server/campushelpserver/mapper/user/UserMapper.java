package com.server.campushelpserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.user.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    User selectUserByEmail(String email);
}

