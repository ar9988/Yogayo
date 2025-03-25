package com.red.yogaback.repository;

import com.red.yogaback.model.RoomRecord;
import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRecordRepository extends JpaRepository<RoomRecord,Long> {
    List<RoomRecord> findByUser(User user);

    int countByUser(User user);
}
