package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.VehicleRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface VehicleRecordService extends IService<VehicleRecord> {

    List<VehicleRecord> getAllWithDetails();

    List<VehicleRecord> getByStatus(Integer status);

    VehicleRecord vehicleEntry(Long parkingLotId, String plateNumber, String plateImageUrl, BigDecimal confidence);

    VehicleRecord vehicleExit(Long recordId);

    Map<String, Object> recognizePlate(byte[] imageData);
}
