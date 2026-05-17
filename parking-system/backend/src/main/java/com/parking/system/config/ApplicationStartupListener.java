package com.parking.system.config;

import com.parking.system.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 * 在应用启动完成后执行初始化任务
 */
@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {
    
    @Autowired(required = false)
    private CacheService cacheService;
    
    @Autowired
    private com.parking.system.service.UserService userService;
    
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("应用启动完成，开始执行初始化任务...");
        
        try {
            // 执行缓存预热
            if (cacheService != null) {
                log.info("开始缓存预热...");
                cacheService.warmUp();
                log.info("缓存预热完成");
            }
            
            // 修复管理员密码
            fixAdminPassword();
            
            log.info("初始化任务执行完成");
            
        } catch (Exception e) {
            log.error("初始化任务执行失败", e);
        }
    }

    private void fixAdminPassword() {
        try {
            com.parking.system.entity.User admin = userService.getByUsername("admin");
            if (admin != null) {
                // 重置密码为 123456
                String newPassword = passwordEncoder.encode("123456");
                admin.setPassword(newPassword);
                userService.updateById(admin);
                log.info("已重置管理员密码为: 123456 (Hash: {})", newPassword);
            }
            
            // 确保测试用户存在并重置密码
            com.parking.system.entity.User user = userService.getByUsername("user");
            if (user != null) {
                String userPassword = passwordEncoder.encode("123456");
                user.setPassword(userPassword);
                userService.updateById(user);
                log.info("已重置测试用户(user)密码为: 123456");
            } else {
                // 创建测试用户
                com.parking.system.entity.User newUser = new com.parking.system.entity.User();
                newUser.setUsername("user");
                newUser.setPassword(passwordEncoder.encode("123456"));
                newUser.setName("测试用户");
                newUser.setPhone("13800000000");
                newUser.setRole(0);
                newUser.setStatus(1);
                newUser.setCreateTime(new java.util.Date());
                newUser.setUpdateTime(new java.util.Date());
                userService.save(newUser);
                log.info("已创建测试用户(user), 密码: 123456");
            }
        } catch (Exception e) {
            log.error("重置密码失败", e);
        }
    }
}