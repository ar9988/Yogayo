package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Room")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId; // room_id

    // 방 생성자: Room은 반드시 하나의 User(생성자)에 속함

    private String password;        // password
    private int roomMax;           // room_max
    private int roomCount;         // room_count
    private String roomName;        // room_name

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean hasPassword;     // is_password

    private Long createdAt;         // created_at
    private Long deletedAt;         // deleted_at
    private Long roomState;         // room_state

    @OneToMany(mappedBy = "room")
    private List<User> users;

    // 한 방에는 여러 기록(RoomRecord)이 있을 수 있음
    @OneToMany(mappedBy = "room")
    private List<RoomRecord> roomRecords;

    // 한 방에 여러 RoomCoursePose가 포함될 수 있음
    @OneToMany(mappedBy = "room")
    private List<RoomCoursePose> roomCoursePoses;

    private Long creatorId;
}
