package com.red.yogaback.auth.service;

import com.red.yogaback.auth.dto.LoginRequest;
import com.red.yogaback.auth.dto.LoginResponse;
import com.red.yogaback.auth.dto.SignUpRequest;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.UserRepository;
import com.red.yogaback.repository.UserRecordRepository;
import com.red.yogaback.security.jwt.JWTToken;
import com.red.yogaback.security.jwt.JWTUtil;
import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.red.yogaback.service.S3FileStorageService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final S3FileStorageService s3FileStorageService;
    // 추가: UserRecordRepository 주입
    private final UserRecordRepository userRecordRepository;

    @Override //회원가입
    public boolean signUp(SignUpRequest signUpRequest, MultipartFile userProfile) {
        try {
            if(isIdDuplicate(signUpRequest.getUserLoginId())) { //유저로그인 아이디를 불러와서 중복 조회
                log.info("중복검사");
                throw new CustomException(ErrorCode.EXIST_ID);
            } else {
                String userProfileUrl;
                if (userProfile != null && !userProfile.isEmpty()) {
                    userProfileUrl = s3FileStorageService.storeFile(userProfile);
                } else {
                    userProfileUrl = "";
                }
                User user = User.builder()
                        .userLoginId(signUpRequest.getUserLoginId())
                        .userPwd(passwordEncoder.encode(signUpRequest.getUserPwd()))
                        .userName(signUpRequest.getUserName())
                        .userNickname(signUpRequest.getUserNickname())
                        .userProfile(userProfileUrl)
                        .createdAt(System.currentTimeMillis())
                        .build();
                userRepository.save(user);

                // User 생성 후 기본 값으로 UserRecord 생성
                UserRecord userRecord = UserRecord.builder()
                        .user(user)
                        .exConDays(0L)
                        .roomWin(0L)
                        .exDays(0L)
                        .createAt(System.currentTimeMillis())
                        .build();
                userRecordRepository.save(userRecord);

                return true;
            }
        } catch (Exception e) {
            log.debug(e.getMessage());
            return false;
        }
    }

    @Override // 중복 확인
    public boolean isIdDuplicate(String userLoginId) {
        return userRepository.existsByUserLoginId(userLoginId);
    }//유저아이디가 데이터 베이스에 있는지 확인하고 true나 false로 반환

    @Override//로그인
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUserLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_FAILURE));

        boolean matchPassword = passwordEncoder.matches(loginRequest.getPassword(), user.getUserPwd());

        if (matchPassword) {
            // JWT 토큰 생성 시 userId, userNickname, userProfile을 함께 전달
            JWTToken jwtToken = jwtUtil.createTokens(
                    user.getUserId(),
                    user.getUserNickname(),
                    user.getUserProfile()
            );
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
