package com.example.moviebox.admin.dto;

import javax.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminRequest {
	@NotNull
	private String email;

	@NotNull
	private String password;
}
