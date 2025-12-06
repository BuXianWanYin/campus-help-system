package com.server.campushelpserver.controller;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.config.JwtConfig;
import com.server.campushelpserver.entity.User;
import com.server.campushelpserver.service.UserService;
import com.server.campushelpserver.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户注册、登录、验证码相关接口")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Operation(summary = "用户注册", description = "邮箱注册，需要验证码验证")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(
            @Parameter(description = "用户信息") @Validated(User.RegisterGroup.class) @RequestBody User user,
            @Parameter(description = "验证码") @RequestParam String code) {
        // 验证验证码
        boolean isValid = verificationCodeService.verifyCode("REGISTER", user.getEmail(), code);
        if (!isValid) {
            return Result.error("验证码错误或已过期，请重新获取");
        }
        
        // 注册用户（包含事务，如果后续步骤失败会自动回滚）
        // 注意：生成Token在Controller层，不在事务内，如果Token生成失败，用户已保存
        // 但这种情况极少发生，且不影响用户使用（可以重新登录获取Token）
        User registeredUser = userService.register(user);
        
        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", registeredUser.getId());
        claims.put("email", registeredUser.getEmail());
        claims.put("role", registeredUser.getRole());
        claims.put("isVerified", registeredUser.getIsVerified());
        claims.put("canAcceptTask", registeredUser.getCanAcceptTask());
        String token = jwtConfig.generateToken(claims);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("user", registeredUser);
        result.put("token", token);
        return Result.success("注册成功", result);
    }
    
    @Operation(summary = "用户登录", description = "邮箱登录，支持密码登录和验证码登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "密码（密码登录时必填）") @RequestParam(required = false) String password,
            @Parameter(description = "验证码（验证码登录时必填）") @RequestParam(required = false) String code) {
        User user;
        
        // 判断是密码登录还是验证码登录
        if (password != null && !password.isEmpty()) {
            // 密码登录
            user = userService.login(email, password);
        } else if (code != null && !code.isEmpty()) {
            // 验证码登录
            boolean isValid = verificationCodeService.verifyCode("LOGIN", email, code);
            if (!isValid) {
                return Result.error("验证码错误或已过期，请重新获取");
            }
            user = userService.getUserByEmail(email);
            if (user == null) {
                return Result.error("该邮箱未注册，请先注册");
            }
            if (user.getStatus() == 0) {
                return Result.error("账号已被禁用，请联系管理员");
            }
        } else {
            return Result.error("密码或验证码不能为空");
        }
        
        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("isVerified", user.getIsVerified());
        claims.put("canAcceptTask", user.getCanAcceptTask());
        String token = jwtConfig.generateToken(claims);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        return Result.success("登录成功", result);
    }
    
    @Operation(summary = "检查邮箱是否已注册", description = "检查邮箱是否已被注册，用于前端实时验证")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱") @RequestParam String email) {
        User existingUser = userService.getUserByEmail(email);
        boolean isRegistered = existingUser != null;
        return Result.success(isRegistered ? "该邮箱已被注册" : "该邮箱可以注册", isRegistered);
    }
    
    @Operation(summary = "发送验证码", description = "发送邮箱验证码，支持注册、登录、重置密码，带防刷机制")
    @PostMapping("/send-code")
    public Result<String> sendCode(
            @Parameter(description = "验证码类型：REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码") @RequestParam String type,
            @Parameter(description = "邮箱") @RequestParam String email,
            HttpServletRequest request) {
        // 获取客户端IP
        String clientIp = getClientIp(request);
        String code = verificationCodeService.sendVerificationCode(type, email, clientIp);
        return Result.success("验证码已发送，请查收邮件", code);
    }
    
    @Operation(summary = "重置密码", description = "通过验证码重置密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "验证码") @RequestParam String code,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        // 验证验证码
        boolean isValid = verificationCodeService.verifyCode("RESET_PASSWORD", email, code);
        if (!isValid) {
            return Result.error("验证码错误或已过期，请重新获取");
        }
        
        // 检查用户是否存在
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return Result.error("该邮箱未注册，请先注册");
        }
        
        // 重置密码
        userService.resetPassword(email, newPassword);
        return Result.success("密码重置成功，请使用新密码登录");
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

