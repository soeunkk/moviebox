package com.example.moviebox.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ACCOUNT-001", "사용자를 찾을 수 없는 경우"),

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
