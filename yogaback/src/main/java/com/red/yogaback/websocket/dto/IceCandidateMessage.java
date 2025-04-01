package com.red.yogaback.websocket.dto;
//ICE 후보 정보를 담는 DTO입니다.
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