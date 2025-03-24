package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Badge")
@Getter
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId; // badge_id

    private String badgeName;      // badge_name
    private String badgeImg;       // badge_img
    private String badgeCondition; // badge_condition


    // 한 뱃지에 대한 사용자 뱃지 내역
    @OneToMany(mappedBy = "badge")
    private List<UserBadge> userBadges;

    public Badge(Long badgeId, String badgeName, String badgeImg, String badgeCondition) {
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.badgeImg = badgeImg;
        this.badgeCondition = badgeCondition;
    }
}
