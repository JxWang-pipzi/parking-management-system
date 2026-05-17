package com.parking.system;

import com.parking.system.entity.ParkingLot;
import com.parking.system.service.ParkingLotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParkingLotServiceTest {

    @Autowired
    private ParkingLotService parkingLotService;

    @Test
    void testGetAllParkingLots() {
        List<ParkingLot> parkingLots = parkingLotService.list();
        assertNotNull(parkingLots, "停车场列表不应为空");
    }

    @Test
    void testGetNearbyParkingLots() {
        Double latitude = 39.9042;
        Double longitude = 116.4074;
        Double radius = 5000.0;

        List<ParkingLot> parkingLots = parkingLotService.getNearbyParkingLots(latitude, longitude, radius);
        assertNotNull(parkingLots, "附近停车场列表不应为空");
    }

    @Test
    void testAddParkingLot() {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName("测试停车场_" + System.currentTimeMillis());
        parkingLot.setAddress("测试地址");
        parkingLot.setTotalSpaces(100);
        parkingLot.setAvailableSpaces(100);
        parkingLot.setHourlyRate(new java.math.BigDecimal("10.00"));
        parkingLot.setLatitude(39.9042);
        parkingLot.setLongitude(116.4074);
        parkingLot.setStatus(1);

        boolean result = parkingLotService.save(parkingLot);
        assertTrue(result, "添加停车场应该成功");
        assertNotNull(parkingLot.getId(), "添加后应该有ID");
    }

    @Test
    void testUpdateAvailableSpaces() {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName("更新车位测试停车场_" + System.nanoTime());
        parkingLot.setAddress("成都市测试地址");
        parkingLot.setTotalSpaces(10);
        parkingLot.setAvailableSpaces(5);
        parkingLot.setHourlyRate(new BigDecimal("10.00"));
        parkingLot.setLatitude(30.5728);
        parkingLot.setLongitude(104.0668);
        parkingLot.setStatus(1);
        assertTrue(parkingLotService.save(parkingLot), "测试停车场创建应该成功");

        boolean result = parkingLotService.updateAvailableSpaces(parkingLot.getId(), -1);
        assertTrue(result, "更新可用车位数应该成功");

        ParkingLot updatedLot = parkingLotService.getById(parkingLot.getId());
        assertEquals(4, updatedLot.getAvailableSpaces(), "可用车位数应该减少1");
    }
}
