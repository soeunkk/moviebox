package com.example.moviebox.jwt.controller;

import com.example.moviebox.common.dto.ApiResponse;
import com.example.moviebox.jwt.dto.TokenDto;
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
	public ApiResponse<TokenDto.Response> reissue(@RequestBody @Valid TokenDto.Request tokenRequest) {
		TokenDto.Response tokenResponse = tokenService.reissue(tokenRequest);
		return ApiResponse.success(tokenResponse);
	}
}
