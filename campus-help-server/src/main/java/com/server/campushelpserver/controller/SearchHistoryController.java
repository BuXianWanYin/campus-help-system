package com.server.campushelpserver.controller;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.SearchHistory;
import com.server.campushelpserver.entity.User;
import com.server.campushelpserver.service.SearchHistoryService;
import com.server.campushelpserver.service.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索历史控制器
 */
@RestController
@RequestMapping("/api/search-history")
@Tag(name = "搜索历史", description = "搜索历史相关接口")
public class SearchHistoryController {
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new com.server.campushelpserver.exception.BusinessException("未登录");
        }
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new com.server.campushelpserver.exception.BusinessException("用户不存在");
        }
        return user.getId();
    }
    
    @Operation(summary = "获取搜索历史列表", description = "获取当前用户的搜索历史")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public Result<List<SearchHistory>> getSearchHistoryList(
            @Parameter(description = "模块类型：LOST_FOUND-失物招领，GOODS-闲置交易，TASK-跑腿服务") @RequestParam(required = false) String moduleType,
            @Parameter(description = "限制数量") @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = getCurrentUserId();
        List<SearchHistory> list = searchHistoryService.getSearchHistoryList(userId, moduleType, limit);
        return Result.success("查询成功", list);
    }
    
    @Operation(summary = "删除搜索历史", description = "删除指定的搜索历史记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public Result<Void> deleteSearchHistory(
            @Parameter(description = "搜索历史ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        searchHistoryService.deleteSearchHistory(userId, id);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "清空搜索历史", description = "清空当前用户指定模块的搜索历史")
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public Result<Void> clearSearchHistory(
            @Parameter(description = "模块类型：LOST_FOUND-失物招领，GOODS-闲置交易，TASK-跑腿服务") @RequestParam(required = false) String moduleType) {
        Long userId = getCurrentUserId();
        searchHistoryService.clearSearchHistory(userId, moduleType);
        return Result.success("清空成功", null);
    }
}

