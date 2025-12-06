package com.server.campushelpserver.service.impl;

import com.server.campushelpserver.service.PublishFrequencyService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 发布频率检测服务实现类
 */
@Service
public class PublishFrequencyServiceImpl implements PublishFrequencyService {
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 检查发布频率
     * @param userId 用户ID
     * @param contentType 内容类型：LOST_FOUND、GOODS、STUDY_QUESTION
     * @return 是否通过频率检测
     */
    @Override
    public boolean checkFrequency(Long userId, String contentType) {
        // 限制：同一用户1小时内最多发布3条同类型内容
        String key = "publish:frequency:" + contentType + ":" + userId;
        String count = stringRedisTemplate.opsForValue().get(key);
        int countInt = count == null ? 0 : Integer.parseInt(count);
        
        if (countInt >= 3) {
            return false;
        }
        
        // 增加计数
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, 1, TimeUnit.HOURS);
        
        return true;
    }
}

