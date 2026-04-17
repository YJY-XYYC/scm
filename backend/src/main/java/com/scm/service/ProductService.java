package com.scm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Product;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    IPage<Product> getProductList(Page<Product> page, String name, Integer categoryId, Boolean showAllStatus);
    void updateStatus(Long id, Integer status);
    Boolean updateStock(Long id, Integer stock);
    String uploadImage(MultipartFile file);
    Long getIncrease(LocalDateTime since);
    List<Map<String, Object>> getTopProduct(Integer limit);
    List<Map<String, Object>> getCategoryDistribution();
    List<String> getProductCategories();
} 