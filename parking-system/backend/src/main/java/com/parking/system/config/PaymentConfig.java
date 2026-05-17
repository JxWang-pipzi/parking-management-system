package com.parking.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {
    
    // 微信支付配置
    private WechatConfig wechat = new WechatConfig();
    
    // 支付宝配置
    private AlipayConfig alipay = new AlipayConfig();
    
    // 通用配置
    private String notifyUrl = "http://localhost:8080/api/payment/callback";
    private String returnUrl = "http://localhost:8080/api/payment/return";
    private Integer paymentTimeoutMinutes = 30; // 支付超时时间（分钟）
    
    @Data
    public static class WechatConfig {
        private String appId = "your_wechat_app_id";
        private String mchId = "your_wechat_mch_id";
        private String apiKey = "your_wechat_api_key";
        private String certPath = "classpath:cert/wechat_cert.p12";
        private String apiV3Key = "your_wechat_api_v3_key";
        private String serialNo = "your_wechat_serial_no";
        private String privateKeyPath = "classpath:cert/wechat_private_key.pem";
        private boolean sandbox = true; // 是否使用沙箱环境
    }
    
    @Data
    public static class AlipayConfig {
        private String appId = "your_alipay_app_id";
        private String privateKey = "your_alipay_private_key";
        private String publicKey = "your_alipay_public_key";
        private String alipayPublicKey = "alipay_public_key";
        private String signType = "RSA2";
        private String charset = "UTF-8";
        private String format = "json";
        private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do"; // 沙箱环境
        private boolean sandbox = true; // 是否使用沙箱环境
    }
}