package com.server.campushelpserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.user.VerificationCode;

/**
 * 验证码Service接口
 */
public interface VerificationCodeService extends IService<VerificationCode> {
    
    /**
     * 发送验证码
     * @param type 验证码类型
     * @param email 邮箱
     * @return 验证码
     */
    String sendVerificationCode(String type, String email);
    
    /**
     * 验证验证码
     * @param type 验证码类型
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String type, String email, String code);
}

