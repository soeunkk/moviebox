package com.example.moviebox.exception;

import com.example.moviebox.common.dto.ApiError;
import com.example.moviebox.common.dto.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
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
	protected ResponseEntity<ApiResponse<?>> handleBusinessException(HttpServletRequest request, BusinessException businessException) {
		ApiError errorContents = ApiError.from(businessException, request.getRequestURI());
		return ResponseEntity
			.status(businessException.getHttpStatus())
			.body(ApiResponse.error(errorContents));
	}

	// 권한 없음
	@ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(AccessDeniedException.class)
	protected ApiResponse<?> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
		log.error(e.getMessage());
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.ACCESS_DENIED)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// 지원하지 않는 Http Method로 요청
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ApiResponse<?> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {
		log.error(e.getMessage());
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.METHOD_NOT_ALLOWED)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// Request Parameter 누락
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ApiResponse<?> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException e) {
		String errorMessage = "Request Parameter 중 "  + e.getParameterName() + "(이)가 누락되었습니다.";
		log.error(errorMessage);
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// 지원하지 않는 Request Body의 Content Type
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	protected ApiResponse<?> handleHttpMediaTypeNotSupportedException(HttpServletRequest request, HttpMediaTypeNotSupportedException e) {
		String errorMessage = e.getMessage();
		log.error(errorMessage);
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.UNSUPPORTED_MEDIA_TYPE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// @Valid에서 binding error 발생
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ApiResponse<?> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException e) {
		List<String> params = new ArrayList<>();
		for (FieldError error : e.getBindingResult().getFieldErrors()) {
			params.add(error.getField() + ": " + error.getDefaultMessage());
		}
		String errorMessage = String.join(", ", params);

		log.error(errorMessage);
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(errorMessage)
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// MethodArgumentTypeMismatchException: 형식 오류
	// HttpMessageNotReadableException: 아예 잘못된 형식으로 request 요청
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
	protected ApiResponse<?> handleInvalidInputValueException(HttpServletRequest request, RuntimeException e) {
		log.error(e.getMessage());
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.INVALID_INPUT_VALUE)
			.errorMessage(e.getMessage())
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}

	// MailException: 메일을 전송할 때 발생한 오류
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(MailException.class)
	protected ApiResponse<?> handleMailException(HttpServletRequest request, MailException e) {
		log.error(e.getMessage());
		ApiError errorContents = ApiError.builder()
			.errorCode(ErrorCode.CAN_NOT_SEND_EMAIL)
			.errorMessage("서버 내부 오류로 인해 사용자에게 메일을 전송할 수 없습니다.")
			.errorOccurrencePath(request.getRequestURI())
			.build();
		return ApiResponse.error(errorContents);
	}
}
