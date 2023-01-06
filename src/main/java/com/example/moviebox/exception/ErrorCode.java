package com.example.moviebox.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "유효성 검증에 실패한 경우"),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-002", "요청한 HTTP 메소드 방식을 지원하지 않는 경우"),
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON-003", "지원하지 않는 Content Type으로 요청한 경우"),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMON-004", "접근이 거부된 경우"),
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACCOUNT-001", "사용자를 찾을 수 없는 경우"),
	CAN_NOT_CREATE_USER(HttpStatus.BAD_REQUEST, "ACCOUNT-002", "해당 정보로 계정을 생성할 수 없는 경우"),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-001", "비정상적인 토큰인 경우");

	private final HttpStatus httpStatus;
	private final String errorType;
	private final String description;
}
