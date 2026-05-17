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
@TableName("payment_record")
public class PaymentRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private BigDecimal amount;

    private Integer paymentMethod;

    private String transactionId;

    private Integer status;

    private Date createTime;

    @TableField(exist = false)
    private Date updateTime;

    @TableField(exist = false)
    private String outTradeNo;

    @TableField(exist = false)
    private String paymentPlatformOrderId;

    @TableField(exist = false)
    private String paymentUrl;

    @TableField(exist = false)
    private String qrCode;

    @TableField(exist = false)
    private Date paymentTime;

    @TableField(exist = false)
    private Date expireTime;

    @TableField(exist = false)
    private String failureReason;

    @TableField(exist = false)
    private String refundId;

    @TableField(exist = false)
    private BigDecimal refundAmount;

    @TableField(exist = false)
    private Date refundTime;

    @TableField(exist = false)
    private String callbackData;

    @TableField(exist = false)
    private String paymentChannel;

}