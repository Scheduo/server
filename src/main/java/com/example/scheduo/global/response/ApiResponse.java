package com.example.scheduo.global.response;

import com.example.scheduo.global.response.exception.ErrorResponseDto;
import com.example.scheduo.global.response.status.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonPropertyOrder({"code", "success", "message", "data"})
public class ApiResponse<T> {
	private final int code;
	private final boolean success;
	private final String message;
	private final T data;

	// 성공한 경우 응답 생성
	public static <T> ApiResponse<T> onSuccess(T result) {
		return new ApiResponse<>(ResponseStatus._OK.getHttpStatus().value(), true, "OK.", result);
	}

	public static <T> ApiResponse<T> onSuccess(String message, T result) {
		return new ApiResponse<>(ResponseStatus._OK.getHttpStatus().value(), true, message, result);
	}

	// 실패 응답 생성
	public static ErrorResponseDto onFailure(ResponseStatus status) {
		return ErrorResponseDto.of(status.getHttpStatus().value(), status.getStatus(), status.getMessage());
	}

	// 실패 응답 생성 (커스텀 메시지, 상태코드 등)
	public static ErrorResponseDto onFailure(int code, String status, String message) {
		return ErrorResponseDto.of(code, status, message);
	}
}
