package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.service.CacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 缓存管理控制器
 */
@Api(tags = "缓存管理")
@Slf4j
@RestController
@RequestMapping("/cache")
@CrossOrigin
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class CacheManagementController {
    
    @Autowired
    private CacheService cacheService;
    
    @ApiOperation("获取缓存统计信息")
    @GetMapping("/statistics")
    public Response<Map<String, Object>> getCacheStatistics() {
        Map<String, Object> stats = cacheService.getCacheStatistics();
        return Response.success(stats);
    }
    
    @ApiOperation("缓存预热")
    @PostMapping("/warm-up")
    public Response<Void> warmUp() {
        log.info("手动触发缓存预热");
        cacheService.warmUp();
        return Response.success("缓存预热完成", null);
    }
    
    @ApiOperation("清空所有缓存")
    @PostMapping("/clear-all")
    public Response<Void> clearAll() {
        log.info("手动清空所有缓存");
        cacheService.clearAll();
        return Response.success("所有缓存已清空", null);
    }
    
    @ApiOperation("删除指定缓存")
    @DeleteMapping("/{key}")
    public Response<Void> deleteCache(
            @ApiParam("缓存键") @PathVariable String key) {
        log.info("删除缓存: key={}", key);
        cacheService.delete(key);
        return Response.success("缓存删除成功", null);
    }
    
    @ApiOperation("批量删除缓存")
    @DeleteMapping("/pattern/{pattern}")
    public Response<Void> deleteCachePattern(
            @ApiParam("缓存键模式") @PathVariable String pattern) {
        log.info("批量删除缓存: pattern={}", pattern);
        cacheService.deletePattern(pattern);
        return Response.success("批量删除缓存成功", null);
    }
    
    @ApiOperation("检查缓存是否存在")
    @GetMapping("/exists/{key}")
    public Response<Boolean> exists(
            @ApiParam("缓存键") @PathVariable String key) {
        boolean exists = cacheService.exists(key);
        return Response.success(exists);
    }
    
    @ApiOperation("获取所有缓存键")
    @GetMapping("/keys")
    public Response<Set<String>> getAllKeys(
            @ApiParam("键模式") @RequestParam(required = false, defaultValue = "*") String pattern) {
        Set<String> keys = cacheService.getAllKeys(pattern);
        return Response.success(keys);
    }
    
    @ApiOperation("获取缓存值")
    @GetMapping("/value/{key}")
    public Response<Object> getCacheValue(
            @ApiParam("缓存键") @PathVariable String key) {
        Object value = cacheService.get(key);
        
        if (value != null) {
            return Response.success(value);
        } else {
            return Response.error("缓存不存在或已过期");
        }
    }
}