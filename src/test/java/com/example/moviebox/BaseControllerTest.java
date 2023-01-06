package com.example.moviebox;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.moviebox.configuration.RedisConfiguration;
import com.example.moviebox.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.*;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest
@ContextConfiguration(classes = {MovieBoxApplication.class, SecurityException.class, RedisConfiguration.class})
public abstract class BaseControllerTest {
	protected static final List<FieldDescriptor> ERROR_RESPONSE_FIELDS = Arrays.asList(	// for rest docs
		fieldWithPath("type").description("에러 코드"),
		fieldWithPath("title").description("간략한 설명"),
		fieldWithPath("status").description("HTTP 응답 코드"),
		fieldWithPath("detail").description("자세한 설명"),
		fieldWithPath("instance").description("에러 발생 근원지")
	);

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	protected WebApplicationContext webApplicationContext;

	@BeforeEach
	public void init(WebApplicationContext ctx, RestDocumentationContextProvider restDocumentationContextProvider) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
			.apply(documentationConfiguration(restDocumentationContextProvider))
			.addFilters(new CharacterEncodingFilter("UTF-8", true))
			.alwaysDo(print())
			.build();
	}

	protected void checkErrorResponse(ResultActions result, BusinessException ex) throws Exception {
		result.andExpect(jsonPath("$.type").value(ex.getErrorCode().getErrorType()))
			.andExpect(jsonPath("$.title").value(ex.getErrorCode().getDescription()))
			.andExpect(jsonPath("$.status").value(ex.getHttpStatus().value()))
			.andExpect(jsonPath("$.detail").value(ex.getMessage()));
	}
}
