package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.UserVehicle;
import com.parking.system.mapper.UserVehicleMapper;
import com.parking.system.service.UserVehicleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class UserVehicleServiceImpl extends ServiceImpl<UserVehicleMapper, UserVehicle> implements UserVehicleService {

    @Resource
    private UserVehicleMapper userVehicleMapper;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserVehicleServiceImpl.class);

    @Override
    public UserVehicle getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<UserVehicle> getByUserId(Long userId) {
        log.info("[成功][阶段2][查询用户车辆] 时间：{} | 参数：userId={}", System.currentTimeMillis(), userId);
        QueryWrapper<UserVehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("is_default").orderByDesc("create_time");
        List<UserVehicle> list = userVehicleMapper.selectList(wrapper);
        log.info("[成功][阶段4][查询用户车辆] 结果：共{}辆", list.size());
        return list;
    }

    @Override
    public UserVehicle getDefaultByUserId(Long userId) {
        QueryWrapper<UserVehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("is_default", 1).last("LIMIT 1");
        return userVehicleMapper.selectOne(wrapper);
    }

    @Override
    public boolean addVehicle(UserVehicle vehicle) {
        log.info("[成功][阶段2][添加车辆] 时间：{} | 参数：plateNumber={}", System.currentTimeMillis(), vehicle.getPlateNumber());
        vehicle.setCreateTime(new Date());
        vehicle.setUpdateTime(new Date());
        if (vehicle.getIsDefault() == null) {
            List<UserVehicle> existing = getByUserId(vehicle.getUserId());
            if (existing.isEmpty()) {
                vehicle.setIsDefault(1);
            } else {
                vehicle.setIsDefault(0);
            }
        } else if (vehicle.getIsDefault() == 1) {
            UpdateWrapper<UserVehicle> clearDefault = new UpdateWrapper<>();
            clearDefault.eq("user_id", vehicle.getUserId()).set("is_default", 0);
            userVehicleMapper.update(null, clearDefault);
        }
        boolean result = save(vehicle);
        if (result) {
            log.info("[成功][阶段4][添加车辆] 结果：id={}", vehicle.getId());
        } else {
            log.warn("[失败][阶段4][添加车辆] 原因：保存失败");
        }
        return result;
    }

    @Override
    public boolean updateVehicle(UserVehicle vehicle) {
        log.info("[成功][阶段2][更新车辆] 时间：{} | 参数：id={}", System.currentTimeMillis(), vehicle.getId());
        vehicle.setUpdateTime(new Date());
        if (vehicle.getIsDefault() != null && vehicle.getIsDefault() == 1) {
            UpdateWrapper<UserVehicle> clearDefault = new UpdateWrapper<>();
            clearDefault.eq("user_id", vehicle.getUserId()).set("is_default", 0);
            userVehicleMapper.update(null, clearDefault);
        }
        UpdateWrapper<UserVehicle> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", vehicle.getId()).eq("user_id", vehicle.getUserId());
        boolean result = update(vehicle, wrapper);
        log.info("[成功][阶段4][更新车辆] 结果：{}", result ? "成功" : "失败");
        return result;
    }

    @Override
    public boolean deleteVehicle(Long id, Long userId) {
        log.info("[成功][阶段2][删除车辆] 时间：{} | 参数：id={},userId={}", System.currentTimeMillis(), id, userId);
        QueryWrapper<UserVehicle> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        boolean result = remove(wrapper);
        log.info("[成功][阶段4][删除车辆] 结果：{}", result ? "成功" : "失败");
        return result;
    }

    @Override
    public boolean setDefault(Long id, Long userId) {
        log.info("[成功][阶段2][设置默认车辆] 时间：{} | 参数：id={},userId={}", System.currentTimeMillis(), id, userId);
        UpdateWrapper<UserVehicle> clearDefault = new UpdateWrapper<>();
        clearDefault.eq("user_id", userId).set("is_default", 0);
        userVehicleMapper.update(null, clearDefault);

        UpdateWrapper<UserVehicle> setNewDefault = new UpdateWrapper<>();
        setNewDefault.eq("id", id).eq("user_id", userId).set("is_default", 1);
        boolean result = userVehicleMapper.update(null, setNewDefault) > 0;
        log.info("[成功][阶段4][设置默认车辆] 结果：{}", result ? "成功" : "失败");
        return result;
    }
}
