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
    
    /**
     * 发送认领申请邮件（通知发布者）
     * @param to 收件人邮箱（发布者邮箱）
     * @param nickname 发布者昵称
     * @param itemTitle 失物标题
     */
    void sendClaimApplyEmail(String to, String nickname, String itemTitle);
    
    /**
     * 发送认领确认邮件（通知认领者）
     * @param to 收件人邮箱（认领者邮箱）
     * @param nickname 认领者昵称
     * @param itemTitle 失物标题
     */
    void sendClaimConfirmedEmail(String to, String nickname, String itemTitle);
    
    /**
     * 发送认领拒绝邮件（通知认领者）
     * @param to 收件人邮箱（认领者邮箱）
     * @param nickname 认领者昵称
     * @param itemTitle 失物标题
     * @param reason 拒绝原因
     */
    void sendClaimRejectedEmail(String to, String nickname, String itemTitle, String reason);
    
    /**
     * 异步发送认领申请邮件（通知发布者）
     * @param to 收件人邮箱（发布者邮箱）
     * @param nickname 发布者昵称
     * @param itemTitle 失物标题
     */
    void sendClaimApplyEmailAsync(String to, String nickname, String itemTitle);
    
    /**
     * 异步发送认领确认邮件（通知认领者）
     * @param to 收件人邮箱（认领者邮箱）
     * @param nickname 认领者昵称
     * @param itemTitle 失物标题
     */
    void sendClaimConfirmedEmailAsync(String to, String nickname, String itemTitle);
    
    /**
     * 异步发送认领拒绝邮件（通知认领者）
     * @param to 收件人邮箱（认领者邮箱）
     * @param nickname 认领者昵称
     * @param itemTitle 失物标题
     * @param reason 拒绝原因
     */
    void sendClaimRejectedEmailAsync(String to, String nickname, String itemTitle, String reason);
    
    /**
     * 异步发送订单创建邮件（通知卖家）
     * @param to 收件人邮箱（卖家邮箱）
     * @param nickname 卖家昵称
     * @param goodsTitle 商品标题
     * @param orderNo 订单号
     */
    void sendOrderCreatedEmailAsync(String to, String nickname, String goodsTitle, String orderNo);
    
    /**
     * 异步发送问题被回答邮件（通知问题发布者）
     * @param to 收件人邮箱（问题发布者邮箱）
     * @param nickname 问题发布者昵称
     * @param questionTitle 问题标题
     */
    void sendQuestionAnsweredEmailAsync(String to, String nickname, String questionTitle);
    
    /**
     * 异步发送回答被采纳邮件（通知回答者）
     * @param to 收件人邮箱（回答者邮箱）
     * @param nickname 回答者昵称
     * @param questionTitle 问题标题
     */
    void sendAnswerAcceptedEmailAsync(String to, String nickname, String questionTitle);
}

