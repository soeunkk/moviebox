package com.example.moviebox.user.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique = true)
	private String email;
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	@Enumerated(EnumType.STRING)
	private SocialType socialType;
	private String accessToken;

	private boolean isEmailAuth;
	private String emailAuthKey;
	private LocalDateTime emailAuthDate;

	private LocalDateTime registrationDate;

	public static User createAdminUser(String email, String encodingPassword) {
		return User.builder()
			.email(email)
			.password(encodingPassword)
			.role(Role.ADMIN)
			.isEmailAuth(false)
			.emailAuthKey(UUID.randomUUID().toString())
			.registrationDate(LocalDateTime.now())
			.build();
	}

	public void completeEmailAuthentication() {
		isEmailAuth = true;
		emailAuthDate = LocalDateTime.now();
	}
}
