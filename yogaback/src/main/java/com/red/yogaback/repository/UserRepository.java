package com.red.yogaback.repository;

import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserLoginId(String userLoginId);
    Boolean existsByUserLoginId(String userLoginId);
}
