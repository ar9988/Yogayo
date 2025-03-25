package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.model.Badge;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserBadge;
import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final UserRecordRepository userRecordRepository;


    // 배지 목록 요청
    public List<BadgeListRes> getBadgeList(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchElementException("유저를 찾을 수 없습니다.")
        );
        List<UserBadge> userBadges = userBadgeRepository.findByUser(user);
        List<Badge> badges = badgeRepository.findAll();


        return badges.stream().map((badge) -> {
            Optional<UserBadge> userBadgeOpt = userBadges.stream().filter(userBadge -> userBadge.getBadge().equals(badge))
                    .findFirst();

            int progress = userBadgeOpt.map(UserBadge::getProgress).orElse(0);
            int highLevel = userBadgeOpt.map(UserBadge::getHighLevel).orElse(0);

            List<BadgeListRes.BadgeDetailRes> badgeDetailRes = badge.getBadgeDetails()
                    .stream().map((detail) -> new BadgeListRes.BadgeDetailRes(
                            detail.getBadgeDetailId(),
                            detail.getBadgeDetailName(),
                            detail.getBadgeDetailImg(),
                            detail.getBadgeDescription(),
                            detail.getBadgeGoal(),
                            detail.getBadgeLevel()
                    )).collect(Collectors.toList());

            return new BadgeListRes(
                    badge.getBadgeId(),
                    badge.getBadgeName(),
                    progress,
                    highLevel,
                    badgeDetailRes
            );
        }).collect(Collectors.toList());
    }

    // 유저 정보 요청
    public UserInfoRes getUserInfo(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->
                new NoSuchElementException("유저를 찾을 수 없습니다."));
        UserRecord userRecord = userRecordRepository.findByUser(user).orElseThrow(()->
                new NoSuchElementException("유저 기록을 찾을 수 없습니다."));
        UserInfoRes userInfoRes = UserInfoRes.builder()
                .userId(userId)
                .userName(user.getUserName())
                .userNickName(user.getUserNickname())
                .userProfile(user.getUserProfile())
                .exConDays(userRecord.getExConDays())
                .exDays(userRecord.getExDays())
                .roomWin(userRecord.getRoomWin())
                .build();

        return userInfoRes;
    }


}
