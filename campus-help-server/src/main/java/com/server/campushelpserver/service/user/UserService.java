package com.server.campushelpserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.user.User;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    User getUserByEmail(String email);
    
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册结果
     */
    User register(User user);
    
    /**
     * 用户登录
     * @param email 邮箱
     * @param password 密码
     * @return 用户信息
     */
    User login(String email, String password);
    
    /**
     * 根据邮箱获取当前用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    User getCurrentUser(String email);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUserInfo(User user);
    
    /**
     * 分页查询用户列表（管理员用）
     * @param page 分页参数
     * @param email 邮箱（可选）
     * @param nickname 昵称（可选）
     * @param role 角色（可选）
     * @param status 状态（可选）
     * @return 用户分页列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getUserPage(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page,
            String email, String nickname, String role, Integer status);
    
    /**
     * 提交实名认证
     * @param userId 用户ID
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @param studentId 学号
     * @return 更新后的用户信息
     */
    User submitVerification(Long userId, String realName, String idCard, String studentId);
    
    /**
     * 审核实名认证（管理员）
     * @param userId 用户ID
     * @param auditResult 审核结果：1-通过，0-拒绝
     * @param auditReason 审核原因
     * @return 更新后的用户信息
     */
    User auditVerification(Long userId, Integer auditResult, String auditReason);
    
    /**
     * 重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @return 更新后的用户信息
     */
    User resetPassword(String email, String newPassword);
    
    /**
     * 修改密码（需要验证当前密码）
     * @param email 邮箱
     * @param currentPassword 当前密码
     * @param newPassword 新密码
     * @return 更新后的用户信息
     */
    User changePassword(String email, String currentPassword, String newPassword);
}

