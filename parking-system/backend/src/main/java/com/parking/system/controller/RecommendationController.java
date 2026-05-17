package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.ParkingRecommendation;
import com.parking.system.entity.ParkingLot;
import com.parking.system.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("/user/{userId}")
    public Response<List<ParkingLot>> getPersonalizedRecommendations(
            @PathVariable Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        List<ParkingLot> recommendations = 
            recommendationService.getPersonalizedRecommendations(userId, latitude, longitude);
        return Response.success(recommendations);
    }
    
    @GetMapping("/parking-lots")
    public Response<List<ParkingLot>> getRecommendedParkingLots(
            @RequestParam Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        List<ParkingLot> lots = recommendationService.getRecommendedParkingLots(userId, latitude, longitude, limit);
        return Response.success(lots);
    }
    
    @PostMapping("/generate")
    public Response<ParkingRecommendation> generateRecommendation(
            @RequestParam Long userId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long spaceId) {
        ParkingRecommendation recommendation = 
            recommendationService.generateRecommendation(userId, parkingLotId, spaceId);
        return Response.success(recommendation);
    }
    
    @PostMapping("/behavior")
    public Response<Void> recordUserBehavior(
            @RequestParam Long userId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long spaceId,
            @RequestParam Integer behaviorType) {
        recommendationService.recordUserBehavior(userId, parkingLotId, spaceId, behaviorType);
        return Response.success(null);
    }
    
    @GetMapping("/user/{userId}/preferences")
    public Response<Map<String, Object>> analyzeUserPreferences(@PathVariable Long userId) {
        Map<String, Object> preferences = recommendationService.analyzeUserPreferences(userId);
        return Response.success(preferences);
    }
    
    @GetMapping("/parking-lot/{parkingLotId}/patterns")
    public Response<Map<String, Object>> analyzeParkingPatterns(@PathVariable Long parkingLotId) {
        Map<String, Object> patterns = recommendationService.analyzeParkingPatterns(parkingLotId);
        return Response.success(patterns);
    }
    
    @GetMapping("/score")
    public Response<Double> calculateScore(
            @RequestParam Long userId,
            @RequestParam Long parkingLotId,
            @RequestParam(required = false) Long spaceId) {
        Double score = recommendationService.calculateRecommendationScore(userId, parkingLotId, spaceId);
        return Response.success(score);
    }
    
    @GetMapping("/reason")
    public Response<String> generateReason(
            @RequestParam Long userId,
            @RequestParam Long parkingLotId,
            @RequestParam Double score) {
        String reason = recommendationService.generateRecommendationReason(userId, parkingLotId, score);
        return Response.success(reason);
    }
    
    @PostMapping("/{recommendationId}/feedback")
    public Response<Void> updateFeedback(
            @PathVariable Long recommendationId,
            @RequestParam Integer feedbackType) {
        recommendationService.updateRecommendationFeedback(recommendationId, feedbackType);
        return Response.success(null);
    }
    
    @GetMapping("/hot-areas")
    public Response<List<Map<String, Object>>> getHotParkingAreas() {
        List<Map<String, Object>> hotAreas = recommendationService.getHotParkingAreas();
        return Response.success(hotAreas);
    }
    
    @GetMapping("/parking-lot/{parkingLotId}/efficiency")
    public Response<Map<String, Object>> getParkingEfficiencyStats(@PathVariable Long parkingLotId) {
        Map<String, Object> stats = recommendationService.getParkingEfficiencyStats(parkingLotId);
        return Response.success(stats);
    }
}
