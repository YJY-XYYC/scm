package com.scm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件上传配置类
 */
@Configuration
@ConfigurationProperties(prefix = "upload")
@Data
public class FileUploadConfig {
    
    private String path; // 上传路径
    private String prefix; // 访问路径前缀
    private String allowedTypes; // 允许的文件类型
    private Integer maxSize; // 最大文件大小（MB）
    
    /**
     * 获取允许的文件类型集合
     */
    public Set<String> getAllowedTypesSet() {
        return new HashSet<>(Arrays.asList(allowedTypes.split(",")));
    }
    
    /**
     * 获取当天的上传子目录
     */
    public String getCurrentDayPath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return LocalDate.now().format(formatter);
    }
    
    /**
     * 获取完整的文件存储目录
     */
    public String getFullUploadPath() {
        String currentDayPath = getCurrentDayPath();
        String fullPath = path + currentDayPath;
        
        // 确保目录存在
        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        return fullPath;
    }
    
    /**
     * 获取访问URL路径
     */
    public String getAccessUrl(String filename) {
        return prefix + getCurrentDayPath() + "/" + filename;
    }
    
    /**
     * 获取上传路径
     */
    public String getPath() {
        return path;
    }
    
    /**
     * 获取访问路径前缀
     */
    public String getPrefix() {
        return prefix;
    }
    
    /**
     * 获取最大文件大小
     */
    public Integer getMaxSize() {
        return maxSize;
    }
}