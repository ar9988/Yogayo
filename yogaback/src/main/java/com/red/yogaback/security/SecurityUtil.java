package com.red.yogaback.security;

import com.red.yogaback.security.dto.CustomUserDetails;
import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.error.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {

    public static Long getCurrentMemberId() {
        CustomUserDetails userDetails = getCustomUserDetails();
        return userDetails.getUserId();
    }

    public static String getCurrentUserNickName() {
        CustomUserDetails userDetails = getCustomUserDetails();
        return userDetails.getUserNickName();
    }

    public static String getCurrentUserProfile() {
        CustomUserDetails userDetails = getCustomUserDetails();
        return userDetails.getUserProfile();
    }

    private static CustomUserDetails getCustomUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
