package com.red.yogaback.auth;

import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserLoginId(String userLoginId);

    Boolean existsByUserLoginId(String userLoginId);

}