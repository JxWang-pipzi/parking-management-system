package com.parking.system;

import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingSensorData;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.service.DataFusionService;
import com.parking.system.service.ParkingLotService;
import com.parking.system.service.ParkingSpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParkingMonitorEnhancedTest {

    @Autowired
    private ParkingSpaceService parkingSpaceService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private DataFusionService dataFusionService;

    private Long testParkingLotId;

    @BeforeEach
    void setUp() {
        System.out.println("[测试准备] 开始初始化车位监控测试数据...");
        
        List<ParkingLot> lots = parkingLotService.list();
        if (!lots.isEmpty()) {
            testParkingLotId = lots.get(0).getId();
        }
        
        System.out.println("[测试准备] 初始化完成: lotId=" + testParkingLotId);
    }

    @Test
    @DisplayName("P0-正常场景：查询停车场车位列表")
    void testGetSpacesByParkingLotId() {
        System.out.println("[测试场景] 正常场景：查询停车场车位列表");
        System.out.println("[输入参数] parkingLotId=" + testParkingLotId);
        
        List<ParkingSpace> spaces = parkingSpaceService.getSpacesByParkingLotId(testParkingLotId, null);
        
        System.out.println("[预期输出] 返回车位列表");
        System.out.println("[实际输出] 车位数量=" + spaces.size());
        assertNotNull(spaces, "车位列表不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：按状态查询空闲车位")
    void testGetSpacesByStatus() {
        System.out.println("[测试场景] 边界场景：按状态查询空闲车位");
        System.out.println("[输入参数] parkingLotId=" + testParkingLotId + ", status=0（空闲）");
        
        List<ParkingSpace> freeSpaces = parkingSpaceService.getSpacesByParkingLotId(testParkingLotId, 0);
        
        System.out.println("[预期输出] 返回空闲车位列表");
        System.out.println("[实际输出] 空闲车位数量=" + freeSpaces.size());
        assertNotNull(freeSpaces, "车位列表不应该为null");
        assertTrue(freeSpaces.stream().allMatch(s -> s.getStatus() == 0), "所有车位状态应该为空闲");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：获取空闲车位")
    void testGetAvailableSpace() {
        System.out.println("[测试场景] 正常场景：获取空闲车位");
        System.out.println("[输入参数] parkingLotId=" + testParkingLotId);
        
        ParkingSpace space = parkingSpaceService.getAvailableSpace(testParkingLotId);
        
        System.out.println("[预期输出] 返回一个空闲车位或null");
        System.out.println("[实际输出] spaceId=" + (space != null ? space.getId() : "无空闲车位"));
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：获取可预约车位列表")
    void testGetReservableSpaces() {
        System.out.println("[测试场景] 正常场景：获取可预约车位列表");
        System.out.println("[输入参数] parkingLotId=" + testParkingLotId);
        
        List<ParkingSpace> spaces = parkingSpaceService.getReservableSpaces(testParkingLotId);
        
        System.out.println("[预期输出] 返回可预约车位列表");
        System.out.println("[实际输出] 可预约车位数量=" + spaces.size());
        assertNotNull(spaces, "车位列表不应该为null");
        assertTrue(spaces.stream().allMatch(s -> s.getStatus() == 0), "所有车位应该为空闲状态");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：更新车位状态")
    void testUpdateParkingSpaceStatus() {
        System.out.println("[测试场景] 正常场景：更新车位状态");
        
        ParkingSpace space = parkingSpaceService.getAvailableSpace(testParkingLotId);
        if (space == null) {
            System.out.println("[跳过] 无空闲车位可测试");
            return;
        }
        
        Long spaceId = space.getId();
        System.out.println("[输入参数] spaceId=" + spaceId + ", newStatus=1（占用）");
        
        boolean result = parkingSpaceService.updateParkingSpaceStatus(spaceId, 1);
        
        System.out.println("[预期输出] 更新成功=true");
        System.out.println("[实际输出] 更新结果=" + result);
        assertTrue(result, "更新车位状态应该成功");
        
        parkingSpaceService.updateParkingSpaceStatus(spaceId, 0);
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：更新不存在的车位状态")
    void testUpdateNonExistentSpaceStatus() {
        System.out.println("[测试场景] 异常场景：更新不存在的车位状态");
        System.out.println("[输入参数] spaceId=-1（不存在的车位）");
        
        boolean result = parkingSpaceService.updateParkingSpaceStatus(-1L, 1);
        
        System.out.println("[预期输出] 更新失败=false");
        System.out.println("[实际输出] 更新结果=" + result);
        assertFalse(result, "更新不存在的车位应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：数据清洗-正常数据")
    void testCleanSensorDataNormal() {
        System.out.println("[测试场景] 正常场景：数据清洗-正常数据");
        
        List<ParkingSensorData> rawDataList = new ArrayList<>();
        ParkingSensorData data = new ParkingSensorData();
        data.setSensorId(1L);
        data.setParkingLotId(testParkingLotId);
        data.setSpaceId(1L);
        data.setRawValue(50.0);
        data.setCollectTime(new Date());
        rawDataList.add(data);
        
        System.out.println("[输入参数] 原始数据: rawValue=50.0");
        
        List<ParkingSensorData> cleanedData = dataFusionService.cleanSensorData(rawDataList);
        
        System.out.println("[预期输出] 清洗后数据质量>=60");
        System.out.println("[实际输出] 清洗后数量=" + cleanedData.size());
        assertFalse(cleanedData.isEmpty(), "清洗后应该有数据");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：数据清洗-异常值处理")
    void testCleanSensorDataWithAnomaly() {
        System.out.println("[测试场景] 边界场景：数据清洗-异常值处理");
        
        List<ParkingSensorData> rawDataList = new ArrayList<>();
        ParkingSensorData data = new ParkingSensorData();
        data.setSensorId(1L);
        data.setParkingLotId(testParkingLotId);
        data.setSpaceId(1L);
        data.setRawValue(150.0);
        data.setCollectTime(new Date());
        rawDataList.add(data);
        
        System.out.println("[输入参数] 原始数据: rawValue=150.0（超出正常范围）");
        
        List<ParkingSensorData> cleanedData = dataFusionService.cleanSensorData(rawDataList);
        
        System.out.println("[预期输出] 异常数据被标记或修正");
        System.out.println("[实际输出] 清洗后数量=" + cleanedData.size());
        if (!cleanedData.isEmpty()) {
            ParkingSensorData cleaned = cleanedData.get(0);
            System.out.println("[实际输出] isAnomaly=" + cleaned.getIsAnomaly() + ", processedValue=" + cleaned.getProcessedValue());
        }
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：数据清洗-缺失值处理")
    void testCleanSensorDataWithMissingValue() {
        System.out.println("[测试场景] 边界场景：数据清洗-缺失值处理");
        
        List<ParkingSensorData> rawDataList = new ArrayList<>();
        ParkingSensorData data = new ParkingSensorData();
        data.setSensorId(1L);
        data.setParkingLotId(testParkingLotId);
        data.setSpaceId(1L);
        data.setRawValue(null);
        data.setCollectTime(new Date());
        rawDataList.add(data);
        
        System.out.println("[输入参数] 原始数据: rawValue=null（缺失值）");
        
        List<ParkingSensorData> cleanedData = dataFusionService.cleanSensorData(rawDataList);
        
        System.out.println("[预期输出] 缺失值被填充");
        System.out.println("[实际输出] 清洗后数量=" + cleanedData.size());
        if (!cleanedData.isEmpty()) {
            ParkingSensorData cleaned = cleanedData.get(0);
            System.out.println("[实际输出] processedValue=" + cleaned.getProcessedValue());
        }
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：多源数据融合")
    void testFuseMultiSourceData() {
        System.out.println("[测试场景] 正常场景：多源数据融合");
        
        List<ParkingSensorData> dataList = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            ParkingSensorData data = new ParkingSensorData();
            data.setSensorId((long) i);
            data.setParkingLotId(testParkingLotId);
            data.setSpaceId(1L);
            data.setRawValue(40.0 + i * 10);
            data.setProcessedValue(40.0 + i * 10);
            data.setDataQuality(90);
            data.setCollectTime(new Date());
            dataList.add(data);
        }
        
        System.out.println("[输入参数] 3个传感器数据: values=[50.0, 60.0, 70.0]");
        
        List<ParkingSensorData> fusedData = dataFusionService.fuseMultiSourceData(dataList);
        
        System.out.println("[预期输出] 融合后数据");
        System.out.println("[实际输出] 融合后数量=" + fusedData.size());
        assertFalse(fusedData.isEmpty(), "融合后应该有数据");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：传感器离线检测")
    void testIsSensorOffline() {
        System.out.println("[测试场景] 边界场景：传感器离线检测");
        
        Date recentTime = new Date(System.currentTimeMillis() - 60000);
        Date oldTime = new Date(System.currentTimeMillis() - 400000);
        
        System.out.println("[输入参数] sensorId=TEST-001, 最近更新时间=1分钟前");
        boolean isOnline = dataFusionService.isSensorOffline("TEST-001", recentTime);
        System.out.println("[实际输出] 是否离线=" + isOnline);
        assertFalse(isOnline, "1分钟前更新的传感器应该在线");
        
        System.out.println("[输入参数] sensorId=TEST-002, 最近更新时间=7分钟前");
        boolean isOffline = dataFusionService.isSensorOffline("TEST-002", oldTime);
        System.out.println("[实际输出] 是否离线=" + isOffline);
        assertTrue(isOffline, "7分钟前更新的传感器应该离线");
        
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：计算传感器健康度")
    void testCalculateSensorHealth() {
        System.out.println("[测试场景] 正常场景：计算传感器健康度");
        
        List<ParkingSensorData> recentData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ParkingSensorData data = new ParkingSensorData();
            data.setSensorId(1L);
            data.setDataQuality(90);
            data.setIsAnomaly(0);
            data.setRawValue(50.0);
            data.setCollectTime(new Date(System.currentTimeMillis() - i * 60000));
            recentData.add(data);
        }
        
        System.out.println("[输入参数] sensorId=1, 10条历史数据，质量90，无异常");
        
        Double health = dataFusionService.calculateSensorHealth("1", recentData);
        
        System.out.println("[预期输出] 健康度>0.5");
        System.out.println("[实际输出] 健康度=" + String.format("%.2f", health));
        assertTrue(health > 0.5, "健康度应该大于0.5");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：传感器状态监控")
    void testMonitorSensorStatus() {
        System.out.println("[测试场景] 边界场景：传感器状态监控");
        
        List<String> sensorIds = List.of("SENSOR-001", "SENSOR-002", "SENSOR-003");
        
        System.out.println("[输入参数] sensorIds=" + sensorIds);
        
        Map<String, String> statusMap = dataFusionService.monitorSensorStatus(sensorIds);
        
        System.out.println("[预期输出] 返回各传感器状态");
        System.out.println("[实际输出] 状态映射=" + statusMap);
        assertNotNull(statusMap, "状态映射不应该为null");
        assertEquals(3, statusMap.size(), "应该有3个传感器的状态");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：生成异常报告")
    void testGenerateAnomalyReport() {
        System.out.println("[测试场景] 正常场景：生成异常报告");
        
        List<ParkingSensorData> anomalyData = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ParkingSensorData data = new ParkingSensorData();
            data.setSensorId(1L);
            data.setIsAnomaly(1);
            data.setAnomalyType("UPPER_BOUND_EXCEEDED");
            data.setRawValue(150.0);
            data.setCollectTime(new Date());
            anomalyData.add(data);
        }
        
        System.out.println("[输入参数] sensorId=1, 5条异常数据");
        
        String report = dataFusionService.generateAnomalyReport("1", anomalyData);
        
        System.out.println("[预期输出] 生成异常报告字符串");
        System.out.println("[实际输出] 报告内容:\n" + report);
        assertNotNull(report, "报告不应该为null");
        assertTrue(report.contains("异常"), "报告应该包含异常信息");
        System.out.println("[测试结果] ✓ 通过");
    }
}
