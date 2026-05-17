package com.parking.system.config;

import com.parking.system.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers("/users/login", "/users/register", "/users/wechat-login").permitAll()
                .antMatchers(HttpMethod.GET, "/parking-lots", "/parking-lots/**").permitAll()
                .antMatchers(HttpMethod.GET, "/geo/**").permitAll()
                .antMatchers(HttpMethod.GET, "/parking-spaces", "/parking-spaces/{id}").permitAll()
                .antMatchers("/health/**").permitAll()
                .antMatchers("/ws/**").permitAll()
                .antMatchers("/payment/callback/**").permitAll()
                .antMatchers(HttpMethod.POST, "/parking-spaces").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/parking-spaces/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/parking-spaces/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/parking-lots").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/parking-lots/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/parking-lots/**").hasRole("ADMIN")
                .antMatchers("/cache/**").hasRole("ADMIN")
                .antMatchers("/sensors/**").hasRole("ADMIN")
                .antMatchers("/vehicle-records/**").hasRole("ADMIN")
                .antMatchers("/order-management/**").hasRole("ADMIN")
                .antMatchers("/payment/list", "/payment/refund", "/payment/refund/status").hasRole("ADMIN")
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
