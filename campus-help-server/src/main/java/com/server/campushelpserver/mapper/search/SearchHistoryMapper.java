package com.server.campushelpserver.mapper.search;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.search.SearchHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 搜索历史Mapper接口
 */
@Mapper
public interface SearchHistoryMapper extends BaseMapper<SearchHistory> {
}

