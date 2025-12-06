package com.server.campushelpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园帮系统后端启动类
 */
@SpringBootApplication
public class CampusHelpServerApplication {

    /**
     * 应用程序入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CampusHelpServerApplication.class, args);
    }

}
