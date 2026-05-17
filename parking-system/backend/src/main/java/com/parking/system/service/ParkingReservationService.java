package com.parking.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.system.entity.ParkingReservation;

public interface ParkingReservationService extends IService<ParkingReservation> {
    boolean createReservation(ParkingReservation reservation);
    boolean cancelReservation(Long id, String reason);
    boolean confirmReservation(Long id);
    int cleanupExpiredReservations();
}
