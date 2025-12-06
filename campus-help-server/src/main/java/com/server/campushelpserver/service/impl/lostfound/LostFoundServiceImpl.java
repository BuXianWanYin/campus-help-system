package com.server.campushelpserver.service.impl.lostfound;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.lostfound.ClaimRecord;
import com.server.campushelpserver.entity.lostfound.LostFound;
import com.server.campushelpserver.entity.lostfound.dto.ClaimDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundDTO;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundSearchDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.lostfound.ClaimRecordMapper;
import com.server.campushelpserver.mapper.lostfound.LostFoundMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.chat.ChatSessionService;
import com.server.campushelpserver.service.common.PublishFrequencyService;
import com.server.campushelpserver.service.lostfound.LostFoundService;
import com.server.campushelpserver.service.message.EmailService;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.service.sensitive.SensitiveWordService;
import com.server.campushelpserver.entity.chat.dto.CreateSessionDTO;
import com.server.campushelpserver.util.SensitiveWordCheckResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 失物招领服务实现类
 */
@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {
    
    @Autowired
    private LostFoundMapper lostFoundMapper;
    
    @Autowired
    private ClaimRecordMapper claimRecordMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private PublishFrequencyService publishFrequencyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishLostFound(LostFoundDTO dto, Long userId) {
        // 1. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 2. 发布频率检测
        boolean frequencyOk = publishFrequencyService.checkFrequency(userId, "LOST_FOUND");
        
        // 3. 验证用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 4. 创建失物信息
        LostFound lostFound = new LostFound();
        BeanUtils.copyProperties(dto, lostFound);
        lostFound.setUserId(userId);
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                lostFound.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        // 设置默认值
        if (lostFound.getReward() == null) {
            lostFound.setReward(java.math.BigDecimal.ZERO);
        }
        if (lostFound.getViewCount() == null) {
            lostFound.setViewCount(0);
        }
        if (lostFound.getVersion() == null) {
            lostFound.setVersion(0);
        }
        
        // 5. 判断审核状态
        if (!checkResult.isPass() && !checkResult.isNeedManualReview()) {
            // 包含严重敏感词，自动拒绝
            lostFound.setStatus("REJECTED");
            lostFound.setAuditStatus("REJECTED");
            lostFound.setAuditReason(checkResult.getMessage()); // 自动填充拒绝原因
            lostFound.setAuditTime(LocalDateTime.now());
        } else if (checkResult.isPass() && frequencyOk) {
            // 自动审核通过
            lostFound.setStatus("PENDING_CLAIM");
            lostFound.setAuditStatus("APPROVED");
            lostFound.setAuditTime(LocalDateTime.now());
        } else {
            // 转人工审核
            lostFound.setStatus("PENDING_REVIEW");
            lostFound.setAuditStatus("PENDING");
            
            // 记录触发原因
            StringBuilder reason = new StringBuilder();
            if (!checkResult.isPass()) {
                reason.append("包含敏感词；");
            }
            if (!frequencyOk) {
                reason.append("发布频繁；");
            }
            lostFound.setAuditTriggerReason(reason.toString());
        }
        
        lostFound.setCreateTime(LocalDateTime.now());
        
        // 6. 保存到数据库
        lostFoundMapper.insert(lostFound);
        
        // 7. 如果需要人工审核，发送消息给所有管理员
        if ("PENDING_REVIEW".equals(lostFound.getStatus())) {
            try {
                String typeText = "LOST".equals(lostFound.getType()) ? "失物" : "招领";
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的" + typeText + "待审核",
                    "有一条新的" + typeText + "《" + lostFound.getTitle() + "》需要审核，触发原因：" + lostFound.getAuditTriggerReason(),
                    "LOST_FOUND",
                    lostFound.getId()
                );
            } catch (Exception e) {
                // 发送通知失败不影响业务逻辑，只记录日志
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
        
        // 7. 如果被自动拒绝，抛出异常提示用户
        if ("REJECTED".equals(lostFound.getStatus())) {
            throw new BusinessException(checkResult.getMessage());
        }
        
        return lostFound.getId();
    }
    
    @Override
    public Page<LostFound> searchLostFound(LostFoundSearchDTO searchDTO) {
        // 1. 构建查询条件
        Page<LostFound> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(LostFound::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(LostFound::getDescription, searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(LostFound::getCategory, searchDTO.getCategory());
        }
        
        // 类型筛选
        if (StringUtils.hasText(searchDTO.getType())) {
            wrapper.eq(LostFound::getType, searchDTO.getType());
        }
        
        // 状态筛选
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(LostFound::getStatus, searchDTO.getStatus());
        } else {
            // 默认只显示待认领、认领中、已认领的
            wrapper.in(LostFound::getStatus, "PENDING_CLAIM", "CLAIMING", "CLAIMED");
        }
        
        // 地点筛选
        if (StringUtils.hasText(searchDTO.getLocation())) {
            wrapper.like(LostFound::getLostLocation, searchDTO.getLocation());
        }
        
        // 悬赏筛选
        if (searchDTO.getHasReward() != null && searchDTO.getHasReward()) {
            wrapper.gt(LostFound::getReward, 0);
        }
        
        // 排序
        if ("view".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(LostFound::getViewCount);
        } else if ("reward".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(LostFound::getReward);
        } else {
            // 默认按创建时间倒序（latest）
            wrapper.orderByDesc(LostFound::getCreateTime);
        }
        
        // 2. 查询
        Page<LostFound> resultPage = lostFoundMapper.selectPage(page, wrapper);
        
        // 3. 为每个失物填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (LostFound lostFound : resultPage.getRecords()) {
                fillUserInfo(lostFound);
            }
        }
        
        return resultPage;
    }
    
    @Override
    public LostFound getLostFoundDetail(Long id) {
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 只有审核通过的失物才增加浏览量（状态不是待审核和已拒绝）
        // 审核通过的状态：PENDING_CLAIM, CLAIMING, CLAIMED, CLOSED
        if (!"PENDING_REVIEW".equals(lostFound.getStatus()) && !"REJECTED".equals(lostFound.getStatus())) {
            lostFoundMapper.incrementViewCount(id);
        }
        
        // 填充用户信息
        fillUserInfo(lostFound);
        
        return lostFound;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyClaim(ClaimDTO dto, Long userId) {
        // 1. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(dto.getLostFoundId());
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 2. 验证状态
        if (!"PENDING_CLAIM".equals(lostFound.getStatus()) && !"CLAIMING".equals(lostFound.getStatus())) {
            throw new BusinessException("该失物当前不可认领");
        }
        
        // 3. 不能认领自己发布的
        if (lostFound.getUserId().equals(userId)) {
            throw new BusinessException("不能认领自己发布的失物");
        }
        
        // 4. 检查是否已经申请过（只检查未删除的）
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimRecord::getLostFoundId, dto.getLostFoundId())
               .eq(ClaimRecord::getClaimerId, userId)
               .eq(ClaimRecord::getDeleteFlag, 0); // 只检查未删除的
        ClaimRecord existRecord = claimRecordMapper.selectOne(wrapper);
        if (existRecord != null) {
            throw new BusinessException("您已申请认领过该失物");
        }
        
        // 5. 如果当前状态是PENDING_CLAIM，更新为CLAIMING（表示有认领申请）
        // 如果已经是CLAIMING，说明已有其他用户申请，不需要更新状态
        // 允许多个用户申请认领，所以不需要使用乐观锁强制更新
        if ("PENDING_CLAIM".equals(lostFound.getStatus())) {
            lostFound.setStatus("CLAIMING");
            lostFound.setUpdateTime(LocalDateTime.now());
            lostFoundMapper.updateById(lostFound);
        }
        
        // 6. 创建认领记录
        ClaimRecord record = new ClaimRecord();
        record.setLostFoundId(dto.getLostFoundId());
        record.setClaimerId(userId);
        record.setDescription(dto.getDescription());
        record.setLostTime(dto.getLostTime());
        record.setOtherInfo(dto.getOtherInfo());
        
        // 转换证明文件列表为JSON字符串
        if (dto.getProofImages() != null && !dto.getProofImages().isEmpty()) {
            try {
                record.setProofImages(objectMapper.writeValueAsString(dto.getProofImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("证明文件处理失败：" + e.getMessage());
            }
        }
        
        record.setStatus("PENDING");
        record.setCreateTime(LocalDateTime.now());
        claimRecordMapper.insert(record);
        
        // 7. 创建私信会话
        try {
            CreateSessionDTO createSessionDTO = new CreateSessionDTO();
            createSessionDTO.setTargetUserId(lostFound.getUserId());
            createSessionDTO.setRelatedType("LOST_FOUND");
            createSessionDTO.setRelatedId(lostFound.getId());
            chatSessionService.createOrGetSession(createSessionDTO, userId);
        } catch (Exception e) {
            // 创建会话失败不影响认领记录的创建，只记录日志
            System.err.println("创建私信会话失败: " + e.getMessage());
        }
        
        // 8. 发送系统消息通知给发布者
        try {
            systemMessageService.sendMessage(
                lostFound.getUserId(),
                "CLAIM_APPLY",
                "收到认领申请",
                "您发布的失物《" + lostFound.getTitle() + "》收到了认领申请，请及时查看",
                "LOST_FOUND",
                lostFound.getId()
            );
        } catch (Exception e) {
            // 发送通知失败不影响认领记录的创建，只记录日志
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
        
        // 9. 异步发送邮件通知给发布者
        try {
            User publisher = userMapper.selectById(lostFound.getUserId());
            if (publisher != null && publisher.getEmail() != null) {
                String publisherNickname = publisher.getNickname() != null ? publisher.getNickname() : "用户";
                emailService.sendClaimApplyEmailAsync(
                    publisher.getEmail(),
                    publisherNickname,
                    lostFound.getTitle()
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响认领记录的创建，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmClaim(Long claimRecordId, Long userId) {
        // 1. 查询认领记录
        ClaimRecord record = claimRecordMapper.selectById(claimRecordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        
        // 2. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(record.getLostFoundId());
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 3. 验证权限（只有发布者可以确认）
        if (!lostFound.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        // 4. 验证状态
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("该认领已处理");
        }
        
        // 5. 更新认领记录
        record.setStatus("CONFIRMED");
        record.setConfirmTime(LocalDateTime.now());
        claimRecordMapper.updateById(record);
        
        // 6. 拒绝其他所有待处理的认领申请
        LambdaQueryWrapper<ClaimRecord> otherWrapper = new LambdaQueryWrapper<>();
        otherWrapper.eq(ClaimRecord::getLostFoundId, lostFound.getId())
                   .eq(ClaimRecord::getStatus, "PENDING")
                   .ne(ClaimRecord::getId, claimRecordId)
                   .eq(ClaimRecord::getDeleteFlag, 0);
        List<ClaimRecord> otherRecords = claimRecordMapper.selectList(otherWrapper);
        if (otherRecords != null && !otherRecords.isEmpty()) {
            for (ClaimRecord otherRecord : otherRecords) {
                otherRecord.setStatus("REJECTED");
                otherRecord.setRejectReason("物品已被其他用户认领");
                otherRecord.setRejectTime(LocalDateTime.now());
                claimRecordMapper.updateById(otherRecord);
                
                // 发送系统消息通知给被拒绝的认领者
                try {
                    systemMessageService.sendMessage(
                        otherRecord.getClaimerId(),
                        "CLAIM_REJECTED",
                        "认领被拒绝",
                        "很抱歉，您认领的失物《" + lostFound.getTitle() + "》已被其他用户认领",
                        "LOST_FOUND",
                        lostFound.getId()
                    );
                } catch (Exception e) {
                    System.err.println("发送系统消息失败: " + e.getMessage());
                }
            }
        }
        
        // 7. 更新失物状态
        lostFound.setStatus("CLAIMED");
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(lostFound);
        
        // 7. 发送系统消息通知给认领者
        try {
            systemMessageService.sendMessage(
                record.getClaimerId(),
                "CLAIM_CONFIRMED",
                "认领成功",
                "您认领的失物《" + lostFound.getTitle() + "》已被确认，请联系发布者取回物品",
                "LOST_FOUND",
                lostFound.getId()
            );
        } catch (Exception e) {
            // 发送通知失败不影响业务逻辑，只记录日志
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
        
        // 8. 异步发送邮件通知给认领者
        try {
            User claimer = userMapper.selectById(record.getClaimerId());
            if (claimer != null && claimer.getEmail() != null) {
                String claimerNickname = claimer.getNickname() != null ? claimer.getNickname() : "用户";
                emailService.sendClaimConfirmedEmailAsync(
                    claimer.getEmail(),
                    claimerNickname,
                    lostFound.getTitle()
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响业务逻辑，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectClaim(Long claimRecordId, String reason, Long userId) {
        // 1. 查询认领记录
        ClaimRecord record = claimRecordMapper.selectById(claimRecordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        
        // 2. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(record.getLostFoundId());
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 3. 验证权限（只有发布者可以拒绝）
        if (!lostFound.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        
        // 4. 验证状态
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("该认领已处理");
        }
        
        // 5. 更新认领记录
        record.setStatus("REJECTED");
        record.setRejectReason(reason);
        record.setRejectTime(LocalDateTime.now());
        claimRecordMapper.updateById(record);
        
        // 6. 检查是否还有其他待处理的认领申请
        LambdaQueryWrapper<ClaimRecord> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ClaimRecord::getLostFoundId, lostFound.getId())
                   .eq(ClaimRecord::getStatus, "PENDING")
                   .eq(ClaimRecord::getDeleteFlag, 0);
        long pendingCount = claimRecordMapper.selectCount(checkWrapper);
        
        // 如果没有其他待处理的申请，状态恢复为待认领；否则保持认领中状态
        if (pendingCount == 0) {
            lostFound.setStatus("PENDING_CLAIM");
        } else {
            // 还有其他待处理的申请，保持认领中状态
            lostFound.setStatus("CLAIMING");
        }
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(lostFound);
        
        // 7. 发送系统消息通知给认领者
        try {
            systemMessageService.sendMessage(
                record.getClaimerId(),
                "CLAIM_REJECTED",
                "认领被拒绝",
                "很抱歉，您认领的失物《" + lostFound.getTitle() + "》被拒绝。原因：" + reason,
                "LOST_FOUND",
                lostFound.getId()
            );
        } catch (Exception e) {
            // 发送通知失败不影响业务逻辑，只记录日志
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
        
        // 8. 异步发送邮件通知给认领者
        try {
            User claimer = userMapper.selectById(record.getClaimerId());
            if (claimer != null && claimer.getEmail() != null) {
                String claimerNickname = claimer.getNickname() != null ? claimer.getNickname() : "用户";
                emailService.sendClaimRejectedEmailAsync(
                    claimer.getEmail(),
                    claimerNickname,
                    lostFound.getTitle(),
                    reason
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响业务逻辑，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
    }
    
    @Override
    public Page<LostFound> getMyPosts(LostFoundSearchDTO searchDTO, Long userId) {
        // 1. 构建查询条件
        Page<LostFound> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        
        // 必须查询当前用户发布的
        wrapper.eq(LostFound::getUserId, userId);
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(LostFound::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(LostFound::getDescription, searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(LostFound::getCategory, searchDTO.getCategory());
        }
        
        // 类型筛选
        if (StringUtils.hasText(searchDTO.getType())) {
            wrapper.eq(LostFound::getType, searchDTO.getType());
        }
        
        // 状态筛选（我的发布页面显示所有状态）
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(LostFound::getStatus, searchDTO.getStatus());
        }
        
        // 地点筛选
        if (StringUtils.hasText(searchDTO.getLocation())) {
            wrapper.like(LostFound::getLostLocation, searchDTO.getLocation());
        }
        
        // 排序
        if ("view".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(LostFound::getViewCount);
        } else if ("reward".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(LostFound::getReward);
        } else {
            // 默认按创建时间倒序（latest）
            wrapper.orderByDesc(LostFound::getCreateTime);
        }
        
        // 2. 查询
        Page<LostFound> resultPage = lostFoundMapper.selectPage(page, wrapper);
        
        // 3. 为每个失物填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (LostFound lostFound : resultPage.getRecords()) {
                fillUserInfo(lostFound);
            }
        }
        
        return resultPage;
    }
    
    @Override
    public java.util.List<ClaimRecord> getClaimRecords(Long lostFoundId) {
        // 查询该失物的所有认领记录（未删除的）
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimRecord::getLostFoundId, lostFoundId)
               .eq(ClaimRecord::getDeleteFlag, 0)  // 只查询未删除的
               .orderByDesc(ClaimRecord::getCreateTime);
        
        return claimRecordMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLostFound(Long id, LostFoundDTO dto, Long userId) {
        // 1. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 2. 验证权限（只有发布者可以编辑）
        if (!lostFound.getUserId().equals(userId)) {
            throw new BusinessException("无权编辑此失物");
        }
        
        // 3. 验证状态（已认领、已关闭的不允许编辑）
        if ("CLAIMED".equals(lostFound.getStatus()) || "CLOSED".equals(lostFound.getStatus())) {
            throw new BusinessException("该失物已认领或已关闭，无法编辑");
        }
        
        // 4. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 5. 更新失物信息
        BeanUtils.copyProperties(dto, lostFound, "id", "userId", "createTime", "status", "auditStatus", "viewCount", "favoriteCount", "commentCount", "version", "deleteFlag");
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                lostFound.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        // 6. 如果之前是被拒绝状态，编辑后需要重新审核
        boolean wasRejected = "REJECTED".equals(lostFound.getStatus());
        boolean needReaudit = false; // 标记是否需要重新审核
        
        // 7. 如果包含敏感词，需要重新审核
        if (!checkResult.isPass()) {
            lostFound.setStatus("PENDING_REVIEW");
            lostFound.setAuditStatus("PENDING");
            lostFound.setAuditTriggerReason("包含敏感词");
            lostFound.setAuditReason(null); // 清除之前的拒绝原因
            needReaudit = true;
        } else if (wasRejected) {
            // 如果之前是被拒绝状态，编辑后需要重新审核
            lostFound.setStatus("PENDING_REVIEW");
            lostFound.setAuditStatus("PENDING");
            lostFound.setAuditTriggerReason("编辑被拒绝的失物，需要重新审核");
            lostFound.setAuditReason(null); // 清除之前的拒绝原因
            needReaudit = true;
        } else if ("PENDING_REVIEW".equals(lostFound.getStatus())) {
            // 如果之前是待审核，编辑后仍然保持待审核
            needReaudit = true;
        }
        
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(lostFound);
        
        // 8. 如果需要重新审核，发送消息给所有管理员
        if (needReaudit && "PENDING_REVIEW".equals(lostFound.getStatus())) {
            try {
                String typeText = "LOST".equals(lostFound.getType()) ? "失物" : "招领";
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的" + typeText + "待审核",
                    "有一条新的" + typeText + "《" + lostFound.getTitle() + "》需要审核，触发原因：" + lostFound.getAuditTriggerReason(),
                    "LOST_FOUND",
                    lostFound.getId()
                );
            } catch (Exception e) {
                // 发送通知失败不影响业务逻辑，只记录日志
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLostFound(Long id, Long userId) {
        // 1. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 2. 验证权限（只有发布者可以删除）
        if (!lostFound.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此失物");
        }
        
        // 3. 验证状态（认领中的不允许删除，需要先处理认领）
        if ("CLAIMING".equals(lostFound.getStatus())) {
            throw new BusinessException("该失物正在认领中，无法删除");
        }
        
        // 4. 逻辑删除
        this.removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeLostFound(Long id, Long userId) {
        // 1. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 2. 验证权限（只有发布者可以关闭）
        if (!lostFound.getUserId().equals(userId)) {
            throw new BusinessException("无权关闭此失物");
        }
        
        // 3. 验证状态（已关闭的不允许再次关闭）
        if ("CLOSED".equals(lostFound.getStatus())) {
            throw new BusinessException("该失物已关闭");
        }
        
        // 4. 更新状态
        lostFound.setStatus("CLOSED");
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(lostFound);
    }
    
    /**
     * 获取当前用户对某个失物的申请
     */
    @Override
    public ClaimRecord getMyClaimRecord(Long lostFoundId, Long userId) {
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimRecord::getLostFoundId, lostFoundId)
               .eq(ClaimRecord::getClaimerId, userId)
               .eq(ClaimRecord::getDeleteFlag, 0)  // 未删除的
               .orderByDesc(ClaimRecord::getCreateTime)
               .last("LIMIT 1");
        
        ClaimRecord record = claimRecordMapper.selectOne(wrapper);
        
        // 填充用户信息
        if (record != null && record.getClaimerId() != null) {
            User user = userMapper.selectById(record.getClaimerId());
            if (user != null) {
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                record.setUser(simpleUser);
            }
        }
        
        return record;
    }
    
    /**
     * 更新认领申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateClaimRecord(Long claimRecordId, ClaimDTO dto, Long userId) {
        // 1. 查询认领记录
        ClaimRecord record = claimRecordMapper.selectById(claimRecordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        
        // 2. 验证权限（只有申请者本人可以编辑）
        if (!record.getClaimerId().equals(userId)) {
            throw new BusinessException("无权编辑此申请");
        }
        
        // 3. 验证状态（只有待处理状态的可以编辑）
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("只有待处理状态的申请可以编辑");
        }
        
        // 4. 查询失物信息，验证失物状态
        LostFound lostFound = lostFoundMapper.selectById(record.getLostFoundId());
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        if (!"PENDING_CLAIM".equals(lostFound.getStatus()) && !"CLAIMING".equals(lostFound.getStatus())) {
            throw new BusinessException("该失物当前不可认领，无法编辑申请");
        }
        
        // 5. 更新认领记录
        if (dto.getDescription() != null) {
            record.setDescription(dto.getDescription());
        }
        if (dto.getLostTime() != null) {
            record.setLostTime(dto.getLostTime());
        }
        if (dto.getOtherInfo() != null) {
            record.setOtherInfo(dto.getOtherInfo());
        }
        if (dto.getProofImages() != null) {
            try {
                record.setProofImages(objectMapper.writeValueAsString(dto.getProofImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("证明文件处理失败：" + e.getMessage());
            }
        }
        
        record.setUpdateTime(LocalDateTime.now());
        claimRecordMapper.updateById(record);
    }
    
    /**
     * 删除认领申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteClaimRecord(Long claimRecordId, Long userId) {
        // 1. 查询认领记录
        ClaimRecord record = claimRecordMapper.selectById(claimRecordId);
        if (record == null) {
            throw new BusinessException("认领记录不存在");
        }
        
        // 2. 验证权限（只有申请者本人可以删除）
        if (!record.getClaimerId().equals(userId)) {
            throw new BusinessException("无权删除此申请");
        }
        
        // 3. 验证状态（只有待处理状态的可以删除）
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException("只有待处理状态的申请可以删除");
        }
        
        // 4. 逻辑删除
        record.setDeleteFlag(1);
        record.setUpdateTime(LocalDateTime.now());
        claimRecordMapper.updateById(record);
        
        // 5. 检查该失物是否还有其他待处理的申请
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimRecord::getLostFoundId, record.getLostFoundId())
               .eq(ClaimRecord::getStatus, "PENDING")
               .eq(ClaimRecord::getDeleteFlag, 0);
        long pendingCount = claimRecordMapper.selectCount(wrapper);
        
        // 6. 如果没有其他待处理的申请，且失物状态是CLAIMING，则改回PENDING_CLAIM
        if (pendingCount == 0) {
            LostFound lostFound = lostFoundMapper.selectById(record.getLostFoundId());
            if (lostFound != null && "CLAIMING".equals(lostFound.getStatus())) {
                lostFound.setStatus("PENDING_CLAIM");
                lostFound.setUpdateTime(LocalDateTime.now());
                lostFoundMapper.updateById(lostFound);
            }
        }
    }
    
    /**
     * 管理员审核失物招领
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditLostFound(Long id, Integer auditResult, String auditReason, Long adminId) {
        // 1. 查询失物信息
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 2. 验证状态（只有待审核状态的可以审核）
        if (!"PENDING_REVIEW".equals(lostFound.getStatus()) || !"PENDING".equals(lostFound.getAuditStatus())) {
            throw new BusinessException("该失物不在待审核状态");
        }
        
        // 3. 验证拒绝原因
        if (auditResult == 0 && (auditReason == null || auditReason.trim().isEmpty())) {
            throw new BusinessException("拒绝审核必须填写拒绝原因");
        }
        
        // 4. 更新审核状态
        lostFound.setAuditStatus(auditResult == 1 ? "APPROVED" : "REJECTED");
        lostFound.setAuditReason(auditResult == 0 ? auditReason : null);
        lostFound.setAuditTime(LocalDateTime.now());
        lostFound.setAuditAdminId(adminId);
        
        if (auditResult == 1) {
            // 审核通过，状态改为待认领
            lostFound.setStatus("PENDING_CLAIM");
        } else {
            // 审核拒绝，状态改为已拒绝
            lostFound.setStatus("REJECTED");
        }
        
        lostFound.setUpdateTime(LocalDateTime.now());
        lostFoundMapper.updateById(lostFound);
        
        // 5. 发送系统消息通知发布者
        try {
            String typeText = "LOST".equals(lostFound.getType()) ? "失物" : "招领";
            String messageTitle = auditResult == 1 ? typeText + "审核通过" : typeText + "审核被拒绝";
            String messageContent = auditResult == 1 
                ? "您发布的" + typeText + "《" + lostFound.getTitle() + "》已通过审核，现已上线。"
                : "很抱歉，您发布的" + typeText + "《" + lostFound.getTitle() + "》未通过审核。原因：" + auditReason + "。您可以修改后重新提交审核。";
            
            systemMessageService.sendMessage(
                lostFound.getUserId(),
                auditResult == 1 ? "LOST_FOUND_APPROVED" : "LOST_FOUND_REJECTED",
                messageTitle,
                messageContent,
                "LOST_FOUND",
                lostFound.getId()
            );
        } catch (Exception e) {
            // 发送通知失败不影响业务逻辑，只记录日志
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审核的失物招领列表（管理员）
     */
    @Override
    public Page<LostFound> getPendingAuditList(LostFoundSearchDTO searchDTO) {
        Page<LostFound> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        
        // 审核状态筛选
        if (StringUtils.hasText(searchDTO.getAuditStatus()) && !"ALL".equals(searchDTO.getAuditStatus())) {
            if ("PENDING".equals(searchDTO.getAuditStatus())) {
                // 待审核：状态为PENDING_REVIEW且审核状态为PENDING
                wrapper.eq(LostFound::getStatus, "PENDING_REVIEW")
                       .eq(LostFound::getAuditStatus, "PENDING");
            } else if ("APPROVED".equals(searchDTO.getAuditStatus())) {
                // 已通过：审核状态为APPROVED
                wrapper.eq(LostFound::getAuditStatus, "APPROVED");
            } else if ("REJECTED".equals(searchDTO.getAuditStatus())) {
                // 已拒绝：审核状态为REJECTED
                wrapper.eq(LostFound::getAuditStatus, "REJECTED");
            }
        } else {
            // 默认查看所有需要审核的数据（包括待审核、已通过、已拒绝）
            // 只排除逻辑删除的
        }
        
        wrapper.eq(LostFound::getDeleteFlag, 0);
        
        // 类型筛选
        if (searchDTO.getType() != null && !searchDTO.getType().isEmpty()) {
            wrapper.eq(LostFound::getType, searchDTO.getType());
        }
        
        // 分类筛选
        if (searchDTO.getCategory() != null && !searchDTO.getCategory().isEmpty()) {
            wrapper.eq(LostFound::getCategory, searchDTO.getCategory());
        }
        
        // 地点筛选
        if (searchDTO.getLocation() != null && !searchDTO.getLocation().trim().isEmpty()) {
            wrapper.like(LostFound::getLostLocation, searchDTO.getLocation().trim());
        }
        
        // 关键词搜索
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
            String keyword = searchDTO.getKeyword().trim();
            wrapper.and(w -> w.like(LostFound::getTitle, keyword)
                             .or()
                             .like(LostFound::getDescription, keyword));
        }
        
        // 排序：默认按创建时间倒序，已审核的按审核时间倒序
        wrapper.orderByDesc(LostFound::getAuditTime, LostFound::getCreateTime);
        
        Page<LostFound> resultPage = lostFoundMapper.selectPage(page, wrapper);
        
        // 为每个失物填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (LostFound lostFound : resultPage.getRecords()) {
                fillUserInfo(lostFound);
            }
        }
        
        return resultPage;
    }
    
    /**
     * 填充用户信息
     */
    private void fillUserInfo(LostFound lostFound) {
        if (lostFound.getUserId() != null) {
            User user = userMapper.selectById(lostFound.getUserId());
            if (user != null) {
                // 创建简化的用户对象（只包含必要字段，避免返回敏感信息）
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                lostFound.setUser(simpleUser);
            }
        }
    }
}

