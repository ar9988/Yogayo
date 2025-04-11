package com.red.yogaback.repository;

import com.red.yogaback.model.Pose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoseRepository extends JpaRepository<Pose, Long> {
    // 기본 CRUD 메서드는 JpaRepository에서 제공됨
    // 필요한 경우 추가 쿼리 메서드를 여기에 정의할 수 있음
}
