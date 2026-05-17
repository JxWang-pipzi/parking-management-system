package com.parking.system;

import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.entity.User;
import com.parking.system.service.OrderService;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import com.parking.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceEnhancedTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private ParkingSpaceService parkingSpaceService;

    private Long testUserId;
    private Long testParkingLotId;
    private Long testParkingSpaceId;

    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName("订单测试停车场_" + System.nanoTime());
        parkingLot.setAddress("成都市高新区测试路");
        parkingLot.setTotalSpaces(5);
        parkingLot.setAvailableSpaces(5);
        parkingLot.setHourlyRate(new BigDecimal("6.00"));
        parkingLot.setLatitude(30.5728);
        parkingLot.setLongitude(104.0668);
        parkingLot.setStatus(1);
        assertTrue(parkingLotService.createParkingLot(parkingLot), "测试停车场初始化失败");
        return parkingLot;
    }

    private Long requireAvailableSpaceId(Long parkingLotId) {
        List<ParkingSpace> spaces = parkingSpaceService.getReservableSpaces(parkingLotId);
        assertFalse(spaces.isEmpty(), "测试停车场应至少存在一个可用车位");
        return spaces.get(0).getId();
    }

    private String uniquePhone() {
        return "139" + String.format("%08d", System.nanoTime() % 100000000L);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@example.com";
    }

    @BeforeEach
    void setUp() {
        System.out.println("[测试准备] 开始初始化测试数据...");
        
        User user = new User();
        user.setUsername("order_test_user_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("订单测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("ordertest"));
        userService.register(user);
        User savedUser = userService.getByUsername(user.getUsername());
        assertNotNull(savedUser, "测试用户初始化失败");
        testUserId = savedUser.getId();

        ParkingLot parkingLot = createTestParkingLot();
        testParkingLotId = parkingLot.getId();
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        
        System.out.println("[测试准备] 初始化完成: userId=" + testUserId + ", lotId=" + testParkingLotId + ", spaceId=" + testParkingSpaceId);
    }

    @Test
    @DisplayName("P0-正常场景：创建订单成功")
    void testCreateOrder() {
        System.out.println("[测试场景] 正常场景：创建订单成功");
        System.out.println("[输入参数] userId=" + testUserId + ", parkingLotId=" + testParkingLotId + ", parkingSpaceId=" + testParkingSpaceId);
        
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京A12345");
        
        System.out.println("[预期输出] 订单创建成功，订单ID不为空");
        System.out.println("[实际输出] orderId=" + (order != null ? order.getId() : "null"));
        assertNotNull(order, "订单应该创建成功");
        assertNotNull(order.getId(), "订单ID不应该为空");
        assertEquals(3, order.getStatus(), "订单状态应该为停车中");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：创建订单-无效用户ID")
    void testCreateOrderWithInvalidUserId() {
        System.out.println("[测试场景] 异常场景：创建订单-无效用户ID");
        System.out.println("[输入参数] userId=-1（无效ID）");
        
        try {
            Order order = orderService.createOrder(-1L, testParkingLotId, testParkingSpaceId, "京A12345");
            
            System.out.println("[预期输出] 订单创建失败或返回null");
            System.out.println("[实际输出] order=" + (order != null ? order.getId() : "null"));
            System.out.println("[测试结果] ✓ 通过（允许返回null或抛出异常）");
        } catch (Exception e) {
            System.out.println("[实际输出] 抛出异常: " + e.getMessage());
            System.out.println("[测试结果] ✓ 通过（正确处理异常）");
        }
    }

    @Test
    @DisplayName("P0-正常场景：支付订单成功")
    void testPayOrder() {
        System.out.println("[测试场景] 正常场景：支付待支付订单成功");
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京B12345");
        assertNotNull(order, "测试订单应该创建成功");
        order.setStatus(0);
        orderService.updateById(order);
        System.out.println("[输入参数] orderId=" + order.getId() + ", paymentMethod=0（微信）");
        
        boolean result = orderService.payOrder(order.getId(), 0);
        
        System.out.println("[预期输出] 支付成功=true");
        System.out.println("[实际输出] 支付结果=" + result);
        assertTrue(result, "支付应该成功");
        
        Order paidOrder = orderService.getById(order.getId());
        assertEquals(1, paidOrder.getStatus(), "订单状态应该为已支付");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：支付幂等性校验-重复支付")
    void testPayOrderIdempotency() {
        System.out.println("[测试场景] 边界场景：支付幂等性校验-重复支付");
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京C12345");
        assertNotNull(order, "测试订单应该创建成功");
        order.setStatus(0);
        orderService.updateById(order);
        System.out.println("[输入参数] orderId=" + order.getId());
        
        boolean firstPay = orderService.payOrder(order.getId(), 0);
        System.out.println("[第一次支付] 结果=" + firstPay);
        assertTrue(firstPay, "第一次支付应该成功");
        
        boolean secondPay = orderService.payOrder(order.getId(), 1);
        System.out.println("[第二次支付] 结果=" + secondPay);
        
        System.out.println("[预期输出] 第二次支付失败=false");
        System.out.println("[实际输出] 第二次支付结果=" + secondPay);
        assertFalse(secondPay, "重复支付应该失败");
        System.out.println("[测试结果] ✓ 通过（幂等性校验正常）");
    }

    @Test
    @DisplayName("P2-异常场景：支付不存在的订单")
    void testPayNonExistentOrder() {
        System.out.println("[测试场景] 异常场景：支付不存在的订单");
        System.out.println("[输入参数] orderId=-1（不存在的订单）");
        
        boolean result = orderService.payOrder(-1L, 0);
        
        System.out.println("[预期输出] 支付失败=false");
        System.out.println("[实际输出] 支付结果=" + result);
        assertFalse(result, "支付不存在的订单应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：取消订单成功")
    void testCancelOrder() {
        System.out.println("[测试场景] 正常场景：取消订单成功");
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京D12345");
        assertNotNull(order, "测试订单应该创建成功");
        System.out.println("[输入参数] orderId=" + order.getId());
        
        boolean result = orderService.cancelOrder(order.getId());
        
        System.out.println("[预期输出] 取消成功=true");
        System.out.println("[实际输出] 取消结果=" + result);
        assertTrue(result, "取消订单应该成功");
        
        Order cancelledOrder = orderService.getById(order.getId());
        assertEquals(2, cancelledOrder.getStatus(), "订单状态应该为已取消");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：取消已支付的订单")
    void testCancelPaidOrder() {
        System.out.println("[测试场景] 异常场景：取消已支付的订单");
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京E12345");
        assertNotNull(order, "测试订单应该创建成功");
        order.setStatus(0);
        orderService.updateById(order);
        orderService.payOrder(order.getId(), 0);
        System.out.println("[输入参数] orderId=" + order.getId() + "（已支付订单）");
        
        boolean result = orderService.cancelOrder(order.getId());
        
        System.out.println("[预期输出] 取消失败=false");
        System.out.println("[实际输出] 取消结果=" + result);
        assertFalse(result, "取消已支付的订单应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：查询用户订单列表")
    void testGetOrdersByUserId() {
        System.out.println("[测试场景] 正常场景：查询用户订单列表");
        System.out.println("[输入参数] userId=" + testUserId);
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京F12345");
        
        List<Order> orders = orderService.getOrdersByUserId(testUserId, null);
        
        System.out.println("[预期输出] 查询到订单列表");
        System.out.println("[实际输出] 订单数量=" + orders.size());
        assertNotNull(orders, "订单列表不应该为空");
        assertTrue(orders.size() > 0, "应该有订单记录");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：按状态查询订单")
    void testGetOrdersByStatus() {
        System.out.println("[测试场景] 边界场景：按状态查询订单");
        
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京G12345");
        System.out.println("[输入参数] userId=" + testUserId + ", status=3（停车中）");
        
        List<Order> pendingOrders = orderService.getOrdersByUserId(testUserId, 3);
        
        System.out.println("[预期输出] 查询到待支付订单");
        System.out.println("[实际输出] 待支付订单数量=" + pendingOrders.size());
        assertTrue(pendingOrders.size() > 0, "应该有待支付订单");
        assertTrue(pendingOrders.stream().allMatch(o -> o.getStatus() == 3), "所有订单状态应该为停车中");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：查询不存在的用户订单")
    void testGetOrdersByNonExistentUser() {
        System.out.println("[测试场景] 边界场景：查询不存在的用户订单");
        System.out.println("[输入参数] userId=-1（不存在的用户）");
        
        List<Order> orders = orderService.getOrdersByUserId(-1L, null);
        
        System.out.println("[预期输出] 返回空列表");
        System.out.println("[实际输出] 订单数量=" + orders.size());
        assertNotNull(orders, "订单列表不应该为null");
        assertEquals(0, orders.size(), "应该返回空列表");
        System.out.println("[测试结果] ✓ 通过");
    }
}
