package com.parking.system;

import com.parking.system.common.Response;
import com.parking.system.controller.UserController;
import com.parking.system.entity.User;
import com.parking.system.service.UserService;
import com.parking.system.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setupAuthentication(Long userId, boolean isAdmin) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (isAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId, "credentials", authorities);
        SecurityContext context = new SecurityContextImpl(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testRegister_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");

        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encodedpassword");
        when(userService.register(any(User.class))).thenReturn(true);

        Response<User> response = userController.register(user);

        assertEquals(200, response.getCode());
        assertEquals("注册成功", response.getMessage());
        verify(passwordEncoder).encode("123456");
        verify(userService).register(any(User.class));
    }

    @Test
    void testRegister_EmptyUsername() {
        User user = new User();
        user.setUsername("");
        user.setPassword("123456");

        Response<User> response = userController.register(user);

        assertEquals(400, response.getCode());
        assertEquals("用户名不能为空", response.getMessage());
        verify(userService, never()).register(any(User.class));
    }

    @Test
    void testRegister_ShortUsername() {
        User user = new User();
        user.setUsername("ab");
        user.setPassword("123456");

        Response<User> response = userController.register(user);

        assertEquals(400, response.getCode());
        assertEquals("用户名长度不能少于3位", response.getMessage());
        verify(userService, never()).register(any(User.class));
    }

    @Test
    void testRegister_EmptyPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        Response<User> response = userController.register(user);

        assertEquals(400, response.getCode());
        assertEquals("密码不能为空", response.getMessage());
        verify(userService, never()).register(any(User.class));
    }

    @Test
    void testRegister_ShortPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("12345");

        Response<User> response = userController.register(user);

        assertEquals(400, response.getCode());
        assertEquals("密码长度不能少于6位", response.getMessage());
        verify(userService, never()).register(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");

        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encodedpassword");
        when(userService.register(any(User.class))).thenReturn(false);

        Response<User> response = userController.register(user);

        assertEquals(400, response.getCode());
        assertEquals("用户名已存在", response.getMessage());
    }

    @Test
    void testLogin_Success() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "123456");

        User loginUser = new User();
        loginUser.setId(1L);
        loginUser.setUsername("testuser");
        loginUser.setRole(0);

        when(userService.login("testuser", "123456")).thenReturn(loginUser);
        when(jwtUtil.generateToken(1L, "testuser", 0)).thenReturn("mock-token");

        Response<Map<String, Object>> response = userController.login(loginData);

        assertEquals(200, response.getCode());
        assertEquals("登录成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("mock-token", response.getData().get("token"));
        verify(jwtUtil).generateToken(1L, "testuser", 0);
    }

    @Test
    void testLogin_WrongPassword() {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "wrongpassword");

        when(userService.login("testuser", "wrongpassword")).thenReturn(null);

        Response<Map<String, Object>> response = userController.login(loginData);

        assertEquals(400, response.getCode());
        assertEquals("用户名或密码错误", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testUpdateUser_CannotModifyRole() {
        Long userId = 1L;

        User inputUser = new User();
        inputUser.setName("新名字");
        inputUser.setRole(1);

        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUsername("testuser");
        dbUser.setName("新名字");
        dbUser.setRole(0);

        when(userService.updateById(any(User.class))).thenReturn(true);
        when(userService.getById(1L)).thenReturn(dbUser);

        Response<User> response = userController.updateUser(userId, inputUser);

        assertEquals(200, response.getCode());
        verify(userService).updateById(argThat(user ->
                user.getRole() == null && user.getPassword() == null
        ));
    }

    @Test
    void testUpdateUser_CannotModifyPassword() {
        Long userId = 1L;

        User inputUser = new User();
        inputUser.setName("新名字");
        inputUser.setPassword("newpassword");

        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUsername("testuser");
        dbUser.setName("新名字");
        dbUser.setRole(0);

        when(userService.updateById(any(User.class))).thenReturn(true);
        when(userService.getById(1L)).thenReturn(dbUser);

        Response<User> response = userController.updateUser(userId, inputUser);

        assertEquals(200, response.getCode());
        verify(userService).updateById(argThat(user ->
                user.getPassword() == null && user.getRole() == null
        ));
    }

    @Test
    void testGetAllUsers_NonAdmin() {
        setupAuthentication(2L, false);

        Response<List<User>> response = userController.getAllUsers();

        assertEquals(403, response.getCode());
        assertEquals("无权访问，仅管理员可操作", response.getMessage());
        verify(userService, never()).list();
    }

    @Test
    void testChangePassword_Success() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("oldPassword", "oldpass");
        params.put("newPassword", "newpass");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$oldencoded");

        when(userService.getById(1L)).thenReturn(user);
        when(userService.login("testuser", "oldpass")).thenReturn(user);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newencoded");
        when(userService.updateById(any(User.class))).thenReturn(true);

        Response<?> response = userController.updatePassword(params);

        assertEquals(200, response.getCode());
        assertEquals("密码修改成功", response.getData());
        verify(userService).updateById(argThat(u -> "$2a$10$newencoded".equals(u.getPassword())));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        setupAuthentication(1L, false);

        Map<String, Object> params = new HashMap<>();
        params.put("oldPassword", "wrongold");
        params.put("newPassword", "newpass");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$oldencoded");

        when(userService.getById(1L)).thenReturn(user);
        when(userService.login("testuser", "wrongold")).thenReturn(null);

        Response<?> response = userController.updatePassword(params);

        assertEquals(400, response.getCode());
        assertTrue(response.getMessage().contains("原密码错误"));
        verify(userService, never()).updateById(any(User.class));
    }
}
