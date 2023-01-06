package com.example.moviebox.jwt.service;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.JwtTokenProvider;
import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.jwt.dto.TokenDto.Request;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final JwtTokenProvider jwtProvider;
	private final RedisService redisService;

	@Transactional
	public TokenDto.Response reissue(TokenDto.Request request) {
		Long userId = validateRefreshTokenAndGetUserId(request);
		return jwtProvider.generateAccessTokenAndRefreshToken(userId);
	}

	private Long validateRefreshTokenAndGetUserId(Request request) {
		if (!jwtProvider.validateToken(request.getRefreshToken())) {
			throw BusinessException.INVALID_REFRESH_TOKEN;
		}
		if (!jwtProvider.validateToken(request.getAccessToken())) {
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
