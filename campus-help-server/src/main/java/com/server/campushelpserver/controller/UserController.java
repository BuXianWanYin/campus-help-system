package com.server.campushelpserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.dto.ChangePasswordRequestDTO;
import com.server.campushelpserver.entity.dto.VerificationAuditRequestDTO;
import com.server.campushelpserver.entity.dto.VerificationRequestDTO;
import com.server.campushelpserver.entity.User;
import com.server.campushelpserver.service.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息管理相关接口")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User user = userService.getCurrentUser(email);
        return Result.success("获取成功", user);
    }
    
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/current")
    public Result<User> updateCurrentUser(@Parameter(description = "用户信息") @Validated @RequestBody User user) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        // 设置当前用户ID
        user.setId(currentUser.getId());
        User updatedUser = userService.updateUserInfo(user);
        return Result.success("更新成功", updatedUser);
    }
    
    @Operation(summary = "分页查询用户列表", description = "管理员分页查询用户列表，支持条件筛选")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/page")
    public Result<Page<User>> getUserPage(
            @Parameter(description = "分页参数") Page<User> page,
            @Parameter(description = "邮箱（模糊查询）") @RequestParam(required = false) String email,
            @Parameter(description = "昵称（模糊查询）") @RequestParam(required = false) String nickname,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<User> result = userService.getUserPage(page, email, nickname, role, status);
        return Result.success("查询成功", result);
    }
    
    @Operation(summary = "提交实名认证", description = "用户提交实名认证信息")
    @PostMapping("/verification/submit")
    public Result<User> submitVerification(@Parameter(description = "认证信息") @Validated @RequestBody VerificationRequestDTO request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        User updatedUser = userService.submitVerification(
            currentUser.getId(),
            request.getRealName(),
            request.getIdCard(),
            request.getStudentId(),
            request.getUserType(),
            request.getProofImages()
        );
        return Result.success("认证信息提交成功，请等待管理员审核", updatedUser);
    }
    
    @Operation(summary = "审核实名认证", description = "管理员审核用户实名认证")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/verification/audit")
    public Result<User> auditVerification(@Parameter(description = "审核信息") @Validated @RequestBody VerificationAuditRequestDTO request) {
        User updatedUser = userService.auditVerification(
            request.getUserId(),
            request.getAuditResult(),
            request.getAuditReason()
        );
        String message = request.getAuditResult() == 1 ? "审核通过" : "审核拒绝";
        return Result.success(message, updatedUser);
    }
    
    @Operation(summary = "修改密码", description = "用户修改登录密码，需要验证当前密码")
    @PostMapping("/change-password")
    public Result<String> changePassword(@Parameter(description = "修改密码请求") @Validated @RequestBody ChangePasswordRequestDTO request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User currentUser = userService.getUserByEmail(email);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        return Result.success("密码修改成功，请重新登录");
    }
    
    @Operation(summary = "根据ID获取用户信息", description = "管理员根据用户ID获取用户详细信息")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public Result<User> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 清除敏感信息
        user.setPassword(null);
        return Result.success("查询成功", user);
    }
}

