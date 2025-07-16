package com.example.scheduo.global.response.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.scheduo.global.logger.service.LoggingService;
import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.status.ResponseStatus;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Hidden
@RestControllerAdvice(annotations = {RestController.class})
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private final LoggingService loggingService;

	// 커스텀 에러 핸들러
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex, HttpServletRequest request) {
		ResponseStatus status = ex.getResponseStatus();

		loggingService.logError(request, status.getStatus(), ex.getMessage());

		ErrorResponseDto response = ApiResponse.onFailure(
			status.getHttpStatus().value(),
			status.getStatus(),
			ex.getMessage()
		);
		return ResponseEntity.status(status.getHttpStatus()).body(response);
	}

	// 그 외 처리하지 않은 예외에 대한 핸들러 (선택)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleException(Exception ex, HttpServletRequest request) {
		ErrorResponseDto response = ApiResponse.onFailure(ResponseStatus.INTERNAL_SERVER_ERROR);
		loggingService.logError(request, response.getStatus(), ex.getMessage());

		return ResponseEntity.status(ResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
	}

	// Validation 에러
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		ResponseStatus status = ResponseStatus.VALIDATION_ERROR;

		loggingService.logError(request, status.getStatus(), message);

		ErrorResponseDto response = ApiResponse.onFailure(status.getHttpStatus().value(), status.getStatus(), message);

		return ResponseEntity.status(ResponseStatus.VALIDATION_ERROR.getHttpStatus()).body(response);
	}
}
