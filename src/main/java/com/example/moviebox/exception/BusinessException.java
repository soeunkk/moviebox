package com.example.moviebox.exception;

public class BusinessException extends RuntimeException {
	public static final BusinessException EMAIL_AUTH_KEY_INVALID = new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일 인증 키가 올바르지 않습니다.");
	public static final BusinessException USER_NOT_FOUND_BY_EMAIL = new BusinessException(ErrorCode.USER_NOT_FOUND, "가입 되지 않은 이메일입니다.");
	public static final BusinessException EMAIL_ALREADY_EXIST = new BusinessException(ErrorCode.CAN_NOT_CREATE_USER, "이미 존재하는 이메일입니다.");
	public static final BusinessException EMAIL_FORMAT_INVALID = new BusinessException(ErrorCode.CAN_NOT_CREATE_USER, "유효하지 않은 이메일 형식입니다.");

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
