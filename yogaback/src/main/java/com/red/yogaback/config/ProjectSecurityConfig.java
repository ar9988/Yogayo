package com.red.yogaback.config;

import com.red.yogaback.security.jwt.JWTFilter;
import com.red.yogaback.security.jwt.JWTUtil;
import com.red.yogaback.error.CustomAccessDeniedHandler;
import com.red.yogaback.error.CustomBasicAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.password.HaveIBeenPwnedRestApiPasswordChecker;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
public class ProjectSecurityConfig {

    private final JWTUtil jwtUtil;

    @Value("${url.https}")
    private String httpsServerUrl;

    @Value("${url.http}")
    private String httpServerUrl;

    public ProjectSecurityConfig(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/auth/**", "/error", "/api/notifications/button-patterns", "/api/groups/invite/**", "/api/yoga/all", "/api/yoga/detail/**","/ws").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs","/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JWTFilter(jwtUtil), BasicAuthenticationFilter.class)
                .requiresChannel(rcc -> rcc.anyRequest().requiresInsecure())
                .exceptionHandling(ehc -> ehc.accessDeniedHandler(new CustomAccessDeniedHandler())
                        .authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint()));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CompromisedPasswordChecker compromisedPasswordChecker() {
        return new HaveIBeenPwnedRestApiPasswordChecker();
    }
}
