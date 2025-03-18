package com.red.yogaback.auth;

import com.red.yogaback.auth.dto.LoginRequest;
import com.red.yogaback.auth.dto.LoginResponse;
import com.red.yogaback.auth.dto.SignUpRequest;
import com.red.yogaback.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<Boolean> signUp(@RequestBody SignUpRequest signUpRequest) {
        Boolean isSuccess = authService.signUp(signUpRequest);
        return ResponseEntity.ok(isSuccess);
    }

    @GetMapping("/duplicate-check")
    public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam(name = "userLoginId") String userLoginId) {
        return ResponseEntity.ok(authService.isIdDuplicate(userLoginId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
}