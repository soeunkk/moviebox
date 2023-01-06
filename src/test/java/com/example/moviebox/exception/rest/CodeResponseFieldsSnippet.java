package com.example.moviebox.exception.rest;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.payload.AbstractFieldsSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

public class CodeResponseFieldsSnippet extends AbstractFieldsSnippet {
	public CodeResponseFieldsSnippet(String type, PayloadSubsectionExtractor<?> subsectionExtractor,
		List<FieldDescriptor> descriptors, Map<String, Object> attributes,
		boolean ignoreUndocumentedFields) {
		super(type, descriptors, attributes, ignoreUndocumentedFields, subsectionExtractor);
	}

	@Override
	protected MediaType getContentType(Operation operation) {
		return operation.getResponse().getHeaders().getContentType();
	}

	@Override
	protected byte[] getContent(Operation operation) {
		return operation.getResponse().getContent();
	}
}
