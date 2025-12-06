package com.server.campushelpserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.entity.SensitiveWord;
import com.server.campushelpserver.entity.dto.SensitiveWordDTO;
import com.server.campushelpserver.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词管理控制器
 */
@RestController
@RequestMapping("/api/admin/sensitive-word")
@Tag(name = "敏感词管理", description = "敏感词管理相关接口")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class SensitiveWordController {
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Operation(summary = "获取敏感词列表", description = "分页查询敏感词列表，支持关键词搜索")
    @GetMapping("/list")
    public Result<Page<SensitiveWord>> getSensitiveWordList(
            @Parameter(description = "分页参数") Page<SensitiveWord> page,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getDeleteFlag, 0);
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(SensitiveWord::getWord, keyword.trim());
        }
        
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        Page<SensitiveWord> result = sensitiveWordService.page(page, wrapper);
        return Result.success("查询成功", result);
    }
    
    @Operation(summary = "获取所有敏感词", description = "获取所有未删除的敏感词（不分页）")
    @GetMapping("/all")
    public Result<List<SensitiveWord>> getAllSensitiveWords() {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getDeleteFlag, 0);
        wrapper.orderByDesc(SensitiveWord::getCreateTime);
        
        List<SensitiveWord> list = sensitiveWordService.list(wrapper);
        return Result.success("查询成功", list);
    }
    
    @Operation(summary = "添加敏感词", description = "添加新的敏感词，支持批量添加（多个敏感词用逗号分隔）")
    @PostMapping("/add")
    public Result<Long> addSensitiveWord(
            @Parameter(description = "敏感词信息") @Validated @RequestBody SensitiveWordDTO dto) {
        String wordInput = dto.getWord().trim();
        if (wordInput.isEmpty()) {
            return Result.error("敏感词不能为空");
        }
        
        // 支持批量添加：用逗号分隔多个敏感词
        String[] words = wordInput.split("[,，]"); // 支持中文和英文逗号
        int successCount = 0;
        int skipCount = 0;
        Long lastId = null;
        
        for (String word : words) {
            word = word.trim();
            if (word.isEmpty()) {
                continue; // 跳过空词
            }
            
            // 检查敏感词是否已存在
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, word);
            wrapper.eq(SensitiveWord::getDeleteFlag, 0);
            SensitiveWord existing = sensitiveWordService.getOne(wrapper);
            if (existing != null) {
                skipCount++;
                continue; // 跳过已存在的敏感词
            }
            
            // 创建新的敏感词
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setLevel(dto.getLevel());
            sensitiveWord.setCreateTime(LocalDateTime.now());
            sensitiveWord.setUpdateTime(LocalDateTime.now());
            sensitiveWord.setDeleteFlag(0);
            
            sensitiveWordService.save(sensitiveWord);
            lastId = sensitiveWord.getId();
            successCount++;
        }
        
        if (successCount == 0) {
            if (skipCount > 0) {
                return Result.error("所有敏感词都已存在");
            }
            return Result.error("没有有效的敏感词");
        }
        
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        
        String message = String.format("添加成功：成功添加 %d 个敏感词", successCount);
        if (skipCount > 0) {
            message += String.format("，跳过 %d 个已存在的敏感词", skipCount);
        }
        
        return Result.success(message, lastId);
    }
    
    @Operation(summary = "更新敏感词", description = "更新敏感词信息")
    @PutMapping("/{id}")
    public Result<Void> updateSensitiveWord(
            @Parameter(description = "敏感词ID") @PathVariable Long id,
            @Parameter(description = "敏感词信息") @Validated @RequestBody SensitiveWordDTO dto) {
        SensitiveWord sensitiveWord = sensitiveWordService.getById(id);
        if (sensitiveWord == null || sensitiveWord.getDeleteFlag() == 1) {
            return Result.error("敏感词不存在");
        }
        
        // 如果修改了敏感词内容，检查是否与其他敏感词重复
        if (!sensitiveWord.getWord().equals(dto.getWord().trim())) {
            LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SensitiveWord::getWord, dto.getWord().trim());
            wrapper.eq(SensitiveWord::getDeleteFlag, 0);
            wrapper.ne(SensitiveWord::getId, id);
            SensitiveWord existing = sensitiveWordService.getOne(wrapper);
            if (existing != null) {
                return Result.error("敏感词已存在");
            }
        }
        
        sensitiveWord.setWord(dto.getWord().trim());
        sensitiveWord.setLevel(dto.getLevel());
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        
        sensitiveWordService.updateById(sensitiveWord);
        
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "删除敏感词", description = "删除敏感词（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSensitiveWord(
            @Parameter(description = "敏感词ID") @PathVariable Long id) {
        SensitiveWord sensitiveWord = sensitiveWordService.getById(id);
        if (sensitiveWord == null || sensitiveWord.getDeleteFlag() == 1) {
            return Result.error("敏感词不存在");
        }
        
        sensitiveWord.setDeleteFlag(1);
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        sensitiveWordService.updateById(sensitiveWord);
        
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "批量删除敏感词", description = "批量删除敏感词（逻辑删除）")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteSensitiveWords(
            @Parameter(description = "敏感词ID列表") @RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的敏感词");
        }
        
        for (Long id : ids) {
            SensitiveWord sensitiveWord = sensitiveWordService.getById(id);
            if (sensitiveWord != null && sensitiveWord.getDeleteFlag() == 0) {
                sensitiveWord.setDeleteFlag(1);
                sensitiveWord.setUpdateTime(LocalDateTime.now());
                sensitiveWordService.updateById(sensitiveWord);
            }
        }
        
        // 重新加载敏感词库
        sensitiveWordService.reloadSensitiveWords();
        
        return Result.success("批量删除成功", null);
    }
    
    @Operation(summary = "重新加载敏感词库", description = "手动触发重新加载敏感词库")
    @PostMapping("/reload")
    public Result<Void> reloadSensitiveWords() {
        sensitiveWordService.reloadSensitiveWords();
        return Result.success("重新加载成功", null);
    }
}

