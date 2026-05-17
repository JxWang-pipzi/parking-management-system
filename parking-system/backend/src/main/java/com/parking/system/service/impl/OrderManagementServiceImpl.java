package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.entity.ParkingLot;
import com.parking.system.mapper.OrderMapper;
import com.parking.system.mapper.ParkingSpaceMapper;
import com.parking.system.mapper.ParkingLotMapper;
import com.parking.system.service.OrderManagementService;
import com.parking.system.service.WebSocketService;
import com.parking.system.service.ParkingLotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单管理服务实现
 */
@Slf4j
@Service
public class OrderManagementServiceImpl implements OrderManagementService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;
    
    @Autowired
    private ParkingLotMapper parkingLotMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private ParkingLotService parkingLotService;
    
    // 停车费率（元/小时）
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("10.00");
    
    @Override
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long parkingSpaceId, String plateNumber) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取停车位信息
            ParkingSpace space = parkingSpaceMapper.selectById(parkingSpaceId);
            if (space == null) {
                result.put("success", false);
                result.put("message", "停车位不存在");
                return result;
            }
            
            // 检查停车位状态
            if (space.getStatus() != 0 && space.getStatus() != 2) {
                result.put("success", false);
                result.put("message", "停车位不可用");
                return result;
            }
            
            // 获取停车场信息
            ParkingLot lot = parkingLotMapper.selectById(space.getParkingLotId());
            if (lot == null) {
                result.put("success", false);
                result.put("message", "停车场不存在");
                return result;
            }
            
            // 生成订单号
            String orderNo = generateOrderNo();
            
            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setParkingLotId(space.getParkingLotId());
            order.setParkingSpaceId(parkingSpaceId);
            order.setPlateNumber(plateNumber);
            order.setStartTime(new Date());
            order.setStatus(3); // 进行中
            order.setCreateTime(new Date());
            order.setInvoiceRequested(false);
            
            int inserted = orderMapper.insert(order);
            
            if (inserted > 0) {
                // 更新停车位状态为占用
                space.setStatus(1); // 占用
                parkingSpaceMapper.updateById(space);
                
                // 更新停车场可用车位数
                parkingLotService.updateAvailableSpaces(space.getParkingLotId(), -1);
                
                // 推送订单创建通知
                webSocketService.pushUserMessage(userId, "订单创建成功", Map.of(
                    "orderId", order.getId(),
                    "orderNo", orderNo,
                    "parkingLotName", lot.getName(),
                    "spaceNumber", space.getSpaceNumber(),
                    "startTime", order.getStartTime()
                ));
                
                result.put("success", true);
                result.put("message", "订单创建成功");
                result.put("data", Map.of(
                    "orderId", order.getId(),
                    "orderNo", orderNo,
                    "parkingLotName", lot.getName(),
                    "spaceNumber", space.getSpaceNumber(),
                    "startTime", order.getStartTime()
                ));
                
                log.info("订单创建成功: orderId={}, userId={}, spaceId={}", 
                    order.getId(), userId, parkingSpaceId);
                
            } else {
                result.put("success", false);
                result.put("message", "订单创建失败");
            }
            
        } catch (Exception e) {
            log.error("创建订单失败: userId={}, spaceId={}", userId, parkingSpaceId, e);
            result.put("success", false);
            result.put("message", "系统错误，订单创建失败");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, Integer newStatus) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            
            Integer oldStatus = order.getStatus();
            order.setStatus(newStatus);
            order.setUpdateTime(new Date());
            
            // 根据状态更新相关时间字段
            switch (newStatus) {
                case 1: // 已完成
                    order.setPaymentTime(new Date());
                    order.setCompletionTime(new Date());
                    break;
                case 2: // 已取消
                    order.setCancellationTime(new Date());
                    break;
            }
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                // 推送状态更新通知
                webSocketService.pushUserMessage(order.getUserId(), "订单状态更新", Map.of(
                    "orderId", orderId,
                    "orderNo", order.getOrderNo(),
                    "oldStatus", getStatusText(oldStatus),
                    "newStatus", getStatusText(newStatus)
                ));
                
                log.info("订单状态更新: orderId={}, status: {} -> {}", orderId, oldStatus, newStatus);
                return true;
            }
            
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}, newStatus={}", orderId, newStatus, e);
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public Map<String, Object> completeOrder(Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            if (order.getStatus() != 3) { // 必须是进行中状态
                result.put("success", false);
                result.put("message", "订单状态不正确");
                return result;
            }
            
            // 计算停车时长和费用
            Date endTime = new Date();
            long durationMillis = endTime.getTime() - order.getStartTime().getTime();
            int durationMinutes = (int) (durationMillis / (60 * 1000));
            
            // 获取停车场费率
            ParkingLot lot = parkingLotMapper.selectById(order.getParkingLotId());
            BigDecimal hourlyRate = lot != null && lot.getHourlyRate() != null ? 
                lot.getHourlyRate() : DEFAULT_HOURLY_RATE;
            
            // 计算费用（按分钟计算，不足1分钟按1分钟计）
            BigDecimal hours = new BigDecimal(durationMinutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
            BigDecimal amount = hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
            
            // 更新订单
            order.setEndTime(endTime);
            order.setDuration(durationMinutes);
            order.setAmount(amount);
            order.setActualAmount(amount); // 暂时没有优惠
            order.setStatus(1); // 已完成
            order.setPaymentTime(endTime);
            order.setCompletionTime(endTime);
            order.setUpdateTime(endTime);
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                // 释放停车位
                ParkingSpace space = parkingSpaceMapper.selectById(order.getParkingSpaceId());
                if (space != null) {
                    space.setStatus(0); // 空闲
                    parkingSpaceMapper.updateById(space);
                    
                    // 更新停车场可用车位数
                    parkingLotService.updateAvailableSpaces(order.getParkingLotId(), 1);
                }
                
                // 推送订单完成通知
                webSocketService.pushUserMessage(order.getUserId(), "支付离场成功", Map.of(
                    "orderId", orderId,
                    "orderNo", order.getOrderNo(),
                    "duration", durationMinutes,
                    "amount", amount
                ));
                
                result.put("success", true);
                result.put("message", "订单已完成，车位已释放");
                result.put("data", Map.of(
                    "orderId", orderId,
                    "orderNo", order.getOrderNo(),
                    "startTime", order.getStartTime(),
                    "endTime", endTime,
                    "duration", durationMinutes,
                    "amount", amount
                ));
                
                log.info("订单完成: orderId={}, duration={}分钟, amount={}", 
                    orderId, durationMinutes, amount);
                
            } else {
                result.put("success", false);
                result.put("message", "订单完成失败");
            }
            
        } catch (Exception e) {
            log.error("完成订单失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "系统错误，订单完成失败");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public boolean cancelOrder(Long orderId, String reason) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            
            if (order.getStatus() != 0 && order.getStatus() != 3) {
                return false;
            }
            
            int originalStatus = order.getStatus();
            
            order.setStatus(2);
            order.setCancellationTime(new Date());
            order.setCancellationReason(reason);
            order.setUpdateTime(new Date());
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                if (originalStatus == 3) {
                    ParkingSpace space = parkingSpaceMapper.selectById(order.getParkingSpaceId());
                    if (space != null) {
                        space.setStatus(0); // 空闲
                        parkingSpaceMapper.updateById(space);
                        
                        // 更新停车场可用车位数
                        parkingLotService.updateAvailableSpaces(order.getParkingLotId(), 1);
                    }
                }
                
                // 推送取消通知
                webSocketService.pushUserMessage(order.getUserId(), "订单已取消", Map.of(
                    "orderId", orderId,
                    "orderNo", order.getOrderNo(),
                    "reason", reason
                ));
                
                log.info("订单取消: orderId={}, reason={}", orderId, reason);
                return true;
            }
            
        } catch (Exception e) {
            log.error("取消订单失败: orderId={}", orderId, e);
        }
        
        return false;
    }
    
    @Override
    public Map<String, Object> getOrderDetails(Long orderId) {
        Map<String, Object> details = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                details.put("exists", false);
                return details;
            }
            
            details.put("exists", true);
            details.put("order", order);
            
            // 获取停车场信息
            if (order.getParkingLotId() != null) {
                ParkingLot lot = parkingLotMapper.selectById(order.getParkingLotId());
                details.put("parkingLot", lot);
            }
            
            // 获取停车位信息
            if (order.getParkingSpaceId() != null) {
                ParkingSpace space = parkingSpaceMapper.selectById(order.getParkingSpaceId());
                details.put("parkingSpace", space);
            }
            
            // 计算停车时长（如果订单进行中）
            if (order.getStatus() == 3 && order.getStartTime() != null) {
                long currentDuration = (System.currentTimeMillis() - order.getStartTime().getTime()) / (60 * 1000);
                details.put("currentDuration", currentDuration);
            }
            
        } catch (Exception e) {
            log.error("获取订单详情失败: orderId={}", orderId, e);
            details.put("error", "获取订单详情失败");
        }
        
        return details;
    }
    
    @Override
    public List<Order> getUserOrders(Long userId, Integer status, Integer page, Integer pageSize) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            
            if (status != null) {
                wrapper.eq("status", status);
            }
            
            wrapper.orderByDesc("create_time");
            
            List<Order> orders;
            if (page != null && pageSize != null) {
                Page<Order> pageObj = new Page<>(page, pageSize);
                orders = orderMapper.selectPage(pageObj, wrapper).getRecords();
            } else {
                orders = orderMapper.selectList(wrapper);
            }
            fillParkingLotName(orders);
            return orders;
            
        } catch (Exception e) {
            log.error("获取用户订单列表失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Order> getParkingLotOrders(Long parkingLotId, Integer status, Integer page, Integer pageSize) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("parking_lot_id", parkingLotId);
            
            if (status != null) {
                wrapper.eq("status", status);
            }
            
            wrapper.orderByDesc("create_time");
            
            List<Order> orders;
            if (page != null && pageSize != null) {
                Page<Order> pageObj = new Page<>(page, pageSize);
                orders = orderMapper.selectPage(pageObj, wrapper).getRecords();
            } else {
                orders = orderMapper.selectList(wrapper);
            }
            fillParkingLotName(orders);
            return orders;
            
        } catch (Exception e) {
            log.error("获取停车场订单列表失败: parkingLotId={}", parkingLotId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> requestInvoice(Long orderId, String invoiceType, String invoiceTitle, 
                                             String taxNo, String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            // 只有已支付的订单可以申请发票
            if (order.getStatus() != 1) {
                result.put("success", false);
                result.put("message", "订单状态不正确，只有已支付订单可以申请发票");
                return result;
            }
            
            // 检查是否已申请发票
            if (Boolean.TRUE.equals(order.getInvoiceRequested())) {
                result.put("success", false);
                result.put("message", "该订单已申请过发票");
                return result;
            }
            
            // 更新订单发票信息
            order.setInvoiceRequested(true);
            order.setInvoiceType(invoiceType);
            order.setInvoiceTitle(invoiceTitle);
            order.setInvoiceTaxNo(taxNo);
            order.setInvoiceEmail(email);
            order.setInvoiceStatus("PENDING");
            order.setUpdateTime(new Date());
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                // 推送发票申请通知
                webSocketService.pushUserMessage(order.getUserId(), "发票申请成功", Map.of(
                    "orderId", orderId,
                    "orderNo", order.getOrderNo(),
                    "invoiceType", invoiceType,
                    "invoiceTitle", invoiceTitle
                ));
                
                result.put("success", true);
                result.put("message", "发票申请成功，我们将在24小时内开具发票");
                
                log.info("发票申请成功: orderId={}, invoiceType={}, invoiceTitle={}", 
                    orderId, invoiceType, invoiceTitle);
                
            } else {
                result.put("success", false);
                result.put("message", "发票申请失败");
            }
            
        } catch (Exception e) {
            log.error("申请发票失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "系统错误，发票申请失败");
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public Map<String, Object> generateInvoice(Long orderId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || !Boolean.TRUE.equals(order.getInvoiceRequested())) {
                result.put("success", false);
                result.put("message", "订单不存在或未申请发票");
                return result;
            }
            
            // 生成发票号
            String invoiceNo = generateInvoiceNo();
            
            // 生成发票PDF（这里简化处理，实际应该生成真实的PDF文件）
            String invoiceUrl = "/invoices/" + invoiceNo + ".pdf";
            
            // 更新订单发票信息
            order.setInvoiceNo(invoiceNo);
            order.setInvoiceGeneratedTime(new Date());
            order.setInvoiceUrl(invoiceUrl);
            order.setInvoiceStatus("GENERATED");
            order.setUpdateTime(new Date());
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                result.put("success", true);
                result.put("message", "发票生成成功");
                result.put("data", Map.of(
                    "invoiceNo", invoiceNo,
                    "invoiceUrl", invoiceUrl,
                    "generatedTime", order.getInvoiceGeneratedTime()
                ));
                
                log.info("发票生成成功: orderId={}, invoiceNo={}", orderId, invoiceNo);
                
            } else {
                result.put("success", false);
                result.put("message", "发票生成失败");
            }
            
        } catch (Exception e) {
            log.error("生成发票失败: orderId={}", orderId, e);
            result.put("success", false);
            result.put("message", "系统错误，发票生成失败");
        }
        
        return result;
    }
    
    @Override
    public boolean sendInvoiceByEmail(Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getInvoiceUrl() == null) {
                return false;
            }
            
            // 这里应该实现真实的邮件发送逻辑
            // 简化处理，只记录日志
            log.info("发送发票到邮箱: orderId={}, email={}, invoiceUrl={}", 
                orderId, order.getInvoiceEmail(), order.getInvoiceUrl());
            
            // 更新发票状态
            order.setInvoiceStatus("SENT");
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
            
            // 推送发票发送通知
            webSocketService.pushUserMessage(order.getUserId(), "发票已发送", Map.of(
                "orderId", orderId,
                "orderNo", order.getOrderNo(),
                "email", order.getInvoiceEmail()
            ));
            
            return true;
            
        } catch (Exception e) {
            log.error("发送发票失败: orderId={}", orderId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getInvoiceInfo(Long orderId) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                info.put("exists", false);
                return info;
            }
            
            info.put("exists", true);
            info.put("invoiceRequested", order.getInvoiceRequested());
            
            if (Boolean.TRUE.equals(order.getInvoiceRequested())) {
                info.put("invoiceType", order.getInvoiceType());
                info.put("invoiceTitle", order.getInvoiceTitle());
                info.put("invoiceTaxNo", order.getInvoiceTaxNo());
                info.put("invoiceEmail", order.getInvoiceEmail());
                info.put("invoiceNo", order.getInvoiceNo());
                info.put("invoiceGeneratedTime", order.getInvoiceGeneratedTime());
                info.put("invoiceUrl", order.getInvoiceUrl());
                info.put("invoiceStatus", order.getInvoiceStatus());
            }
            
        } catch (Exception e) {
            log.error("获取发票信息失败: orderId={}", orderId, e);
            info.put("error", "获取发票信息失败");
        }
        
        return info;
    }
    
    @Override
    public byte[] downloadInvoice(Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getInvoiceUrl() == null) {
                return null;
            }
            
            // 这里应该实现真实的PDF文件读取逻辑
            // 简化处理，返回空数组
            log.info("下载发票: orderId={}, invoiceUrl={}", orderId, order.getInvoiceUrl());
            
            return new byte[0];
            
        } catch (Exception e) {
            log.error("下载发票失败: orderId={}", orderId, e);
            return null;
        }
    }
    
    @Override
    @Transactional
    public boolean rateOrder(Long orderId, Integer rating, String feedback) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            
            // 只有已完成的订单可以评价
            if (order.getStatus() != 1) {
                return false;
            }
            
            order.setRating(rating);
            order.setFeedback(feedback);
            order.setUpdateTime(new Date());
            
            int updated = orderMapper.updateById(order);
            
            if (updated > 0) {
                log.info("订单评价成功: orderId={}, rating={}", orderId, rating);
                return true;
            }
            
        } catch (Exception e) {
            log.error("订单评价失败: orderId={}", orderId, e);
        }
        
        return false;
    }
    
    @Override
    public Map<String, Object> getOrderStatistics(Long userId, Date startDate, Date endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            
            if (startDate != null) {
                wrapper.ge("create_time", startDate);
            }
            if (endDate != null) {
                wrapper.le("create_time", endDate);
            }
            
            List<Order> orders = orderMapper.selectList(wrapper);
            
            long totalOrders = orders.size();
            long completedOrders = orders.stream().filter(o -> o.getStatus() == 1).count();
            long cancelledOrders = orders.stream().filter(o -> o.getStatus() == 2).count();
            
            BigDecimal totalAmount = orders.stream()
                .filter(o -> o.getAmount() != null)
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            int totalDuration = orders.stream()
                .filter(o -> o.getDuration() != null)
                .mapToInt(Order::getDuration)
                .sum();
            
            stats.put("totalOrders", totalOrders);
            stats.put("completedOrders", completedOrders);
            stats.put("cancelledOrders", cancelledOrders);
            stats.put("totalAmount", totalAmount);
            stats.put("totalDuration", totalDuration);
            stats.put("avgDuration", totalOrders > 0 ? totalDuration / totalOrders : 0);
            
        } catch (Exception e) {
            log.error("获取订单统计失败: userId={}", userId, e);
        }
        
        return stats;
    }
    
    @Override
    public List<Order> searchOrders(String keyword, Integer status, Date startDate, Date endDate, 
                                   Integer page, Integer pageSize) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("order_no", keyword)
                    .or().like("plate_number", keyword));
            }
            
            if (status != null) {
                wrapper.eq("status", status);
            }
            
            if (startDate != null) {
                wrapper.ge("create_time", startDate);
            }
            if (endDate != null) {
                wrapper.le("create_time", endDate);
            }
            
            wrapper.orderByDesc("create_time");
            
            List<Order> orders;
            if (page != null && pageSize != null) {
                Page<Order> pageObj = new Page<>(page, pageSize);
                orders = orderMapper.selectPage(pageObj, wrapper).getRecords();
            } else {
                orders = orderMapper.selectList(wrapper);
            }
            fillParkingLotName(orders);
            return orders;
            
        } catch (Exception e) {
            log.error("搜索订单失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public byte[] exportOrders(Long userId, Date startDate, Date endDate) {
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            
            if (startDate != null) {
                wrapper.ge("create_time", startDate);
            }
            if (endDate != null) {
                wrapper.le("create_time", endDate);
            }
            
            List<Order> orders = orderMapper.selectList(wrapper);
            
            // 这里应该实现真实的Excel导出逻辑
            // 简化处理，返回空数组
            log.info("导出订单: userId={}, count={}", userId, orders.size());
            
            return new byte[0];
            
        } catch (Exception e) {
            log.error("导出订单失败: userId={}", userId, e);
            return null;
        }
    }
    
    @Override
    public Map<String, Object> getOrderReceipt(Long orderId) {
        Map<String, Object> receipt = new HashMap<>();
        
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                receipt.put("exists", false);
                return receipt;
            }
            
            receipt.put("exists", true);
            receipt.put("orderNo", order.getOrderNo());
            receipt.put("plateNumber", order.getPlateNumber());
            receipt.put("startTime", order.getStartTime());
            receipt.put("endTime", order.getEndTime());
            receipt.put("duration", order.getDuration());
            receipt.put("amount", order.getAmount());
            receipt.put("actualAmount", order.getActualAmount());
            receipt.put("discountAmount", order.getDiscountAmount());
            receipt.put("paymentTime", order.getPaymentTime());
            
            // 获取停车场信息
            if (order.getParkingLotId() != null) {
                ParkingLot lot = parkingLotMapper.selectById(order.getParkingLotId());
                if (lot != null) {
                    receipt.put("parkingLotName", lot.getName());
                    receipt.put("parkingLotAddress", lot.getAddress());
                }
            }
            
            // 获取停车位信息
            if (order.getParkingSpaceId() != null) {
                ParkingSpace space = parkingSpaceMapper.selectById(order.getParkingSpaceId());
                if (space != null) {
                    receipt.put("spaceNumber", space.getSpaceNumber());
                }
            }
            
        } catch (Exception e) {
            log.error("获取订单收据失败: orderId={}", orderId, e);
            receipt.put("error", "获取订单收据失败");
        }
        
        return receipt;
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    /**
     * 生成发票号
     */
    private String generateInvoiceNo() {
        return "INV" + System.currentTimeMillis();
    }
    
    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已完成";
            case 2: return "已取消";
            case 3: return "进行中";
            case 5: return "已退款";
            default: return "未知状态";
        }
    }

    @Override
    public Map<String, Object> getOrdersList(String keyword, Integer page, Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("order_no", keyword)
                    .or().like("plate_number", keyword));
            }
            
            wrapper.orderByDesc("create_time");
            
            Page<Order> pageObj = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
            Page<Order> orderPage = orderMapper.selectPage(pageObj, wrapper);
            
            List<Order> records = orderPage.getRecords();
            fillParkingLotName(records);
            
            result.put("records", records);
            result.put("total", orderPage.getTotal());
            result.put("current", orderPage.getCurrent());
            result.put("size", orderPage.getSize());
            
        } catch (Exception e) {
            log.error("获取订单列表失败: keyword={}", keyword, e);
            result.put("records", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public boolean updateOrder(Order order) {
        try {
            order.setUpdateTime(new Date());
            return orderMapper.updateById(order) > 0;
        } catch (Exception e) {
            log.error("更新订单失败: orderId={}", order.getId(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteOrder(Long orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
            
            // 如果订单正在进行中，需要释放停车位
            if (order.getStatus() == 3 && order.getParkingSpaceId() != null) {
                ParkingSpace space = parkingSpaceMapper.selectById(order.getParkingSpaceId());
                if (space != null) {
                    space.setStatus(0); // 设置为空闲
                    space.setUpdateTime(new Date());
                    parkingSpaceMapper.updateById(space);
                    
                    // 更新停车场可用车位数
                    if (space.getParkingLotId() != null) {
                        parkingLotService.updateAvailableSpaces(space.getParkingLotId(), 1);
                    }
                }
            }
            
            return orderMapper.deleteById(orderId) > 0;
        } catch (Exception e) {
            log.error("删除订单失败: orderId={}", orderId, e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public Map<String, Object> batchDeleteOrders(List<Long> orderIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long orderId : orderIds) {
                if (deleteOrder(orderId)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", String.format("成功删除%d个订单，失败%d个", successCount, failCount));
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            
        } catch (Exception e) {
            log.error("批量删除订单失败", e);
            result.put("success", false);
            result.put("message", "批量删除订单失败");
        }
        
        return result;
    }
    
    @Override
    public List<Order> getAllOrders(Integer page, Integer pageSize) {
        try {
            Page<Order> pageObj = new Page<>(page, pageSize);
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("create_time");
            
            Page<Order> resultPage = orderMapper.selectPage(pageObj, wrapper);
            List<Order> orders = resultPage.getRecords();
            fillParkingLotName(orders);
            return orders;
        } catch (Exception e) {
            log.error("获取所有订单失败", e);
            return new ArrayList<>();
        }
    }
    
    private void fillParkingLotName(List<Order> orders) {
        for (Order order : orders) {
            if (order.getParkingLotId() != null) {
                ParkingLot lot = parkingLotMapper.selectById(order.getParkingLotId());
                if (lot != null) {
                    order.setParkingLotName(lot.getName());
                }
            }
        }
    }
}
