package com.example.moviebox.jwt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.configuration.security.SecurityUser;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.*;
import com.example.moviebox.jwt.dto.TokenCreation;
import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.user.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
	@Mock
	private JwtTokenProvider jwtProvider;
	@Mock
	private RedisService redisService;

	@InjectMocks
	private TokenService tokenService;

	@Test
	public void testReissue() {
		given(jwtProvider.validateToken(anyString()))
			.willReturn(true);
		SecurityUser userDetails = new SecurityUser(User.builder()
			.id(1L)
			.password("pw")
			.role(Role.ADMIN)
			.build());
		given(jwtProvider.getAuthentication(anyString()))
			.willReturn(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));
		given(redisService.getTokenValues(anyLong()))
			.willReturn("refresh-token1");
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(TokenDto.builder()
				.grantType("Bearer")
				.accessToken("access-token2")
				.refreshToken("refresh-token2")
				.build());

		TokenDto tokenResponse = tokenService.reissue(new TokenCreation.Request("access-token1", "refresh-token1"));

		assertEquals("Bearer", tokenResponse.getGrantType());
		assertEquals("access-token2", tokenResponse.getAccessToken());
		assertEquals("refresh-token2", tokenResponse.getRefreshToken());
	}

	@Test
	public void testReissueByWrongRefreshToken() {
		given(jwtProvider.validateToken("refresh-token"))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> tokenService.reissue(new TokenCreation.Request("access-token", "refresh-token")));

		assertEquals(BusinessException.INVALID_REFRESH_TOKEN, exception);
	}

	@Test
	public void testReissueByWrongAccessToken() {
		given(jwtProvider.validateToken(anyString()))
			.willReturn(true);
		given(jwtProvider.validateToken("access-token"))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> tokenService.reissue(new TokenCreation.Request("access-token", "refresh-token")));

		assertEquals(BusinessException.INVALID_ACCESS_TOKEN, exception);
	}

	@Test
	public void testReissueByPreviousRefreshToken() {
		given(jwtProvider.validateToken(anyString()))
			.willReturn(true);
		SecurityUser userDetails = new SecurityUser(User.builder()
			.id(1L)
			.password("pw")
			.role(Role.ADMIN)
			.build());
		given(jwtProvider.getAuthentication(anyString()))
			.willReturn(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));
		given(redisService.getTokenValues(anyLong()))
			.willReturn("new-refresh-token");

		BusinessException exception = assertThrows(BusinessException.class,
			() -> tokenService.reissue(new TokenCreation.Request("access-token", "previous-refresh-token")));

		assertEquals(BusinessException.EXPIRED_REFRESH_TOKEN, exception);
	}

	@Test
	public void testReissueByExpiredRefreshToken() {
		given(jwtProvider.validateToken(anyString()))
			.willReturn(true);
		SecurityUser userDetails = new SecurityUser(User.builder()
			.id(1L)
			.password("pw")
			.role(Role.ADMIN)
			.build());
		given(jwtProvider.getAuthentication(anyString()))
			.willReturn(new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities()));
		given(redisService.getTokenValues(anyLong()))
			.willReturn(null);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> tokenService.reissue(new TokenCreation.Request("access-token", "refresh-token")));

		assertEquals(BusinessException.EXPIRED_REFRESH_TOKEN, exception);
	}
}
