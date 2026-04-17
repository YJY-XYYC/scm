package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scm.common.Result;
import com.scm.entity.Product;
import com.scm.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public Result<IPage<Product>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean showAllStatus) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        // 调用服务层方法，只查询property=3的数据
        return Result.success(productService.getProductList(page, name, categoryId, showAllStatus));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Product> add(@RequestBody Product product) {
        productService.save(product);
        return Result.success(product);
    }

    @PutMapping
    public Result<Product> update(@RequestBody Product product) {
        productService.updateById(product);
        return Result.success(product);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(productService.removeById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        return Result.success(productService.updateById(product));
    }

    @PutMapping("/{id}/stock")
    public Result<Boolean> updateStock(
            @PathVariable Long id,
            @RequestParam Integer stock) {
        return Result.success(productService.updateStock(id, stock));
    }
    
    @GetMapping("/category/list")
    public Result<List<String>> getProductCategories() {
        return Result.success(productService.getProductCategories());
    }
}