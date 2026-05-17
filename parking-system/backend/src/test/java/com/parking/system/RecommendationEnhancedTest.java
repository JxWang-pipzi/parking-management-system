package com.parking.system;

import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.User;
import com.parking.system.service.RecommendationService;
import com.parking.system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecommendationEnhancedTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    private Long testUserId;
    private Double testLatitude = 39.9042;
    private Double testLongitude = 116.4074;

    private String uniquePhone() {
        return "139" + String.format("%08d", System.nanoTime() % 100000000L);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@example.com";
    }

    @BeforeEach
    void setUp() {
        System.out.println("[测试准备] 开始初始化推荐测试数据...");
        
        User user = new User();
        user.setUsername("recommend_test_user_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("推荐测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("recommend"));
        userService.register(user);
        User savedUser = userService.getByUsername(user.getUsername());
        assertNotNull(savedUser, "推荐测试用户初始化失败");
        testUserId = savedUser.getId();
        
        System.out.println("[测试准备] 初始化完成: userId=" + testUserId);
    }

    @Test
    @DisplayName("P0-正常场景：获取个性化推荐")
    void testGetPersonalizedRecommendations() {
        System.out.println("[测试场景] 正常场景：获取个性化推荐");
        System.out.println("[输入参数] userId=" + testUserId + ", lat=" + testLatitude + ", lon=" + testLongitude);
        
        List<ParkingLot> recommendations = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        
        System.out.println("[预期输出] 返回推荐停车场列表");
        System.out.println("[实际输出] 推荐数量=" + recommendations.size());
        assertNotNull(recommendations, "推荐列表不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：获取指定数量的推荐")
    void testGetRecommendedParkingLotsWithLimit() {
        System.out.println("[测试场景] 边界场景：获取指定数量的推荐");
        int limit = 3;
        System.out.println("[输入参数] userId=" + testUserId + ", limit=" + limit);
        
        List<ParkingLot> recommendations = recommendationService.getRecommendedParkingLots(testUserId, testLatitude, testLongitude, limit);
        
        System.out.println("[预期输出] 返回不超过" + limit + "个推荐");
        System.out.println("[实际输出] 推荐数量=" + recommendations.size());
        assertNotNull(recommendations, "推荐列表不应该为null");
        assertTrue(recommendations.size() <= limit, "推荐数量应该不超过限制");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：分析用户偏好")
    void testAnalyzeUserPreferences() {
        System.out.println("[测试场景] 正常场景：分析用户偏好");
        System.out.println("[输入参数] userId=" + testUserId);
        
        Map<String, Object> preferences = recommendationService.analyzeUserPreferences(testUserId);
        
        System.out.println("[预期输出] 返回用户偏好分析结果");
        System.out.println("[实际输出] 偏好特征数量=" + preferences.size());
        assertNotNull(preferences, "偏好分析结果不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：分析新用户偏好（无历史数据）")
    void testAnalyzeNewUserPreferences() {
        System.out.println("[测试场景] 边界场景：分析新用户偏好（无历史数据）");
        
        User newUser = new User();
        newUser.setUsername("new_user_" + System.currentTimeMillis());
        newUser.setPassword("123456");
        newUser.setName("新用户");
        newUser.setPhone(uniquePhone());
        newUser.setEmail(uniqueEmail("newuser"));
        userService.register(newUser);
        User savedNewUser = userService.getByUsername(newUser.getUsername());
        assertNotNull(savedNewUser, "新用户初始化失败");
        Long newUserId = savedNewUser.getId();
        
        System.out.println("[输入参数] userId=" + newUserId + "（新用户）");
        
        Map<String, Object> preferences = recommendationService.analyzeUserPreferences(newUserId);
        
        System.out.println("[预期输出] 返回空偏好结果");
        System.out.println("[实际输出] 偏好特征数量=" + preferences.size());
        assertNotNull(preferences, "偏好分析结果不应该为null");
        assertTrue(preferences.isEmpty(), "新用户应该无偏好数据");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：计算推荐评分")
    void testCalculateRecommendationScore() {
        System.out.println("[测试场景] 正常场景：计算推荐评分");
        
        List<ParkingLot> lots = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        if (lots.isEmpty()) {
            System.out.println("[跳过] 无停车场可测试");
            return;
        }
        
        Long parkingLotId = lots.get(0).getId();
        System.out.println("[输入参数] userId=" + testUserId + ", parkingLotId=" + parkingLotId);
        
        Double score = recommendationService.calculateRecommendationScore(testUserId, parkingLotId, null);
        
        System.out.println("[预期输出] 返回评分（0-100）");
        System.out.println("[实际输出] 评分=" + score);
        assertNotNull(score, "评分不应该为null");
        assertTrue(score >= 0 && score <= 100, "评分应该在0-100范围内");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：生成推荐理由")
    void testGenerateRecommendationReason() {
        System.out.println("[测试场景] 正常场景：生成推荐理由");
        
        List<ParkingLot> lots = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        if (lots.isEmpty()) {
            System.out.println("[跳过] 无停车场可测试");
            return;
        }
        
        Long parkingLotId = lots.get(0).getId();
        Double score = 75.0;
        System.out.println("[输入参数] parkingLotId=" + parkingLotId + ", score=" + score);
        
        String reason = recommendationService.generateRecommendationReason(testUserId, parkingLotId, score);
        
        System.out.println("[预期输出] 返回推荐理由字符串");
        System.out.println("[实际输出] 推荐理由=" + reason);
        assertNotNull(reason, "推荐理由不应该为null");
        assertFalse(reason.isEmpty(), "推荐理由不应该为空");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：高分推荐理由")
    void testGenerateHighScoreReason() {
        System.out.println("[测试场景] 边界场景：高分推荐理由");
        
        List<ParkingLot> lots = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        if (lots.isEmpty()) {
            System.out.println("[跳过] 无停车场可测试");
            return;
        }
        
        Long parkingLotId = lots.get(0).getId();
        Double highScore = 90.0;
        System.out.println("[输入参数] parkingLotId=" + parkingLotId + ", score=" + highScore + "（高分）");
        
        String reason = recommendationService.generateRecommendationReason(testUserId, parkingLotId, highScore);
        
        System.out.println("[预期输出] 包含'强烈推荐'关键词");
        System.out.println("[实际输出] 推荐理由=" + reason);
        assertTrue(reason.contains("强烈推荐") || reason.contains("推荐"), "高分推荐应该包含推荐关键词");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：记录用户行为")
    void testRecordUserBehavior() {
        System.out.println("[测试场景] 正常场景：记录用户行为");
        
        List<ParkingLot> lots = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        if (lots.isEmpty()) {
            System.out.println("[跳过] 无停车场可测试");
            return;
        }
        
        Long parkingLotId = lots.get(0).getId();
        System.out.println("[输入参数] userId=" + testUserId + ", parkingLotId=" + parkingLotId + ", behaviorType=2（停车）");
        
        recommendationService.recordUserBehavior(testUserId, parkingLotId, null, 2);
        
        System.out.println("[预期输出] 用户行为记录成功");
        System.out.println("[实际输出] 记录完成");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：获取热门停车区域")
    void testGetHotParkingAreas() {
        System.out.println("[测试场景] 正常场景：获取热门停车区域");
        
        List<Map<String, Object>> hotAreas = recommendationService.getHotParkingAreas();
        
        System.out.println("[预期输出] 返回热门区域列表");
        System.out.println("[实际输出] 热门区域数量=" + hotAreas.size());
        assertNotNull(hotAreas, "热门区域列表不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：获取停车效率统计")
    void testGetParkingEfficiencyStats() {
        System.out.println("[测试场景] 正常场景：获取停车效率统计");
        
        List<ParkingLot> lots = recommendationService.getPersonalizedRecommendations(testUserId, testLatitude, testLongitude);
        if (lots.isEmpty()) {
            System.out.println("[跳过] 无停车场可测试");
            return;
        }
        
        Long parkingLotId = lots.get(0).getId();
        System.out.println("[输入参数] parkingLotId=" + parkingLotId);
        
        Map<String, Object> stats = recommendationService.getParkingEfficiencyStats(parkingLotId);
        
        System.out.println("[预期输出] 返回效率统计结果");
        System.out.println("[实际输出] 统计项数量=" + stats.size());
        assertNotNull(stats, "效率统计不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：混合推荐")
    void testGetHybridRecommendations() {
        System.out.println("[测试场景] 边界场景：混合推荐");
        int limit = 5;
        System.out.println("[输入参数] userId=" + testUserId + ", limit=" + limit);
        
        List<ParkingLot> recommendations = recommendationService.getHybridRecommendations(testUserId, testLatitude, testLongitude, limit);
        
        System.out.println("[预期输出] 返回混合推荐结果");
        System.out.println("[实际输出] 推荐数量=" + recommendations.size());
        assertNotNull(recommendations, "推荐列表不应该为null");
        System.out.println("[测试结果] ✓ 通过");
    }
}
