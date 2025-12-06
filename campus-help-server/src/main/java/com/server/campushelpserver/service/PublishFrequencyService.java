package com.server.campushelpserver.service;

/**
 * 发布频率检测服务接口
 */
public interface PublishFrequencyService {
    
    /**
     * 检查发布频率
     * @param userId 用户ID
     * @param contentType 内容类型：LOST_FOUND、GOODS、STUDY_QUESTION
     * @return 是否通过频率检测
     */
    boolean checkFrequency(Long userId, String contentType);
}
