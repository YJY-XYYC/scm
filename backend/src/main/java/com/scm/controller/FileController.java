package com.scm.controller;

import com.scm.common.Result;
import com.scm.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 处理文件上传请求
     * 用于商品图片等文件的上传
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        try {
            // 调用文件服务进行上传，使用"product"作为子目录
            String fileUrl = fileService.uploadFile(file, "product");
            log.info("文件上传成功，访问路径：{}", fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }
}