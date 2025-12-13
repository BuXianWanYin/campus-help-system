package com.server.campushelpserver.service.impl;

import com.server.campushelpserver.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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
    
    @Override
    public void sendClaimApplyEmail(String to, String nickname, String itemTitle) {
        String subject = "收到失物认领申请通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "您发布的失物《%s》收到了认领申请。\n\n" +
            "请及时登录校园帮系统查看认领申请详情，并根据认领信息进行确认。\n\n" +
            "认领流程：\n" +
            "1. 登录系统，进入\"我的发布\"页面\n" +
            "2. 查看失物详情和认领申请\n" +
            "3. 核对认领信息后选择确认或拒绝\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, itemTitle
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    public void sendClaimConfirmedEmail(String to, String nickname, String itemTitle) {
        String subject = "失物认领成功通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "恭喜您！您认领的失物《%s》已被发布者确认。\n\n" +
            "请及时联系发布者沟通取回物品的相关事宜。\n\n" +
            "建议：\n" +
            "1. 通过系统私信功能与发布者沟通\n" +
            "2. 约定合适的取物时间和地点\n" +
            "3. 确认物品信息无误后再取回\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, itemTitle
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    public void sendClaimRejectedEmail(String to, String nickname, String itemTitle, String reason) {
        String subject = "失物认领未通过通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "很抱歉，您认领的失物《%s》未能通过发布者的确认。\n\n" +
            "拒绝原因：%s\n\n" +
            "您可以：\n" +
            "1. 查看失物详情，核对物品信息\n" +
            "2. 如果确认是您的物品，可以联系发布者进一步沟通\n" +
            "3. 继续关注其他失物信息\n\n" +
            "如有疑问，可以通过系统私信功能联系发布者。\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, itemTitle, reason != null ? reason : "未提供原因"
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendClaimApplyEmailAsync(String to, String nickname, String itemTitle) {
        sendClaimApplyEmail(to, nickname, itemTitle);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendClaimConfirmedEmailAsync(String to, String nickname, String itemTitle) {
        sendClaimConfirmedEmail(to, nickname, itemTitle);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendClaimRejectedEmailAsync(String to, String nickname, String itemTitle, String reason) {
        sendClaimRejectedEmail(to, nickname, itemTitle, reason);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendOrderCreatedEmailAsync(String to, String nickname, String goodsTitle, String orderNo) {
        String subject = "收到新订单通知";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "恭喜您！您的商品《%s》被购买了。\n\n" +
            "订单号：%s\n\n" +
            "请及时登录校园帮系统查看订单详情，并与买家沟通交易事宜。\n\n" +
            "建议操作：\n" +
            "1. 登录系统，进入\"我的交易\"页面\n" +
            "2. 查看订单详情和买家信息\n" +
            "3. 根据交易方式（邮寄/自提）安排发货或约定交易时间\n" +
            "4. 通过系统聊天功能与买家沟通\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, goodsTitle, orderNo
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendQuestionAnsweredEmailAsync(String to, String nickname, String questionTitle) {
        String subject = "您的问题收到新回答";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "您发布的学习问题《%s》收到了新的回答。\n\n" +
            "请及时登录校园帮系统查看回答详情，如果回答有帮助，可以采纳该回答。\n\n" +
            "建议操作：\n" +
            "1. 登录系统，进入\"我的发布\"页面\n" +
            "2. 查看问题详情和所有回答\n" +
            "3. 选择最满意的回答进行采纳\n" +
            "4. 如果问题已解决，可以将问题状态标记为已解决\n\n" +
            "感谢您使用校园帮系统！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, questionTitle
        );
        sendEmail(to, subject, content);
    }
    
    @Override
    @Async("emailExecutor")
    public void sendAnswerAcceptedEmailAsync(String to, String nickname, String questionTitle) {
        String subject = "您的回答被采纳";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "恭喜您！您对学习问题《%s》的回答已被提问者采纳。\n\n" +
            "您的回答帮助了其他同学，感谢您的贡献！\n\n" +
            "感谢您使用校园帮系统，继续为校园学习互助贡献力量！\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            nickname, questionTitle
        );
        sendEmail(to, subject, content);
    }
}

