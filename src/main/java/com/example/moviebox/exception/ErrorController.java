package com.example.moviebox.exception;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ErrorController {
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(HttpServletRequest request, BusinessException businessException) {
		return ResponseEntity
			.status(businessException.getHttpStatus())
			.body(ErrorResponse.from(businessException, request.getRequestURI()));
	}

	// 권한 없음
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	protected ErrorResponse handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
		log.error(e.getMessage());
		return ErrorResponse.builder()
			.errorCode(ErrorCode.ACCESS_DENIED)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}

	// 지원하지 않는 Http Method로 요청
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ErrorResponse handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
		log.error(e.getMessage());
		return ErrorResponse.builder()
			.errorCode(ErrorCode.METHOD_NOT_ALLOWED)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}

	// Request Parameter 누락
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ErrorResponse handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
		String errorMessage = "Request Parameter 중 "  + e.getParameterName() + "(이)가 누락되었습니다.";
		log.error(errorMessage);
		return ErrorResponse.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}

	// 지원하지 않는 Request Body의 Content Type
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	protected ErrorResponse handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
		String errorMessage = e.getMessage();
		log.error(errorMessage);
		return ErrorResponse.builder()
			.errorCode(ErrorCode.UNSUPPORTED_MEDIA_TYPE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}

	// @Valid에서 binding error 발생
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ErrorResponse handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
		List<String> params = new ArrayList<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			params.add(error.getField() + ": " + error.getDefaultMessage());
		}
		String errorMessage = String.join(", ", params);
		log.error(errorMessage);
		return ErrorResponse.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}

	// MethodArgumentTypeMismatchException: 형식 오류
	// HttpMessageNotReadableException: 아예 잘못된 형식으로 request 요청
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
	protected ErrorResponse handleInvalidInputValueException(HttpServletRequest request, RuntimeException e) {
		log.error(e.getMessage());
		return ErrorResponse.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
	}
}
