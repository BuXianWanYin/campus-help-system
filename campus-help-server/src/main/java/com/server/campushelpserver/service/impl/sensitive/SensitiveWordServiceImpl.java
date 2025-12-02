package com.server.campushelpserver.service.impl.sensitive;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.sensitive.SensitiveWord;
import com.server.campushelpserver.mapper.sensitive.SensitiveWordMapper;
import com.server.campushelpserver.service.sensitive.SensitiveWordService;
import com.server.campushelpserver.util.AhoCorasick;
import com.server.campushelpserver.util.SensitiveWordCheckResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现类
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {
    
    private AhoCorasick ahoCorasick = new AhoCorasick();
    
    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        reloadSensitiveWords();
    }
    
    @Override
    public SensitiveWordCheckResult check(String text) {
        if (text == null || text.isEmpty()) {
            return SensitiveWordCheckResult.pass();
        }
        
        // 检测敏感词
        List<String> foundWords = ahoCorasick.search(text);
        
        if (foundWords.isEmpty()) {
            return SensitiveWordCheckResult.pass();
        }
        
        // 检查敏感词严重程度
        for (String word : foundWords) {
            SensitiveWord sw = baseMapper.selectByWord(word);
            if (sw != null && "SEVERE".equals(sw.getLevel())) {
                return SensitiveWordCheckResult.reject("包含严重敏感词：" + word);
            }
        }
        
        return SensitiveWordCheckResult.needManualReview("包含敏感词：" + String.join("、", foundWords));
    }
    
    @Override
    public void reloadSensitiveWords() {
        try {
            // 从数据库加载敏感词
            List<SensitiveWord> words = this.list();
            List<String> wordList = words.stream()
                    .map(SensitiveWord::getWord)
                    .collect(Collectors.toList());
            
            // 构建AC自动机
            ahoCorasick = new AhoCorasick();
            ahoCorasick.build(wordList);
            
            log.info("敏感词库加载完成，共 {} 个敏感词", words.size());
        } catch (Exception e) {
            log.error("加载敏感词库失败", e);
        }
    }
}

