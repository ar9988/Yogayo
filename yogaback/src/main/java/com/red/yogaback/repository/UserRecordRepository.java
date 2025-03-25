package com.red.yogaback.repository;


import com.red.yogaback.model.User;
import com.red.yogaback.model.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRecordRepository extends JpaRepository<UserRecord,Long> {
    Optional<UserRecord> findByUser(User user);
}
