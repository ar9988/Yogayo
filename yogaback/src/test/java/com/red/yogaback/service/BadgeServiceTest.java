package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.model.Badge;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserBadge;
import com.red.yogaback.model.UserRecord;
import com.red.yogaback.repository.BadgeRepository;
import com.red.yogaback.repository.UserBadgeRepository;
import com.red.yogaback.repository.UserRecordRepository;
import com.red.yogaback.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {


    @InjectMocks
    private BadgeService badgeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @Mock
    private UserRecordRepository userRecordRepository;

    @Test
    void 배지목록요청_테스트() {

        // given
        Long userId = 1L;
        User user = User.builder()
                .userId(userId)
                .userName("test")
                .userNickname("test")
                .build();


        Badge badge1 = Badge.builder()
                .badgeId(1L)
                .badgeName("testBadge")
                .badgeMaxLv(3)
                .badgeDetails(List.of())
                .userBadges(List.of())
                .build();


        Badge badge2 = Badge.builder()
                .badgeId(2L)
                .badgeName("testBadge2")
                .badgeMaxLv(3)
                .badgeDetails(List.of())
                .userBadges(List.of())
                .build();


        List<Badge> badges = List.of(badge1, badge2);

        UserBadge userBadge = UserBadge.builder()
                .userBadgeId(1L)
                .user(user)
                .badge(badge1)
                .isNew(false)
                .progress(50)
                .highLevel(2)
                .createdAt(System.currentTimeMillis())
                .build();

        List<UserBadge> userBadges = List.of(userBadge);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userBadgeRepository.findByUser(user)).thenReturn(userBadges);
        when(badgeRepository.findAll()).thenReturn(badges);

        // when
        List<BadgeListRes> result = badgeService.getBadgeList(userId);

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getBadgeId()).isEqualTo(1L);
        assertThat(result.get(0).getBadgeProgress()).isEqualTo(50);
        assertThat(result.get(0).getHighLevel()).isEqualTo(2);


        assertThat(result.get(1).getBadgeId()).isEqualTo(2L);
        assertThat(result.get(1).getBadgeProgress()).isEqualTo(0);
        assertThat(result.get(1).getHighLevel()).isEqualTo(0);
    }

    @Test
    void 유저정보조회_테스트() {

        //given
        User user = User.builder()
                .userId(1L)
                .userName("test")
                .userNickname("test")
                .build();

        UserRecord userRecord = UserRecord.builder()
                .user(user)
                .userRecordId(1L)
                .exConDays(3L)
                .exDays(5L)
                .roomWin(2L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRecordRepository.findByUser(user)).thenReturn(Optional.of(userRecord));

        // when
        UserInfoRes result = badgeService.getUserInfo(1L);


        // then
        assertThat(result.getExConDays()).isEqualTo(3L);
        assertThat(result.getExDays()).isEqualTo(5L);
        assertThat(result.getRoomWin()).isEqualTo(2L);
    }

}