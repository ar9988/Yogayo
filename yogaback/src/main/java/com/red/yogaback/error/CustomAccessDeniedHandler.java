package com.red.yogaback.error;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;
/*Spring Security에서 인가(authorization) 실패 시
 처리하는 사용자 정의 핸들러(Custom Access Denied Handler)를 구현
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LocalDateTime currentTimeStamp = LocalDateTime.now();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ? accessDeniedException.getMessage() : "Authorization failed";
        String path = request.getRequestURI();

        response.setHeader("yogayo-denied-reason", "Authentication failed");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format("{\"status\": %d, \"message\": \"%s\"}",
                HttpStatus.FORBIDDEN.value(), message);
        response.getWriter().write(jsonResponse);
    }
}
