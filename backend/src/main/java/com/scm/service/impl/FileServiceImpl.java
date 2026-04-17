package com.scm.service.impl;

import com.scm.config.FileUploadConfig;
import com.scm.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    @Override
    public String uploadFile(MultipartFile file) {
        // 默认不使用子目录，保持原有行为
        return uploadFile(file, null);
    }
    
    @Override
    public String uploadFile(MultipartFile file, String subDir) {
        // 验证文件是否为空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !fileUploadConfig.getAllowedTypesSet().contains(contentType)) {
            throw new RuntimeException("不支持的文件类型：" + contentType);
        }
        
        // 验证文件大小
        long fileSize = file.getSize();
        long maxSizeBytes = fileUploadConfig.getMaxSize() * 1024 * 1024; // 转换为字节
        if (fileSize > maxSizeBytes) {
            throw new RuntimeException("文件大小超过限制，最大允许：" + fileUploadConfig.getMaxSize() + "MB");
        }
        
        try {
            // 生成文件名（使用UUID避免文件名冲突）
            String originalFilename = file.getOriginalFilename();
            String fileExtension = StringUtils.getFilenameExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // 获取基础上传路径
            String basePath = fileUploadConfig.getPath();
            
            // 获取日期路径
            String currentDayPath = fileUploadConfig.getCurrentDayPath();
            
            // 构建完整的文件存储路径
            StringBuilder fullPathBuilder = new StringBuilder(basePath);
            if (subDir != null && !subDir.isEmpty()) {
                fullPathBuilder.append(subDir).append("/");
            }
            fullPathBuilder.append(currentDayPath);
            
            String fullPath = fullPathBuilder.toString();
            
            // 确保目录存在
            File directory = new File(fullPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            String filePath = fullPath + "/" + filename;
            
            // 保存文件
            File dest = new File(filePath);
            file.transferTo(dest);
            
            // 构建访问URL
            StringBuilder accessUrlBuilder = new StringBuilder(fileUploadConfig.getPrefix());
            if (subDir != null && !subDir.isEmpty()) {
                accessUrlBuilder.append(subDir).append("/");
            }
            accessUrlBuilder.append(currentDayPath).append("/").append(filename);
            
            String accessUrl = accessUrlBuilder.toString();
            log.info("文件上传成功：{}，访问路径：{}", filename, accessUrl);
            return accessUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }
    
    @Override
    public boolean deleteFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }
        
        try {
            // 从访问路径中提取实际文件路径
            // 移除访问路径前缀
            String prefix = fileUploadConfig.getPrefix();
            String relativePath = filePath;
            if (filePath.startsWith(prefix)) {
                relativePath = filePath.substring(prefix.length());
            }
            
            // 构建完整的文件路径
            String fullPath = fileUploadConfig.getPath() + relativePath;
            
            File file = new File(fullPath);
            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                log.info("文件删除{}" + (deleted ? "成功：" : "失败：") + filePath);
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("文件删除失败：" + filePath, e);
            return false;
        }
    }
}