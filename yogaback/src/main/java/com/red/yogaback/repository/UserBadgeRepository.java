package com.red.yogaback.repository;

import com.red.yogaback.model.Badge;
import com.red.yogaback.model.User;
import com.red.yogaback.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUser(User user);

    UserBadge findByUserAndBadge(User user, Badge badge);


}
