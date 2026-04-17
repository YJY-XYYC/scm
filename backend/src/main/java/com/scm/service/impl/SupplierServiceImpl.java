package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Supplier;
import com.scm.entity.SupplierItem;
import com.scm.mapper.SupplierMapper;
import com.scm.mapper.SupplierItemMapper;
import com.scm.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {
    
    @Autowired
    private SupplierItemMapper supplierItemMapper;
    
    @Override
    public IPage<Supplier> getSupplierList(Page<Supplier> page, String name, String code, Integer status) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        
        // 智能判断搜索内容并从对应字段查询
        // 修改搜索逻辑：同时在name和code字段上进行OR搜索
        // 这样无论用户输入的是供应商名称还是编码，都能找到匹配的结果
        if (org.springframework.util.StringUtils.hasText(name) || org.springframework.util.StringUtils.hasText(code)) {
            // 使用or条件组，只要name或code任一字段匹配即可
            wrapper.and(w -> {
                if (org.springframework.util.StringUtils.hasText(name)) {
                    w.like(Supplier::getName, name);
                }
                if (org.springframework.util.StringUtils.hasText(code)) {
                    w.or().like(Supplier::getCode, code);
                }
            });
        }
        
        // 如果状态不为空，添加状态查询条件
        if (status != null) {
            wrapper.eq(Supplier::getStatus, status);
        }
        
        // 添加排序条件
        wrapper.orderByDesc(Supplier::getCreateTime);
        
        // 执行分页查询
        return this.page(page, wrapper);
    }
    
    @Override
    public Boolean updateStatus(Long id, Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setStatus(status);
        return this.updateById(supplier);
    }
    
    @Override
    public Supplier getSupplierDetail(Long id) {
        // 查询供应商基本信息
        Supplier supplier = this.getById(id);
        if (supplier != null) {
            // 查询供应商产品关联信息
            List<Map<String, Object>> items = supplierItemMapper.selectItemsBySupplierId(id);
            // 调试日志：查看原始查询结果
            System.out.println("原始关联产品信息: " + items);
            // 转换下划线命名为驼峰命名
            List<Map<String, Object>> camelItems = items.stream()
                    .map(item -> {
                        Map<String, Object> camelItem = new HashMap<>();
                        item.forEach((key, value) -> {
                            String camelKey = StringUtils.underlineToCamel(key);
                            camelItem.put(camelKey, value);
                        });
                        return camelItem;
                    })
                    .collect(Collectors.toList());
            // 添加调试日志
            System.out.println("转换后的关联产品信息: " + camelItems);
            supplier.setSupplierProducts(camelItems);
        }
        return supplier;
    }

    @Override
    public Boolean updateSupplierItem(SupplierItem supplierItem) {
        return supplierItemMapper.updateById(supplierItem) > 0;
    }

    @Override
    public Boolean addSupplierItem(SupplierItem supplierItem) {
        return supplierItemMapper.insert(supplierItem) > 0;
    }

    @Override
    public List<Supplier> getSuppliersByProductName(String productName) {
        if (StringUtils.isNotBlank(productName)) {
            // 根据产品名称查询所有相关的供应商ID
            List<Long> supplierIds = supplierItemMapper.selectSupplierIdsByProductName(productName);
            // 如果有供应商ID，根据这些ID查询启用的供应商列表
            if (!supplierIds.isEmpty()) {
                LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
                wrapper.in(Supplier::getId, supplierIds)
                       .eq(Supplier::getStatus, 1); // 仅显示启用的供应商
                return list(wrapper);
            }
        }
        // 如果productName为空或未找到相关供应商，返回所有启用的供应商
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getStatus, 1); // 仅显示启用的供应商
        return list(wrapper);
    }

    @Override
    public List<String> getProductNames() {
        return supplierItemMapper.selectDistinctProductNames();
    }

    @Override
    public Integer getPropertyByProductName(String productName) {
        if (org.springframework.util.StringUtils.hasText(productName)) {
            return supplierItemMapper.selectPropertyByProductName(productName);
        }
        return null;
    }
}