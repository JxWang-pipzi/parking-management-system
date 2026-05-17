package com.parking.system.service.impl;

import com.parking.system.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务实现
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // 在线用户集合
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();
    
    @Override
    public void pushSpaceStatusUpdate(Long spaceId, String status) {
        try {
            Map<String, Object> message = Map.of(
                "spaceId", spaceId,
                "status", status,
                "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/space/" + spaceId, message);
            log.debug("推送停车位状态更新: spaceId={}, status={}", spaceId, status);
        } catch (Exception e) {
            log.error("推送停车位状态更新失败", e);
        }
    }
    
    @Override
    public void pushParkingLotUpdate(Long lotId, Map<String, Object> data) {
        try {
            messagingTemplate.convertAndSend("/topic/lot/" + lotId, data);
            log.debug("推送停车场状态更新: lotId={}", lotId);
        } catch (Exception e) {
            log.error("推送停车场状态更新失败", e);
        }
    }
    
    @Override
    public void pushRecommendationUpdate(Long userId, Map<String, Object> recommendation) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/recommendation",
                recommendation
            );
            log.debug("推送推荐更新: userId={}", userId);
        } catch (Exception e) {
            log.error("推送推荐更新失败", e);
        }
    }
    
    @Override
    public void pushReservationUpdate(Long userId, Map<String, Object> reservation) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/reservation",
                reservation
            );
            log.debug("推送预约状态更新: userId={}", userId);
        } catch (Exception e) {
            log.error("推送预约状态更新失败", e);
        }
    }
    
    @Override
    public void pushPaymentUpdate(Long userId, Map<String, Object> payment) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/payment",
                payment
            );
            log.debug("推送支付状态更新: userId={}", userId);
        } catch (Exception e) {
            log.error("推送支付状态更新失败", e);
        }
    }
    
    @Override
    public void pushOrderUpdate(Long userId, Map<String, Object> order) {
        try {
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/order",
                order
            );
            log.debug("推送订单状态更新: userId={}", userId);
        } catch (Exception e) {
            log.error("推送订单状态更新失败", e);
        }
    }
    
    @Override
    public void pushCacheStatus(Map<String, Object> cacheStats) {
        try {
            messagingTemplate.convertAndSend("/topic/cache/status", cacheStats);
            log.debug("推送缓存状态更新");
        } catch (Exception e) {
            log.error("推送缓存状态更新失败", e);
        }
    }
    
    @Override
    public void pushParkingSpaceUpdate(Object parkingSpace) {
        try {
            messagingTemplate.convertAndSend("/topic/space/update", parkingSpace);
            log.debug("推送停车位更新");
        } catch (Exception e) {
            log.error("推送停车位更新失败", e);
        }
    }
    
    @Override
    public void pushUserMessage(Long userId, String messageType, Object message) {
        try {
            Map<String, Object> payload = Map.of(
                "type", messageType,
                "data", message,
                "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/message",
                payload
            );
            log.debug("推送用户消息: userId={}, type={}", userId, messageType);
        } catch (Exception e) {
            log.error("推送用户消息失败", e);
        }
    }
    
    @Override
    public void pushSystemBroadcast(String messageType, Object message) {
        try {
            Map<String, Object> payload = Map.of(
                "type", messageType,
                "data", message,
                "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/system", payload);
            log.debug("推送系统广播: type={}", messageType);
        } catch (Exception e) {
            log.error("推送系统广播失败", e);
        }
    }
    
    @Override
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    @Override
    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }
    
    /**
     * 用户上线
     */
    public void userOnline(Long userId) {
        onlineUsers.add(userId);
        log.info("用户上线: userId={}, 当前在线用户数: {}", userId, onlineUsers.size());
    }
    
    /**
     * 用户下线
     */
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        log.info("用户下线: userId={}, 当前在线用户数: {}", userId, onlineUsers.size());
    }
}
