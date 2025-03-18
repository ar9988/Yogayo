package com.red.yogaback.security.jwt;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JWTResponse {

    private String accessToken;
    private String refreshToken;

    @Builder
    public JWTResponse(JWTToken token) {
        this.accessToken = token.getAccessToken();
        this.refreshToken = token.getRefreshToken();
    }
}