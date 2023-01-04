package com.example.moviebox.admin.dto;

import javax.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRequest {
	@NotEmpty
	private String email;

	@NotEmpty
	private String password;
}
