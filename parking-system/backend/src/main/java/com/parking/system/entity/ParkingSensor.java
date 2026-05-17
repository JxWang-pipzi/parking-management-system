package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("parking_sensor")
public class ParkingSensor {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parkingLotId;
    
    private Long spaceId;
    
    private String sensorCode;
    
    private Integer sensorType;
    
    private String sensorName;
    
    private String manufacturer;
    
    private String model;
    
    private Integer status;
    
    private Double lastValue;
    
    private Date lastUpdateTime;
    
    private Integer dataQuality;
    
    private String ipAddress;
    
    private Integer port;
    
    private String config;
    
    private Date createTime;
    
    private Date updateTime;
}
