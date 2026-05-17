package com.parking.system.service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 */
public interface CacheService {
    
    /**
     * 设置缓存
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     */
    Object get(String key);
    
    /**
     * 删除缓存
     */
    void delete(String key);
    
    /**
     * 批量删除缓存
     */
    void deletePattern(String pattern);
    
    /**
     * 检查缓存是否存在
     */
    boolean exists(String key);
    
    /**
     * 设置缓存过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存剩余时间
     */
    long getExpire(String key, TimeUnit unit);
    
    /**
     * 缓存预热
     */
    void warmUp();
    
    /**
     * 获取缓存统计信息
     */
    Map<String, Object> getCacheStatistics();
    
    /**
     * 清空所有缓存
     */
    void clearAll();
    
    /**
     * 获取所有缓存键
     */
    Set<String> getAllKeys(String pattern);
}