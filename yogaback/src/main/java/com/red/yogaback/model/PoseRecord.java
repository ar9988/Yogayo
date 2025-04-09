package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

@Entity
@Table(name = "PoseRecord")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자 (protected)
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 빌더가 사용할 전체 생성자 (private)
@Builder  // 빌더 패턴 활성화
public class PoseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poseRecordId; // pose_record_id

    // 자세 기록은 특정 User에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 변경: 기존 RoomRecord 대신 Room과 직접 연결 (nullable 처리하여 solo 기록 허용)
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;

    // 자세 기록은 반드시 하나의 Pose와 연관됨
    @ManyToOne
    @JoinColumn(name = "pose_id", nullable = false)
    private Pose pose;

    private Long createdAt;  // created_at
    private Float accuracy;  // accuracy
    private Integer ranking; // ranking (Nullable allowed for solo records)
    private Float poseTime;  // pose_time (예약어 회피를 위해 "poseTime"으로 사용)
    private String recordImg; // record_img
}
