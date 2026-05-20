package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("parking_space")
public class ParkingSpace implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private String spaceNumber;

    private Integer type; // 0：普通车位，1：残疾人车位，2：VIP车位

    private Integer status; // 0：空闲，1：占用，2：预约

    private Date createTime;

    private Date updateTime;
    
    // 预约相关字段 - 数据库暂无这些字段，标记为不存在
    @TableField(exist = false)
    private Long reservedUserId; // 预约用户ID
    
    @TableField(exist = false)
    private Date reservationTime; // 预约时间
    
    @TableField(exist = false)
    private Date reservationExpireTime; // 预约过期时间
    
    @TableField(exist = false)
    private String reservationStatus; // 预约状态：ACTIVE, EXPIRED, CANCELLED, USED

    @TableField(exist = false)
    private String parkingLotName;

}