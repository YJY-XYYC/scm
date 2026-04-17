package com.scm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scm.common.Result;
import com.scm.dto.OrderQueryDTO;
import com.scm.dto.CreateOrderDTO;
import com.scm.entity.Order;
import com.scm.entity.OrderItem;
import com.scm.mapper.OrderItemMapper;
import com.scm.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @GetMapping("/list")
    public Result<IPage<Order>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long userId) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        return Result.success(orderService.getOrderList(page, orderNo, status, startTime, endTime, userId));
    }

    @GetMapping("/check-no")
    public Result<Boolean> checkOrderNoExists(@RequestParam String orderNo) {
        boolean exists = orderService.checkOrderNoExists(orderNo);
        return Result.success(!exists); // 返回true表示可以使用，false表示已存在
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        System.out.println("收到订单详情请求，订单ID: " + id);
        Order order = orderService.getOrderDetail(id);
        System.out.println("查询到订单详情: " + order);
        if (order != null && order.getItems() != null) {
            System.out.println("订单项数量: " + order.getItems().size());
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<Order> add(@RequestBody Order order) {
        // 设置默认状态为待支付(0)
        order.setStatus(0);
        
        // 创建CreateOrderDTO对象
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setUserId(order.getUserId());
        createOrderDTO.setOrderNo(order.getOrderNo());
        createOrderDTO.setConsignee(order.getConsignee());
        createOrderDTO.setPhone(order.getPhone());
        createOrderDTO.setAddress(order.getAddress());
        
        // 转换订单项
        List<CreateOrderDTO.OrderItemDTO> itemDTOs = new ArrayList<>();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                CreateOrderDTO.OrderItemDTO itemDTO = new CreateOrderDTO.OrderItemDTO();
                itemDTO.setProductId(item.getProductId());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setPrice(item.getPrice());
                itemDTOs.add(itemDTO);
            }
        }
        createOrderDTO.setItems(itemDTOs);
        
        // 使用createOrder方法保存订单及订单项
        orderService.createOrder(createOrderDTO);
        
        return Result.success(order);
    }

    @PutMapping
    public Result<Order> update(@RequestBody Order order) {
        orderService.updateById(order);
        return Result.success(order);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 先删除对应的订单项
        orderItemMapper.deleteByOrderId(id);
        // 再删除订单
        return Result.success(orderService.removeById(id));
    }
    
    @DeleteMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> batchDelete(@RequestBody List<Long> ids) {
        log.info("开始批量删除订单，ids: {}", ids);
        try {
            // 验证ids参数
            if (ids == null || ids.isEmpty()) {
                log.warn("批量删除订单参数为空");
                return Result.error("删除的订单ID列表不能为空");
            }
            
            // 循环删除每个订单及其订单项
            for (Long id : ids) {
                log.info("准备删除订单ID: {}", id);
                
                // 验证订单是否存在
                Order order = orderService.getById(id);
                if (order == null) {
                    log.warn("订单ID: {} 不存在", id);
                    continue; // 跳过不存在的订单，继续删除其他订单
                }
                
                // 先删除对应的订单项
                int deletedItems = orderItemMapper.deleteByOrderId(id);
                log.info("删除订单项成功，订单ID: {}, 删除数量: {}", id, deletedItems);
                
                // 再删除订单
                boolean deleted = orderService.removeById(id);
                log.info("删除订单成功，订单ID: {}, 删除结果: {}", id, deleted);
                
                if (!deleted) {
                    throw new RuntimeException("订单删除失败：" + id);
                }
            }
            return Result.success(true);
        } catch (RuntimeException e) {
            log.error("批量删除订单运行时异常:", e);
            // 事务会自动回滚
            return Result.error("批量删除订单失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("批量删除订单发生未知异常:", e);
            // 事务会自动回滚
            return Result.error("批量删除订单失败：系统异常，请联系管理员");
        }
    }

    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        return Result.success(orderService.updateById(order));
    }

    // 发货
    @PutMapping("/{id}/ship")
    public Result<Boolean> shipOrder(@PathVariable Long id) {
        orderService.shipOrder(id);
        return Result.success(true);
    }

    // 支付订单
    @PutMapping("/{id}/pay")
    public Result<Boolean> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return Result.success(true);
    }

    // 取消订单
    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success(true);
    }
    
    // 完成订单
    @PutMapping("/{id}/complete")
    public Result<Boolean> completeOrder(@PathVariable Long id) {
        orderService.completeOrder(id);
        return Result.success(true);
    }
}