package com.example.moviebox.admin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.example.moviebox.utils.MailUtils;
import com.example.moviebox.exception.*;
import com.example.moviebox.jwt.*;
import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.user.domain.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtTokenProvider jwtProvider;
	@Mock
	private MailUtils mailUtil;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AdminService adminService;

	@Test
	public void testRegister() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(passwordEncoder.encode(anyString()))
			.willReturn("encoded-password");
		given(userRepository.save(any()))
			.willReturn(User.builder()
				.email("email@gmail.com")
				.build());
		willDoNothing()
			.given(mailUtil).sendMail(anyString(), anyString(), anyString());
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

		adminService.register("email@gmail.com", "pw");

		verify(userRepository, times(1)).save(captor.capture());
		assertEquals(Role.ADMIN, captor.getValue().getRole());
		assertEquals("email@gmail.com", captor.getValue().getEmail());
		assertEquals("encoded-password", captor.getValue().getPassword());
		assertFalse(captor.getValue().isEmailAuth());
		assertNotNull(captor.getValue().getEmailAuthKey());
	}

	@Test
	public void testRegisterByWrongFormatEmail() {
		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.register("wrong-format-email", "pw"));

		assertEquals(BusinessException.EMAIL_FORMAT_INVALID, exception);
	}

	@Test
	public void testRegisterByExistEmail() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(true);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.register("email@gmail.com", "pw"));

		assertEquals(BusinessException.EMAIL_ALREADY_EXIST, exception);
	}

	@Test
	public void testRegisterWhenMailExceptionThrown() {
		given(userRepository.existsByEmail(anyString()))
			.willReturn(false);
		given(passwordEncoder.encode(anyString()))
			.willReturn("encoded-password");
		given(userRepository.save(any()))
			.willReturn(User.builder()
				.email("email@gmail.com")
				.build());
		willThrow(new MailSendException(""))
			.given(mailUtil).sendMail(anyString(), anyString(), anyString());

		assertThrows(MailException.class,
			() -> adminService.register("email@gmail.com", "pw"));
	}

	@Test
	public void testAuthenticateMail() {
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

		adminService.authenticateMail("auth-key");

		verify(userRepository, times(1)).save(captor.capture());
		assertTrue(captor.getValue().isEmailAuth());
		assertNotNull(captor.getValue().getEmailAuthDate());
	}

	@Test
	public void testAuthenticateMailByWrongKey() {
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.authenticateMail("auth-key"));

		assertEquals(BusinessException.EMAIL_AUTH_KEY_INVALID, exception);
	}

	@Test
	public void testAuthenticateMailWhenAlreadyAuthenticatedMail() {
		given(userRepository.findByEmailAuthKey(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("pw")
				.emailAuthDate(null)
				.isEmailAuth(true)
				.build()));

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.authenticateMail("auth-key"));

		assertEquals(BusinessException.ALREADY_COMPLETE_AUTHENTICATION, exception);
	}

	@Test
	public void testLogin() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("pw")
				.role(Role.ADMIN)
				.isEmailAuth(true)
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

		assertEquals(BusinessException.USER_NOT_FOUND_BY_EMAIL, exception);
	}

	@Test
	public void testLoginWhenNotYetEmailAuth() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("wrong-password")
				.role(Role.ADMIN)
				.isEmailAuth(false)
				.build()));

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.login("email", "pw"));

		assertEquals(BusinessException.EMAIL_NOT_VERIFIED_YET, exception);
	}

	@Test
	public void testLoginByWrongPassword() {
		given(userRepository.findByEmail(anyString()))
			.willReturn(Optional.of(User.builder()
				.id(1L)
				.email("email")
				.password("wrong-password")
				.role(Role.ADMIN)
				.isEmailAuth(true)
				.build()));
		given(passwordEncoder.matches(anyString(), anyString()))
			.willReturn(false);

		BusinessException exception = assertThrows(BusinessException.class,
			() -> adminService.login("email", "pw"));

		assertEquals(BusinessException.USER_NOT_FOUND_BY_PASSWORD, exception);
	}
}
