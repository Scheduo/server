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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/logout")
	public ApiResponse<?> logout(@RequestMember Member member, @RequestBody AuthRequestDto.RefreshToken request) {
		authService.logout(member.getId(), request.getRefreshToken());
		return ApiResponse.onSuccess();
	}

	@PostMapping("/token")
	public ApiResponse<AuthResponseDto.Token> rotateToken(@RequestBody AuthRequestDto.RefreshToken request) {
		AuthResponseDto.Token tokenDto = authService.rotateToken(request.getRefreshToken());
		return ApiResponse.onSuccess(tokenDto);
	}
}
