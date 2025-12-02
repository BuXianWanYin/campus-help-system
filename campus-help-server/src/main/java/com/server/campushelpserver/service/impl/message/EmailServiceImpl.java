package com.server.campushelpserver.service.impl.message;

import com.server.campushelpserver.service.message.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Value("${spring.mail.username}")
    private String mailFrom;
    
    private final JavaMailSender mailSender;
    
    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("校园帮系统 <" + mailFrom + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            logger.info("邮件发送成功：收件人={}, 主题={}", to, subject);
        } catch (Exception e) {
            logger.error("邮件发送失败：收件人={}, 主题={}, 错误={}", to, subject, e.getMessage(), e);
            // 邮件发送失败不影响主流程，只记录日志
        }
    }
    
    @Override
    public void sendVerificationApprovedEmail(String to, String realName) {
        String subject = "实名认证审核通过通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "恭喜您！您的实名认证已通过审核。\n\n" +
            "您现在可以：\n" +
            "1. 发布闲置商品和跑腿任务\n" +
            "2. 参与商品交易和接单跑腿\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            realName
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    public void sendVerificationRejectedEmail(String to, String realName, String reason) {
        String subject = "实名认证审核未通过通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "很抱歉，您的实名认证未通过审核。\n\n" +
            "拒绝原因：%s\n\n" +
            "您可以：\n" +
            "1. 登录系统查看详细拒绝原因\n" +
            "2. 修改信息后重新提交认证\n\n" +
            "如有疑问，请联系管理员。\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            realName, reason
        );
        sendEmail(to, subject, content);
    }
}

