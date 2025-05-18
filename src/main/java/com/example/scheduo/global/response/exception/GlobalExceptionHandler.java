package com.example.scheduo.global.response.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler {

	// 커스텀 에러 핸들러
	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex) {
		ResponseStatus status = ex.getResponseStatus();
		log.info("[CustomError] " + status.getStatus() + " - " + status.getMessage());

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
		ErrorResponseDto response = ApiResponse.onFailure(ResponseStatus._INTERNAL_SERVER_ERROR);
		log.info("[Error] " + response.getStatus() + " - " + response.getMessage());

		return ResponseEntity.status(ResponseStatus._INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
	}
}
