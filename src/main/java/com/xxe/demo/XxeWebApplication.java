package com.xxe.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * XXE漏洞演示Web应用程序主类
 * 提供文件上传和POC生成功能
 */
@SpringBootApplication
public class XxeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxeWebApplication.class, args);
    }

    // 配置文件上传大小限制
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置单个文件的最大大小
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        // 设置请求的最大大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }

}
