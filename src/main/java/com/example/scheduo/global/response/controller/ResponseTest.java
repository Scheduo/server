package com.example.scheduo.global.response.controller;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/response")
public class ResponseTest {
	@GetMapping("/success")
	public ResponseEntity<SuccessResponse<Object>> success() {
		SuccessResponse<Object> res = new SuccessResponse<>(
			200,
			"성공 응답 테스트",
			Collections.emptyMap() // 빈 객체
		);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/fail")
	public ResponseEntity<ErrorResponse> fail() {
		ErrorResponse res = new ErrorResponse(
			404,
			"COMMON_0001",
			"실패 응답 테스트."
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
	}
}
