package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.mapper.ParkingSpaceMapper;
import com.parking.system.service.ParkingSpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {

    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(LOG_TIME_FORMAT);
    }

    @Override
    public List<ParkingSpace> getSpacesByParkingLotId(Long parkingLotId, Integer status) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][查询车位] 时间：{} | 参数：parkingLotId={}, status={} | 开始查询车位", 
                time, parkingLotId, status);
        
        try {
            QueryWrapper<ParkingSpace> wrapper = new QueryWrapper<>();
            wrapper.eq("parking_lot_id", parkingLotId);
            if (status != null) {
                wrapper.eq("status", status);
            }
            List<ParkingSpace> spaces = list(wrapper);
            
            log.info("[成功][阶段4-结果反馈][查询车位] 时间：{} | 参数：parkingLotId={}, status={} | 结果：查询到{}个车位", 
                    time, parkingLotId, status, spaces.size());
            return spaces;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][查询车位] 时间：{} | 原因：{} | 参数：parkingLotId={}, status={}", 
                    time, e.getMessage(), parkingLotId, status);
            throw e;
        }
    }

    @Override
    public boolean reserveParkingSpace(Long spaceId, Long userId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][预约车位] 时间：{} | 参数：spaceId={}, userId={} | 开始预约车位", 
                time, spaceId, userId);
        
        try {
            QueryWrapper<ParkingSpace> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("id", spaceId);
            updateWrapper.eq("status", 0);
            
            ParkingSpace update = new ParkingSpace();
            update.setId(spaceId);
            update.setStatus(2);
            
            boolean result = update(update, updateWrapper);
            
            if (result) {
                log.info("[成功][阶段4-结果反馈][预约车位] 时间：{} | 参数：spaceId={}, userId={} | 结果：预约成功", 
                        time, spaceId, userId);
            } else {
                log.warn("[失败][阶段4-结果反馈][预约车位] 时间：{} | 原因：车位已被占用或不存在 | 参数：spaceId={}", time, spaceId);
            }
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][预约车位] 时间：{} | 原因：{} | 参数：spaceId={}, userId={}", 
                    time, e.getMessage(), spaceId, userId);
            throw e;
        }
    }

    @Override
    public boolean releaseParkingSpace(Long spaceId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][释放车位] 时间：{} | 参数：spaceId={} | 开始释放车位", time, spaceId);
        
        try {
            ParkingSpace space = getById(spaceId);
            if (space == null) {
                log.warn("[失败][阶段2-核心操作][释放车位] 时间：{} | 原因：车位不存在 | 参数：spaceId={}", time, spaceId);
                return false;
            }
            
            space.setStatus(0);
            boolean result = updateById(space);
            
            if (result) {
                log.info("[成功][阶段4-结果反馈][释放车位] 时间：{} | 参数：spaceId={} | 结果：释放成功", time, spaceId);
            } else {
                log.warn("[失败][阶段4-结果反馈][释放车位] 时间：{} | 原因：更新失败 | 参数：spaceId={}", time, spaceId);
            }
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][释放车位] 时间：{} | 原因：{} | 参数：spaceId={}", 
                    time, e.getMessage(), spaceId);
            throw e;
        }
    }

    @Override
    public ParkingSpace getAvailableSpace(Long parkingLotId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取空闲车位] 时间：{} | 参数：parkingLotId={} | 开始获取空闲车位", time, parkingLotId);
        
        try {
            QueryWrapper<ParkingSpace> wrapper = new QueryWrapper<>();
            wrapper.eq("parking_lot_id", parkingLotId);
            wrapper.eq("status", 0);
            wrapper.orderByAsc("id");
            wrapper.last("LIMIT 1");
            ParkingSpace space = getOne(wrapper, false);
            
            if (space != null) {
                log.info("[成功][阶段4-结果反馈][获取空闲车位] 时间：{} | 参数：parkingLotId={} | 结果：找到空闲车位spaceId={}", 
                        time, parkingLotId, space.getId());
            } else {
                log.info("[成功][阶段4-结果反馈][获取空闲车位] 时间：{} | 参数：parkingLotId={} | 结果：无空闲车位", 
                        time, parkingLotId);
            }
            return space;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取空闲车位] 时间：{} | 原因：{} | 参数：parkingLotId={}", 
                    time, e.getMessage(), parkingLotId);
            throw e;
        }
    }


    @Override
    public List<ParkingSpace> getReservableSpaces(Long parkingLotId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取可预约车位] 时间：{} | 参数：parkingLotId={} | 开始获取可预约车位", time, parkingLotId);
        
        try {
            QueryWrapper<ParkingSpace> wrapper = new QueryWrapper<>();
            wrapper.eq("parking_lot_id", parkingLotId);
            wrapper.eq("status", 0);
            wrapper.orderBy(true, true, "space_number");
            List<ParkingSpace> spaces = list(wrapper);
            
            log.info("[成功][阶段4-结果反馈][获取可预约车位] 时间：{} | 参数：parkingLotId={} | 结果：找到{}个可预约车位", 
                    time, parkingLotId, spaces.size());
            return spaces;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取可预约车位] 时间：{} | 原因：{} | 参数：parkingLotId={}", 
                    time, e.getMessage(), parkingLotId);
            throw e;
        }
    }

    @Override
    public int batchReleaseParkingSpaces(List<Long> spaceIds) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][批量释放车位] 时间：{} | 参数：spaceIds={} | 开始批量释放车位", time, spaceIds);
        
        if (spaceIds == null || spaceIds.isEmpty()) {
            log.warn("[失败][阶段2-核心操作][批量释放车位] 时间：{} | 原因：参数为空 | 参数：spaceIds=null", time);
            return 0;
        }

        int releasedCount = 0;

        for (Long spaceId : spaceIds) {
            if (releaseParkingSpace(spaceId)) {
                releasedCount++;
            }
        }

        log.info("[成功][阶段4-结果反馈][批量释放车位] 时间：{} | 参数：请求数={} | 结果：成功释放{}个", 
                time, spaceIds.size(), releasedCount);

        return releasedCount;
    }
    
    @Override
    public boolean updateParkingSpaceStatus(Long spaceId, Integer newStatus) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][更新车位状态] 时间：{} | 参数：spaceId={}, newStatus={} | 开始更新车位状态", 
                time, spaceId, newStatus);
        
        try {
            ParkingSpace space = getById(spaceId);
            if (space == null) {
                log.warn("[失败][阶段2-核心操作][更新车位状态] 时间：{} | 原因：车位不存在 | 参数：spaceId={}", time, spaceId);
                return false;
            }
            
            Integer oldStatus = space.getStatus();
            space.setStatus(newStatus);
            boolean result = updateById(space);
            
            if (result) {
                log.info("[成功][阶段4-结果反馈][更新车位状态] 时间：{} | 参数：spaceId={} | 结果：状态从{}变更为{}", 
                        time, spaceId, oldStatus, newStatus);
            } else {
                log.warn("[失败][阶段4-结果反馈][更新车位状态] 时间：{} | 原因：更新失败 | 参数：spaceId={}", time, spaceId);
            }
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][更新车位状态] 时间：{} | 原因：{} | 参数：spaceId={}, newStatus={}", 
                    time, e.getMessage(), spaceId, newStatus);
            throw e;
        }
    }

    @Override
    public Long atomicAllocateSpace(Long parkingLotId, Integer targetStatus) {
        String time = getCurrentTime();
        log.info("[成功][阶段1-入口][原子分配车位] 时间：{} | 参数：parkingLotId={}, targetStatus={}", time, parkingLotId, targetStatus);

        try {
            QueryWrapper<ParkingSpace> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parking_lot_id", parkingLotId);
            queryWrapper.eq("status", 0);
            queryWrapper.last("LIMIT 1");

            ParkingSpace freeSpace = getOne(queryWrapper, false);
            if (freeSpace == null) {
                log.warn("[失败][阶段2-核心操作][原子分配车位] 时间：{} | 原因：无空闲车位 | 参数：parkingLotId={}", time, parkingLotId);
                return null;
            }

            QueryWrapper<ParkingSpace> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("id", freeSpace.getId());
            updateWrapper.eq("status", 0);

            ParkingSpace update = new ParkingSpace();
            update.setId(freeSpace.getId());
            update.setStatus(targetStatus);

            boolean result = update(update, updateWrapper);
            if (result) {
                log.info("[成功][阶段4-结果反馈][原子分配车位] 时间：{} | 参数：parkingLotId={} | 结果：spaceId={}", time, parkingLotId, freeSpace.getId());
                return freeSpace.getId();
            } else {
                log.warn("[失败][阶段4-结果反馈][原子分配车位] 时间：{} | 原因：并发冲突 | 参数：parkingLotId={}", time, parkingLotId);
                return null;
            }
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][原子分配车位] 时间：{} | 原因：{} | 参数：parkingLotId={}", time, e.getMessage(), parkingLotId);
            throw e;
        }
    }

}
