package com.example.moviebox.exception;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
	private String type;
	private String title;
	private int status;
	private String detail;
	private String instance;

	@Builder
	public ErrorResponse(ErrorCode errorCode, String errorMessage, String errorOccurrencePath) {
		type = errorCode.getErrorType();
		title = errorCode.getDescription();
		status = errorCode.getHttpStatus().value();
		detail = errorMessage;
		instance = errorOccurrencePath;
	}

	public static ErrorResponse from(BusinessException businessException, String errorOccurrencePath) {
		return new ErrorResponse(businessException.getErrorCode(), businessException.getMessage(), errorOccurrencePath);
	}
}
