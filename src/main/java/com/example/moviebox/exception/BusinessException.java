package com.example.moviebox.exception;

public class BusinessException extends RuntimeException {
	public static final BusinessException USER_NOT_FOUND_BY_EMAIL = new BusinessException(ErrorCode.USER_NOT_FOUND, "가입 되지 않은 이메일입니다.");

	private ErrorCode errorCode;
	private String message;

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
