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
}

