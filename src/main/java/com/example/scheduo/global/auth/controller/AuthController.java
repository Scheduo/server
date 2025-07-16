package com.example.scheduo.global.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.auth.dto.AuthRequestDto;
import com.example.scheduo.global.auth.dto.AuthResponseDto;
import com.example.scheduo.global.auth.service.AuthService;
import com.example.scheduo.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {
	private final AuthService authService;

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "로그아웃을 합니다.")
	public ApiResponse<?> logout(@RequestMember Member member, @RequestBody AuthRequestDto.RefreshToken request) {
		authService.logout(member.getId(), request.getRefreshToken());
		return ApiResponse.onSuccess();
	}

	@PostMapping("/token")
	@Operation(summary = "AccessToken/RefreshToken 재발급", description = "사용자의 토큰을 재발급 합니다.")
	public ApiResponse<AuthResponseDto.Token> rotateToken(@RequestBody AuthRequestDto.RefreshToken request) {
		AuthResponseDto.Token tokenDto = authService.rotateToken(request.getRefreshToken());
		return ApiResponse.onSuccess(tokenDto);
	}
}
