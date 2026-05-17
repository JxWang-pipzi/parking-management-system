package com.parking.system.service;

import com.parking.system.entity.ParkingSensor;
import com.parking.system.entity.ParkingSensorData;
import java.util.List;
import java.util.Map;

public interface ParkingSensorService {
    
    List<ParkingSensor> getAllSensors();
    
    ParkingSensor getSensorById(Long id);
    
    List<ParkingSensor> getSensorsByParkingLot(Long parkingLotId);
    
    boolean createSensor(ParkingSensor sensor);
    
    boolean updateSensor(ParkingSensor sensor);
    
    boolean deleteSensor(Long id);
    
    boolean receiveSensorData(ParkingSensorData data);
    
    List<ParkingSensorData> getSensorData(Long sensorId, Long startTime, Long endTime);
    
    Map<String, Object> getSensorQuality(Long sensorId);
    
    Map<String, Object> getSensorStatistics();
}
