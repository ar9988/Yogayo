package com.red.yogaback.auth;

import com.red.yogaback.auth.dto.LoginRequest;
import com.red.yogaback.auth.dto.LoginResponse;
import com.red.yogaback.auth.dto.SignUpRequest;
import com.red.yogaback.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@Tag(name = "회원 API", description = "회원 가입 및 로그인 기능")
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입", description = "필요 파라미터 : 로그인 ID, 비밀번호, 이름, 닉네임, 프로필 사진(선택)"
    public ResponseEntity<Boolean> signUp(
            @RequestPart("signUpRequest") SignUpRequest signUpRequest,
            @RequestPart(value = "userProfile", required = false) MultipartFile userProfile){
        signUpRequest.setUserProfile(Optional.ofNullable(userProfile));
        Boolean isSuccess = authService.signUp(signUpRequest,userProfile);
        return ResponseEntity.ok(isSuccess);
    }

    @GetMapping("/duplicate-check")
    @Operation(summary = "아이디 중복확인", description = "필요 파라미터 : 로그인 ID")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam(name = "userLoginId") String userLoginId) {
        return ResponseEntity.ok(authService.isIdDuplicate(userLoginId));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "필요 파라미터 : 로그인 ID, 비밀번호")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}