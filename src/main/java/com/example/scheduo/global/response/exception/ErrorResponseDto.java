package com.example.scheduo.global.response.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;

@Getter
@JsonPropertyOrder({"code", "success", "status", "message"})
public class ErrorResponseDto {
	private final int code;
	private final String status;
	private final boolean success;
	private final String message;

	public ErrorResponseDto(int code, String status, String message) {
		this.code = code;
		this.status = status;
		this.success = false;
		this.message = message;
	}

	public static ErrorResponseDto of(int code, String status, String message) {
		return new ErrorResponseDto(code, status, message);
	}
}
