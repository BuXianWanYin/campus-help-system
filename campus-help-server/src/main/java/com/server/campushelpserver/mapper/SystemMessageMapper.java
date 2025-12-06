package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.SystemMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统消息Mapper接口
 */
@Mapper
public interface SystemMessageMapper extends BaseMapper<SystemMessage> {
}

