package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.dto.respond.UserInfoRes;
import com.red.yogaback.model.*;
import com.red.yogaback.repository.BadgeRepository;
import com.red.yogaback.repository.UserBadgeRepository;
import com.red.yogaback.repository.UserRecordRepository;
import com.red.yogaback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private User user;

    @BeforeEach
    void setUp(){
        Long userId = 1L;
        user = User.builder()
                .userId(userId)
                .userName("test")
                .userNickname("test")
                .build();
    }

    @Test
    void 배지목록요청_테스트() {

        // given
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

        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        when(userBadgeRepository.findByUser(user)).thenReturn(userBadges);
        when(badgeRepository.findAll()).thenReturn(badges);

        // when
        List<BadgeListRes> result = badgeService.getBadgeList(user.getUserId());

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

    @Test
    void 레벨1_배지부여_테스트(){
        // given
        Badge badge = Badge.builder()
                .badgeId(6L)
                .badgeName("요가의 달인")
                .build();
        when(badgeRepository.findById(6L)).thenReturn(Optional.of(badge));
        when(userBadgeRepository.findByUserAndBadge(user, badge)).thenReturn(null);

        // when
        badgeService.assignBadge(user, 6L, 1,1);

        // then
        ArgumentCaptor<UserBadge> captor = ArgumentCaptor.forClass(UserBadge.class);
        verify(userBadgeRepository).save(captor.capture());

        UserBadge savedBadge = captor.getValue();
        assertEquals(user, savedBadge.getUser());
        assertEquals(badge, savedBadge.getBadge());
        assertEquals(1, savedBadge.getHighLevel());
        assertEquals(1, savedBadge.getProgress());
    }

    @Test
    void 새로운_배지반환_테스트(){

        // given
        // 배지 설정
        Badge badge = Badge.builder()
                .badgeId(1L)
                .badgeName("Test Badge")
                .badgeMaxLv(3)
                .build();

        // 유저 배지 설정
        UserBadge userBadge = UserBadge.builder()
                .user(user)
                .badge(badge)
                .isNew(true)
                .progress(50)
                .highLevel(2)
                .createdAt(System.currentTimeMillis())
                .build();


        // 주어진 유저가 가지고 있는 배지 반환
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userBadgeRepository.findByUser(user)).thenReturn(Collections.singletonList(userBadge));


        // 배지 디테일 설정
        BadgeDetail badgeDetail = BadgeDetail.builder()
                .badgeLevel(2)
                .badgeDetailName("Test Badge Detail")
                .badgeDescription("Test Description")
                .badgeGoal(100)
                .badgeDetailImg("image.jpg")
                .badge(badge)
                .build();

        badge.setBadgeDetails(Collections.singletonList(badgeDetail));

        // when
        List<BadgeListRes> result = badgeService.getNewBadge(user.getUserId());

        // then
        assertEquals(1,result.size());
        assertEquals("Test Badge", result.get(0).getBadgeName());
        assertFalse(userBadge.isNew());

        verify(userBadgeRepository,times(1)).save(userBadge);

    }

}