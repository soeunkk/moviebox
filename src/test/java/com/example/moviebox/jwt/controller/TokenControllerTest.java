package com.example.moviebox.jwt.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.exception.BusinessException;
import com.example.moviebox.jwt.dto.*;
import com.example.moviebox.jwt.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TokenController.class)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class TokenControllerTest extends BaseControllerTest {
	@MockBean
	private TokenService tokenService;

	@Test
	public void testReissue() throws Exception {
		given(tokenService.reissue(any()))
			.willReturn(TokenDto.builder()
				.grantType("Bearer")
				.accessToken("access-token2")
				.refreshToken("refresh-token2")
				.build());

		ResultActions result = mockMvc.perform(post("/api/token/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenCreation.Request("access-token1", "refresh-token1")
				)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$..grantType").value("Bearer"))
			.andExpect(jsonPath("$..accessToken").value("access-token2"))
			.andExpect(jsonPath("$..refreshToken").value("refresh-token2"));

		// docs
		result.andDo(document("[success] reissue",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(ResourceSnippetParameters.builder()
				.summary("JWT 토큰 재발행")
				.tag("token")
				.requestFields(
					fieldWithPath("accessToken").description("현재 Access 토큰"),
					fieldWithPath("refreshToken").description("현재 Refresh 토큰")
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
	public void testReissueByWrongRefreshToken() throws Exception {
		given(tokenService.reissue(any()))
			.willThrow(BusinessException.INVALID_REFRESH_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/token/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenCreation.Request("access-token1", "wrong-refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorResponse(actions, BusinessException.INVALID_REFRESH_TOKEN);

		// docs
		actions.andDo(document("[fail] reissue - wrong refresh token",
			preprocessRequest(prettyPrint()),
			preprocessResponse(prettyPrint()),
			resource(ResourceSnippetParameters.builder().tag("token").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testReissueByWrongAccessToken() throws Exception {
		given(tokenService.reissue(any()))
			.willThrow(BusinessException.INVALID_ACCESS_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/token/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenCreation.Request("wrong-access-token1", "refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorResponse(actions, BusinessException.INVALID_ACCESS_TOKEN);

		// docs
		actions.andDo(
			document("[fail] reissue - wrong access token",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("token").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}

	@Test
	public void testReissueByExpiredRefreshToken() throws Exception {
		given(tokenService.reissue(any()))
			.willThrow(BusinessException.EXPIRED_REFRESH_TOKEN);

		ResultActions actions = mockMvc.perform(post("/api/token/reissue")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new TokenCreation.Request("access-token1", "expired-refresh-token1")
				)))
			.andExpect(status().isUnauthorized());
		checkErrorResponse(actions, BusinessException.EXPIRED_REFRESH_TOKEN);

		// docs
		actions.andDo(
			document("[fail] reissue - expired refresh token",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				resource(ResourceSnippetParameters.builder().tag("token").responseFields(ERROR_RESPONSE_FIELDS).build())));
	}
}
