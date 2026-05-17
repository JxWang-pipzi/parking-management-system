package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.ParkingReservation;
import com.parking.system.mapper.ParkingReservationMapper;
import com.parking.system.service.ParkingReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingReservationServiceImpl extends ServiceImpl<ParkingReservationMapper, ParkingReservation> implements ParkingReservationService {

    @Override
    public boolean createReservation(ParkingReservation reservation) {
        reservation.setStatus(0);
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        return save(reservation);
    }

    @Override
    public boolean cancelReservation(Long id, String reason) {
        ParkingReservation reservation = getById(id);
        if (reservation == null) return false;
        reservation.setStatus(2);
        reservation.setCancelReason(reason);
        reservation.setUpdateTime(LocalDateTime.now());
        return updateById(reservation);
    }

    @Override
    public boolean confirmReservation(Long id) {
        ParkingReservation reservation = getById(id);
        if (reservation == null) return false;
        reservation.setStatus(1);
        reservation.setUpdateTime(LocalDateTime.now());
        return updateById(reservation);
    }

    @Override
    public int cleanupExpiredReservations() {
        // 获取所有待确认且超过预约时间的预约
        LambdaQueryWrapper<ParkingReservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ParkingReservation::getStatus, 0)
                .lt(ParkingReservation::getReservationTime, LocalDateTime.now());
        
        List<ParkingReservation> expired = list(queryWrapper);
        if (!expired.isEmpty()) {
            expired.forEach(res -> {
                res.setStatus(4); // 4: 已过期
                res.setUpdateTime(LocalDateTime.now());
            });
            updateBatchById(expired);
            return expired.size();
        }
        return 0;
    }
}
