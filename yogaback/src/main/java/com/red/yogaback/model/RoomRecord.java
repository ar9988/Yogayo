package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RoomRecord")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RoomRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomRecordId; // room_record_id

    // 방 기록은 특정 사용자에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 방 기록은 반드시 하나의 Room에 속함
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private Integer totalRanking;   // total_ranking
    private Integer totalScore;     // total_score
    private Long createdAt;         // created_at

//    // 한 방 기록에는 여러 PoseRecord가 있을 수 있음
//    @OneToMany(mappedBy = "room")
//    private List<PoseRecord> poseRecords;
}
