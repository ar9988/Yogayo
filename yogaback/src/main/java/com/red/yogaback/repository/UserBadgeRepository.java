package com.red.yogaback.repository;

import com.red.yogaback.model.User;
import com.red.yogaback.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);
}
