package com.example.scheduo.global.response.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.status.ResponseStatus;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Hidden
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler {

	// 커스텀 에러 핸들러
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {
		ResponseStatus status = ex.getResponseStatus();
		log.error("[CustomError] {} - {}", status.getStatus(), status.getMessage());

		ErrorResponseDto response = ApiResponse.onFailure(
			status.getHttpStatus().value(),
			status.getStatus(),
			ex.getMessage()
		);
		return ResponseEntity.status(status.getHttpStatus()).body(response);
	}

	// 그 외 처리하지 않은 예외에 대한 핸들러 (선택)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {
		ErrorResponseDto response = ApiResponse.onFailure(ResponseStatus.INTERNAL_SERVER_ERROR);
		log.error("[UnhandledError] {} - {}", response.getStatus(), ex.getMessage());

		return ResponseEntity.status(ResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
	}

	// Validation 에러
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
		ResponseStatus status = ResponseStatus.VALIDATION_ERROR;

		ErrorResponseDto response = ApiResponse.onFailure(status.getHttpStatus().value(), status.getStatus(), message);
		log.error("[ValidationError] {} - {}", response.getStatus(), message);

		return ResponseEntity.status(ResponseStatus.VALIDATION_ERROR.getHttpStatus()).body(response);
	}
}
