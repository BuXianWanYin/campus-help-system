package com.server.campushelpserver.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具类（用于生成BCrypt加密密码）
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = args.length > 0 ? args[0] : "123456";
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
    }
}

