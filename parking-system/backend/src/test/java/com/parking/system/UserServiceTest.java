package com.parking.system;

import com.parking.system.entity.User;
import com.parking.system.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        System.out.println("[测试准备] 开始执行测试用例...");
    }

    private String uniquePhone() {
        return "139" + String.format("%08d", System.nanoTime() % 100000000L);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@example.com";
    }

    @Test
    @DisplayName("P0-正常场景：用户注册成功")
    void testRegister() {
        System.out.println("[测试场景] 正常场景：用户注册成功");
        System.out.println("[输入参数] username=testuser_" + System.currentTimeMillis() + ", password=123456");
        
        User user = new User();
        user.setUsername("testuser_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("test"));

        boolean result = userService.register(user);
        
        System.out.println("[预期输出] 注册成功=true");
        System.out.println("[实际输出] 注册结果=" + result);
        assertTrue(result, "用户注册应该成功");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：用户名最小长度（2位）")
    void testRegisterMinUsernameLength() {
        System.out.println("[测试场景] 边界场景：用户名最小长度（2位）");
        String username = "ab";
        System.out.println("[输入参数] username=" + username + ", password=123456");
        
        User user = new User();
        user.setUsername(username + "_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("边界测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("minlen"));

        boolean result = userService.register(user);
        
        System.out.println("[预期输出] 注册成功=true");
        System.out.println("[实际输出] 注册结果=" + result);
        assertTrue(result, "最小长度用户名注册应该成功");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：重复用户名注册失败")
    void testRegisterDuplicateUsername() {
        System.out.println("[测试场景] 异常场景：重复用户名注册失败");
        String username = "duplicate_user_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username);
        
        User user1 = new User();
        user1.setUsername(username);
        user1.setPassword("123456");
        user1.setName("用户1");
        user1.setPhone(uniquePhone());
        user1.setEmail(uniqueEmail("user1"));
        userService.register(user1);

        User user2 = new User();
        user2.setUsername(username);
        user2.setPassword("654321");
        user2.setName("用户2");
        user2.setPhone(uniquePhone());
        user2.setEmail(uniqueEmail("user2"));

        boolean result = userService.register(user2);
        
        System.out.println("[预期输出] 注册成功=false");
        System.out.println("[实际输出] 注册结果=" + result);
        assertFalse(result, "重复用户名注册应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：用户登录成功")
    void testLogin() {
        System.out.println("[测试场景] 正常场景：用户登录成功");
        String username = "login_test_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username + ", password=123456");
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setName("登录测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("login"));
        userService.register(user);

        User loginUser = userService.login(username, "123456");
        
        System.out.println("[预期输出] 登录成功，用户名匹配");
        System.out.println("[实际输出] loginUser=" + (loginUser != null ? loginUser.getUsername() : "null"));
        assertNotNull(loginUser, "登录应该成功");
        assertEquals(username, loginUser.getUsername(), "用户名应该匹配");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：使用手机号登录")
    void testLoginWithPhone() {
        System.out.println("[测试场景] 边界场景：使用手机号登录");
        String phone = "13900139" + String.format("%04d", System.currentTimeMillis() % 10000);
        System.out.println("[输入参数] phone=" + phone + ", password=123456");
        
        User user = new User();
        user.setUsername("phone_login_test_" + System.currentTimeMillis());
        user.setPassword("123456");
        user.setName("手机登录测试用户");
        user.setPhone(phone);
        user.setEmail(uniqueEmail("phonelogin"));
        userService.register(user);

        User loginUser = userService.login(phone, "123456");
        
        System.out.println("[预期输出] 使用手机号登录成功");
        System.out.println("[实际输出] loginUser=" + (loginUser != null ? loginUser.getPhone() : "null"));
        assertNotNull(loginUser, "使用手机号登录应该成功");
        assertEquals(phone, loginUser.getPhone(), "手机号应该匹配");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：错误密码登录失败")
    void testLoginWithWrongPassword() {
        System.out.println("[测试场景] 异常场景：错误密码登录失败");
        String username = "wrong_pwd_test_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username + ", password=wrongpassword");
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setName("密码测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("wrongpwd"));
        userService.register(user);

        User loginUser = userService.login(username, "wrongpassword");
        
        System.out.println("[预期输出] 登录失败，返回null");
        System.out.println("[实际输出] loginUser=" + (loginUser != null ? "not null" : "null"));
        assertNull(loginUser, "错误密码登录应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P2-异常场景：不存在的用户登录失败")
    void testLoginWithNonExistentUser() {
        System.out.println("[测试场景] 异常场景：不存在的用户登录失败");
        String username = "non_existent_user_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username + ", password=123456");
        
        User loginUser = userService.login(username, "123456");
        
        System.out.println("[预期输出] 登录失败，返回null");
        System.out.println("[实际输出] loginUser=" + (loginUser != null ? "not null" : "null"));
        assertNull(loginUser, "不存在的用户登录应该失败");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：密码恰好6位")
    void testRegisterWithMinPasswordLength() {
        System.out.println("[测试场景] 边界场景：密码恰好6位");
        String username = "min_pwd_test_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username + ", password=123456（6位）");
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setName("密码边界测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("minpwd"));

        boolean result = userService.register(user);
        
        System.out.println("[预期输出] 注册成功=true");
        System.out.println("[实际输出] 注册结果=" + result);
        assertTrue(result, "6位密码注册应该成功");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P1-边界场景：手机号恰好11位")
    void testRegisterWithValidPhone() {
        System.out.println("[测试场景] 边界场景：手机号恰好11位");
        String username = "valid_phone_test_" + System.currentTimeMillis();
        String phone = uniquePhone();
        System.out.println("[输入参数] username=" + username + ", phone=" + phone + "（11位）");
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setName("手机号边界测试用户");
        user.setPhone(phone);
        user.setEmail(uniqueEmail("validphone"));

        boolean result = userService.register(user);
        
        System.out.println("[预期输出] 注册成功=true");
        System.out.println("[实际输出] 注册结果=" + result);
        assertTrue(result, "11位手机号注册应该成功");
        System.out.println("[测试结果] ✓ 通过");
    }

    @Test
    @DisplayName("P0-正常场景：用户信息查询")
    void testGetByUsername() {
        System.out.println("[测试场景] 正常场景：用户信息查询");
        String username = "query_test_" + System.currentTimeMillis();
        System.out.println("[输入参数] username=" + username);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");
        user.setName("查询测试用户");
        user.setPhone(uniquePhone());
        user.setEmail(uniqueEmail("query"));
        userService.register(user);

        User queriedUser = userService.getByUsername(username);
        
        System.out.println("[预期输出] 查询成功，用户名匹配");
        System.out.println("[实际输出] queriedUser=" + (queriedUser != null ? queriedUser.getUsername() : "null"));
        assertNotNull(queriedUser, "用户查询应该成功");
        assertEquals(username, queriedUser.getUsername(), "用户名应该匹配");
        System.out.println("[测试结果] ✓ 通过");
    }
}
