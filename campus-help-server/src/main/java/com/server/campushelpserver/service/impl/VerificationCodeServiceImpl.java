package com.server.campushelpserver.service.impl;

import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码Service实现类（使用Redis存储）
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeServiceImpl.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Value("${verification.code.expire-minutes:5}")
    private int expireMinutes;
    
    @Value("${verification.code.length:6}")
    private int codeLength;
    
    @Value("${verification.code.redis-key-prefix:verification:code:}")
    private String redisKeyPrefix;
    
    @Value("${verification.code.limit.window-seconds:60}")
    private int limitWindowSeconds;
    
    @Value("${verification.code.limit.email-type-limit:1}")
    private int emailTypeLimit;
    
    @Value("${verification.code.limit.ip-limit:3}")
    private int ipLimit;
    
    @Value("${verification.code.limit.email-total-limit:5}")
    private int emailTotalLimit;
    
    @Value("${spring.mail.username}")
    private String mailFrom;
    
    /**
     * 发送验证码（带防刷机制）
     * @param type 验证码类型（REGISTER、LOGIN、RESET_PASSWORD）
     * @param email 邮箱
     * @param clientIp 客户端IP
     * @return 验证码（开发环境返回，生产环境不返回）
     */
    public String sendVerificationCode(String type, String email, String clientIp) {
        // 1. 检查同一邮箱同一类型限流
        String emailTypeKey = "verification:limit:" + type + ":" + email;
        String emailTypeCount = stringRedisTemplate.opsForValue().get(emailTypeKey);
        if (emailTypeCount != null) {
            Long remainingSeconds = stringRedisTemplate.getExpire(emailTypeKey, TimeUnit.SECONDS);
            throw new BusinessException("验证码发送过于频繁，请" + (remainingSeconds != null ? remainingSeconds : 60) + "秒后重试");
        }
        
        // 2. 检查同一IP限流
        String ipKey = "verification:limit:ip:" + clientIp;
        String ipCount = stringRedisTemplate.opsForValue().get(ipKey);
        int ipCountInt = ipCount == null ? 0 : Integer.parseInt(ipCount);
        if (ipCountInt >= ipLimit) {
            Long remainingSeconds = stringRedisTemplate.getExpire(ipKey, TimeUnit.SECONDS);
            throw new BusinessException("IP请求过于频繁，请" + (remainingSeconds != null ? remainingSeconds : 60) + "秒后重试");
        }
        
        // 3. 检查同一邮箱所有类型限流
        String emailTotalKey = "verification:limit:total:" + email;
        String emailTotalCount = stringRedisTemplate.opsForValue().get(emailTotalKey);
        int emailTotalCountInt = emailTotalCount == null ? 0 : Integer.parseInt(emailTotalCount);
        if (emailTotalCountInt >= emailTotalLimit) {
            Long remainingSeconds = stringRedisTemplate.getExpire(emailTotalKey, TimeUnit.SECONDS);
            throw new BusinessException("该邮箱验证码发送过于频繁，请" + (remainingSeconds != null ? remainingSeconds : 60) + "秒后重试");
        }
        
        // 4. 生成验证码
        String code = generateCode();
        
        // 5. 存储验证码到Redis（5分钟过期）
        String codeKey = redisKeyPrefix + type + ":" + email;
        stringRedisTemplate.opsForValue().set(codeKey, code, expireMinutes, TimeUnit.MINUTES);
        
        // 6. 设置限流标记
        // 同一邮箱同一类型：60秒内只能发送1次
        stringRedisTemplate.opsForValue().set(emailTypeKey, "1", limitWindowSeconds, TimeUnit.SECONDS);
        
        // 同一IP：增加计数
        stringRedisTemplate.opsForValue().increment(ipKey);
        stringRedisTemplate.expire(ipKey, limitWindowSeconds, TimeUnit.SECONDS);
        
        // 同一邮箱所有类型：增加计数
        stringRedisTemplate.opsForValue().increment(emailTotalKey);
        stringRedisTemplate.expire(emailTotalKey, limitWindowSeconds, TimeUnit.SECONDS);
        
        // 7. 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("校园帮系统 <" + mailFrom + ">");
            message.setTo(email);
            message.setSubject("校园帮系统验证码");
            message.setText("您的验证码是：" + code + "，有效期" + expireMinutes + "分钟，请勿泄露给他人。");
            mailSender.send(message);
        } catch (Exception e) {
            // 邮件发送失败，清理已存储的数据
            stringRedisTemplate.delete(codeKey);
            // 清理限流标记，允许用户重试
            stringRedisTemplate.delete(emailTypeKey);
            // 不清理IP和邮箱总限流标记，防止恶意重试
            
            // 记录详细的错误日志
            String errorMsg = e.getMessage();
            String errorClass = e.getClass().getSimpleName();
            
            // 记录完整异常信息到日志
            logger.error("邮件发送失败 - 异常类型: {}, 异常消息: {}, 目标邮箱: {}", 
                    errorClass, errorMsg, email, e);
            
            // 根据异常类型和消息提供更具体的错误提示
            if (errorMsg != null) {
                String lowerMsg = errorMsg.toLowerCase();
                if (lowerMsg.contains("timeout") || lowerMsg.contains("timed out")) {
                    throw new BusinessException("邮件发送超时，请检查网络连接或稍后重试");
                } else if (lowerMsg.contains("authentication") || lowerMsg.contains("auth") || lowerMsg.contains("535")) {
                    throw new BusinessException("邮箱认证失败，请检查邮箱账号和授权码配置");
                } else if (lowerMsg.contains("connection") || lowerMsg.contains("connect")) {
                    throw new BusinessException("无法连接到邮件服务器，请检查网络或邮件服务器配置");
                } else if (lowerMsg.contains("ssl") || lowerMsg.contains("tls")) {
                    throw new BusinessException("SSL/TLS 连接失败，请检查邮件服务器配置");
                }
            }
            
            // 默认错误提示
            throw new BusinessException("邮件发送失败：" + (errorMsg != null ? errorMsg : errorClass) + "，请检查邮箱配置或联系管理员");
        }
        
        return code;
    }
    
    /**
     * 发送验证码（兼容旧接口，不传IP时使用默认IP）
     */
    @Override
    public String sendVerificationCode(String type, String email) {
        return sendVerificationCode(type, email, "unknown");
    }
    
    /**
     * 验证验证码
     * @param type 验证码类型
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    @Override
    public boolean verifyCode(String type, String email, String code) {
        String codeKey = redisKeyPrefix + type + ":" + email;
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        
        if (storedCode == null) {
            return false;
        }
        
        if (!storedCode.equals(code)) {
            return false;
        }
        
        // 验证成功后删除验证码（防止重复使用）
        stringRedisTemplate.delete(codeKey);
        
        return true;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
