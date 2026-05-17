package com.parking.system.config;

import com.parking.system.service.impl.WebSocketServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 */
@Slf4j
@Component
public class WebSocketEventListener {
    
    @Autowired
    private WebSocketServiceImpl webSocketService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket连接建立: sessionId={}", sessionId);
        
        // 从header中获取用户ID
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.userOnline(userId);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID: {}", userIdStr);
            }
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket连接断开: sessionId={}", sessionId);
        
        // 从header中获取用户ID
        String userIdStr = headerAccessor.getFirstNativeHeader("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webSocketService.userOffline(userId);
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID: {}", userIdStr);
            }
        }
    }
}
