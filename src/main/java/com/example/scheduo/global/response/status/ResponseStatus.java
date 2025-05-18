package com.example.scheduo.global.response.status;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseStatus {
	//일반적인 응답
	_OK(HttpStatus.OK, "SUCCESS", "OK."),

	// 에러 응답
	_INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_0001", "서버 에러가 발생했습니다."),
	_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_0002", "리소스를 찾을 수 없습니다."),
	_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_0003", "인증에 실패했습니다."),
	_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_0004", "권한이 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String status;
	private final String message;
}
