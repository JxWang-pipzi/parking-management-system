package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("parking_recommendation")
public class ParkingRecommendation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long parkingLotId;
    
    private Long spaceId;
    
    private Double recommendationScore;
    
    private String recommendationReason;
    
    private Integer recommendationType;
    
    private Integer timeSlot;
    
    private Integer weekday;
    
    private Integer status;
    
    private Integer clickCount;
    
    private Integer useCount;
    
    private Date expireTime;
    
    private Date createTime;
}
