package com.server.campushelpserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.user.VerificationCode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 验证码Mapper接口
 */
@Mapper
public interface VerificationCodeMapper extends BaseMapper<VerificationCode> {
}

