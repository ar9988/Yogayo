package com.red.yogaback.auth.service;
import com.red.yogaback.auth.dto.LoginResponse;
import com.red.yogaback.auth.dto.LoginRequest;
import com.red.yogaback.auth.dto.SignUpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface AuthService {
    /*회원가입 처리 메서드
     * 클라이언트에서 회원가입 정보를 담은 객체를 전달하면,
     * 회원가입 절차를 진행하고 성공 여부를 true 또는 false로 반환*/
    boolean signUp(SignUpRequest signUpRequest, MultipartFile userProfile);

    /*회원가입이나 ID 변경 시, 사용자가 입력한 로그인 아이디가 이미 사용 중인지 확인하는 메서드.
    중복 여부에 따라 true(중복됨) 또는 false(사용 가능)를 반환.*/
    boolean isIdDuplicate(String userLoginId);

    /*로그인 작업을 수행하는 메서드
    * 클라이언트에서 로그인 정보(예: loginId, password 등)를 담은 LoginRequest 객체를 보내면,
    *  인증에 성공할 경우 사용자 정보를 담은 LoginResponse 객체를 반환*/
    LoginResponse login(LoginRequest loginRequest);
}