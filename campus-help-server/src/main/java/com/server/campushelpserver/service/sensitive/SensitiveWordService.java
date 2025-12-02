package com.server.campushelpserver.service.sensitive;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.campushelpserver.entity.sensitive.SensitiveWord;
import com.server.campushelpserver.util.SensitiveWordCheckResult;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService extends IService<SensitiveWord> {
    
    /**
     * 检测文本中的敏感词
     * @param text 待检测文本
     * @return 检测结果
     */
    SensitiveWordCheckResult check(String text);
    
    /**
     * 重新加载敏感词库
     */
    void reloadSensitiveWords();
}

