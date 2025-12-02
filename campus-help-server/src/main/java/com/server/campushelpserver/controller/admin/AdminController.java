package com.server.campushelpserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 管理员后台控制器
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员后台", description = "管理员后台管理相关接口")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 获取待审核的实名认证列表
     */
    @Operation(summary = "获取待审核的实名认证列表", description = "分页查询待审核的实名认证申请")
    @GetMapping("/verification/pending")
    public Result<Page<User>> getPendingVerifications(
            @Parameter(description = "分页参数") Page<User> page) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getVerificationStatus, "PENDING")
               .eq(User::getDeleteFlag, 0)
               .orderByDesc(User::getVerificationSubmitTime);
        
        Page<User> result = userService.page(page, wrapper);
        // 清除敏感信息
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success("查询成功", result);
    }
    
    /**
     * 封禁用户
     */
    @Operation(summary = "封禁用户", description = "管理员封禁违规用户")
    @PostMapping("/user/ban")
    public Result<Void> banUser(@Parameter(description = "封禁信息") @Validated @RequestBody BanUserRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User admin = userService.getUserByEmail(email);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        User user = userService.getById(request.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 不能封禁管理员
        if ("ADMIN".equals(user.getRole())) {
            return Result.error("不能封禁管理员");
        }
        
        // 更新用户状态
        user.setStatus(2); // 已封禁
        user.setBanType(request.getBanType());
        user.setBanReason(request.getReason());
        user.setBanTime(LocalDateTime.now());
        user.setBanAdminId(admin.getId());
        
        if ("TEMPORARY".equals(request.getBanType())) {
            user.setBanDays(request.getBanDays());
            // 计算解封时间
            user.setUnbanTime(LocalDateTime.now().plusDays(request.getBanDays()));
        }
        
        userService.updateById(user);
        return Result.success();
    }
    
    /**
     * 解封用户
     */
    @Operation(summary = "解封用户", description = "管理员解封被封禁的用户")
    @PostMapping("/user/unban/{userId}")
    public Result<Void> unbanUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getStatus() == null || user.getStatus() != 2) {
            return Result.error("该用户未被封禁");
        }
        
        // 解封用户
        user.setStatus(1); // 正常
        user.setBanType(null);
        user.setBanReason(null);
        user.setBanDays(null);
        user.setBanTime(null);
        user.setUnbanTime(null);
        user.setBanAdminId(null);
        
        userService.updateById(user);
        return Result.success();
    }
    
    /**
     * 封禁用户请求DTO
     */
    public static class BanUserRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotBlank(message = "封禁类型不能为空")
        private String banType; // TEMPORARY-临时，PERMANENT-永久
        
        @NotBlank(message = "封禁原因不能为空")
        private String reason;
        
        private Integer banDays; // 临时封禁时必填
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getBanType() {
            return banType;
        }
        
        public void setBanType(String banType) {
            this.banType = banType;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public Integer getBanDays() {
            return banDays;
        }
        
        public void setBanDays(Integer banDays) {
            this.banDays = banDays;
        }
    }
}

