package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import java.math.BigDecimal;

@Data
@TableName("parking_user_behavior")
public class ParkingUserBehavior {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long parkingLotId;
    
    private Long spaceId;
    
    private Integer behaviorType;
    
    private Integer timeSlot;
    
    private Integer weekday;
    
    private Integer duration;
    
    private BigDecimal amount;
    
    private Double satisfactionScore;
    
    private String behaviorTags;
    
    private Date behaviorTime;
    
    private Date createTime;
}