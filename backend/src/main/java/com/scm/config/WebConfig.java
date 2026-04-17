package com.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于配置静态资源访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，允许通过URL访问上传的文件
        // 将 /upload/** 直接映射到文件系统的 D:/upload/ 目录
        // 这样可以正确访问带子目录和日期路径的文件
        registry.addResourceHandler(fileUploadConfig.getPrefix() + "**")
                .addResourceLocations("file:" + fileUploadConfig.getPath());
    }
}