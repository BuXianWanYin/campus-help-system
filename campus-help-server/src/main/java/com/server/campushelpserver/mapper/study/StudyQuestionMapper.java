package com.server.campushelpserver.mapper.study;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.study.StudyQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 学习问题Mapper接口
 */
@Mapper
public interface StudyQuestionMapper extends BaseMapper<StudyQuestion> {
    
    /**
     * 增加浏览次数
     * @param id 问题ID
     */
    @Update("UPDATE study_question SET view_count = view_count + 1, update_time = NOW() WHERE id = #{id} AND delete_flag = 0")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * 增加回答数量
     * @param id 问题ID
     */
    @Update("UPDATE study_question SET answer_count = answer_count + 1, update_time = NOW() WHERE id = #{id} AND delete_flag = 0")
    void incrementAnswerCount(@Param("id") Long id);
}

