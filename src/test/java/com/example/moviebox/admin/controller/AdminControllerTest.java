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
import com.example.moviebox.exception.*;
import com.example.moviebox.jwt.dto.TokenDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AdminController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class AdminControllerTest extends BaseControllerTest {
	@MockBean
	private AdminService adminService;

	@Test
	public void testRegister() throws Exception {
		willDoNothing()
			.given(adminService).register(anyString(), anyString());

		ResultActions result = mockMvc.perform(post("/api/admin/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					objectMapper.writeValueAsString(new AdminRequest("example@email.com", "password"))
				))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true));

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
		willThrow(BusinessException.EMAIL_FORMAT_INVALID)
			.given(adminService).register(anyString(), anyString());

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
		willThrow(BusinessException.EMAIL_ALREADY_EXIST)
			.given(adminService).register(anyString(), anyString());

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
	public void testRegisterWhenMailExceptionThrown() throws Exception {
		willThrow(new MailAuthenticationException(""))
			.given(adminService).register(anyString(), anyString());

		ResultActions result = mockMvc.perform(post("/api/admin/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new AdminRequest("exist-email@email.com", "password"))))
			.andExpect(status().isInternalServerError());
		checkErrorResponse(result, ErrorCode.CAN_NOT_SEND_EMAIL);

		// docs
		result.andDo(document("[fail] register - fail send mail",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testAuthenticateMail() throws Exception {
		willDoNothing()
			.given(adminService).authenticateMail(anyString());

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
	public void testAuthenticateMailByWrongKey() throws Exception {
		willThrow(BusinessException.EMAIL_AUTH_KEY_INVALID)
			.given(adminService).authenticateMail(anyString());

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
	public void testAuthenticateMailWhenAlreadyAuthenticatedMail() throws Exception {
		willThrow(BusinessException.ALREADY_COMPLETE_AUTHENTICATION)
			.given(adminService).authenticateMail(anyString());

		ResultActions result = mockMvc.perform(get("/api/admin/email-auth?key=auth-key"))
			.andExpect(status().isNotFound());
		checkErrorResponse(result, BusinessException.ALREADY_COMPLETE_AUTHENTICATION);

		// docs
		result.andDo(document("[fail] email auth - already authenticated mail",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(ResourceSnippetParameters.builder().tag("admin").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testLogin() throws Exception {
		given(adminService.login(anyString(), anyString()))
			.willReturn(TokenDto.builder()
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
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$..grantType").value("Bearer"))
			.andExpect(jsonPath("$..accessToken").value("access-token"))
			.andExpect(jsonPath("$..refreshToken").value("refresh-token"));

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
						fieldWithPath("success").description("요청 성공 여부"),
						fieldWithPath("data").description("결과 데이터"),
						fieldWithPath("data.grantType").description("인증 타입"),
						fieldWithPath("data.accessToken").description("새로 발급된 Access 토큰"),
						fieldWithPath("data.refreshToken").description("새로 발급된 Refresh 토큰"),
						fieldWithPath("error").description("에러 내용")
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
