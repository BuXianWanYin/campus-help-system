package com.server.campushelpserver.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 */
public class SecurityUtils {
    
    /**
     * 获取当前登录用户的邮箱
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal().toString();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户的ID
     * 从 JWT Token 中解析 userId
     */
    public static Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            String token = getTokenFromRequest(request);
            
            if (token == null || token.isEmpty()) {
                return null;
            }
            
            // 使用 JwtConfig 解析 token（需要从 Spring Context 中获取）
            // 由于是静态方法，这里暂时返回 null，由 Controller 层处理
            // 更好的方式是在 Controller 中通过 SecurityUtils.getCurrentUserEmail() 获取 email
            // 然后通过 UserService 查询用户获取 ID
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从请求中获取 Token（用于解析 userId）
     */
    private static String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

