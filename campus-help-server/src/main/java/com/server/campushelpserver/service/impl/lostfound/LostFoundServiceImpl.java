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
import com.server.campushelpserver.service.common.PublishFrequencyService;
import com.server.campushelpserver.service.lostfound.LostFoundService;
import com.server.campushelpserver.service.sensitive.SensitiveWordService;
import com.server.campushelpserver.util.SensitiveWordCheckResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishLostFound(LostFoundDTO dto, Long userId) {
        // 1. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 2. 发布频率检测
        boolean frequencyOk = publishFrequencyService.checkFrequency(userId, "LOST_FOUND");
        
        // 3. 用户信用检测（新注册用户7天内）
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        boolean newUser = false;
        if (user.getCreateTime() != null) {
            Duration duration = Duration.between(user.getCreateTime(), LocalDateTime.now());
            newUser = duration.toDays() < 7;
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
        if (lostFound.getFavoriteCount() == null) {
            lostFound.setFavoriteCount(0);
        }
        if (lostFound.getCommentCount() == null) {
            lostFound.setCommentCount(0);
        }
        if (lostFound.getVersion() == null) {
            lostFound.setVersion(0);
        }
        
        // 5. 判断审核状态
        if (checkResult.isPass() && frequencyOk && !newUser) {
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
            if (newUser) {
                reason.append("新注册用户；");
            }
            lostFound.setAuditTriggerReason(reason.toString());
        }
        
        lostFound.setCreateTime(LocalDateTime.now());
        
        // 6. 保存到数据库
        lostFoundMapper.insert(lostFound);
        
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
            wrapper.orderByDesc(LostFound::getCreateTime);
        }
        
        // 2. 查询
        return lostFoundMapper.selectPage(page, wrapper);
    }
    
    @Override
    public LostFound getLostFoundDetail(Long id) {
        LostFound lostFound = lostFoundMapper.selectById(id);
        if (lostFound == null) {
            throw new BusinessException("失物信息不存在");
        }
        
        // 增加浏览次数
        lostFoundMapper.incrementViewCount(id);
        
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
        
        // 4. 检查是否已经申请过
        LambdaQueryWrapper<ClaimRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClaimRecord::getLostFoundId, dto.getLostFoundId())
               .eq(ClaimRecord::getClaimerId, userId);
        ClaimRecord existRecord = claimRecordMapper.selectOne(wrapper);
        if (existRecord != null) {
            throw new BusinessException("您已申请认领过该失物");
        }
        
        // 5. 使用乐观锁更新失物状态
        int rows = lostFoundMapper.updateStatusWithVersion(
                dto.getLostFoundId(),
                "PENDING_CLAIM",
                "CLAIMING",
                lostFound.getVersion()
        );
        
        if (rows == 0) {
            throw new BusinessException("认领失败，请重试");
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
        
        // 6. 更新失物状态
        lostFound.setStatus("CLAIMED");
        lostFoundMapper.updateById(lostFound);
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
        
        // 6. 失物状态恢复为待认领
        lostFound.setStatus("PENDING_CLAIM");
        lostFoundMapper.updateById(lostFound);
    }
}

