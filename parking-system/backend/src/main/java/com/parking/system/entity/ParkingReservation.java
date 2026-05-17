package com.parking.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("parking_reservation")
public class ParkingReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long parkingLotId;
    private Long spaceId;
    private String plateNumber;
    private LocalDateTime reservationTime;
    private Integer duration;
    private BigDecimal amount;
    private Integer status; // 0: Pending, 1: Confirmed, 2: Cancelled, 3: Completed, 4: Expired
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
