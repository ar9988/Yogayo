package com.red.yogaback.repository;

import com.red.yogaback.model.User;
import com.red.yogaback.model.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    // 특정 userId를 가진 UserCourse 조회
    List<UserCourse> findByUserUserId(Long userId);
    List<UserCourse> findByUser(User user);

    int countByUser(User user);
}
