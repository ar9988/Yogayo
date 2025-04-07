package com.red.yogaback.websocket.dto;

// 클라이언트가 보내는 방(Room) 관련 액션 메시지를 담는 DTO입니다.
public class RoomActionMessage {
    // 클라이언트가 전송하는 원시 데이터(payload)를 담기 위한 필드
    private Object payload;

    // 기본 생성자: 프레임워크(예: Jackson)에서 역직렬화 시 필요할 수 있습니다.
    public RoomActionMessage() {}

    // payload 값을 초기화하는 생성자
    public RoomActionMessage(Object payload) {
        this.payload = payload;
    }

    // getter: payload 값을 반환
    public Object getPayload() {
        return payload;
    }

    // setter: payload 값을 설정
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    /*
     * 개선방향:
     *  - 제네릭 타입 활용: Object 대신 <T> 제네릭을 사용하면 타입 안정성을 높일 수 있습니다.
     *  - 불변 객체로 전환: 생성자 주입 후 setter를 제거하면 DTO가 변경 불가능해져 안정성이 향상됩니다.
     *  - Lombok 사용 고려: @Data 또는 @Getter/@Setter 어노테이션을 활용해 보일러플레이트 코드를 줄일 수 있습니다.
     *  - toString(), equals(), hashCode() 재정의: 로깅이나 컬렉션 사용 시 편의성을 위해 메소드 재정의를 고려할 수 있습니다.
     */
}
