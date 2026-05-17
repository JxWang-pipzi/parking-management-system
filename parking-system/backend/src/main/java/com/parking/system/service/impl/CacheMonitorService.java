package com.parking.system.service.impl;

import com.parking.system.service.CacheService;
import com.parking.system.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 缓存监控服务
 * 定期监控缓存状态和性能
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class CacheMonitorService {
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 每5分钟监控一次缓存状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void monitorCacheStatus() {
        try {
            Map<String, Object> stats = cacheService.getCacheStatistics();
            
            double hitRate = (Double) stats.get("hitRate");
            long totalKeys = (Long) stats.get("totalKeys");
            
            log.info("缓存监控 - 命中率: {}, 总键数: {}", String.format("%.2f%%", hitRate * 100), totalKeys);
            
            // 如果命中率过低，发出告警
            if (hitRate < 0.5 && (Long) stats.get("totalRequests") > 100) {
                log.warn("缓存命中率过低: {}，建议检查缓存策略", String.format("%.2f%%", hitRate * 100));
                
                webSocketService.pushSystemBroadcast("缓存性能告警", Map.of(
                    "message", "缓存命中率过低",
                    "hitRate", hitRate,
                    "totalKeys", totalKeys
                ));
            }
            
            // 如果缓存键数量过多，发出告警
            if (totalKeys > 10000) {
                log.warn("缓存键数量过多: {}，建议清理过期缓存", totalKeys);
                
                webSocketService.pushSystemBroadcast("缓存容量告警", Map.of(
                    "message", "缓存键数量过多",
                    "totalKeys", totalKeys
                ));
            }
            
        } catch (Exception e) {
            log.error("缓存监控失败", e);
        }
    }
    
    /**
     * 每天凌晨2点执行缓存预热
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledWarmUp() {
        try {
            log.info("开始定时缓存预热...");
            cacheService.warmUp();
            log.info("定时缓存预热完成");
        } catch (Exception e) {
            log.error("定时缓存预热失败", e);
        }
    }
    
    /**
     * 每小时记录缓存统计
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void logCacheStatistics() {
        try {
            Map<String, Object> stats = cacheService.getCacheStatistics();
            
            log.info("缓存统计报告:");
            log.info("  - 缓存命中: {}", stats.get("cacheHits"));
            log.info("  - 缓存未命中: {}", stats.get("cacheMisses"));
            log.info("  - 总请求数: {}", stats.get("totalRequests"));
            log.info("  - 命中率: {}", String.format("%.2f%%", (Double) stats.get("hitRate") * 100));
            log.info("  - 总键数: {}", stats.get("totalKeys"));
            
            Map<String, Integer> keysByPrefix = (Map<String, Integer>) stats.get("keysByPrefix");
            if (keysByPrefix != null && !keysByPrefix.isEmpty()) {
                log.info("  - 按前缀统计:");
                keysByPrefix.forEach((prefix, count) -> 
                    log.info("    {} : {}", prefix, count));
            }
            
        } catch (Exception e) {
            log.error("记录缓存统计失败", e);
        }
    }
}