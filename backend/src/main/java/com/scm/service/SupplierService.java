package com.scm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scm.entity.Supplier;
import com.scm.entity.SupplierItem;
import java.util.List;

public interface SupplierService extends IService<Supplier> {
    IPage<Supplier> getSupplierList(Page<Supplier> page, String name, String code, Integer status);
    Boolean updateStatus(Long id, Integer status);
    Supplier getSupplierDetail(Long id);
    Boolean updateSupplierItem(SupplierItem supplierItem);
    Boolean addSupplierItem(SupplierItem supplierItem);
    List<Supplier> getSuppliersByProductName(String productName);
    List<String> getProductNames();
    Integer getPropertyByProductName(String productName);
}