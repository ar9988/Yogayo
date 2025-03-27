package com.red.yogaback.repository;


import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoseRecordRepository extends JpaRepository<PoseRecord,Long> {
    List<PoseRecord> findByUser(User user);

    // 유저의 최대 정확도 조회
    @Query("SELECT MAX(p.accuracy) FROM PoseRecord p WHERE p.user = :user")
    Optional<Integer> findMaxAccuracyByUser(@Param("user") User user);

    // 유저의 최대 포즈 유지 시간 조회
    @Query("SELECT MAX(p.poseTime) FROM PoseRecord p WHERE p.user = :user")
    Optional<Integer> findMaxPoseTimeByUser(@Param("user") User user);
}
