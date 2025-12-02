package com.server.campushelpserver.util;

import lombok.Data;

/**
 * 敏感词检测结果
 */
@Data
public class SensitiveWordCheckResult {
    
    /**
     * 是否通过
     */
    private boolean pass;
    
    /**
     * 是否需要人工审核
     */
    private boolean needManualReview;
    
    /**
     * 检测结果消息
     */
    private String message;
    
    /**
     * 通过的检测结果
     */
    public static SensitiveWordCheckResult pass() {
        SensitiveWordCheckResult result = new SensitiveWordCheckResult();
        result.pass = true;
        result.needManualReview = false;
        result.message = "通过检测";
        return result;
    }
    
    /**
     * 需要人工审核
     */
    public static SensitiveWordCheckResult needManualReview(String message) {
        SensitiveWordCheckResult result = new SensitiveWordCheckResult();
        result.pass = false;
        result.needManualReview = true;
        result.message = message;
        return result;
    }
    
    /**
     * 拒绝（严重敏感词）
     */
    public static SensitiveWordCheckResult reject(String message) {
        SensitiveWordCheckResult result = new SensitiveWordCheckResult();
        result.pass = false;
        result.needManualReview = false;
        result.message = message;
        return result;
    }
}

