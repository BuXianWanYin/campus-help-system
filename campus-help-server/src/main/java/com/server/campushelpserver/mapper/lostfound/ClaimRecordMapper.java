package com.server.campushelpserver.mapper.lostfound;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.lostfound.ClaimRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认领记录Mapper接口
 */
@Mapper
public interface ClaimRecordMapper extends BaseMapper<ClaimRecord> {
}

