package com.server.campushelpserver.controller.lostfound;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.lostfound.LostFound;
import com.server.campushelpserver.entity.lostfound.ClaimRecord;
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
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
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
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/claim")
    public Result<Void> applyClaim(@Parameter(description = "认领信息") @Validated @RequestBody ClaimDTO dto) {
        Long userId = getCurrentUserId();
        lostFoundService.applyClaim(dto, userId);
        return Result.success("认领申请已提交", null);
    }
    
    @Operation(summary = "确认认领", description = "确认认领申请")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/claim/{claimRecordId}/confirm")
    public Result<Void> confirmClaim(@Parameter(description = "认领记录ID") @PathVariable Long claimRecordId) {
        Long userId = getCurrentUserId();
        lostFoundService.confirmClaim(claimRecordId, userId);
        return Result.success("认领已确认", null);
    }
    
    @Operation(summary = "拒绝认领", description = "拒绝认领申请")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/claim/{claimRecordId}/reject")
    public Result<Void> rejectClaim(
            @Parameter(description = "认领记录ID") @PathVariable Long claimRecordId,
            @Parameter(description = "拒绝原因") @RequestParam String reason) {
        Long userId = getCurrentUserId();
        lostFoundService.rejectClaim(claimRecordId, reason, userId);
        return Result.success("认领已拒绝", null);
    }
    
    @Operation(summary = "获取当前用户发布的失物列表", description = "获取当前登录用户发布的所有失物信息")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/my-posts")
    public Result<Page<LostFound>> getMyPosts(@Parameter(description = "搜索条件") LostFoundSearchDTO searchDTO) {
        Long userId = getCurrentUserId();
        Page<LostFound> page = lostFoundService.getMyPosts(searchDTO, userId);
        return Result.success("查询成功", page);
    }
    
    @Operation(summary = "获取认领记录列表", description = "获取某个失物的所有认领记录")
    @GetMapping("/{id}/claims")
    public Result<java.util.List<ClaimRecord>> getClaimRecords(@Parameter(description = "失物ID") @PathVariable Long id) {
        java.util.List<ClaimRecord> records = lostFoundService.getClaimRecords(id);
        
        // 为每个认领记录填充用户信息
        for (ClaimRecord record : records) {
            User user = userService.getById(record.getClaimerId());
            if (user != null) {
                // 创建一个简化的用户对象（只包含必要字段，避免返回敏感信息）
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                record.setUser(simpleUser);
            }
        }
        
        return Result.success("查询成功", records);
    }
    
    @Operation(summary = "编辑失物", description = "编辑失物信息（仅发布者可编辑）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> updateLostFound(
            @Parameter(description = "失物ID") @PathVariable Long id,
            @Parameter(description = "失物信息") @Validated @RequestBody LostFoundDTO dto) {
        Long userId = getCurrentUserId();
        lostFoundService.updateLostFound(id, dto, userId);
        return Result.success("编辑成功", null);
    }
    
    @Operation(summary = "删除失物", description = "删除失物（逻辑删除，仅发布者可删除）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteLostFound(@Parameter(description = "失物ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        lostFoundService.deleteLostFound(id, userId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "关闭失物", description = "关闭失物（用户自行下架，仅发布者可操作）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/close")
    public Result<Void> closeLostFound(@Parameter(description = "失物ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        lostFoundService.closeLostFound(id, userId);
        return Result.success("关闭成功", null);
    }
    
    @Operation(summary = "获取我的申请", description = "获取当前用户对某个失物提交的申请")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}/my-claim")
    public Result<ClaimRecord> getMyClaimRecord(@Parameter(description = "失物ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        ClaimRecord record = lostFoundService.getMyClaimRecord(id, userId);
        
        // 为认领记录填充用户信息
        if (record != null && record.getClaimerId() != null) {
            User user = userService.getById(record.getClaimerId());
            if (user != null) {
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                record.setUser(simpleUser);
            }
        }
        
        return Result.success("查询成功", record);
    }
    
    @Operation(summary = "更新认领申请", description = "更新认领申请信息（仅申请者本人可操作）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/claim/{claimRecordId}")
    public Result<Void> updateClaimRecord(
            @Parameter(description = "认领记录ID") @PathVariable Long claimRecordId,
            @Parameter(description = "认领信息") @RequestBody ClaimDTO dto) {
        Long userId = getCurrentUserId();
        lostFoundService.updateClaimRecord(claimRecordId, dto, userId);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除认领申请", description = "删除认领申请（仅申请者本人可操作，仅待处理状态的可以删除）")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/claim/{claimRecordId}")
    public Result<Void> deleteClaimRecord(@Parameter(description = "认领记录ID") @PathVariable Long claimRecordId) {
        Long userId = getCurrentUserId();
        lostFoundService.deleteClaimRecord(claimRecordId, userId);
        return Result.success("删除成功", null);
    }
}

