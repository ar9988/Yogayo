package com.red.yogaback.security.jwt;

import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.error.CustomException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JWTUtil {

    @Value("${jwt.expiration.access}")
    private Long accessTokenValidTime;

    @Value("${jwt.expiration.refresh}")
    private Long refreshTokenValidTime;
    private SecretKey secretKey;

    public JWTUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_FORM);
        }
        String token = authorization.split(" ")[1];
        return token;
    }

    public Long getMemberId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("memberId", Long.class);
    }

    public String getUserNickName(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userNickName", String.class);
    }

    public String getUserProfile(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userProfile", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public boolean isExpired(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return false;
    }

    // JWT 토큰 생성 시 memberId, userNickName, userProfile 정보를 모두 포함
    public JWTToken createTokens(Long memberId, String userNickName, String userProfile) {
        String accessToken = createToken(memberId, userNickName, userProfile, accessTokenValidTime);
        String refreshToken = createToken(memberId, userNickName, userProfile, refreshTokenValidTime);
        return new JWTToken("Bearer", accessToken, refreshToken);
    }

    public String createAccessToken(Long memberId, String userNickName, String userProfile) {
        return createToken(memberId, userNickName, userProfile, accessTokenValidTime);
    }

    public String createRefreshToken(Long memberId, String userNickName, String userProfile) {
        return createToken(memberId, userNickName, userProfile, refreshTokenValidTime);
    }

    // createToken 메소드를 수정하여 추가 정보를 payload에 포함시킴
    public String createToken(Long memberId, String userNickName, String userProfile, Long expiredMs) {
        return Jwts.builder()
                .claim("memberId", memberId)
                .claim("userNickName", userNickName)
                .claim("userProfile", userProfile)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public JWTToken refresh(String refreshToken) {
        if (verifyRefreshToken(refreshToken)) {
            Long memberId = getMemberId(refreshToken);
            String userNickName = getUserNickName(refreshToken);
            String userProfile = getUserProfile(refreshToken);
            String accessToken = createAccessToken(memberId, userNickName, userProfile);
            return new JWTToken("Bearer", accessToken, createRefreshToken(memberId, userNickName, userProfile));
        }
        return null;
    }

    public Boolean verifyRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true; // 토큰 검증 성공
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
