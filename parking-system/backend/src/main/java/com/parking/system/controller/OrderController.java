package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingLot;
import com.parking.system.service.OrderService;
import com.parking.system.service.ParkingLotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/orders")
@Api(tags = "订单管理")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private ParkingLotService parkingLotService;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        return principal instanceof Long ? (Long) principal : null;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean canAccess(Order order) {
        Long userId = currentUserId();
        return order != null && (isAdmin() || (userId != null && userId.equals(order.getUserId())));
    }

    private void enrichOrder(Order order) {
        if (order != null && order.getParkingLotId() != null) {
            ParkingLot lot = parkingLotService.getById(order.getParkingLotId());
            if (lot != null) {
                order.setParkingLotName(lot.getName());
            }
        }
    }

    @GetMapping
    @ApiOperation("获取订单列表")
    public Response<List<Order>> getOrders(@RequestParam(required = false) Integer status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][获取订单列表] 时间：{} | 参数：userId={}, status={}", System.currentTimeMillis(), principal, status);

        if (!(principal instanceof Long)) {
            log.warn("[失败][阶段4][获取订单列表] 时间：{} | 原因：未登录", System.currentTimeMillis());
            return Response.error("未登录");
        }

        Long userId = (Long) principal;
        List<Order> orders = orderService.getOrdersByUserId(userId, status);

        log.info("[成功][阶段4][返回订单列表] 时间：{} | 结果：共{}个订单", System.currentTimeMillis(), orders.size());
        return Response.success("获取成功", orders);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取订单详情")
    public Response<Order> getOrder(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][获取订单详情] 时间：{} | 参数：orderId={}", System.currentTimeMillis(), id);

        Order order = orderService.getById(id);
        if (order != null && canAccess(order)) {
            enrichOrder(order);
            log.info("[成功][阶段4][返回订单] 时间：{} | 结果：id={}", System.currentTimeMillis(), id);
            return Response.success("获取成功", order);
        }
        if (order != null) {
            log.warn("[失败][阶段4][越权访问订单] 时间：{} | 原因：orderId={}", System.currentTimeMillis(), id);
            return Response.error(403, "无权访问该订单");
        }
        log.warn("[失败][阶段4][订单不存在] 时间：{} | 原因：id={}", System.currentTimeMillis(), id);
        return Response.error("订单不存在");
    }

    @PostMapping
    @ApiOperation("创建订单")
    public Response<Order> createOrder(@RequestBody Map<String, Object> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][创建订单] 时间：{} | 参数：{}", System.currentTimeMillis(), params);

        if (!(principal instanceof Long)) {
            log.warn("[失败][阶段4][创建订单] 时间：{} | 原因：未登录", System.currentTimeMillis());
            return Response.error("未登录");
        }

        Long userId = (Long) principal;

        if (params.get("parkingLotId") == null) {
            log.warn("[失败][阶段2][创建订单] 时间：{} | 原因：停车场ID为空", System.currentTimeMillis());
            return Response.error("停车场ID不能为空");
        }
        if (params.get("plateNumber") == null || params.get("plateNumber").toString().trim().isEmpty()) {
            log.warn("[失败][阶段2][创建订单] 时间：{} | 原因：车牌号为空", System.currentTimeMillis());
            return Response.error("车牌号不能为空");
        }
        String plateNumber = params.get("plateNumber").toString().trim();
        String plateNumberRegex = "^[\\u4e00-\\u9fa5][A-Z][A-Z0-9]{5,6}$";
        if (!plateNumber.matches(plateNumberRegex)) {
            log.warn("[失败][阶段2][创建订单] 时间：{} | 原因：车牌号格式不合法 | 参数：plateNumber={}", System.currentTimeMillis(), plateNumber);
            return Response.error("车牌号格式不合法，需以中文省份简称开头+大写字母+5-6位字母数字");
        }

        Long parkingLotId = Long.valueOf(params.get("parkingLotId").toString());
        Long parkingSpaceId = params.get("parkingSpaceId") != null ? Long.valueOf(params.get("parkingSpaceId").toString()) : null;

        Order order = orderService.createOrder(userId, parkingLotId, parkingSpaceId, plateNumber);
        if (order != null) {
            log.info("[成功][阶段4][订单创建成功] 时间：{} | 结果：orderId={}", System.currentTimeMillis(), order.getId());
            return Response.success("创建成功", order);
        }
        log.warn("[失败][阶段4][订单创建失败] 时间：{} | 原因：创建失败", System.currentTimeMillis());
        return Response.error("创建失败");
    }

    @PostMapping("/{id}/pay")
    @ApiOperation("支付订单")
    public Response<?> payOrder(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        log.info("[成功][阶段2][支付订单] 时间：{} | 参数：orderId={}", System.currentTimeMillis(), id);

        Order order = orderService.getById(id);
        if (order == null) {
            return Response.error("订单不存在");
        }
        if (!canAccess(order)) {
            return Response.error(403, "无权操作该订单");
        }

        if (params.get("paymentMethod") == null) {
            log.warn("[失败][阶段2][支付订单] 时间：{} | 原因：支付方式为空 | 参数：orderId={}", System.currentTimeMillis(), id);
            return Response.error("支付方式不能为空");
        }

        Integer paymentMethod;
        try {
            paymentMethod = Integer.valueOf(params.get("paymentMethod").toString());
        } catch (NumberFormatException e) {
            log.warn("[失败][阶段2][支付订单] 时间：{} | 原因：支付方式格式错误 | 参数：orderId={}", System.currentTimeMillis(), id);
            return Response.error("支付方式格式错误");
        }

        if (orderService.payOrder(id, paymentMethod)) {
            log.info("[成功][阶段4][支付成功] 时间：{} | 结果：orderId={}", System.currentTimeMillis(), id);
            return Response.success("支付成功");
        }
        log.warn("[失败][阶段4][支付失败] 时间：{} | 原因：orderId={}", System.currentTimeMillis(), id);
        return Response.error("支付失败");
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Response<?> cancelOrder(@PathVariable Long id) {
        log.info("[成功][阶段2][取消订单] 时间：{} | 参数：orderId={}", System.currentTimeMillis(), id);

        Order order = orderService.getById(id);
        if (order == null) {
            return Response.error("订单不存在");
        }
        if (!canAccess(order)) {
            return Response.error(403, "无权操作该订单");
        }

        if (orderService.cancelOrder(id)) {
            log.info("[成功][阶段4][取消成功] 时间：{} | 结果：orderId={}", System.currentTimeMillis(), id);
            return Response.success("取消成功");
        }
        log.warn("[失败][阶段4][取消失败] 时间：{} | 原因：orderId={}", System.currentTimeMillis(), id);
        return Response.error("取消失败");
    }

    @PutMapping("/{id}/complete")
    @ApiOperation("完成订单（车辆出场）")
    public Response<?> completeOrder(@PathVariable Long id) {
        log.info("[成功][阶段2][完成订单] 时间：{} | 参数：orderId={}", System.currentTimeMillis(), id);

        Order order = orderService.getById(id);
        if (order == null) {
            return Response.error("订单不存在");
        }
        if (!canAccess(order)) {
            return Response.error(403, "无权操作该订单");
        }

        if (orderService.completeOrder(id)) {
            log.info("[成功][阶段4][完成成功] 时间：{} | 结果：orderId={}", System.currentTimeMillis(), id);
            return Response.success("订单已完成，车位已释放");
        }
        log.warn("[失败][阶段4][完成失败] 时间：{} | 原因：orderId={}", System.currentTimeMillis(), id);
        return Response.error("完成失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Response<?> deleteOrder(@PathVariable Long id) {
        log.info("[成功][阶段2][删除订单] 时间：{} | 参数：orderId={}", System.currentTimeMillis(), id);

        Order order = orderService.getById(id);
        if (order == null) {
            return Response.error("订单不存在");
        }
        if (!canAccess(order)) {
            return Response.error(403, "无权操作该订单");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            log.warn("[失败][阶段2][删除订单] 时间：{} | 原因：订单状态不允许删除，当前状态={} | 参数：orderId={}", System.currentTimeMillis(), order.getStatus(), id);
            return Response.error("只能删除已完成或已取消的订单");
        }

        if (orderService.removeById(id)) {
            log.info("[成功][阶段4][删除成功] 时间：{} | 结果：orderId={}", System.currentTimeMillis(), id);
            return Response.success("删除成功");
        }
        log.warn("[失败][阶段4][删除失败] 时间：{} | 原因：orderId={}", System.currentTimeMillis(), id);
        return Response.error("删除失败");
    }
}
