package com.example.moviebox.admin.controller;

import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
import com.example.moviebox.common.dto.ApiResponse;
import com.example.moviebox.jwt.dto.TokenDto;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final AdminService adminService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/register")
	public ApiResponse<?> register(@RequestBody @Valid AdminRequest user) {
		adminService.register(user.getEmail(), user.getPassword());
		return ApiResponse.success();
	}

	@GetMapping("/email-auth")
	public String emailAuth(@RequestParam(name="key") String authKey) {	// 사용자에게 보여줄 것이므로 ApiResponse에 담지 않음
		adminService.authenticateMail(authKey);
		return "인증이 완료되었습니다.";
	}

	@PostMapping("/login")
	public ApiResponse<TokenDto.Response> login(@RequestBody @Valid AdminRequest adminLoginRequest) {
		TokenDto.Response tokenResponse = adminService.login(adminLoginRequest.getEmail(), adminLoginRequest.getPassword());
		return ApiResponse.success(tokenResponse);
	}
}
