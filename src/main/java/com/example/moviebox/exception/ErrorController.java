package com.example.moviebox.exception;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ErrorController {
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> error(HttpServletRequest request, BusinessException businessException) {
		return ResponseEntity
			.status(businessException.getHttpStatus())
			.body(ErrorResponse.from(businessException, request.getRequestURI()));
	}
}
