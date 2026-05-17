package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    Order createOrder(Long userId, Long parkingLotId, Long parkingSpaceId, String plateNumber);

    boolean payOrder(Long orderId, Integer paymentMethod);

    boolean cancelOrder(Long orderId);

    boolean completeOrder(Long orderId);

    List<Order> getOrdersByUserId(Long userId, Integer status);

}