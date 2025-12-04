package com.server.campushelpserver.service.impl.goods;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.goods.dto.GoodsDTO;
import com.server.campushelpserver.entity.goods.dto.GoodsSearchDTO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.goods.GoodsMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.common.PublishFrequencyService;
import com.server.campushelpserver.service.goods.GoodsService;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.service.sensitive.SensitiveWordService;
import com.server.campushelpserver.util.SensitiveWordCheckResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 商品服务实现类
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private PublishFrequencyService publishFrequencyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishGoods(GoodsDTO dto, Long userId) {
        // 1. 验证实名认证
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getIsVerified() == null || user.getIsVerified() != 1) {
            throw new BusinessException("发布商品需要实名认证，请先完成实名认证");
        }
        
        // 2. 验证价格（售价必须大于0）
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("售价必须大于0");
        }
        
        // 3. 验证库存
        if (dto.getStock() == null || dto.getStock() <= 0) {
            throw new BusinessException("库存数量必须大于0");
        }
        
        // 4. 验证交易方式
        if ("FACE_TO_FACE".equals(dto.getTradeMethod())) {
            if (!StringUtils.hasText(dto.getTradeLocation())) {
                throw new BusinessException("自提方式必须填写自提地点");
            }
        }
        
        // 5. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 6. 发布频率检测
        boolean frequencyOk = publishFrequencyService.checkFrequency(userId, "GOODS");
        
        // 7. 用户信用检测（新注册用户7天内）
        boolean newUser = false;
        if (user.getCreateTime() != null) {
            Duration duration = Duration.between(user.getCreateTime(), LocalDateTime.now());
            newUser = duration.toDays() < 7;
        }
        
        // 8. 创建商品信息
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setUserId(userId);
        goods.setCurrentPrice(dto.getPrice());
        goods.setStock(dto.getStock());
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                goods.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        // 设置默认值
        if (goods.getShippingFee() == null) {
            goods.setShippingFee(BigDecimal.ZERO);
        }
        if (goods.getViewCount() == null) {
            goods.setViewCount(0);
        }
        if (goods.getVersion() == null) {
            goods.setVersion(0);
        }
        
        // 9. 判断审核状态
        if (!checkResult.isPass() && !checkResult.isNeedManualReview()) {
            // 包含严重敏感词，自动拒绝
            goods.setStatus("REJECTED");
            goods.setAuditStatus("REJECTED");
            goods.setAuditReason(checkResult.getMessage());
            goods.setAuditTime(LocalDateTime.now());
        } else if (checkResult.isPass() && frequencyOk && !newUser) {
            // 自动审核通过
            goods.setStatus("ON_SALE");
            goods.setAuditStatus("APPROVED");
            goods.setAuditTime(LocalDateTime.now());
        } else {
            // 转人工审核
            goods.setStatus("PENDING_REVIEW");
            goods.setAuditStatus("PENDING");
            
            // 记录触发原因
            StringBuilder reason = new StringBuilder();
            if (!checkResult.isPass()) {
                reason.append(checkResult.getMessage()).append("；");
            }
            if (!frequencyOk) {
                reason.append("发布频繁；");
            }
            if (newUser) {
                reason.append("新注册用户；");
            }
            goods.setAuditTriggerReason(reason.toString());
        }
        
        goods.setCreateTime(LocalDateTime.now());
        
        // 10. 保存到数据库
        goodsMapper.insert(goods);
        
        // 11. 如果需要人工审核，发送消息给所有管理员
        if ("PENDING_REVIEW".equals(goods.getStatus())) {
            try {
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的商品待审核",
                    "有一条新的商品《" + goods.getTitle() + "》需要审核，触发原因：" + goods.getAuditTriggerReason(),
                    "GOODS",
                    goods.getId()
                );
            } catch (Exception e) {
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
        
        // 12. 如果被自动拒绝，抛出异常提示用户
        if ("REJECTED".equals(goods.getStatus())) {
            throw new BusinessException(checkResult.getMessage());
        }
        
        return goods.getId();
    }
    
    @Override
    public Page<Goods> searchGoods(GoodsSearchDTO searchDTO) {
        Page<Goods> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(Goods::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(Goods::getDescription, searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(Goods::getCategory, searchDTO.getCategory());
        }
        
        // 成色筛选
        if (StringUtils.hasText(searchDTO.getCondition())) {
            wrapper.eq(Goods::getCondition, searchDTO.getCondition());
        }
        
        // 状态筛选（默认只显示在售商品）
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(Goods::getStatus, searchDTO.getStatus());
        } else {
            wrapper.eq(Goods::getStatus, "ON_SALE");
        }
        
        // 价格区间筛选
        if (searchDTO.getMinPrice() != null) {
            wrapper.ge(Goods::getCurrentPrice, searchDTO.getMinPrice());
        }
        if (searchDTO.getMaxPrice() != null) {
            wrapper.le(Goods::getCurrentPrice, searchDTO.getMaxPrice());
        }
        
        // 不显示待审核和已拒绝的（普通用户看不到）
        wrapper.notIn(Goods::getStatus, "PENDING_REVIEW", "REJECTED");
        
        // 排序
        if ("price_asc".equals(searchDTO.getSortBy())) {
            wrapper.orderByAsc(Goods::getCurrentPrice);
        } else if ("price_desc".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(Goods::getCurrentPrice);
        } else if ("view".equals(searchDTO.getSortBy())) {
            wrapper.orderByDesc(Goods::getViewCount);
        } else {
            wrapper.orderByDesc(Goods::getCreateTime);
        }
        
        // 查询
        Page<Goods> resultPage = goodsMapper.selectPage(page, wrapper);
        
        // 为每个商品填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (Goods goods : resultPage.getRecords()) {
                fillUserInfo(goods);
            }
        }
        
        return resultPage;
    }
    
    @Override
    public Goods getGoodsDetail(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 权限检查
        boolean canView = false;
        
        try {
            String email = com.server.campushelpserver.util.SecurityUtils.getCurrentUserEmail();
            if (email != null) {
                User currentUser = userMapper.selectUserByEmail(email);
                if (currentUser != null) {
                    // 检查是否是发布者 - 发布者可以查看自己的商品（包括待审核和已拒绝的）
                    if (goods.getUserId().equals(currentUser.getId())) {
                        canView = true;
                    }
                    // 检查是否是管理员 - 管理员可以查看所有商品
                    if ("ADMIN".equals(currentUser.getRole())) {
                        canView = true;
                    }
                }
            }
        } catch (Exception e) {
            // 未登录用户继续检查
        }
        
        // 如果不是发布者和管理员，检查商品状态
        // 只有审核通过的商品（ON_SALE、SOLD_OUT、CLOSED等）才允许普通用户查看
        if (!canView && !"PENDING_REVIEW".equals(goods.getStatus()) && !"REJECTED".equals(goods.getStatus())) {
            canView = true;
        }
        
        if (!canView) {
            throw new BusinessException("无权查看此商品");
        }
        
        // 只有审核通过的商品才增加浏览量（发布者和管理员查看待审核/已拒绝的商品不增加浏览量）
        if (!"PENDING_REVIEW".equals(goods.getStatus()) && !"REJECTED".equals(goods.getStatus())) {
            goodsMapper.incrementViewCount(id);
        }
        
        // 填充用户信息
        fillUserInfo(goods);
        
        return goods;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(Long id, GoodsDTO dto, Long userId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!goods.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此商品");
        }
        
        // 检查是否有订单（这里简化处理，实际应该查询订单表）
        // 如果有订单，价格和库存不能修改
        
        // 验证价格
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("售价必须大于0");
        }
        
        // 记录原始状态
        String originalStatus = goods.getStatus();
        String originalAuditStatus = goods.getAuditStatus();
        
        // 更新商品信息
        BeanUtils.copyProperties(dto, goods, "id", "userId", "createTime", "viewCount", "version");
        goods.setCurrentPrice(dto.getPrice());
        goods.setImages(null);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                goods.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        goods.setUpdateTime(LocalDateTime.now());
        
        // 判断是否需要重新审核
        boolean needReaudit = false;
        
        // 如果之前被拒绝，编辑后需要重新审核
        if ("REJECTED".equals(originalStatus)) {
            needReaudit = true;
        }
        
        // 检测敏感词
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        if (!checkResult.isPass()) {
            needReaudit = true;
        }
        
        // 如果需要重新审核
        if (needReaudit) {
            goods.setStatus("PENDING_REVIEW");
            goods.setAuditStatus("PENDING");
            goods.setAuditTriggerReason(
                "REJECTED".equals(originalStatus) ? "重新发布被拒绝的商品" : 
                checkResult.getMessage()
            );
        }
        
        goodsMapper.updateById(goods);
        
        // 如果需要重新审核，发送消息给所有管理员
        if (needReaudit && "PENDING_REVIEW".equals(goods.getStatus())) {
            try {
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的商品待审核",
                    "有一条新的商品《" + goods.getTitle() + "》需要审核，触发原因：" + goods.getAuditTriggerReason(),
                    "GOODS",
                    goods.getId()
                );
            } catch (Exception e) {
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGoods(Long id, Long userId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!goods.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此商品");
        }
        
        // 使用MyBatis Plus逻辑删除
        this.removeById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offshelfGoods(Long id, Long userId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!goods.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        if (!"ON_SALE".equals(goods.getStatus())) {
            throw new BusinessException("只有上架的商品才能下架");
        }
        
        goods.setStatus("CLOSED");
        goods.setOffshelfType("USER");
        goods.setOffshelfTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reshelfGoods(Long id, Long userId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!goods.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }
        
        if (!"CLOSED".equals(goods.getStatus())) {
            throw new BusinessException("只有已关闭的商品才能重新上架");
        }
        
        if (goods.getStock() == null || goods.getStock() <= 0) {
            throw new BusinessException("库存为0，不能上架");
        }
        
        goods.setStatus("ON_SALE");
        goods.setOffshelfType(null);
        goods.setOffshelfTime(null);
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }
    
    @Override
    public Page<Goods> getMyGoodsList(GoodsSearchDTO searchDTO, Long userId) {
        Page<Goods> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Goods::getUserId, userId);
        
        // 状态筛选
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(Goods::getStatus, searchDTO.getStatus());
        }
        
        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(Goods::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(Goods::getDescription, searchDTO.getKeyword()));
        }
        
        wrapper.orderByDesc(Goods::getCreateTime);
        
        return goodsMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGoods(Long id, Integer auditResult, String auditReason, Long adminId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        if (!"PENDING_REVIEW".equals(goods.getStatus())) {
            throw new BusinessException("该商品当前不需要审核");
        }
        
        if (auditResult == 1) {
            // 审核通过
            goods.setStatus("ON_SALE");
            goods.setAuditStatus("APPROVED");
            goods.setAuditReason(null);
        } else {
            // 审核拒绝
            goods.setStatus("REJECTED");
            goods.setAuditStatus("REJECTED");
            goods.setAuditReason(auditReason);
        }
        
        goods.setAuditTime(LocalDateTime.now());
        goods.setAuditAdminId(adminId);
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        
        // 发送系统消息给发布者
        String messageType = auditResult == 1 ? "GOODS_APPROVED" : "GOODS_REJECTED";
        String messageTitle = auditResult == 1 ? "商品审核通过" : "商品审核被拒绝";
        String messageContent = auditResult == 1 
            ? "您的商品《" + goods.getTitle() + "》审核通过，已上架"
            : "很抱歉，您的商品《" + goods.getTitle() + "》审核被拒绝。原因：" + auditReason;
        
        systemMessageService.sendMessage(
            goods.getUserId(),
            messageType,
            messageTitle,
            messageContent,
            "GOODS",
            id
        );
    }
    
    @Override
    public Page<Goods> getPendingAuditList(GoodsSearchDTO searchDTO) {
        Page<Goods> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Goods::getStatus, "PENDING_REVIEW");
        wrapper.eq(Goods::getAuditStatus, "PENDING");
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(Goods::getCategory, searchDTO.getCategory());
        }
        
        // 关键词筛选
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(Goods::getTitle, searchDTO.getKeyword())
                             .or()
                             .like(Goods::getDescription, searchDTO.getKeyword()));
        }
        
        wrapper.orderByDesc(Goods::getCreateTime);
        
        return goodsMapper.selectPage(page, wrapper);
    }
    
    /**
     * 填充用户信息
     */
    private void fillUserInfo(Goods goods) {
        if (goods.getUserId() != null) {
            User user = userMapper.selectById(goods.getUserId());
            if (user != null) {
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                simpleUser.setIsVerified(user.getIsVerified());
                goods.setUser(simpleUser);
            }
        }
    }
}

