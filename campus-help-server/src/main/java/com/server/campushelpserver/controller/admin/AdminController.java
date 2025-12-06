package com.server.campushelpserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.admin.dto.DailyTrendDTO;
import com.server.campushelpserver.entity.admin.dto.DashboardStatsDTO;
import com.server.campushelpserver.entity.goods.Goods;
import com.server.campushelpserver.entity.lostfound.LostFound;
import com.server.campushelpserver.entity.lostfound.dto.LostFoundSearchDTO;
import com.server.campushelpserver.entity.study.StudyQuestion;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.mapper.goods.GoodsMapper;
import com.server.campushelpserver.mapper.lostfound.LostFoundMapper;
import com.server.campushelpserver.mapper.study.StudyQuestionMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.lostfound.LostFoundService;
import com.server.campushelpserver.service.study.QuestionService;
import com.server.campushelpserver.service.user.UserService;
import com.server.campushelpserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理员后台控制器
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员后台", description = "管理员后台管理相关接口")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LostFoundService lostFoundService;
    
    @Autowired
    private com.server.campushelpserver.service.goods.GoodsService goodsService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private LostFoundMapper lostFoundMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private StudyQuestionMapper studyQuestionMapper;
    
    /**
     * 获取实名认证列表（支持按状态筛选和关键词搜索）
     */
    @Operation(summary = "获取实名认证列表", description = "分页查询实名认证申请，支持按状态筛选和关键词搜索")
    @GetMapping("/verification/list")
    public Result<Page<User>> getVerificationList(
            @Parameter(description = "分页参数") Page<User> page,
            @Parameter(description = "审核状态：PENDING-待审核，VERIFIED-已通过，REJECTED-已拒绝，为空则查询全部") 
            @RequestParam(required = false) String status,
            @Parameter(description = "关键词：搜索昵称、邮箱、真实姓名或学号")
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleteFlag, 0);
        
        // 如果指定了状态，则按状态筛选；否则查询所有状态的认证记录
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getVerificationStatus, status);
        } else {
            // 查询所有已提交认证的记录（排除未提交的）
            wrapper.in(User::getVerificationStatus, "PENDING", "VERIFIED", "REJECTED");
        }
        
        // 关键词搜索（昵称、邮箱、真实姓名、学号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getStudentId, keyword));
        }
        
        wrapper.orderByDesc(User::getVerificationSubmitTime);
        
        Page<User> result = userService.page(page, wrapper);
        // 清除敏感信息
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success("查询成功", result);
    }
    
    /**
     * 获取待审核的实名认证列表（兼容旧接口）
     */
    @Operation(summary = "获取待审核的实名认证列表", description = "分页查询待审核的实名认证申请")
    @GetMapping("/verification/pending")
    public Result<Page<User>> getPendingVerifications(
            @Parameter(description = "分页参数") Page<User> page) {
        return getVerificationList(page, "PENDING", null);
    }
    
    /**
     * 封禁用户
     */
    @Operation(summary = "封禁用户", description = "管理员封禁违规用户")
    @PostMapping("/user/ban")
    public Result<Void> banUser(@Parameter(description = "封禁信息") @Validated @RequestBody BanUserRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User admin = userService.getUserByEmail(email);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        User user = userService.getById(request.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 不能封禁管理员
        if ("ADMIN".equals(user.getRole())) {
            return Result.error("不能封禁管理员");
        }
        
        // 更新用户状态
        user.setStatus(2); // 已封禁
        user.setBanType(request.getBanType());
        user.setBanReason(request.getReason());
        user.setBanTime(LocalDateTime.now());
        user.setBanAdminId(admin.getId());
        
        if ("TEMPORARY".equals(request.getBanType())) {
            user.setBanDays(request.getBanDays());
            // 计算解封时间
            user.setUnbanTime(LocalDateTime.now().plusDays(request.getBanDays()));
        }
        
        userService.updateById(user);
        return Result.success();
    }
    
    /**
     * 解封用户
     */
    @Operation(summary = "解封用户", description = "管理员解封被封禁的用户")
    @PostMapping("/user/unban/{userId}")
    public Result<Void> unbanUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getStatus() == null || user.getStatus() != 2) {
            return Result.error("该用户未被封禁");
        }
        
        // 解封用户
        user.setStatus(1); // 正常
        user.setBanType(null);
        user.setBanReason(null);
        user.setBanDays(null);
        user.setBanTime(null);
        user.setUnbanTime(null);
        user.setBanAdminId(null);
        
        userService.updateById(user);
        return Result.success();
    }
    
    /**
     * 获取待审核的失物招领列表
     */
    @Operation(summary = "获取待审核的失物招领列表", description = "分页查询待审核的失物招领")
    @GetMapping("/lost-found/pending")
    public Result<Page<LostFound>> getPendingLostFoundList(
            @Parameter(description = "分页参数") Page<LostFound> page,
            @Parameter(description = "搜索条件") LostFoundSearchDTO searchDTO) {
        searchDTO.setPageNum((int) page.getCurrent());
        searchDTO.setPageSize((int) page.getSize());
        Page<LostFound> result = lostFoundService.getPendingAuditList(searchDTO);
        return Result.success("查询成功", result);
    }
    
    /**
     * 审核失物招领
     */
    @Operation(summary = "审核失物招领", description = "管理员审核失物招领")
    @PostMapping("/lost-found/{id}/audit")
    public Result<Void> auditLostFound(
            @Parameter(description = "失物ID") @PathVariable Long id,
            @Parameter(description = "审核信息") @Validated @RequestBody LostFoundAuditRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User admin = userService.getUserByEmail(email);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        lostFoundService.auditLostFound(id, request.getAuditResult(), request.getAuditReason(), admin.getId());
        String message = request.getAuditResult() == 1 ? "审核通过" : "审核拒绝";
        return Result.success(message, null);
    }
    
    /**
     * 封禁用户请求DTO
     */
    public static class BanUserRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotBlank(message = "封禁类型不能为空")
        private String banType; // TEMPORARY-临时，PERMANENT-永久
        
        @NotBlank(message = "封禁原因不能为空")
        private String reason;
        
        private Integer banDays; // 临时封禁时必填
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getBanType() {
            return banType;
        }
        
        public void setBanType(String banType) {
            this.banType = banType;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        public Integer getBanDays() {
            return banDays;
        }
        
        public void setBanDays(Integer banDays) {
            this.banDays = banDays;
        }
    }
    
    /**
     * 获取待审核的商品列表
     */
    @Operation(summary = "获取待审核的商品列表", description = "分页查询待审核的商品")
    @GetMapping("/goods/pending")
    public Result<Page<com.server.campushelpserver.entity.goods.Goods>> getPendingGoodsList(
            @Parameter(description = "分页参数") Page<com.server.campushelpserver.entity.goods.Goods> page,
            @Parameter(description = "搜索条件") com.server.campushelpserver.entity.goods.dto.GoodsSearchDTO searchDTO) {
        searchDTO.setPageNum((int) page.getCurrent());
        searchDTO.setPageSize((int) page.getSize());
        Page<com.server.campushelpserver.entity.goods.Goods> result = goodsService.getPendingAuditList(searchDTO);
        return Result.success("查询成功", result);
    }
    
    /**
     * 审核商品
     */
    @Operation(summary = "审核商品", description = "管理员审核商品")
    @PostMapping("/goods/{id}/audit")
    public Result<Void> auditGoods(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Parameter(description = "审核信息") @Validated @RequestBody GoodsAuditRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            return Result.error("未登录");
        }
        User admin = userService.getUserByEmail(email);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        
        goodsService.auditGoods(id, request.getAuditResult(), request.getAuditReason(), admin.getId());
        String message = request.getAuditResult() == 1 ? "审核通过" : "审核拒绝";
        return Result.success(message, null);
    }
    
    /**
     * 失物招领审核请求DTO
     */
    public static class LostFoundAuditRequest {
        @NotNull(message = "审核结果不能为空")
        private Integer auditResult; // 1-通过，0-拒绝
        
        private String auditReason; // 拒绝原因（拒绝时必填）
        
        // Getters and Setters
        public Integer getAuditResult() {
            return auditResult;
        }
        
        public void setAuditResult(Integer auditResult) {
            this.auditResult = auditResult;
        }
        
        public String getAuditReason() {
            return auditReason;
        }
        
        public void setAuditReason(String auditReason) {
            this.auditReason = auditReason;
        }
    }
    
    /**
     * 商品审核请求DTO
     */
    public static class GoodsAuditRequest {
        @NotNull(message = "审核结果不能为空")
        private Integer auditResult; // 1-通过，0-拒绝
        
        private String auditReason; // 拒绝原因（拒绝时必填）
        
        // Getters and Setters
        public Integer getAuditResult() {
            return auditResult;
        }
        
        public void setAuditResult(Integer auditResult) {
            this.auditResult = auditResult;
        }
        
        public String getAuditReason() {
            return auditReason;
        }
        
        public void setAuditReason(String auditReason) {
            this.auditReason = auditReason;
        }
    }
    
    /**
     * 获取数据概览统计信息
     */
    @Operation(summary = "获取数据概览统计信息", description = "获取管理员后台数据概览所需的统计数据")
    @GetMapping("/dashboard/stats")
    public Result<DashboardStatsDTO> getDashboardStats(
            @Parameter(description = "统计周期：7days-最近7天，30days-最近30天，semester-本学期，year-本学年") 
            @RequestParam(required = false, defaultValue = "7days") String period) {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 计算时间范围
        LocalDateTime startTime = calculateStartTime(period);
        
        // 1. 用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleteFlag, 0);
        stats.setTotalUsers(userMapper.selectCount(userWrapper));
        
        userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getVerificationStatus, "PENDING")
                   .eq(User::getDeleteFlag, 0);
        stats.setPendingVerifications(userMapper.selectCount(userWrapper));
        
        userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getVerificationStatus, "VERIFIED")
                   .eq(User::getDeleteFlag, 0);
        stats.setVerifiedUsers(userMapper.selectCount(userWrapper));
        
        userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getStatus, 2) // 已封禁
                   .eq(User::getDeleteFlag, 0);
        stats.setBannedUsers(userMapper.selectCount(userWrapper));
        
        // 2. 互助数据统计（只统计审核通过的）
        LambdaQueryWrapper<LostFound> lostFoundWrapper = new LambdaQueryWrapper<>();
        lostFoundWrapper.eq(LostFound::getAuditStatus, "APPROVED")
                       .eq(LostFound::getDeleteFlag, 0);
        if (startTime != null) {
            lostFoundWrapper.ge(LostFound::getCreateTime, startTime);
        }
        Long lostFoundCount = lostFoundMapper.selectCount(lostFoundWrapper);
        stats.setLostFoundCount(lostFoundCount);
        
        LambdaQueryWrapper<Goods> goodsWrapper = new LambdaQueryWrapper<>();
        goodsWrapper.eq(Goods::getAuditStatus, "APPROVED")
                   .eq(Goods::getDeleteFlag, 0);
        if (startTime != null) {
            goodsWrapper.ge(Goods::getCreateTime, startTime);
        }
        Long goodsCount = goodsMapper.selectCount(goodsWrapper);
        stats.setGoodsCount(goodsCount);
        
        LambdaQueryWrapper<StudyQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(StudyQuestion::getAuditStatus, "APPROVED")
                      .eq(StudyQuestion::getDeleteFlag, 0);
        if (startTime != null) {
            questionWrapper.ge(StudyQuestion::getCreateTime, startTime);
        }
        Long studyQuestionCount = studyQuestionMapper.selectCount(questionWrapper);
        stats.setStudyQuestionCount(studyQuestionCount);
        
        stats.setTotalAssistanceCount(lostFoundCount + goodsCount + studyQuestionCount);
        
        // 3. 活跃用户数（最近30天有发布失物、商品或问题的用户）
        LocalDateTime activeStartTime = LocalDateTime.now().minusDays(30);
        Set<Long> activeUserIds = new HashSet<>();
        
        // 获取最近30天发布失物的用户
        lostFoundWrapper = new LambdaQueryWrapper<>();
        lostFoundWrapper.eq(LostFound::getAuditStatus, "APPROVED")
                       .eq(LostFound::getDeleteFlag, 0)
                       .ge(LostFound::getCreateTime, activeStartTime)
                       .select(LostFound::getUserId);
        lostFoundMapper.selectList(lostFoundWrapper).forEach(lf -> activeUserIds.add(lf.getUserId()));
        
        // 获取最近30天发布商品的用户
        goodsWrapper = new LambdaQueryWrapper<>();
        goodsWrapper.eq(Goods::getAuditStatus, "APPROVED")
                   .eq(Goods::getDeleteFlag, 0)
                   .ge(Goods::getCreateTime, activeStartTime)
                   .select(Goods::getUserId);
        goodsMapper.selectList(goodsWrapper).forEach(g -> activeUserIds.add(g.getUserId()));
        
        // 获取最近30天发布问题的用户
        questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.eq(StudyQuestion::getAuditStatus, "APPROVED")
                      .eq(StudyQuestion::getDeleteFlag, 0)
                      .ge(StudyQuestion::getCreateTime, activeStartTime)
                      .select(StudyQuestion::getUserId);
        studyQuestionMapper.selectList(questionWrapper).forEach(q -> activeUserIds.add(q.getUserId()));
        
        stats.setActiveUsers((long) activeUserIds.size());
        
        // 4. 互助类型分布
        Map<String, Long> typeDistribution = new HashMap<>();
        typeDistribution.put("失物招领", lostFoundCount);
        typeDistribution.put("闲置交易", goodsCount);
        typeDistribution.put("学习互助", studyQuestionCount);
        stats.setTypeDistribution(typeDistribution);
        
        // 5. 每日互助趋势（最近7天）
        List<DailyTrendDTO> dailyTrends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);
            
            DailyTrendDTO trend = new DailyTrendDTO();
            trend.setDate(dayStart.format(formatter));
            
            // 失物招领
            lostFoundWrapper = new LambdaQueryWrapper<>();
            lostFoundWrapper.eq(LostFound::getAuditStatus, "APPROVED")
                           .eq(LostFound::getDeleteFlag, 0)
                           .ge(LostFound::getCreateTime, dayStart)
                           .le(LostFound::getCreateTime, dayEnd);
            trend.setLostFoundCount(lostFoundMapper.selectCount(lostFoundWrapper));
            
            // 闲置交易
            goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.eq(Goods::getAuditStatus, "APPROVED")
                       .eq(Goods::getDeleteFlag, 0)
                       .ge(Goods::getCreateTime, dayStart)
                       .le(Goods::getCreateTime, dayEnd);
            trend.setGoodsCount(goodsMapper.selectCount(goodsWrapper));
            
            // 学习互助
            questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(StudyQuestion::getAuditStatus, "APPROVED")
                          .eq(StudyQuestion::getDeleteFlag, 0)
                          .ge(StudyQuestion::getCreateTime, dayStart)
                          .le(StudyQuestion::getCreateTime, dayEnd);
            trend.setStudyQuestionCount(studyQuestionMapper.selectCount(questionWrapper));
            
            trend.setTotal(trend.getLostFoundCount() + trend.getGoodsCount() + trend.getStudyQuestionCount());
            dailyTrends.add(trend);
        }
        
        stats.setDailyTrends(dailyTrends);
        
        return Result.success("查询成功", stats);
    }
    
    /**
     * 根据周期计算开始时间
     */
    private LocalDateTime calculateStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "7days":
                return now.minusDays(7);
            case "30days":
                return now.minusDays(30);
            case "semester":
                // 假设学期从9月开始，或者从3月开始
                int month = now.getMonthValue();
                if (month >= 9 || month <= 2) {
                    // 上学期：9月-2月
                    return LocalDateTime.of(now.getYear() - (month <= 2 ? 1 : 0), 9, 1, 0, 0);
                } else {
                    // 下学期：3月-8月
                    return LocalDateTime.of(now.getYear(), 3, 1, 0, 0);
                }
            case "year":
                return LocalDateTime.of(now.getYear(), 1, 1, 0, 0);
            default:
                return now.minusDays(7);
        }
    }
}

