package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "UserCourse")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userCourseId; // course_id

    // 해당 코스는 반드시 하나의 User에 속함
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String courseName; // course_name

    @Column(columnDefinition = "TINYINT(1)")
    private boolean tutorial; // tutorial

    private Long deletedAt; // deleted_at
    private Long createdAt; // created_at
    private Long modifyAt;  // modify_at

    // 한 코스는 여러 UserCoursePose를 가질 수 있음
    @OneToMany(mappedBy = "userCourse")
    private List<UserCoursePose> userCoursePoses;
}
