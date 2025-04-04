package com.red.yogaback.websocket.dto;

public class RoomActionMessage {
    // 클라이언트가 보내는 원시 데이터를 담을 수 있도록 함.
    private Object payload;

    public RoomActionMessage() {}

    public RoomActionMessage(Object payload) {
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
