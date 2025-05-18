package com.example.scheduo.global.response.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

@RestController
@RequestMapping("/test/response")
public class ResponseTest {
	@GetMapping("/success1")
	public ApiResponse<String> success1() {
		return ApiResponse.onSuccess("Data");
	}

	@GetMapping("/success2")
	public ApiResponse<String> success2() {
		return ApiResponse.onSuccess("성공 응답 테스트", "testing");
	}

	@GetMapping("/fail1")
	public ApiResponse<String> fail1() {
		throwErrorFail1();
		return ApiResponse.onSuccess("에러1 컨트롤러", null);
	}

	@GetMapping("/fail2")
	public ApiResponse<String> fail2() {
		throwErrorFail2();
		return ApiResponse.onSuccess("에러2 컨트롤러", null);
	}

	private String throwErrorFail1() {
		throw new Error();
	}

	private Object throwErrorFail2() {
		throw new ApiException(ResponseStatus._UNAUTHORIZED);
	}
}
