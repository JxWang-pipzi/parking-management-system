package com.parking.system;

import com.parking.system.entity.Order;
import com.parking.system.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testGetOrdersByUserId() {
        Long userId = 1L;
        List<Order> orders = orderService.getOrdersByUserId(userId, null);
        assertNotNull(orders, "订单列表不应为空");
    }

    @Test
    void testGetOrdersByStatus() {
        Long userId = 1L;
        Integer status = 0;
        List<Order> orders = orderService.getOrdersByUserId(userId, status);
        assertNotNull(orders, "按状态筛选的订单列表不应为空");
        
        for (Order order : orders) {
            assertEquals(status, order.getStatus(), "订单状态应该匹配");
        }
    }

    @Test
    void testCancelOrder() {
        List<Order> orders = orderService.getOrdersByUserId(1L, 0);
        if (!orders.isEmpty()) {
            Order order = orders.get(0);
            boolean result = orderService.cancelOrder(order.getId());
            assertTrue(result, "取消订单应该成功");
            
            Order cancelledOrder = orderService.getById(order.getId());
            assertEquals(2, cancelledOrder.getStatus(), "订单状态应该变为已取消");
        }
    }
}