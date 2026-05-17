package com.parking.system.controller;

import com.parking.system.service.WebSocketService;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket控制器
 * 处理客户端发送的WebSocket消息
 */
@Slf4j
@Controller
public class WebSocketController {
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private ParkingLotService parkingLotService;
    
    @Autowired
    private RecommendationService recommendationService;
    
    /**
     * 处理客户端订阅停车场更新的请求
     */
    @MessageMapping("/subscribe-parking-lot")
    public void subscribeParkingLot(@Payload Map<String, Object> message, 
                                   SimpMessageHeaderAccessor headerAccessor,
                                   Principal principal) {
        try {
            Long parkingLotId = Long.parseLong(message.get("parkingLotId").toString());
            Long userId = extractUserId(principal);
            
            log.info("用户订阅停车场更新: userId={}, parkingLotId={}", userId, parkingLotId);
            
            // 立即发送当前停车场状态
            var parkingLot = parkingLotService.getById(parkingLotId);
            if (parkingLot != null) {
                webSocketService.pushUserMessage(userId, "停车场状态", parkingLot);
            }
            
        } catch (Exception e) {
            log.error("处理停车场订阅失败", e);
        }
    }
    
    /**
     * 处理客户端请求推荐更新
     */
    @MessageMapping("/request-recommendations")
    public void requestRecommendations(@Payload Map<String, Object> message,
                                     Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Double latitude = message.get("latitude") != null ? 
                Double.parseDouble(message.get("latitude").toString()) : null;
            Double longitude = message.get("longitude") != null ? 
                Double.parseDouble(message.get("longitude").toString()) : null;
            
            log.info("用户请求推荐更新: userId={}, lat={}, lng={}", userId, latitude, longitude);
            
            // 获取推荐并推送
            var recommendations = recommendationService.getHybridRecommendations(
                userId, latitude, longitude, 10);
            
            Map<String, Object> recommendationData = new HashMap<>();
            recommendationData.put("recommendations", recommendations);
            recommendationData.put("count", recommendations.size());
            
            webSocketService.pushRecommendationUpdate(userId, recommendationData);
            
        } catch (Exception e) {
            log.error("处理推荐请求失败", e);
        }
    }
    
    /**
     * 处理预约相关消息
     */
    @MessageMapping("/reservation-action")
    public void handleReservationAction(@Payload Map<String, Object> message,
                                       Principal principal) {
        try {
            Long userId = extractUserId(principal);
            String action = (String) message.get("action");
            Long spaceId = Long.parseLong(message.get("spaceId").toString());
            
            log.info("处理预约操作: userId={}, action={}, spaceId={}", userId, action, spaceId);
            
            switch (action) {
                case "reserve":
                    Integer duration = message.get("duration") != null ? 
                        Integer.parseInt(message.get("duration").toString()) : null;
                    webSocketService.pushUserMessage(userId, "预约请求已接收", Map.of(
                        "spaceId", spaceId,
                        "action", action
                    ));
                    break;
                    
                case "cancel":
                    webSocketService.pushUserMessage(userId, "取消预约请求已接收", Map.of(
                        "spaceId", spaceId,
                        "action", action
                    ));
                    break;
                    
                case "extend":
                    Integer additionalMinutes = message.get("additionalMinutes") != null ? 
                        Integer.parseInt(message.get("additionalMinutes").toString()) : null;
                    webSocketService.pushUserMessage(userId, "延长预约请求已接收", Map.of(
                        "spaceId", spaceId,
                        "action", action,
                        "additionalMinutes", additionalMinutes
                    ));
                    break;
                    
                default:
                    log.warn("未知的预约操作: {}", action);
            }
            
        } catch (Exception e) {
            log.error("处理预约操作失败", e);
        }
    }
    
    /**
     * 处理客户端心跳消息
     */
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload Map<String, Object> message,
                               Principal principal) {
        try {
            Long userId = extractUserId(principal);
            
            // 响应心跳
            webSocketService.pushUserMessage(userId, "心跳响应", 
                Map.of("timestamp", System.currentTimeMillis()));
            
        } catch (Exception e) {
            log.error("处理心跳失败", e);
        }
    }
    
    /**
     * 处理客户端位置更新
     */
    @MessageMapping("/location-update")
    public void handleLocationUpdate(@Payload Map<String, Object> message,
                                   Principal principal) {
        try {
            Long userId = extractUserId(principal);
            Double latitude = Double.parseDouble(message.get("latitude").toString());
            Double longitude = Double.parseDouble(message.get("longitude").toString());
            
            log.debug("用户位置更新: userId={}, lat={}, lng={}", userId, latitude, longitude);
            
            // 基于新位置更新推荐
            var recommendations = recommendationService.getHybridRecommendations(
                userId, latitude, longitude, 5);
            
            Map<String, Object> recommendationData = new HashMap<>();
            recommendationData.put("recommendations", recommendations);
            recommendationData.put("count", recommendations.size());
            
            webSocketService.pushRecommendationUpdate(userId, recommendationData);
            
        } catch (Exception e) {
            log.error("处理位置更新失败", e);
        }
    }
    
    /**
     * 从Principal中提取用户ID
     */
    private Long extractUserId(Principal principal) {
        if (principal == null) {
            return null;
        }
        
        try {
            return Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            log.warn("无法解析用户ID: {}", principal.getName());
            return null;
        }
    }
}