package com.scm.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     * @param file 上传的文件
     * @return 文件访问路径
     */
    String uploadFile(MultipartFile file);
    
    /**
     * 上传文件到指定子目录
     * @param file 上传的文件
     * @param subDir 子目录名称
     * @return 文件访问路径
     */
    String uploadFile(MultipartFile file, String subDir);
    
    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);
}