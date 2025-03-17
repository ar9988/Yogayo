package com.red.yogaback.auth.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginResponse {
    private Long userId;
    private String userLoginId;
    private String userName;
    private String userNickname;
    private String userProfile;
    private String accessToken;
    private String refreshToken;
}
