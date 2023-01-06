package com.example.moviebox.common.dto;

import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.exception.ErrorCode;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiError {
	private String type;
	private String title;
	private int status;
	private String detail;
	private String instance;

	@Builder
	public ApiError(ErrorCode errorCode, String errorMessage, String errorOccurrencePath) {
		type = errorCode.getErrorType();
		title = errorCode.getDescription();
		status = errorCode.getHttpStatus().value();
		detail = errorMessage;
		instance = errorOccurrencePath;
	}

	public static ApiError from(BusinessException businessException, String errorOccurrencePath) {
		return new ApiError(businessException.getErrorCode(), businessException.getMessage(), errorOccurrencePath);
	}
}
