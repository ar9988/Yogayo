package com.red.yogaback.repository;

import com.red.yogaback.model.UserCoursePose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserCoursePoseRepository extends JpaRepository<UserCoursePose, Long> {

    // courseId를 이용해 UserCoursePose 일괄 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM UserCoursePose ucp WHERE ucp.userCourse.userCourseId = :courseId")
    void deleteAllByUserCourseUserCourseId(Long courseId);
}
