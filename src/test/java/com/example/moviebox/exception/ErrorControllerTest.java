package com.example.moviebox.exception;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.admin.controller.AdminController;
import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(value = {ErrorController.class, AdminController.class})
class ErrorControllerTest extends BaseControllerTest {
	@MockBean
	private AdminService adminService;

	@DisplayName("이메일 인증 키가 올바르지 않은 경우를 에러 핸들링한다.")
	@Test
	public void handleBusinessException() throws Exception {
		willThrow(BusinessException.EMAIL_AUTH_KEY_INVALID)
			.given(adminService).emailAuth(anyString());

		mockMvc.perform(get("/api/admin/email-auth?id=kkk"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$..type").value("COMMON-001"))
			.andExpect(jsonPath("$..status").value(400))
			.andExpect(jsonPath("$..instance").value("/api/admin/email-auth"));
	}

	@DisplayName("잘못된 HTTP 메소드로 요청한 경우를 에러 핸들링한다.")
	@Test
	public void handleHttpRequestMethodNotSupportedException() throws Exception {
		mockMvc.perform(post("/api/admin/email-auth?id=kkk"))
			.andExpect(status().isMethodNotAllowed());
	}

	@DisplayName("Parameter가 누락된 경우를 에러 핸들링한다.")
	@Test
	public void handleMissingServletRequestParameterException() throws Exception {
		mockMvc.perform(get("/api/admin/email-auth"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$..type").value("COMMON-001"))
			.andExpect(jsonPath("$..status").value(400))
			.andExpect(jsonPath("$..instance").value("/api/admin/email-auth"));
	}

	@DisplayName("Content Type이 잘못 되었을 경우를 에러 핸들링한다.")
	@Test
	public void handleHttpMediaTypeNotSupportedException() throws Exception {
		mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.TEXT_PLAIN)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("abc123@gmail.com", "123123")
				)))
			.andExpect(status().isUnsupportedMediaType())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$..type").value("COMMON-003"))
			.andExpect(jsonPath("$..status").value(415))
			.andExpect(jsonPath("$..instance").value("/api/admin/login"));
	}

	@DisplayName("Request Body 중 일부가 누락 되었을 경우를 에러 핸들링한다.")
	@Test
	public void handleMethodArgumentNotValidException() throws Exception {
		mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("abc123@gmail.com", null)
				)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$..type").value("COMMON-001"))
			.andExpect(jsonPath("$..status").value(400))
			.andExpect(jsonPath("$..instance").value("/api/admin/login"));
	}

	@DisplayName("Request Body가 전체 누락 되었을 경우를 에러 핸들링한다.")
	@Test
	public void handleInvalidInputValueException_HttpMessageNotReadableException() throws Exception {
		mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$..type").value("COMMON-001"))
			.andExpect(jsonPath("$..status").value(400))
			.andExpect(jsonPath("$..instance").value("/api/admin/login"));
	}
}
