package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.ParkingUserBehavior;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParkingUserBehaviorMapper extends BaseMapper<ParkingUserBehavior> {
    
    List<ParkingUserBehavior> selectByUserId(@Param("userId") Long userId);
    
    List<ParkingUserBehavior> selectByParkingLotId(@Param("parkingLotId") Long parkingLotId);
}
