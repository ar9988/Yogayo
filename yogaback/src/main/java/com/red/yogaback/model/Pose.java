package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Pose")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poseId; // pose_id

    private String poseName;        // pose_name
    private String poseDescription; // pose_description
    private String poseImg;         // pose_img
    private Long poseLevel;         // pose_level
    private String poseVideo;       // pose_video
    private String poseAnimation;   // pose_animation

    // 자기 참조: 상위 자세 (없을 수도 있음)
    @ManyToOne
    @JoinColumn(name = "set_pose_id")
    private Pose setPose;

    // 하위 자세들 (자기 참조)
    @OneToMany(mappedBy = "setPose")
    private List<Pose> childPoses;

    // 한 자세가 여러 PoseRecord에 기록될 수 있음
    @OneToMany(mappedBy = "pose")
    private List<PoseRecord> poseRecords;

    // 한 자세가 여러 RoomCoursePose에 등장할 수 있음
    @OneToMany(mappedBy = "pose")
    private List<RoomCoursePose> roomCoursePoses;

    // 한 자세가 여러 UserCoursePose에 등장할 수 있음
    @OneToMany(mappedBy = "pose")
    private List<UserCoursePose> userCoursePoses;
}
