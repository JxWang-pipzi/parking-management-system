package com.parking.system.controller;

import com.parking.system.common.Response;
import com.parking.system.entity.User;
import com.parking.system.service.UserService;
import com.parking.system.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private com.parking.system.service.OrderService orderService;

    private User sanitizeUser(User user) {
        if (user == null) return null;
        User sanitized = new User();
        sanitized.setId(user.getId());
        sanitized.setUsername(user.getUsername());
        sanitized.setName(user.getName());
        sanitized.setPhone(user.getPhone());
        sanitized.setEmail(user.getEmail());
        sanitized.setAvatar(user.getAvatar());
        sanitized.setRole(user.getRole());
        sanitized.setStatus(user.getStatus());
        sanitized.setCreateTime(user.getCreateTime());
        sanitized.setUpdateTime(user.getUpdateTime());
        return sanitized;
    }

    @Resource
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String buildUniqueWechatPhone(String openid) {
        String base = "wx" + Math.abs(openid.hashCode());
        String candidate = base;
        int suffix = 1;
        while (true) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> phoneWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            phoneWrapper.eq("phone", candidate);
            User exists = userService.getOne(phoneWrapper, false);
            if (exists == null) {
                return candidate;
            }
            candidate = base + "_" + suffix;
            suffix++;
        }
    }

    @GetMapping
    @ApiOperation("获取所有用户")
    public Response<List<User>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            log.warn("[失败][阶段1][获取用户列表] 时间：{} | 原因：非管理员无权访问", System.currentTimeMillis());
            return Response.error(403, "无权访问，仅管理员可操作");
        }
        log.info("[成功][阶段2][获取用户列表] 时间：{} | 参数：无", System.currentTimeMillis());
        List<User> users = userService.list();
        List<User> sanitizedUsers = users.stream().map(this::sanitizeUser).collect(java.util.stream.Collectors.toList());
        log.info("[成功][阶段4][返回用户列表] 时间：{} | 结果：共{}个用户", System.currentTimeMillis(), sanitizedUsers.size());
        return Response.success(sanitizedUsers);
    }

    @GetMapping("/stats")
    @ApiOperation("获取用户统计信息")
    public Response<Map<String, Object>> getUserStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        log.info("[成功][阶段2][获取用户统计] 时间：{} | 参数：用户={}", System.currentTimeMillis(), auth.getName());

        Map<String, Object> stats = new HashMap<>();
        if (principal instanceof Long) {
            Long userId = (Long) principal;
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.parking.system.entity.Order> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            long orderCount = orderService.count(queryWrapper);
            stats.put("orders", orderCount);
        } else {
            stats.put("orders", 0);
        }
        stats.put("balance", 0);
        stats.put("points", 0);
        stats.put("coupons", 0);

        log.info("[成功][阶段4][返回统计数据] 时间：{} | 结果：{}", System.currentTimeMillis(), stats);
        return Response.success(stats);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取用户详情")
    public Response<User> getUserById(@PathVariable Long id) {
        log.info("[成功][阶段2][获取用户详情] 时间：{} | 参数：id={}", System.currentTimeMillis(), id);
        User user = userService.getById(id);
        if (user != null) {
            log.info("[成功][阶段4][返回用户] 时间：{} | 结果：id={}, username={}", System.currentTimeMillis(), user.getId(), user.getUsername());
            return Response.success(sanitizeUser(user));
        }
        log.warn("[失败][阶段4][用户不存在] 时间：{} | 原因：id={}", System.currentTimeMillis(), id);
        return Response.error("用户不存在");
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Response<User> register(@RequestBody User user) {
        log.info("[成功][阶段2][用户注册] 时间：{} | 参数：username={}", System.currentTimeMillis(), user.getUsername());

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            log.warn("[失败][阶段2][用户注册] 时间：{} | 原因：用户名为空", System.currentTimeMillis());
            return Response.error("用户名不能为空");
        }
        if (user.getUsername().trim().length() < 3) {
            log.warn("[失败][阶段2][用户注册] 时间：{} | 原因：用户名长度不足3位", System.currentTimeMillis());
            return Response.error("用户名长度不能少于3位");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            log.warn("[失败][阶段2][用户注册] 时间：{} | 原因：密码为空", System.currentTimeMillis());
            return Response.error("密码不能为空");
        }
        if (user.getPassword().trim().length() < 6) {
            log.warn("[失败][阶段2][用户注册] 时间：{} | 原因：密码长度不足6位", System.currentTimeMillis());
            return Response.error("密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        if (userService.register(user)) {
            log.info("[成功][阶段4][注册成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), user.getId());
            return Response.success("注册成功", sanitizeUser(user));
        }
        log.warn("[失败][阶段4][注册失败] 时间：{} | 原因：用户名已存在", System.currentTimeMillis());
        return Response.error("用户名已存在");
    }

    @PostMapping("/wechat-login")
    @ApiOperation("微信一键登录")
    public Response<Map<String, Object>> wechatLogin(@RequestBody Map<String, String> data) {
        try {
            String code = data.get("code");
            log.info("[成功][阶段2][微信登录] 时间：{} | 参数：code={}", System.currentTimeMillis(), code);

            if (code == null) {
                log.warn("[失败][阶段2][微信登录] 时间：{} | 原因：无效的登录凭证", System.currentTimeMillis());
                return Response.error("无效的登录凭证");
            }

            String openid = "mock-openid-" + code;
            String nickname = data.get("nickname");
            String avatarUrl = data.get("avatarUrl");

            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            queryWrapper.eq("username", openid);
            User user = userService.getOne(queryWrapper);

            if (user == null) {
                String virtualPhone = buildUniqueWechatPhone(openid);
                user = new User();
                user.setUsername(openid);
                user.setName(nickname != null && !nickname.trim().isEmpty() ? nickname : "微信用户");
                user.setAvatar(avatarUrl);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setPhone(virtualPhone);
                user.setRole(0);
                user.setStatus(1);
                user.setCreateTime(new Date());
                user.setUpdateTime(new Date());
                userService.save(user);
                log.info("[成功][阶段3][创建微信用户] 时间：{} | 结果：id={}", System.currentTimeMillis(), user.getId());
            } else {
                boolean needUpdate = false;
                if (nickname != null && !nickname.trim().isEmpty() && !nickname.equals(user.getName())) {
                    user.setName(nickname);
                    needUpdate = true;
                }
                if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals(user.getAvatar())) {
                    user.setAvatar(avatarUrl);
                    needUpdate = true;
                }
                if (needUpdate) {
                    userService.updateById(user);
                }
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", sanitizeUser(user));

            log.info("[成功][阶段4][微信登录成功] 时间：{} | 结果：userId={}, role={}", System.currentTimeMillis(), user.getId(), user.getRole());
            return Response.success("登录成功", result);
        } catch (Exception e) {
            log.error("[失败][阶段4][微信登录异常] 时间：{} | 原因：{}", System.currentTimeMillis(), e.getMessage());
            return Response.error("微信登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Response<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("phone") != null ? loginData.get("phone") : loginData.get("username");
        String password = loginData.get("password");

        log.info("[成功][阶段2][用户登录] 时间：{} | 参数：username={}", System.currentTimeMillis(), username);

        User loginUser = userService.login(username, password);
        if (loginUser != null) {
            String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getUsername(), loginUser.getRole());
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", sanitizeUser(loginUser));

            log.info("[成功][阶段4][登录成功] 时间：{} | 结果：userId={}, role={}", System.currentTimeMillis(), loginUser.getId(), loginUser.getRole());
            return Response.success("登录成功", result);
        }
        log.warn("[失败][阶段4][登录失败] 时间：{} | 原因：用户名或密码错误", System.currentTimeMillis());
        return Response.error("用户名或密码错误");
    }

    @PostMapping
    @ApiOperation("添加用户")
    public Response<User> addUser(@RequestBody User user) {
        log.info("[成功][阶段2][添加用户] 时间：{} | 参数：username={}", System.currentTimeMillis(), user.getUsername());

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null) {
            user.setRole(0);
        }

        if (userService.register(user)) {
            log.info("[成功][阶段4][添加成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), user.getId());
            return Response.success("添加成功", sanitizeUser(user));
        }
        log.warn("[失败][阶段4][添加失败] 时间：{} | 原因：用户名已存在", System.currentTimeMillis());
        return Response.error("用户名已存在");
    }

    @PutMapping("/{id}")
    @ApiOperation("更新用户")
    public Response<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("[成功][阶段2][更新用户] 时间：{} | 参数：id={}", System.currentTimeMillis(), id);
        user.setId(id);
        user.setRole(null);
        user.setPassword(null);
        if (userService.updateById(user)) {
            User updatedUser = userService.getById(id);
            log.info("[成功][阶段4][更新成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), id);
            return Response.success("更新成功", sanitizeUser(updatedUser));
        }
        log.warn("[失败][阶段4][更新失败] 时间：{} | 原因：更新失败", System.currentTimeMillis());
        return Response.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Response<Void> deleteUser(@PathVariable Long id) {
        log.info("[成功][阶段2][删除用户] 时间：{} | 参数：id={}", System.currentTimeMillis(), id);
        if (userService.removeById(id)) {
            log.info("[成功][阶段4][删除成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), id);
            return Response.success(null);
        }
        log.warn("[失败][阶段4][删除失败] 时间：{} | 原因：删除失败", System.currentTimeMillis());
        return Response.error("删除失败");
    }

    @GetMapping("/profile")
    @ApiOperation("获取个人信息")
    public Response<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][获取个人信息] 时间：{} | 参数：用户={}", System.currentTimeMillis(), auth.getName());

        if (principal instanceof Long) {
            Long userId = (Long) principal;
            User user = userService.getById(userId);
            if (user != null) {
                log.info("[成功][阶段4][返回个人信息] 时间：{} | 结果：id={}, username={}", System.currentTimeMillis(), user.getId(), user.getUsername());
                return Response.success("获取成功", sanitizeUser(user));
            }
        }

        log.warn("[失败][阶段4][获取失败] 时间：{} | 原因：用户不存在", System.currentTimeMillis());
        return Response.error("用户不存在");
    }

    @PutMapping("/profile")
    @ApiOperation("更新个人信息")
    public Response<User> updateProfile(@RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][更新个人信息] 时间：{} | 参数：user={}", System.currentTimeMillis(), user.getId());

        if (principal instanceof Long) {
            Long userId = (Long) principal;
            user.setId(userId);
            if (userService.updateById(user)) {
                User updatedUser = userService.getById(userId);
                log.info("[成功][阶段4][更新成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), userId);
                return Response.success("更新成功", sanitizeUser(updatedUser));
            }
        }
        log.warn("[失败][阶段4][更新失败] 时间：{} | 原因：更新失败", System.currentTimeMillis());
        return Response.error("更新失败");
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Response<?> updatePassword(@RequestBody Map<String, Object> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("[成功][阶段2][修改密码] 时间：{} | 参数：用户={}", System.currentTimeMillis(), auth.getName());

        if (principal instanceof Long) {
            Long userId = (Long) principal;
            String oldPassword = (String) params.get("oldPassword");
            String newPassword = (String) params.get("newPassword");

            if (newPassword == null || newPassword.length() < 6) {
                log.warn("[失败][阶段2][修改密码] 时间：{} | 原因：新密码长度不足", System.currentTimeMillis());
                return Response.error("新密码长度至少为6位");
            }

            User user = userService.getById(userId);
            if (user != null && userService.login(user.getUsername(), oldPassword) != null) {
                user.setPassword(passwordEncoder.encode(newPassword));
                if (userService.updateById(user)) {
                    log.info("[成功][阶段4][密码修改成功] 时间：{} | 结果：id={}", System.currentTimeMillis(), userId);
                    return Response.success("密码修改成功");
                }
            }
        }
        log.warn("[失败][阶段4][密码修改失败] 时间：{} | 原因：原密码错误", System.currentTimeMillis());
        return Response.error("原密码错误或更新失败");
    }
}
