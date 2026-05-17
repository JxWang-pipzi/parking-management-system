package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.Order;
import com.parking.system.service.OrderManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@Api(tags = "订单管理")
@Slf4j
@RestController
@RequestMapping("/order-management")
@CrossOrigin
public class OrderManagementController {
    
    @Autowired
    private OrderManagementService orderManagementService;
    
    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Response<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Long parkingSpaceId = Long.valueOf(params.get("parkingSpaceId").toString());
        String plateNumber = params.get("plateNumber").toString();
        
        log.info("创建订单: userId={}, spaceId={}, plateNumber={}", userId, parkingSpaceId, plateNumber);
        
        Map<String, Object> result = orderManagementService.createOrder(userId, parkingSpaceId, plateNumber);
        
        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("完成订单")
    @PostMapping("/complete/{orderId}")
    public Response<Map<String, Object>> completeOrder(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        log.info("完成订单: orderId={}", orderId);
        
        Map<String, Object> result = orderManagementService.completeOrder(orderId);
        
        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderId}")
    public Response<Void> cancelOrder(
            @ApiParam("订单ID") @PathVariable Long orderId,
            @ApiParam("取消原因") @RequestParam String reason) {
        
        log.info("取消订单: orderId={}, reason={}", orderId, reason);
        
        boolean success = orderManagementService.cancelOrder(orderId, reason);
        
        if (success) {
            return Response.success("订单取消成功", null);
        } else {
            return Response.error("取消订单失败");
        }
    }
    
    @ApiOperation("获取订单详情")
    @GetMapping("/details/{orderId}")
    public Response<Map<String, Object>> getOrderDetails(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        Map<String, Object> details = orderManagementService.getOrderDetails(orderId);
        
        if ((Boolean) details.getOrDefault("exists", false)) {
            return Response.success(details);
        } else {
            return Response.error("订单不存在");
        }
    }
    
    @ApiOperation("获取用户订单列表")
    @GetMapping("/user/{userId}")
    public Response<List<Order>> getUserOrders(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        List<Order> orders = orderManagementService.getUserOrders(userId, status, page, pageSize);
        
        return Response.success(orders);
    }
    
    @ApiOperation("获取停车场订单列表")
    @GetMapping("/parking-lot/{parkingLotId}")
    public Response<List<Order>> getParkingLotOrders(
            @ApiParam("停车场ID") @PathVariable Long parkingLotId,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        List<Order> orders = orderManagementService.getParkingLotOrders(parkingLotId, status, page, pageSize);
        
        return Response.success(orders);
    }
    
