package com.server.campushelpserver.service.user;

/**
 * 验证码Service接口（使用Redis存储）
 */
public interface VerificationCodeService {
    
    /**
     * 发送验证码
     * @param type 验证码类型（REGISTER、LOGIN、RESET_PASSWORD）
     * @param email 邮箱
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendVerificationCode(String type, String email);
    
    /**
     * 发送验证码（带客户端IP，用于防刷）
     * @param type 验证码类型（REGISTER、LOGIN、RESET_PASSWORD）
     * @param email 邮箱
     * @param clientIp 客户端IP
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    String sendVerificationCode(String type, String email, String clientIp);
    
    /**
     * 验证验证码
     * @param type 验证码类型
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String type, String email, String code);
}

