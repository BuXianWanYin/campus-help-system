package com.server.campushelpserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.SearchHistory;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.SearchHistoryMapper;
import com.server.campushelpserver.service.SearchHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史Service实现类
 */
@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSearchHistory(Long userId, String keyword, String moduleType) {
        if (userId == null) {
            return; // 未登录用户不保存搜索历史
        }
        
        if (!StringUtils.hasText(keyword)) {
            return; // 空关键词不保存
        }
        
        // 检查是否已存在相同的搜索记录（最近1小时内）
        LambdaQueryWrapper<SearchHistory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SearchHistory::getUserId, userId)
                   .eq(SearchHistory::getKeyword, keyword)
                   .eq(SearchHistory::getModuleType, moduleType)
                   .eq(SearchHistory::getDeleteFlag, 0)
                   .ge(SearchHistory::getSearchTime, LocalDateTime.now().minusHours(1))
                   .orderByDesc(SearchHistory::getSearchTime)
                   .last("LIMIT 1");
        
        SearchHistory existHistory = this.getOne(checkWrapper);
        if (existHistory != null) {
            // 如果已存在，更新搜索时间
            existHistory.setSearchTime(LocalDateTime.now());
            existHistory.setUpdateTime(LocalDateTime.now());
            this.updateById(existHistory);
            return;
        }
        
        // 创建新的搜索历史记录
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setModuleType(moduleType);
        history.setSearchTime(LocalDateTime.now());
        // deleteFlag 由 MyBatis Plus 自动处理，无需手动设置
        
        this.save(history);
        
        // 限制每个用户的搜索历史数量（最多保留20条）
        LambdaQueryWrapper<SearchHistory> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SearchHistory::getUserId, userId)
                   .eq(SearchHistory::getModuleType, moduleType)
                   .orderByDesc(SearchHistory::getSearchTime);
        
        List<SearchHistory> allHistory = this.list(countWrapper);
        if (allHistory.size() > 20) {
            // 删除最旧的记录（使用MyBatis Plus逻辑删除）
            List<SearchHistory> toDelete = allHistory.subList(20, allHistory.size());
            List<Long> idsToDelete = new ArrayList<>();
            for (SearchHistory h : toDelete) {
                idsToDelete.add(h.getId());
            }
            if (!idsToDelete.isEmpty()) {
                this.removeByIds(idsToDelete);
            }
        }
    }
    
    @Override
    public List<SearchHistory> getSearchHistoryList(Long userId, String moduleType, Integer limit) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
               .eq(SearchHistory::getDeleteFlag, 0)
               .orderByDesc(SearchHistory::getSearchTime);
        
        if (StringUtils.hasText(moduleType)) {
            wrapper.eq(SearchHistory::getModuleType, moduleType);
        }
        
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        } else {
            wrapper.last("LIMIT 10"); // 默认10条
        }
        
        return this.list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSearchHistory(Long userId, Long historyId) {
        SearchHistory history = this.getById(historyId);
        if (history == null) {
            throw new BusinessException("搜索历史不存在");
        }
        
        if (!history.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此搜索历史");
        }
        
        // 使用MyBatis Plus逻辑删除，自动更新delete_flag为1
        this.removeById(historyId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSearchHistory(Long userId, String moduleType) {
        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId);
        
        if (StringUtils.hasText(moduleType)) {
            wrapper.eq(SearchHistory::getModuleType, moduleType);
        }
        
        // 使用MyBatis Plus逻辑删除，自动更新delete_flag为1
        this.remove(wrapper);
    }
}

