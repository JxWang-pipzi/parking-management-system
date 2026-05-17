package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.ParkingLot;

import java.util.List;

public interface ParkingLotService extends IService<ParkingLot> {

    List<ParkingLot> listParkingLots(Double latitude, Double longitude);

    List<ParkingLot> getNearbyParkingLots(Double latitude, Double longitude, Double radius);

    ParkingLot getParkingLotWithSpaces(Long id);

    boolean updateAvailableSpaces(Long parkingLotId, Integer delta);

    boolean createParkingLot(ParkingLot parkingLot);

    boolean updateParkingLotInfo(ParkingLot parkingLot);

}
