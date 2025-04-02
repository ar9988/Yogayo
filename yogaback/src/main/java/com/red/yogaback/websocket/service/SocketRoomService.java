package com.red.yogaback.websocket.service;

import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SocketRoomService {
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
    
    // 모든 방을 반환하는 메서드
    public Collection<Room> getAllRooms() {
        return rooms.values();
    }
}
