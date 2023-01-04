package com.example.moviebox.admin.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
import com.example.moviebox.common.redis.RedisService;
import com.example.moviebox.jwt.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;

class AdminControllerTest extends BaseControllerTest {
	@MockBean
	private AdminService adminService;

	@Value("${jwt.secret}")
	private String secretKeyString;
	private Key secretKey;

	@BeforeEach
	private void init() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
		this.secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@Test
	public void testRegister() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willReturn(1L);

		mockMvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AdminRequest("email", "pw"))))
			.andExpect(status().isOk())
			.andExpect(content().string("1"));
	}

	@Test
	public void testEmailAuth() throws Exception {
		willDoNothing()
			.given(adminService).emailAuth(anyString());

		mockMvc.perform(get("/api/email-auth?id=kkk"))
			.andExpect(status().isOk())
			.andExpect(content().string("인증이 완료되었습니다."));
	}

	@Test
	public void testLogin() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token")
				.refreshToken("refresh-token")
				.build()
			);

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AdminRequest("email", "pw"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.grantType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"));
	}

	@Test
	public void testReissue() throws Exception {
		given(adminService.reissue(any()))
			.willReturn(TokenDto.Response.builder()
				.grantType("Bearer")
				.accessToken("access-token2")
				.refreshToken("refresh-token2")
				.build());

		mockMvc.perform(post("/api/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenDto.Request("access-token1", "refresh-token1")
				)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.grantType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").value("access-token2"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token2"));
	}
}
