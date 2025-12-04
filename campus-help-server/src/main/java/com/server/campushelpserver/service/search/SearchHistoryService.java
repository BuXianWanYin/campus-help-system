package com.server.campushelpserver.service.search;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.search.SearchHistory;

import java.util.List;

/**
 * 搜索历史Service接口
 */
public interface SearchHistoryService extends IService<SearchHistory> {
    
    /**
     * 保存搜索历史
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param moduleType 模块类型
     */
    void saveSearchHistory(Long userId, String keyword, String moduleType);
    
    /**
     * 获取用户的搜索历史列表
     * @param userId 用户ID
     * @param moduleType 模块类型
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    List<SearchHistory> getSearchHistoryList(Long userId, String moduleType, Integer limit);
    
    /**
     * 删除搜索历史
     * @param userId 用户ID
     * @param historyId 搜索历史ID
     */
    void deleteSearchHistory(Long userId, Long historyId);
    
    /**
     * 清空用户的搜索历史
     * @param userId 用户ID
     * @param moduleType 模块类型
     */
    void clearSearchHistory(Long userId, String moduleType);
}

