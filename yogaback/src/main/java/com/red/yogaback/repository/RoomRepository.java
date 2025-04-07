package com.red.yogaback.repository;

import com.red.yogaback.model.Room;
import com.red.yogaback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RoomRepository extends JpaRepository<Room,Long> {
    List<Room> findByRoomNameContaining(String name);

    Room findByUser(User user);
}
