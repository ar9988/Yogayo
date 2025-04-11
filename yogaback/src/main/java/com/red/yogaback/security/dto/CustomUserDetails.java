package com.red.yogaback.security.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private Long userId;
    private String userNickName;
    private String userProfile;
    private Collection<GrantedAuthority> authorities;

    // 새 생성자: userId, userNickName, userProfile를 함께 설정
    public CustomUserDetails(Long userId, String userNickName, String userProfile) {
        this.userId = userId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
        // 권한 정보는 상황에 따라 추가합니다. 여기서는 기본적으로 빈 리스트로 설정합니다.
        this.authorities = new ArrayList<>();
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserNickName() {
        return userNickName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // 비밀번호는 사용하지 않음
    }

    @Override
    public String getUsername() {
        // 필요에 따라 userNickName을 사용자 이름으로 반환하거나, 다른 값을 반환할 수 있습니다.
        return userNickName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
