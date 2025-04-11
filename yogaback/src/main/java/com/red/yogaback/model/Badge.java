package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Badge")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId; // badge_id

    private String badgeName;

    private int badgeMaxLv;

    @OneToMany(mappedBy = "badge", fetch = FetchType.LAZY)
    private List<BadgeDetail> badgeDetails;

    @OneToMany(mappedBy = "badge", fetch = FetchType.LAZY)
    private List<UserBadge> userBadges;



}
