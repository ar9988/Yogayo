package com.red.yogaback.websocket.model;

public class IceCandidateMessage {
    private String candidate;

    public IceCandidateMessage() {
    }

    public IceCandidateMessage(String candidate) {
        this.candidate = candidate;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }
}