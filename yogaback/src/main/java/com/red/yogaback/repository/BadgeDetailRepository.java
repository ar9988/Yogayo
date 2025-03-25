package com.red.yogaback.repository;

import com.red.yogaback.model.BadgeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgeDetailRepository extends JpaRepository<BadgeDetail, Long> {


}
