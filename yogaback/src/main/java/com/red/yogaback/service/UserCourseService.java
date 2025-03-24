package com.red.yogaback.service;

import com.red.yogaback.constant.ErrorCode;
import com.red.yogaback.dto.request.CreateCourseRequest;
import com.red.yogaback.dto.respond.UserCourseRes;
import com.red.yogaback.error.CustomException;
import com.red.yogaback.model.*;
import com.red.yogaback.repository.*;
import com.red.yogaback.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCourseService {

    private final UserRepository userRepository;
    private final PoseRepository poseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserCoursePoseRepository userCoursePoseRepository;

    /**
     * [POST] 커스텀 코스 생성
     */
    @Transactional
    public Long createCourse(CreateCourseRequest request) {
        // 1) JWT 인증 정보에서 userId를 꺼냄
        Long userId = SecurityUtil.getCurrentMemberId();

        // 2) userId로 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3) UserCourse 엔티티 생성 & 저장
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourseName(request.getCourseName());
        userCourse.setTutorial(false); // tutorial 필드 필요 없으면 false
        userCourse.setCreatedAt(System.currentTimeMillis());
        userCourseRepository.save(userCourse);

        // 4) 요청 바디의 poses 배열을 순회하며, UserCoursePose 엔티티 생성/저장
        for (CreateCourseRequest.PoseInfo poseInfo : request.getPoses()) {
            Pose pose = poseRepository.findById(poseInfo.getPoseId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POSE_NOT_FOUND));

            UserCoursePose userCoursePose = new UserCoursePose();
            userCoursePose.setUserCourse(userCourse);
            userCoursePose.setPose(pose);
            userCoursePose.setUserOrderIndex(poseInfo.getUserOrderIndex());
            userCoursePose.setCreatedAt(System.currentTimeMillis());

            userCoursePoseRepository.save(userCoursePose);
        }

        return userCourse.getUserCourseId();
    }

    /**
     * [GET] 현재 로그인한 사용자의 모든 커스텀 코스 조회
     */
    @Transactional(readOnly = true)
    public List<UserCourseRes> getUserCourses() {
        Long userId = SecurityUtil.getCurrentMemberId();

        // userId가 일치하는 코스만 가져오기
        List<UserCourse> userCourses = userCourseRepository.findByUserUserId(userId);

        // Entity -> DTO 변환
        return userCourses.stream()
                .map(UserCourseRes::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * [PUT] 특정 코스 수정
     * - courseName 변경
     * - poses 재설정(기존 UserCoursePose 모두 삭제 후 새로 추가)
     */
    @Transactional
    public void updateCourse(Long courseId, CreateCourseRequest request) {
        Long userId = SecurityUtil.getCurrentMemberId();

        // 1) 해당 코스 존재 여부 확인
        UserCourse userCourse = userCourseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 본인 코스인지 확인
        if (!userCourse.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCES_DENIED);
        }

        // 3) 코스명 변경
        userCourse.setCourseName(request.getCourseName());
        userCourse.setModifyAt(System.currentTimeMillis());

        // 4) 기존 포즈들 삭제 -> 새로 추가
        userCoursePoseRepository.deleteAllByUserCourseUserCourseId(courseId);

        for (CreateCourseRequest.PoseInfo poseInfo : request.getPoses()) {
            Pose pose = poseRepository.findById(poseInfo.getPoseId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POSE_NOT_FOUND));

            UserCoursePose userCoursePose = new UserCoursePose();
            userCoursePose.setUserCourse(userCourse);
            userCoursePose.setPose(pose);
            userCoursePose.setUserOrderIndex(poseInfo.getUserOrderIndex());
            userCoursePose.setCreatedAt(System.currentTimeMillis());

            userCoursePoseRepository.save(userCoursePose);
        }
    }

    /**
     * [DELETE] 특정 코스 삭제
     */
    @Transactional
    public void deleteCourse(Long courseId) {
        Long userId = SecurityUtil.getCurrentMemberId();

        UserCourse userCourse = userCourseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 본인 코스가 아니면 삭제 불가
        if (!userCourse.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCES_DENIED);
        }

        // 연관된 UserCoursePose 삭제 (또는 Cascade 설정 사용)
        userCoursePoseRepository.deleteAllByUserCourseUserCourseId(courseId);

        // 최종적으로 UserCourse 삭제
        userCourseRepository.delete(userCourse);
    }
}
