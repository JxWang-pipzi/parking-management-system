package com.parking.system.service.impl;

import com.parking.system.entity.ParkingLot;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停车场监控服务
 * 定时推送停车场状态更新
 */
@Slf4j
@Service
public class ParkingLotMonitorService {
    
    @Autowired
    private ParkingLotService parkingLotService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 每30秒推送停车场统计信息
     */
    @Scheduled(fixedRate = 30000)
    public void pushParkingLotStatistics() {
        try {
            List<ParkingLot> lots = parkingLotService.list();
            for (ParkingLot lot : lots) {
                Map<String, Object> data = new HashMap<>();
                data.put("lotId", lot.getId());
                data.put("lotName", lot.getName());
                data.put("totalSpaces", lot.getTotalSpaces());
                data.put("availableSpaces", lot.getAvailableSpaces());
                data.put("occupancyRate", calculateOccupancyRate(lot));
                data.put("timestamp", System.currentTimeMillis());
                
                webSocketService.pushParkingLotUpdate(lot.getId(), data);
            }
            log.debug("推送停车场统计信息完成");
        } catch (Exception e) {
            log.error("推送停车场统计信息失败", e);
        }
    }
    
    /**
     * 计算占用率
     */
    private double calculateOccupancyRate(ParkingLot lot) {
        if (lot.getTotalSpaces() == 0) {
            return 0.0;
        }
        int occupiedSpaces = lot.getTotalSpaces() - lot.getAvailableSpaces();
        return (double) occupiedSpaces / lot.getTotalSpaces() * 100;
    }
}
