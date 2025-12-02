package com.server.campushelpserver.controller.common;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.config.FileConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@Tag(name = "文件管理", description = "文件上传相关接口")
public class FileController {
    
    @Autowired
    private FileConfig fileConfig;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};
    
    @Operation(summary = "上传图片", description = "上传图片文件，支持jpg、jpeg、png、gif格式，单张不超过5MB")
    @PostMapping("/upload")
    public Result<String> upload(
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "模块名称（如：avatar、lost-found、goods、task）") @RequestParam(required = false, defaultValue = "common") String module) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("图片大小超过限制，单张图片不超过5MB");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.error("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            return Result.error("图片格式不支持，仅支持jpg、jpeg、png、gif格式");
        }
        
        // 生成文件路径
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String relativePath = module + File.separator + dateDir + File.separator + fileName;
        
        // 获取上传路径并规范化
        String uploadPath = fileConfig.getUploadPath();
        // 处理相对路径，转换为绝对路径
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 如果是相对路径，使用项目根目录
            String projectRoot = System.getProperty("user.dir");
            uploadDir = new File(projectRoot, uploadPath);
        }
        
        // 确保上传根目录存在
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                return Result.error("无法创建上传目录：" + uploadDir.getAbsolutePath());
            }
        }
        
        // 构建完整文件路径
        File targetFile = new File(uploadDir, relativePath);
        File parentDir = targetFile.getParentFile();
        
        // 创建父目录
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                return Result.error("无法创建文件目录：" + parentDir.getAbsolutePath());
            }
        }
        
        // 保存文件
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage() + "，路径：" + targetFile.getAbsolutePath());
        }
        
        // 返回访问路径
        String accessPath = "/uploads/" + relativePath.replace(File.separator, "/");
        return Result.success("上传成功", accessPath);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 检查文件扩展名是否允许
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}

