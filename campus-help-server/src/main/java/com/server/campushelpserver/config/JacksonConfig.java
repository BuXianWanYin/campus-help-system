package com.server.campushelpserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 配置类
 * 用于配置 LocalDateTime 等 Java 8 时间类型的序列化和反序列化
 * 支持多种日期格式：
 * 1. ISO 8601 格式（如：2025-12-03T04:18:00.000Z、2025-12-03T04:18:00）
 * 2. 自定义格式（如：2025-12-03 04:18:00）
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期时间格式化器（自定义格式）
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 自定义 LocalDateTime 反序列化器
     * 支持 ISO 8601 格式和自定义格式
     */
    private static class FlexibleLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
        
        public FlexibleLocalDateTimeDeserializer() {
            super(DATE_TIME_FORMATTER);
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String dateString = parser.getText();
            if (dateString == null || dateString.trim().isEmpty()) {
                return null;
            }
            
            dateString = dateString.trim();
            
            try {
                // 先尝试 ISO 8601 格式（如：2025-12-03T04:18:00.000Z 或 2025-12-03T04:18:00）
                if (dateString.contains("T")) {
                    // 处理带时区的ISO格式（如：2025-12-03T04:18:00.000Z）
                    if (dateString.endsWith("Z") || dateString.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                        try {
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString);
                            return zonedDateTime.toLocalDateTime();
                        } catch (DateTimeParseException e) {
                            // 如果解析失败，尝试移除时区信息
                            String withoutTz = dateString.replaceAll("Z$|[+-]\\d{2}:?\\d{2}$", "");
                            if (withoutTz.contains("T")) {
                                // 将T替换为空格，然后使用自定义格式解析
                                withoutTz = withoutTz.replace("T", " ");
                                return LocalDateTime.parse(withoutTz, DATE_TIME_FORMATTER);
                            }
                        }
                    } else {
                        // 处理不带时区的ISO格式（如：2025-12-03T04:18:00 或 2025-12-03T04:18:00.000）
                        String withoutTz = dateString.split("[+-]")[0].replace("Z", "");
                        // 移除毫秒部分（如果有）
                        if (withoutTz.contains(".")) {
                            withoutTz = withoutTz.substring(0, withoutTz.indexOf("."));
                        }
                        // 将T替换为空格，然后使用自定义格式解析
                        String customFormat = withoutTz.replace("T", " ");
                        return LocalDateTime.parse(customFormat, DATE_TIME_FORMATTER);
                    }
                } else {
                    // 尝试自定义格式（如：2025-12-03 04:18:00）
                    return LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
                }
            } catch (DateTimeParseException e) {
                // 如果所有格式都失败，抛出异常
                throw new IOException("无法解析日期时间: " + dateString + "，支持的格式：ISO 8601 或 yyyy-MM-dd HH:mm:ss", e);
            }
            
            // 如果上面的逻辑都没有返回，抛出异常
            throw new IOException("无法解析日期时间: " + dateString);
        }
    }

    /**
     * 配置 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 配置 LocalDateTime 的序列化和反序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());

        return builder
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
