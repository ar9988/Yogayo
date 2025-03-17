package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "UserRecord")
@Getter
@Setter
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userRecordId; // user_record_id

    // 1:1 관계 – UserRecord는 반드시 하나의 User와 연관됨
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Long exConDays; // ex_con_days
    private Long roomWin;   // room_win (예전 group_win에서 변경)
    private Long exDays;    // ex_days
    private Long createAt;  // create_at
}
