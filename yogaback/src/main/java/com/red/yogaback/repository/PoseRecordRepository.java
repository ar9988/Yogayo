package com.red.yogaback.repository;


import com.red.yogaback.model.PoseRecord;
import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoseRecordRepository extends JpaRepository<PoseRecord,Long> {
    List<PoseRecord> findByUser(User user);

    boolean existsByUserAndAccuracyGreaterThanEqual(User user, int accuracy);

    boolean existsByUserAndPoseTimeGreaterThanEqual(User user, int poseTime);
}
