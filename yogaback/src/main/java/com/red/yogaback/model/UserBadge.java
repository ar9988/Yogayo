package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "UserBadge")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBadgeId; // user_badge_id

    // 해당 뱃지 내역은 반드시 하나의 User에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 해당 뱃지 내역은 반드시 하나의 Badge에 속함
    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isNew; // is_new

    private Long createdAt; // created_at
}
