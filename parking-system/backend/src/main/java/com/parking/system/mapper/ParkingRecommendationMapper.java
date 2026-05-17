package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.ParkingRecommendation;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ParkingRecommendationMapper extends BaseMapper<ParkingRecommendation> {
    
    List<ParkingRecommendation> selectByUserId(@Param("userId") Long userId);
    
    List<ParkingRecommendation> selectActiveByUserId(@Param("userId") Long userId);
}
