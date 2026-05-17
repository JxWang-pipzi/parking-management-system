package com.parking.system.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.parking.system.config.PaymentConfig;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.mapper.PaymentRecordMapper;
import com.parking.system.service.PaymentService;
import com.parking.system.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 支付服务实现
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    private PaymentConfig paymentConfig;
    
    @Autowired
    private WebSocketService webSocketService;
    
    private AlipayClient alipayClient;
    
    // 初始化支付宝客户端
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            PaymentConfig.AlipayConfig config = paymentConfig.getAlipay();
            alipayClient = new DefaultAlipayClient(
                config.getGatewayUrl(),
                config.getAppId(),
                config.getPrivateKey(),
                config.getFormat(),
                config.getCharset(),
                config.getAlipayPublicKey(),
                config.getSignType()
            );
        }
        return alipayClient;
    }
    
    @Override
    @Transactional
    public Map<String, Object> createPayment(Long orderId, Long userId, BigDecimal amount, 
                                           Integer paymentMethod, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 生成商户订单号
            String outTradeNo = generateOutTradeNo(orderId);
            
            // 创建支付记录
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setOrderId(orderId);
            paymentRecord.setUserId(userId);
            paymentRecord.setAmount(amount);
            paymentRecord.setPaymentMethod(paymentMethod);
            paymentRecord.setOutTradeNo(outTradeNo);
            paymentRecord.setStatus(0); // 待支付
            paymentRecord.setCreateTime(new Date());
            paymentRecord.setExpireTime(new Date(System.currentTimeMillis() + 
                paymentConfig.getPaymentTimeoutMinutes() * 60 * 1000));
            
            // 根据支付方式创建支付订单
            Map<String, Object> paymentResult;
            switch (paymentMethod) {
                case 0: // 微信支付
                    paymentResult = createWechatPayment(orderId, userId, amount, description);
                    break;
                case 1: // 支付宝
                    paymentResult = createAlipayPayment(orderId, userId, amount, description);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式");
                    return result;
            }
            
            if ((Boolean) paymentResult.get("success")) {
                // 更新支付记录
                paymentRecord.setPaymentUrl((String) paymentResult.get("paymentUrl"));
                paymentRecord.setQrCode((String) paymentResult.get("qrCode"));
                paymentRecord.setPaymentPlatformOrderId((String) paymentResult.get("platformOrderId"));
                
                paymentRecordMapper.insert(paymentRecord);
                
                result.put("success", true);
                result.put("message", "支付订单创建成功");
                result.put("data", Map.of(
                    "outTradeNo", outTradeNo,
                    "paymentUrl", paymentResult.get("paymentUrl"),
                    "qrCode", paymentResult.get("qrCode"),
                    "amount", amount,
                    "expireTime", paymentRecord.getExpireTime()
                ));
                
                // 推送支付创建通知
                webSocketService.pushUserMessage(userId, "支付订单已创建", Map.of(
                    "outTradeNo", outTradeNo,
                    "amount", amount,
                    "paymentMethod", getPaymentMethodText(paymentMethod)
                ));
                
            } else {
                result.put("success", false);
                result.put("message", paymentResult.get("message"));
            }
            
        } catch (Exception e) {
            log.error("创建支付订单失败: orderId={}, userId={}", orderId, userId, e);
            result.put("success", false);
            result.put("message", "创建支付订单失败");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> createWechatPayment(Long orderId, Long userId, BigDecimal amount, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 注意：这里是微信支付的简化实现
            // 实际项目中需要使用微信支付SDK进行真实的API调用
            
            log.info("创建微信支付订单: orderId={}, amount={}", orderId, amount);
            
            // 模拟微信支付响应
            String paymentUrl = "weixin://wxpay/bizpayurl?pr=" + UUID.randomUUID().toString().replace("-", "");
            String qrCode = generatePaymentQRCode(paymentUrl);
            
            result.put("success", true);
            result.put("paymentUrl", paymentUrl);
            result.put("qrCode", qrCode);
            result.put("platformOrderId", "wx_" + System.currentTimeMillis());
            
            log.info("微信支付订单创建成功: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("创建微信支付订单失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "微信支付订单创建失败");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> createAlipayPayment(Long orderId, Long userId, BigDecimal amount, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AlipayClient client = getAlipayClient();
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            
            String outTradeNo = generateOutTradeNo(orderId);
            
            // 设置请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("total_amount", amount.toString());
            bizContent.put("subject", description != null ? description : "停车费支付");
            bizContent.put("store_id", "parking_lot_001");
            bizContent.put("timeout_express", paymentConfig.getPaymentTimeoutMinutes() + "m");
            
            request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            request.setNotifyUrl(paymentConfig.getNotifyUrl() + "/alipay");
            
            AlipayTradePrecreateResponse response = client.execute(request);
            
            if (response.isSuccess()) {
                String qrCode = generatePaymentQRCode(response.getQrCode());
                
                result.put("success", true);
                result.put("paymentUrl", response.getQrCode());
                result.put("qrCode", qrCode);
                result.put("platformOrderId", response.getOutTradeNo());
                
                log.info("支付宝支付订单创建成功: orderId={}, outTradeNo={}", orderId, outTradeNo);
                
            } else {
                result.put("success", false);
                result.put("message", "支付宝订单创建失败: " + response.getMsg());
                log.error("支付宝支付订单创建失败: orderId={}, error={}", orderId, response.getMsg());
            }
            
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付订单失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "支付宝支付订单创建失败");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryPaymentStatus(String outTradeNo, Integer paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            PaymentRecord paymentRecord = getPaymentRecord(outTradeNo);
            if (paymentRecord == null) {
                result.put("success", false);
                result.put("message", "支付记录不存在");
                return result;
            }
            
            // 根据支付方式查询状态
            switch (paymentMethod) {
                case 0: // 微信支付
                    result = queryWechatPaymentStatus(outTradeNo);
                    break;
                case 1: // 支付宝
                    result = queryAlipayPaymentStatus(outTradeNo);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式");
                    return result;
            }
            
            // 更新本地支付记录状态
            if ((Boolean) result.get("success") && result.containsKey("status")) {
                Integer status = (Integer) result.get("status");
                String transactionId = (String) result.get("transactionId");
                updatePaymentStatus(outTradeNo, status, transactionId);
            }
            
        } catch (Exception e) {
            log.error("查询支付状态失败: outTradeNo={}", outTradeNo, e);
            result.put("success", false);
            result.put("message", "查询支付状态失败");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean handlePaymentCallback(String callbackData, Integer paymentMethod) {
        try {
            log.info("处理支付回调: paymentMethod={}, data={}", paymentMethod, callbackData);
            
            // 根据支付方式处理回调
            switch (paymentMethod) {
                case 0: // 微信支付
                    return handleWechatCallback(callbackData);
                case 1: // 支付宝
                    return handleAlipayCallback(callbackData);
                default:
                    log.warn("不支持的支付方式回调: {}", paymentMethod);
                    return false;
            }
            
        } catch (Exception e) {
            log.error("处理支付回调失败: paymentMethod={}", paymentMethod, e);
            return false;
        }
    }
    
    @Override
    public boolean cancelPayment(String outTradeNo, Integer paymentMethod) {
        try {
            PaymentRecord paymentRecord = getPaymentRecord(outTradeNo);
            if (paymentRecord == null || paymentRecord.getStatus() != 0) {
                return false;
            }
            
            // 更新支付记录状态为取消
            paymentRecord.setStatus(2); // 支付失败/取消
            paymentRecord.setFailureReason("用户取消支付");
            paymentRecord.setUpdateTime(new Date());
            
            int updated = paymentRecordMapper.updateById(paymentRecord);
            
            if (updated > 0) {
                // 推送取消通知
                webSocketService.pushUserMessage(paymentRecord.getUserId(), "支付已取消", Map.of(
                    "outTradeNo", outTradeNo,
                    "amount", paymentRecord.getAmount()
                ));
                
                log.info("支付取消成功: outTradeNo={}", outTradeNo);
                return true;
            }
            
        } catch (Exception e) {
            log.error("取消支付失败: outTradeNo={}", outTradeNo, e);
        }
        
        return false;
    }
    
    @Override
    public Map<String, Object> refundPayment(String outTradeNo, BigDecimal refundAmount, String reason) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            PaymentRecord paymentRecord = getPaymentRecord(outTradeNo);
            if (paymentRecord == null || paymentRecord.getStatus() != 1) {
                result.put("success", false);
                result.put("message", "支付记录不存在或状态不正确");
                return result;
            }
            
            // 检查退款金额
            if (refundAmount.compareTo(paymentRecord.getAmount()) > 0) {
                result.put("success", false);
                result.put("message", "退款金额不能大于支付金额");
                return result;
            }
            
            // 根据支付方式进行退款
            switch (paymentRecord.getPaymentMethod()) {
                case 0: // 微信支付
                    result = refundWechatPayment(outTradeNo, refundAmount, reason);
                    break;
                case 1: // 支付宝
                    result = refundAlipayPayment(outTradeNo, refundAmount, reason);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式");
                    return result;
            }
            
            // 更新支付记录
            if ((Boolean) result.get("success")) {
                paymentRecord.setStatus(3); // 已退款
                paymentRecord.setRefundAmount(refundAmount);
                paymentRecord.setRefundTime(new Date());
                paymentRecord.setRefundId((String) result.get("refundId"));
                paymentRecord.setUpdateTime(new Date());
                
                paymentRecordMapper.updateById(paymentRecord);
                
                // 推送退款通知
                webSocketService.pushUserMessage(paymentRecord.getUserId(), "退款成功", Map.of(
                    "outTradeNo", outTradeNo,
                    "refundAmount", refundAmount,
                    "reason", reason
                ));
            }
            
        } catch (Exception e) {
            log.error("申请退款失败: outTradeNo={}", outTradeNo, e);
            result.put("success", false);
            result.put("message", "申请退款失败");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> queryRefundStatus(String refundId, Integer paymentMethod) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据支付方式查询退款状态
            switch (paymentMethod) {
                case 0: // 微信支付
                    result = queryWechatRefundStatus(refundId);
                    break;
                case 1: // 支付宝
                    result = queryAlipayRefundStatus(refundId);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "不支持的支付方式");
                    return result;
            }
            
        } catch (Exception e) {
            log.error("查询退款状态失败: refundId={}", refundId, e);
            result.put("success", false);
            result.put("message", "查询退款状态失败");
        }
        
        return result;
    }
    
    @Override
    public String generatePaymentQRCode(String paymentUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(paymentUrl, BarcodeFormat.QR_CODE, 300, 300);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (WriterException | IOException e) {
            log.error("生成支付二维码失败: paymentUrl={}", paymentUrl, e);
            return null;
        }
    }
    
    @Override
    public boolean isPaymentExpired(String outTradeNo) {
        try {
            PaymentRecord paymentRecord = getPaymentRecord(outTradeNo);
            if (paymentRecord == null) {
                return true;
            }
            
            return paymentRecord.getExpireTime() != null && 
                   paymentRecord.getExpireTime().before(new Date());
                   
        } catch (Exception e) {
            log.error("检查支付是否过期失败: outTradeNo={}", outTradeNo, e);
            return true;
        }
    }
    
    @Override
    public PaymentRecord getPaymentRecord(String outTradeNo) {
        try {
            return paymentRecordMapper.selectByOutTradeNo(outTradeNo);
        } catch (Exception e) {
            log.error("获取支付记录失败: outTradeNo={}", outTradeNo, e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean updatePaymentStatus(String outTradeNo, Integer status, String transactionId) {
        try {
            PaymentRecord paymentRecord = getPaymentRecord(outTradeNo);
            if (paymentRecord == null) {
                return false;
            }
            
            paymentRecord.setStatus(status);
            paymentRecord.setTransactionId(transactionId);
            paymentRecord.setUpdateTime(new Date());
            
            if (status == 1) { // 支付成功
                paymentRecord.setPaymentTime(new Date());
            }
            
            int updated = paymentRecordMapper.updateById(paymentRecord);
            
            if (updated > 0) {
                // 推送支付状态更新
                webSocketService.pushUserMessage(paymentRecord.getUserId(), "支付状态更新", Map.of(
                    "outTradeNo", outTradeNo,
                    "status", status,
                    "statusText", getPaymentStatusText(status),
                    "amount", paymentRecord.getAmount()
                ));
                
                return true;
            }
            
        } catch (Exception e) {
            log.error("更新支付状态失败: outTradeNo={}, status={}", outTradeNo, status, e);
        }
        
        return false;
    }
    
    /**
     * 生成商户订单号
     */
    private String generateOutTradeNo(Long orderId) {
        return "PK" + System.currentTimeMillis() + orderId;
    }
    
    /**
     * 查询微信支付状态
     */
    private Map<String, Object> queryWechatPaymentStatus(String outTradeNo) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟微信支付查询
        // 实际项目中需要调用微信支付查询API
        log.info("查询微信支付状态: outTradeNo={}", outTradeNo);
        
        // 模拟返回支付成功
        result.put("success", true);
        result.put("status", 1); // 支付成功
        result.put("transactionId", "wx_" + System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 查询支付宝支付状态
     */
    private Map<String, Object> queryAlipayPaymentStatus(String outTradeNo) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AlipayClient client = getAlipayClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            
            AlipayTradeQueryResponse response = client.execute(request);
            
            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                
                result.put("success", true);
                result.put("transactionId", response.getTradeNo());
                
                switch (tradeStatus) {
                    case "TRADE_SUCCESS":
                    case "TRADE_FINISHED":
                        result.put("status", 1); // 支付成功
                        break;
                    case "WAIT_BUYER_PAY":
                        result.put("status", 0); // 待支付
                        break;
                    case "TRADE_CLOSED":
                        result.put("status", 2); // 支付失败/关闭
                        break;
                    default:
                        result.put("status", 0); // 默认待支付
                }
                
            } else {
                result.put("success", false);
                result.put("message", response.getMsg());
            }
            
        } catch (AlipayApiException e) {
            log.error("查询支付宝支付状态失败: outTradeNo={}", outTradeNo, e);
            result.put("success", false);
            result.put("message", "查询支付状态失败");
        }
        
        return result;
    }
    
    /**
     * 处理微信支付回调
     */
    private boolean handleWechatCallback(String callbackData) {
        try {
            // 实际项目中需要验证微信支付回调签名
            log.info("处理微信支付回调: {}", callbackData);
            
            // 模拟解析回调数据
            // 实际需要解析XML或JSON格式的回调数据
            
            return true;
            
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return false;
        }
    }
    
    /**
     * 处理支付宝回调
     */
    private boolean handleAlipayCallback(String callbackData) {
        try {
            // 实际项目中需要验证支付宝回调签名
            log.info("处理支付宝回调: {}", callbackData);
            
            // 模拟解析回调数据
            // 实际需要解析表单数据
            
            return true;
            
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return false;
        }
    }
    
    /**
     * 微信支付退款
     */
    private Map<String, Object> refundWechatPayment(String outTradeNo, BigDecimal refundAmount, String reason) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟微信支付退款
        log.info("微信支付退款: outTradeNo={}, amount={}", outTradeNo, refundAmount);
        
        result.put("success", true);
        result.put("refundId", "wx_refund_" + System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 支付宝退款
     */
    private Map<String, Object> refundAlipayPayment(String outTradeNo, BigDecimal refundAmount, String reason) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            AlipayClient client = getAlipayClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);
            bizContent.put("refund_amount", refundAmount.toString());
            bizContent.put("refund_reason", reason);
            bizContent.put("out_request_no", "refund_" + System.currentTimeMillis());
            
            request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));
            
            AlipayTradeRefundResponse response = client.execute(request);
            
            if (response.isSuccess()) {
                result.put("success", true);
                result.put("refundId", response.getOutTradeNo());
            } else {
                result.put("success", false);
                result.put("message", response.getMsg());
            }
            
        } catch (AlipayApiException e) {
            log.error("支付宝退款失败: outTradeNo={}", outTradeNo, e);
            result.put("success", false);
            result.put("message", "退款失败");
        }
        
        return result;
    }
    
    /**
     * 查询微信退款状态
     */
    private Map<String, Object> queryWechatRefundStatus(String refundId) {
        Map<String, Object> result = new HashMap<>();
        
        // 模拟微信退款查询
        log.info("查询微信退款状态: refundId={}", refundId);
        
        result.put("success", true);
        result.put("status", "SUCCESS");
        
        return result;
    }
    
    /**
     * 查询支付宝退款状态
     */
    private Map<String, Object> queryAlipayRefundStatus(String refundId) {
        Map<String, Object> result = new HashMap<>();
        
        // 支付宝退款是实时的，不需要单独查询状态
        result.put("success", true);
        result.put("status", "SUCCESS");
        
        return result;
    }
    
    /**
     * 获取支付方式文本
     */
    private String getPaymentMethodText(Integer paymentMethod) {
        switch (paymentMethod) {
            case 0: return "微信支付";
            case 1: return "支付宝";
            case 2: return "银联支付";
            default: return "未知支付方式";
        }
    }
    
    /**
     * 获取支付状态文本
     */
    private String getPaymentStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "支付成功";
            case 2: return "支付失败";
            case 3: return "已退款";
            default: return "未知状态";
        }
    }
}