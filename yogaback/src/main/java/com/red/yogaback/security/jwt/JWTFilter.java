package com.red.yogaback.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.red.yogaback.security.dto.CustomUserDetails;
import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.error.CustomException;
import com.red.yogaback.error.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("Processing JWT authentication...");

        try {
            String token = jwtUtil.getJwtFromRequest(request);

            if (token == null || jwtUtil.isExpired(token)) {
                throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
            }

            // 토큰에서 추가 정보를 추출합니다.
            Long userId = jwtUtil.getMemberId(token);
            String userNickName = jwtUtil.getUserNickName(token);
            String userProfile = jwtUtil.getUserProfile(token);

            // 추가 정보를 포함하여 CustomUserDetails 생성
            CustomUserDetails userDetails = new CustomUserDetails(userId, userNickName, userProfile);

            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            setErrorResponse(response, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        log.info("Current path: {}", path);
        return path.startsWith("/api/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources/") ||
                path.startsWith("/webjars/") ||
                path.equals("/api/notifications/button-patterns") ||
                path.startsWith("/api/groups/invite") ||
                path.startsWith("/api/yoga/all") ||
                path.startsWith("/api/yoga/detail") ||
                path.startsWith("/ws");
    }

    private void setErrorResponse(HttpServletResponse response, CustomException e) throws IOException {
        ErrorResponse error = new ErrorResponse(e.getErrorCode().getHttpStatus(), e.getMessage());
        response.setStatus(e.getErrorCode().getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
