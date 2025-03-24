package com.red.yogaback.controller;

import com.red.yogaback.dto.request.CreateCourseRequest;
import com.red.yogaback.dto.respond.UserCourseRes;
import com.red.yogaback.service.UserCourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@Tag(name = "요가 포즈 관련 API", description = "요가 포즈 및 커스텀 코스 관리 기능")
@RequestMapping("/api/yoga")
@RequiredArgsConstructor
public class UserCourseController {

    private final UserCourseService userCourseService;

    /**
     * [POST] 커스텀 코스 생성
     */
    @PostMapping("/course")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request) {
        Long userCourseId = userCourseService.createCourse(request);
        // JSON 형태로 응답
        return ResponseEntity.ok(Collections.singletonMap("userCourseId", userCourseId));
    }

    /**
     * [GET] 현재 로그인한 사용자의 모든 커스텀 코스 조회
     */
    @GetMapping("/course")
    public ResponseEntity<List<UserCourseRes>> getCourses() {
        List<UserCourseRes> courses = userCourseService.getUserCourses();
        return ResponseEntity.ok(courses);
    }

    /**
     * [PUT] 특정 코스 수정
     */
    @PutMapping("/course/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Long courseId,
                                          @RequestBody CreateCourseRequest request) {
        userCourseService.updateCourse(courseId, request);
        return ResponseEntity.ok(Collections.singletonMap("updatedCourseId", courseId));
    }

    /**
     * [DELETE] 특정 코스 삭제
     */
    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long courseId) {
        userCourseService.deleteCourse(courseId);
        return ResponseEntity.ok(Collections.singletonMap("deletedCourseId", courseId));
    }
}
