package com.example.moviebox.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.configuration.security.SecurityUser;
import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.user.domain.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class JwtTokenProviderTest {
	@Mock
	private UserDetailsService userDetailsService;
	@Mock
	private RedisService redisService;	// redis는 Git Action에서 사용할 수 없으므로 Mock
	@Value("${jwt.secret}")
	private String secretKeyString;
	private JwtTokenProvider jwtProvider;
	private Key secretKey;

	@BeforeEach
	private void initEach() {
		jwtProvider = new JwtTokenProvider(userDetailsService, redisService, secretKeyString);

		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@DisplayName("관리자 권한의 토큰이 올바르게 생성된다.")
	@Test
	public void testGenerateAccessTokenAndRefreshToken() {
		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);

		assertNotNull(tokenDto.getGrantType());
		assertNotNull(tokenDto.getAccessToken());
		assertNotNull(tokenDto.getRefreshToken());
	}

	@DisplayName("토큰에서 올바른 인증 정보를 조회한다.")
	@Test
	public void testGetAuthentication() {
		given(userDetailsService.loadUserByUsername(anyString()))
			.willReturn(new SecurityUser(User.builder()
				.id(1L)
				.password("pw")
				.role(Role.ADMIN)
				.build()));

		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = tokenDto.getAccessToken();

		Authentication authentication = jwtProvider.getAuthentication(accessToken);

		assertEquals("[ROLE_ADMIN]", authentication.getAuthorities().toString());
	}

	@DisplayName("유효하지 않은 토큰 형식으로 인증 정보를 조회할 경우 예외를 발생시킨다.")
	@Test
	public void testGetAuthenticationByInvalidToken() {
		assertThrows(MalformedJwtException.class,
			() -> jwtProvider.getAuthentication("invalidToken"));
	}

	@DisplayName("만료된 토큰으로 인증 정보를 조회할 경우 예외를 발생시킨다.")
	@Test
	public void testGetAuthenticationByExpiredToken() {
		Date expirationDate = new Date(new Date().getTime() - 1);
		final String expiredToken = createAdminToken(expirationDate, secretKey);

		assertThrows(ExpiredJwtException.class,
			() -> jwtProvider.getAuthentication(expiredToken));
	}

	@DisplayName("시크릿 키가 틀린 토큰 정보로 인증 정보를 조회할 경우 예외를 발생시킨다.")
	@Test
	public void testGetAuthenticationByWrongSecretKeyToken() {
		Date expirationDate = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000L);
		final String invalidSecretToken = createAdminToken(expirationDate, createInvalidSecretKey());

		assertThrows(SignatureException.class,
			() -> jwtProvider.getAuthentication(invalidSecretToken));
	}

	@DisplayName("토큰 값이 빈 상태로 인증 정보를 조회할 경우 예외를 발생시킨다.")
	@Test
	public void testGetAuthenticationByEmptyToken() {
		assertThrows(IllegalArgumentException .class,
			() -> jwtProvider.getAuthentication(""));
	}

	@DisplayName("토큰이 올바르면 true를 반환한다.")
	@Test
	public void testIsValidateToken() {
		TokenDto tokenDto = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = tokenDto.getAccessToken();
		String refreshToken = tokenDto.getAccessToken();

		assertTrue(jwtProvider.isValidateToken(accessToken));
		assertTrue(jwtProvider.isValidateToken(refreshToken));
	}

	@DisplayName("유효하지 않은 토큰 형식이면 false를 반환한다.")
	@Test
	public void testIsValidateTokenByInvalidToken() {
		assertFalse(jwtProvider.isValidateToken("invalidToken"));
	}

	@DisplayName("만료된 토큰이면 false를 반환한다.")
	@Test
	public void testIsValidateTokenByExpiredToken() {
		Date expirationDate = new Date(new Date().getTime() - 1);
		final String expiredToken = createAdminToken(expirationDate, secretKey);

		assertFalse(jwtProvider.isValidateToken(expiredToken));
	}

	@DisplayName("시크릿 키가 틀리면 false를 반환한다.")
	@Test
	public void testIsValidateTokenByWrongSecretKeyToken() {
		Date expirationDate = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000L);
		final String invalidSecretToken = createAdminToken(expirationDate, createInvalidSecretKey());

		assertFalse(jwtProvider.isValidateToken(invalidSecretToken));
	}

	@DisplayName("토큰 값이 없으면 false를 반환한다.")
	@Test
	public void testIsValidateTokenByEmptyToken() {
		assertFalse(jwtProvider.isValidateToken(""));
	}

	private static String createAdminToken(Date expirationDate, Key secretKey) {
		Claims claims = Jwts.claims().setSubject("1");
		claims.put("role", Role.ADMIN);
		return Jwts.builder()
			.setClaims(claims)
			.setExpiration(expirationDate)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}

	private static Key createInvalidSecretKey() {
		String invalidSecretKeyString = "rkskekfkakqktkdkssudgktpdywjdmsrlathdmsdlqslekgkgkgkgakqjsrjdbhf";
		byte[] keyBytes = Decoders.BASE64.decode(invalidSecretKeyString);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
