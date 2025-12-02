package com.server.campushelpserver.service.message;

/**
 * 邮件服务接口
 */
public interface EmailService {
    
    /**
     * 发送邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendEmail(String to, String subject, String content);
    
    /**
     * 发送实名认证审核通过邮件
     * @param to 收件人邮箱
     * @param realName 真实姓名
     */
    void sendVerificationApprovedEmail(String to, String realName);
    
    /**
     * 发送实名认证审核拒绝邮件
     * @param to 收件人邮箱
     * @param realName 真实姓名
     * @param reason 拒绝原因
     */
    void sendVerificationRejectedEmail(String to, String realName, String reason);
}

