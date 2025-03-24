package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.model.Badge;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserBadge;
import com.red.yogaback.repository.BadgeRepository;
import com.red.yogaback.repository.UserBadgeRepository;
import com.red.yogaback.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBadgeRepository userBadgeRepository;

    @InjectMocks
    private BadgeService badgeService;


    @Test
    void 배지목록요청_테스트() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .userId(userId)
                .userName("test user")
                .build();


        Badge badge = new Badge(1L,"테스트 배지1","test1.png","test1");
        Badge badge2 = new Badge(2L,"테스트 배지1","test1.png","test1");
        Badge badge3 = new Badge(3L,"테스트 배지1","test1.png","test1");

        UserBadge userBadge = new UserBadge(userId,user,badge,false,1000L);

        List<Badge> badges = List.of(badge, badge2, badge3);
        List<UserBadge> userBadges = List.of(userBadge);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(badgeRepository.findAll()).thenReturn(badges);
        when(userBadgeRepository.findByUser(user)).thenReturn(userBadges);

        // when
        List<BadgeListRes> result =  badgeService.getBadgeList(userId);

        // then
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0).isAchieved()).isTrue();
        assertThat(result.get(1).isAchieved()).isFalse();
        assertThat(result.get(2).isAchieved()).isFalse();

    }
}