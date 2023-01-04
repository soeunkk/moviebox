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
import com.example.moviebox.jwt.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class AdminControllerTest extends BaseControllerTest {
	private static final List<FieldDescriptor> ERROR_RESPONSE_FIELDS = Arrays.asList(
		fieldWithPath("type").description("에러 코드"),
		fieldWithPath("title").description("간략한 설명"),
		fieldWithPath("status").description("HTTP 응답 코드"),
		fieldWithPath("detail").description("자세한 설명"),
		fieldWithPath("instance").description("에러 발생 근원지")
	);

	@MockBean
	private AdminService adminService;

	@Test
	public void testRegister() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willReturn(1L);

		mockMvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))))
			.andExpect(status().isOk())
			.andExpect(content().string("1"))
			.andDo(document("[success] register",
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

		ResultActions actions = mockMvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("wrong-email-format", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorMockMvc(actions, BusinessException.EMAIL_FORMAT_INVALID);
		actions.andDo(document("[fail] register - wrong format email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testRegisterByExistEmail() throws Exception {
		given(adminService.register(anyString(), anyString()))
			.willThrow(BusinessException.EMAIL_ALREADY_EXIST);

		ResultActions actions = mockMvc.perform(post("/api/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("exist-email@email.com", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorMockMvc(actions, BusinessException.EMAIL_ALREADY_EXIST);
		actions.andDo(document("[fail] register - exist email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testEmailAuth() throws Exception {
		willDoNothing()
			.given(adminService).emailAuth(anyString());

		mockMvc.perform(get("/api/email-auth?key=emailAuthKey"))
			.andExpect(status().isOk())
			.andExpect(content().string("인증이 완료되었습니다."))
			.andDo(document("[success] email auth",
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

		ResultActions actions = mockMvc.perform(get("/api/email-auth?key=wrongKey"))
			.andExpect(status().isBadRequest());
		checkErrorMockMvc(actions, BusinessException.EMAIL_AUTH_KEY_INVALID);
		actions.andDo(document("[fail] email auth - wrong key",
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

		mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.grantType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").value("access-token"))
			.andExpect(jsonPath("$.refreshToken").value("refresh-token"))
			.andDo(document("[success] login",
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

		ResultActions actions = mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("wrong-email@email.com", "password"))))
			.andExpect(status().isBadRequest());
		checkErrorMockMvc(actions, BusinessException.USER_NOT_FOUND_BY_EMAIL);
		actions.andDo(document("[fail] login - wrong email",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())
			));
	}

	@Test
	public void testLoginWhenNotYetEmailAuth() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willThrow(BusinessException.EMAIL_NOT_VERIFIED_YET);

		ResultActions actions = mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))))
			.andExpect(status().isBadRequest());

		checkErrorMockMvc(actions, BusinessException.EMAIL_NOT_VERIFIED_YET);

		actions.andDo(document("[fail] login - not yet email auth",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testLoginByWrongPassword() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willThrow(BusinessException.USER_NOT_FOUND_BY_PASSWORD);

		ResultActions actions = mockMvc.perform(post("/api/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("example@email.com", "wrong-password"))))
			.andExpect(status().isBadRequest());

		checkErrorMockMvc(actions, BusinessException.USER_NOT_FOUND_BY_PASSWORD);

		actions.andDo(document("[fail] login - wrong password",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
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
			.andExpect(jsonPath("$.refreshToken").value("refresh-token2"))
			.andDo(document("[success] reissue",
				resource(ResourceSnippetParameters.builder()
					.summary("JWT 토큰 재발행")
					.tag("admin")
					.requestFields(
						fieldWithPath("accessToken").description("현재 Access 토큰"),
						fieldWithPath("refreshToken").description("현재 Refresh 토큰")
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
	public void testReissueByWrongRefreshToken() throws Exception {
		given(adminService.reissue(any()))
			.willThrow(BusinessException.INVALID_REFRESH_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenDto.Request("access-token1", "wrong-refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorMockMvc(actions, BusinessException.INVALID_REFRESH_TOKEN);
		actions.andDo(document("[fail] reissue - wrong refresh token",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testReissueByWrongAccessToken() throws Exception {
		given(adminService.reissue(any()))
			.willThrow(BusinessException.INVALID_ACCESS_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenDto.Request("wrong-access-token1", "refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorMockMvc(actions, BusinessException.INVALID_ACCESS_TOKEN);
		actions.andDo(
			document("[fail] reissue - wrong access token",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testReissueByExpiredRefreshToken() throws Exception {
		given(adminService.reissue(any()))
			.willThrow(BusinessException.EXPIRED_REFRESH_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenDto.Request("access-token1", "expired-refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorMockMvc(actions, BusinessException.EXPIRED_REFRESH_TOKEN);
		actions.andDo(
			document("[fail] reissue - expired refresh token",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	private void checkErrorMockMvc(ResultActions resultActions, BusinessException ex) throws Exception {
		resultActions.andExpect(jsonPath("$.type").value(ex.getErrorCode().getErrorType()))
			.andExpect(jsonPath("$.title").value(ex.getErrorCode().getDescription()))
			.andExpect(jsonPath("$.status").value(ex.getHttpStatus().value()))
			.andExpect(jsonPath("$.detail").value(ex.getMessage()));
	}
}
