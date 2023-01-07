package com.example.moviebox.admin.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRequest {
	@NotEmpty(message = "email을 입력해주세요.")
	@Email(message = "email 형식이 올바르지 않습니다.")
	private String email;

	@NotEmpty(message = "password를 입력해주세요.")
	private String password;
}
