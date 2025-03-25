package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Room")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId; // room_id

    // 방 생성자: Room은 반드시 하나의 User(생성자)에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User creator;

    private String password;        // password
    private Long roomMax;           // room_max
    private Long roomCount;         // room_count
    private String roomName;        // room_name

    @Column(columnDefinition = "TINYINT(1)")
    private boolean isPassword;     // is_password

    private Long createdAt;         // created_at
    private Long deletedAt;         // deleted_at
    private Long roomState;         // room_state

    // 한 방에는 여러 기록(RoomRecord)이 있을 수 있음
    @OneToMany(mappedBy = "room")
    private List<RoomRecord> roomRecords;

    // 한 방에 여러 RoomCoursePose가 포함될 수 있음
    @OneToMany(mappedBy = "room")
    private List<RoomCoursePose> roomCoursePoses;
}
