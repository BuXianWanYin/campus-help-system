package com.server.campushelpserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.campushelpserver.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 私信消息Mapper接口
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 标记消息为已读
     * @param sessionId 会话ID
     * @param receiverId 接收者ID
     */
    @Update("UPDATE chat_message SET is_read = 1, update_time = NOW() " +
            "WHERE session_id = #{sessionId} AND receiver_id = #{receiverId} AND is_read = 0 AND delete_flag = 0")
    void markAsRead(@Param("sessionId") Long sessionId, @Param("receiverId") Long receiverId);
}

