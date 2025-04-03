package com.red.yogaback.websocket.dto;

public class RoomActionMessage {
    private String action; // 예: "enter", "ready", "signal", "iceCandidate", "heartbeat"
    private Object message; // 각 action에 따라 필요한 데이터가 들어감 (필요시 구체적인 DTO로 변환 가능)

    // getter, setter, 생성자 등
    public RoomActionMessage() {}

    public RoomActionMessage(String action, Object message) {
        this.action = action;
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }
}
