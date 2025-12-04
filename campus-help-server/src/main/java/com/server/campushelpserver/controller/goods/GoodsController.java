package com.server.campushelpserver.controller.goods;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.goods.dto.GoodsDTO;
import com.server.campushelpserver.entity.goods.dto.GoodsSearchDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.goods.GoodsService;
import com.server.campushelpserver.service.search.SearchHistoryService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/api/goods")
@Tag(name = "商品管理", description = "商品管理相关接口")
public class GoodsController {
    
    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
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
    
    @Operation(summary = "发布商品", description = "发布商品（需要实名认证）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/publish")
    public Result<Long> publishGoods(@Parameter(description = "商品信息") @Validated @RequestBody GoodsDTO dto) {
        Long userId = getCurrentUserId();
        Long id = goodsService.publishGoods(dto, userId);
        return Result.success("发布成功", id);
    }
    
    @Operation(summary = "搜索商品列表", description = "根据条件搜索商品列表")
    @GetMapping("/list")
    public Result<Page<Goods>> searchGoods(@Parameter(description = "搜索条件") GoodsSearchDTO searchDTO) {
        // 保存搜索历史（如果有关键词）
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
            try {
                Long userId = null;
                try {
                    userId = getCurrentUserId();
                } catch (Exception e) {
                    // 未登录用户不保存搜索历史
                }
                if (userId != null) {
                    searchHistoryService.saveSearchHistory(userId, searchDTO.getKeyword().trim(), "GOODS");
                }
            } catch (Exception e) {
                // 保存搜索历史失败不影响搜索功能
            }
        }
        
        Page<Goods> page = goodsService.searchGoods(searchDTO);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    @GetMapping("/{id}")
    public Result<Goods> getGoodsDetail(@Parameter(description = "商品ID") @PathVariable Long id) {
        Goods goods = goodsService.getGoodsDetail(id);
        return Result.success("查询成功", goods);
    }
    
    @Operation(summary = "编辑商品", description = "编辑商品信息（仅发布者可编辑）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> updateGoods(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "商品信息") @Validated @RequestBody GoodsDTO dto) {
        Long userId = getCurrentUserId();
        goodsService.updateGoods(id, dto, userId);
        return Result.success("编辑成功", null);
    }
    
    @Operation(summary = "删除商品", description = "删除商品（逻辑删除，仅发布者可删除）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@Parameter(description = "商品ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        goodsService.deleteGoods(id, userId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "下架商品", description = "下架商品（用户自行下架，仅发布者可操作）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/offshelf")
    public Result<Void> offshelfGoods(@Parameter(description = "商品ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        goodsService.offshelfGoods(id, userId);
        return Result.success("下架成功", null);
    }
    
    @Operation(summary = "重新上架商品", description = "重新上架商品（仅发布者可操作）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/reshelf")
    public Result<Void> reshelfGoods(@Parameter(description = "商品ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        goodsService.reshelfGoods(id, userId);
        return Result.success("上架成功", null);
    }
    
    @Operation(summary = "获取当前用户发布的商品列表", description = "获取当前登录用户发布的所有商品信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/my-posts")
    public Result<Page<Goods>> getMyGoodsList(@Parameter(description = "搜索条件") GoodsSearchDTO searchDTO) {
        Long userId = getCurrentUserId();
        Page<Goods> page = goodsService.getMyGoodsList(searchDTO, userId);
        return Result.success("查询成功", page);
    }
}

