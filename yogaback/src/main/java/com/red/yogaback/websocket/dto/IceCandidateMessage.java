package com.red.yogaback.websocket.dto;
// ICE 후보 정보를 담는 DTO (Data Transfer Object)입니다.

public class IceCandidateMessage {
    // ICE 후보(candidate) 값을 저장하는 필드
    private String candidate;

    // 기본 생성자: 프레임워크나 라이브러리에서 객체 생성 시 필요할 수 있음
    public IceCandidateMessage() {
    }

    // 후보 값을 초기화하는 생성자
    public IceCandidateMessage(String candidate) {
        this.candidate = candidate;
    }

    // getter: candidate 값을 반환
    public String getCandidate() {
        return candidate;
    }

    // setter: candidate 값을 설정
    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    /*
     * 개선방향:
     *  - 불변 객체로 전환: DTO가 단순히 데이터를 전달하기 위한 용도라면,
     *    생성자를 통해 값 설정 후 setter 제거하여 불변 객체로 만들면 안정성이 향상됩니다.
     *  - Lombok 라이브러리 활용: @Data, @Getter, @Setter 등의 어노테이션을 사용하여 보일러플레이트 코드를 줄일 수 있습니다.
     *  - equals, hashCode, toString 메소드 재정의: 객체 비교나 로깅/디버깅 용도로 재정의하는 것을 고려할 수 있습니다.
     */
}
