package com.red.yogaback.service;

import com.red.yogaback.dto.respond.BadgeListRes;
import com.red.yogaback.model.Badge;
import com.red.yogaback.model.UserBadge;
import com.red.yogaback.repository.BadgeRepository;
import com.red.yogaback.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    // 뱃지 목록 요청
    public List<BadgeListRes> getBadgeList(Long userId) {

        List<Badge> badges = badgeRepository.findAll();
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);
        ArrayList<BadgeListRes> result = new ArrayList<>();

        for (UserBadge userBadge : userBadges) {
            Badge badge = userBadge.getBadge();
            BadgeListRes badgeListRes = new BadgeListRes(badge.getBadgeId(), badge.getBadgeName(), badge.getBadgeImg(), badge.getBadgeCondition(), true);
            result.add(badgeListRes);
        }

        for (Badge badge : badges) {
            for (UserBadge userBadge : userBadges) {
                if (badge == userBadge.getBadge()) {
                    break;
                }
            }
            BadgeListRes badgeListRes = new BadgeListRes(badge.getBadgeId(), badge.getBadgeName(), badge.getBadgeImg(), badge.getBadgeCondition(), false);
            result.add(badgeListRes);
        }

        return result;
    }
}
