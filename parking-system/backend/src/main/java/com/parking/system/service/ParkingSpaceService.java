package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.ParkingSpace;

import java.util.List;

public interface ParkingSpaceService extends IService<ParkingSpace> {

    List<ParkingSpace> getSpacesByParkingLotId(Long parkingLotId, Integer status);

    boolean reserveParkingSpace(Long spaceId, Long userId);

    boolean releaseParkingSpace(Long spaceId);

    ParkingSpace getAvailableSpace(Long parkingLotId);

    boolean updateParkingSpaceStatus(Long spaceId, Integer newStatus);

    List<ParkingSpace> getReservableSpaces(Long parkingLotId);

    int batchReleaseParkingSpaces(List<Long> spaceIds);

    Long atomicAllocateSpace(Long parkingLotId, Integer targetStatus);

}