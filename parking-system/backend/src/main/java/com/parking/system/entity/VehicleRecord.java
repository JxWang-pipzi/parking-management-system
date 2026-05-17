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
@TableName("vehicle_record")
public class VehicleRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long parkingLotId;

    private Long parkingSpaceId;

    private String plateNumber;

    private Date entryTime;

    private Date exitTime;

    private Integer status;

    private String plateImageUrl;

    private BigDecimal recognitionConfidence;

    private Date createTime;

    private Date updateTime;

    @TableField(exist = false)
    private String parkingLotName;

    @TableField(exist = false)
    private String spaceNumber;
}
