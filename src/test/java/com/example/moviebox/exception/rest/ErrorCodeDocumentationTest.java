package com.example.moviebox.exception.rest;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.moviebox.BaseControllerTest;
import com.example.moviebox.exception.ErrorCode;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ErrorCodeController.class)
public class ErrorCodeDocumentationTest extends BaseControllerTest {
	@Test
	@DisplayName("ErrorCode 문서화")
	public void errorCodeDocumentation() throws Exception {
		// given + when
		ResultActions result = this.mockMvc.perform(get("/errors")
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());

		// docs
		result.andDo(document("error-code",
			codeResponseFields("code-response", beneathPath("errorCode"),
				attributes(key("title").value("error-code")),
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
