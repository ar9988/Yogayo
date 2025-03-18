package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PoseRecord")
@Getter
@Setter
public class PoseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poseRecordId; // pose_record_id

    // 자세 기록은 특정 User에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 자세 기록은 특정 RoomRecord에 속함
    @ManyToOne
    @JoinColumn(name = "room_record_id", nullable = false)
    private RoomRecord roomRecord;

    // 자세 기록은 반드시 하나의 Pose와 연관됨
    @ManyToOne
    @JoinColumn(name = "pose_id", nullable = false)
    private Pose pose;

    private Long createdAt;  // created_at
    private Float accuracy;  // accuracy
    private Integer ranking; // ranking
    private Float poseTime;  // pose_time (예약어 회피를 위해 "poseTime"으로 사용)
    private String recordImg; // record_img
}
