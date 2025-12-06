package com.server.campushelpserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.campushelpserver.entity.SensitiveWord;
import com.server.campushelpserver.mapper.SensitiveWordMapper;
import com.server.campushelpserver.service.SensitiveWordService;
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
            // 从数据库加载未删除的敏感词（显式过滤已删除的记录）
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SensitiveWord> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getDeleteFlag, 0);
            List<SensitiveWord> words = this.list(wrapper);
            
            List<String> wordList = words.stream()
                    .map(SensitiveWord::getWord)
                    .filter(word -> word != null && !word.trim().isEmpty()) // 过滤空词
                    .collect(Collectors.toList());
            
            // 构建AC自动机
            ahoCorasick = new AhoCorasick();
            ahoCorasick.build(wordList);
            
            log.info("敏感词库重新加载完成，共 {} 个敏感词", wordList.size());
        } catch (Exception e) {
            log.error("重新加载敏感词库失败", e);
            throw new RuntimeException("重新加载敏感词库失败", e);
        }
    }
}

