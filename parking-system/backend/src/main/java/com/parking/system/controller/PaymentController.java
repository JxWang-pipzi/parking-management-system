package com.parking.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.system.common.Response;
import com.parking.system.entity.PaymentRecord;
import com.parking.system.mapper.PaymentRecordMapper;
import com.parking.system.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付控制器
 */
@Api(tags = "支付管理")
@Slf4j
@RestController
@RequestMapping("/payment")
@CrossOrigin
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @ApiOperation("获取支付记录列表（管理员）")
    @GetMapping("/list")
    public Response<Map<String, Object>> getPaymentList(
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        log.info("[成功][阶段1-入口][获取支付列表] 时间：{} | 参数：keyword={}, status={}, page={}, pageSize={}", 
                System.currentTimeMillis(), keyword, status, page, pageSize);
        
        try {
            Page<PaymentRecord> pageParam = new Page<>(page, pageSize);
            QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like("transaction_id", keyword).or().like("order_id", keyword);
            }
            if (status != null) {
                wrapper.eq("status", status);
            }
            wrapper.orderByDesc("create_time");
            
            Page<PaymentRecord> result = paymentRecordMapper.selectPage(pageParam, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", result.getRecords());
            response.put("total", result.getTotal());
            response.put("page", page);
            response.put("pageSize", pageSize);
            
            log.info("[成功][阶段4-结果反馈][获取支付列表] 时间：{} | 结果：total={}", 
                    System.currentTimeMillis(), result.getTotal());
            
            return Response.success(response);
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取支付列表] 时间：{} | 原因：{}", 
                    System.currentTimeMillis(), e.getMessage());
            return Response.error("获取支付记录列表失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("创建支付订单")
    @PostMapping("/create")
    public Response<Map<String, Object>> createPayment(
            @ApiParam("订单ID") @RequestParam Long orderId,
            @ApiParam("用户ID") @RequestParam Long userId,
            @ApiParam("支付金额") @RequestParam BigDecimal amount,
            @ApiParam("支付方式") @RequestParam Integer paymentMethod,
            @ApiParam("支付描述") @RequestParam(required = false) String description) {
        
        log.info("创建支付订单: orderId={}, userId={}, amount={}, paymentMethod={}", 
            orderId, userId, amount, paymentMethod);
        
        Map<String, Object> result = paymentService.createPayment(orderId, userId, amount, paymentMethod, description);
        
        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("查询支付状态")
    @GetMapping("/status")
    public Response<Map<String, Object>> queryPaymentStatus(
            @ApiParam("商户订单号") @RequestParam String outTradeNo,
            @ApiParam("支付方式") @RequestParam Integer paymentMethod) {
        
        Map<String, Object> result = paymentService.queryPaymentStatus(outTradeNo, paymentMethod);
        
        if ((Boolean) result.get("success")) {
            return Response.success(result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("取消支付")
    @PostMapping("/cancel")
    public Response<Void> cancelPayment(
            @ApiParam("商户订单号") @RequestParam String outTradeNo,
            @ApiParam("支付方式") @RequestParam Integer paymentMethod) {
        
        log.info("取消支付: outTradeNo={}, paymentMethod={}", outTradeNo, paymentMethod);
        
        boolean success = paymentService.cancelPayment(outTradeNo, paymentMethod);
        
        if (success) {
            return Response.success("支付取消成功", null);
        } else {
            return Response.error("取消支付失败");
        }
    }
    
    @ApiOperation("申请退款")
    @PostMapping("/refund")
    public Response<Map<String, Object>> refundPayment(@RequestBody Map<String, Object> params) {
        String outTradeNo = (String) params.get("transactionId");
        Object refundAmountObj = params.get("refundAmount");
        String reason = (String) params.get("reason");

        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            return Response.error("交易号不能为空");
        }
        if (refundAmountObj == null) {
            return Response.error("退款金额不能为空");
        }

        BigDecimal refundAmount;
        try {
            refundAmount = new BigDecimal(refundAmountObj.toString());
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.error("退款金额必须大于0");
            }
        } catch (NumberFormatException e) {
            return Response.error("退款金额格式错误");
        }

        log.info("申请退款: outTradeNo={}, refundAmount={}, reason={}", outTradeNo, refundAmount, reason);
        
        Map<String, Object> result = paymentService.refundPayment(outTradeNo, refundAmount, reason);
        
        if ((Boolean) result.get("success")) {
            return Response.success("退款申请成功", result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("查询退款状态")
    @GetMapping("/refund/status")
    public Response<Map<String, Object>> queryRefundStatus(
            @ApiParam("退款ID") @RequestParam String refundId,
            @ApiParam("支付方式") @RequestParam Integer paymentMethod) {
        
        Map<String, Object> result = paymentService.queryRefundStatus(refundId, paymentMethod);
        
        if ((Boolean) result.get("success")) {
            return Response.success(result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("获取支付记录")
    @GetMapping("/record")
    public Response<PaymentRecord> getPaymentRecord(
            @ApiParam("商户订单号") @RequestParam String outTradeNo) {
        
        PaymentRecord paymentRecord = paymentService.getPaymentRecord(outTradeNo);
        
        if (paymentRecord != null) {
            return Response.success(paymentRecord);
        } else {
            return Response.error("支付记录不存在");
        }
    }
    
    @ApiOperation("检查支付是否过期")
    @GetMapping("/expired")
    public Response<Boolean> isPaymentExpired(
            @ApiParam("商户订单号") @RequestParam String outTradeNo) {
        
        boolean expired = paymentService.isPaymentExpired(outTradeNo);
        
        return Response.success(expired);
    }
    
    @ApiOperation("微信支付回调")
    @PostMapping("/callback/wechat")
    public String wechatPaymentCallback(HttpServletRequest request) {
        try {
            // 读取回调数据
            StringBuilder callbackData = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                callbackData.append(line);
            }
            
            log.info("收到微信支付回调: {}", callbackData.toString());
            
            boolean success = paymentService.handlePaymentCallback(callbackData.toString(), 0);
            
            if (success) {
                return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            } else {
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
            }
            
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统错误]]></return_msg></xml>";
        }
    }
    
    @ApiOperation("支付宝支付回调")
    @PostMapping("/callback/alipay")
    public String alipayPaymentCallback(HttpServletRequest request) {
        try {
            // 获取支付宝回调参数
            Map<String, String[]> parameterMap = request.getParameterMap();
            StringBuilder callbackData = new StringBuilder();
            
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                callbackData.append(entry.getKey()).append("=").append(entry.getValue()[0]).append("&");
            }
            
            log.info("收到支付宝支付回调: {}", callbackData.toString());
            
            boolean success = paymentService.handlePaymentCallback(callbackData.toString(), 1);
            
            if (success) {
                return "success";
            } else {
                return "fail";
            }
            
        } catch (Exception e) {
            log.error("处理支付宝支付回调失败", e);
            return "fail";
        }
    }
    
    @ApiOperation("支付返回页面")
    @GetMapping("/return")
    public Response<String> paymentReturn(@RequestParam Map<String, String> params) {
        log.info("支付返回: {}", params);
        return Response.success("支付处理完成");
    }
}