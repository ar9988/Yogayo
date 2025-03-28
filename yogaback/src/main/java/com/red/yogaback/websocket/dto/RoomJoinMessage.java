package com.red.yogaback.websocket.dto;

import lombok.Data;

@Data
public class RoomJoinMessage {
    private String roomId;
    private String userId;
}