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
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_0005", "입력하신 값의 형식이 올바르지 않습니다. 올바른 형식으로 입력해주세요."),

	// 토큰 관련 에러 응답
	NOT_EXIST_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_0001", "토큰이 없습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_0002", "토큰이 유효하지 않습니다."),
	DEFAULT_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "TOKEN_0003", "인증에 실패하였습니다."),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_0004","Refresh Token이 유효하지 않습니다."),
	ALREADY_LOGGED_OUT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_0005","이미 로그아웃 처리 된 Refresh Token입니다."),
	REFRESH_TOKEN_MEMBER_MISMATCH(HttpStatus.UNAUTHORIZED, "TOKEN_0006", "해당 Refresh Token에 대한 권한이 없습니다."),
	EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN_0007", "Refresh Token이 유효하지 않거나 이미 만료되었습니다."),

	// 레디스 관련 에러 응답
	REDIS_SERIALIZE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_0001", "리프레시 토큰 직렬화에 실패했습니다."),
	REDIS_DESERIALIZE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_0002", "리프레시 토큰 역직렬화에 실패했습니다."),

	// 멤버 관련 에러 응답
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_0001", "멤버를 찾을 수 없습니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "MEMBER_0002", "이미 사용중인 닉네임입니다."),

	//캘린더 관련 에러 응답
	CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_0001", "캘린더를 찾을 수 없습니다."),
	MEMBER_NOT_OWNER(HttpStatus.FORBIDDEN, "CALENDAR_0002", "해당 캘린더의 소유자가 아닙니다."),
	MEMBER_ALREADY_INVITED(HttpStatus.CONFLICT, "CALENDAR_0003", "이미 초대된 멤버입니다."),
	MEMBER_ALREADY_PARTICIPANT(HttpStatus.CONFLICT, "CALENDAR_0004", "이미 참여중인 멤버입니다."),
	INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_0005", "초대 정보를 찾을 수 없습니다."),
	INVITATION_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "CALENDAR_0006", "이미 수락된 초대입니다."),
	INVITATION_ALREADY_DECLINED(HttpStatus.CONFLICT, "CALENDAR_0007", "이미 거절된 초대입니다."),
	INVALID_CALENDAR_PARTICIPATION(HttpStatus.FORBIDDEN, "CALENDAR_0008", "참여 정보를 찾을 수 없습니다."),
	MEMBER_NOT_ACCEPT(HttpStatus.FORBIDDEN, "CALENDAR_0009", "해당 멤버의 참여 상태가 ACCEPT가 아닙니다.");

	private final HttpStatus httpStatus;
	private final String status;
	private final String message;
}
