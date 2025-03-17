package com.red.yogaback.auth.service;

import com.red.yogaback.auth.AuthRepository;
import com.red.yogaback.auth.dto.LoginRequest;
import com.red.yogaback.auth.dto.LoginResponse;
import com.red.yogaback.auth.dto.SignUpRequest;
import com.red.yogaback.model.User;
import com.red.yogaback.security.jwt.JWTToken;
import com.red.yogaback.security.jwt.JWTUtil;
import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public boolean signUp(SignUpRequest signUpRequest) {
        try {
            if(isIdDuplicate(signUpRequest.getUserLoginId())) {
                throw new CustomException(ErrorCode.EXIST_ID);
            } else {
                User user = User.builder()
                        .userLoginId(signUpRequest.getUserLoginId())
                        .userPwd(passwordEncoder.encode(signUpRequest.getUserPwd()))
                        .userName(signUpRequest.getUserName())
                        .userNickname(signUpRequest.getUserNickname())
                        .userProfile(signUpRequest.getUserProfile())
                        .build();
                authRepository.save(user);
                return true;
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isIdDuplicate(String userLoginId) {
        return authRepository.existsByUserLoginId(userLoginId);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = authRepository.findByUserLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_FAILURE));

        boolean matchPassword = passwordEncoder.matches(loginRequest.getPassword(), user.getUserPwd());

        if (matchPassword) {
            JWTToken jwtToken = jwtUtil.createTokens(user.getUserId());
            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .userLoginId(user.getUserLoginId())
                    .userName(user.getUserName())
                    .userNickname(user.getUserNickname())
                    .userProfile(user.getUserProfile())
                    .accessToken(jwtToken.getAccessToken())
                    .refreshToken(jwtToken.getRefreshToken())
                    .build();
        } else {
            throw new CustomException(ErrorCode.AUTH_FAILURE);
        }
    }
}
