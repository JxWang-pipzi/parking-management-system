package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("parking_sensor_data")
public class ParkingSensorData {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sensorId;
    
    private Long parkingLotId;
    
    private Long spaceId;
    
    private Double rawValue;
    
    private Double processedValue;
    
    private Integer dataType;
    
    private Integer dataQuality;
    
    private String qualityMetrics;
    
    private Integer isAnomaly;
    
    private String anomalyType;
    
    private Date collectTime;
    
    private Date processTime;
    
    private Date createTime;
}
