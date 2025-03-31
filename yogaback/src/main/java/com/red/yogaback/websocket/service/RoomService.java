package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    // roomId -> Room
    private Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void createRoom(String roomId) {
        rooms.putIfAbsent(roomId, new Room(roomId));
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }
}