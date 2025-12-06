package com.server.campushelpserver.service.study;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.study.StudyQuestion;
import com.server.campushelpserver.entity.study.StudyAnswer;
import com.server.campushelpserver.entity.study.dto.QuestionDTO;
import com.server.campushelpserver.entity.study.dto.AnswerDTO;
import com.server.campushelpserver.entity.study.dto.QuestionSearchDTO;
import com.server.campushelpserver.entity.study.dto.QuestionDetailVO;

/**
 * 学习问题服务接口
 */
public interface QuestionService extends IService<StudyQuestion> {
    
    /**
     * 发布问题
     * @param dto 问题信息
     * @param userId 用户ID
     * @return 问题ID
     */
    Long publishQuestion(QuestionDTO dto, Long userId);
    
    /**
     * 搜索问题列表
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<StudyQuestion> getQuestionList(QuestionSearchDTO searchDTO);
    
    /**
     * 获取问题详情
     * @param questionId 问题ID
     * @param userId 当前用户ID（可为null）
     * @return 问题详情（包含回答列表）
     */
    QuestionDetailVO getQuestionDetail(Long questionId, Long userId);
    
    /**
     * 回答问题
     * @param questionId 问题ID
     * @param dto 回答信息
     * @param userId 用户ID
     * @return 回答ID
     */
    Long answerQuestion(Long questionId, AnswerDTO dto, Long userId);
    
    /**
     * 采纳答案
     * @param questionId 问题ID
     * @param answerId 回答ID
     * @param userId 用户ID（发布者）
     */
    void acceptAnswer(Long questionId, Long answerId, Long userId);
    
    /**
     * 取消问题
     * @param questionId 问题ID
     * @param userId 用户ID（发布者）
     */
    void cancelQuestion(Long questionId, Long userId);
    
    /**
     * 更新问题
     * @param questionId 问题ID
     * @param dto 问题信息
     * @param userId 用户ID（发布者）
     */
    void updateQuestion(Long questionId, QuestionDTO dto, Long userId);
    
    /**
     * 获取我发布的问题列表
     * @param userId 用户ID
     * @param status 问题状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<StudyQuestion> getMyPublishedQuestions(Long userId, String status, Integer pageNum, Integer pageSize);
    
    /**
     * 获取我回答的问题列表
     * @param userId 用户ID
     * @param status 问题状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<StudyQuestion> getMyAnsweredQuestions(Long userId, String status, Integer pageNum, Integer pageSize);
    
    /**
     * 管理员审核问题
     * @param questionId 问题ID
     * @param approved 审核结果：true-通过，false-拒绝
     * @param reason 拒绝原因（拒绝时必填）
     * @param adminId 管理员ID
     */
    void auditQuestion(Long questionId, Boolean approved, String reason, Long adminId);
    
    /**
     * 获取待审核问题列表（管理员）
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    Page<StudyQuestion> getPendingAuditList(QuestionSearchDTO searchDTO);
    
    /**
     * 管理员下架问题
     * @param questionId 问题ID
     * @param reason 下架原因
     * @param adminId 管理员ID
     */
    void offshelfQuestion(Long questionId, String reason, Long adminId);
    
    /**
     * 获取问题下的所有回答列表（管理员）
     * @param questionId 问题ID
     * @return 回答列表
     */
    java.util.List<StudyAnswer> getAnswersByQuestionId(Long questionId);
    
    /**
     * 管理员审核回答
     * @param answerId 回答ID
     * @param approved 审核结果：true-通过，false-拒绝
     * @param reason 拒绝原因（拒绝时必填）
     * @param adminId 管理员ID
     */
    void auditAnswer(Long answerId, Boolean approved, String reason, Long adminId);
    
    /**
     * 管理员删除回答
     * @param answerId 回答ID
     * @param reason 删除原因
     * @param adminId 管理员ID
     */
    void deleteAnswer(Long answerId, String reason, Long adminId);
    
}

