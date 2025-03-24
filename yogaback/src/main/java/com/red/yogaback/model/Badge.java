package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Badge")
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
