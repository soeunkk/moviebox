package com.example.moviebox.admin.service;

import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.utils.MailUtils;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.*;
import com.example.moviebox.user.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AdminService {
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtProvider;
	private final MailUtils mailUtil;
	private final UserRepository userRepository;

	@Value("${server.domain}")
	private String serverDomain;

	@Transactional
	public void register(String email, String password) {
		validateRegistration(email);

		User user = User.createAdminUser(email, passwordEncoder.encode(password));
		User savedUser = userRepository.save(user);

		sendConfirmationEmail(savedUser);
	}

	private void validateRegistration(String email) {
		if (userRepository.existsByEmail(email)) {
			throw BusinessException.EMAIL_ALREADY_EXIST;
		}
	}

	private void sendConfirmationEmail(User user) {
		String email = user.getEmail();
		String subject = "Moviebox 인증 메일";
		String text = "<p>Moviebox 관리자 가입을 축하드립니다.</p><p>이메일 인증을 원한다면 아래 버튼을 클릭해 주세요.</p>"
			+ "<div><a href='" + serverDomain + "/api/admin/email-auth?key=" + user.getEmailAuthKey() + "'>인증</a></div>";
		mailUtil.sendMail(email, subject, text);
	}

	@Transactional
	public void authenticateMail(String authKey) {
		User user = userRepository.findByEmailAuthKey(authKey)
			.orElseThrow(() -> BusinessException.EMAIL_AUTH_KEY_INVALID);

		if (user.isEmailAuth()) {
			throw BusinessException.ALREADY_COMPLETE_AUTHENTICATION;
		}

		user.completeEmailAuthentication();
		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public TokenDto login(String email, String password) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BusinessException.USER_NOT_FOUND_BY_EMAIL);
		validateLogin(user, password);

		return jwtProvider.generateAccessTokenAndRefreshToken(user.getId());
	}

	private void validateLogin(User user, String password) {
		if (!user.isEmailAuth()) {
			throw BusinessException.EMAIL_NOT_VERIFIED_YET;
		}

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw BusinessException.USER_NOT_FOUND_BY_PASSWORD;
		}
	}
}
