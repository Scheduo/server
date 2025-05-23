package com.example.scheduo.global.response.status;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseStatus {
	//일반적인 응답
	OK(HttpStatus.OK, "SUCCESS", "OK."),

	// 에러 응답
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_0001", "서버 에러가 발생했습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_0002", "리소스를 찾을 수 없습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_0003", "인증에 실패했습니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_0004", "권한이 없습니다."),

	// 멤버 관련 에러 응답
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_0001", "멤버를 찾을 수 없습니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER_0002", "이미 사용중인 닉네임입니다."),

	//캘린더 관련 에러 응답
	CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_0001", "캘린더를 찾을 수 없습니다."),
	MEMBER_NOT_OWNER(HttpStatus.FORBIDDEN, "CALENDAR_0002", "해당 캘린더의 소유자가 아닙니다."),
	MEMBER_ALREADY_INVITED(HttpStatus.CONFLICT, "CALENDAR_0003", "이미 초대된 멤버입니다."),
	MEMBER_ALREADY_PARTICIPANT(HttpStatus.CONFLICT, "CALENDAR_0004", "이미 참여중인 멤버입니다."),
	;

	private final HttpStatus httpStatus;
	private final String status;
	private final String message;
}
