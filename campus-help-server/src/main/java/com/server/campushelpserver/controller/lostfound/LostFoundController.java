package com.server.campushelpserver.controller.lostfound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.lostfound.LostFound;
import com.server.campushelpserver.entity.lostfound.dto.ClaimDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundSearchDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.lostfound.LostFoundService;
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
 * 失物招领控制器
 */
@RestController
@RequestMapping("/api/lost-found")
@Tag(name = "失物招领", description = "失物招领相关接口")
public class LostFoundController {
    
    @Autowired
    private LostFoundService lostFoundService;
    
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
    
    @Operation(summary = "发布失物", description = "发布失物信息")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/publish")
    public Result<Long> publishLostFound(@Parameter(description = "失物信息") @Validated @RequestBody LostFoundDTO dto) {
        Long userId = getCurrentUserId();
        Long id = lostFoundService.publishLostFound(dto, userId);
        return Result.success("发布成功", id);
    }
    
    @Operation(summary = "搜索失物列表", description = "根据条件搜索失物列表")
    @GetMapping("/list")
    public Result<Page<LostFound>> searchLostFound(@Parameter(description = "搜索条件") LostFoundSearchDTO searchDTO) {
        Page<LostFound> page = lostFoundService.searchLostFound(searchDTO);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取失物详情", description = "根据ID获取失物详细信息")
    @GetMapping("/{id}")
    public Result<LostFound> getLostFoundDetail(@Parameter(description = "失物ID") @PathVariable Long id) {
        LostFound lostFound = lostFoundService.getLostFoundDetail(id);
        return Result.success("查询成功", lostFound);
    }
    
    @Operation(summary = "申请认领", description = "申请认领失物")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/claim")
    public Result<Void> applyClaim(@Parameter(description = "认领信息") @Validated @RequestBody ClaimDTO dto) {
        Long userId = getCurrentUserId();
        lostFoundService.applyClaim(dto, userId);
        return Result.success("认领申请已提交", null);
    }
    
    @Operation(summary = "确认认领", description = "确认认领申请")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/claim/{claimRecordId}/confirm")
    public Result<Void> confirmClaim(@Parameter(description = "认领记录ID") @PathVariable Long claimRecordId) {
        Long userId = getCurrentUserId();
        lostFoundService.confirmClaim(claimRecordId, userId);
        return Result.success("认领已确认", null);
    }
    
    @Operation(summary = "拒绝认领", description = "拒绝认领申请")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/claim/{claimRecordId}/reject")
    public Result<Void> rejectClaim(
            @Parameter(description = "认领记录ID") @PathVariable Long claimRecordId,
            @Parameter(description = "拒绝原因") @RequestParam String reason) {
        Long userId = getCurrentUserId();
        lostFoundService.rejectClaim(claimRecordId, reason, userId);
        return Result.success("认领已拒绝", null);
    }
}

