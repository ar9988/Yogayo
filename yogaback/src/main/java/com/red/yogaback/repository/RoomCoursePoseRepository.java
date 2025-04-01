package com.red.yogaback.repository;

import com.red.yogaback.model.RoomCoursePose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomCoursePoseRepository extends JpaRepository<RoomCoursePose, Long> {
}
