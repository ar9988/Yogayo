package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "UserCoursePose")
@Getter
@Setter
public class UserCoursePose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coursePoseId; // course_pose_id

    // 해당 코스 포즈는 하나의 Pose와 연관됨
    @ManyToOne
    @JoinColumn(name = "pose_id", nullable = false)
    private Pose pose;

    // 해당 코스 포즈는 반드시 하나의 UserCourse에 속함
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private UserCourse userCourse;

    @Column(name = "user_order_Index")
    private Long userOrderIndex; // user_order_Index

    private Long createdAt; // created_at
}
