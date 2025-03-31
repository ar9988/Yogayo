package com.red.yogaback.websocket.model;

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