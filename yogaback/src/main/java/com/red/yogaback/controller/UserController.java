package com.red.yogaback.controller;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final BadgeService badgeService;

    @GetMapping("/badge")
    public ResponseEntity<List<BadgeListRes>> getBadgeList(Long userId) {
        return ResponseEntity.ok(badgeService.getBadgeList(userId));
    }

}
