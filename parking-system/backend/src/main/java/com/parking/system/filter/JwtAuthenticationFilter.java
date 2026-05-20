package com.parking.system.filter;

import com.parking.system.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestPath.substring(contextPath.length());

        if (isWhitelistedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (jwtUtil.validateToken(jwt)) {
                    Claims claims = jwtUtil.getClaimsFromToken(jwt);
                    String username = claims.getSubject();
                    Long userId = claims.get("userId", Long.class);
                    Integer role = claims.get("role", Integer.class);

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (role != null && role == 1) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else {
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("[成功][阶段1][JWT认证] 时间：{} | 用户ID：{} | 角色：{}", System.currentTimeMillis(), userId, role == 1 ? "管理员" : "普通用户");
                } else {
                    log.warn("[失败][阶段1][JWT认证] 时间：{} | 原因：Token无效或已过期", System.currentTimeMillis());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期，请重新登录\",\"data\":null}");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("[失败][阶段1][JWT认证] 时间：{} | 原因：{}", System.currentTimeMillis(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":401,\"message\":\"认证失败，请重新登录\",\"data\":null}");
            } catch (IOException ignored) {}
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelistedPath(String path) {
        return path.equals("/users/login") ||
               path.equals("/users/register") ||
               path.equals("/users/wechat-login") ||
               path.startsWith("/health/") ||
               path.startsWith("/ws/") ||
               path.startsWith("/payment/callback/") ||
               path.startsWith("/geo/");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
