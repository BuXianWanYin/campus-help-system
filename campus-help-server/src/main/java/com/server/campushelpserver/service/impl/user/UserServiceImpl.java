package com.server.campushelpserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.message.EmailService;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    public User getUserByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        wrapper.eq(User::getDeleteFlag, 0);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(User user) {
        // 检查邮箱是否已注册
        User existingUser = getUserByEmail(user.getEmail());
        if (existingUser != null) {
            throw new BusinessException("该邮箱已被注册，请使用其他邮箱或直接登录");
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("USER");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 管理员自动获得认证状态，无需实名认证
        if ("ADMIN".equals(user.getRole())) {
            user.setIsVerified(1);
            user.setCanAcceptTask(1);
            user.setVerificationStatus("VERIFIED");
        } else {
            if (user.getIsVerified() == null) {
                user.setIsVerified(0);
            }
            if (user.getCanAcceptTask() == null) {
                user.setCanAcceptTask(0);
            }
            if (user.getVerificationStatus() == null) {
                user.setVerificationStatus("NOT_VERIFIED");
            }
        }
        if (user.getUserType() == null) {
            user.setUserType("学生");
        }
        if (user.getGender() == null) {
            user.setGender(0);
        }
        if (user.getDeleteFlag() == null) {
            user.setDeleteFlag(0);
        }
        
        // 保存用户
        this.save(user);
        return user;
    }
    
    @Override
    public User login(String email, String password) {
        // 查询用户
        User user = getUserByEmail(email);
        if (user == null) {
            throw new BusinessException("该邮箱未注册，请先注册");
        }
        
        // 检查账号状态（1-正常，2-已封禁）
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误，请重新输入");
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
        
        return user;
    }
    
    @Override
    public User getCurrentUser(String email) {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清除敏感信息
        user.setPassword(null);
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUserInfo(User user) {
        // 检查用户是否存在
        User existingUser = this.getById(user.getId());
        if (existingUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改邮箱，检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(existingUser.getEmail())) {
            User emailUser = getUserByEmail(user.getEmail());
            if (emailUser != null && !emailUser.getId().equals(user.getId())) {
                throw new BusinessException("该邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息（不更新密码、角色、状态等敏感字段）
        if (StringUtils.hasText(user.getNickname())) {
            existingUser.setNickname(user.getNickname());
        }
        // 头像可以设置为空字符串来清除头像
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }
        if (StringUtils.hasText(user.getGrade())) {
            existingUser.setGrade(user.getGrade());
        }
        if (StringUtils.hasText(user.getMajor())) {
            existingUser.setMajor(user.getMajor());
        }
        if (StringUtils.hasText(user.getEmail())) {
            existingUser.setEmail(user.getEmail());
        }
        
        this.updateById(existingUser);
        return existingUser;
    }
    
    @Override
    public Page<User> getUserPage(Page<User> page, String email, String nickname, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(email)) {
            wrapper.like(User::getEmail, email);
        }
        if (StringUtils.hasText(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User submitVerification(Long userId, String realName, String idCard, String studentId, String userType, List<String> proofImages) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 管理员不需要实名认证
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("管理员无需实名认证");
        }
        
        // 检查是否已经认证
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException("您已完成实名认证，无需重复提交");
        }
        
        // 检查是否有待审核的认证
        if ("PENDING".equals(user.getVerificationStatus())) {
            throw new BusinessException("您的认证正在审核中，请勿重复提交");
        }
        
        // 检查身份证号是否已被使用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIdCard, idCard);
        wrapper.eq(User::getDeleteFlag, 0);
        wrapper.ne(User::getId, userId);
        User existingUser = this.getOne(wrapper);
        if (existingUser != null) {
            throw new BusinessException("该身份证号已被其他用户使用");
        }
        
        // 检查学号是否已被使用
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentId, studentId);
        wrapper.eq(User::getDeleteFlag, 0);
        wrapper.ne(User::getId, userId);
        existingUser = this.getOne(wrapper);
        if (existingUser != null) {
            throw new BusinessException("该学号已被其他用户使用");
        }
        
        // 验证证明文件
        if (proofImages == null || proofImages.isEmpty()) {
            throw new BusinessException("请至少上传1张证明文件");
        }
        if (proofImages.size() > 3) {
            throw new BusinessException("证明文件最多上传3张");
        }
        
        // 将证明文件列表转换为JSON字符串
        String proofJson;
        try {
            proofJson = objectMapper.writeValueAsString(proofImages);
        } catch (JsonProcessingException e) {
            throw new BusinessException("证明文件处理失败：" + e.getMessage());
        }
        
        // 更新用户信息（提交认证信息，等待审核）
        user.setRealName(realName);
        user.setIdCard(idCard);
        user.setStudentId(studentId);
        user.setUserType(userType);
        user.setVerificationProof(proofJson);
        user.setVerificationStatus("PENDING");
        user.setVerificationSubmitTime(LocalDateTime.now());
        // is_verified 保持为 0，等待管理员审核
        
        this.updateById(user);
        
        // 发送消息给所有管理员
        try {
            String userDisplayName = (user.getNickname() != null && !user.getNickname().trim().isEmpty()) 
                ? user.getNickname() 
                : user.getEmail();
            systemMessageService.sendMessageToAllAdmins(
                "ADMIN_VERIFICATION_REQUIRED",
                "新的实名认证待审核",
                "用户" + userDisplayName + "（" + user.getEmail() + "）提交了实名认证申请，请及时审核",
                "VERIFICATION",
                userId
            );
        } catch (Exception e) {
            // 发送通知失败不影响业务逻辑，只记录日志
            System.err.println("发送管理员通知失败: " + e.getMessage());
        }
        
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User auditVerification(Long userId, Integer auditResult, String auditReason) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查是否已经认证
        if (user.getIsVerified() != null && user.getIsVerified() == 1) {
            throw new BusinessException("该用户已完成实名认证，无需重复审核");
        }
        
        // 获取当前管理员ID（从SecurityContext中获取）
        Long adminId = getCurrentAdminId(); 
        
        if (auditResult == 1) {
            // 审核通过
            user.setIsVerified(1);
            user.setVerificationStatus("VERIFIED");
            // 实名认证后自动获得接单权限
            user.setCanAcceptTask(1);
            
            // 发送系统消息
            systemMessageService.sendMessage(
                userId,
                "VERIFICATION_APPROVED",
                "实名认证审核通过",
                "恭喜您！您的实名认证已通过审核。您现在可以发布闲置商品和跑腿任务，也可以参与商品交易和接单跑腿了。",
                "VERIFICATION",
                userId
            );
            
            // 发送邮件通知
            emailService.sendVerificationApprovedEmail(user.getEmail(), user.getRealName());
        } else {
            // 审核拒绝
            if (!StringUtils.hasText(auditReason)) {
                throw new BusinessException("拒绝审核必须填写审核原因");
            }
            user.setVerificationStatus("REJECTED");
            user.setVerificationAuditReason(auditReason);
            // 拒绝后不清空认证信息，用户可以查看拒绝原因后修改重新提交
            
            // 发送系统消息
            systemMessageService.sendMessage(
                userId,
                "VERIFICATION_REJECTED",
                "实名认证审核未通过",
                "很抱歉，您的实名认证未通过审核。拒绝原因：" + auditReason + "。您可以修改信息后重新提交认证。",
                "VERIFICATION",
                userId
            );
            
            // 发送邮件通知
            emailService.sendVerificationRejectedEmail(user.getEmail(), user.getRealName(), auditReason);
        }
        
        // 设置审核信息
        user.setVerificationAuditTime(LocalDateTime.now());
        user.setVerificationAuditAdminId(adminId);
        
        this.updateById(user);
        return user;
    }
    
    /**
     * 获取当前管理员ID（从SecurityContext中获取）
     */
    private Long getCurrentAdminId() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        User admin = getUserByEmail(email);
        return admin != null ? admin.getId() : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User resetPassword(String email, String newPassword) {
        // 检查用户是否存在
        User user = getUserByEmail(email);
        if (user == null) {
            throw new BusinessException("该邮箱未注册，请先注册");
        }
        
        // 验证新密码格式（至少8位，且包含字母和数字）
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessException("密码至少8位");
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new BusinessException("密码至少8位，且包含字母和数字");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User changePassword(String email, String currentPassword, String newPassword) {
        // 检查用户是否存在
        User user = getUserByEmail(email);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证当前密码
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("当前密码错误，请重新输入");
        }
        
        // 验证新密码不能与当前密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("新密码不能与当前密码相同");
        }
        
        // 验证新密码格式（至少8位，且包含字母和数字）
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessException("密码至少8位");
        }
        if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")) {
            throw new BusinessException("密码至少8位，且包含字母和数字");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        
        return user;
    }
}

