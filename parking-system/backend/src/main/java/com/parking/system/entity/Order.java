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
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long parkingLotId;

    private Long parkingSpaceId;

    private String plateNumber;

    private Date startTime;

    private Date endTime;

    private Integer duration;

    private BigDecimal amount;

    private Integer status;

    private String orderNo;

    private Date createTime;

    private Date updateTime;

    private Date paymentTime;

    private Date completionTime;

    private Date cancellationTime;

    private String cancellationReason;

    private Boolean invoiceRequested;

    private String invoiceType;

    private String invoiceTitle;

    private String invoiceTaxNo;

    private String invoiceEmail;

    private String invoiceNo;

    private Date invoiceGeneratedTime;

    private String invoiceUrl;

    private String invoiceStatus;

    private String remark;

    private BigDecimal discountAmount;

    private BigDecimal actualAmount;

    private String couponCode;

    private Integer rating;

    private String feedback;

    @TableField(exist = false)
    private String parkingLotName;

}
