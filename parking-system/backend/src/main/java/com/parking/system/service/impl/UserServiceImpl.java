package com.parking.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.system.entity.User;
import com.parking.system.mapper.UserMapper;
import com.parking.system.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);
    
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(LOG_TIME_FORMAT);
    }
    
    private String maskSensitiveInfo(String info) {
        if (info == null || info.length() <= 3) {
            return "***";
        }
        return info.substring(0, 2) + "***" + info.substring(info.length() - 1);
    }

    @Override
    public User login(String username, String password) {
        String time = getCurrentTime();
        String maskedUsername = maskSensitiveInfo(username);
        
        log.info("[成功][阶段1-入口][用户登录] 时间：{} | 参数：username={} | 开始执行登录验证", time, maskedUsername);
        
        User user = getByUsername(username);
        
        if (user == null) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", username);
            user = getOne(wrapper);
        }
        
        if (user == null) {
            log.warn("[失败][阶段1-入口][用户登录] 时间：{} | 原因：用户不存在 | 参数：username={}", time, maskedUsername);
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            log.info("[成功][阶段2-核心操作][用户登录] 时间：{} | 参数：userId={}, username={} | 结果：登录成功", 
                    time, user.getId(), user.getUsername());
            return user;
        } else {
            log.warn("[失败][阶段2-核心操作][用户登录] 时间：{} | 原因：密码错误 | 参数：userId={}, username={}", 
                    time, user.getId(), user.getUsername());
            return null;
        }
    }

    @Override
    public boolean register(User user) {
        String time = getCurrentTime();
        String maskedUsername = maskSensitiveInfo(user.getUsername());
        
        log.info("[成功][阶段1-入口][用户注册] 时间：{} | 参数：username={}, phone={} | 开始执行注册", 
                time, maskedUsername, maskSensitiveInfo(user.getPhone()));
        
        if (getByUsername(user.getUsername()) != null) {
            log.warn("[失败][阶段2-核心操作][用户注册] 时间：{} | 原因：用户名已存在 | 参数：username={}", time, maskedUsername);
            return false;
        }
        
        try {
            if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            if (user.getRole() == null) {
                user.setRole(0);
            }
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
            boolean result = save(user);
            
            if (result) {
                log.info("[成功][阶段4-结果反馈][用户注册] 时间：{} | 参数：username={} | 结果：注册成功，userId={}", 
                        time, maskedUsername, user.getId());
            } else {
                log.warn("[失败][阶段4-结果反馈][用户注册] 时间：{} | 原因：数据库保存失败 | 参数：username={}", time, maskedUsername);
            }
            return result;
        } catch (Exception e) {
            log.error("[失败][阶段2-核心操作][用户注册] 时间：{} | 原因：{} | 参数：username={}", 
                    time, e.getMessage(), maskedUsername);
            return false;
        }
    }

    @Override
    public User getByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return getOne(wrapper);
    }

}
