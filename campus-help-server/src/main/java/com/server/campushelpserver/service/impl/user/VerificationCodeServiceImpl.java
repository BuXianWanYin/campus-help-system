package com.server.campushelpserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.user.VerificationCode;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.user.VerificationCodeMapper;
import com.server.campushelpserver.service.user.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 验证码Service实现类
 */
@Service
public class VerificationCodeServiceImpl extends ServiceImpl<VerificationCodeMapper, VerificationCode> implements VerificationCodeService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendVerificationCode(String type, String email) {
        // 生成6位随机验证码
        String code = generateCode();
        
        // 将之前的验证码标记为已使用
        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getType, type);
        wrapper.eq(VerificationCode::getTarget, email);
        wrapper.eq(VerificationCode::getIsUsed, 0);
        wrapper.gt(VerificationCode::getExpireTime, LocalDateTime.now());
        VerificationCode existingCode = this.getOne(wrapper);
        if (existingCode != null) {
            existingCode.setIsUsed(1);
            this.updateById(existingCode);
        }
        
        // 保存验证码
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setType(type);
        verificationCode.setTarget(email);
        verificationCode.setCode(code);
        verificationCode.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        verificationCode.setIsUsed(0);
        this.save(verificationCode);
        
        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("校园帮助系统验证码");
            message.setText("您的验证码是：" + code + "，有效期5分钟，请勿泄露给他人。");
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException("邮件发送失败，请检查邮箱配置");
        }
        
        return code;
    }
    
    @Override
    public boolean verifyCode(String type, String email, String code) {
        // 查询验证码
        LambdaQueryWrapper<VerificationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerificationCode::getType, type);
        wrapper.eq(VerificationCode::getTarget, email);
        wrapper.eq(VerificationCode::getCode, code);
        wrapper.eq(VerificationCode::getIsUsed, 0);
        wrapper.gt(VerificationCode::getExpireTime, LocalDateTime.now());
        wrapper.orderByDesc(VerificationCode::getCreateTime);
        wrapper.last("LIMIT 1");
        VerificationCode verificationCode = this.getOne(wrapper);
        
        if (verificationCode == null) {
            return false;
        }
        
        // 标记为已使用
        verificationCode.setIsUsed(1);
        this.updateById(verificationCode);
        
        return true;
    }
    
    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}

