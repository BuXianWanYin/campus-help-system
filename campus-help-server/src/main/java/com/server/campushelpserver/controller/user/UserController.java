package com.server.campushelpserver.controller.user;

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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    
    @Operation(summary = "根据ID获取用户信息", description = "管理员根据用户ID获取用户详细信息")
    @PreAuthorize("hasAuthority('ADMIN')")
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

