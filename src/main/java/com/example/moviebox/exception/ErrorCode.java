package com.example.moviebox.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode {
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "유효성 검증에 실패한 경우"),
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACCOUNT-001", "사용자를 찾을 수 없는 경우"),
	CAN_NOT_CREATE_USER(HttpStatus.BAD_REQUEST, "ACCOUNT-002", "해당 정보로 계정을 생성할 수 없는 경우");

	private final HttpStatus httpStatus;
	private final String code;
	private final String description;

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
