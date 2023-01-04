package com.example.moviebox.admin.controller;

import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
import com.example.moviebox.jwt.TokenDto;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AdminController {
	private final AdminService adminService;

	@PostMapping("/register")
	public long register(@RequestBody @Valid AdminRequest user) {
		return adminService.register(user.getEmail(), user.getPassword());
	}

	@GetMapping("/email-auth")
	public String emailAuth(@RequestParam(name="key") String authKey) {
		adminService.emailAuth(authKey);
		return "인증이 완료되었습니다.";
	}

	@PostMapping("/login")
	public TokenDto.Response login(@RequestBody @Valid AdminRequest adminLoginRequest) {
		return adminService.login(adminLoginRequest.getEmail(), adminLoginRequest.getPassword());
	}

	@PostMapping("/reissue")
	public TokenDto.Response reissue(@RequestBody @Valid TokenDto.Request tokenRequest) {	// TODO: admin에 있어도 되는가?
		return adminService.reissue(tokenRequest);
	}
}
