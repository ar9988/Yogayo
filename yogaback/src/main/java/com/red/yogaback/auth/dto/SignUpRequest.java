package com.red.yogaback.auth.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {
    private String userLoginId;
    private String userPwd;
    private String userName;
    private String userNickname;
    private MultipartFile userProfile;
}