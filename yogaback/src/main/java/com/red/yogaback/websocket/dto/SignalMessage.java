package com.red.yogaback.websocket.dto;
//시그널 정보를 담는 DTO입니다.
public class SignalMessage {
    private String signal;

    public SignalMessage() {
    }

    public SignalMessage(String signal) {
        this.signal = signal;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }
}