package com.parking.system.service.impl;

import com.parking.system.service.ParkingReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 预约清理服务
 * 定期清理过期预约
 */
@Slf4j
@Service
public class ReservationCleanupService {
    
    @Autowired
    private ParkingReservationService parkingReservationService;
    
    /**
     * 每2分钟清理一次过期预约
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void cleanupExpiredReservations() {
        try {
            int cleanedCount = parkingReservationService.cleanupExpiredReservations();
            
            if (cleanedCount > 0) {
                log.info("定时清理过期预约完成，清理了 {} 个过期预约", cleanedCount);
            } else {
                log.debug("定时清理过期预约完成，无过期预约需要清理");
            }
            
        } catch (Exception e) {
            log.error("定时清理过期预约失败", e);
        }
    }
    
    /**
     * 每小时统计预约情况
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void logReservationStatistics() {
        try {
            // 这里可以添加预约统计日志
            log.info("预约系统运行正常，定时清理任务正在执行");
            
        } catch (Exception e) {
            log.error("记录预约统计失败", e);
        }
    }
}