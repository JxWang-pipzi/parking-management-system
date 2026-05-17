package com.parking.system.service;

import com.parking.system.entity.ParkingRecommendation;
import com.parking.system.entity.ParkingLot;
import java.util.List;
import java.util.Map;

public interface RecommendationService {
    
    List<ParkingLot> getPersonalizedRecommendations(Long userId, Double latitude, Double longitude);
    
    List<ParkingLot> getRecommendedParkingLots(Long userId, Double latitude, Double longitude, Integer limit);
    
    ParkingRecommendation generateRecommendation(Long userId, Long parkingLotId, Long spaceId);
    
    void recordUserBehavior(Long userId, Long parkingLotId, Long spaceId, Integer behaviorType);
    
    Map<String, Object> analyzeUserPreferences(Long userId);
    
    Map<String, Object> analyzeParkingPatterns(Long parkingLotId);
    
    Double calculateRecommendationScore(Long userId, Long parkingLotId, Long spaceId);
    
    String generateRecommendationReason(Long userId, Long parkingLotId, Double score);
    
    void updateRecommendationFeedback(Long recommendationId, Integer feedbackType);
    
    List<Map<String, Object>> getHotParkingAreas();
    
    Map<String, Object> getParkingEfficiencyStats(Long parkingLotId);
    
    List<ParkingLot> getHybridRecommendations(Long userId, Double latitude, Double longitude, int limit);
}
