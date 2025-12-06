package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
    
    /**
     * 根据敏感词查询
     * @param word 敏感词
     * @return 敏感词信息
     */
    @Select("SELECT * FROM sensitive_word WHERE word = #{word} AND delete_flag = 0 LIMIT 1")
    SensitiveWord selectByWord(String word);
}

