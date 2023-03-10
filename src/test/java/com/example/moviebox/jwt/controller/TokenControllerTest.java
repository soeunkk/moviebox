package com.example.moviebox.jwt.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
		given(tokenService.reissue(anyString(), anyString()))
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
				.summary("JWT ?????? ?????????")
				.tag("token")
				.requestFields(
					fieldWithPath("accessToken").description("?????? Access ??????"),
					fieldWithPath("refreshToken").description("?????? Refresh ??????")
				)
				.responseFields(
					fieldWithPath("success").description("?????? ?????? ??????"),
					fieldWithPath("data").description("?????? ?????????"),
					fieldWithPath("data.grantType").description("?????? ??????"),
					fieldWithPath("data.accessToken").description("?????? ????????? Access ??????"),
					fieldWithPath("data.refreshToken").description("?????? ????????? Refresh ??????"),
					fieldWithPath("error").description("?????? ??????")
				)
				.build())
		));
	}

	@Test
	public void testReissueByWrongRefreshToken() throws Exception {
		given(tokenService.reissue(anyString(), anyString()))
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
		given(tokenService.reissue(anyString(), anyString()))
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
		given(tokenService.reissue(anyString(), anyString()))
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
