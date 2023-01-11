package com.example.moviebox.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenDto {
	private String grantType;
	private String accessToken;
	private String refreshToken;
}
