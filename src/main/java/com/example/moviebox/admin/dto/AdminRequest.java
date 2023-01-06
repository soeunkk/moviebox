package com.example.moviebox.admin.dto;

import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRequest {
	@NotEmpty(message = "email을 입력해주세요.")
	private String email;

	@NotEmpty(message = "password를 입력해주세요.")
	private String password;
}
