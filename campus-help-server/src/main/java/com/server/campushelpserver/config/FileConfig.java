package com.server.campushelpserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 文件上传配置类
 */
@Configuration
public class FileConfig implements WebMvcConfigurer {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.access-path}")
    private String accessPath;

    /**
     * 配置静态资源访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取并规范化上传路径
        File uploadDir = new File(uploadPath);
        if (!uploadDir.isAbsolute()) {
            // 如果是相对路径，使用项目根目录
            String projectRoot = System.getProperty("user.dir");
            uploadDir = new File(projectRoot, uploadPath);
        }
        
        // 创建上传目录
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建上传目录：" + uploadDir.getAbsolutePath());
            }
        }
        
        // 获取绝对路径
        String absolutePath = uploadDir.getAbsolutePath();
        // 确保路径以分隔符结尾
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        // 配置静态资源映射
        registry.addResourceHandler(accessPath)
                .addResourceLocations("file:" + absolutePath);
    }

    /**
     * 获取上传路径
     */
    public String getUploadPath() {
        return uploadPath;
    }
}

