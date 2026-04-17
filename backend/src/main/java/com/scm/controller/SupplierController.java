package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Supplier;
import com.scm.entity.SupplierItem;
import com.scm.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/supplier")
public class SupplierController {
    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);

    @Autowired
    private SupplierService supplierService;

    @GetMapping("/list")
    public Result<IPage<Supplier>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer status) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        return Result.success(supplierService.getSupplierList(page, name, code, status));
    }

    @PutMapping("/item")
    public Result<Boolean> updateSupplierItem(@RequestBody SupplierItem supplierItem) {
        return Result.success(supplierService.updateSupplierItem(supplierItem));
    }

    @PostMapping("/item")
    public Result<Boolean> addSupplierItem(@RequestBody SupplierItem supplierItem) {
        log.info("插入供应商产品信息: {}", supplierItem);
        boolean result = supplierService.addSupplierItem(supplierItem);
        log.info("插入结果: {}", result);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Supplier> getById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierDetail(id);
        System.out.println("返回的Supplier对象: " + supplier);
        System.out.println("Supplier对象的supplierProducts: " + (supplier != null ? supplier.getSupplierProducts() : null));
        return Result.success(supplier);
    }

    @GetMapping("/by-product")
    public Result<List<Supplier>> getSuppliersByProductName(@RequestParam String productName) {
        return Result.success(supplierService.getSuppliersByProductName(productName));
    }

    @GetMapping("/product-names")
    public Result<List<String>> getProductNames() {
        return Result.success(supplierService.getProductNames());
    }

    @GetMapping("/property-by-product")
    public Result<Integer> getPropertyByProductName(@RequestParam String productName) {
        return Result.success(supplierService.getPropertyByProductName(productName));
    }

    @PostMapping
    public Result<Supplier> add(@RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.success(supplier);
    }

    @PutMapping
    public Result<Supplier> update(@RequestBody Supplier supplier) {
        supplierService.updateById(supplier);
        return Result.success(supplier);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(supplierService.removeById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return Result.success(supplierService.updateStatus(id, status));
    }
}