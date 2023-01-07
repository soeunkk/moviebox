package com.example.moviebox.jwt;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.jwt.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {
	private final UserDetailsService userDetailsService;
	private final RedisService redisService;

	private long accessTokenValidTime = 30 * 60 * 1000L;	// Access Token: 30분간 토큰 유효
	private long refreshTokenValidTime = 30 * 24 * 60 * 60 * 1000L;	// Refresh Token: 30일간 토큰 유효
	private Key secretKey;

	public JwtTokenProvider(final UserDetailsService userDetailsService,
		final RedisService redisService,
		@Value("${jwt.secret}") String secretKey) {
		this.userDetailsService = userDetailsService;
		this.redisService = redisService;

		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	// JWT Access Token + Refresh Token 생성
	public TokenDto generateAccessTokenAndRefreshToken(Long userId) {
		Date now = new Date();
		Date accessTokenExpiresIn = new Date(now.getTime() + accessTokenValidTime);
		String accessToken = Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(now)
			.setExpiration(accessTokenExpiresIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();

		Date refreshTokenExpiresIn = new Date(now.getTime() + refreshTokenValidTime);
		String refreshToken =  Jwts.builder()
			.setExpiration(refreshTokenExpiresIn)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
		redisService.setTokenValues(userId, refreshToken);

		return TokenDto.builder()
			.grantType("Bearer")
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	// JWT 토큰에서 인증 정보 조회
	public Authentication getAuthentication(String accessToken) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(accessToken));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	// JWT 토큰에서 회원 구별 정보 추출
	private String getUserId(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
	}

	// JWT 토큰 검증
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("Invalid JWT Token");
		} catch (ExpiredJwtException e) {
			log.info("Expired JWT Token");
		} catch (UnsupportedJwtException e) {
			log.info("Unsupported JWT Token");
		} catch (IllegalArgumentException e) {
			log.info("JWT claims string is empty.");
		}
		return false;
	}
}
