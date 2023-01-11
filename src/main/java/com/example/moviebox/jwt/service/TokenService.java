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

	public TokenDto reissue(TokenCreation.Request request) {
		Long userId = validateRefreshTokenAndGetUserId(request);
		return jwtProvider.generateAccessTokenAndRefreshToken(userId);
	}

	private Long validateRefreshTokenAndGetUserId(TokenCreation.Request request) {
		if (!jwtProvider.isValidateToken(request.getRefreshToken())) {
			throw BusinessException.INVALID_REFRESH_TOKEN;
		}
		if (!jwtProvider.isValidateToken(request.getAccessToken())) {
			throw BusinessException.INVALID_ACCESS_TOKEN;
		}

		Authentication authentication = jwtProvider.getAuthentication(request.getAccessToken());

		String refreshToken = redisService.getTokenValues(Long.parseLong(authentication.getName()));
		if (refreshToken == null || !refreshToken.equals(request.getRefreshToken())) {
			throw BusinessException.EXPIRED_REFRESH_TOKEN;
		}

		return Long.parseLong(authentication.getName());
	}
}
