package com.example.moviebox.exception.rest;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorCodeView {
	private Map<String, String> errorCode;
}
