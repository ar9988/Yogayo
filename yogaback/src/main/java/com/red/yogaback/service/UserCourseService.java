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

import java.util.ArrayList;
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
     * - 생성 후 UserCourseRes DTO로 반환
     */
    @Transactional
    public UserCourseRes createCourse(CreateCourseRequest request) {
        // 1) JWT 인증 정보에서 userId를 꺼냄
        Long userId = SecurityUtil.getCurrentMemberId();

        // 2) userId로 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3) UserCourse 엔티티 생성 & 저장
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourseName(request.getCourseName());
        // tutorial 값이 null이면 false로 처리
        userCourse.setTutorial(request.getTutorial() != null ? request.getTutorial() : false);
        userCourse.setCreatedAt(System.currentTimeMillis());
        userCourseRepository.save(userCourse);

        // 4) UserCoursePose 생성/저장
        List<UserCoursePose> poseList = new ArrayList<>();
        for (CreateCourseRequest.PoseInfo poseInfo : request.getPoses()) {
            Pose pose = poseRepository.findById(poseInfo.getPoseId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POSE_NOT_FOUND));

            UserCoursePose userCoursePose = new UserCoursePose();
            userCoursePose.setUserCourse(userCourse);
            userCoursePose.setPose(pose);
            userCoursePose.setUserOrderIndex(poseInfo.getUserOrderIndex());
            userCoursePose.setCreatedAt(System.currentTimeMillis());
            userCoursePoseRepository.save(userCoursePose);

            poseList.add(userCoursePose);
        }

        // 엔티티 객체에도 poseList를 넣어줘야, fromEntity()에서 접근 가능
        userCourse.setUserCoursePoses(poseList);

        // 5) 생성된 코스 정보를 DTO로 변환하여 반환
        return UserCourseRes.fromEntity(userCourse);
    }

    /**
     * [GET] 현재 로그인한 사용자의 모든 커스텀 코스 조회
     */
    @Transactional(readOnly = true)
    public List<UserCourseRes> getUserCourses() {
        Long userId = SecurityUtil.getCurrentMemberId();

        List<UserCourse> userCourses = userCourseRepository.findByUserUserId(userId);
        return userCourses.stream()
                .map(UserCourseRes::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * [PUT] 특정 코스 수정
     * - 수정 후 UserCourseRes DTO로 반환
     */
    @Transactional
    public UserCourseRes updateCourse(Long courseId, CreateCourseRequest request) {
        Long userId = SecurityUtil.getCurrentMemberId();

        UserCourse userCourse = userCourseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!userCourse.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCES_DENIED);
        }

        // 코스명 변경
        userCourse.setCourseName(request.getCourseName());
        userCourse.setModifyAt(System.currentTimeMillis());

        // tutorial 갱신 (null이면 기존 값 유지)
        if (request.getTutorial() != null) {
            userCourse.setTutorial(request.getTutorial());
        }

        // 기존 포즈들 삭제 -> 새로 추가
        userCoursePoseRepository.deleteAllByUserCourseUserCourseId(courseId);

        List<UserCoursePose> poseList = new ArrayList<>();
        for (CreateCourseRequest.PoseInfo poseInfo : request.getPoses()) {
            Pose pose = poseRepository.findById(poseInfo.getPoseId())
                    .orElseThrow(() -> new CustomException(ErrorCode.POSE_NOT_FOUND));

            UserCoursePose userCoursePose = new UserCoursePose();
            userCoursePose.setUserCourse(userCourse);
            userCoursePose.setPose(pose);
            userCoursePose.setUserOrderIndex(poseInfo.getUserOrderIndex());
            userCoursePose.setCreatedAt(System.currentTimeMillis());
            userCoursePoseRepository.save(userCoursePose);

            poseList.add(userCoursePose);
        }

        userCourse.setUserCoursePoses(poseList);

        // 수정된 코스 정보를 DTO로 변환
        return UserCourseRes.fromEntity(userCourse);
    }

    /**
     * [DELETE] 특정 코스 삭제
     * - 성공 시 true 반환
     */
    @Transactional
    public boolean deleteCourse(Long courseId) {
        Long userId = SecurityUtil.getCurrentMemberId();

        UserCourse userCourse = userCourseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!userCourse.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCES_DENIED);
        }

        // 연관된 UserCoursePose 삭제
        userCoursePoseRepository.deleteAllByUserCourseUserCourseId(courseId);

        // UserCourse 삭제
        userCourseRepository.delete(userCourse);
        return true;
    }
}
