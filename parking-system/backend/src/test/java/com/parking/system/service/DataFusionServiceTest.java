package com.parking.system.service;

import com.parking.system.entity.ParkingSensorData;
import com.parking.system.service.impl.DataFusionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataFusionServiceTest {

    @InjectMocks
    private DataFusionServiceImpl dataFusionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCleanSensorData_Normal() {
        ParkingSensorData data = new ParkingSensorData();
        data.setSensorId(1L);
        data.setRawValue(50.0);
        data.setCollectTime(new Date());

        List<ParkingSensorData> rawList = Arrays.asList(data);
        List<ParkingSensorData> cleaned = dataFusionService.cleanSensorData(rawList);

        assertEquals(1, cleaned.size());
        assertEquals(50.0, cleaned.get(0).getProcessedValue());
        assertEquals(0, cleaned.get(0).getIsAnomaly());
    }

    @Test
    void testCleanSensorData_Anomaly() {
        ParkingSensorData data = new ParkingSensorData();
        data.setSensorId(1L);
        data.setRawValue(150.0); // > 100, Anomaly
        data.setCollectTime(new Date());

        List<ParkingSensorData> rawList = Arrays.asList(data);
        List<ParkingSensorData> cleaned = dataFusionService.cleanSensorData(rawList);

        assertEquals(1, cleaned.size());
        assertEquals(100.0, cleaned.get(0).getProcessedValue()); // Capped at 100
        assertEquals(1, cleaned.get(0).getIsAnomaly());
        assertEquals("UPPER_BOUND_EXCEEDED", cleaned.get(0).getAnomalyType());
    }

    @Test
    void testDetectConflicts() {
        ParkingSensorData sensor1 = new ParkingSensorData();
        sensor1.setSensorId(1L);
        sensor1.setProcessedValue(90.0); // Occupied

        ParkingSensorData sensor2 = new ParkingSensorData();
        sensor2.setSensorId(2L);
        sensor2.setProcessedValue(10.0); // Empty

        List<ParkingSensorData> list = Arrays.asList(sensor1, sensor2);
        List<ParkingSensorData> conflicts = dataFusionService.detectConflicts(list);

        // Standard deviation will be high, conflict expected
        assertFalse(conflicts.isEmpty());
        assertEquals("CONFLICT_DETECTED", conflicts.get(0).getAnomalyType());
    }

    @Test
    void testMajorityVotingFusion() {
        ParkingSensorData s1 = new ParkingSensorData();
        s1.setSensorId(1L);
        s1.setProcessedValue(90.0); // OCCUPIED

        ParkingSensorData s2 = new ParkingSensorData();
        s2.setSensorId(2L);
        s2.setProcessedValue(85.0); // OCCUPIED

        ParkingSensorData s3 = new ParkingSensorData();
        s3.setSensorId(3L);
        s3.setProcessedValue(10.0); // AVAILABLE

        List<ParkingSensorData> list = Arrays.asList(s1, s2, s3);
        String result = dataFusionService.majorityVotingFusion(list);

        assertEquals("OCCUPIED", result);
    }
}
