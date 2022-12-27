package com.example.moviebox.admin.controller;

import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
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
	public String emailAuth(@RequestParam(name="id") String authKey) {
		adminService.emailAuth(authKey);
		return "인증이 완료되었습니다.";
	}

	@PostMapping("/login")
	public String login(@RequestBody @Valid AdminRequest user) {
		return adminService.login(user.getEmail(), user.getPassword());
	}
}
