package com.red.yogaback.websocket.model;

public class JoinRoomMessage {
    private String roomId;

    public JoinRoomMessage() {
    }

    public JoinRoomMessage(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
