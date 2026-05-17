package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("parking_lot")
public class ParkingLot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private Integer totalSpaces;

    private Integer availableSpaces;

    private BigDecimal hourlyRate;

    private Double latitude = 0.0;

    private Double longitude = 0.0;

    private Integer status; // 0：关闭，1：开放

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private Double distance;

    @TableField(exist = false)
    private Integer routeDistanceMeters;

    @TableField(exist = false)
    private Integer routeDurationSeconds;

    @TableField(exist = false)
    private String routeDistanceText;

    @TableField(exist = false)
    private String routeDurationText;

    @TableField(exist = false)
    private String routeStrategy;

    @TableField(exist = false)
    private String routeProvider;
}
