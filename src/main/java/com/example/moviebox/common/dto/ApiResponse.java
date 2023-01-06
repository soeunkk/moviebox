package com.example.moviebox.common.dto;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {
	private static final ApiResponse<?> SUCCESS_RESPONSE = success(null);

	private boolean success;
	private T data;
	private ApiError error;

	public static ApiResponse<?> success() {
		return SUCCESS_RESPONSE;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static ApiResponse<?> error(ApiError error) {
		return new ApiResponse<>(false, null, error);
	}
}
