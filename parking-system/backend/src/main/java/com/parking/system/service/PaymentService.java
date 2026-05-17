package com.parking.system.service;

import com.parking.system.entity.PaymentRecord;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 创建支付订单
     */
    Map<String, Object> createPayment(Long orderId, Long userId, BigDecimal amount, 
                                     Integer paymentMethod, String description);
    
    /**
     * 微信支付 - 统一下单
     */
    Map<String, Object> createWechatPayment(Long orderId, Long userId, BigDecimal amount, String description);
    
    /**
     * 支付宝支付 - 创建订单
     */
    Map<String, Object> createAlipayPayment(Long orderId, Long userId, BigDecimal amount, String description);
    
    /**
     * 查询支付状态
     */
    Map<String, Object> queryPaymentStatus(String outTradeNo, Integer paymentMethod);
    
    /**
     * 处理支付回调
     */
    boolean handlePaymentCallback(String callbackData, Integer paymentMethod);
    
    /**
     * 取消支付
     */
    boolean cancelPayment(String outTradeNo, Integer paymentMethod);
    
    /**
     * 申请退款
     */
    Map<String, Object> refundPayment(String outTradeNo, BigDecimal refundAmount, String reason);
    
    /**
     * 查询退款状态
     */
    Map<String, Object> queryRefundStatus(String refundId, Integer paymentMethod);
    
    /**
     * 生成支付二维码
     */
    String generatePaymentQRCode(String paymentUrl);
    
    /**
     * 检查支付是否超时
     */
    boolean isPaymentExpired(String outTradeNo);
    
    /**
     * 获取支付记录
     */
    PaymentRecord getPaymentRecord(String outTradeNo);
    
    /**
     * 更新支付状态
     */
    boolean updatePaymentStatus(String outTradeNo, Integer status, String transactionId);
}