    @ApiOperation("申请发票")
    @PostMapping("/invoice/request")
    public Response<Map<String, Object>> requestInvoice(
            @ApiParam("订单ID") @RequestParam Long orderId,
            @ApiParam("发票类型") @RequestParam String invoiceType,
            @ApiParam("发票抬头") @RequestParam String invoiceTitle,
            @ApiParam("纳税人识别号") @RequestParam(required = false) String taxNo,
            @ApiParam("接收邮箱") @RequestParam String email) {
        
        log.info("申请发票: orderId={}, invoiceType={}, invoiceTitle={}", orderId, invoiceType, invoiceTitle);
        
        Map<String, Object> result = orderManagementService.requestInvoice(orderId, invoiceType, invoiceTitle, taxNo, email);
        
        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("生成发票（管理员功能）")
    @PostMapping("/invoice/generate/{orderId}")
    public Response<Map<String, Object>> generateInvoice(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        log.info("生成发票: orderId={}", orderId);
        
        Map<String, Object> result = orderManagementService.generateInvoice(orderId);
        
        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }
    
    @ApiOperation("发送发票到邮箱")
    @PostMapping("/invoice/send/{orderId}")
    public Response<Void> sendInvoiceByEmail(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        log.info("发送发票: orderId={}", orderId);
        
        boolean success = orderManagementService.sendInvoiceByEmail(orderId);
        
        if (success) {
            return Response.success("发票发送成功", null);
        } else {
            return Response.error("发送发票失败");
        }
    }
    
    @ApiOperation("获取发票信息")
    @GetMapping("/invoice/info/{orderId}")
    public Response<Map<String, Object>> getInvoiceInfo(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        Map<String, Object> info = orderManagementService.getInvoiceInfo(orderId);
        
        return Response.success(info);
    }
    
    @ApiOperation("下载发票")
    @GetMapping("/invoice/download/{orderId}")
    public void downloadInvoice(
            @ApiParam("订单ID") @PathVariable Long orderId,
            HttpServletResponse response) throws IOException {
        
        log.info("下载发票: orderId={}", orderId);
        
        byte[] invoiceData = orderManagementService.downloadInvoice(orderId);
        
        if (invoiceData != null && invoiceData.length > 0) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=invoice_" + orderId + ".pdf");
            response.getOutputStream().write(invoiceData);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "发票不存在");
        }
    }
    
    @ApiOperation("订单评价")
    @PostMapping("/rate")
    public Response<Void> rateOrder(
            @ApiParam("订单ID") @RequestParam Long orderId,
            @ApiParam("评分（1-5星）") @RequestParam Integer rating,
            @ApiParam("反馈内容") @RequestParam(required = false) String feedback) {
        
        log.info("订单评价: orderId={}, rating={}", orderId, rating);
        
        boolean success = orderManagementService.rateOrder(orderId, rating, feedback);
        
        if (success) {
            return Response.success("评价成功", null);
        } else {
            return Response.error("评价失败");
        }
    }
    
    @ApiOperation("获取订单统计")
    @GetMapping("/statistics/{userId}")
    public Response<Map<String, Object>> getOrderStatistics(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        
        Map<String, Object> stats = orderManagementService.getOrderStatistics(userId, startDate, endDate);
        
        return Response.success(stats);
    }
    
    @ApiOperation("搜索订单")
    @GetMapping("/search")
    public Response<List<Order>> searchOrders(
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        List<Order> orders = orderManagementService.searchOrders(keyword, status, startDate, endDate, page, pageSize);
        
        return Response.success(orders);
    }
    
    @ApiOperation("导出订单")
    @GetMapping("/export/{userId}")
    public void exportOrders(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @ApiParam("结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletResponse response) throws IOException {
        
        log.info("导出订单: userId={}", userId);
        
        byte[] excelData = orderManagementService.exportOrders(userId, startDate, endDate);
        
        if (excelData != null && excelData.length > 0) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=orders_" + userId + ".xlsx");
            response.getOutputStream().write(excelData);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "无订单数据");
        }
    }
    
    @ApiOperation("获取订单收据")
    @GetMapping("/receipt/{orderId}")
    public Response<Map<String, Object>> getOrderReceipt(
            @ApiParam("订单ID") @PathVariable Long orderId) {
        
        Map<String, Object> receipt = orderManagementService.getOrderReceipt(orderId);
        
        if ((Boolean) receipt.getOrDefault("exists", false)) {
            return Response.success(receipt);
        } else {
            return Response.error("订单不存在");
        }
    }

    @ApiOperation("获取订单列表（管理员，带分页）")
    @GetMapping("/list")
    public Response<Map<String, Object>> getOrdersList(
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        Map<String, Object> result = orderManagementService.getOrdersList(keyword, page, pageSize);
        return Response.success(result);
    }

    @ApiOperation("更新订单信息（管理员功能）")
    @PutMapping("/update/{orderId}")
    public Response<Order> updateOrder(
            @ApiParam("订单ID") @PathVariable Long orderId,
            @RequestBody Order order) {

        log.info("更新订单: orderId={}", orderId);

        order.setId(orderId);
        boolean success = orderManagementService.updateOrder(order);

        if (success) {
            return Response.success("订单更新成功", order);
        } else {
            return Response.error("更新订单失败");
        }
    }

    @ApiOperation("删除订单（管理员功能）")
    @DeleteMapping("/delete/{orderId}")
    public Response<Void> deleteOrder(
            @ApiParam("订单ID") @PathVariable Long orderId) {

        log.info("删除订单: orderId={}", orderId);

        boolean success = orderManagementService.deleteOrder(orderId);

        if (success) {
            return Response.success("订单删除成功", null);
        } else {
            return Response.error("删除订单失败");
        }
    }

    @ApiOperation("批量删除订单（管理员功能）")
    @DeleteMapping("/batch-delete")
    public Response<Map<String, Object>> batchDeleteOrders(
            @ApiParam("订单ID列表") @RequestBody List<Long> orderIds) {

        log.info("批量删除订单: count={}", orderIds.size());

        Map<String, Object> result = orderManagementService.batchDeleteOrders(orderIds);

        if ((Boolean) result.get("success")) {
            return Response.success((String) result.get("message"), result);
        } else {
            return Response.error((String) result.get("message"));
        }
    }

    @ApiOperation("获取所有订单（管理员功能）")
    @GetMapping("/all")
    public Response<List<Order>> getAllOrders(
            @ApiParam("页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam("每页数量") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        List<Order> orders = orderManagementService.getAllOrders(page, pageSize);

        return Response.success(orders);
    }
}
