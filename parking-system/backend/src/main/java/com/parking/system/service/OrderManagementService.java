package com.parking.system.service;

import com.parking.system.entity.Order;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 订单管理服务接口
 */
public interface OrderManagementService {
    
    /**
     * 创建订单
     */
    Map<String, Object> createOrder(Long userId, Long parkingSpaceId, String plateNumber);
    
    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, Integer newStatus);
    
    /**
     * 完成订单（结束停车）
     */
    Map<String, Object> completeOrder(Long orderId);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, String reason);
    
    /**
     * 获取订单详情
     */
    Map<String, Object> getOrderDetails(Long orderId);
    
    /**
     * 获取用户订单列表
     */
    List<Order> getUserOrders(Long userId, Integer status, Integer page, Integer pageSize);
    
    /**
     * 获取停车场订单列表
     */
    List<Order> getParkingLotOrders(Long parkingLotId, Integer status, Integer page, Integer pageSize);
    
    /**
     * 申请发票
     */
    Map<String, Object> requestInvoice(Long orderId, String invoiceType, String invoiceTitle, 
                                      String taxNo, String email);
    
    /**
     * 生成电子发票
     */
    Map<String, Object> generateInvoice(Long orderId);
    
    /**
     * 发送发票到邮箱
     */
    boolean sendInvoiceByEmail(Long orderId);
    
    /**
     * 获取发票信息
     */
    Map<String, Object> getInvoiceInfo(Long orderId);
    
    /**
     * 下载发票
     */
    byte[] downloadInvoice(Long orderId);
    
    /**
     * 订单评价
     */
    boolean rateOrder(Long orderId, Integer rating, String feedback);
    
    /**
     * 获取订单统计
     */
    Map<String, Object> getOrderStatistics(Long userId, Date startDate, Date endDate);
    
    /**
     * 搜索订单
     */
    List<Order> searchOrders(String keyword, Integer status, Date startDate, Date endDate, 
                            Integer page, Integer pageSize);
    
    /**
     * 导出订单
     */
    byte[] exportOrders(Long userId, Date startDate, Date endDate);
    
    /**
     * 获取订单收据
     */
    Map<String, Object> getOrderReceipt(Long orderId);

    /**
     * 获取订单列表（管理员，带分页）
     */
    Map<String, Object> getOrdersList(String keyword, Integer page, Integer pageSize);
    
    /**
     * 更新订单信息（管理员功能）
     */
    boolean updateOrder(Order order);
    
    /**
     * 删除订单（管理员功能）
     */
    boolean deleteOrder(Long orderId);
    
    /**
     * 批量删除订单（管理员功能）
     */
    Map<String, Object> batchDeleteOrders(List<Long> orderIds);
    
    /**
     * 获取所有订单（管理员功能）
     */
    List<Order> getAllOrders(Integer page, Integer pageSize);
}