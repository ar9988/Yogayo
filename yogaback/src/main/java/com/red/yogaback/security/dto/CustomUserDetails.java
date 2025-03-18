 package com.red.yogaback.security.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

 public class CustomUserDetails implements UserDetails {
     private Long userId;
     private Collection<GrantedAuthority> authorities;

     public CustomUserDetails(Long userId) {
         this.userId = userId;
 //        this.authorities = createAuthorities(roles);
     }

     public Long getUserId() {
         return userId;
     }

     private Collection<GrantedAuthority> createAuthorities(String roles){
         Collection<GrantedAuthority> authorities = new ArrayList<>();

         for(String role : roles.split(",")){
             if (!StringUtils.hasText(role)) continue;
             authorities.add(new SimpleGrantedAuthority(role));
         }
         return authorities;
     }

     @Override
     public Collection<? extends GrantedAuthority> getAuthorities() {
         return authorities;
     }

     @Override
     public String getPassword() {
         // 비밀번호는 사용하지 않으므로 null 반환
         return null;
     }

     @Override
     public String getUsername() {
         // 사용자를 식별할 수 있는 유니크한 값 반환
         return null;
     }

     @Override
     public boolean isAccountNonExpired() {
         // 계정 만료 여부, 여기서는 항상 true 반환
         return true;
     }

     @Override
     public boolean isAccountNonLocked() {
         // 계정 잠김 여부, 여기서는 항상 true 반환
         return true;
     }

     @Override
     public boolean isCredentialsNonExpired() {
         // 자격 증명 만료 여부, 여기서는 항상 true 반환
         return true;
     }

     @Override
     public boolean isEnabled() {
         // 계정 활성화 여부, 여기서는 항상 true 반환
         return true;
     }
 }
