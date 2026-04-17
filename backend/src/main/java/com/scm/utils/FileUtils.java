package com.scm.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtils {
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.prefix}")
    private String uploadPrefix;
    
    @Value("${upload.allowed-types}")
    private String[] allowedTypes;
    
    @Value("${upload.max-size}")
    private long maxSize;
    
    public String upload(MultipartFile file) throws IOException {
        // 检查文件类型
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new RuntimeException("不支持的文件类型");
        }
        
        // 检查文件大小
        if (file.getSize() > maxSize * 1024 * 1024) {
            throw new RuntimeException("文件大小超过限制");
        }
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 保存文件
        File destFile = new File(uploadPath + filename);
        file.transferTo(destFile);
        
        // 返回访问URL
        return uploadPrefix + filename;
    }
} 