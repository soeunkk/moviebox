package com.example.moviebox.jwt.service;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.JwtTokenProvider;
import com.example.moviebox.jwt.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final JwtTokenProvider jwtProvider;
	private final RedisService redisService;

	public TokenDto reissue(String accessToken, String refreshToken) {
		Long userId = validateTokenAndGetUserId(accessToken, refreshToken);
		return jwtProvider.generateAccessTokenAndRefreshToken(userId);
	}

	private Long validateTokenAndGetUserId(String accessToken, String refreshToken) {
		if (!jwtProvider.isValidateToken(accessToken)) {
			throw BusinessException.INVALID_ACCESS_TOKEN;
		}
		if (!jwtProvider.isValidateToken(refreshToken)) {
			throw BusinessException.INVALID_REFRESH_TOKEN;
		}

		Authentication authentication = jwtProvider.getAuthentication(accessToken);
		long userId = Long.parseLong(authentication.getName());
		String refreshTokenInDatabase = redisService.getRefreshTokenValue(userId);
		if (refreshTokenInDatabase == null || !refreshTokenInDatabase.equals(refreshToken)) {
			throw BusinessException.EXPIRED_REFRESH_TOKEN;
		}

		return Long.parseLong(authentication.getName());
	}
}
