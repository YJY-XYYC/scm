package com.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scm.entity.ProductionPlan;
import com.scm.entity.Product;
import com.scm.entity.Material;
import com.scm.mapper.ProductionPlanMapper;
import com.scm.service.ProductionPlanService;
import com.scm.service.ProductService;
import com.scm.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductionPlanServiceImpl extends ServiceImpl<ProductionPlanMapper, ProductionPlan> implements ProductionPlanService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionPlanServiceImpl.class);

    @Autowired
    private ProductService productService;
    
    @Autowired
    private MaterialService materialService;
    
    @Override
    public Page<ProductionPlan> getProductionPlans(Map<String, Object> params) {
        // 获取分页参数并进行类型转换
        Integer page = params.get("page") != null ? Integer.valueOf(params.get("page").toString()) : 1;
        Integer pageSize = params.get("pageSize") != null ? Integer.valueOf(params.get("pageSize").toString()) : 10;
        
        // 创建查询条件
        QueryWrapper<ProductionPlan> queryWrapper = new QueryWrapper<>();
        
        // 计划名称或产物名称查询（使用OR逻辑）
        if ((params.containsKey("planName") && params.get("planName") != null && !params.get("planName").toString().isEmpty()) ||
            (params.containsKey("productName") && params.get("productName") != null && !params.get("productName").toString().isEmpty())) {
            
            // 获取搜索关键词
            String planNameKeyword = params.containsKey("planName") && params.get("planName") != null ? params.get("planName").toString() : "";
            String productNameKeyword = params.containsKey("productName") && params.get("productName") != null ? params.get("productName").toString() : "";
            
            // 使用OR条件组，只要计划名称或产物名称任一字段匹配即可
            queryWrapper.and(wrapper -> {
                // 如果计划名称关键词不为空，添加计划名称查询
                if (!planNameKeyword.isEmpty()) {
                    wrapper.like("plan_name", planNameKeyword);
                }
                
                // 如果产物名称关键词不为空，添加产物名称查询
                if (!productNameKeyword.isEmpty()) {
                    // 如果计划名称关键词也不为空，使用OR连接
                    if (!planNameKeyword.isEmpty()) {
                        wrapper.or();
                    }
                    wrapper.like("product_name", productNameKeyword);
                }
            });
        }
        
        // 状态查询
        if (params.containsKey("status") && params.get("status") != null && !params.get("status").toString().isEmpty()) {
            queryWrapper.eq("status", Integer.valueOf(params.get("status").toString()));
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc("create_time");
        
        // 分页查询
        return this.page(new Page<>(page, pageSize), queryWrapper);
    }
    
    @Override
    public boolean createProductionPlan(ProductionPlan productionPlan) {
        // 设置创建时间和更新时间
        Date now = new Date();
        productionPlan.setCreateTime(now);
        productionPlan.setUpdateTime(now);
        
        return this.save(productionPlan);
    }
    
    @Override
    @Transactional
    public boolean updateProductionPlan(ProductionPlan productionPlan) {
        // 获取旧的生产计划
        ProductionPlan oldPlan = baseMapper.selectById(productionPlan.getId());
        if (oldPlan != null) {
            // 检查状态是否从计划中(1)变为生产中(2)
            if (oldPlan.getStatus() == 1 && productionPlan.getStatus() == 2) {
                // 从库存中扣除所需材料
                deductMaterialsFromStock(oldPlan.getRequiredMaterials());
            } else if (oldPlan.getStatus() == 2 && productionPlan.getStatus() == 3) {
                // 将生产的产品存入库存
                // 获取生产的产物个数，默认为1
                Integer productQuantity = oldPlan.getProductQuantity() != null ? oldPlan.getProductQuantity() : 1;
                // 获取产物属性，默认为3（成品）
                Integer productProperty = oldPlan.getProductProperty() != null ? oldPlan.getProductProperty() : 3;
                addProductToStock(oldPlan.getProductName(), productQuantity, productProperty);
            }
        }
        
        // 设置更新时间
        productionPlan.setUpdateTime(new Date());
        
        return this.updateById(productionPlan);
    }
    
    @Override
    public boolean deleteProductionPlan(Long id) {
        return this.removeById(id);
    }
    
    @Override
    @Transactional
    public boolean updateProductionPlanStatus(Long id, Integer status) {
        ProductionPlan productionPlan = baseMapper.selectById(id);
        if (productionPlan != null) {
            productionPlan.setStatus(status);
            productionPlan.setUpdateTime(new Date());
            
            if (status == 2) { // 开始生产
                deductMaterialsFromStock(productionPlan.getRequiredMaterials());
            } else if (status == 3) { // 完成生产
                // 获取生产的产物个数，默认为1
                Integer productQuantity = productionPlan.getProductQuantity() != null ? productionPlan.getProductQuantity() : 1;
                // 获取产物属性，默认为3（成品）
                Integer productProperty = productionPlan.getProductProperty() != null ? productionPlan.getProductProperty() : 3;
                addProductToStock(productionPlan.getProductName(), productQuantity, productProperty);
            }
            
            return this.updateById(productionPlan);
        }
        return false;
    }
    
    /**
     * 从库存中扣除所需材料
     * @param requiredMaterials 所需材料字符串，格式：物料ID*数量/物料ID*数量
     */
    private void deductMaterialsFromStock(String requiredMaterials) {
        logger.info("开始扣除材料，requiredMaterials: {}", requiredMaterials);
        if (requiredMaterials != null && !requiredMaterials.isEmpty()) {
            String[] materials = requiredMaterials.split("/");
            for (String material : materials) {
                logger.info("处理材料: {}", material);
                String[] parts = material.split("\\*");
                if (parts.length == 2) {
                    try {
                        Long materialId = Long.parseLong(parts[0]);
                        Integer quantity = Integer.parseInt(parts[1]);
                        logger.info("扣除材料ID: {}, 数量: {}", materialId, quantity);
                        
                        // 根据物料ID查询物料信息
                        Material materialInfo = materialService.getById(materialId);
                        if (materialInfo != null) {
                            logger.info("找到物料: {}, 名称: {}", materialInfo.getId(), materialInfo.getMaterialName());
                            
                            // 根据物料名称查询对应的产品
                            Product product = productService.lambdaQuery()
                                    .eq(Product::getName, materialInfo.getMaterialName())
                                    .one();
                            
                            if (product != null) {
                                logger.info("找到对应的产品: {}, ID: {}", product.getName(), product.getId());
                                // 扣除库存（数量为负数）
                                adjustStock(product.getId(), -quantity, "生产计划消耗");
                            } else {
                                logger.warn("未找到对应的产品: {}", materialInfo.getMaterialName());
                            }
                        } else {
                            logger.warn("未找到物料: {}", materialId);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略格式错误的材料
                        logger.error("材料格式错误: {}", material, e);
                    }
                }
            }
        }
        logger.info("扣除材料结束");
    }
    
    /**
     * 将生产的产品存入库存
     * @param productName 产品名称
     * @param quantity 数量
     * @param productProperty 产物属性：2=半成品，3=成品
     */
    private void addProductToStock(String productName, Integer quantity, Integer productProperty) {
        logger.info("开始添加产品到库存，productName: {}, 数量: {}", productName, quantity);
        // 根据产品名称查找产品ID
        Product product = productService.lambdaQuery()
                .eq(Product::getName, productName)
                .one();
        if (product != null) {
            logger.info("找到产品: {}, ID: {}", product.getName(), product.getId());
            // 增加库存
            adjustStock(product.getId(), quantity, "生产计划完成");
        } else {
            logger.info("未找到产品: {}, 开始创建新产品", productName);
            // 创建新的产品记录
            Product newProduct = new Product();
            newProduct.setName(productName);
            newProduct.setProperty(productProperty); // 使用传入的产物属性
            newProduct.setStatus(0); // 默认为启用状态
            newProduct.setForbidden(1); // 默认为启用（不禁用）
            newProduct.setStock(0); // 初始库存为0
            // 设置默认值
            newProduct.setCode(""); // 生成简单的默认代码
            newProduct.setCategory(""); // 默认类别
            newProduct.setDescription(""); // 默认描述
            
            // 保存新产品
            boolean saved = productService.save(newProduct);
            if (saved) {
                logger.info("成功创建新产品: {}, ID: {}", newProduct.getName(), newProduct.getId());
                // 增加库存
                adjustStock(newProduct.getId(), quantity, "生产计划完成");
            } else {
                logger.error("创建新产品失败: {}", productName);
            }
        }
        logger.info("添加产品到库存结束");
    }
    
    /**
     * 调整库存
     * @param productId 产品ID
     * @param quantity 调整数量（正数增加，负数减少）
     * @param remark 备注
     */
    private void adjustStock(Long productId, Integer quantity, String remark) {
        logger.info("开始调整库存，productId: {}, 数量: {}, 备注: {}", productId, quantity, remark);
        Product product = productService.getById(productId);
        if (product != null) {
            logger.info("当前库存: {}, 调整数量: {}", product.getStock(), quantity);
            int newStock = product.getStock() + quantity;
            logger.info("新库存: {}", newStock);
            if (newStock >= 0) {
                boolean success = productService.updateStock(productId, newStock);
                logger.info("库存更新结果: {}", success);
            } else {
                logger.warn("库存不足，无法调整。新库存: {}", newStock);
            }
        } else {
            logger.warn("未找到产品: {}", productId);
        }
        logger.info("调整库存结束");
    }
}
