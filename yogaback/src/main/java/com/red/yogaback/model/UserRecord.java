package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "UserRecord")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userRecordId; // user_record_id

    // 1:1 관계 – UserRecord는 반드시 하나의 User와 연관됨
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Long exConDays; // 연속 운동 일수
    private Long roomWin;   // 방 우승 (예전 group_win에서 변경)
    private Long exDays;    // 총 운동 일수
    private Long createAt;  // 생성 시각 (millis)

    // 새롭게 추가된 요소들
    // <1>: 오늘자 운동 일자 (현재 운동 기록 날짜)
    private LocalDate currentExerciseDate;
    // <2>: 오늘이 아닌 과거 운동 일자 (바뀌기 전의 currentExerciseDate)
    private LocalDate previousExerciseDate;
}
