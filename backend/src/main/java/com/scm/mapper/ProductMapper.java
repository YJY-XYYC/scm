package com.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scm.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    @Select("SELECT * FROM sys_product WHERE property = 3 AND forbidden = 1 ORDER BY sales DESC LIMIT #{limit}")
    List<Map<String, Object>> selectTopProduct(Integer limit);
    
    @Select("SELECT category, COUNT(*) as count FROM sys_product WHERE property = 3 AND forbidden = 1 GROUP BY category")
    List<Map<String, Object>> selectCategoryDistribution();
    
    @Select("SELECT DISTINCT category FROM sys_product WHERE property = 3 AND forbidden = 1 AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectProductCategories();
}