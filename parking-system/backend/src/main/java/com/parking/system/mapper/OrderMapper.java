package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends BaseMapper<Order> {
    
    List<Order> selectByParkingLotId(@Param("parkingLotId") Long parkingLotId);
    
    List<Order> selectByUserId(@Param("userId") Long userId);
}
