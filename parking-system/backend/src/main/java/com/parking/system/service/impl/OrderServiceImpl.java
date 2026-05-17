package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.Order;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.mapper.OrderMapper;
import com.parking.system.service.OrderService;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import com.parking.system.service.PaymentRecordService;
import com.parking.system.service.WebSocketService;
import com.parking.system.config.ParkingWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private PaymentRecordService paymentRecordService;

    @Resource
    private WebSocketService webSocketService;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final ConcurrentHashMap<Long, Object> paymentLocks = new ConcurrentHashMap<>();
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(LOG_TIME_FORMAT);
    }

    private static final int FREE_DURATION_MINUTES = 15;

    private boolean isFirstParkingAtLot(Long userId, Long parkingLotId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("parking_lot_id", parkingLotId)
               .in("status", 1, 3);
        return count(wrapper) <= 1;
    }

    private java.math.BigDecimal calculateAmount(long durationMinutes, java.math.BigDecimal hourlyRate, boolean isFirstParking) {
        if (isFirstParking && durationMinutes <= FREE_DURATION_MINUTES) {
            return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        java.math.BigDecimal hours = java.math.BigDecimal.valueOf(durationMinutes)
                .divide(java.math.BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        return hours.multiply(hourlyRate).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, Long parkingLotId, Long parkingSpaceId, String plateNumber) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][创建订单] 时间：{} | 参数：userId={}, parkingLotId={}, parkingSpaceId={}, plateNumber={} | 开始创建订单", 
                time, userId, parkingLotId, parkingSpaceId, plateNumber);
        
        try {
            if (parkingSpaceId == null) {
                parkingSpaceId = parkingSpaceService.atomicAllocateSpace(parkingLotId, 1);
                if (parkingSpaceId != null) {
                    log.info("[成功][阶段2-核心操作][创建订单] 时间：{} | 原子分配车位: parkingSpaceId={}", time, parkingSpaceId);
                } else {
                    log.warn("[失败][阶段2-核心操作][创建订单] 时间：{} | 原因：无空闲车位 | 参数：parkingLotId={}", time, parkingLotId);
                    return null;
                }
            } else {
                QueryWrapper<com.parking.system.entity.ParkingSpace> allocWrapper = new QueryWrapper<>();
                allocWrapper.eq("id", parkingSpaceId).eq("status", 0);
                com.parking.system.entity.ParkingSpace allocUpdate = new com.parking.system.entity.ParkingSpace();
                allocUpdate.setId(parkingSpaceId);
                allocUpdate.setStatus(1);
                if (!parkingSpaceService.update(allocUpdate, allocWrapper)) {
                    log.warn("[失败][阶段2-核心操作][创建订单] 时间：{} | 原因：指定车位已被占用 | 参数：parkingSpaceId={}", time, parkingSpaceId);
                    return null;
                }
            }

            Order order = new Order();
            order.setUserId(userId);
            order.setParkingLotId(parkingLotId);
            order.setParkingSpaceId(parkingSpaceId);
            order.setPlateNumber(plateNumber);
            order.setStartTime(new Date());
            order.setStatus(3);
            order.setAmount(new java.math.BigDecimal(0));

            if (save(order)) {
                parkingLotService.updateAvailableSpaces(parkingLotId, -1);

                webSocketService.pushSystemBroadcast("新订单", Map.of(
                    "orderId", order.getId(),
                    "userId", userId,
                    "parkingLotId", parkingLotId,
                    "parkingSpaceId", parkingSpaceId,
                    "plateNumber", plateNumber,
                    "type", "ORDER_CREATED"
                ));
                webSocketService.pushParkingLotUpdate(parkingLotId, Map.of(
                    "event", "space_occupied",
                    "parkingSpaceId", parkingSpaceId
                ));
                ParkingWebSocketHandler.pushOrderUpdate(userId, Map.of(
                    "orderId", order.getId(),
                    "status", 3,
                    "type", "ORDER_CREATED",
                    "parkingLotId", parkingLotId,
                    "plateNumber", plateNumber
                ));
                ParkingWebSocketHandler.pushParkingLotUpdate(parkingLotId, Map.of(
                    "event", "space_occupied",
                    "parkingSpaceId", parkingSpaceId
                ));

                log.info("[成功][阶段4-结果反馈][创建订单] 时间：{} | 参数：userId={}, parkingLotId={} | 结果：订单创建成功，orderId={}", 
                        time, userId, parkingLotId, order.getId());
                return order;
            }
            
            log.warn("[失败][阶段4-结果反馈][创建订单] 时间：{} | 原因：订单保存失败 | 参数：userId={}, parkingLotId={}", 
                    time, userId, parkingLotId);
            return null;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][创建订单] 时间：{} | 原因：{} | 参数：userId={}, parkingLotId={}", 
                    time, e.getMessage(), userId, parkingLotId);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Integer paymentMethod) {
        String time = getCurrentTime();
        
        synchronized (getPaymentLock(orderId)) {
            try {
                Order order = getById(orderId);
                if (order == null) {
                    log.warn("[失败][阶段1-入口][支付订单] 时间：{} | 原因：订单不存在 | 参数：orderId={}", time, orderId);
                    return false;
                }
                
                if (order.getStatus() != 0) {
                    log.warn("[失败][阶段2-核心操作][支付订单] 时间：{} | 原因：订单状态不允许支付，当前状态={} | 参数：orderId={}", 
                            time, order.getStatus(), orderId);
                    return false;
                }
                
                log.info("[成功][阶段1-入口][支付订单] 时间：{} | 参数：orderId={}, paymentMethod={} | 开始处理支付", 
                        time, orderId, paymentMethod);
                
                try {
                    long duration = (System.currentTimeMillis() - order.getStartTime().getTime()) / (1000 * 60);
                    com.parking.system.entity.ParkingLot parkingLot = parkingLotService.getById(order.getParkingLotId());
                    java.math.BigDecimal hourlyRate = (parkingLot != null && parkingLot.getHourlyRate() != null) ?
                        parkingLot.getHourlyRate() : java.math.BigDecimal.TEN;
                    boolean isFirst = isFirstParkingAtLot(order.getUserId(), order.getParkingLotId());
                    java.math.BigDecimal amount = calculateAmount(duration, hourlyRate, isFirst);

                    com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Order> casWrapper =
                            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
                    casWrapper.eq("id", orderId)
                              .eq("status", 0)
                              .set("end_time", new Date())
                              .set("duration", (int) duration)
                              .set("amount", amount)
                              .set("status", 1)
                              .set("payment_time", new Date())
                              .set("completion_time", new Date());

                    log.info("[成功][阶段2-核心操作][支付订单] 时间：{} | 参数：orderId={}, duration={}分钟, hourlyRate={}, amount={}, freePeriod={}分钟", 
                            time, orderId, duration, hourlyRate, amount, FREE_DURATION_MINUTES);

                    if (update(new Order(), casWrapper)) {
                        PaymentRecord paymentRecord = new PaymentRecord();
                        paymentRecord.setOrderId(orderId);
                        paymentRecord.setUserId(order.getUserId());
                        paymentRecord.setAmount(amount);
                        paymentRecord.setPaymentMethod(paymentMethod);
                        paymentRecord.setTransactionId(UUID.randomUUID().toString());
                        paymentRecord.setStatus(1);

                        if (paymentRecordService.save(paymentRecord)) {
                            if (order.getParkingSpaceId() != null) {
                                parkingSpaceService.releaseParkingSpace(order.getParkingSpaceId());
                                parkingLotService.updateAvailableSpaces(order.getParkingLotId(), 1);
                            }

                            webSocketService.pushSystemBroadcast("订单支付", Map.of(
                                "orderId", orderId,
                                "userId", order.getUserId(),
                                "parkingLotId", order.getParkingLotId(),
                                "parkingSpaceId", order.getParkingSpaceId(),
                                "amount", amount,
                                "type", "ORDER_PAID"
                            ));
                            webSocketService.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                                "event", "order_paid",
                                "parkingSpaceId", order.getParkingSpaceId()
                            ));
                            ParkingWebSocketHandler.pushOrderUpdate(order.getUserId(), Map.of(
                                "orderId", orderId,
                                "status", 1,
                                "type", "ORDER_PAID",
                                "amount", amount
                            ));
                            ParkingWebSocketHandler.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                                "event", "order_paid",
                                "parkingSpaceId", order.getParkingSpaceId()
                            ));

                            log.info("[成功][阶段4-结果反馈][支付订单] 时间：{} | 参数：orderId={}, amount={} | 结果：支付成功", 
                                    time, orderId, amount);
                            return true;
                        }
                    }
                    
                    log.warn("[失败][阶段4-结果反馈][支付订单] 时间：{} | 原因：CAS更新失败，可能并发冲突 | 参数：orderId={}", time, orderId);
                    return false;
                } catch (Exception e) {
                    log.error("[失败][阶段2-核心操作][支付订单] 时间：{} | 原因：{} | 参数：orderId={}", 
                            time, e.getMessage(), orderId);
                    throw e;
                }
            } finally {
                paymentLocks.remove(orderId);
            }
        }
    }
    
    private Object getPaymentLock(Long orderId) {
        return paymentLocks.computeIfAbsent(orderId, k -> new Object());
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        String time = getCurrentTime();
        
        Order order = getById(orderId);
        if (order == null) {
            log.warn("[失败][阶段1-入口][取消订单] 时间：{} | 原因：订单不存在 | 参数：orderId={}", time, orderId);
            return false;
        }
        
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            log.warn("[失败][阶段2-核心操作][取消订单] 时间：{} | 原因：订单状态不允许取消，当前状态={} | 参数：orderId={}", 
                    time, order.getStatus(), orderId);
            return false;
        }
        
        log.info("[成功][阶段1-入口][取消订单] 时间：{} | 参数：orderId={} | 开始处理取消", time, orderId);

        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Order> casWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        casWrapper.eq("id", orderId)
                  .in("status", 0, 3)
                  .set("status", 2)
                  .set("cancellation_time", new Date());
        
        try {
            if (update(new Order(), casWrapper)) {
                parkingSpaceService.releaseParkingSpace(order.getParkingSpaceId());
                parkingLotService.updateAvailableSpaces(order.getParkingLotId(), 1);

                webSocketService.pushSystemBroadcast("订单取消", Map.of(
                    "orderId", orderId,
                    "userId", order.getUserId(),
                    "parkingLotId", order.getParkingLotId(),
                    "parkingSpaceId", order.getParkingSpaceId(),
                    "type", "ORDER_CANCELLED"
                ));
                webSocketService.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                    "event", "space_released",
                    "parkingSpaceId", order.getParkingSpaceId()
                ));
                ParkingWebSocketHandler.pushOrderUpdate(order.getUserId(), Map.of(
                    "orderId", orderId,
                    "status", 2,
                    "type", "ORDER_CANCELLED"
                ));
                ParkingWebSocketHandler.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                    "event", "space_released",
                    "parkingSpaceId", order.getParkingSpaceId()
                ));

                log.info("[成功][阶段4-结果反馈][取消订单] 时间：{} | 参数：orderId={} | 结果：取消成功", time, orderId);
                return true;
            }
            
            log.warn("[失败][阶段4-结果反馈][取消订单] 时间：{} | 原因：CAS更新失败，可能并发冲突 | 参数：orderId={}", time, orderId);
            return false;
        } catch (Exception e) {
            log.error("[失败][阶段3-分支操作][取消订单] 时间：{} | 原因：{} | 参数：orderId={}", 
                    time, e.getMessage(), orderId);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean completeOrder(Long orderId) {
        String time = getCurrentTime();

        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Order> casWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        casWrapper.eq("id", orderId).eq("status", 3);

        Order order = getOne(new QueryWrapper<Order>().eq("id", orderId));
        if (order == null) {
            log.warn("[失败][阶段1-入口][完成订单] 时间：{} | 原因：订单不存在 | 参数：orderId={}", time, orderId);
            return false;
        }

        if (order.getStatus() != 3) {
            log.warn("[失败][阶段2-核心操作][完成订单] 时间：{} | 原因：订单状态不允许完成，当前状态={} | 参数：orderId={}",
                    time, order.getStatus(), orderId);
            return false;
        }

        log.info("[成功][阶段1-入口][完成订单] 时间：{} | 参数：orderId={} | 开始处理", time, orderId);

        try {
            Date endTime = new Date();
            long duration = Math.max(1, (endTime.getTime() - order.getStartTime().getTime()) / (1000 * 60));
            com.parking.system.entity.ParkingLot parkingLot = parkingLotService.getById(order.getParkingLotId());
            java.math.BigDecimal hourlyRate = (parkingLot != null && parkingLot.getHourlyRate() != null) ?
                    parkingLot.getHourlyRate() : java.math.BigDecimal.TEN;
            boolean isFirst = isFirstParkingAtLot(order.getUserId(), order.getParkingLotId());
            java.math.BigDecimal amount = calculateAmount(duration, hourlyRate, isFirst);

            casWrapper.set("end_time", endTime)
                      .set("duration", (int) duration)
                      .set("amount", amount)
                      .set("actual_amount", amount)
                      .set("payment_time", endTime)
                      .set("completion_time", endTime)
                      .set("status", 1);

            log.info("[成功][阶段2-核心操作][完成订单] 时间：{} | 参数：orderId={}, duration={}分钟, hourlyRate={}, amount={}, freePeriod={}分钟",
                    time, orderId, duration, hourlyRate, amount, FREE_DURATION_MINUTES);

            if (update(new Order(), casWrapper)) {
                PaymentRecord paymentRecord = new PaymentRecord();
                paymentRecord.setOrderId(orderId);
                paymentRecord.setUserId(order.getUserId());
                paymentRecord.setAmount(amount);
                paymentRecord.setPaymentMethod(1);
                paymentRecord.setTransactionId(UUID.randomUUID().toString());
                paymentRecord.setStatus(1);
                paymentRecordService.save(paymentRecord);

                if (order.getParkingSpaceId() != null) {
                    parkingSpaceService.releaseParkingSpace(order.getParkingSpaceId());
                    parkingLotService.updateAvailableSpaces(order.getParkingLotId(), 1);
                }

                webSocketService.pushSystemBroadcast("订单完成", Map.of(
                    "orderId", orderId,
                    "userId", order.getUserId(),
                    "parkingLotId", order.getParkingLotId(),
                    "parkingSpaceId", order.getParkingSpaceId(),
                    "type", "ORDER_COMPLETED"
                ));
                webSocketService.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                    "event", "space_released",
                    "parkingSpaceId", order.getParkingSpaceId()
                ));
                ParkingWebSocketHandler.pushOrderUpdate(order.getUserId(), Map.of(
                    "orderId", orderId,
                    "status", 1,
                    "type", "ORDER_COMPLETED"
                ));
                ParkingWebSocketHandler.pushParkingLotUpdate(order.getParkingLotId(), Map.of(
                    "event", "space_released",
                    "parkingSpaceId", order.getParkingSpaceId()
                ));

                log.info("[成功][阶段4-结果反馈][完成订单] 时间：{} | 参数：orderId={} | 结果：完成成功，车位已释放", time, orderId);
                return true;
            }

            log.warn("[失败][阶段4-结果反馈][完成订单] 时间：{} | 原因：CAS更新失败，可能并发冲突 | 参数：orderId={}", time, orderId);
            return false;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][完成订单] 时间：{} | 原因：{} | 参数：orderId={}",
                    time, e.getMessage(), orderId);
            throw e;
        }
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId, Integer status) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][查询订单] 时间：{} | 参数：userId={}, status={} | 开始查询订单", 
                time, userId, status);
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            List<Order> orders = list(wrapper);
            
            for (Order order : orders) {
                if (order.getParkingLotId() != null) {
                    com.parking.system.entity.ParkingLot lot = parkingLotService.getById(order.getParkingLotId());
                    if (lot != null) {
                        order.setParkingLotName(lot.getName());
                    }
                }
            }
            
            log.info("[成功][阶段4-结果反馈][查询订单] 时间：{} | 参数：userId={}, status={} | 结果：查询到{}条订单", 
                    time, userId, status, orders.size());
            return orders;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][查询订单] 时间：{} | 原因：{} | 参数：userId={}, status={}", 
                    time, e.getMessage(), userId, status);
            throw e;
        }
    }

}
