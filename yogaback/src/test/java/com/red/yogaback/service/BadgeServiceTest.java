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

    @Test
    void 배지목록요청_테스트() {

        // given
        Long userId = 1L;
        User user = User.builder()
                .userId(userId)
                .userName("test")
                .userNickname("test")
                .build();


        Badge badge1 = new Badge(1L,"testBadge",3,List.of(),List.of());
        Badge badge2 = new Badge(2L, "testBadge2", 3, List.of(), List.of());

        List<Badge> badges = List.of(badge1,badge2);


        UserBadge userBadge = new UserBadge(1L,user,badge1,false,50,2,System.currentTimeMillis());
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
}