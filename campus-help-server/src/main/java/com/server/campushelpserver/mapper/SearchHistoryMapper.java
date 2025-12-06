package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.SearchHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索历史Mapper接口
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
}

