package com.server.campushelpserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索历史实体类
 */
@Data
@TableName("search_history")
@Schema(description = "搜索历史实体")
public class SearchHistory implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "搜索历史ID")
    private Long id;
    
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    
    @TableField("keyword")
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @TableField("module_type")
    @Schema(description = "模块类型：LOST_FOUND-失物招领，GOODS-闲置交易，TASK-跑腿服务")
    private String moduleType;
    
    @TableField("search_time")
    @Schema(description = "搜索时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime searchTime;
    
    @TableField("delete_flag")
    @Schema(description = "逻辑删除标志：0-未删除，1-已删除")
    private Integer deleteFlag;
    
    @TableField("create_time")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

