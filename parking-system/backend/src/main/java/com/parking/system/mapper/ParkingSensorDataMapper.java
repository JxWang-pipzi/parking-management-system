package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.ParkingSensorData;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParkingSensorDataMapper extends BaseMapper<ParkingSensorData> {
    
    List<ParkingSensorData> selectBySensorId(@Param("sensorId") Long sensorId);
    
    List<ParkingSensorData> selectBySpaceId(@Param("spaceId") Long spaceId);
}
