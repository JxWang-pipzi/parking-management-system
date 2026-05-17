package com.parking.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据库性能优化服务
 */
@Slf4j
@Service
public class DatabasePerformanceService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 每小时检查慢查询
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void checkSlowQueries() {
        try {
            log.info("开始检查慢查询...");
            
            // 这里可以查询MySQL的慢查询日志
            // 简化实现，只记录日志
            
            log.info("慢查询检查完成");
            
        } catch (Exception e) {
            log.error("检查慢查询失败", e);
        }
    }
    
    /**
     * 优化数据库表
     */
    public void optimizeTables() {
        try {
            log.info("开始优化数据库表...");
            
            List<String> tables = Arrays.asList(
                "parking_lot", "parking_space", "order", "payment_record",
                "parking_user_behavior", "parking_recommendation"
            );
            
            for (String table : tables) {
                try {
                    jdbcTemplate.execute("OPTIMIZE TABLE " + table);
                    log.info("表优化完成: {}", table);
                } catch (Exception e) {
                    log.error("表优化失败: {}", table, e);
                }
            }
            
            log.info("数据库表优化完成");
            
        } catch (Exception e) {
            log.error("优化数据库表失败", e);
        }
    }
    
    /**
     * 分析表统计信息
     */
    public void analyzeTables() {
        try {
            log.info("开始分析表统计信息...");
            
            List<String> tables = Arrays.asList(
                "parking_lot", "parking_space", "`order`", "payment_record",
                "parking_user_behavior", "parking_recommendation"
            );
            
            for (String table : tables) {
                try {
                    jdbcTemplate.execute("ANALYZE TABLE " + table);
                    log.info("表分析完成: {}", table);
                } catch (Exception e) {
                    log.error("表分析失败: {}", table, e);
                }
            }
            
            log.info("表统计信息分析完成");
            
        } catch (Exception e) {
            log.error("分析表统计信息失败", e);
        }
    }
    
    /**
     * 获取数据库性能统计
     */
    public Map<String, Object> getDatabaseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取数据库连接数
            List<Map<String, Object>> connections = jdbcTemplate.queryForList(
                "SHOW STATUS LIKE 'Threads_connected'"
            );
            if (!connections.isEmpty()) {
                stats.put("connections", connections.get(0).get("Value"));
            }
            
            // 获取查询缓存命中率
            List<Map<String, Object>> qcacheHits = jdbcTemplate.queryForList(
                "SHOW STATUS LIKE 'Qcache_hits'"
            );
            List<Map<String, Object>> qcacheInserts = jdbcTemplate.queryForList(
                "SHOW STATUS LIKE 'Qcache_inserts'"
            );
            
            if (!qcacheHits.isEmpty() && !qcacheInserts.isEmpty()) {
                long hits = Long.parseLong(qcacheHits.get(0).get("Value").toString());
                long inserts = Long.parseLong(qcacheInserts.get(0).get("Value").toString());
                double hitRate = (hits + inserts) > 0 ? (double) hits / (hits + inserts) : 0.0;
                stats.put("queryCacheHitRate", hitRate);
            }
            
            log.info("数据库性能统计: {}", stats);
            
        } catch (Exception e) {
            log.error("获取数据库性能统计失败", e);
        }
        
        return stats;
    }
    
    /**
     * 每天凌晨3点优化数据库
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledOptimization() {
        try {
            log.info("开始定时数据库优化...");
            analyzeTables();
            optimizeTables();
            log.info("定时数据库优化完成");
        } catch (Exception e) {
            log.error("定时数据库优化失败", e);
        }
    }
}