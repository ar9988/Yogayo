package com.red.yogaback.auth.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {
    private String userLoginId;
    private String userPwd;
    private String userName;
    private String userNickname;
    private String userProfile;
}