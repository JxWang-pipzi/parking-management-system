package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.mapper.PaymentRecordMapper;
import com.parking.system.service.PaymentService;
import com.parking.system.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 支付监控服务
 * 定期检查支付状态和处理超时支付
 */
@Slf4j
@Service
public class PaymentMonitorService {
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    /**
     * 每5分钟检查一次支付状态
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkPaymentStatus() {
        try {
            // 查询待支付的订单
            QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0); // 待支付
            wrapper.lt("create_time", new Date(System.currentTimeMillis() - 60000)); // 创建时间超过1分钟
            wrapper.gt("expire_time", new Date()); // 未过期
            
            List<PaymentRecord> pendingPayments = paymentRecordMapper.selectList(wrapper);
            
            for (PaymentRecord payment : pendingPayments) {
                // 查询支付状态
                Map<String, Object> result = paymentService.queryPaymentStatus(
                    payment.getOutTradeNo(), payment.getPaymentMethod());
                
                if ((Boolean) result.get("success") && result.containsKey("status")) {
                    Integer status = (Integer) result.get("status");
                    String transactionId = (String) result.get("transactionId");
                    
                    // 如果状态有变化，更新本地记录
                    if (!status.equals(payment.getStatus())) {
                        paymentService.updatePaymentStatus(payment.getOutTradeNo(), status, transactionId);
                        
                        log.info("支付状态更新: outTradeNo={}, oldStatus={}, newStatus={}", 
                            payment.getOutTradeNo(), payment.getStatus(), status);
                    }
                }
            }
            
            if (!pendingPayments.isEmpty()) {
                log.debug("支付状态检查完成，检查了 {} 个待支付订单", pendingPayments.size());
            }
            
        } catch (Exception e) {
            log.error("检查支付状态失败", e);
        }
    }
    
    /**
     * 每10分钟处理一次过期支付
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void handleExpiredPayments() {
        try {
            // 查询过期的待支付订单
            QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0); // 待支付
            wrapper.lt("expire_time", new Date()); // 已过期
            
            List<PaymentRecord> expiredPayments = paymentRecordMapper.selectList(wrapper);
            
            for (PaymentRecord payment : expiredPayments) {
                // 取消过期支付
                boolean cancelled = paymentService.cancelPayment(payment.getOutTradeNo(), payment.getPaymentMethod());
                
                if (cancelled) {
                    // 推送过期通知
                    webSocketService.pushUserMessage(payment.getUserId(), "支付已过期", Map.of(
                        "outTradeNo", payment.getOutTradeNo(),
                        "amount", payment.getAmount(),
                        "expireTime", payment.getExpireTime()
                    ));
                    
                    log.info("处理过期支付: outTradeNo={}, userId={}", 
                        payment.getOutTradeNo(), payment.getUserId());
                }
            }
            
            if (!expiredPayments.isEmpty()) {
                log.info("过期支付处理完成，处理了 {} 个过期支付", expiredPayments.size());
            }
            
        } catch (Exception e) {
            log.error("处理过期支付失败", e);
        }
    }
    
    /**
     * 每小时统计支付情况
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void logPaymentStatistics() {
        try {
            Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
            
            // 统计最近一小时的支付情况
            QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
            wrapper.gt("create_time", oneHourAgo);
            
            List<PaymentRecord> recentPayments = paymentRecordMapper.selectList(wrapper);
            
            long totalCount = recentPayments.size();
            long successCount = recentPayments.stream().filter(p -> p.getStatus() == 1).count();
            long failedCount = recentPayments.stream().filter(p -> p.getStatus() == 2).count();
            long pendingCount = recentPayments.stream().filter(p -> p.getStatus() == 0).count();
            
            log.info("支付统计（最近1小时）: 总计={}, 成功={}, 失败={}, 待支付={}", 
                totalCount, successCount, failedCount, pendingCount);
            
            // 推送系统统计信息
            if (totalCount > 0) {
                webSocketService.pushSystemBroadcast("支付系统统计", Map.of(
                    "period", "最近1小时",
                    "totalCount", totalCount,
                    "successCount", successCount,
                    "failedCount", failedCount,
                    "pendingCount", pendingCount,
                    "successRate", totalCount > 0 ? (double) successCount / totalCount : 0.0
                ));
            }
            
        } catch (Exception e) {
            log.error("统计支付情况失败", e);
        }
    }
}