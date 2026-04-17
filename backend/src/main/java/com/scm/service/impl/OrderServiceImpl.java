package com.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scm.entity.Order;
import com.scm.entity.OrderItem;
import com.scm.entity.Product;
import com.scm.mapper.OrderMapper;
import com.scm.mapper.OrderItemMapper;
import com.scm.service.OrderService;
import com.scm.service.ProductService;
import com.scm.dto.CreateOrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ProductService productService;
    
    @Override
    public IPage<Order> getOrderList(Page<Order> page, String orderNo, Integer status, String startTime, String endTime, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        // 添加状态筛选条件
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        // 添加用户ID筛选条件
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        // 添加时间范围筛选条件
        if (StringUtils.hasText(startTime)) {
            try {
                LocalDateTime start = LocalDateTime.parse(startTime + "T00:00:00");
                wrapper.ge(Order::getCreateTime, start);
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        if (StringUtils.hasText(endTime)) {
            try {
                LocalDateTime end = LocalDateTime.parse(endTime + "T23:59:59");
                wrapper.le(Order::getCreateTime, end);
            } catch (Exception e) {
                // 日期格式错误，忽略该条件
            }
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Order getOrderDetail(Long id) {
        // 查询订单基本信息
        Order order = this.getById(id);
        if (order != null) {
            // 从数据库查询订单项数据
            List<Map<String, Object>> itemMaps = orderItemMapper.selectItemsByOrderId(id);
            List<OrderItem> items = new ArrayList<>();
            
            // 将查询结果转换为OrderItem对象，确保正确处理字段名映射（数据库下划线格式到Java驼峰格式）
            for (Map<String, Object> itemMap : itemMaps) {
                OrderItem item = new OrderItem();
                // 基本字段映射
                if (itemMap.get("id") != null) item.setId(Long.valueOf(itemMap.get("id").toString()));
                if (itemMap.get("order_id") != null) item.setOrderId(Long.valueOf(itemMap.get("order_id").toString()));
                if (itemMap.get("product_id") != null) item.setProductId(Long.valueOf(itemMap.get("product_id").toString()));
                
                // 确保产品名称和图片字段正确映射，满足前端对驼峰式字段名的要求
                if (itemMap.get("product_name") != null) item.setProductName(itemMap.get("product_name").toString());
                if (itemMap.get("product_image") != null) item.setProductImage(itemMap.get("product_image").toString());
                
                // 数量和金额字段映射
                if (itemMap.get("quantity") != null) item.setQuantity(Integer.valueOf(itemMap.get("quantity").toString()));
                if (itemMap.get("price") != null) item.setPrice(new BigDecimal(itemMap.get("price").toString()));
                if (itemMap.get("total_amount") != null) item.setTotalAmount(new BigDecimal(itemMap.get("total_amount").toString()));
                
                // 时间字段映射
                if (itemMap.get("create_time") != null) item.setCreateTime((LocalDateTime) itemMap.get("create_time"));
                if (itemMap.get("update_time") != null) item.setUpdateTime((LocalDateTime) itemMap.get("update_time"));
                
                items.add(item);
            }
            
            // 设置订单项到订单对象
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void payOrder(Long id) {
        log.info("开始处理订单支付，订单ID: {}", id);
        
        // 获取订单详情
        Order order = this.getById(id);
        if (order == null) {
            log.error("订单不存在，订单ID: {}", id);
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 0) {
            log.error("订单状态错误，无法支付，订单ID: {}, 当前状态: {}", id, order.getStatus());
            throw new RuntimeException("订单状态错误，无法支付");
        }
        
        // 查询订单项
        List<Map<String, Object>> itemMaps = orderItemMapper.selectItemsByOrderId(id);
        if (itemMaps == null || itemMaps.isEmpty()) {
            log.error("订单无商品，无法支付，订单ID: {}", id);
            throw new RuntimeException("订单无商品，无法支付");
        }
        
        // 检查库存并减少库存
        for (Map<String, Object> itemMap : itemMaps) {
            Long productId = Long.valueOf(itemMap.get("product_id").toString());
            Integer quantity = Integer.valueOf(itemMap.get("quantity").toString());
            
            // 获取商品信息
            Product product = productService.getById(productId);
            if (product == null) {
                log.error("商品不存在，商品ID: {}", productId);
                throw new RuntimeException("商品不存在");
            }
            
            log.info("处理商品: {}, 当前库存: {}, 购买数量: {}", product.getName(), product.getStock(), quantity);
            
            // 检查库存是否足够
            if (product.getStock() < quantity) {
                log.error("商品库存不足：{}，当前库存: {}, 购买数量: {}", product.getName(), product.getStock(), quantity);
                throw new RuntimeException("商品库存不足：" + product.getName());
            }
            
            // 减少库存
            int newStock = product.getStock() - quantity;
            log.info("更新商品: {} 的库存，从 {} 减少到 {}", product.getName(), product.getStock(), newStock);
            
            boolean stockUpdated = productService.updateStock(productId, newStock);
            if (!stockUpdated) {
                log.error("更新库存失败：{}", product.getName());
                throw new RuntimeException("更新库存失败：" + product.getName());
            }
            
            log.info("商品: {} 库存更新成功，新库存: {}", product.getName(), newStock);
        }
        
        // 更新订单状态为已支付
        Order updateOrder = new Order();
        updateOrder.setId(id);
        updateOrder.setStatus(1); // 已支付状态
        boolean orderUpdated = this.updateById(updateOrder);
        
        if (orderUpdated) {
            log.info("订单支付成功，订单ID: {}, 状态更新为已支付", id);
        } else {
            log.error("更新订单状态失败，订单ID: {}", id);
            throw new RuntimeException("更新订单状态失败");
        }
    }

    @Override
    public void shipOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(2); // 已发货状态
        this.updateById(order);
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(-1); // 已取消状态
        this.updateById(order);
    }

    @Override
    public void completeOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(3); // 已完成状态
        this.updateById(order);
    }

    @Override
    public Long getIncrease(LocalDateTime since) {
        return baseMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, since));
    }

    @Override
    public BigDecimal getTotalSales() {
        return baseMapper.selectTotalSales();
    }

    @Override
    public BigDecimal getLastWeekSales() {
        LocalDateTime startTime = LocalDateTime.now().minusWeeks(1);
        return baseMapper.selectSalesBetween(startTime, LocalDateTime.now());
    }

    @Override
    public List<Map<String, Object>> getWeeklyTrend(LocalDateTime date) {
        return baseMapper.selectWeeklyTrend(date);
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(LocalDateTime date) {
        return baseMapper.selectMonthlyTrend(date);
    }

    @Override
    public void createOrder(CreateOrderDTO createOrderDTO) {
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderDTO.OrderItemDTO itemDTO : createOrderDTO.getItems()) {
            totalAmount = totalAmount.add(itemDTO.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
        }
        
        // 创建订单对象并设置属性
        Order order = new Order();
        order.setUserId(createOrderDTO.getUserId());
        // 优先使用前端传递的订单编号，如果没有则自动生成
        String orderNo = (createOrderDTO.getOrderNo() != null && !createOrderDTO.getOrderNo().trim().isEmpty()) ? 
                         createOrderDTO.getOrderNo() : generateOrderNo();
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待支付状态
        // 设置必填字段
        order.setConsignee(createOrderDTO.getConsignee() != null ? createOrderDTO.getConsignee() : "默认收货人");
        order.setPhone(createOrderDTO.getPhone() != null ? createOrderDTO.getPhone() : "13800138000");
        order.setAddress(createOrderDTO.getAddress() != null ? createOrderDTO.getAddress() : "默认地址");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 保存订单主表信息
        boolean saved = super.save(order);
        
        // 如果订单保存成功且DTO中包含订单项，则保存订单项
        if (saved && createOrderDTO.getItems() != null && !createOrderDTO.getItems().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (CreateOrderDTO.OrderItemDTO itemDTO : createOrderDTO.getItems()) {
                // 检查商品是否上架
                Product product = productService.getById(itemDTO.getProductId());
                if (product == null) {
                    throw new RuntimeException("商品不存在");
                }
                if (product.getStatus() != 1) {
                    throw new RuntimeException("商品未上架，无法添加到订单");
                }
                
                // 创建OrderItem对象并设置属性
                OrderItem item = new OrderItem();
                // 设置基本属性
                item.setOrderId(order.getId());
                item.setProductId(itemDTO.getProductId());
                item.setProductName(product.getName());
                item.setProductImage(product.getImage());
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                
                // 计算总金额
                if (itemDTO.getPrice() != null && itemDTO.getQuantity() != null) {
                    item.setTotalAmount(itemDTO.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
                }
                
                // 设置商品名称和图片（已从productService获取）
                
                // 设置创建和更新时间
                item.setCreateTime(now);
                item.setUpdateTime(now);
                
                // 保存订单项到数据库
                orderItemMapper.insert(item);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getWeeklySalesTrend(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectWeeklySalesTrend(start, end);
    }

    @Override
    public List<Map<String, Object>> getMonthlySalesTrend(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectMonthlySalesTrend(start, end);
    }

    @Override
    public boolean save(Order order) {
        // 保存订单主表信息
        boolean saved = super.save(order);
        
        // 如果订单保存成功且包含订单项，则保存订单项
        if (saved && order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                // 设置订单项与订单的关联
                item.setOrderId(order.getId());
                
                // 根据productId查询商品信息，填充商品名称和图片
                // 注意：这里应该通过ProductMapper或ProductService查询，为简化直接从数据库查询
                Map<String, Object> productInfo = getProductInfoById(item.getProductId());
                if (productInfo != null) {
                    item.setProductName(productInfo.get("name") != null ? productInfo.get("name").toString() : "未知商品");
                    item.setProductImage(productInfo.get("image") != null ? productInfo.get("image").toString() : "");
                }
                
                // 设置创建和更新时间
                LocalDateTime now = LocalDateTime.now();
                item.setCreateTime(now);
                item.setUpdateTime(now);
                
                // 保存订单项到数据库
                orderItemMapper.insert(item);
            }
        }
        
        return saved;
    }
    
    /**
     * 根据商品ID查询商品信息
     */
    private Map<String, Object> getProductInfoById(Long productId) {
        try {
            Product product = productService.getById(productId);
            if (product != null) {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("name", product.getName());
                productInfo.put("image", product.getImage());
                return productInfo;
            }
            return null;
        } catch (Exception e) {
            System.err.println("查询商品信息失败: " + productId + ", 错误: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> selectSalesRank(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectSalesRank(start, end);
    }

    @Override
    public List<Map<String, Object>> getWeeklyOrderTrend(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectWeeklyOrderTrend(start, end);
    }

    @Override
    public List<Map<String, Object>> getMonthlyOrderTrend(LocalDateTime start, LocalDateTime end) {
        return baseMapper.selectMonthlyOrderTrend(start, end);
    }

    @Override
    public boolean checkOrderNoExists(String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
               .eq(Order::getDeleted, 0);
        return this.count(wrapper) > 0;
    }
    
    // 生成订单编号的方法
    private String generateOrderNo() {
        // 简单生成订单编号的逻辑：年月日时分秒+6位随机数
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 1000000));
        return timestamp + String.format("%06d", Integer.parseInt(random));
    }
}