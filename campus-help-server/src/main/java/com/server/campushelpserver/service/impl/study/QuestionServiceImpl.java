package com.server.campushelpserver.service.impl.study;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.campushelpserver.entity.study.StudyQuestion;
import com.server.campushelpserver.entity.study.StudyAnswer;
import com.server.campushelpserver.entity.study.dto.QuestionDTO;
import com.server.campushelpserver.entity.study.dto.AnswerDTO;
import com.server.campushelpserver.entity.study.dto.QuestionSearchDTO;
import com.server.campushelpserver.entity.study.dto.QuestionDetailVO;
import com.server.campushelpserver.entity.user.User;
import com.server.campushelpserver.exception.BusinessException;
import com.server.campushelpserver.mapper.study.StudyQuestionMapper;
import com.server.campushelpserver.mapper.study.StudyAnswerMapper;
import com.server.campushelpserver.mapper.user.UserMapper;
import com.server.campushelpserver.service.common.PublishFrequencyService;
import com.server.campushelpserver.service.message.SystemMessageService;
import com.server.campushelpserver.service.message.EmailService;
import com.server.campushelpserver.service.study.QuestionService;
import com.server.campushelpserver.service.sensitive.SensitiveWordService;
import com.server.campushelpserver.util.SensitiveWordCheckResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习问题服务实现类
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<StudyQuestionMapper, StudyQuestion> implements QuestionService {
    
    @Autowired
    private StudyQuestionMapper studyQuestionMapper;
    
    @Autowired
    private StudyAnswerMapper studyAnswerMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private com.server.campushelpserver.service.user.UserService userService;
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private PublishFrequencyService publishFrequencyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private SystemMessageService systemMessageService;
    
    @Autowired
    private EmailService emailService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishQuestion(QuestionDTO dto, Long userId) {
        // 1. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 2. 发布频率检测
        boolean frequencyOk = publishFrequencyService.checkFrequency(userId, "STUDY_QUESTION");
        
        // 3. 验证用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 4. 创建问题
        StudyQuestion question = new StudyQuestion();
        BeanUtils.copyProperties(dto, question);
        question.setUserId(userId);
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (dto.getImages().size() > 3) {
                throw new BusinessException("问题图片最多3张");
            }
            try {
                question.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        // 设置默认值
        if (question.getReward() == null) {
            question.setReward(BigDecimal.ZERO);
        }
        if (question.getViewCount() == null) {
            question.setViewCount(0);
        }
        if (question.getAnswerCount() == null) {
            question.setAnswerCount(0);
        }
        
        // 5. 判断审核状态
        if (!checkResult.isPass() && !checkResult.isNeedManualReview()) {
            // 包含严重敏感词，自动拒绝
            question.setStatus("REJECTED");
            question.setAuditStatus("REJECTED");
            question.setAuditReason(checkResult.getMessage());
            question.setAuditTime(LocalDateTime.now());
        } else if (checkResult.isPass() && frequencyOk) {
            // 自动审核通过
            question.setStatus("PENDING_ANSWER");
            question.setAuditStatus("APPROVED");
            question.setAuditTime(LocalDateTime.now());
        } else {
            // 转人工审核
            question.setStatus("PENDING_REVIEW");
            question.setAuditStatus("PENDING");
            
            // 记录触发原因
            StringBuilder reason = new StringBuilder();
            if (!checkResult.isPass()) {
                reason.append("包含敏感词；");
            }
            if (!frequencyOk) {
                reason.append("发布频繁；");
            }
            question.setAuditTriggerReason(reason.toString());
        }
        
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        
        // 6. 保存到数据库
        studyQuestionMapper.insert(question);
        
        // 7. 如果需要人工审核，发送消息给所有管理员
        if ("PENDING_REVIEW".equals(question.getStatus())) {
            try {
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的学习问题待审核",
                    "有一条新的学习问题《" + question.getTitle() + "》需要审核，触发原因：" + question.getAuditTriggerReason(),
                    "STUDY_QUESTION",
                    question.getId()
                );
            } catch (Exception e) {
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
        
        // 8. 如果被自动拒绝，抛出异常提示用户
        if ("REJECTED".equals(question.getStatus())) {
            throw new BusinessException(checkResult.getMessage());
        }
        
        return question.getId();
    }
    
    @Override
    public Page<StudyQuestion> getQuestionList(QuestionSearchDTO searchDTO) {
        // 1. 构建查询条件
        Page<StudyQuestion> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        
        // 检查是否是管理员
        boolean isAdmin = false;
        try {
            String email = com.server.campushelpserver.util.SecurityUtils.getCurrentUserEmail();
            if (email != null) {
                User currentUser = userService.getUserByEmail(email);
                if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                    isAdmin = true;
                }
            }
        } catch (Exception e) {
            // 获取用户信息失败，按普通用户处理
        }
        
        // 只查询已审核通过且未删除的问题
        wrapper.eq(StudyQuestion::getAuditStatus, "APPROVED")
               .ne(StudyQuestion::getStatus, "CANCELLED")
               .ne(StudyQuestion::getStatus, "REJECTED")
               .eq(StudyQuestion::getDeleteFlag, 0);
        
        // 管理员可以看到已下架的问题，普通用户看不到
        if (!isAdmin) {
            wrapper.ne(StudyQuestion::getStatus, "ADMIN_OFFSHELF");
        }
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(StudyQuestion::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(StudyQuestion::getDescription, searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(StudyQuestion::getCategory, searchDTO.getCategory());
        }
        
        // 状态筛选
        if (StringUtils.hasText(searchDTO.getStatus())) {
            wrapper.eq(StudyQuestion::getStatus, searchDTO.getStatus());
        }
        
        // 是否有酬劳筛选
        if (searchDTO.getHasReward() != null) {
            if (searchDTO.getHasReward()) {
                wrapper.gt(StudyQuestion::getReward, 0);
            } else {
                wrapper.eq(StudyQuestion::getReward, 0);
            }
        }
        
        // 排序
        if (StringUtils.hasText(searchDTO.getSortBy())) {
            switch (searchDTO.getSortBy()) {
                case "latest":
                    wrapper.orderByDesc(StudyQuestion::getCreateTime);
                    break;
                case "reward":
                    wrapper.orderByDesc(StudyQuestion::getReward);
                    break;
                case "popular":
                    wrapper.orderByDesc(StudyQuestion::getAnswerCount);
                    break;
                case "recent_answer":
                    wrapper.orderByDesc(StudyQuestion::getLastAnswerTime);
                    break;
                default:
                    wrapper.orderByDesc(StudyQuestion::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(StudyQuestion::getCreateTime);
        }
        
        // 2. 执行查询
        Page<StudyQuestion> resultPage = studyQuestionMapper.selectPage(page, wrapper);
        
        // 3. 为每个问题填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (StudyQuestion question : resultPage.getRecords()) {
                fillUserInfo(question);
            }
        }
        
        return resultPage;
    }
    
    @Override
    public QuestionDetailVO getQuestionDetail(Long questionId, Long userId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 如果不是管理员，检查审核状态
        if (userId == null) {
            // 未登录用户只能查看已审核通过的问题
            if (!"APPROVED".equals(question.getAuditStatus())) {
                throw new BusinessException("问题不存在");
            }
        } else {
            User user = userMapper.selectById(userId);
            if (user == null || !"ADMIN".equals(user.getRole())) {
                // 非管理员只能查看已审核通过的问题
                if (!"APPROVED".equals(question.getAuditStatus())) {
                    // 但发布者可以查看自己的问题
                    if (!question.getUserId().equals(userId)) {
                        throw new BusinessException("问题不存在");
                    }
                }
            }
        }
        
        // 3. 增加浏览次数（只有非发布者访问才增加）
        if (userId == null || !question.getUserId().equals(userId)) {
            studyQuestionMapper.incrementViewCount(questionId);
        }
        
        // 4. 查询回答列表（普通用户只能看到已审核通过的回答，管理员可以看到所有）
        LambdaQueryWrapper<StudyAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(StudyAnswer::getQuestionId, questionId)
                     .eq(StudyAnswer::getDeleteFlag, 0);
        
        // 检查是否是管理员
        boolean isAdmin = false;
        if (userId != null) {
            try {
                User currentUser = userMapper.selectById(userId);
                if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                    isAdmin = true;
                }
            } catch (Exception e) {
                // 获取用户信息失败，按普通用户处理
            }
        }
        
        // 普通用户只能看到已审核通过的回答
        if (!isAdmin) {
            answerWrapper.eq(StudyAnswer::getAuditStatus, "APPROVED");
        }
        
        answerWrapper.orderByDesc(StudyAnswer::getIsAccepted) // 已采纳的回答在前
                     .orderByDesc(StudyAnswer::getCreateTime); // 按时间倒序
        
        List<StudyAnswer> answers = studyAnswerMapper.selectList(answerWrapper);
        
        // 5. 填充问题用户信息
        fillUserInfo(question);
        
        // 6. 填充回答用户信息
        if (answers != null && !answers.isEmpty()) {
            for (StudyAnswer answer : answers) {
                fillAnswerUserInfo(answer);
            }
        }
        
        // 7. 构建返回对象
        QuestionDetailVO vo = new QuestionDetailVO();
        vo.setQuestion(question);
        vo.setAnswers(answers);
        
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long answerQuestion(Long questionId, AnswerDTO dto, Long userId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证问题状态
        if (!"APPROVED".equals(question.getAuditStatus())) {
            throw new BusinessException("问题尚未审核通过，无法回答");
        }
        
        if ("SOLVED".equals(question.getStatus()) || "CANCELLED".equals(question.getStatus())) {
            throw new BusinessException("该问题已解决或已取消，无法回答");
        }
        
        // 3. 检查是否已回答过
        LambdaQueryWrapper<StudyAnswer> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(StudyAnswer::getQuestionId, questionId)
                    .eq(StudyAnswer::getUserId, userId)
                    .eq(StudyAnswer::getDeleteFlag, 0);
        StudyAnswer existAnswer = studyAnswerMapper.selectOne(existWrapper);
        if (existAnswer != null) {
            throw new BusinessException("您已经回答过这个问题了");
        }
        
        // 4. 不能回答自己的问题
        if (question.getUserId().equals(userId)) {
            throw new BusinessException("不能回答自己的问题");
        }
        
        // 5. 敏感词检测
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(dto.getContent());
        
        // 6. 创建回答
        StudyAnswer answer = new StudyAnswer();
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent(dto.getContent());
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (dto.getImages().size() > 3) {
                throw new BusinessException("回答图片最多3张");
            }
            try {
                answer.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        }
        
        answer.setIsAccepted(0); // 0表示未采纳
        answer.setLikeCount(0);
        answer.setDeleteFlag(0);
        answer.setCreateTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        
        // 7. 判断审核状态（类似问题的审核机制）
        if (!checkResult.isPass() && !checkResult.isNeedManualReview()) {
            // 包含严重敏感词，自动拒绝
            throw new BusinessException(checkResult.getMessage());
        } else if (checkResult.isPass()) {
            // 自动审核通过
            answer.setAuditStatus("APPROVED");
            answer.setAuditTime(LocalDateTime.now());
        } else {
            // 转人工审核
            answer.setAuditStatus("PENDING");
            StringBuilder reason = new StringBuilder();
            if (!checkResult.isPass()) {
                reason.append("包含敏感词；");
            }
            answer.setAuditTriggerReason(reason.toString());
        }
        
        // 8. 保存回答
        studyAnswerMapper.insert(answer);
        
        // 9. 如果审核通过，更新问题的回答数量和最后回答时间
        if ("APPROVED".equals(answer.getAuditStatus())) {
            question.setAnswerCount((question.getAnswerCount() == null ? 0 : question.getAnswerCount()) + 1);
            question.setLastAnswerTime(LocalDateTime.now());
            if ("PENDING_ANSWER".equals(question.getStatus())) {
                question.setStatus("ANSWERED");
            }
            question.setUpdateTime(LocalDateTime.now());
            studyQuestionMapper.updateById(question);
            
            // 10. 发送系统消息通知问题发布者
            try {
                systemMessageService.sendMessage(
                    question.getUserId(),
                    "QUESTION_ANSWERED",
                    "您的问题有新回答",
                    "您的问题《" + question.getTitle() + "》收到了新的回答",
                    "STUDY_QUESTION",
                    questionId
                );
            } catch (Exception e) {
                System.err.println("发送系统消息失败: " + e.getMessage());
            }
        } else {
            // 11. 如果需要人工审核，发送消息给所有管理员
            try {
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的回答待审核",
                    "问题《" + question.getTitle() + "》收到了新的回答，需要审核，触发原因：" + answer.getAuditTriggerReason(),
                    "STUDY_ANSWER",
                    answer.getId()
                );
            } catch (Exception e) {
                System.err.println("发送管理员审核通知失败: " + e.getMessage());
            }
        }
        
        // 10. 异步发送邮件通知给问题发布者
        try {
            User questionPublisher = userMapper.selectById(question.getUserId());
            if (questionPublisher != null && questionPublisher.getEmail() != null) {
                String publisherNickname = (questionPublisher.getNickname() != null && !questionPublisher.getNickname().trim().isEmpty()) 
                    ? questionPublisher.getNickname() 
                    : questionPublisher.getEmail();
                emailService.sendQuestionAnsweredEmailAsync(
                    questionPublisher.getEmail(),
                    publisherNickname,
                    question.getTitle()
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响回答的创建，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
        
        return answer.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptAnswer(Long questionId, Long answerId, Long userId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证权限（只有发布者可以采纳答案）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("只有问题发布者可以采纳答案");
        }
        
        // 3. 验证问题状态
        if ("SOLVED".equals(question.getStatus()) || "CANCELLED".equals(question.getStatus())) {
            throw new BusinessException("该问题已解决或已取消，无法采纳答案");
        }
        
        // 4. 查询回答
        StudyAnswer answer = studyAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == 1) {
            throw new BusinessException("回答不存在");
        }
        
        // 5. 验证回答是否属于该问题
        if (!answer.getQuestionId().equals(questionId)) {
            throw new BusinessException("回答不属于该问题");
        }
        
        // 6. 如果已有采纳的答案，先取消采纳
        if (question.getAcceptedAnswerId() != null) {
            StudyAnswer oldAnswer = studyAnswerMapper.selectById(question.getAcceptedAnswerId());
            if (oldAnswer != null && oldAnswer.getDeleteFlag() == 0) {
                oldAnswer.setIsAccepted(0); // 0表示未采纳
                oldAnswer.setAcceptTime(null);
                oldAnswer.setUpdateTime(LocalDateTime.now());
                studyAnswerMapper.updateById(oldAnswer);
            }
        }
        
        // 7. 采纳答案
        answer.setIsAccepted(1); // 1表示已采纳
        answer.setAcceptTime(LocalDateTime.now());
        answer.setUpdateTime(LocalDateTime.now());
        studyAnswerMapper.updateById(answer);
        
        // 8. 更新问题状态
        question.setAcceptedAnswerId(answerId);
        question.setStatus("SOLVED");
        question.setSolveTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
        
        // 9. 发送系统消息通知回答者
        try {
            systemMessageService.sendMessage(
                answer.getUserId(),
                "ANSWER_ACCEPTED",
                "您的回答被采纳",
                "您对问题《" + question.getTitle() + "》的回答已被采纳",
                "STUDY_QUESTION",
                questionId
            );
        } catch (Exception e) {
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
        
        // 10. 异步发送邮件通知给回答者
        try {
            User answerer = userMapper.selectById(answer.getUserId());
            if (answerer != null && answerer.getEmail() != null) {
                String answererNickname = (answerer.getNickname() != null && !answerer.getNickname().trim().isEmpty()) 
                    ? answerer.getNickname() 
                    : answerer.getEmail();
                emailService.sendAnswerAcceptedEmailAsync(
                    answerer.getEmail(),
                    answererNickname,
                    question.getTitle()
                );
            }
        } catch (Exception e) {
            // 发送邮件失败不影响回答采纳，只记录日志
            System.err.println("发送邮件通知失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelQuestion(Long questionId, Long userId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证权限（只有发布者可以取消）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("只有问题发布者可以取消问题");
        }
        
        // 3. 验证状态
        if ("SOLVED".equals(question.getStatus())) {
            throw new BusinessException("已解决的问题无法取消");
        }
        
        if ("CANCELLED".equals(question.getStatus())) {
            throw new BusinessException("问题已取消");
        }
        
        // 4. 更新状态
        question.setStatus("CANCELLED");
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long questionId, QuestionDTO dto, Long userId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证权限（只有发布者可以编辑）
        if (!question.getUserId().equals(userId)) {
            throw new BusinessException("无权编辑此问题");
        }
        
        // 3. 验证状态（已解决、已取消的不允许编辑）
        if ("SOLVED".equals(question.getStatus()) || "CANCELLED".equals(question.getStatus())) {
            throw new BusinessException("该问题已解决或已取消，无法编辑");
        }
        
        // 4. 敏感词检测
        String checkText = dto.getTitle() + " " + dto.getDescription();
        SensitiveWordCheckResult checkResult = sensitiveWordService.check(checkText);
        
        // 5. 发布频率检测
        boolean frequencyOk = publishFrequencyService.checkFrequency(userId, "STUDY_QUESTION");
        
        // 6. 验证用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 7. 更新问题信息
        question.setTitle(dto.getTitle());
        question.setCategory(dto.getCategory());
        question.setDescription(dto.getDescription());
        question.setReward(dto.getReward() != null ? dto.getReward() : BigDecimal.ZERO);
        
        // 转换图片列表为JSON字符串
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            if (dto.getImages().size() > 3) {
                throw new BusinessException("问题图片最多3张");
            }
            try {
                question.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("图片处理失败：" + e.getMessage());
            }
        } else {
            question.setImages(null);
        }
        
        // 8. 如果之前是被拒绝状态，编辑后需要重新审核
        boolean wasRejected = "REJECTED".equals(question.getStatus());
        boolean needReaudit = false;
        
        // 9. 如果包含敏感词或发布频繁，需要重新审核
        if (!checkResult.isPass() || !frequencyOk) {
            question.setStatus("PENDING_REVIEW");
            question.setAuditStatus("PENDING");
            question.setAuditTriggerReason(null);
            question.setAuditReason(null); // 清除之前的拒绝原因
            question.setAuditTime(null);
            question.setAuditAdminId(null);
            
            // 构建触发原因
            StringBuilder reason = new StringBuilder();
            if (!checkResult.isPass()) {
                reason.append("包含敏感词；");
            }
            if (!frequencyOk) {
                reason.append("发布频繁；");
            }
            if (reason.length() > 0) {
                question.setAuditTriggerReason(reason.toString());
            }
            needReaudit = true;
        } else if (wasRejected) {
            // 如果之前是被拒绝状态，编辑后需要重新审核
            question.setStatus("PENDING_REVIEW");
            question.setAuditStatus("PENDING");
            question.setAuditTriggerReason("编辑被拒绝的问题，需要重新审核");
            question.setAuditReason(null); // 清除之前的拒绝原因
            question.setAuditTime(null);
            question.setAuditAdminId(null);
            needReaudit = true;
        } else if ("PENDING_REVIEW".equals(question.getStatus())) {
            // 如果之前是待审核，编辑后仍然保持待审核
            needReaudit = true;
        }
        
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
        
        // 10. 如果需要重新审核，发送消息给所有管理员
        if (needReaudit && "PENDING_REVIEW".equals(question.getStatus())) {
            try {
                systemMessageService.sendMessageToAllAdmins(
                    "ADMIN_AUDIT_REQUIRED",
                    "新的学习问题待审核",
                    "有一条新的学习问题《" + question.getTitle() + "》需要审核，触发原因：" + question.getAuditTriggerReason(),
                    "STUDY_QUESTION",
                    questionId
                );
            } catch (Exception e) {
                System.err.println("发送管理员通知失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    public Page<StudyQuestion> getMyPublishedQuestions(Long userId, String status, Integer pageNum, Integer pageSize) {
        Page<StudyQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(StudyQuestion::getUserId, userId)
               .eq(StudyQuestion::getDeleteFlag, 0);
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(StudyQuestion::getStatus, status);
        }
        
        wrapper.orderByDesc(StudyQuestion::getCreateTime);
        
        return studyQuestionMapper.selectPage(page, wrapper);
    }
    
    @Override
    public Page<StudyQuestion> getMyAnsweredQuestions(Long userId, String status, Integer pageNum, Integer pageSize) {
        // 1. 先查询用户回答过的问题ID列表
        LambdaQueryWrapper<StudyAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(StudyAnswer::getUserId, userId)
                     .eq(StudyAnswer::getDeleteFlag, 0)
                     .select(StudyAnswer::getQuestionId)
                     .groupBy(StudyAnswer::getQuestionId);
        
        List<StudyAnswer> answers = studyAnswerMapper.selectList(answerWrapper);
        if (answers == null || answers.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        
        // 2. 提取问题ID列表
        List<Long> questionIds = answers.stream()
            .map(StudyAnswer::getQuestionId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        // 3. 查询问题列表
        Page<StudyQuestion> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.in(StudyQuestion::getId, questionIds)
               .eq(StudyQuestion::getDeleteFlag, 0);
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(StudyQuestion::getStatus, status);
        }
        
        wrapper.orderByDesc(StudyQuestion::getCreateTime);
        
        return studyQuestionMapper.selectPage(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditQuestion(Long questionId, Boolean approved, String reason, Long adminId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证审核状态
        if (!"PENDING".equals(question.getAuditStatus())) {
            throw new BusinessException("该问题已审核，无需重复审核");
        }
        
        // 3. 更新审核结果
        question.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        question.setAuditReason(reason);
        question.setAuditTime(LocalDateTime.now());
        question.setAuditAdminId(adminId);
        
        if (approved) {
            question.setStatus("PENDING_ANSWER");
        } else {
            question.setStatus("REJECTED");
        }
        
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
        
        // 4. 发送系统消息通知发布者
        try {
            String messageTitle = approved ? "问题审核通过" : "问题审核未通过";
            String messageContent = approved 
                ? "您的问题《" + question.getTitle() + "》已审核通过"
                : "您的问题《" + question.getTitle() + "》审核未通过，原因：" + reason;
            
            systemMessageService.sendMessage(
                question.getUserId(),
                approved ? "QUESTION_APPROVED" : "QUESTION_REJECTED",
                messageTitle,
                messageContent,
                "STUDY_QUESTION",
                questionId
            );
        } catch (Exception e) {
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
    }
    
    @Override
    public Page<StudyQuestion> getPendingAuditList(QuestionSearchDTO searchDTO) {
        Page<StudyQuestion> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());
        LambdaQueryWrapper<StudyQuestion> wrapper = new LambdaQueryWrapper<>();
        
        // 审核状态筛选
        if (StringUtils.hasText(searchDTO.getAuditStatus()) && !"ALL".equals(searchDTO.getAuditStatus())) {
            if ("PENDING".equals(searchDTO.getAuditStatus())) {
                // 待审核：审核状态为PENDING
                wrapper.eq(StudyQuestion::getAuditStatus, "PENDING");
            } else if ("APPROVED".equals(searchDTO.getAuditStatus())) {
                // 已通过：审核状态为APPROVED
                wrapper.eq(StudyQuestion::getAuditStatus, "APPROVED");
            } else if ("REJECTED".equals(searchDTO.getAuditStatus())) {
                // 已拒绝：审核状态为REJECTED
                wrapper.eq(StudyQuestion::getAuditStatus, "REJECTED");
            }
        }
        // 如果为ALL或不传，则查询所有审核相关的记录（待审核、已通过、已拒绝）
        
        wrapper.eq(StudyQuestion::getDeleteFlag, 0);
        
        // 关键词搜索
        if (StringUtils.hasText(searchDTO.getKeyword())) {
            wrapper.and(w -> w.like(StudyQuestion::getTitle, searchDTO.getKeyword())
                            .or()
                            .like(StudyQuestion::getDescription, searchDTO.getKeyword()));
        }
        
        // 分类筛选
        if (StringUtils.hasText(searchDTO.getCategory())) {
            wrapper.eq(StudyQuestion::getCategory, searchDTO.getCategory());
        }
        
        // 排序：待审核的按创建时间正序（最早的先审核），已审核的按审核时间倒序
        wrapper.orderByAsc(StudyQuestion::getCreateTime)
               .orderByDesc(StudyQuestion::getAuditTime);
        
        Page<StudyQuestion> resultPage = studyQuestionMapper.selectPage(page, wrapper);
        
        // 为每个问题填充用户信息
        if (resultPage.getRecords() != null && !resultPage.getRecords().isEmpty()) {
            for (StudyQuestion question : resultPage.getRecords()) {
                fillUserInfo(question);
            }
        }
        
        return resultPage;
    }
    
    /**
     * 填充问题用户信息
     */
    private void fillUserInfo(StudyQuestion question) {
        if (question.getUserId() != null) {
            User user = userMapper.selectById(question.getUserId());
            if (user != null) {
                // 创建简化的用户对象（只包含必要字段，避免返回敏感信息）
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                question.setUser(simpleUser);
            }
        }
    }
    
    /**
     * 填充回答用户信息
     */
    private void fillAnswerUserInfo(StudyAnswer answer) {
        if (answer.getUserId() != null) {
            User user = userMapper.selectById(answer.getUserId());
            if (user != null) {
                // 创建简化的用户对象（只包含必要字段，避免返回敏感信息）
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setNickname(user.getNickname());
                simpleUser.setAvatar(user.getAvatar());
                answer.setUser(simpleUser);
            }
        }
    }
    
    @Override
    public java.util.List<StudyAnswer> getAnswersByQuestionId(Long questionId) {
        LambdaQueryWrapper<StudyAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyAnswer::getQuestionId, questionId)
               .eq(StudyAnswer::getDeleteFlag, 0)
               .orderByDesc(StudyAnswer::getIsAccepted)
               .orderByDesc(StudyAnswer::getCreateTime);
        
        java.util.List<StudyAnswer> answers = studyAnswerMapper.selectList(wrapper);
        
        // 填充用户信息
        if (answers != null && !answers.isEmpty()) {
            for (StudyAnswer answer : answers) {
                fillAnswerUserInfo(answer);
            }
        }
        
        return answers;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditAnswer(Long answerId, Boolean approved, String reason, Long adminId) {
        // 1. 查询回答
        StudyAnswer answer = studyAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == 1) {
            throw new BusinessException("回答不存在");
        }
        
        // 2. 验证审核状态
        if (!"PENDING".equals(answer.getAuditStatus())) {
            throw new BusinessException("该回答已审核，无需重复审核");
        }
        
        // 3. 更新审核结果
        answer.setAuditStatus(approved ? "APPROVED" : "REJECTED");
        answer.setAuditTime(LocalDateTime.now());
        answer.setAuditAdminId(adminId);
        if (!approved) {
            answer.setAuditReason(reason);
        }
        answer.setUpdateTime(LocalDateTime.now());
        studyAnswerMapper.updateById(answer);
        
        // 4. 如果审核通过，更新问题的回答数量和最后回答时间
        if (approved) {
            StudyQuestion question = studyQuestionMapper.selectById(answer.getQuestionId());
            if (question != null) {
                question.setAnswerCount((question.getAnswerCount() == null ? 0 : question.getAnswerCount()) + 1);
                question.setLastAnswerTime(LocalDateTime.now());
                if ("PENDING_ANSWER".equals(question.getStatus())) {
                    question.setStatus("ANSWERED");
                }
                question.setUpdateTime(LocalDateTime.now());
                studyQuestionMapper.updateById(question);
                
                // 5. 发送系统消息通知问题发布者
                try {
                    systemMessageService.sendMessage(
                        question.getUserId(),
                        "QUESTION_ANSWERED",
                        "您的问题有新回答",
                        "您的问题《" + question.getTitle() + "》收到了新的回答",
                        "STUDY_QUESTION",
                        question.getId()
                    );
                } catch (Exception e) {
                    System.err.println("发送系统消息失败: " + e.getMessage());
                }
            }
        } else {
            // 6. 如果审核拒绝，发送系统消息通知回答者
            try {
                StudyQuestion question = studyQuestionMapper.selectById(answer.getQuestionId());
                String questionTitle = question != null ? question.getTitle() : "未知问题";
                systemMessageService.sendMessage(
                    answer.getUserId(),
                    "ANSWER_REJECTED",
                    "您的回答审核未通过",
                    "您对问题《" + questionTitle + "》的回答审核未通过，原因：" + reason,
                    "STUDY_ANSWER",
                    answerId
                );
            } catch (Exception e) {
                System.err.println("发送系统消息失败: " + e.getMessage());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offshelfQuestion(Long questionId, String reason, Long adminId) {
        // 1. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(questionId);
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 2. 验证状态
        if ("ADMIN_OFFSHELF".equals(question.getStatus())) {
            throw new BusinessException("问题已下架");
        }
        
        // 3. 更新状态
        question.setStatus("ADMIN_OFFSHELF");
        question.setOffshelfReason(reason);
        question.setOffshelfTime(LocalDateTime.now());
        question.setOffshelfAdminId(adminId);
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
        
        // 4. 发送系统消息通知发布者
        try {
            systemMessageService.sendMessage(
                question.getUserId(),
                "QUESTION_OFFSHELF",
                "您的问题已被下架",
                "您的问题《" + question.getTitle() + "》已被管理员下架，原因：" + reason,
                "STUDY_QUESTION",
                questionId
            );
        } catch (Exception e) {
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnswer(Long answerId, String reason, Long adminId) {
        // 1. 查询回答
        StudyAnswer answer = studyAnswerMapper.selectById(answerId);
        if (answer == null || answer.getDeleteFlag() == 1) {
            throw new BusinessException("回答不存在");
        }
        
        // 2. 查询问题
        StudyQuestion question = studyQuestionMapper.selectById(answer.getQuestionId());
        if (question == null || question.getDeleteFlag() == 1) {
            throw new BusinessException("问题不存在");
        }
        
        // 3. 逻辑删除回答
        answer.setDeleteFlag(1);
        answer.setUpdateTime(LocalDateTime.now());
        studyAnswerMapper.updateById(answer);
        
        // 4. 如果回答已被采纳，需要更新问题的采纳状态
        if (answer.getIsAccepted() != null && answer.getIsAccepted() == 1) {
            question.setAcceptedAnswerId(null);
            question.setAcceptTime(null);
            // 如果问题状态是已解决，可能需要更新状态
            if ("SOLVED".equals(question.getStatus())) {
                question.setStatus("ANSWERED");
            }
        }
        
        // 5. 更新问题的回答数量（减少1）
        if (question.getAnswerCount() != null && question.getAnswerCount() > 0) {
            question.setAnswerCount(question.getAnswerCount() - 1);
        }
        question.setUpdateTime(LocalDateTime.now());
        studyQuestionMapper.updateById(question);
        
        // 6. 获取回答内容的前50个字符用于消息提示
        String answerPreview = answer.getContent();
        if (answerPreview != null && answerPreview.length() > 50) {
            answerPreview = answerPreview.substring(0, 50) + "...";
        }
        
        // 7. 发送系统消息通知回答者
        try {
            String messageContent = "您的回答" + (answerPreview != null && !answerPreview.isEmpty() ? answerPreview : "") + " 已被管理员删除。原因：" + reason;
            systemMessageService.sendMessage(
                answer.getUserId(),
                "ANSWER_DELETED",
                "您的回答已被删除",
                messageContent,
                "STUDY_ANSWER",
                answerId
            );
        } catch (Exception e) {
            System.err.println("发送系统消息失败: " + e.getMessage());
        }
        
        // 8. 发送邮件通知回答者
        try {
            User answerUser = userMapper.selectById(answer.getUserId());
            if (answerUser != null && answerUser.getEmail() != null) {
                emailService.sendEmail(
                    answerUser.getEmail(),
                    "您的回答已被删除",
                    "尊敬的用户，\n\n" +
                    "您的回答" + (answerPreview != null ? "《" + answerPreview + "》" : "") + "已被管理员删除。\n\n" +
                    "删除原因：" + reason + "\n\n" +
                    "如有疑问，请联系管理员。\n\n" +
                    "校园帮系统"
                );
            }
        } catch (Exception e) {
            System.err.println("发送邮件失败: " + e.getMessage());
        }
    }
}

