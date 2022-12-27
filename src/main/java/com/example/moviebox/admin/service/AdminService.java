package com.example.moviebox.admin.service;

import com.example.moviebox.component.MailUtil;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.JwtProvider;
import com.example.moviebox.user.domain.*;
import java.util.Optional;
import java.util.regex.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminService {
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final MailUtil mailUtil;
	private final UserRepository userRepository;

	@Value("${server.domain}")
	private String serverDomain;

	public long register(String email, String password) {
		validateRegistration(email);

		User user = User.createAdminUser(email, passwordEncoder.encode(password));
		User savedUser = userRepository.save(user);

		sendConfirmationEmail(savedUser);

		return savedUser.getId();
	}

	private void validateRegistration(String email) {
		if (!isValidEmailFormat(email)) {
			throw BusinessException.EMAIL_FORMAT_INVALID;
		}

		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isPresent()) {
			throw BusinessException.EMAIL_ALREADY_EXIST;
		}
	}

	private static boolean isValidEmailFormat(String email){
		boolean validation = false;

		String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(email);
		if(m.matches()) {
			validation = true;
		}

		return validation;
	}

	private void sendConfirmationEmail(User user) {
		String email = user.getEmail();
		String subject = "Moviebox 인증 메일";
		String text = "<p>Moviebox 관리자 가입을 축하드립니다.</p><p>이메일 인증을 원한다면 아래 버튼을 클릭해 주세요.</p>"
			+ "<div><a href='" + serverDomain + "/api/email-auth?id=" + user.getEmailAuthKey() + "'>인증</a></div>";
		mailUtil.sendMail(email, subject, text);
	}

	public void emailAuth(String authKey) {
		User user = userRepository.findByEmailAuthKey(authKey)
			.orElseThrow(() -> BusinessException.EMAIL_AUTH_KEY_INVALID);
		user.completeEmailAuthentication();
		userRepository.save(user);
	}

	public String login(String email, String password) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> BusinessException.USER_NOT_FOUND_BY_EMAIL);
		validateLogin(user, password);

		return jwtProvider.createToken(user.getEmail(), user.getRole());
	}

	private void validateLogin(User user, String password) {
		if (!user.isEmailAuthYn()) {
			throw BusinessException.EMAIL_NOT_VERIFIED_YET;
		}

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw BusinessException.USER_NOT_FOUND_BY_PASSWORD;
		}
	}
}
