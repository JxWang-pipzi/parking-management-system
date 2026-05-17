package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.ParkingSensor;
import com.parking.system.entity.ParkingSensorData;
import com.parking.system.service.ParkingSensorService;
import com.parking.system.service.DataFusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sensors")
public class ParkingSensorController {
    
    @Autowired
    private ParkingSensorService sensorService;
    
    @Autowired
    private DataFusionService dataFusionService;
    
    @GetMapping
    public Response<List<ParkingSensor>> getAllSensors() {
        List<ParkingSensor> sensors = sensorService.getAllSensors();
        return Response.success(sensors);
    }
    
    @GetMapping("/{id}")
    public Response<ParkingSensor> getSensorById(@PathVariable Long id) {
        ParkingSensor sensor = sensorService.getSensorById(id);
        if (sensor == null) {
            return Response.error("传感器不存在");
        }
        return Response.success(sensor);
    }
    
    @GetMapping("/parking-lot/{parkingLotId}")
    public Response<List<ParkingSensor>> getSensorsByParkingLot(@PathVariable Long parkingLotId) {
        List<ParkingSensor> sensors = sensorService.getSensorsByParkingLot(parkingLotId);
        return Response.success(sensors);
    }
    
    @PostMapping
    public Response<ParkingSensor> createSensor(@RequestBody ParkingSensor sensor) {
        boolean success = sensorService.createSensor(sensor);
        if (success) {
            return Response.success(sensor);
        }
        return Response.error("创建传感器失败");
    }
    
    @PutMapping("/{id}")
    public Response<ParkingSensor> updateSensor(@PathVariable Long id, @RequestBody ParkingSensor sensor) {
        sensor.setId(id);
        boolean success = sensorService.updateSensor(sensor);
        if (success) {
            return Response.success(sensor);
        }
        return Response.error("更新传感器失败");
    }
    
    @DeleteMapping("/{id}")
    public Response<Void> deleteSensor(@PathVariable Long id) {
        boolean success = sensorService.deleteSensor(id);
        if (success) {
            return Response.success(null);
        }
        return Response.error("删除传感器失败");
    }
    
    @PostMapping("/{id}/data")
    public Response<Void> receiveSensorData(@PathVariable Long id, @RequestBody ParkingSensorData data) {
        data.setSensorId(id);
        boolean success = sensorService.receiveSensorData(data);
        if (success) {
            return Response.success(null);
        }
        return Response.error("接收传感器数据失败");
    }
    
    @GetMapping("/{id}/data")
    public Response<List<ParkingSensorData>> getSensorData(
            @PathVariable Long id,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        List<ParkingSensorData> dataList = sensorService.getSensorData(id, startTime, endTime);
        return Response.success(dataList);
    }
    
    @GetMapping("/data/clean")
    public Response<List<ParkingSensorData>> cleanData(@RequestBody List<ParkingSensorData> rawDataList) {
        List<ParkingSensorData> cleanedData = dataFusionService.cleanSensorData(rawDataList);
        return Response.success(cleanedData);
    }
    
    @PostMapping("/data/fuse")
    public Response<List<ParkingSensorData>> fuseData(@RequestBody List<ParkingSensorData> dataList) {
        List<ParkingSensorData> fusedData = dataFusionService.fuseMultiSourceData(dataList);
        return Response.success(fusedData);
    }
    
    @GetMapping("/{id}/quality")
    public Response<Map<String, Object>> getSensorQuality(@PathVariable Long id) {
        Map<String, Object> quality = sensorService.getSensorQuality(id);
        return Response.success(quality);
    }
    
    @GetMapping("/statistics")
    public Response<Map<String, Object>> getSensorStatistics() {
        Map<String, Object> stats = sensorService.getSensorStatistics();
        return Response.success(stats);
    }
}
