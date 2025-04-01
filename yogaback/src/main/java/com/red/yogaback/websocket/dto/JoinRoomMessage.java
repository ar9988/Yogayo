package com.red.yogaback.websocket.dto;
//사용자가 방에 참여할 때 전송하는 메시지로, 방 ID만 포함합니다.
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
