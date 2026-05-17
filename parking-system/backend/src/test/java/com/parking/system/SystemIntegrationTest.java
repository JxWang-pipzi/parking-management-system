package com.parking.system;

import com.parking.system.entity.Order;
import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.entity.User;
import com.parking.system.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SystemIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ParkingLotService parkingLotService;

    @Autowired
    private ParkingSpaceService parkingSpaceService;

    @Autowired
    private RecommendationService recommendationService;

    private Long testUserId;
    private Long testParkingLotId;
    private Long testParkingSpaceId;

    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setName("集成测试停车场_" + System.nanoTime());
        parkingLot.setAddress("成都市武侯区联调大道");
        parkingLot.setTotalSpaces(6);
        parkingLot.setAvailableSpaces(6);
        parkingLot.setHourlyRate(new BigDecimal("8.00"));
        parkingLot.setLatitude(30.5728);
        parkingLot.setLongitude(104.0668);
        parkingLot.setStatus(1);
        assertTrue(parkingLotService.createParkingLot(parkingLot), "集成测试停车场初始化失败");
        return parkingLot;
    }

    private Long requireAvailableSpaceId(Long parkingLotId) {
        List<ParkingSpace> spaces = parkingSpaceService.getReservableSpaces(parkingLotId);
        assertFalse(spaces.isEmpty(), "集成测试停车场应至少存在一个可用车位");
        return spaces.get(0).getId();
    }

    private String uniquePhone() {
        return "139" + String.format("%08d", System.nanoTime() % 100000000L);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@example.com";
    }

    @BeforeEach
    void setUp() {
        System.out.println("[集成测试准备] 开始初始化测试数据...");
        
        User user = new User();
        user.setUsername("integration_test_user_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("集成测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("integration"));
        userService.register(user);
        User savedUser = userService.getByUsername(user.getUsername());
        assertNotNull(savedUser, "集成测试用户初始化失败");
        testUserId = savedUser.getId();

        ParkingLot parkingLot = createTestParkingLot();
        testParkingLotId = parkingLot.getId();
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        
        System.out.println("[集成测试准备] 初始化完成");
    }

    @Test
    @DisplayName("P0-完整用户操作链路测试：注册-登录-查询-预约-支付")
    void testCompleteUserOperationChain() {
        System.out.println("[集成测试] 完整用户操作链路测试开始");
        
        System.out.println("[阶段1-入口] 用户注册");
        User newUser = new User();
        newUser.setUsername("chain_test_" + System.currentTimeMillis());
        newUser.setPassword("123456");
        newUser.setName("链路测试用户");
        newUser.setPhone(uniquePhone());
        newUser.setEmail(uniqueEmail("chain"));
        boolean registerResult = userService.register(newUser);
        assertTrue(registerResult, "注册应该成功");
        System.out.println("[成功] 用户注册完成");
        
        System.out.println("[阶段2-核心操作] 用户登录");
        User loginUser = userService.login(newUser.getUsername(), "123456");
        assertNotNull(loginUser, "登录应该成功");
        System.out.println("[成功] 用户登录完成，userId=" + loginUser.getId());
        
        System.out.println("[阶段2-核心操作] 查询附近停车场");
        List<ParkingLot> nearbyLots = recommendationService.getPersonalizedRecommendations(loginUser.getId(), 39.9042, 116.4074);
        assertNotNull(nearbyLots, "查询停车场应该成功");
        System.out.println("[成功] 查询到" + nearbyLots.size() + "个停车场");
        
        if (!nearbyLots.isEmpty()) {
            ParkingLot selectedLot = nearbyLots.get(0);
            System.out.println("[阶段2-核心操作] 查询停车场车位");
            List<ParkingSpace> spaces = parkingSpaceService.getReservableSpaces(selectedLot.getId());
            System.out.println("[成功] 查询到" + spaces.size() + "个可预约车位");
            
            if (!spaces.isEmpty()) {
                ParkingSpace selectedSpace = spaces.get(0);
                System.out.println("[阶段2-核心操作] 创建订单");
                Order order = orderService.createOrder(loginUser.getId(), selectedLot.getId(), selectedSpace.getId(), "京Z99999");
                assertNotNull(order, "创建订单应该成功");
                System.out.println("[成功] 订单创建完成，orderId=" + order.getId());
                
                System.out.println("[阶段2-核心操作] 支付订单");
                boolean completeResult = orderService.completeOrder(order.getId());
                assertTrue(completeResult, "离场结算应该成功");
                System.out.println("[成功] 订单离场完成");
                
                System.out.println("[阶段4-结果反馈] 查询订单状态");
                Order paidOrder = orderService.getById(order.getId());
                assertEquals(1, paidOrder.getStatus(), "订单状态应该为已支付");
                System.out.println("[成功] 订单状态确认完成");
            }
        }
        
        System.out.println("[测试结果] ✓ 完整用户操作链路测试通过");
    }

    @Test
    @DisplayName("P0-跨模块集成测试：用户-订单-支付-车位联动")
    void testCrossModuleIntegration() {
        System.out.println("[集成测试] 跨模块集成测试开始");
        
        System.out.println("[步骤1] 查询停车场初始状态");
        ParkingLot lot = parkingLotService.getById(testParkingLotId);
        int initialAvailable = lot.getAvailableSpaces();
        System.out.println("[信息] 初始可用车位: " + initialAvailable);
        
        System.out.println("[步骤2] 创建订单");
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京A88888");
        assertNotNull(order, "创建订单应该成功");
        System.out.println("[成功] 订单创建完成");
        
        System.out.println("[步骤3] 验证车位状态变更");
        ParkingSpace space = parkingSpaceService.getById(testParkingSpaceId);
        assertEquals(1, space.getStatus(), "车位状态应该为占用");
        System.out.println("[成功] 车位状态验证通过");
        
        System.out.println("[步骤4] 验证停车场可用车位减少");
        lot = parkingLotService.getById(testParkingLotId);
        assertEquals(initialAvailable - 1, lot.getAvailableSpaces(), "可用车位应该减少1");
        System.out.println("[成功] 停车场状态验证通过");
        
        System.out.println("[步骤5] 完成订单");
        boolean payResult = orderService.completeOrder(order.getId());
        assertTrue(payResult, "完成订单应该成功");
        System.out.println("[成功] 完成订单");
        
        System.out.println("[步骤6] 验证支付后车位释放");
        space = parkingSpaceService.getById(testParkingSpaceId);
        assertEquals(0, space.getStatus(), "车位状态应该恢复为空闲");
        System.out.println("[成功] 车位释放验证通过");
        
        System.out.println("[步骤7] 验证停车场可用车位恢复");
        lot = parkingLotService.getById(testParkingLotId);
        assertEquals(initialAvailable, lot.getAvailableSpaces(), "可用车位应该恢复");
        System.out.println("[成功] 停车场状态恢复验证通过");
        
        System.out.println("[测试结果] ✓ 跨模块集成测试通过");
    }

    @Test
    @DisplayName("P1-高并发场景测试：多用户同时创建订单")
    void testConcurrentOrderCreation() throws InterruptedException {
        System.out.println("[集成测试] 高并发场景测试开始");
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        System.out.println("[信息] 启动" + threadCount + "个并发线程");
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    User user = new User();
                    user.setUsername("concurrent_user_" + index + "_" + System.currentTimeMillis());
                    user.setPassword("123456");
                    user.setName("并发用户" + index);
                    user.setPhone("139" + String.format("%08d", (System.nanoTime() + index) % 100000000L));
                    user.setEmail("concurrent" + index + "_" + System.nanoTime() + "@example.com");
                    userService.register(user);
                    User savedConcurrentUser = userService.getByUsername(user.getUsername());
                    if (savedConcurrentUser == null) {
                        failCount.incrementAndGet();
                        return;
                    }
                    Long userId = savedConcurrentUser.getId();
                    
                    List<ParkingSpace> spaces = parkingSpaceService.getSpacesByParkingLotId(testParkingLotId, 0);
                    if (!spaces.isEmpty()) {
                        Order order = orderService.createOrder(userId, testParkingLotId, spaces.get(0).getId(), "京C" + String.format("%05d", index));
                        if (order != null) {
                            successCount.incrementAndGet();
                        } else {
                            failCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("[异常] 线程" + index + "执行失败: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        System.out.println("[结果] 成功: " + successCount.get() + ", 失败: " + failCount.get());
        System.out.println("[测试结果] ✓ 高并发场景测试通过（系统未崩溃）");
    }

    @Test
    @DisplayName("P2-异常场景恢复测试：订单创建后取消")
    void testOrderCancellationAndRecovery() {
        System.out.println("[集成测试] 异常场景恢复测试开始");
        
        System.out.println("[步骤1] 查询停车场初始状态");
        ParkingLot lot = parkingLotService.getById(testParkingLotId);
        int initialAvailable = lot.getAvailableSpaces();
        
        System.out.println("[步骤2] 创建订单");
        testParkingSpaceId = requireAvailableSpaceId(testParkingLotId);
        Order order = orderService.createOrder(testUserId, testParkingLotId, testParkingSpaceId, "京D77777");
        assertNotNull(order, "创建订单应该成功");
        
        System.out.println("[步骤3] 取消订单");
        boolean cancelResult = orderService.cancelOrder(order.getId());
        assertTrue(cancelResult, "取消订单应该成功");
        
        System.out.println("[步骤4] 验证车位状态恢复");
        ParkingSpace space = parkingSpaceService.getById(testParkingSpaceId);
        assertEquals(0, space.getStatus(), "车位状态应该恢复为空闲");
        
        System.out.println("[步骤5] 验证停车场可用车位恢复");
        lot = parkingLotService.getById(testParkingLotId);
        assertEquals(initialAvailable, lot.getAvailableSpaces(), "可用车位应该恢复");
        
        System.out.println("[测试结果] ✓ 异常场景恢复测试通过");
    }

    @Test
    @DisplayName("P0-推荐系统集成测试")
    void testRecommendationIntegration() {
        System.out.println("[集成测试] 推荐系统集成测试开始");
        
        System.out.println("[步骤1] 记录用户行为");
        recommendationService.recordUserBehavior(testUserId, testParkingLotId, null, 2);
        System.out.println("[成功] 用户行为记录完成");
        
        System.out.println("[步骤2] 分析用户偏好");
        Map<String, Object> preferences = recommendationService.analyzeUserPreferences(testUserId);
        assertNotNull(preferences, "偏好分析应该成功");
        System.out.println("[成功] 用户偏好分析完成");
        
        System.out.println("[步骤3] 获取个性化推荐");
        List<ParkingLot> recommendations = recommendationService.getPersonalizedRecommendations(testUserId, 39.9042, 116.4074);
        assertNotNull(recommendations, "推荐应该成功");
        System.out.println("[成功] 获取到" + recommendations.size() + "个推荐");
        
        System.out.println("[测试结果] ✓ 推荐系统集成测试通过");
    }
}
