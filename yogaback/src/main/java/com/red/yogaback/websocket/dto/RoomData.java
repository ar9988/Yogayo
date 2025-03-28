package com.red.yogaback.websocket.dto;

import lombok.Data;

@Data
public class RoomData {
    private String roomId;
    private int maxParticipants;
    private String yogaCourse;
}