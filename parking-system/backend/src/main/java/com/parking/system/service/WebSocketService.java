package com.parking.system.service;

import java.util.Map;

/**
 * WebSocket服务接口
 * 提供实时数据推送功能
 */
public interface WebSocketService {
    
    /**
     * 推送停车位状态更新
     */
    void pushSpaceStatusUpdate(Long spaceId, String status);
    
    /**
     * 推送停车场状态更新
     */
    void pushParkingLotUpdate(Long lotId, Map<String, Object> data);
    
    /**
     * 推送推荐更新
     */
    void pushRecommendationUpdate(Long userId, Map<String, Object> recommendation);
    
    /**
     * 推送预约状态更新
     */
    void pushReservationUpdate(Long userId, Map<String, Object> reservation);
    
    /**
     * 推送支付状态更新
     */
    void pushPaymentUpdate(Long userId, Map<String, Object> payment);
    
    /**
     * 推送订单状态更新
     */
    void pushOrderUpdate(Long userId, Map<String, Object> order);
    
    /**
     * 推送缓存状态更新
     */
    void pushCacheStatus(Map<String, Object> cacheStats);
    
    /**
     * 推送停车位更新
     */
    void pushParkingSpaceUpdate(Object parkingSpace);
    
    /**
     * 推送用户消息
     */
    void pushUserMessage(Long userId, String messageType, Object message);
    
    /**
     * 推送系统广播
     */
    void pushSystemBroadcast(String messageType, Object message);
    
    /**
     * 获取在线用户数
     */
    int getOnlineUserCount();
    
    /**
     * 检查用户是否在线
     */
    boolean isUserOnline(Long userId);
}
