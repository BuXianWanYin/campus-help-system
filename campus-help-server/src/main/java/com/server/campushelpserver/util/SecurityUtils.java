package com.server.campushelpserver.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
     * 注意：此方法需要通过email查询用户获取ID，建议在Controller中通过UserService.getUserByEmail获取
     */
    public static Long getCurrentUserId() {
        // 由于SecurityContext中只存储了email，需要通过email查询用户获取ID
        // 此方法需要在有UserService注入的地方使用，或者直接通过email查询
        // 建议在Controller中通过以下方式获取：
        // String email = SecurityUtils.getCurrentUserEmail();
        // User user = userService.getUserByEmail(email);
        // Long userId = user != null ? user.getId() : null;
        return null;
    }
}

