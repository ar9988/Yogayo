//WebSocket 연결 시 사용자의 ID, 방 ID, 닉네임, 프로필 정보를 불변 객체로 관리합니다.
package com.red.yogaback.websocket.service;

// 사용자 세션에 대한 정보를 담는 불변(Immutable) 데이터 클래스입니다.
// 필드: userId, roomId, userNickName, userProfile
public final class UserSession {
    // 사용자 고유 ID
    private final String userId;
    // 사용자가 속한 방 ID
    private final String roomId;
    // 사용자 닉네임
    private final String userNickName;
    // 사용자 프로필 정보(예: 프로필 이미지 URL)
    private final String userProfile;

    /**
     * 생성자: 모든 필드를 초기화하며, 객체 생성 후 변경 불가능합니다.
     *
     * 개선방향:
     *  - Builder 패턴 도입: 필드가 늘어날 경우 가독성을 위해 Builder 사용을 고려할 수 있습니다.
     *  - Lombok @Value 어노테이션 활용: 보일러플레이트 코드 제거 및 불변 클래스 선언을 간결하게 할 수 있습니다.
     */
    public UserSession(String userId, String roomId, String userNickName, String userProfile) {
        this.userId = userId;
        this.roomId = roomId;
        this.userNickName = userNickName;
        this.userProfile = userProfile;
    }

    // getter: userId 반환
    public String getUserId() {
        return userId;
    }

    // getter: roomId 반환
    public String getRoomId() {
        return roomId;
    }

    // getter: userNickName 반환
    public String getUserNickName() {
        return userNickName;
    }

    // getter: userProfile 반환
    public String getUserProfile() {
        return userProfile;
    }

    /*
     * 개선방향:
     * 1. toString(), equals(), hashCode() 메소드 재정의:
     *    - 디버깅 및 컬렉션 키 사용 시 편의성을 위해 메소드 오버라이드 고려.
     * 2. 입력값 검증 추가:
     *    - 생성자에서 null 또는 빈 문자열 검증을 수행하여 유효한 값만 허용할 수 있습니다.
     * 3. 세션 만료 시간 관리:
     *    - 필요 시 세션 생성 시 타임스탬프를 추가하고, 만료 로직을 포함할 수 있습니다.
     */
}
