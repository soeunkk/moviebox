package com.example.moviebox.jwt.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenCreation {
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Request {
		@NotEmpty(message = "accessToken을 입력해주세요.")
		private String accessToken;
		@NotEmpty(message = "refreshToken을 입력해주세요.")
		private String refreshToken;
	}

	@Getter
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Response {
		private String grantType;
		private String accessToken;
		private String refreshToken;

		public static Response from(TokenDto tokenDto) {
			return Response.builder()
				.grantType(tokenDto.getGrantType())
				.accessToken(tokenDto.getAccessToken())
				.refreshToken(tokenDto.getRefreshToken())
				.build();
		}
	}
}
