package com.server.campushelpserver.controller.auth;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.config.JwtConfig;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.service.user.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        
        // 注册用户
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
    
    @Operation(summary = "发送验证码", description = "发送邮箱验证码，支持注册、登录、重置密码")
    @PostMapping("/send-code")
    public Result<String> sendCode(
            @Parameter(description = "验证码类型：REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码") @RequestParam String type,
            @Parameter(description = "邮箱") @RequestParam String email) {
        String code = verificationCodeService.sendVerificationCode(type, email);
        return Result.success("验证码已发送，请查收邮件", code);
    }
}

