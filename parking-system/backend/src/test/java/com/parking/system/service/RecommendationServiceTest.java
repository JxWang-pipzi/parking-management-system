package com.parking.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingUserBehavior;
import com.parking.system.mapper.ParkingLotMapper;
import com.parking.system.mapper.ParkingUserBehaviorMapper;
import com.parking.system.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RecommendationServiceTest {

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Mock
    private ParkingLotMapper parkingLotMapper;

    @Mock
    private ParkingUserBehaviorMapper userBehaviorMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPersonalizedRecommendations() {
        // 1. Mock Parking Lots
        ParkingLot lot1 = new ParkingLot();
        lot1.setId(1L);
        lot1.setName("Lot A");
        lot1.setLatitude(39.90);
        lot1.setLongitude(116.40);
        lot1.setHourlyRate(new BigDecimal("10.0"));
        lot1.setTotalSpaces(100);
        lot1.setAvailableSpaces(50);

        ParkingLot lot2 = new ParkingLot();
        lot2.setId(2L);
        lot2.setName("Lot B");
        lot2.setLatitude(39.91); // Farther
        lot2.setLongitude(116.41);
        lot2.setHourlyRate(new BigDecimal("5.0")); // Cheaper
        lot2.setTotalSpaces(100);
        lot2.setAvailableSpaces(10);

        when(parkingLotMapper.selectList(null)).thenReturn(Arrays.asList(lot1, lot2));

        // 2. Mock User Behavior
        ParkingUserBehavior behavior = new ParkingUserBehavior();
        behavior.setUserId(1L);
        behavior.setParkingLotId(1L); // User likes Lot A
        behavior.setAmount(new BigDecimal("20.0"));
        behavior.setDuration(120); // 2 hours -> 10/hour

        when(userBehaviorMapper.selectList(any(QueryWrapper.class))).thenReturn(Arrays.asList(behavior));

        // 3. Test Recommendation
        List<ParkingLot> recommendations = recommendationService.getPersonalizedRecommendations(1L, 39.90, 116.40);

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        
        // Lot A should be first because of distance and history
        assertEquals(1L, recommendations.get(0).getId());
    }

    @Test
    void testAnalyzeUserPreferences() {
        ParkingUserBehavior behavior1 = new ParkingUserBehavior();
        behavior1.setAmount(new BigDecimal("10.0"));
        behavior1.setDuration(60); // 10/h
        behavior1.setTimeSlot(9);
        behavior1.setWeekday(1);

        ParkingUserBehavior behavior2 = new ParkingUserBehavior();
        behavior2.setAmount(new BigDecimal("20.0"));
        behavior2.setDuration(60); // 20/h
        behavior2.setTimeSlot(9);
        behavior2.setWeekday(1);

        when(userBehaviorMapper.selectList(any(QueryWrapper.class)))
            .thenReturn(Arrays.asList(behavior1, behavior2));

        Map<String, Object> preferences = recommendationService.analyzeUserPreferences(1L);

        assertEquals(15.0, (Double) preferences.get("avgPrice"), 0.01);
        assertEquals(9, preferences.get("preferredTime"));
        assertEquals(1, preferences.get("preferredWeekday"));
    }
}
