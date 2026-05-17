package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.ParkingLot;
import com.parking.system.mapper.ParkingLotMapper;
import com.parking.system.service.MapRouteService;
import com.parking.system.service.ParkingLotService;
import org.springframework.stereotype.Service;

import com.parking.system.entity.ParkingSpace;
import com.parking.system.mapper.ParkingSpaceMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot> implements ParkingLotService {

    private static final double EARTH_RADIUS_METERS = 6378137.0;

    @Resource
    private ParkingSpaceMapper parkingSpaceMapper;

    @Resource
    private MapRouteService mapRouteService;

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    @Override
    public List<ParkingLot> listParkingLots(Double latitude, Double longitude) {
        QueryWrapper<ParkingLot> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<ParkingLot> parkingLots = list(wrapper);
        return enrichAndSortParkingLots(parkingLots, latitude, longitude, null);
    }

    @Override
    public List<ParkingLot> getNearbyParkingLots(Double latitude, Double longitude, Double radius) {
        QueryWrapper<ParkingLot> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        List<ParkingLot> allLots = list(wrapper);

        List<ParkingLot> nearbyLots = allLots.stream()
                .filter(lot -> calculateDistance(latitude, longitude, lot.getLatitude(), lot.getLongitude()) <= radius)
                .collect(Collectors.toList());

        return enrichAndSortParkingLots(nearbyLots, latitude, longitude, radius);
    }

    @Override
    public ParkingLot getParkingLotWithSpaces(Long id) {
        return getById(id);
    }

    @Override
    public boolean updateAvailableSpaces(Long parkingLotId, Integer delta) {
        if (delta == null || delta == 0) return false;
        QueryWrapper<ParkingLot> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("id", parkingLotId);
        if (delta > 0) {
            updateWrapper.apply("available_spaces + {0} <= total_spaces", delta);
        } else {
            updateWrapper.apply("available_spaces + {0} >= 0", delta);
        }
        ParkingLot update = new ParkingLot();
        update.setAvailableSpaces(null);
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ParkingLot> atomicWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        atomicWrapper.eq("id", parkingLotId);
        if (delta > 0) {
            atomicWrapper.apply("available_spaces + {0} <= total_spaces", delta);
        } else {
            atomicWrapper.apply("available_spaces + {0} >= 0", delta);
        }
        atomicWrapper.setSql("available_spaces = available_spaces + " + delta);
        return update(new ParkingLot(), atomicWrapper);
    }

    @Override
    @Transactional
    public boolean createParkingLot(ParkingLot parkingLot) {
        // 1. 保存停车场信息
        if (save(parkingLot)) {
            // 2. 根据总车位数自动生成车位
            int totalSpaces = parkingLot.getTotalSpaces();
            if (totalSpaces > 0) {
                for (int i = 1; i <= totalSpaces; i++) {
                    ParkingSpace space = new ParkingSpace();
                    space.setParkingLotId(parkingLot.getId());
                    // 生成车位编号：A001, A002, ...
                    space.setSpaceNumber(String.format("A%03d", i));
                    space.setType(0); // 默认普通车位
                    space.setStatus(0); // 默认空闲
                    space.setCreateTime(new Date());
                    space.setUpdateTime(new Date());
                    parkingSpaceMapper.insert(space);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateParkingLotInfo(ParkingLot parkingLot) {
        // 2. 更新停车场基本信息
        if (updateById(parkingLot)) {
            // 3. 处理车位数量变更
            // 获取当前实际的车位数量
            Long currentCountLong = parkingSpaceMapper.selectCount(new QueryWrapper<ParkingSpace>().eq("parking_lot_id", parkingLot.getId()));
            int currentCount = currentCountLong != null ? currentCountLong.intValue() : 0;
            Integer newTotal = parkingLot.getTotalSpaces();
            
            if (newTotal == null) return true; // 如果没有更新总车位数，则不做处理
            
            if (newTotal > currentCount) {
                // 增加车位
                for (int i = currentCount + 1; i <= newTotal; i++) {
                    ParkingSpace space = new ParkingSpace();
                    space.setParkingLotId(parkingLot.getId());
                    space.setSpaceNumber(String.format("A%03d", i));
                    space.setType(0);
                    space.setStatus(0);
                    space.setCreateTime(new Date());
                    space.setUpdateTime(new Date());
                    parkingSpaceMapper.insert(space);
                }
            } else if (newTotal < currentCount) {
                // 减少车位：删除多余的空闲车位（从后往前删）
                int deleteCount = currentCount - newTotal;
                // 查询该停车场所有空闲车位，按ID倒序排列
                QueryWrapper<ParkingSpace> wrapper = new QueryWrapper<>();
                wrapper.eq("parking_lot_id", parkingLot.getId());
                wrapper.eq("status", 0); // 只能删除空闲车位
                wrapper.orderByDesc("id");
                wrapper.last("LIMIT " + deleteCount);
                
                parkingSpaceMapper.delete(wrapper);
            }
            return true;
        }
        return false;
    }

    private List<ParkingLot> enrichAndSortParkingLots(List<ParkingLot> parkingLots, Double latitude, Double longitude, Double radius) {
        if (parkingLots == null || parkingLots.isEmpty()) {
            return new ArrayList<>();
        }
        for (ParkingLot lot : parkingLots) {
            if (latitude != null && longitude != null) {
                mapRouteService.enrichParkingLotRoute(lot, latitude, longitude);
            }
        }
        if (latitude != null && longitude != null) {
            parkingLots.sort(Comparator.comparingDouble(lot -> lot.getDistance() == null ? Double.MAX_VALUE : lot.getDistance()));
        }
        if (radius == null) {
            return parkingLots;
        }
        return parkingLots.stream()
                .filter(lot -> lot.getDistance() != null && lot.getDistance() <= radius)
                .collect(Collectors.toList());
    }

}
