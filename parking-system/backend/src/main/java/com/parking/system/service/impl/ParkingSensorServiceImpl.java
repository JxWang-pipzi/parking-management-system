package com.parking.system.service.impl;

import com.parking.system.entity.ParkingSensor;
import com.parking.system.entity.ParkingSensorData;
import com.parking.system.service.ParkingSensorService;
import com.parking.system.service.DataFusionService;
import com.parking.system.mapper.ParkingSensorMapper;
import com.parking.system.mapper.ParkingSensorDataMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.*;

@Slf4j
@Service
public class ParkingSensorServiceImpl implements ParkingSensorService {
    
    @Autowired
    private ParkingSensorMapper sensorMapper;
    
    @Autowired
    private ParkingSensorDataMapper sensorDataMapper;
    
    @Autowired
    private DataFusionService dataFusionService;
    
    @Override
    public List<ParkingSensor> getAllSensors() {
        try {
            List<ParkingSensor> sensors = sensorMapper.selectList(null);
            return sensors != null ? sensors : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取传感器列表失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public ParkingSensor getSensorById(Long id) {
        return sensorMapper.selectById(id);
    }
    
    @Override
    public List<ParkingSensor> getSensorsByParkingLot(Long parkingLotId) {
        QueryWrapper<ParkingSensor> wrapper = new QueryWrapper<>();
        wrapper.eq("parking_lot_id", parkingLotId);
        return sensorMapper.selectList(wrapper);
    }
    
    @Override
    public boolean createSensor(ParkingSensor sensor) {
        sensor.setCreateTime(new Date());
        sensor.setUpdateTime(new Date());
        sensor.setStatus(1);
        sensor.setDataQuality(100);
        return sensorMapper.insert(sensor) > 0;
    }
    
    @Override
    public boolean updateSensor(ParkingSensor sensor) {
        sensor.setUpdateTime(new Date());
        return sensorMapper.updateById(sensor) > 0;
    }
    
    @Override
    public boolean deleteSensor(Long id) {
        return sensorMapper.deleteById(id) > 0;
    }
    
    @Override
    public boolean receiveSensorData(ParkingSensorData data) {
        data.setCollectTime(new Date());
        data.setProcessTime(new Date());
        data.setCreateTime(new Date());
        
        data = dataFusionService.processAnomaly(data);
        data = dataFusionService.fillMissingValue(data);
        data = dataFusionService.filterNoise(data);
        data.setDataQuality(dataFusionService.calculateDataQuality(data));
        
        boolean inserted = sensorDataMapper.insert(data) > 0;
        
        if (inserted) {
            updateSensorStatus(data.getSensorId(), data);
        }
        
        return inserted;
    }
    
    @Override
    public List<ParkingSensorData> getSensorData(Long sensorId, Long startTime, Long endTime) {
        QueryWrapper<ParkingSensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("sensor_id", sensorId);
        
        if (startTime != null) {
            wrapper.ge("collect_time", new Date(startTime));
        }
        if (endTime != null) {
            wrapper.le("collect_time", new Date(endTime));
        }
        
        wrapper.orderByDesc("collect_time");
        return sensorDataMapper.selectList(wrapper);
    }
    
    @Override
    public Map<String, Object> getSensorQuality(Long sensorId) {
        Map<String, Object> quality = new HashMap<>();
        
        ParkingSensor sensor = sensorMapper.selectById(sensorId);
        if (sensor == null) {
            return quality;
        }
        
        quality.put("sensorId", sensorId);
        quality.put("sensorCode", sensor.getSensorCode());
        quality.put("currentQuality", sensor.getDataQuality());
        quality.put("status", sensor.getStatus());
        
        List<ParkingSensorData> recentData = getSensorData(sensorId, 
            System.currentTimeMillis() - 3600000, null);
        
        if (!recentData.isEmpty()) {
            double avgQuality = recentData.stream()
                .mapToInt(ParkingSensorData::getDataQuality)
                .average()
                .orElse(0);
            quality.put("avgQuality", avgQuality);
            
            long anomalyCount = recentData.stream()
                .filter(d -> d.getIsAnomaly() != null && d.getIsAnomaly() == 1)
                .count();
            quality.put("anomalyCount", anomalyCount);
            quality.put("anomalyRate", (double) anomalyCount / recentData.size() * 100);
        }
        
        return quality;
    }
    
    @Override
    public Map<String, Object> getSensorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<ParkingSensor> allSensors = getAllSensors();
            if (allSensors == null) {
                allSensors = new ArrayList<>();
            }
            
            stats.put("totalSensors", allSensors.size());
            
            long activeSensors = allSensors.stream()
                .filter(s -> s.getStatus() != null && s.getStatus() == 1)
                .count();
            stats.put("activeSensors", activeSensors);
            
            double avgQuality = allSensors.stream()
                .filter(s -> s.getDataQuality() != null)
                .mapToInt(ParkingSensor::getDataQuality)
                .average()
                .orElse(0);
            stats.put("avgQuality", avgQuality);
            
            Map<Integer, Long> typeDistribution = new HashMap<>();
            for (ParkingSensor sensor : allSensors) {
                Integer type = sensor.getSensorType();
                if (type != null) {
                    typeDistribution.put(type, typeDistribution.getOrDefault(type, 0L) + 1);
                }
            }
            stats.put("typeDistribution", typeDistribution);
            
        } catch (Exception e) {
            log.error("获取传感器统计失败", e);
            stats.put("totalSensors", 0);
            stats.put("activeSensors", 0);
            stats.put("avgQuality", 0);
            stats.put("typeDistribution", new HashMap<>());
        }
        
        return stats;
    }
    
    private void updateSensorStatus(Long sensorId, ParkingSensorData data) {
        ParkingSensor sensor = sensorMapper.selectById(sensorId);
        if (sensor != null) {
            sensor.setLastValue(data.getProcessedValue());
            sensor.setLastUpdateTime(new Date());
            sensor.setDataQuality(data.getDataQuality());
            sensorMapper.updateById(sensor);
        }
    }
}
