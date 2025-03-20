package com.red.yogaback.auth.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@Setter
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {
    private String userLoginId;
    private String userPwd;
    private String userName;
    private String userNickname;

//    @Builder.Default
//    private Optional<MultipartFile> userProfile = Optional.empty();
}