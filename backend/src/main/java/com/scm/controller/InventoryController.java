package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Product;
import com.scm.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存管理控制器
 */
@RestController
@RequestMapping("/inventory")
@Slf4j
public class InventoryController {

    @Autowired
    private ProductService productService;

    /**
     * 获取库存列表（分页）
     */
    @GetMapping("/list")
    public Result<IPage<Product>> getInventoryList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer property,
            @RequestParam(required = false) Integer forbidden) {
        
        Page<Product> page = new Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword).or().like(Product::getCode, keyword);
        }
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        
        // 库存分类筛选
        if (property != null) {
            wrapper.eq(Product::getProperty, property);
        }
        
        // 状态筛选
        if (forbidden != null) {
            wrapper.eq(Product::getForbidden, forbidden);
        }
        
        // 低库存筛选
        if (Boolean.TRUE.equals(lowStock)) {
            wrapper.lt(Product::getStock, 50);
        }
        
        wrapper.orderByAsc(Product::getStock);
        return Result.success(productService.page(page, wrapper));
    }

    /**
     * 调整库存
     */
    @PutMapping("/adjust/{id}")
    public Result<Boolean> adjustStock(
            @PathVariable Long id,
            @RequestParam Integer adjustAmount,
            @RequestParam(required = false) String remark) {
        
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        
        int newStock = product.getStock() + adjustAmount;
        if (newStock < 0) {
            return Result.error("调整后库存不能为负数");
        }
        
        boolean result = productService.updateStock(id, newStock);
        
        if (result) {
            String adjustType = adjustAmount > 0 ? "入库" : adjustAmount < 0 ? "出库" : "调整";
            log.info("库存调整: 商品ID={}, 商品编码={}, 商品名称={}, 调整前库存={}, 调整数量={}, 调整后库存={}, 调整类型={}, 备注={}", 
                    id, product.getCode(), product.getName(), product.getStock(), adjustAmount, newStock, adjustType, remark);
        }
        
        return Result.success(result);
    }

    /**
     * 获取低库存预警商品
     */
    @GetMapping("/low-stock")
    public Result<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "50") Integer threshold) {
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.lt(Product::getStock, threshold)
               .orderByAsc(Product::getStock);
        
        List<Product> products = productService.list(wrapper);
        return Result.success(products);
    }

    /**
     * 获取库存统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getInventoryStatistics() {
        
        List<Product> allProducts = productService.list();
        
        // 计算总库存价值，处理price可能为null的情况
        BigDecimal totalValue = allProducts.stream()
                .map(p -> {
                    BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
                    return price.multiply(new BigDecimal(p.getStock()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算总库存数量
        int totalStock = allProducts.stream()
                .mapToInt(Product::getStock)
                .sum();
        
        // 计算低库存物品数量
        int lowStockCount = (int) allProducts.stream()
                .filter(p -> p.getStock() < 50)
                .count();
        
        // 计算零库存物品数量
        int zeroStockCount = (int) allProducts.stream()
                .filter(p -> p.getStock() == 0)
                .count();
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalProducts", allProducts.size());
        statistics.put("totalStock", totalStock);
        statistics.put("totalValue", totalValue);
        statistics.put("lowStockCount", lowStockCount);
        statistics.put("zeroStockCount", zeroStockCount);
        
        return Result.success(statistics);
    }

    /**
     * 批量调整库存
     */
    @PutMapping("/batch-adjust")
    @Transactional
    public Result<Map<String, Object>> batchAdjustStock(
            @RequestBody List<Map<String, Object>> adjustments) {
        
        log.info("Received batch adjust request: {}", adjustments);
        int successCount = 0;
        int failCount = 0;
        StringBuilder failDetails = new StringBuilder();
        
        for (Map<String, Object> adjustment : adjustments) {
            log.info("Processing adjustment: {}", adjustment);
            try {
                Long id = Long.valueOf(adjustment.get("id").toString());
                Integer adjustAmount = Integer.valueOf(adjustment.get("adjustAmount").toString());
                String remark = adjustment.get("remark") != null ? adjustment.get("remark").toString() : "";
                
                Product product = productService.getById(id);
                if (product == null) {
                    failCount++;
                    failDetails.append("商品ID: " + id + " 不存在; ");
                    continue;
                }
                
                // 更新库存
                int newStock = product.getStock() + adjustAmount;
                if (newStock < 0) {
                    failCount++;
                    failDetails.append("商品: " + product.getName() + " 调整后库存不能为负数; ");
                    continue;
                }
                product.setStock(newStock);
                
                // 更新其他属性
                Object codeValue = adjustment.get("code");
                if (codeValue != null) {
                    product.setCode(codeValue.toString());
                }
                Object nameValue = adjustment.get("name");
                if (nameValue != null) {
                    product.setName(nameValue.toString());
                }
                Object categoryValue = adjustment.get("category");
                if (categoryValue != null) {
                    product.setCategory(categoryValue.toString());
                }
                
                Object propertyValue = adjustment.get("property");
                if (propertyValue != null) {
                    product.setProperty(Integer.valueOf(propertyValue.toString()));
                }
                
                Object priceValue = adjustment.get("price");
                if (priceValue != null) {
                    product.setPrice(new BigDecimal(priceValue.toString()));
                }
                
                Object statusValue = adjustment.get("status");
                if (statusValue != null) {
                    product.setStatus(Integer.valueOf(statusValue.toString()));
                }
                
                Object descriptionValue = adjustment.get("description");
                if (descriptionValue != null) {
                    product.setDescription(descriptionValue.toString());
                }
                
                Object libraryCodingValue = adjustment.get("library_coding");
                if (libraryCodingValue != null) {
                    product.setLibraryCoding(libraryCodingValue.toString());
                }
                
                Object forbiddenValue = adjustment.get("forbidden");
                if (forbiddenValue != null) {
                    product.setForbidden(Integer.valueOf(forbiddenValue.toString()));
                }
                
                // 处理商品图片
                Object imageValue = adjustment.get("image");
                if (imageValue != null) {
                    product.setImage(imageValue.toString());
                }
                
                boolean result = productService.updateById(product);
                log.info("批量库存调整更新结果: 商品ID={}, 是否成功={}", id, result);
                if (result) {
                    successCount++;
                    String adjustType = adjustAmount > 0 ? "入库" : adjustAmount < 0 ? "出库" : "调整";
                    log.info("批量库存调整: 商品ID={}, 商品编码={}, 商品名称={}, 调整前库存={}, 调整数量={}, 调整后库存={}, 调整类型={}, 备注={}", 
                            id, product.getCode(), product.getName(), product.getStock() - adjustAmount, adjustAmount, newStock, adjustType, remark);
                } else {
                    failCount++;
                    failDetails.append("商品: " + product.getName() + " 调整失败; ");
                }
            } catch (Exception e) {
                failCount++;
                failDetails.append("数据格式错误: " + e.getMessage() + "; ");
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", adjustments.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails.toString());
        
        return Result.success(result);
    }
    
    /**
     * 更新商品禁用状态
     */
    @PutMapping("/product/forbidden")
    public Result<Boolean> updateProductForbiddenStatus(
            @RequestParam Long productId,
            @RequestParam Integer forbidden) {
        Product product = new Product();
        product.setId(productId);
        product.setForbidden(forbidden);
        boolean result = productService.updateById(product);
        log.info("更新商品禁用状态: 商品ID={}, 状态={}, 结果={}", productId, forbidden, result);
        return Result.success(result);
    }
    
    /**
     * 获取库存调整日志
     */
    @GetMapping("/logs")
    public Result<String> getInventoryLogs() {
        try {
            // 日志文件路径
            String logFilePath = "logs/inventory-adjust.log";
            File logFile = new File(logFilePath);
            
            if (!logFile.exists()) {
                return Result.success("");
            }
            
            // 读取日志文件内容
            List<String> lines = Files.readAllLines(logFile.toPath(), StandardCharsets.UTF_8);
            // 反转列表，最新的日志显示在最前面
            Collections.reverse(lines);
            // 拼接成字符串
            String logContent = String.join("\n", lines);
            
            return Result.success(logContent);
        } catch (IOException e) {
            log.error("读取库存日志失败: {}", e.getMessage());
            return Result.error("读取库存日志失败: " + e.getMessage());
        }
    }
}