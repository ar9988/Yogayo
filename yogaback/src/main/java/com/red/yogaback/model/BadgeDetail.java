package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BadgeDetail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Setter
public class BadgeDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeDetailId;

    private String badgeDetailName;

    private String badgeDetailImg;

    private String badgeDescription;

    private int badgeLevel;

    private int badgeGoal;


    @ManyToOne
    @JoinColumn(name = "badge_id")
    private Badge badge;

}


