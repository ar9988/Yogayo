package com.red.yogaback.controller;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.security.SecurityUtil;
import com.red.yogaback.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "마이페이지 및 배지 관련 API", description = "유저 정보, 배지 조회, 교체 기능")
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final BadgeService badgeService;



    @GetMapping("/badge")
    @Operation(summary = "배지 목록 조회")
    public ResponseEntity<List<BadgeListRes>> getBadgeList() {
        Long userId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(badgeService.getBadgeList(userId));
    }

    @GetMapping("/info")
    @Operation(summary = "유저 정보 조회")
    private ResponseEntity<UserInfoRes> getUserInfo() {
        Long userId = SecurityUtil.getCurrentMemberId();
        return ResponseEntity.ok(badgeService.getUserInfo(userId));
    }
}
