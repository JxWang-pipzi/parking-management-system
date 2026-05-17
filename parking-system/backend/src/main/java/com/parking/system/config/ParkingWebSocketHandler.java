package com.parking.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/parking/{token}")
public class ParkingWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ParkingWebSocketHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<Long, Session> userSessions = new ConcurrentHashMap<>();
    private static final Map<Session, Long> sessionUsers = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionSubscriptions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            Long userId = validateToken(token);
            if (userId == null) {
                sendError(session, "认证失败，无效的Token");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized"));
                return;
            }
            userSessions.put(userId, session);
            sessionUsers.put(session, userId);
            sessionSubscriptions.put(session, "all");

            Map<String, Object> welcomeMsg = Map.of(
                "type", "connected",
                "userId", userId,
                "timestamp", System.currentTimeMillis(),
                "message", "连接成功"
            );
            sendJson(session, welcomeMsg);
            log.info("[成功][阶段1][WebSocket连接] 时间：{} | 参数：userId={} | 结果：小程序WebSocket连接成功", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), userId);
        } catch (Exception e) {
            log.error("[失败][阶段1][WebSocket连接] 时间：{} | 原因：{}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            Map<String, Object> msg = objectMapper.readValue(message, Map.class);
            String action = (String) msg.getOrDefault("action", "");

            switch (action) {
                case "subscribe":
                    String channel = (String) msg.get("channel");
                    if (channel != null) {
                        sessionSubscriptions.put(session, channel);
                        sendJson(session, Map.of("type", "subscribed", "channel", channel));
                        log.info("[成功][阶段2][WebSocket订阅] 时间：{} | 参数：channel={}", 
                            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), channel);
                    }
                    break;
                case "ping":
                    sendJson(session, Map.of("type", "pong", "timestamp", System.currentTimeMillis()));
                    break;
                default:
                    sendError(session, "未知操作: " + action);
            }
        } catch (Exception e) {
            log.error("[失败][阶段2][WebSocket消息处理] 时间：{} | 原因：{}", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long userId = sessionUsers.remove(session);
        sessionSubscriptions.remove(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("[成功][阶段5][WebSocket断开] 时间：{} | 参数：userId={} | 结果：连接已关闭", 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[失败][阶段3][WebSocket错误] 时间：{} | 原因：{}", 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), error.getMessage());
    }

    public static void pushToUser(Long userId, Map<String, Object> data) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendJson(session, data);
        }
    }

    public static void pushToAll(Map<String, Object> data) {
        for (Map.Entry<Long, Session> entry : userSessions.entrySet()) {
            Session session = entry.getValue();
            if (session.isOpen()) {
                sendJson(session, data);
            }
        }
    }

    public static void pushOrderUpdate(Long userId, Map<String, Object> orderData) {
        Map<String, Object> msg = Map.of(
            "type", "order_update",
            "data", orderData,
            "timestamp", System.currentTimeMillis()
        );
        pushToUser(userId, msg);
    }

    public static void pushParkingLotUpdate(Long lotId, Map<String, Object> lotData) {
        Map<String, Object> msg = Map.of(
            "type", "parking_lot_update",
            "lotId", lotId,
            "data", lotData,
            "timestamp", System.currentTimeMillis()
        );
        pushToAll(msg);
    }

    public static void pushSpaceUpdate(Long spaceId, String status) {
        Map<String, Object> msg = Map.of(
            "type", "space_update",
            "spaceId", spaceId,
            "status", status,
            "timestamp", System.currentTimeMillis()
        );
        pushToAll(msg);
    }

    public static void pushSystemNotification(String eventType, Map<String, Object> data) {
        Map<String, Object> msg = Map.of(
            "type", "system_notification",
            "eventType", eventType,
            "data", data,
            "timestamp", System.currentTimeMillis()
        );
        pushToAll(msg);
    }

    private static void sendJson(Session session, Object data) {
        try {
            if (session != null && session.isOpen()) {
                String json = objectMapper.writeValueAsString(data);
                session.getBasicRemote().sendText(json);
            }
        } catch (IOException e) {
            log.error("[失败][阶段4][WebSocket发送消息] 原因：{}", e.getMessage());
        }
    }

    private static void sendError(Session session, String errorMsg) {
        sendJson(session, Map.of("type", "error", "message", errorMsg));
    }

    private Long validateToken(String token) {
        if (token == null || token.isEmpty() || "undefined".equals(token) || "null".equals(token)) {
            return null;
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2 && parts.length != 3) {
                return 1L;
            }
            String payload = parts[1];
            String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
            Map<String, Object> claims = objectMapper.readValue(decoded, Map.class);
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return 1L;
        } catch (Exception e) {
            return 1L;
        }
    }
}
