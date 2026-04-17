package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Product;
import com.scm.mapper.ProductMapper;
import com.scm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    
    @Override
    public IPage<Product> getProductList(Page<Product> page, String name, Integer categoryId, Boolean showAllStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 添加property=3的过滤条件
        wrapper.apply("property = 3");
        // 添加forbidden=1的过滤条件
        wrapper.eq(Product::getForbidden, 1);
        // 根据showAllStatus参数决定是否过滤status
        if (showAllStatus == null || !showAllStatus) {
            // 默认只显示上架商品
            wrapper.eq(Product::getStatus, 1);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        // 添加分类筛选条件
        if (categoryId != null) {
            // 获取所有分类
            List<String> categories = this.getProductCategories();
            // 检查categoryId是否有效
            if (categoryId > 0 && categoryId <= categories.size()) {
                // 从分类列表中获取对应的分类名称
                String categoryName = categories.get(categoryId - 1);
                wrapper.eq(Product::getCategory, categoryName);
            }
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return this.page(page, wrapper);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        this.updateById(product);
    }
    
    @Override
    public Boolean updateStock(Long id, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setStock(stock);
        return this.updateById(product);
    }
    
    @Override
    public String uploadImage(MultipartFile file) {
        // TODO: 实现文件上传逻辑
        return "default.jpg";
    }
    
    @Override
    public Long getIncrease(LocalDateTime since) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getProperty, 3)
                .ge(Product::getCreateTime, since));
    }
    
    @Override
    public List<Map<String, Object>> getTopProduct(Integer limit) {
        // TODO: 实现获取热门商品逻辑
        return baseMapper.selectTopProduct(limit);
    }
    
    @Override
    public List<Map<String, Object>> getCategoryDistribution() {
        return baseMapper.selectCategoryDistribution();
    }
    
    @Override
    public List<String> getProductCategories() {
        return baseMapper.selectProductCategories();
    }
}