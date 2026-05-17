package com.parking.system;

import com.parking.system.common.Response;
import com.parking.system.controller.OrderController;
import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingLot;
import com.parking.system.service.OrderService;
import com.parking.system.service.ParkingLotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ParkingLotService parkingLotService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupAuthentication(Long userId, boolean isAdmin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, "credentials", authorities);
        SecurityContext context = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(context);
    }

    private void setupUnauthenticated() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("anonymous", "credentials", Collections.emptyList());
        SecurityContext context = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testCreateOrder_Success() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("parkingLotId", 1);
        params.put("plateNumber", "京A12345");

        Order createdOrder = new Order();
        createdOrder.setId(100L);
        createdOrder.setUserId(1L);
        createdOrder.setParkingLotId(1L);
        createdOrder.setPlateNumber("京A12345");
        createdOrder.setStatus(3);

        when(orderService.createOrder(eq(1L), eq(1L), isNull(), eq("京A12345"))).thenReturn(createdOrder);

        Response<Order> response = orderController.createOrder(params);

        assertEquals(200, response.getCode());
        assertEquals("创建成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(100L, response.getData().getId());
        verify(orderService).createOrder(eq(1L), eq(1L), isNull(), eq("京A12345"));
    }

    @Test
    void testCreateOrder_NoAuth() {
        setupUnauthenticated();

        Map<String, Object> params = new HashMap<>();
        params.put("parkingLotId", 1);
        params.put("plateNumber", "京A12345");

        Response<Order> response = orderController.createOrder(params);

        assertEquals(400, response.getCode());
        assertEquals("未登录", response.getMessage());
        verify(orderService, never()).createOrder(anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void testCreateOrder_MissingParkingLotId() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("plateNumber", "京A12345");

        Response<Order> response = orderController.createOrder(params);

        assertEquals(400, response.getCode());
        assertEquals("停车场ID不能为空", response.getMessage());
        verify(orderService, never()).createOrder(anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void testCreateOrder_MissingPlateNumber() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("parkingLotId", 1);

        Response<Order> response = orderController.createOrder(params);

        assertEquals(400, response.getCode());
        assertEquals("车牌号不能为空", response.getMessage());
        verify(orderService, never()).createOrder(anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void testCreateOrder_InvalidPlateNumber() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("parkingLotId", 1);
        params.put("plateNumber", "ABC123");

        Response<Order> response = orderController.createOrder(params);

        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("车牌号格式不合法"));
        verify(orderService, never()).createOrder(anyLong(), anyLong(), any(), anyString());
    }

    @Test
    void testPayOrder_Success() {
        setupAuthentication(1L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(0);

        Map<String, Object> params = new HashMap<>();
        params.put("paymentMethod", 1);

        when(orderService.getById(1L)).thenReturn(order);
        when(orderService.payOrder(1L, 1)).thenReturn(true);

        Response<?> response = orderController.payOrder(1L, params);

        assertEquals(200, response.getCode());
        assertEquals("支付成功", response.getData());
        verify(orderService).payOrder(1L, 1);
    }

    @Test
    void testPayOrder_InvalidPaymentMethod() {
        setupAuthentication(1L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(0);

        Map<String, Object> params = new HashMap<>();
        params.put("paymentMethod", "abc");

        when(orderService.getById(1L)).thenReturn(order);

        Response<?> response = orderController.payOrder(1L, params);

        assertEquals(400, response.getCode());
        assertEquals("支付方式格式错误", response.getMessage());
        verify(orderService, never()).payOrder(anyLong(), anyInt());
    }

    @Test
    void testCancelOrder_Success() {
        setupAuthentication(1L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(3);

        when(orderService.getById(1L)).thenReturn(order);
        when(orderService.cancelOrder(1L)).thenReturn(true);

        Response<?> response = orderController.cancelOrder(1L);

        assertEquals(200, response.getCode());
        assertEquals("取消成功", response.getData());
        verify(orderService).cancelOrder(1L);
    }

    @Test
    void testDeleteOrder_ActiveOrder() {
        setupAuthentication(1L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(3);

        when(orderService.getById(1L)).thenReturn(order);

        Response<?> response = orderController.deleteOrder(1L);

        assertEquals(400, response.getCode());
        assertEquals("只能删除已完成或已取消的订单", response.getMessage());
        verify(orderService, never()).removeById(anyLong());
    }

    @Test
    void testDeleteOrder_CompletedOrder() {
        setupAuthentication(1L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(1);

        when(orderService.getById(1L)).thenReturn(order);
        when(orderService.removeById(1L)).thenReturn(true);

        Response<?> response = orderController.deleteOrder(1L);

        assertEquals(200, response.getCode());
        assertEquals("删除成功", response.getData());
        verify(orderService).removeById(1L);
    }

    @Test
    void testGetOrder_UnauthorizedAccess() {
        setupAuthentication(2L, false);

        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(1);
        order.setParkingLotId(1L);

        when(orderService.getById(1L)).thenReturn(order);

        Response<Order> response = orderController.getOrder(1L);

        assertEquals(403, response.getCode());
        assertEquals("无权访问该订单", response.getMessage());
    }
}
