package com.server.campushelpserver.entity.dto;

import com.server.campushelpserver.entity.StudyQuestion;
import com.server.campushelpserver.entity.StudyAnswer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 问题详情VO
 */
@Data
@Schema(description = "问题详情VO")
public class QuestionDetailVO {
    
    @Schema(description = "问题信息")
    private StudyQuestion question;
    
    @Schema(description = "回答列表")
    private List<StudyAnswer> answers;
}

