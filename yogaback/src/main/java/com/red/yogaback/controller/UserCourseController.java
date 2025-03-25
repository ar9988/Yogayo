package com.red.yogaback.controller;

import com.red.yogaback.dto.request.CreateCourseRequest;
import com.red.yogaback.dto.respond.UserCourseRes;
import com.red.yogaback.service.UserCourseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "요가 포즈 관련 API", description = "요가 포즈 및 커스텀 코스 관리 기능")
@RequestMapping("/api/yoga")
@RequiredArgsConstructor
public class UserCourseController {

    private final UserCourseService userCourseService;

    /**
     * [POST] 커스텀 코스 생성
     * - 생성된 코스 정보를 JSON으로 반환
     */
    @PostMapping("/course")
    public ResponseEntity<UserCourseRes> createCourse(@RequestBody CreateCourseRequest request) {
        // createCourse가 UserCourseRes를 반환하도록 변경
        UserCourseRes createdCourse = userCourseService.createCourse(request);
        return ResponseEntity.ok(createdCourse);
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
     * - 수정된 코스 정보를 JSON으로 반환
     */
    @PutMapping("/course/{courseId}")
    public ResponseEntity<UserCourseRes> updateCourse(@PathVariable Long courseId,
                                                      @RequestBody CreateCourseRequest request) {
        UserCourseRes updatedCourse = userCourseService.updateCourse(courseId, request);
        return ResponseEntity.ok(updatedCourse);
    }

    /**
     * [DELETE] 특정 코스 삭제
     * - true/false만 반환
     */
    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Boolean> deleteCourse(@PathVariable Long courseId) {
        boolean result = userCourseService.deleteCourse(courseId);
        return ResponseEntity.ok(result);
    }
}
