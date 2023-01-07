package com.example.moviebox.jwt.controller;

import com.example.moviebox.common.dto.ApiResponse;
import com.example.moviebox.jwt.dto.TokenCreation;
import com.example.moviebox.jwt.service.TokenService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/token")
public class TokenController {
	private final TokenService tokenService;

	@PostMapping("/reissue")
	public ApiResponse<TokenCreation.Response> reissue(@RequestBody @Valid TokenCreation.Request tokenRequest) {
		TokenCreation.Response tokenResponse = TokenCreation.Response.from(
			tokenService.reissue(tokenRequest));
		return ApiResponse.success(tokenResponse);
	}
}
