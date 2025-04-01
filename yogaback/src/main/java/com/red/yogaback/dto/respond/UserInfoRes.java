package com.red.yogaback.dto.respond;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoRes {
    private Long userId;
    private String userName;
    private String userNickName;
    private String userProfile;
    private Long exDays;
    private Long exConDays;
    private Long roomWin;
}
