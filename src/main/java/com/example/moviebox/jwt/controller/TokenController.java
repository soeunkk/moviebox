package com.example.moviebox.jwt.controller;

import com.example.moviebox.jwt.dto.TokenDto;
import com.example.moviebox.jwt.dto.TokenDto.Response;
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
	public Response reissue(@RequestBody @Valid TokenDto.Request tokenRequest) {
		return tokenService.reissue(tokenRequest);
	}
}
