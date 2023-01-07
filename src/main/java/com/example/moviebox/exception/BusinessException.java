package com.example.moviebox.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
	public static final BusinessException EMAIL_AUTH_KEY_INVALID = new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일 인증 키가 올바르지 않습니다.");
	public static final BusinessException EMAIL_NOT_VERIFIED_YET = new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일 활성화 이후에 로그인을 해주세요.");
	public static final BusinessException ALREADY_COMPLETE_AUTHENTICATION = new BusinessException(ErrorCode.NOT_FOUND_RESOURCE, "이미 인증된 계정입니다.");
	public static final BusinessException USER_NOT_FOUND_BY_EMAIL = new BusinessException(ErrorCode.USER_NOT_FOUND, "가입 되지 않은 이메일입니다.");
	public static final BusinessException USER_NOT_FOUND_BY_USERID = new BusinessException(ErrorCode.USER_NOT_FOUND, "가입 되지 않은 회원 번호입니다.");
	public static final BusinessException USER_NOT_FOUND_BY_PASSWORD = new BusinessException(ErrorCode.USER_NOT_FOUND, "비밀번호가 올바르지 않습니다.");
	public static final BusinessException EMAIL_ALREADY_EXIST = new BusinessException(ErrorCode.CAN_NOT_CREATE_USER, "이미 존재하는 이메일입니다.");
	public static final BusinessException EMAIL_FORMAT_INVALID = new BusinessException(ErrorCode.CAN_NOT_CREATE_USER, "유효하지 않은 이메일 형식입니다.");
	public static final BusinessException INVALID_REFRESH_TOKEN = new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh Token 정보가 유효하지 않습니다.");
	public static final BusinessException INVALID_ACCESS_TOKEN = new BusinessException(ErrorCode.INVALID_TOKEN, "Access Token 정보가 유효하지 않습니다.");
	public static final BusinessException EXPIRED_REFRESH_TOKEN = new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh Token이 만료되었습니다.");

	private ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public HttpStatus getHttpStatus() {
		return errorCode.getHttpStatus();
	}
}
