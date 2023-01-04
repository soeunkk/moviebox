package com.example.moviebox.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.component.MailUtil;
import com.example.moviebox.configuration.security.SecurityUser;
import com.example.moviebox.exception.*;
import com.example.moviebox.jwt.*;
import com.example.moviebox.user.domain.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtTokenProvider jwtProvider;
	@Mock
	private MailUtil mailUtil;
	@Mock
	private UserRepository userRepository;
	@Mock
	private RedisService redisService;

	@InjectMocks
	private AdminService adminService;

	@Test
	public void testRegister() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.empty());
		given(passwordEncoder.encode(anyString()))
			.willReturn("encoded-password");
		given(userRepository.save(any()))
			.willReturn(User.builder()
				.email("email@gmail.com")
				.build());
		given(mailUtil.sendMail(anyString(), anyString(), anyString()))
			.willReturn(true);
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		adminService.register("email@gmail.com", "pw");

		verify(userRepository, times(1)).save(captor.capture());
		assertEquals(Role.ADMIN, captor.getValue().getRole());
		assertEquals("email@gmail.com", captor.getValue().getEmail());
		assertEquals("encoded-password", captor.getValue().getPassword());
		assertFalse(captor.getValue().isEmailAuthYn());
		assertNotNull(captor.getValue().getEmailAuthKey());
		assertNotNull(captor.getValue().getRegistrationDate());
	}

	@Test
	public void testRegisterByWrongFormatEmail() {
		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.register("wrong-format-email", "pw"));

		assertEquals(BusinessException.EMAIL_FORMAT_INVALID, exception);
	}

	@Test
	public void testRegisterByExistEmail() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email@gmail.con")
				.build()));

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.register("email@gmail.com", "pw"));

		assertEquals(BusinessException.EMAIL_ALREADY_EXIST, exception);
	}

	@Test
	public void testEmailAuth() {
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("pw")
				.emailAuthDate(null)
				.build()));
		given(userRepository.save(any()))
			.willReturn(User.builder().build());
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		adminService.emailAuth("auth-key");

		verify(userRepository, times(1)).save(captor.capture());
		assertTrue(captor.getValue().isEmailAuthYn());
		assertNotNull(captor.getValue().getEmailAuthDate());
	}

	@Test
	public void testEmailAuthByWrongKey() {
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.emailAuth("auth-key"));

		assertEquals(exception, BusinessException.EMAIL_AUTH_KEY_INVALID);
	}

	@Test
	public void testLogin() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("pw")
				.role(Role.ADMIN)
				.emailAuthYn(true)
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(true);
		given(jwtProvider.generateAccessTokenAndRefreshToken(anyLong()))
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token")
				.refreshToken("refresh-token")
				.build());

		TokenDto.Response tokenResponse = adminService.login("email", "pw");

		assertEquals("Bearer", tokenResponse.getGrantType());
		assertEquals("access-token", tokenResponse.getAccessToken());
		assertEquals("refresh-token", tokenResponse.getRefreshToken());
	}

	@Test
	public void testLoginByWrongEmail() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.login("email", "pw"));

		assertEquals(exception, BusinessException.USER_NOT_FOUND_BY_EMAIL);
	}

	@Test
	public void testLoginWhenNotYetEmailAuth() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("wrong-password")
				.role(Role.ADMIN)
				.emailAuthYn(false)
				.build()));

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.login("email", "pw"));

		assertEquals(exception, BusinessException.EMAIL_NOT_VERIFIED_YET);
	}

	@Test
	public void testLoginByWrongPassword() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("wrong-password")
				.role(Role.ADMIN)
				.emailAuthYn(true)
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.login("email", "pw"));

		assertEquals(exception, BusinessException.USER_NOT_FOUND_BY_PASSWORD);
	}

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
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token2")
				.refreshToken("refresh-token2")
				.build());

		TokenDto.Response tokenResponse = adminService.reissue(new TokenDto.Request("access-token1", "refresh-token1"));

		assertEquals("Bearer", tokenResponse.getGrantType());
		assertEquals("access-token2", tokenResponse.getAccessToken());
		assertEquals("refresh-token2", tokenResponse.getRefreshToken());
	}

	@Test
	public void testReissueByWrongRefreshToken() {
		given(jwtProvider.validateToken("refresh-token"))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.reissue(new TokenDto.Request("access-token", "refresh-token")));

		assertEquals(exception, BusinessException.INVALID_REFRESH_TOKEN);
	}

	@Test
	public void testReissueByWrongAccessToken() {
		given(jwtProvider.validateToken(anyString()))
			.willReturn(true);
		given(jwtProvider.validateToken("access-token"))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.reissue(new TokenDto.Request("access-token", "refresh-token")));

		assertEquals(exception, BusinessException.INVALID_ACCESS_TOKEN);
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
			() -> adminService.reissue(new TokenDto.Request("access-token", "previous-refresh-token")));

		assertEquals(exception, BusinessException.EXPIRED_REFRESH_TOKEN);
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
			() -> adminService.reissue(new TokenDto.Request("access-token", "refresh-token")));

		assertEquals(exception, BusinessException.EXPIRED_REFRESH_TOKEN);
	}
}
