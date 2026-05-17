package com.parking.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.system.entity.VehicleRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VehicleRecordMapper extends BaseMapper<VehicleRecord> {

    @Select("SELECT vr.*, pl.name as parking_lot_name, ps.space_number " +
            "FROM vehicle_record vr " +
            "LEFT JOIN parking_lot pl ON vr.parking_lot_id = pl.id " +
            "LEFT JOIN parking_space ps ON vr.parking_space_id = ps.id " +
            "WHERE vr.status = #{status}")
    List<VehicleRecord> findByStatus(Integer status);

    @Select("SELECT vr.*, pl.name as parking_lot_name, ps.space_number " +
            "FROM vehicle_record vr " +
            "LEFT JOIN parking_lot pl ON vr.parking_lot_id = pl.id " +
            "LEFT JOIN parking_space ps ON vr.parking_space_id = ps.id " +
            "ORDER BY vr.create_time DESC")
    List<VehicleRecord> findAllWithDetails();
}
