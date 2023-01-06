package com.example.moviebox.admin.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.epages.restdocs.apispec.*;
import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.admin.dto.AdminRequest;
import com.example.moviebox.admin.service.AdminService;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AdminController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class AdminControllerTest extends BaseControllerTest {
	@MockBean
	private AdminService adminService;

	@Test
	public void testRegister() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willReturn(1L);

		ResultActions result = mockMvc.perform(post("/api/admin/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))
				))
			.andExpect(status().isOk())
			.andExpect(content().string("1"));

		// docs
		result.andDo(document("[success] register",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder()
					.summary("관리자 등록")
					.tag("admin")
					.requestFields(
						fieldWithPath("email").description("이메일"),
						fieldWithPath("password").description("비밀번호")
					)
					.build())
			));
	}

	@Test
	public void testRegisterByWrongFormatEmail() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willThrow(BusinessException.EMAIL_FORMAT_INVALID);

		ResultActions result = mockMvc.perform(post("/api/admin/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("wrong-email-format", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorResponse(result, BusinessException.EMAIL_FORMAT_INVALID);

		// docs
		result.andDo(document("[fail] register - wrong format email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testRegisterByExistEmail() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willThrow(BusinessException.EMAIL_ALREADY_EXIST);

		ResultActions result = mockMvc.perform(post("/api/admin/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("exist-email@email.com", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorResponse(result, BusinessException.EMAIL_ALREADY_EXIST);

		// docs
		result.andDo(document("[fail] register - exist email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testEmailAuth() throws Exception {
		willDoNothing()
			.given(adminService).emailAuth(anyString());

		ResultActions result = mockMvc.perform(get("/api/admin/email-auth?key=emailAuthKey"))
			.andExpect(status().isOk())
			.andExpect(content().string("인증이 완료되었습니다."));

		// docs
		result.andDo(document("[success] email auth",
				resource(ResourceSnippetParameters.builder()
					.summary("관리자 계정 이메일 인증")
					.tag("admin")
					.requestParameters(
						parameterWithName("key").description("이메일 인증 키")
					)
					.build())
			));
	}

	@Test
	public void testEmailAuthByWrongKey() throws Exception {
		willThrow(BusinessException.EMAIL_AUTH_KEY_INVALID)
			.given(adminService).emailAuth(anyString());

		ResultActions result = mockMvc.perform(get("/api/admin/email-auth?key=wrongKey"))
			.andExpect(status().isBadRequest());
		checkErrorResponse(result, BusinessException.EMAIL_AUTH_KEY_INVALID);

		// docs
		result.andDo(document("[fail] email auth - wrong key",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
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

		ResultActions result = mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.grantType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"));

		// docs
		result.andDo(document("[success] login",
				resource(ResourceSnippetParameters.builder()
					.description("이메일과 비밀번호로 관리자 계정을 로그인할 수 있습니다.")
					.summary("관리자 로그인")
					.tag("admin")
					.requestFields(
						fieldWithPath("email").description("이메일"),
						fieldWithPath("password").description("비밀번호")
					)
					.responseFields(
						fieldWithPath("grantType").description("인증 타입"),
						fieldWithPath("accessToken").description("새로 발급된 Access 토큰"),
						fieldWithPath("refreshToken").description("새로 발급된 Refresh 토큰")
					)
					.build())
			));
	}

	@Test
	public void testLoginByWrongEmail() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willThrow(BusinessException.USER_NOT_FOUND_BY_EMAIL);

		ResultActions result = mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("wrong-email@email.com", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorResponse(result, BusinessException.USER_NOT_FOUND_BY_EMAIL);

		// docs
		result.andDo(document("[fail] login - wrong email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())
			));
	}

	@Test
	public void testLoginWhenNotYetEmailAuth() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willThrow(BusinessException.EMAIL_NOT_VERIFIED_YET);

		ResultActions result = mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorResponse(result, BusinessException.EMAIL_NOT_VERIFIED_YET);

		// docs
		result.andDo(document("[fail] login - not yet email auth",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testLoginByWrongPassword() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willThrow(BusinessException.USER_NOT_FOUND_BY_PASSWORD);

		ResultActions result = mockMvc.perform(post("/api/admin/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("example@email.com", "wrong-password"))))
			.andExpect(status().isBadRequest());

		checkErrorResponse(result, BusinessException.USER_NOT_FOUND_BY_PASSWORD);

		// docs
		result.andDo(document("[fail] login - wrong password",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}
}
