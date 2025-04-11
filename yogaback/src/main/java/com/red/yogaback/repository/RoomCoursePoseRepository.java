package com.red.yogaback.repository;

import com.red.yogaback.model.RoomCoursePose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomCoursePoseRepository extends JpaRepository<RoomCoursePose, Long> {
    // roomId 기준으로 해당 RoomCoursePose들을 조회
    List<RoomCoursePose> findByRoom_RoomId(Long roomId);
}
