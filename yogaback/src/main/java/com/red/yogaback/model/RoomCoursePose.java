package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RoomCoursePose")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomCoursePose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomCoursePoseId; // course_pose_id

    // 해당 코스 포즈는 하나의 Pose와 연관됨
    @ManyToOne
    @JoinColumn(name = "pose_id", nullable = false)
    private Pose pose;

    // 해당 코스 포즈는 반드시 하나의 Room에 속함
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "room_order_Index")
    private Long roomOrderIndex; // room_order_Index

    private Long createdAt; // created_at
}
