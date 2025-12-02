package com.server.campushelpserver.util;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成测试类
 */
public class PasswordGeneratorTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encodedPassword = encoder.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
        // 验证
        System.out.println("验证结果: " + encoder.matches(password, encodedPassword));
    }
}

