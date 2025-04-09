package com.red.yogaback.config;

import com.red.yogaback.auth.dto.SignUpRequest;
import com.red.yogaback.auth.service.AuthService;
import com.red.yogaback.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile({"local", "prod"})
@Slf4j
public class DBInitConfig implements ApplicationRunner {

    private final AuthService authServiceImpl;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.count() == 0) initData();
    }

    public void initData() {
        authServiceImpl.signUp(new SignUpRequest("user1","123", "박성민", "박성민 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("user2", "123","김아름", "김아름 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("user3", "123","황선혁", "황선혁 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("user4", "123","황홍법", "황홍법 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("user5", "123","김웅기", "김웅기 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("user6", "123","경이현", "경이현 닉네임"),null);
        authServiceImpl.signUp(new SignUpRequest("test", "123","테스트 계정", "테스트 계정 닉네임"),null);
        log.info("사람 넣기 완료");
    }
}
