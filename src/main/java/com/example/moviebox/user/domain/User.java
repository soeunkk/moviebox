package com.example.moviebox.user.domain;

import java.time.LocalDateTime;
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

	private boolean emailAuthYn;
	private String emailAuthKey;
	private LocalDateTime emailAuthDate;
}
