package com.red.yogaback.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 BAD_REQEUST
    BAD_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    EXIST_ID("이미 존재하는 아이디입니다.", HttpStatus.BAD_REQUEST),
    EXIST_REQUEST("존재하는 요청입니다.", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELDS("필수 필드가 누락되었거나 잘못된 입력입니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_DATA("요청 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_PARAMETERS("요청 파라미터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FORMAT("유효하지 않은 이미지 형식입니다.", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_USER_DATA("요청을 처리하기 위한 유저의 필수 데이터가 존재하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_FORM("유효하지 않은 토큰 형식입니다.", HttpStatus.BAD_REQUEST),

    // 401 UNAUTHORIZED
    UNAUTHORIZED("인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_ACCESS_TOKEN("액세스 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("리프레시 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_FCM_TOKEN("유효하지 않은 fcm 토큰입니다", HttpStatus.UNAUTHORIZED),
    INVALID_PI_TOKEN("유효하지 않은 pi 토큰입니다", HttpStatus.UNAUTHORIZED),

    // 403 FORBIDDEN
    ACCES_DENIED("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404 NOT_FOUND
    NOT_FOUND("요청한 자원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_EXIST_MEMBER("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND),
    NOT_EXIST_DATA("데이터가 존재하지 않습니다.",HttpStatus.OK),
    NO_MATCHING_FRIENDS("요청 조건에 맞는 친구를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NO_MATCHING_FRIEND_REQUESTS("친구 요청이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    AUTH_FAILURE("로그인에 실패하였습니다.", HttpStatus.NOT_FOUND),
    NOT_EXIST_DEVICE_GROUP("존재하지 않는 알림 그룹입니다.", HttpStatus.NOT_FOUND),
    NOT_EXIST_GROUP("존재하지 않는 그룹입니다.", HttpStatus.NOT_FOUND),

    // 409 CONFLICT
    CONFLICT("중복된 요청이 발생했습니다.", HttpStatus.CONFLICT),
    FRIEND_REQUEST_EXISTS("이미 친구 요청이 존재하거나 이미 친구 상태입니다.", HttpStatus.CONFLICT),
    DUPLICATE_RESOURCE("이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),

    // 500 SEVER_ERROR
    SERVER_ERROR("서버 내부 오류가 발생했습니다. 다시 시도해 주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
