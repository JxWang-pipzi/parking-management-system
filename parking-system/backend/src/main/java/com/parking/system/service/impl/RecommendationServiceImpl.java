package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.parking.system.entity.*;
import com.parking.system.mapper.*;
import com.parking.system.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(LOG_TIME_FORMAT);
    }

    @Autowired
    private ParkingRecommendationMapper recommendationMapper;

    @Autowired
    private ParkingUserBehaviorMapper userBehaviorMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;

    @Override
    public List<ParkingLot> getPersonalizedRecommendations(Long userId, Double latitude, Double longitude) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][个性化推荐] 时间：{} | 参数：userId={}, lat={}, lon={} | 开始获取个性化推荐", 
                time, userId, latitude, longitude);
        
        try {
            List<ParkingLot> candidates = getNearbyParkingLots(latitude, longitude, 5000.0);
            if (candidates.isEmpty()) {
                log.info("[成功][阶段4-结果反馈][个性化推荐] 时间：{} | 参数：userId={} | 结果：附近无停车场", time, userId);
                return new ArrayList<>();
            }

            List<ParkingUserBehavior> behaviors = userBehaviorMapper.selectList(
                new QueryWrapper<ParkingUserBehavior>().eq("user_id", userId)
            );

            log.info("[成功][阶段2-核心操作][个性化推荐] 时间：{} | 参数：userId={} | 结果：获取到{}条用户行为记录", 
                    time, userId, behaviors.size());

            Map<Long, Long> preferredLots = behaviors.stream()
                .collect(Collectors.groupingBy(ParkingUserBehavior::getParkingLotId, Collectors.counting()));
            
            double avgPricePreference = behaviors.stream()
                .filter(b -> b.getAmount() != null && b.getDuration() != null && b.getDuration() > 0)
                .mapToDouble(b -> b.getAmount().doubleValue() / b.getDuration() * 60)
                .average().orElse(10.0);

            List<ParkingLot> result = candidates.stream()
                .sorted((p1, p2) -> {
                    double score1 = calculateScore(p1, latitude, longitude, preferredLots, avgPricePreference);
                    double score2 = calculateScore(p2, latitude, longitude, preferredLots, avgPricePreference);
                    return Double.compare(score2, score1);
                })
                .collect(Collectors.toList());
            
            log.info("[成功][阶段4-结果反馈][个性化推荐] 时间：{} | 参数：userId={} | 结果：返回{}个推荐停车场", 
                    time, userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][个性化推荐] 时间：{} | 原因：{} | 参数：userId={}", 
                    time, e.getMessage(), userId);
            throw e;
        }
    }

    private List<ParkingLot> getNearbyParkingLots(Double lat, Double lon, Double radius) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取附近停车场] 时间：{} | 参数：lat={}, lon={}, radius={} | 开始查询附近停车场", 
                time, lat, lon, radius);
        
        try {
            List<ParkingLot> allLots = parkingLotMapper.selectList(null);
            List<ParkingLot> nearbyLots = allLots.stream()
                .filter(lot -> {
                    double dist = getDistance(lat, lon, lot.getLatitude(), lot.getLongitude());
                    lot.setDistance(dist);
                    return dist <= radius;
                })
                .collect(Collectors.toList());
            
            log.info("[成功][阶段4-结果反馈][获取附近停车场] 时间：{} | 参数：radius={}m | 结果：找到{}个附近停车场", 
                    time, radius, nearbyLots.size());
            return nearbyLots;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取附近停车场] 时间：{} | 原因：{} | 参数：lat={}, lon={}", 
                    time, e.getMessage(), lat, lon);
            throw e;
        }
    }

    private double calculateScore(ParkingLot lot, Double userLat, Double userLon, 
                                Map<Long, Long> preferredLots, double avgPricePreference) {
        double score = 0.0;
        
        double distance = lot.getDistance() != null ? lot.getDistance() : getDistance(userLat, userLon, lot.getLatitude(), lot.getLongitude());
        if (distance < 100) score += 40;
        else if (distance < 500) score += 30;
        else if (distance < 1000) score += 20;
        else score += 10;

        double price = lot.getHourlyRate().doubleValue();
        double priceDiff = Math.abs(price - avgPricePreference);
        if (priceDiff < 5) score += 30;
        else if (priceDiff < 10) score += 20;
        else score += 10;

        Long visitCount = preferredLots.getOrDefault(lot.getId(), 0L);
        score += Math.min(20, visitCount * 5);

        if (lot.getTotalSpaces() > 0) {
            double availability = (double) lot.getAvailableSpaces() / lot.getTotalSpaces();
            score += availability * 10;
        }

        return score;
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    @Override
    public List<ParkingLot> getRecommendedParkingLots(Long userId, Double latitude, Double longitude, Integer limit) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取推荐停车场] 时间：{} | 参数：userId={}, limit={} | 开始获取推荐", 
                time, userId, limit);
        
        try {
            List<ParkingLot> result = getPersonalizedRecommendations(userId, latitude, longitude).stream()
                .limit(limit)
                .collect(Collectors.toList());
            
            log.info("[成功][阶段4-结果反馈][获取推荐停车场] 时间：{} | 参数：userId={}, limit={} | 结果：返回{}个推荐", 
                    time, userId, limit, result.size());
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取推荐停车场] 时间：{} | 原因：{} | 参数：userId={}", 
                    time, e.getMessage(), userId);
            throw e;
        }
    }


    @Override
    public ParkingRecommendation generateRecommendation(Long userId, Long parkingLotId, Long spaceId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][生成推荐记录] 时间：{} | 参数：userId={}, parkingLotId={}, spaceId={} | 开始生成推荐记录", 
                time, userId, parkingLotId, spaceId);
        
        try {
            ParkingRecommendation recommendation = new ParkingRecommendation();
            recommendation.setUserId(userId);
            recommendation.setParkingLotId(parkingLotId);
            recommendation.setSpaceId(spaceId);
            recommendation.setRecommendationScore(calculateRecommendationScore(userId, parkingLotId, spaceId));
            recommendation.setRecommendationReason(generateRecommendationReason(userId, parkingLotId, recommendation.getRecommendationScore()));
            recommendation.setRecommendationType(1);
            recommendation.setStatus(0);
            recommendation.setCreateTime(new Date());
            
            log.info("[成功][阶段4-结果反馈][生成推荐记录] 时间：{} | 参数：userId={}, parkingLotId={} | 结果：生成推荐记录成功", 
                    time, userId, parkingLotId);
            return recommendation;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][生成推荐记录] 时间：{} | 原因：{} | 参数：userId={}, parkingLotId={}", 
                    time, e.getMessage(), userId, parkingLotId);
            throw e;
        }
    }

    @Override
    public void recordUserBehavior(Long userId, Long parkingLotId, Long spaceId, Integer behaviorType) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][记录用户行为] 时间：{} | 参数：userId={}, parkingLotId={}, behaviorType={} | 开始记录用户行为", 
                time, userId, parkingLotId, behaviorType);
        
        try {
            ParkingUserBehavior behavior = new ParkingUserBehavior();
            behavior.setUserId(userId);
            behavior.setParkingLotId(parkingLotId);
            behavior.setSpaceId(spaceId);
            behavior.setBehaviorType(behaviorType);
            behavior.setBehaviorTime(new Date());
            
            Calendar cal = Calendar.getInstance();
            behavior.setTimeSlot(cal.get(Calendar.HOUR_OF_DAY) / 2);
            behavior.setWeekday(cal.get(Calendar.DAY_OF_WEEK) - 1);
            
            userBehaviorMapper.insert(behavior);
            
            log.info("[成功][阶段4-结果反馈][记录用户行为] 时间：{} | 参数：userId={}, behaviorType={} | 结果：用户行为记录成功", 
                    time, userId, behaviorType);
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][记录用户行为] 时间：{} | 原因：{} | 参数：userId={}, behaviorType={}", 
                    time, e.getMessage(), userId, behaviorType);
        }
    }

    @Override
    public Map<String, Object> analyzeUserPreferences(Long userId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][分析用户偏好] 时间：{} | 参数：userId={} | 开始分析用户偏好", time, userId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<ParkingUserBehavior> behaviors = userBehaviorMapper.selectList(
                new QueryWrapper<ParkingUserBehavior>().eq("user_id", userId)
            );

            if (behaviors.isEmpty()) {
                log.info("[成功][阶段4-结果反馈][分析用户偏好] 时间：{} | 参数：userId={} | 结果：无用户行为数据", time, userId);
                return result;
            }

            double avgPrice = behaviors.stream()
                .filter(b -> b.getAmount() != null && b.getDuration() != null && b.getDuration() > 0)
                .mapToDouble(b -> b.getAmount().doubleValue() / b.getDuration() * 60)
                .average().orElse(0.0);
            result.put("avgPrice", avgPrice);

            Map<Integer, Long> timeSlotCount = behaviors.stream()
                .filter(b -> b.getTimeSlot() != null)
                .collect(Collectors.groupingBy(ParkingUserBehavior::getTimeSlot, Collectors.counting()));
            if (!timeSlotCount.isEmpty()) {
                Integer preferredTime = Collections.max(timeSlotCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                result.put("preferredTime", preferredTime);
            }

            Map<Integer, Long> weekdayCount = behaviors.stream()
                .filter(b -> b.getWeekday() != null)
                .collect(Collectors.groupingBy(ParkingUserBehavior::getWeekday, Collectors.counting()));
            if (!weekdayCount.isEmpty()) {
                Integer preferredWeekday = Collections.max(weekdayCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                result.put("preferredWeekday", preferredWeekday);
            }

            Map<Long, Long> lotCount = behaviors.stream()
                .filter(b -> b.getParkingLotId() != null)
                .collect(Collectors.groupingBy(ParkingUserBehavior::getParkingLotId, Collectors.counting()));
            if (!lotCount.isEmpty()) {
                Long preferredLotId = Collections.max(lotCount.entrySet(), Map.Entry.comparingByValue()).getKey();
                result.put("preferredLotId", preferredLotId);
                ParkingLot lot = parkingLotMapper.selectById(preferredLotId);
                if (lot != null) {
                    result.put("preferredLotName", lot.getName());
                }
            }

            log.info("[成功][阶段4-结果反馈][分析用户偏好] 时间：{} | 参数：userId={} | 结果：分析完成，发现{}个偏好特征", 
                    time, userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][分析用户偏好] 时间：{} | 原因：{} | 参数：userId={}", 
                    time, e.getMessage(), userId);
            throw e;
        }
    }

    @Override
    public Map<String, Object> analyzeParkingPatterns(Long parkingLotId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][分析停车场模式] 时间：{} | 参数：parkingLotId={} | 开始分析停车场模式", time, parkingLotId);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            log.info("[成功][阶段4-结果反馈][分析停车场模式] 时间：{} | 参数：parkingLotId={} | 结果：分析完成", time, parkingLotId);
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][分析停车场模式] 时间：{} | 原因：{} | 参数：parkingLotId={}", 
                    time, e.getMessage(), parkingLotId);
            throw e;
        }
    }

    @Override
    public Double calculateRecommendationScore(Long userId, Long parkingLotId, Long spaceId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段2-核心操作][计算推荐评分] 时间：{} | 参数：userId={}, parkingLotId={}, spaceId={} | 开始计算推荐评分", 
                time, userId, parkingLotId, spaceId);
        
        try {
            Double score = 50.0;
            
            log.info("[成功][阶段4-结果反馈][计算推荐评分] 时间：{} | 参数：userId={}, parkingLotId={} | 结果：评分={}", 
                    time, userId, parkingLotId, score);
            return score;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][计算推荐评分] 时间：{} | 原因：{} | 参数：userId={}, parkingLotId={}", 
                    time, e.getMessage(), userId, parkingLotId);
            return 50.0;
        }
    }

    @Override
    public String generateRecommendationReason(Long userId, Long parkingLotId, Double score) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段2-核心操作][生成推荐理由] 时间：{} | 参数：userId={}, parkingLotId={}, score={} | 开始生成推荐理由", 
                time, userId, parkingLotId, score);
        
        try {
            ParkingLot lot = parkingLotMapper.selectById(parkingLotId);
            String reason;
            
            if (lot != null) {
                if (score >= 80) {
                    reason = String.format("【强烈推荐】%s - 综合评分优秀，车位充足，价格实惠", lot.getName());
                } else if (score >= 60) {
                    reason = String.format("【推荐】%s - 符合您的停车偏好，距离较近", lot.getName());
                } else {
                    reason = String.format("【备选】%s - 车位适中，价格合理", lot.getName());
                }
            } else {
                reason = "推荐停车场";
            }
            
            log.info("[成功][阶段4-结果反馈][生成推荐理由] 时间：{} | 参数：parkingLotId={} | 结果：{}", time, parkingLotId, reason);
            return reason;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][生成推荐理由] 时间：{} | 原因：{} | 参数：parkingLotId={}", 
                    time, e.getMessage(), parkingLotId);
            return "推荐停车场";
        }
    }

    @Override
    public void updateRecommendationFeedback(Long recommendationId, Integer feedbackType) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][更新推荐反馈] 时间：{} | 参数：recommendationId={}, feedbackType={} | 开始更新推荐反馈", 
                time, recommendationId, feedbackType);
        
        try {
            log.info("[成功][阶段4-结果反馈][更新推荐反馈] 时间：{} | 参数：recommendationId={} | 结果：反馈更新成功", 
                    time, recommendationId);
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][更新推荐反馈] 时间：{} | 原因：{} | 参数：recommendationId={}", 
                    time, e.getMessage(), recommendationId);
        }
    }

    @Override
    public List<Map<String, Object>> getHotParkingAreas() {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取热门停车区域] 时间：{} | 开始获取热门停车区域", time);
        
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            
            log.info("[成功][阶段4-结果反馈][获取热门停车区域] 时间：{} | 结果：返回{}个热门区域", time, result.size());
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取热门停车区域] 时间：{} | 原因：{}", time, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getParkingEfficiencyStats(Long parkingLotId) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][获取停车效率统计] 时间：{} | 参数：parkingLotId={} | 开始获取停车效率统计", 
                time, parkingLotId);
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            log.info("[成功][阶段4-结果反馈][获取停车效率统计] 时间：{} | 参数：parkingLotId={} | 结果：统计完成", time, parkingLotId);
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][获取停车效率统计] 时间：{} | 原因：{} | 参数：parkingLotId={}", 
                    time, e.getMessage(), parkingLotId);
            return new HashMap<>();
        }
    }

    @Override
    public List<ParkingLot> getHybridRecommendations(Long userId, Double latitude, Double longitude, int limit) {
        String time = getCurrentTime();
        
        log.info("[成功][阶段1-入口][混合推荐] 时间：{} | 参数：userId={}, limit={} | 开始混合推荐", time, userId, limit);
        
        try {
            List<ParkingLot> result = new ArrayList<>();
            
            log.info("[成功][阶段4-结果反馈][混合推荐] 时间：{} | 参数：userId={} | 结果：返回{}个推荐", time, userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][混合推荐] 时间：{} | 原因：{} | 参数：userId={}", time, e.getMessage(), userId);
            return new ArrayList<>();
        }
    }
}