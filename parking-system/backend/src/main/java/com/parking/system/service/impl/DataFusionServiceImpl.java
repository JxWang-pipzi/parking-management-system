package com.parking.system.service.impl;

import com.parking.system.entity.ParkingSensorData;
import com.parking.system.service.DataFusionService;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DataFusionServiceImpl implements DataFusionService {
    
    private static final double ANOMALY_THRESHOLD_UPPER = 100.0;
    private static final double ANOMALY_THRESHOLD_LOWER = 0.0;
    private static final double NOISE_FILTER_WINDOW = 5.0;
    private static final long TIME_SYNC_THRESHOLD = 5000L;
    private static final double CONFLICT_THRESHOLD = 0.3; // 冲突检测阈值
    private static final int MIN_SENSORS_FOR_VOTING = 3; // 多数投票最小传感器数量
    
    // 传感器准确率缓存
    private final Map<String, Double> sensorAccuracyCache = new ConcurrentHashMap<>();
    
    @Override
    public List<ParkingSensorData> cleanSensorData(List<ParkingSensorData> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<ParkingSensorData> cleanedData = new ArrayList<>();
        
        for (ParkingSensorData data : rawDataList) {
            ParkingSensorData processedData = data;
            
            if (hasMissingValue(data)) {
                processedData = fillMissingValue(data);
            } else if (isAnomaly(data)) {
                processedData = processAnomaly(data);
            } else {
                // 如果不是缺失值也不是异常值，需要初始化 processedValue 和 isAnomaly
                data.setProcessedValue(data.getRawValue());
                data.setIsAnomaly(0);
            }
            
            processedData = filterNoise(processedData);
            
            processedData.setDataQuality(calculateDataQuality(processedData));
            
            if (processedData.getDataQuality() >= 60) {
                cleanedData.add(processedData);
            }
        }
        
        return cleanedData;
    }
    
    @Override
    public ParkingSensorData processAnomaly(ParkingSensorData data) {
        if (data == null) return null;
        
        double value = data.getRawValue();
        
        if (value > ANOMALY_THRESHOLD_UPPER || value < ANOMALY_THRESHOLD_LOWER) {
            data.setIsAnomaly(1);
            data.setAnomalyType(detectAnomalyType(value));
            
            double correctedValue = Math.max(ANOMALY_THRESHOLD_LOWER, 
                                   Math.min(ANOMALY_THRESHOLD_UPPER, value));
            data.setProcessedValue(correctedValue);
            
            log.warn("检测到异常数据: sensorId={}, rawValue={}, anomalyType={}", 
                    data.getSensorId(), value, data.getAnomalyType());
        } else {
            data.setIsAnomaly(0);
            data.setProcessedValue(value);
        }
        
        return data;
    }
    
    @Override
    public ParkingSensorData fillMissingValue(ParkingSensorData data) {
        if (data == null) return null;
        
        if (data.getRawValue() == null) {
            data.setRawValue(0.0);
            data.setProcessedValue(0.0);
            data.setDataQuality(50);
            
            log.info("填充缺失值: sensorId={}", data.getSensorId());
        }
        
        return data;
    }
    
    @Override
    public ParkingSensorData filterNoise(ParkingSensorData data) {
        if (data == null || data.getProcessedValue() == null) return data;
        
        double value = data.getProcessedValue();
        
        if (Math.abs(value) < NOISE_FILTER_WINDOW) {
            data.setProcessedValue(0.0);
        }
        
        return data;
    }
    
    @Override
    public List<ParkingSensorData> fuseMultiSourceData(List<ParkingSensorData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return new ArrayList<>();
        }

        // 时间同步
        synchronizeTimestamp(dataList);

        // 按车位分组
        Map<Long, List<ParkingSensorData>> groupedBySpace = dataList.stream()
            .collect(Collectors.groupingBy(ParkingSensorData::getSpaceId));

        List<ParkingSensorData> fusedData = new ArrayList<>();

        for (Map.Entry<Long, List<ParkingSensorData>> entry : groupedBySpace.entrySet()) {
            List<ParkingSensorData> spaceDataList = entry.getValue();

            // 使用智能融合算法
            ParkingSensorData fused = intelligentFusion(spaceDataList);
            if (fused != null) {
                fusedData.add(fused);

                log.info("车位{}数据融合完成: 传感器数量={}, 融合值={}, 质量={}",
                        entry.getKey(), spaceDataList.size(), 
                        fused.getProcessedValue(), fused.getDataQuality());
            }
        }

        return fusedData;
    }
    
    @Override
    public Integer calculateDataQuality(ParkingSensorData data) {
        if (data == null) return 0;
        
        int quality = 100;
        
        if (data.getIsAnomaly() != null && data.getIsAnomaly() == 1) {
            quality -= 30;
        }
        
        if (data.getRawValue() == null) {
            quality -= 20;
        }
        
        long timeDiff = System.currentTimeMillis() - data.getCollectTime().getTime();
        if (timeDiff > 60000) {
            quality -= 10;
        }
        if (timeDiff > 300000) {
            quality -= 20;
        }
        
        return Math.max(0, Math.min(100, quality));
    }
    
    @Override
    public Boolean synchronizeTimestamp(List<ParkingSensorData> dataList) {
        if (dataList == null || dataList.isEmpty()) return false;
        
        long referenceTime = System.currentTimeMillis();
        
        for (ParkingSensorData data : dataList) {
            if (data.getCollectTime() != null) {
                long diff = Math.abs(referenceTime - data.getCollectTime().getTime());
                if (diff > TIME_SYNC_THRESHOLD) {
                    data.setCollectTime(new Date(referenceTime));
                    log.debug("时间同步: sensorId={}, oldTime={}", data.getSensorId(), data.getCollectTime());
                }
            }
        }
        
        return true;
    }
    
    @Override
    public ParkingSensorData normalizeData(ParkingSensorData data) {
        if (data == null || data.getProcessedValue() == null) return data;
        
        double value = data.getProcessedValue();
        double normalizedValue = (value - ANOMALY_THRESHOLD_LOWER) / 
                                (ANOMALY_THRESHOLD_UPPER - ANOMALY_THRESHOLD_LOWER);
        
        data.setProcessedValue(normalizedValue * 100);
        
        return data;
    }
    
    private boolean isAnomaly(ParkingSensorData data) {
        if (data == null || data.getRawValue() == null) return false;
        
        double value = data.getRawValue();
        return value > ANOMALY_THRESHOLD_UPPER || value < ANOMALY_THRESHOLD_LOWER;
    }
    
    private boolean hasMissingValue(ParkingSensorData data) {
        return data == null || data.getRawValue() == null;
    }
    
    private String detectAnomalyType(double value) {
        if (value > ANOMALY_THRESHOLD_UPPER) {
            return "UPPER_BOUND_EXCEEDED";
        } else if (value < ANOMALY_THRESHOLD_LOWER) {
            return "LOWER_BOUND_EXCEEDED";
        }
        return "UNKNOWN";
    }
    
    private ParkingSensorData fuseSpaceData(List<ParkingSensorData> spaceDataList) {
        if (spaceDataList == null || spaceDataList.isEmpty()) return null;
        
        ParkingSensorData fused = new ParkingSensorData();
        ParkingSensorData first = spaceDataList.get(0);
        
        fused.setSpaceId(first.getSpaceId());
        fused.setParkingLotId(first.getParkingLotId());
        fused.setCollectTime(new Date());
        fused.setProcessTime(new Date());
        fused.setCreateTime(new Date());
        
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (ParkingSensorData data : spaceDataList) {
            double weight = data.getDataQuality() / 100.0;
            if (data.getProcessedValue() != null) {
                weightedSum += data.getProcessedValue() * weight;
                totalWeight += weight;
            }
        }
        
        if (totalWeight > 0) {
            fused.setProcessedValue(weightedSum / totalWeight);
        } else {
            fused.setProcessedValue(0.0);
        }
        
        fused.setDataQuality(calculateDataQuality(fused));
        fused.setIsAnomaly(0);
        
        return fused;
    }


    @Override
    public String majorityVotingFusion(List<ParkingSensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.size() < MIN_SENSORS_FOR_VOTING) {
            log.warn("传感器数量不足，无法进行多数投票融合: {}",
                    sensorDataList != null ? sensorDataList.size() : 0);
            return "UNKNOWN";
        }

        // 统计每种状态的投票数
        Map<String, Integer> votes = new HashMap<>();
        Map<String, Double> weightedVotes = new HashMap<>();

        for (ParkingSensorData data : sensorDataList) {
            if (data.getProcessedValue() == null) continue;

            String status = determineSpaceStatus(data.getProcessedValue());
            Double accuracy = getSensorAccuracy(String.valueOf(data.getSensorId()));

            // 简单投票
            votes.put(status, votes.getOrDefault(status, 0) + 1);

            // 加权投票（考虑传感器准确率）
            weightedVotes.put(status, weightedVotes.getOrDefault(status, 0.0) + accuracy);
        }

        // 优先使用加权投票结果
        String result = weightedVotes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("UNKNOWN");

        log.info("多数投票融合结果: 简单投票={}, 加权投票结果={}", votes, result);
        return result;
    }

    @Override
    public ParkingSensorData weightedFusion(List<ParkingSensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            return null;
        }

        ParkingSensorData fusedData = new ParkingSensorData();
        ParkingSensorData template = sensorDataList.get(0);

        // 设置基础信息
        fusedData.setSpaceId(template.getSpaceId());
        fusedData.setParkingLotId(template.getParkingLotId());
        fusedData.setCollectTime(new Date());
        fusedData.setProcessTime(new Date());
        fusedData.setCreateTime(new Date());

        double weightedSum = 0.0;
        double totalWeight = 0.0;
        int validSensorCount = 0;

        for (ParkingSensorData data : sensorDataList) {
            if (data.getProcessedValue() == null) continue;

            // 计算权重：数据质量 × 传感器准确率
            double dataQualityWeight = data.getDataQuality() / 100.0;
            double accuracyWeight = getSensorAccuracy(String.valueOf(data.getSensorId()));
            double combinedWeight = dataQualityWeight * accuracyWeight;

            weightedSum += data.getProcessedValue() * combinedWeight;
            totalWeight += combinedWeight;
            validSensorCount++;
        }

        if (totalWeight > 0) {
            fusedData.setProcessedValue(weightedSum / totalWeight);
            fusedData.setRawValue(weightedSum / totalWeight);
        } else {
            fusedData.setProcessedValue(0.0);
            fusedData.setRawValue(0.0);
        }

        // 计算融合后的数据质量
        int fusedQuality = calculateFusedDataQuality(sensorDataList, validSensorCount);
        fusedData.setDataQuality(fusedQuality);
        fusedData.setIsAnomaly(0);

        log.info("加权融合完成: spaceId={}, 有效传感器数={}, 融合值={}, 质量={}",
                fusedData.getSpaceId(), validSensorCount, fusedData.getProcessedValue(), fusedQuality);

        return fusedData;
    }

    @Override
    public List<ParkingSensorData> detectConflicts(List<ParkingSensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.size() < 2) {
            return new ArrayList<>();
        }

        List<ParkingSensorData> conflicts = new ArrayList<>();

        // 计算所有传感器数据的平均值和标准差
        List<Double> values = sensorDataList.stream()
            .filter(data -> data.getProcessedValue() != null)
            .map(ParkingSensorData::getProcessedValue)
            .collect(Collectors.toList());

        if (values.size() < 2) return conflicts;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        // 检测偏离平均值超过阈值的传感器数据
        for (ParkingSensorData data : sensorDataList) {
            if (data.getProcessedValue() == null) continue;

            double deviation = Math.abs(data.getProcessedValue() - mean);
            if (stdDev > 0 && deviation > CONFLICT_THRESHOLD * stdDev) {
                data.setAnomalyType("CONFLICT_DETECTED");
                data.setIsAnomaly(1);
                conflicts.add(data);

                log.warn("检测到传感器冲突: sensorId={}, value={}, mean={}, deviation={}",
                        data.getSensorId(), data.getProcessedValue(), mean, deviation);
            }
        }

        return conflicts;
    }

    @Override
    public Double calculateSensorAccuracy(String sensorId, List<ParkingSensorData> historicalData) {
        if (sensorId == null || historicalData == null || historicalData.isEmpty()) {
            return 0.8; // 默认准确率
        }

        // 从历史数据中计算传感器准确率
        List<ParkingSensorData> sensorData = historicalData.stream()
            .filter(data -> sensorId.equals(data.getSensorId()))
            .collect(Collectors.toList());

        if (sensorData.isEmpty()) {
            return 0.8; // 默认准确率
        }

        // 计算数据质量平均值作为准确率指标
        double avgQuality = sensorData.stream()
            .mapToInt(ParkingSensorData::getDataQuality)
            .average().orElse(80.0);

        // 计算异常数据比例
        long anomalyCount = sensorData.stream()
            .filter(data -> data.getIsAnomaly() != null && data.getIsAnomaly() == 1)
            .count();

        double anomalyRate = (double) anomalyCount / sensorData.size();
        double accuracy = (avgQuality / 100.0) * (1.0 - anomalyRate);

        // 缓存计算结果
        sensorAccuracyCache.put(sensorId, accuracy);

        log.debug("传感器准确率计算: sensorId={}, accuracy={}, avgQuality={}, anomalyRate={}",
                sensorId, accuracy, avgQuality, anomalyRate);

        return Math.max(0.1, Math.min(1.0, accuracy)); // 限制在0.1-1.0范围内
    }

    @Override
    public ParkingSensorData intelligentFusion(List<ParkingSensorData> sensorDataList) {
        if (sensorDataList == null || sensorDataList.isEmpty()) {
            return null;
        }

        // 1. 检测冲突
        List<ParkingSensorData> conflicts = detectConflicts(sensorDataList);

        // 2. 过滤掉冲突数据（如果有足够的非冲突数据）
        List<ParkingSensorData> validData = sensorDataList.stream()
            .filter(data -> !conflicts.contains(data))
            .collect(Collectors.toList());

        // 如果过滤后数据不足，使用原始数据
        if (validData.size() < 2) {
            validData = sensorDataList;
        }

        // 3. 根据传感器数量选择融合策略
        if (validData.size() >= MIN_SENSORS_FOR_VOTING) {
            // 使用多数投票 + 加权融合的混合策略
            String votingResult = majorityVotingFusion(validData);
            ParkingSensorData weightedResult = weightedFusion(validData);

            // 如果投票结果与加权融合结果一致，使用加权融合结果
            if (weightedResult != null) {
                String weightedStatus = determineSpaceStatus(weightedResult.getProcessedValue());
                if (votingResult.equals(weightedStatus)) {
                    weightedResult.setAnomalyType("CONSENSUS_FUSION");
                    log.info("智能融合-共识结果: spaceId={}, status={}",
                            weightedResult.getSpaceId(), votingResult);
                    return weightedResult;
                }
            }
        }

        // 4. 降级到加权融合
        ParkingSensorData result = weightedFusion(validData);
        if (result != null) {
            result.setAnomalyType("WEIGHTED_FUSION");
            log.info("智能融合-加权结果: spaceId={}, value={}",
                    result.getSpaceId(), result.getProcessedValue());
        }

        return result;
    }

    // 辅助方法：获取传感器准确率
    private Double getSensorAccuracy(String sensorId) {
        return sensorAccuracyCache.getOrDefault(sensorId, 0.8);
    }

    // 辅助方法：根据数值确定车位状态
    private String determineSpaceStatus(Double value) {
        if (value == null) return "UNKNOWN";
        if (value > 50.0) return "OCCUPIED";
        if (value < 20.0) return "AVAILABLE";
        return "UNCERTAIN";
    }

    // 辅助方法：计算融合后数据质量
    private int calculateFusedDataQuality(List<ParkingSensorData> sensorDataList, int validSensorCount) {
        if (validSensorCount == 0) return 0;

        // 基础质量：基于有效传感器数量
        int baseQuality = Math.min(90, 60 + validSensorCount * 10);

        // 质量加权：基于各传感器的数据质量
        double avgQuality = sensorDataList.stream()
            .filter(data -> data.getProcessedValue() != null)
            .mapToInt(ParkingSensorData::getDataQuality)
            .average().orElse(60.0);

        return (int) Math.min(100, (baseQuality + avgQuality) / 2);
    }


    // 传感器状态常量
    private static final long SENSOR_OFFLINE_THRESHOLD = 300000L; // 5分钟无数据视为离线
    private static final double HEALTH_THRESHOLD_CRITICAL = 0.3; // 健康度临界值
    private static final double HEALTH_THRESHOLD_WARNING = 0.6; // 健康度警告值
    private static final int ANOMALY_PATTERN_WINDOW = 10; // 异常模式检测窗口

    // 传感器状态缓存
    private final Map<String, String> sensorStatusCache = new ConcurrentHashMap<>();
    private final Map<String, Date> sensorLastSeenCache = new ConcurrentHashMap<>();

    @Override
    public Boolean isSensorOffline(String sensorId, Date lastUpdateTime) {
        if (sensorId == null || lastUpdateTime == null) {
            return true;
        }

        long timeDiff = System.currentTimeMillis() - lastUpdateTime.getTime();
        boolean isOffline = timeDiff > SENSOR_OFFLINE_THRESHOLD;

        // 更新传感器状态缓存
        String currentStatus = isOffline ? "OFFLINE" : "ONLINE";
        sensorStatusCache.put(sensorId, currentStatus);

        if (!isOffline) {
            sensorLastSeenCache.put(sensorId, new Date());
        }

        if (isOffline) {
            log.warn("传感器离线检测: sensorId={}, 最后更新时间={}, 离线时长={}ms",
                    sensorId, lastUpdateTime, timeDiff);
        }

        return isOffline;
    }

    @Override
    public List<String> detectAnomalyPatterns(List<ParkingSensorData> sensorHistory) {
        List<String> patterns = new ArrayList<>();

        if (sensorHistory == null || sensorHistory.size() < ANOMALY_PATTERN_WINDOW) {
            return patterns;
        }

        // 按时间排序
        List<ParkingSensorData> sortedData = sensorHistory.stream()
            .sorted(Comparator.comparing(ParkingSensorData::getCollectTime))
            .collect(Collectors.toList());

        // 检测异常模式

        // 1. 数据卡死模式（连续相同值）
        if (detectStuckPattern(sortedData)) {
            patterns.add("STUCK_VALUE");
        }

        // 2. 数据跳跃模式（值突然大幅变化）
        if (detectJumpPattern(sortedData)) {
            patterns.add("VALUE_JUMP");
        }

        // 3. 数据漂移模式（逐渐偏离正常范围）
        if (detectDriftPattern(sortedData)) {
            patterns.add("VALUE_DRIFT");
        }

        // 4. 高频异常模式（异常数据比例过高）
        if (detectHighAnomalyRate(sortedData)) {
            patterns.add("HIGH_ANOMALY_RATE");
        }

        // 5. 数据缺失模式（频繁数据缺失）
        if (detectMissingDataPattern(sortedData)) {
            patterns.add("FREQUENT_MISSING");
        }

        if (!patterns.isEmpty()) {
            log.warn("检测到传感器异常模式: sensorId={}, patterns={}",
                    sortedData.get(0).getSensorId(), patterns);
        }

        return patterns;
    }

    @Override
    public List<String> getBackupSensors(Long spaceId, String failedSensorId) {
        List<String> backupSensors = new ArrayList<>();

        if (spaceId == null || failedSensorId == null) {
            return backupSensors;
        }

        // 模拟获取同一车位的其他传感器
        // 实际实现中应该从数据库查询
        String[] potentialBackups = {
            "SENSOR_" + spaceId + "_BACKUP_1",
            "SENSOR_" + spaceId + "_BACKUP_2",
            "SENSOR_" + spaceId + "_ULTRASONIC",
            "SENSOR_" + spaceId + "_MAGNETIC",
            "SENSOR_" + spaceId + "_CAMERA"
        };

        for (String backupId : potentialBackups) {
            if (!backupId.equals(failedSensorId)) {
                String status = sensorStatusCache.getOrDefault(backupId, "UNKNOWN");
                if ("ONLINE".equals(status) || "UNKNOWN".equals(status)) {
                    backupSensors.add(backupId);
                }
            }
        }

        log.info("获取备用传感器: spaceId={}, failedSensor={}, backups={}",
                spaceId, failedSensorId, backupSensors);

        return backupSensors;
    }

    @Override
    public Boolean performSensorFailover(String failedSensorId, Long spaceId) {
        if (failedSensorId == null || spaceId == null) {
            return false;
        }

        try {
            // 1. 标记故障传感器
            sensorStatusCache.put(failedSensorId, "FAILED");

            // 2. 获取备用传感器
            List<String> backupSensors = getBackupSensors(spaceId, failedSensorId);

            if (backupSensors.isEmpty()) {
                log.error("传感器故障转移失败: 无可用备用传感器, spaceId={}, failedSensor={}",
                        spaceId, failedSensorId);
                return false;
            }

            // 3. 激活最佳备用传感器
            String bestBackup = selectBestBackupSensor(backupSensors);
            sensorStatusCache.put(bestBackup, "ACTIVE_BACKUP");

            // 4. 记录故障转移事件
            log.warn("传感器故障转移成功: spaceId={}, failedSensor={}, backupSensor={}",
                    spaceId, failedSensorId, bestBackup);

            // 5. 发送告警通知（实际实现中应该发送到监控系统）
            sendFailoverAlert(failedSensorId, bestBackup, spaceId);

            return true;

        } catch (Exception e) {
            log.error("传感器故障转移异常: spaceId={}, failedSensor={}", spaceId, failedSensorId, e);
            return false;
        }
    }

    @Override
    public Double calculateSensorHealth(String sensorId, List<ParkingSensorData> recentData) {
        if (sensorId == null || recentData == null || recentData.isEmpty()) {
            return 0.0;
        }

        double healthScore = 1.0;

        // 1. 数据质量因子
        double avgQuality = recentData.stream()
            .mapToInt(data -> data.getDataQuality() != null ? data.getDataQuality() : 0)
            .average().orElse(0.0) / 100.0;

        // 2. 异常率因子
        long anomalyCount = recentData.stream()
            .filter(data -> data.getIsAnomaly() != null && data.getIsAnomaly() == 1)
            .count();
        double anomalyRate = (double) anomalyCount / recentData.size();

        // 3. 数据完整性因子
        long missingCount = recentData.stream()
            .filter(data -> data.getRawValue() == null)
            .count();
        double completenessRate = 1.0 - ((double) missingCount / recentData.size());

        // 4. 时效性因子
        long currentTime = System.currentTimeMillis();
        double avgTimeDiff = recentData.stream()
            .filter(data -> data.getCollectTime() != null)
            .mapToLong(data -> currentTime - data.getCollectTime().getTime())
            .average().orElse(0.0);
        double timelinessScore = Math.max(0.0, 1.0 - (avgTimeDiff / 300000.0)); // 5分钟内为满分

        // 综合健康度计算
        healthScore = (avgQuality * 0.3 +
                      (1.0 - anomalyRate) * 0.3 +
                      completenessRate * 0.2 +
                      timelinessScore * 0.2);

        healthScore = Math.max(0.0, Math.min(1.0, healthScore));

        log.debug("传感器健康度计算: sensorId={}, health={}, quality={}, anomalyRate={}, completeness={}, timeliness={}",
                sensorId, healthScore, avgQuality, anomalyRate, completenessRate, timelinessScore);

        return healthScore;
    }

    @Override
    public Map<String, String> monitorSensorStatus(List<String> sensorIds) {
        Map<String, String> statusMap = new HashMap<>();

        if (sensorIds == null || sensorIds.isEmpty()) {
            return statusMap;
        }

        for (String sensorId : sensorIds) {
            String status = sensorStatusCache.getOrDefault(sensorId, "UNKNOWN");
            Date lastSeen = sensorLastSeenCache.get(sensorId);

            // 检查是否离线
            if (lastSeen != null && isSensorOffline(sensorId, lastSeen)) {
                status = "OFFLINE";
            }

            statusMap.put(sensorId, status);
        }

        log.info("传感器状态监控: 总数={}, 状态分布={}", sensorIds.size(),
                statusMap.values().stream().collect(Collectors.groupingBy(s -> s, Collectors.counting())));

        return statusMap;
    }

    @Override
    public String generateAnomalyReport(String sensorId, List<ParkingSensorData> anomalyData) {
        if (sensorId == null || anomalyData == null || anomalyData.isEmpty()) {
            return "无异常数据";
        }

        StringBuilder report = new StringBuilder();
        report.append("传感器异常报告\n");
        report.append("================\n");
        report.append("传感器ID: ").append(sensorId).append("\n");
        report.append("异常数据数量: ").append(anomalyData.size()).append("\n");
        report.append("报告生成时间: ").append(new Date()).append("\n\n");

        // 异常类型统计
        Map<String, Long> anomalyTypes = anomalyData.stream()
            .filter(data -> data.getAnomalyType() != null)
            .collect(Collectors.groupingBy(ParkingSensorData::getAnomalyType, Collectors.counting()));

        report.append("异常类型分布:\n");
        anomalyTypes.forEach((type, count) ->
            report.append("  ").append(type).append(": ").append(count).append("次\n"));

        // 异常模式检测
        List<String> patterns = detectAnomalyPatterns(anomalyData);
        if (!patterns.isEmpty()) {
            report.append("\n检测到的异常模式:\n");
            patterns.forEach(pattern -> report.append("  - ").append(pattern).append("\n"));
        }

        // 健康度评估
        Double health = calculateSensorHealth(sensorId, anomalyData);
        report.append("\n传感器健康度: ").append(String.format("%.2f", health * 100)).append("%\n");

        if (health < HEALTH_THRESHOLD_CRITICAL) {
            report.append("⚠️ 警告: 传感器健康度严重偏低，建议立即检修\n");
        } else if (health < HEALTH_THRESHOLD_WARNING) {
            report.append("⚠️ 注意: 传感器健康度偏低，建议安排维护\n");
        }

        return report.toString();
    }

    // 辅助方法：检测数据卡死模式
    private boolean detectStuckPattern(List<ParkingSensorData> data) {
        if (data.size() < 5) return false;

        Double firstValue = getComparableValue(data.get(0));
        if (firstValue == null) return false;

        long sameValueCount = data.stream()
            .map(this::getComparableValue)
            .filter(firstValue::equals)
            .count();

        return sameValueCount >= data.size() * 0.8; // 80%以上相同值
    }

    // 辅助方法：检测数据跳跃模式
    private boolean detectJumpPattern(List<ParkingSensorData> data) {
        if (data.size() < 3) return false;

        int jumpCount = 0;
        for (int i = 1; i < data.size(); i++) {
            Double prev = getComparableValue(data.get(i-1));
            Double curr = getComparableValue(data.get(i));

            if (prev != null && curr != null) {
                double diff = Math.abs(curr - prev);
                if (diff > 50.0) { // 跳跃阈值
                    jumpCount++;
                }
            }
        }

        return jumpCount >= data.size() * 0.3; // 30%以上跳跃
    }

    // 辅助方法：检测数据漂移模式
    private boolean detectDriftPattern(List<ParkingSensorData> data) {
        if (data.size() < 5) return false;

        List<Double> values = data.stream()
            .map(this::getComparableValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (values.size() < 5) return false;

        // 计算趋势
        double firstHalf = values.subList(0, values.size()/2).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);
        double secondHalf = values.subList(values.size()/2, values.size()).stream()
            .mapToDouble(Double::doubleValue).average().orElse(0.0);

        return Math.abs(secondHalf - firstHalf) > 30.0; // 漂移阈值
    }

    private Double getComparableValue(ParkingSensorData data) {
        if (data == null) {
            return null;
        }
        return data.getProcessedValue() != null ? data.getProcessedValue() : data.getRawValue();
    }

    // 辅助方法：检测高异常率
    private boolean detectHighAnomalyRate(List<ParkingSensorData> data) {
        long anomalyCount = data.stream()
            .filter(d -> d.getIsAnomaly() != null && d.getIsAnomaly() == 1)
            .count();

        return (double) anomalyCount / data.size() > 0.5; // 50%以上异常
    }

    // 辅助方法：检测数据缺失模式
    private boolean detectMissingDataPattern(List<ParkingSensorData> data) {
        long missingCount = data.stream()
            .filter(d -> d.getRawValue() == null)
            .count();

        return (double) missingCount / data.size() > 0.3; // 30%以上缺失
    }

    // 辅助方法：选择最佳备用传感器
    private String selectBestBackupSensor(List<String> backupSensors) {
        // 简单策略：选择第一个可用的备用传感器
        // 实际实现中可以根据传感器类型、历史可靠性等因素选择
        return backupSensors.get(0);
    }

    // 辅助方法：发送故障转移告警
    private void sendFailoverAlert(String failedSensorId, String backupSensorId, Long spaceId) {
        // 实际实现中应该发送到监控系统或告警平台
        log.warn("🚨 传感器故障转移告警: 车位={}, 故障传感器={}, 备用传感器={}",
                spaceId, failedSensorId, backupSensorId);
    }
}
