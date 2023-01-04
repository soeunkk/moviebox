package com.example.moviebox.exception.rest;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.admin.service.AdminService;
import com.example.moviebox.exception.ErrorCode;
import com.example.moviebox.exception.rest.CodeResponseFieldsSnippet;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.*;
import org.springframework.test.web.servlet.ResultActions;

public class ErrorCodeDocumentationTest extends BaseControllerTest {
	@MockBean
	private AdminService adminService;

	@DisplayName("ErrorCode 문서화")
	@Test
	public void errorCodeDocumentation() throws Exception {
		// given + when
		ResultActions result = mockMvc.perform(get("/errors")
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());

		// docs
		result.andDo(document("에러 코드",
			codeResponseFields("code-response", beneathPath("errorCode"),
				attributes(key("title").value("에러 코드")),
				enumConvertFieldDescriptor(ErrorCode.values())
			)
		));
	}

	private FieldDescriptor[] enumConvertFieldDescriptor(ErrorCode[] errorCodes) {
		return Arrays.stream(errorCodes)
			.map(enumType -> fieldWithPath(enumType.getErrorType()).description(enumType.getDescription()))
			.toArray(FieldDescriptor[]::new);
	}

	public static CodeResponseFieldsSnippet codeResponseFields(String type,
		PayloadSubsectionExtractor<?> subsectionExtractor,
		Map<String, Object> attributes, FieldDescriptor... descriptors) {
		return new CodeResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes
			, true);
	}
}
