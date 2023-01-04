package com.example.moviebox.exception.rest;

import com.example.moviebox.exception.ErrorCode;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/errors")
public class ErrorCodeController {

	@GetMapping
	public ResponseEntity<ErrorCodeView> getErrorCodes() {
		Map<String, String> errorCodes = Arrays.stream(ErrorCode.values())
			.collect(Collectors.toMap(ErrorCode::getErrorType, ErrorCode::getDescription));
		return new ResponseEntity<>(new ErrorCodeView(errorCodes), HttpStatus.OK);
	}
}
