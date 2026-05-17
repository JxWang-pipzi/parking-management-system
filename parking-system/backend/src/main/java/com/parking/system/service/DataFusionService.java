package com.parking.system.service;

import com.parking.system.entity.ParkingSensorData;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataFusionService {

    List<ParkingSensorData> cleanSensorData(List<ParkingSensorData> rawDataList);

    ParkingSensorData processAnomaly(ParkingSensorData data);

    ParkingSensorData fillMissingValue(ParkingSensorData data);

    ParkingSensorData filterNoise(ParkingSensorData data);

    List<ParkingSensorData> fuseMultiSourceData(List<ParkingSensorData> dataList);

    Integer calculateDataQuality(ParkingSensorData data);

    Boolean synchronizeTimestamp(List<ParkingSensorData> dataList);

    ParkingSensorData normalizeData(ParkingSensorData data);

    // 新增方法：多数投票算法
    String majorityVotingFusion(List<ParkingSensorData> sensorDataList);

    // 新增方法：加权融合算法
    ParkingSensorData weightedFusion(List<ParkingSensorData> sensorDataList);

    // 新增方法：传感器冲突检测
    List<ParkingSensorData> detectConflicts(List<ParkingSensorData> sensorDataList);

    // 新增方法：传感器准确率计算
    Double calculateSensorAccuracy(String sensorId, List<ParkingSensorData> historicalData);

    // 新增方法：智能融合决策
    ParkingSensorData intelligentFusion(List<ParkingSensorData> sensorDataList);


    // 传感器异常检测和故障转移相关方法

    // 检测传感器是否离线
    Boolean isSensorOffline(String sensorId, Date lastUpdateTime);

    // 检测传感器数据异常模式
    List<String> detectAnomalyPatterns(List<ParkingSensorData> sensorHistory);

    // 获取备用传感器列表
    List<String> getBackupSensors(Long spaceId, String failedSensorId);

    // 执行传感器故障转移
    Boolean performSensorFailover(String failedSensorId, Long spaceId);

    // 计算传感器健康度
    Double calculateSensorHealth(String sensorId, List<ParkingSensorData> recentData);

    // 传感器状态监控
    Map<String, String> monitorSensorStatus(List<String> sensorIds);

    // 生成传感器异常报告
    String generateAnomalyReport(String sensorId, List<ParkingSensorData> anomalyData);
}
