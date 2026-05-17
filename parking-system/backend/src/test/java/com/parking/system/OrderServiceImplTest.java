package com.parking.system;

import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import com.parking.system.service.PaymentRecordService;
import com.parking.system.service.WebSocketService;
import com.parking.system.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Spy
    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Mock
    private ParkingSpaceService parkingSpaceService;

    @Mock
    private ParkingLotService parkingLotService;

    @Mock
    private PaymentRecordService paymentRecordService;

    @Mock
    private WebSocketService webSocketService;

    @BeforeEach
    void setUp() {
    }

    private Order createOrder(Long id, Long userId, Long parkingLotId, Long parkingSpaceId, Integer status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setParkingLotId(parkingLotId);
        order.setParkingSpaceId(parkingSpaceId);
        order.setPlateNumber("京A12345");
        order.setStartTime(new Date(System.currentTimeMillis() - 60 * 60 * 1000));
        order.setStatus(status);
        order.setAmount(BigDecimal.ZERO);
        return order;
    }

    @Test
    void testCreateOrder_AllocateSpaceSuccess() {
        doReturn(10L).when(parkingSpaceService).atomicAllocateSpace(1L, 1);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return true;
        }).when(orderServiceImpl).save(any(Order.class));
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        Order result = orderServiceImpl.createOrder(1L, 1L, null, "京A12345");

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getParkingLotId());
        assertEquals(10L, result.getParkingSpaceId());
        assertEquals("京A12345", result.getPlateNumber());
        assertEquals(3, result.getStatus());
        verify(parkingSpaceService).atomicAllocateSpace(1L, 1);
        verify(parkingLotService).updateAvailableSpaces(1L, -1);
    }

    @Test
    void testCreateOrder_NoAvailableSpace() {
        doReturn(null).when(parkingSpaceService).atomicAllocateSpace(1L, 1);

        Order result = orderServiceImpl.createOrder(1L, 1L, null, "京A12345");

        assertNull(result);
        verify(parkingSpaceService).atomicAllocateSpace(1L, 1);
        verify(orderServiceImpl, never()).save(any(Order.class));
    }

    @Test
    void testPayOrder_Success() {
        Order order = createOrder(1L, 1L, 1L, 10L, 0);

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setHourlyRate(BigDecimal.TEN);

        doReturn(order).when(orderServiceImpl).getById(1L);
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));
        when(parkingLotService.getById(1L)).thenReturn(parkingLot);
        when(paymentRecordService.save(any(PaymentRecord.class))).thenReturn(true);
        when(parkingSpaceService.releaseParkingSpace(anyLong())).thenReturn(true);
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        boolean result = orderServiceImpl.payOrder(1L, 1);

        assertTrue(result);
        verify(orderServiceImpl).updateById(argThat(o ->
                o.getStatus() == 1 &&
                        o.getEndTime() != null &&
                        o.getAmount() != null &&
                        o.getAmount().compareTo(BigDecimal.ZERO) > 0
        ));
        verify(paymentRecordService).save(any(PaymentRecord.class));
        verify(parkingSpaceService).releaseParkingSpace(10L);
        verify(parkingLotService).updateAvailableSpaces(1L, 1);
    }

    @Test
    void testPayOrder_WrongStatus() {
        Order order = createOrder(1L, 1L, 1L, 10L, 3);

        doReturn(order).when(orderServiceImpl).getById(1L);

        boolean result = orderServiceImpl.payOrder(1L, 1);

        assertFalse(result);
        verify(orderServiceImpl, never()).updateById(any(Order.class));
    }

    @Test
    void testPayOrder_ConcurrentPayment() throws InterruptedException {
        Order order = createOrder(1L, 1L, 1L, 10L, 0);

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setHourlyRate(BigDecimal.TEN);

        AtomicInteger updateCount = new AtomicInteger(0);

        doReturn(order).when(orderServiceImpl).getById(1L);

        doAnswer(invocation -> {
            Order updatedOrder = invocation.getArgument(0);
            if (updateCount.incrementAndGet() == 1) {
                updatedOrder.setStatus(1);
                return true;
            }
            return false;
        }).when(orderServiceImpl).updateById(any(Order.class));

        when(parkingLotService.getById(1L)).thenReturn(parkingLot);
        when(paymentRecordService.save(any(PaymentRecord.class))).thenReturn(true);
        when(parkingSpaceService.releaseParkingSpace(anyLong())).thenReturn(true);
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        int threadCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    boolean result = orderServiceImpl.payOrder(1L, 1);
                    if (result) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        endLatch.await();

        assertTrue(successCount.get() <= 1, "并发支付同一订单只能成功一次，实际成功次数：" + successCount.get());
    }

    @Test
    void testCancelOrder_Success() {
        Order order = createOrder(1L, 1L, 1L, 10L, 3);

        doReturn(order).when(orderServiceImpl).getById(1L);
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));
        when(parkingSpaceService.releaseParkingSpace(anyLong())).thenReturn(true);
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        boolean result = orderServiceImpl.cancelOrder(1L);

        assertTrue(result);
        verify(orderServiceImpl).updateById(argThat(o ->
                o.getStatus() == 2 && o.getCancellationTime() != null
        ));
        verify(parkingSpaceService).releaseParkingSpace(10L);
        verify(parkingLotService).updateAvailableSpaces(1L, 1);
    }

    @Test
    void testCancelOrder_WrongStatus() {
        Order order = createOrder(1L, 1L, 1L, 10L, 1);

        doReturn(order).when(orderServiceImpl).getById(1L);

        boolean result = orderServiceImpl.cancelOrder(1L);

        assertFalse(result);
        verify(orderServiceImpl, never()).updateById(any(Order.class));
    }

    @Test
    void testCompleteOrder_Success() {
        Order order = createOrder(1L, 1L, 1L, 10L, 3);

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setHourlyRate(BigDecimal.TEN);

        doReturn(order).when(orderServiceImpl).getById(1L);
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));
        when(parkingLotService.getById(1L)).thenReturn(parkingLot);
        when(paymentRecordService.save(any(PaymentRecord.class))).thenReturn(true);
        when(parkingSpaceService.releaseParkingSpace(anyLong())).thenReturn(true);
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        boolean result = orderServiceImpl.completeOrder(1L);

        assertTrue(result);
        verify(orderServiceImpl).updateById(argThat(o ->
                o.getStatus() == 1 &&
                        o.getEndTime() != null &&
                        o.getAmount() != null &&
                        o.getAmount().compareTo(BigDecimal.ZERO) > 0
        ));
        verify(paymentRecordService).save(any(PaymentRecord.class));
        verify(parkingSpaceService).releaseParkingSpace(10L);
        verify(parkingLotService).updateAvailableSpaces(1L, 1);
    }

    @Test
    void testCompleteOrder_WrongStatus() {
        Order order = createOrder(1L, 1L, 1L, 10L, 1);

        doReturn(order).when(orderServiceImpl).getById(1L);

        boolean result = orderServiceImpl.completeOrder(1L);

        assertFalse(result);
        verify(orderServiceImpl, never()).updateById(any(Order.class));
    }

    @Test
    void testPaymentLockCleanup() throws Exception {
        Order order = createOrder(1L, 1L, 1L, 10L, 0);

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setId(1L);
        parkingLot.setHourlyRate(BigDecimal.TEN);

        doReturn(order).when(orderServiceImpl).getById(1L);
        doReturn(true).when(orderServiceImpl).updateById(any(Order.class));
        when(parkingLotService.getById(1L)).thenReturn(parkingLot);
        when(paymentRecordService.save(any(PaymentRecord.class))).thenReturn(true);
        when(parkingSpaceService.releaseParkingSpace(anyLong())).thenReturn(true);
        when(parkingLotService.updateAvailableSpaces(anyLong(), anyInt())).thenReturn(true);

        orderServiceImpl.payOrder(1L, 1);

        Field paymentLocksField = OrderServiceImpl.class.getDeclaredField("paymentLocks");
        paymentLocksField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<Long, Object> paymentLocks = (ConcurrentHashMap<Long, Object>) paymentLocksField.get(orderServiceImpl);

        assertFalse(paymentLocks.containsKey(1L), "支付完成后锁应被清理");
    }
}
