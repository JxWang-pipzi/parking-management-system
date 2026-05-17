package com.parking.system.service.impl;

import com.parking.system.entity.ParkingLot;
import com.parking.system.entity.ParkingSpace;
import com.parking.system.mapper.ParkingLotMapper;
import com.parking.system.mapper.ParkingSpaceMapper;
import com.parking.system.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存服务实现
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class CacheServiceImpl implements CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ParkingLotMapper parkingLotMapper;
    
    @Autowired
    private ParkingSpaceMapper parkingSpaceMapper;
    
    // 缓存统计
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // 缓存键前缀
    private static final String PARKING_LOT_PREFIX = "parking:lot:";
    private static final String PARKING_SPACE_PREFIX = "parking:space:";
    private static final String PARKING_LOT_SPACES_PREFIX = "parking:lot:spaces:";
    private static final String AVAILABLE_SPACES_PREFIX = "parking:available:";
    
    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("缓存设置成功: key={}", key);
        } catch (Exception e) {
            log.error("缓存设置失败: key={}", key, e);
        }
    }
    
    @Override
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value != null) {
                cacheHits.incrementAndGet();
                log.debug("缓存命中: key={}", key);
            } else {
                cacheMisses.incrementAndGet();
                log.debug("缓存未命中: key={}", key);
            }
            
            return value;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            cacheMisses.incrementAndGet();
            return null;
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存删除成功: key={}", key);
        } catch (Exception e) {
            log.error("缓存删除失败: key={}", key, e);
        }
    }
    
    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("批量删除缓存成功: pattern={}, count={}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败: pattern={}", pattern, e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查缓存存在失败: key={}", key, e);
            return false;
        }
    }
    
    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return result != null && result;
        } catch (Exception e) {
            log.error("设置缓存过期时间失败: key={}", key, e);
            return false;
        }
    }
    
    @Override
    public long getExpire(String key, TimeUnit unit) {
        try {
            Long expire = redisTemplate.getExpire(key, unit);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("获取缓存剩余时间失败: key={}", key, e);
            return -1;
        }
    }
    
    @Override
    public void warmUp() {
        log.info("开始缓存预热...");
        
        try {
            // 预热停车场数据
            List<ParkingLot> parkingLots = parkingLotMapper.selectList(null);
            for (ParkingLot lot : parkingLots) {
                String key = PARKING_LOT_PREFIX + lot.getId();
                set(key, lot, 1, TimeUnit.HOURS);
            }
            log.info("停车场数据预热完成: count={}", parkingLots.size());
            
            // 预热停车位数据
            List<ParkingSpace> parkingSpaces = parkingSpaceMapper.selectList(null);
            for (ParkingSpace space : parkingSpaces) {
                String key = PARKING_SPACE_PREFIX + space.getId();
                set(key, space, 30, TimeUnit.MINUTES);
            }
            log.info("停车位数据预热完成: count={}", parkingSpaces.size());
            
            // 预热每个停车场的停车位列表
            Map<Long, List<ParkingSpace>> lotSpacesMap = new HashMap<>();
            for (ParkingSpace space : parkingSpaces) {
                lotSpacesMap.computeIfAbsent(space.getParkingLotId(), k -> new ArrayList<>()).add(space);
            }
            
            for (Map.Entry<Long, List<ParkingSpace>> entry : lotSpacesMap.entrySet()) {
                String key = PARKING_LOT_SPACES_PREFIX + entry.getKey();
                set(key, entry.getValue(), 30, TimeUnit.MINUTES);
            }
            log.info("停车场停车位列表预热完成: count={}", lotSpacesMap.size());
            
            // 预热可用停车位数据
            for (ParkingLot lot : parkingLots) {
                String key = AVAILABLE_SPACES_PREFIX + lot.getId();
                set(key, lot.getAvailableSpaces(), 5, TimeUnit.MINUTES);
            }
            log.info("可用停车位数据预热完成");
            
            log.info("缓存预热完成！");
            
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }
    }
    
    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            long hits = cacheHits.get();
            long misses = cacheMisses.get();
            long total = hits + misses;
            
            stats.put("cacheHits", hits);
            stats.put("cacheMisses", misses);
            stats.put("totalRequests", total);
            stats.put("hitRate", total > 0 ? (double) hits / total : 0.0);
            
            // 获取缓存键数量
            Set<String> allKeys = redisTemplate.keys("*");
            stats.put("totalKeys", allKeys != null ? allKeys.size() : 0);
            
            // 按前缀统计缓存键数量
            Map<String, Integer> keysByPrefix = new HashMap<>();
            if (allKeys != null) {
                for (String key : allKeys) {
                    String prefix = key.split(":")[0] + ":";
                    keysByPrefix.merge(prefix, 1, Integer::sum);
                }
            }
            stats.put("keysByPrefix", keysByPrefix);
            
            log.info("缓存统计: hits={}, misses={}, hitRate={}", hits, misses, stats.get("hitRate"));
            
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
        }
        
        return stats;
    }
    
    @Override
    public void clearAll() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清空所有缓存成功: count={}", keys.size());
            }
            
            // 重置统计
            cacheHits.set(0);
            cacheMisses.set(0);
            
        } catch (Exception e) {
            log.error("清空所有缓存失败", e);
        }
    }
    
    @Override
    public Set<String> getAllKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("获取所有缓存键失败: pattern={}", pattern, e);
            return new HashSet<>();
        }
    }
    
    /**
     * 智能缓存失效 - 根据数据更新频率动态调整缓存时间
     */
    public void smartInvalidate(String key, Object newValue) {
        try {
            Object oldValue = get(key);
            
            if (oldValue == null) {
                // 首次缓存，使用默认时间
                set(key, newValue, 30, TimeUnit.MINUTES);
            } else {
                // 根据更新频率调整缓存时间
                long remainingTime = getExpire(key, TimeUnit.SECONDS);
                
                if (remainingTime > 0) {
                    // 如果缓存还有较长时间，说明更新不频繁，延长缓存时间
                    if (remainingTime > 1200) { // 大于20分钟
                        set(key, newValue, 1, TimeUnit.HOURS);
                    } else {
                        set(key, newValue, 30, TimeUnit.MINUTES);
                    }
                } else {
                    // 缓存已过期或即将过期，说明更新频繁，缩短缓存时间
                    set(key, newValue, 10, TimeUnit.MINUTES);
                }
            }
            
            log.debug("智能缓存失效: key={}", key);
            
        } catch (Exception e) {
            log.error("智能缓存失效失败: key={}", key, e);
        }
    }
    
    /**
     * 缓存穿透保护 - 缓存空值
     */
    public void cacheNullValue(String key) {
        try {
            set(key, "NULL", 5, TimeUnit.MINUTES);
            log.debug("缓存空值: key={}", key);
        } catch (Exception e) {
            log.error("缓存空值失败: key={}", key, e);
        }
    }
    
    /**
     * 获取缓存命中率
     */
    public double getCacheHitRate() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
}