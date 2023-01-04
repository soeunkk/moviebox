package com.example.moviebox.jwt;

import static org.junit.jupiter.api.Assertions.*;

import com.example.moviebox.user.domain.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.*;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

@SpringBootTest
class JwtTokenProviderTest {
	@Autowired
	private JwtTokenProvider jwtProvider;

	@Value("${jwt.secret}")
	private String secretKeyString;
	private Key secretKey;

	@BeforeEach
	private void init() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@DisplayName("관리자 권한의 토큰이 올바르게 생성된다.")
	@Test
	public void testGenerateAccessTokenAndRefreshToken() {
		TokenDto.Response response = jwtProvider.generateAccessTokenAndRefreshToken(1L);

		assertNotNull(response.getGrantType());
		assertNotNull(response.getAccessToken());
		assertNotNull(response.getRefreshToken());
	}

	@DisplayName("토큰에서 올바른 인증 정보를 조회한다.")
	@Test
	public void testGetAuthentication() {
		TokenDto.Response response = jwtProvider.generateAccessTokenAndRefreshToken(1L);	// TODO: admin 권한 있는 걸로 사람 만들던지 해야 ㅎ됨
		String accessToken = response.getAccessToken();

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

	@DisplayName("토큰이 올바르면 true를 반환한다.")
	@Test
	public void testValidateToken() {
		TokenDto.Response response = jwtProvider.generateAccessTokenAndRefreshToken(1L);
		String accessToken = response.getAccessToken();
		String refreshToken = response.getAccessToken();

		assertTrue(jwtProvider.validateToken(accessToken));
		assertTrue(jwtProvider.validateToken(refreshToken));
	}

	@DisplayName("유효하지 않은 토큰 형식이면 false를 반환한다.")
	@Test
	public void testValidateTokenByInvalidToken() {
		assertFalse(jwtProvider.validateToken("invalidToken"));
	}

	@DisplayName("만료된 토큰이면 false를 반환한다.")
	@Test
	public void validateTokenByExpiredToken() {
		Date expirationDate = new Date(new Date().getTime() - 1);
		final String expiredToken = createAdminToken(expirationDate, secretKey);

		assertFalse(jwtProvider.validateToken(expiredToken));
	}

	@DisplayName("시크릿 키가 틀리면 false를 반환한다.")
	@Test
	public void testValidateTokenByWrongSecretKeyToken() {
		Date expirationDate = new Date(new Date().getTime() + 365 * 24 * 60 * 60 * 1000L);
		final String invalidSecretToken = createAdminToken(expirationDate, createInvalidSecretKey());

		assertFalse(jwtProvider.validateToken(invalidSecretToken));
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
