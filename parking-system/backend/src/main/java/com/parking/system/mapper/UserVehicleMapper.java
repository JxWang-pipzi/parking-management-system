package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.UserVehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserVehicleMapper extends BaseMapper<UserVehicle> {
}
