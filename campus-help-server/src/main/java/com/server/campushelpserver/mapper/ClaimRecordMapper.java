package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.ClaimRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认领记录Mapper接口
 */
@Mapper
public interface ClaimRecordMapper extends BaseMapper<ClaimRecord> {
}

